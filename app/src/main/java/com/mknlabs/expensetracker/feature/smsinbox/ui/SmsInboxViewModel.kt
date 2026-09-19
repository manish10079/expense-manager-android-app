package com.mknlabs.expensetracker.feature.smsinbox.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.domain.mapper.getStartOfDayTimestamp
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.DetectedSmsNotification
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxFilter
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxStatus
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.DeleteSmsDetectionsUseCase
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.FileDetectionResult
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.FileSmsDetectionAsTransactionUseCase
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.GetSmsDetectionsUseCase
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.GetSmsInboxUnreadCountUseCase
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.MarkSmsDetectionsViewedUseCase
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.ObserveSmsInboxUseCase
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.sms.SmsConfidence
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.getDayName
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * One detection, already shaped for display.
 *
 * Everything the card needs is resolved here — formatted amount, category name, time
 * text — so the composable stays a pure function of its inputs, which is what lets the
 * Content side of the Route/Content split stay previewable.
 */
@Immutable
data class SmsInboxItemUi(
    val id: String,
    val title: String,
    val amountText: String,
    val isIncome: Boolean,
    val timeText: String,
    val messagePreview: String,
    val categoryLabel: UiText,
    val confidence: SmsConfidence,
    val isSelected: Boolean,
    /**
     * True while the detection is still waiting for the user to look at it.
     *
     * The card shows no status *word* for this: the inbox is a list of things to act on,
     * and it is far more useful for a card to say what it came from than which stage of a
     * private lifecycle it happens to be in. The flag itself is what drives "seen as soon
     * as it is on screen", so it stays in the model even though nothing renders it.
     */
    val isUnread: Boolean,
    /** False once a detection is filed or ignored — there is nothing left to add. */
    val isActionable: Boolean,
    /** True once the user has filed this detection, so tapping it opens what it made. */
    val isFiled: Boolean,
    val linkedTransactionId: String?,
    // Raw values, kept alongside the formatted ones so the Add Transaction screen can be
    // prefilled without trying to reverse-format a display string.
    val amountMinor: Long,
    /** The amount as the Add screen's field takes it: plain digits, no currency symbol. */
    val amountInput: String,
    val note: String,
    val categoryId: Int?,
    val transactionTypeId: Int
)

/**
 * One row of the inbox list.
 *
 * The date separators are real list items rather than a decoration inside a card: a header
 * that is an item scrolls with the content, keeps its own key, and can never be detached
 * from the rows it introduces. This mirrors how the transactions feed is built.
 */
@Immutable
sealed interface SmsInboxListItemUi {

    val id: String

    /** A day separator: the day on the start edge, the date on the end edge. */
    @Immutable
    data class DateHeader(
        override val id: String,
        val dayLabel: UiText,
        val dateLabel: String
    ) : SmsInboxListItemUi

    /** A detection, ready to render. */
    @Immutable
    data class Detection(
        val item: SmsInboxItemUi
    ) : SmsInboxListItemUi {
        override val id: String get() = item.id
    }
}

/**
 * An in-progress correction of a detection, before it becomes a transaction.
 *
 * Amount edits are deliberate rather than incidental: the parser reads the amount from
 * bank prose, so when it mis-reads one the user must be able to fix it before it lands
 * in their ledger.
 */
@Immutable
data class SmsEditorState(
    val id: String,
    val title: String,
    val amountText: String,
    val note: String,
    val categoryId: Int?
)

/** Everything the inbox screen renders, in one immutable snapshot. */
@Immutable
data class SmsInboxUiState(
    /** The render list, date separators included. */
    val listItems: List<SmsInboxListItemUi> = emptyList(),
    val filter: SmsInboxFilter = SmsInboxFilter.ALL,
    val searchQuery: String = "",
    val unreadCount: Int = 0,
    val selectedIds: Set<String> = emptySet(),
    val canLoadMore: Boolean = false,
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    /** Non-empty while a destructive action awaits confirmation. */
    val pendingDeleteIds: Set<String> = emptySet(),
    /** Non-null while a detection is being corrected before it is added. */
    val editor: SmsEditorState? = null,
    /**
     * The row a notification tap asked for. Highlighted and scrolled to on arrival,
     * then released — so an old notification never leaves a row looking selected.
     */
    val focusedId: String? = null,
    /**
     * Rows a swipe has taken off the list but not yet out of the database. Their delete
     * lands when the Undo offer expires — which is what makes Undo a cancel rather than
     * a restore, and the inbox has no restore path.
     */
    val pendingRemovalIds: Set<String> = emptySet(),
    /**
     * Detections that arrived *while this screen was open*. Each one plays its entrance
     * animation once and is then consumed, so opening the inbox never re-animates the
     * rows that were already there.
     */
    val arrivalIds: Set<String> = emptySet()
) {
    /** The detections on screen, in order — the render list without its date separators. */
    val detections: List<SmsInboxItemUi>
        get() = listItems
            .filterIsInstance<SmsInboxListItemUi.Detection>()
            .map { it.item }

    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
    val hasItems: Boolean get() = listItems.isNotEmpty()

    /** Nothing to show, and not because it is still loading. */
    val isEmpty: Boolean get() = listItems.isEmpty() && !isLoading
}

/** One-shot things the screen reacts to but never renders from. */
sealed interface SmsInboxEvent {

    /** A snackbar message (added, ignored, marked read…). */
    data class Message(val text: UiText) : SmsInboxEvent

    /**
     * Rows were swiped away and [count] of them are waiting on the Undo offer. Carried
     * as its own event because it is the only message that has an action attached.
     */
    data class Deleted(val count: Int) : SmsInboxEvent

    /**
     * A live detection was tapped: review it in the Add Transaction screen, prefilled.
     *
     * The screen decides it, not the composable, because which tap means what depends on
     * the detection's own state — and the inbox files nothing without the user pressing
     * Add there.
     */
    data class ReviewInAddTransaction(val item: SmsInboxItemUi) : SmsInboxEvent

    /** An already-filed detection was tapped: open the transaction it created. */
    data class OpenFiledTransaction(val transactionId: String) : SmsInboxEvent
}

/**
 * ViewModel for the detection inbox.
 *
 * Reads go through the inbox use cases and nothing else — the screen never reaches for
 * a DAO or a repository — and filing a detection as a transaction is delegated to
 * [FileSmsDetectionAsTransactionUseCase], which owns the duplicate guard.
 *
 * Paging is a growing query limit rather than an appended list, so the list stays one
 * reactive stream from Room: a detection filed from a notification while the screen is
 * open appears without any manual reconciliation, and a purge that removes rows cannot
 * leave stale entries behind.
 */
@HiltViewModel
class SmsInboxViewModel @Inject constructor(
    private val observeInbox: ObserveSmsInboxUseCase,
    private val getUnreadCount: GetSmsInboxUnreadCountUseCase,
    private val markViewed: MarkSmsDetectionsViewedUseCase,
    private val deleteDetections: DeleteSmsDetectionsUseCase,
    private val fileDetection: FileSmsDetectionAsTransactionUseCase,
    private val getDetections: GetSmsDetectionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsInboxUiState())
    val uiState: StateFlow<SmsInboxUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SmsInboxEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<SmsInboxEvent> = _events.asSharedFlow()

    private val query = MutableStateFlow(InboxQuery())
    private val selectedIds = MutableStateFlow(emptySet<String>())

    /** Rows hidden by a swipe while their delete waits out the Undo window. */
    private val pendingRemovalIds = MutableStateFlow(emptySet<String>())

    /**
     * The newest SMS timestamp already projected. A detection strictly newer than this is
     * what "just arrived" means — comparing ids instead would mis-flag older rows as
     * arrivals every time the page grows or the filter changes.
     */
    private var latestKnownDetectedAt: Long? = null

    /** The single Undo window for whatever is currently pending. */
    private var removalCommitJob: Job? = null

    /**
     * Currency, amount formatting and category names, handed over by the Route — the
     * same hand-off the Budget screen uses. Kept as a flow so a change re-renders the
     * rows already on screen instead of waiting for the next database emission.
     */
    private val display = MutableStateFlow(DisplayContext())

    init {
        viewModelScope.launch {
            combine(
                query.flatMapLatest { current ->
                    observeInbox(
                        filter = current.filter,
                        searchQuery = current.searchQuery.takeIf { it.isNotBlank() },
                        limit = current.limit
                    )
                },
                selectedIds,
                display,
                pendingRemovalIds
            ) { detections, selection, context, pendingRemoval ->
                // An arrival is a detection newer than anything seen so far. The very
                // first projection establishes the baseline instead of flagging the whole
                // first page, and paging in older rows can never qualify.
                val arrivalIds = latestKnownDetectedAt
                    ?.let { previous -> detections.filter { it.detectedAt > previous } }
                    .orEmpty()
                    .map { it.id }
                    .filterNot { it in pendingRemoval }
                    .toSet()
                latestKnownDetectedAt = listOfNotNull(
                    latestKnownDetectedAt,
                    detections.maxOfOrNull { it.detectedAt }
                ).maxOrNull()

                Projection(
                    detections = detections,
                    selection = selection,
                    context = context,
                    limit = query.value.limit,
                    pendingRemoval = pendingRemoval,
                    arrivalIds = arrivalIds
                )
            }.collect { projection ->
                val listItems = buildListItems(
                    // A row mid-Undo-window is hidden from the list, not gone from
                    // storage: it comes straight back if Undo is tapped.
                    detections = projection.detections.filter { it.id !in projection.pendingRemoval },
                    context = projection.context,
                    selection = projection.selection
                )
                _uiState.update { state ->
                    state.copy(
                        listItems = listItems,
                        selectedIds = projection.selection,
                        pendingRemovalIds = projection.pendingRemoval,
                        // An arrival still waiting on the Undo window is not on screen.
                        arrivalIds = projection.arrivalIds - projection.pendingRemoval,
                        canLoadMore = projection.detections.size >= projection.limit,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            getUnreadCount().collect { unread ->
                _uiState.update { it.copy(unreadCount = unread) }
            }
        }
    }

    fun updateDisplayContext(
        categories: List<CategoryType>,
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences,
        dateFormatPattern: String
    ) {
        display.value = DisplayContext(
            categoryNames = categories.associate { it.id to it.name },
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern
        )
    }

    fun onFilterSelected(filter: SmsInboxFilter) {
        if (filter == _uiState.value.filter) return
        _uiState.update { it.copy(filter = filter) }
        query.update { it.copy(filter = filter, limit = DEFAULT_PAGE_SIZE) }
        onClearSelection()
    }

    fun onSearchQueryChanged(searchQuery: String) {
        if (searchQuery == _uiState.value.searchQuery) return
        _uiState.update { it.copy(searchQuery = searchQuery) }
        // A new search restarts at one page; otherwise a narrow query would inherit a
        // limit widened by an earlier scroll and fetch far more than a page.
        query.update { it.copy(searchQuery = searchQuery, limit = DEFAULT_PAGE_SIZE) }
    }

    fun onLoadMore() {
        val state = _uiState.value
        if (!state.canLoadMore || state.isLoading) return
        query.update { it.copy(limit = it.limit + DEFAULT_PAGE_SIZE) }
    }

    fun onToggleSelection(id: String) {
        val next = selectedIds.value.let { if (id in it) it - id else it + id }
        selectedIds.value = next
        // Selecting is also a look, so the row stops counting as unread.
        if (id in next) viewModelScope.launch { markViewed(listOf(id)) }
    }

    /**
     * Selects everything on screen in one go.
     *
     * Bulk actions here are destructive-only ("select all, then delete"), so this exists to
     * save the user a long-press per row rather than to offer a shortcut past reading them.
     */
    fun onSelectAll() {
        val ids = _uiState.value.detections.map { it.id }
        if (ids.isEmpty()) return
        selectedIds.value = ids.toSet()
        viewModelScope.launch { markViewed(ids) }
    }

    fun onClearSelection() {
        selectedIds.value = emptySet()
    }

    /**
     * A card was tapped.
     *
     * The inbox itself no longer files anything: a detection that still needs a decision is
     * handed to the Add Transaction screen, prefilled with the parsed facts, where the user
     * can correct it and press Add. A detection that was already filed is a shortcut to the
     * transaction it creates, and one that was deliberately ignored has nothing left to
     * offer — so the tap is simply a look.
     */
    fun onDetectionOpened(item: SmsInboxItemUi) {
        viewModelScope.launch {
            markViewed(listOf(item.id))
            when {
                item.isFiled -> item.linkedTransactionId?.let { transactionId ->
                    _events.tryEmit(SmsInboxEvent.OpenFiledTransaction(transactionId))
                }

                item.isActionable -> _events.tryEmit(SmsInboxEvent.ReviewInAddTransaction(item))

                else -> Unit
            }
        }
    }

    /**
     * Cards the user has actually been shown are read.
     *
     * Driven by what is visible in the list, not by what was fetched: a page loaded out of
     * sight is not something the user has seen. Marking is idempotent and only ever moves
     * NEW rows, so a settled list issues no further writes.
     */
    fun onDetectionsVisible(ids: List<String>) {
        if (ids.isEmpty()) return
        val visible = ids.toSet()
        val unread = _uiState.value.detections
            .filter { it.isUnread && it.id in visible }
            .map { it.id }
        if (unread.isEmpty()) return
        viewModelScope.launch { markViewed(unread) }
    }

    /**
     * A notification was tapped (or its Edit action used) for [detectionId]: bring that
     * row into view no matter what the user left the screen filtered to, because a tap
     * on a specific detection is a request to see *that* detection.
     *
     * @param openEditor the Edit action — land with the correction dialog already open.
     */
    fun onFocusRequested(detectionId: String, openEditor: Boolean) {
        viewModelScope.launch {
            if (_uiState.value.filter != SmsInboxFilter.ALL || _uiState.value.searchQuery.isNotBlank()) {
                _uiState.update { it.copy(filter = SmsInboxFilter.ALL, searchQuery = "") }
                query.update {
                    it.copy(filter = SmsInboxFilter.ALL, searchQuery = "", limit = DEFAULT_PAGE_SIZE)
                }
            }

            val detection = getDetections(detectionId)
            if (detection == null) {
                // Retention can have purged the row since the card was posted; say so
                // rather than silently opening an inbox that does not contain it.
                emitMessage(UiText.res(R.string.msg_sms_inbox_gone))
                return@launch
            }

            _uiState.update { it.copy(focusedId = detectionId) }
            markViewed(listOf(detectionId))
            if (openEditor) onOpenEditor(detection.toUi(display.value, isSelected = false))
        }
    }

    /**
     * One arrival's entrance has been dealt with — played where the user could see it, or
     * skipped because the list was scrolled away from the top. Either way it is one-shot.
     */
    fun onArrivalHandled(id: String) {
        if (id !in _uiState.value.arrivalIds) return
        _uiState.update { it.copy(arrivalIds = it.arrivalIds - id) }
    }

    /** Releases the focus highlight once the list has had time to scroll to the row. */
    fun onFocusHighlightFinished() {
        if (_uiState.value.focusedId == null) return
        _uiState.update { it.copy(focusedId = null) }
    }

    /** Files one detection as a real transaction. */
    fun onAddDetection(id: String, allowOverride: Boolean = false) {
        viewModelScope.launch { file(listOf(id), allowOverride) }
    }

    /**
     * Bulk "Add All". Files the selection plus every actionable row on screen, since
     * a bulk action on a filtered list should mean "everything I am looking at".
     */
    fun onAddAllActionable() {
        val ids = buildList {
            addAll(selectedIds.value)
            addAll(_uiState.value.detections.filter { it.isActionable }.map { it.id })
        }.distinct()
        if (ids.isEmpty()) return
        viewModelScope.launch { file(ids, allowOverride = false) }
    }

    /**
     * Opens the "Edit before add" step. The amount is prefillable but the parsed value
     * is what is shown, so an unedited confirm files exactly what the bank said.
     */
    fun onOpenEditor(item: SmsInboxItemUi) {
        _uiState.update { state ->
            state.copy(
                editor = SmsEditorState(
                    id = item.id,
                    title = item.title,
                    amountText = item.amountInput,
                    note = "",
                    categoryId = item.categoryId
                )
            )
        }
    }

    fun onEditorAmountChanged(value: String) {
        _uiState.update { state ->
            state.editor?.let { state.copy(editor = it.copy(amountText = value)) } ?: state
        }
    }

    fun onEditorNoteChanged(value: String) {
        _uiState.update { state ->
            state.editor?.let { state.copy(editor = it.copy(note = value)) } ?: state
        }
    }

    fun onEditorCategoryChanged(categoryId: Int) {
        _uiState.update { state ->
            state.editor?.let { state.copy(editor = it.copy(categoryId = categoryId)) } ?: state
        }
    }

    fun onCancelEditor() {
        _uiState.update { it.copy(editor = null) }
    }

    fun onConfirmEditor() {
        val editor = _uiState.value.editor ?: return
        val amountMinor = editor.amountText.replace(",", "").trim().toDoubleOrNull()?.toMinorUnits()
        if (amountMinor == null || amountMinor <= 0L) {
            emitMessage(UiText.res(R.string.msg_sms_inbox_invalid_amount))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, editor = null) }
            val result = fileDetection(
                detectionId = editor.id,
                categoryId = editor.categoryId,
                note = editor.note.trim(),
                amountMinorOverride = amountMinor
            )
            _uiState.update { it.copy(isWorking = false) }
            emitMessage(
                when (result) {
                    is FileDetectionResult.Filed -> UiText.res(R.string.msg_sms_inbox_added, 1)
                    is FileDetectionResult.AlreadyAdded -> UiText.res(R.string.msg_sms_inbox_already_added)
                    is FileDetectionResult.NotFound -> UiText.res(R.string.msg_sms_inbox_gone)
                }
            )
        }
    }

    /** Destructive, so it goes through a confirmation step first. */
    fun onRequestDelete(id: String) {
        _uiState.update { it.copy(pendingDeleteIds = setOf(id)) }
    }

    fun onRequestDeleteSelected() {
        if (selectedIds.value.isEmpty()) return
        _uiState.update { it.copy(pendingDeleteIds = selectedIds.value) }
    }

    fun onCancelDelete() {
        _uiState.update { it.copy(pendingDeleteIds = emptySet()) }
    }

    fun onConfirmDelete() {
        val ids = _uiState.value.pendingDeleteIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, pendingDeleteIds = emptySet()) }
            deleteDetections(ids)
            onClearSelection()
            _uiState.update { it.copy(isWorking = false) }
            emitMessage(UiText.res(R.string.msg_sms_inbox_deleted))
        }
    }

    /**
     * A row was swiped away. It leaves the list at once, but the database write waits out
     * the Undo window — Undo can then simply call the whole thing off, and a mis-swipe
     * costs nothing.
     */
    fun onSwipeDelete(id: String) {
        markForRemoval(listOf(id))
    }

    /**
     * Clears everything the user is looking at.
     *
     * Like the bulk add, "all" means the rows currently on screen: a filtered list should
     * clear what the filter shows rather than everything that happens to be stored. The
     * same Undo window as a swipe applies, so clearing the inbox by mistake costs one tap
     * to reverse instead of a lost list.
     */
    fun onClearAllVisible() {
        markForRemoval(_uiState.value.detections.map { it.id })
    }

    /**
     * Takes [ids] off the list and schedules their delete.
     *
     * Repeated calls inside one window join the same window, so a run of swipes (or a
     * swipe followed by Clear all) is a single Undo rather than a queue of snackbars.
     */
    private fun markForRemoval(ids: List<String>) {
        if (ids.isEmpty()) return
        val pending = pendingRemovalIds.value + ids
        pendingRemovalIds.value = pending
        // Rows on their way out should not stay selected behind the scenes.
        selectedIds.value = selectedIds.value - ids.toSet()

        scheduleRemovalCommit()
        _events.tryEmit(SmsInboxEvent.Deleted(pending.size))
    }

    /** Undo from the snackbar: the rows return and are never deleted. */
    fun onUndoDelete() {
        removalCommitJob?.cancel()
        removalCommitJob = null
        pendingRemovalIds.value = emptySet()
    }

    /**
     * Commits the waiting deletes once the Undo window closes.
     *
     * Driven from here rather than from the snackbar's result so the write cannot be
     * lost: the window is [UNDO_WINDOW_MS], the same as the snackbar's
     * `SnackbarDuration.Long`, so Undo can never be offered after the delete has landed.
     */
    private fun scheduleRemovalCommit() {
        removalCommitJob?.cancel()
        removalCommitJob = viewModelScope.launch {
            delay(UNDO_WINDOW_MS)
            val ids = pendingRemovalIds.value.toList()
            if (ids.isEmpty()) return@launch
            // Deletes the rows and clears any notification still announcing them.
            deleteDetections(ids)
            pendingRemovalIds.value = emptySet()
        }
    }

    private suspend fun file(ids: List<String>, allowOverride: Boolean) {
        _uiState.update { it.copy(isWorking = true) }
        var filed = 0
        var alreadyAdded = 0
        var missing = 0

        for (id in ids) {
            when (fileDetection(id, allowOverride = allowOverride)) {
                is FileDetectionResult.Filed -> filed++
                is FileDetectionResult.AlreadyAdded -> alreadyAdded++
                is FileDetectionResult.NotFound -> missing++
            }
        }

        _uiState.update { it.copy(isWorking = false) }
        onClearSelection()

        // Report what actually happened. A detection that was already in the ledger is
        // never presented as newly added, and a mixed batch is described as mixed.
        when {
            filed > 0 && (alreadyAdded > 0 || missing > 0) ->
                emitMessage(UiText.res(R.string.msg_sms_inbox_added_some, filed, alreadyAdded))
            filed > 0 -> emitMessage(UiText.res(R.string.msg_sms_inbox_added, filed))
            alreadyAdded > 0 -> emitMessage(UiText.res(R.string.msg_sms_inbox_already_added))
            else -> emitMessage(UiText.res(R.string.msg_sms_inbox_gone))
        }
    }

    private fun emitMessage(text: UiText) {
        _events.tryEmit(SmsInboxEvent.Message(text))
    }

    /**
     * Builds the render list: detections in arrival order, with a day separator whenever
     * the day changes.
     *
     * Comparing against the previous row is enough because the query is already ordered
     * newest first — and it is what keeps a page boundary from producing a second header
     * for a day that is already open.
     */
    private fun buildListItems(
        detections: List<DetectedSmsNotification>,
        context: DisplayContext,
        selection: Set<String>,
        now: Long = System.currentTimeMillis()
    ): List<SmsInboxListItemUi> = buildList {
        var currentDayStart: Long? = null
        detections.forEach { detection ->
            val dayStart = getStartOfDayTimestamp(detection.detectedAt)
            if (dayStart != currentDayStart) {
                currentDayStart = dayStart
                add(
                    SmsInboxListItemUi.DateHeader(
                        id = "sms_day_$dayStart",
                        dayLabel = dayLabel(dayStart, now),
                        // The date follows the user's own setting, like every other date in
                        // the app — only the day on the start edge is a fixed word.
                        dateLabel = formatDate(dayStart, context.dateFormatPattern)
                    )
                )
            }
            add(SmsInboxListItemUi.Detection(detection.toUi(context, detection.id in selection)))
        }
    }

    /** "Today" and "Yesterday" are labels, so they are resources; anything older is a date. */
    private fun dayLabel(dayStart: Long, now: Long): UiText {
        val daysAgo = (getStartOfDayTimestamp(now) - dayStart) / MILLIS_PER_DAY
        return when (daysAgo) {
            0L -> UiText.res(R.string.label_today)
            1L -> UiText.res(R.string.label_yesterday)
            else -> UiText.DynamicString(getDayName(dayStart))
        }
    }

    /** Plain digits for the editor field — the same shape the Add screen's field takes. */
    private fun editableAmount(amountMinor: Long): String =
        java.math.BigDecimal.valueOf(amountMinor.toMajorUnits()).stripTrailingZeros().toPlainString()

    /** Active query parameters. A data class so a redundant update is a no-op. */
    private data class InboxQuery(
        val filter: SmsInboxFilter = SmsInboxFilter.ALL,
        val searchQuery: String = "",
        val limit: Int = DEFAULT_PAGE_SIZE
    )

    private data class DisplayContext(
        val categoryNames: Map<Int, String> = emptyMap(),
        val currencyId: Int = DEFAULT_CURRENCY_ID,
        val amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
        val dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN
    )

    private data class Projection(
        val detections: List<DetectedSmsNotification>,
        val selection: Set<String>,
        val context: DisplayContext,
        val limit: Int,
        val pendingRemoval: Set<String>,
        val arrivalIds: Set<String>
    )

    private fun DetectedSmsNotification.toUi(
        context: DisplayContext,
        isSelected: Boolean
    ): SmsInboxItemUi = SmsInboxItemUi(
        id = id,
        // The merchant is the friendliest heading; the bank header is the fallback.
        title = merchantName?.takeIf { it.isNotBlank() } ?: sender,
        amountText = formatCurrencyValue(
            amount = amountMinor.toMajorUnits(),
            currencyId = context.currencyId,
            amountFormatPreferences = context.amountFormatPreferences
        ),
        isIncome = isIncome,
        timeText = TIME_FORMAT.format(Date(detectedAt)),
        // Collapse the bank's line breaks and padding so the card shows one tidy line.
        messagePreview = messageBody.replace(WHITESPACE, " ").trim(),
        categoryLabel = UiText.res(
            R.string.label_sms_inbox_category,
            suggestedCategoryId?.let { context.categoryNames[it] } ?: ""
        ),
        confidence = confidence,
        isSelected = isSelected,
        isUnread = status == SmsInboxStatus.NEW,
        isActionable = status.isActionable,
        isFiled = isFiled,
        linkedTransactionId = linkedTransactionId,
        amountMinor = amountMinor,
        amountInput = editableAmount(amountMinor),
        note = messageBody,
        categoryId = suggestedCategoryId,
        transactionTypeId = transactionTypeId
    )

    private companion object {
        const val DEFAULT_PAGE_SIZE = 30

        /**
         * How long a swiped row can be brought back. Kept equal to the snackbar's
         * `SnackbarDuration.Long`, so the two never disagree about whether Undo is
         * still on offer.
         */
        const val UNDO_WINDOW_MS = 10_000L

        /** Used to compare days when naming a date separator. */
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

        /** Detection times are shown to the minute; the year adds nothing at a glance. */
        val TIME_FORMAT = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())

        val WHITESPACE = Regex("""\s+""")
    }
}
