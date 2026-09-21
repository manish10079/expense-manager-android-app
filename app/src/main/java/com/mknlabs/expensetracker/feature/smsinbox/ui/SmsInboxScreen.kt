package com.mknlabs.expensetracker.feature.smsinbox.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.PlaylistAddCheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.Context
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxFilter
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.sms.SmsConfidence
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.TransactionDateHeader
import com.mknlabs.expensetracker.ui.horizontalSwipe
import com.mknlabs.expensetracker.ui.theme.BadgeExpenseRed
import com.mknlabs.expensetracker.ui.theme.BadgeIncomeGreen
import com.mknlabs.expensetracker.ui.theme.BadgeOnColor
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.transparent
import com.mknlabs.expensetracker.utils.UiText
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Route: owns the ViewModel, hands the display context over once, surfaces one-shot
 * events as snackbars and forwards every user intent.
 *
 * Pure wiring — no layout decisions live here (GEMINI §2).
 */
@Composable
fun SmsInboxRoute(
    categories: List<CategoryType>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    /** The user's own date format, which the day separators follow. */
    dateFormatPattern: String,
    /** The user's own time format, which each row's own clock follows. */
    timeFormat: String,
    onBackClick: () -> Unit,
    /**
     * A live detection was tapped: open the Add Transaction screen prefilled with it. The
     * detection is only filed — and only leaves this list — when the user presses Add there.
     */
    onReviewInAddTransaction: (SmsInboxItemUi) -> Unit = {},
    /** An already-filed detection was tapped: open the transaction it created. */
    onOpenFiledTransaction: (String) -> Unit = {},
    /**
     * Set when the screen was opened from a detected-SMS notification: the row the
     * card was about, and whether its Edit action asked for the correction dialog.
     */
    focusDetectionId: String? = null,
    focusOpenEditor: Boolean = false,
    /** Releases the request once the ViewModel has taken it, so it fires only once. */
    onFocusConsumed: () -> Unit = {},
    viewModel: SmsInboxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // The ViewModel formats amounts and resolves category names, so it needs the same
    // currency + formatting context the rest of the app renders with.
    LaunchedEffect(categories, currencyId, amountFormatPreferences, dateFormatPattern, timeFormat) {
        viewModel.updateDisplayContext(
            categories = categories,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat
        )
    }

    // Opening from a notification brings one row into view. Keyed on the id (and the
    // edit flag) so a second tap on another notification re-focuses, while a plain
    // recomposition does not.
    LaunchedEffect(focusDetectionId, focusOpenEditor) {
        if (focusDetectionId != null) {
            viewModel.onFocusRequested(focusDetectionId, focusOpenEditor)
            // Handed over: without this, returning to the inbox later would re-focus the
            // row an old notification pointed at.
            onFocusConsumed()
        }
    }

    // The highlight is an arrival cue, not a state: it releases itself once the list
    // has had time to scroll, so the row never lingers looking selected.
    LaunchedEffect(uiState.focusedId) {
        if (uiState.focusedId != null) {
            delay(FOCUS_HIGHLIGHT_MS)
            viewModel.onFocusHighlightFinished()
        }
    }

    // Snackbar text is resolved here, where a Context exists — the ViewModel only ever
    // hands over a resource id. `UiText.asString()` is @Composable, so it cannot be
    // used from inside the collecting coroutine; the Context is the way to render a
    // resource id off the composition thread.
    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            when (event) {
                is SmsInboxEvent.Message -> snackbarHostState.showSnackbar(context.resolve(event.text))

                // The only message with an action: a swipe deleted something, and the
                // offer to bring it back lives exactly as long as this snackbar.
                is SmsInboxEvent.Deleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.msg_sms_inbox_swiped_away, event.count),
                        actionLabel = context.getString(R.string.label_undo),
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoDelete()
                }

                // Leaving this screen is the ViewModel's whole job here: the composable
                // only knows where to go, never what a tap on a detection means.
                is SmsInboxEvent.ReviewInAddTransaction -> onReviewInAddTransaction(event.item)

                is SmsInboxEvent.OpenFiledTransaction -> onOpenFiledTransaction(event.transactionId)
            }
        }
    }

    SmsInboxContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        availableCategories = categories,
        onFilterSelected = viewModel::onFilterSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onLoadMore = viewModel::onLoadMore,
        onDetectionOpened = viewModel::onDetectionOpened,
        onDetectionsVisible = viewModel::onDetectionsVisible,
        onToggleSelection = viewModel::onToggleSelection,
        onSelectAll = viewModel::onSelectAll,
        onClearSelection = viewModel::onClearSelection,
        onAdd = viewModel::onAddDetection,
        onAddAll = viewModel::onRequestAddAll,
        onCancelAddAll = viewModel::onCancelAddAll,
        onConfirmAddAll = viewModel::onConfirmAddAll,
        onEditBeforeAdd = viewModel::onOpenEditor,
        onRequestDelete = viewModel::onRequestDelete,
        onRequestDeleteSelected = viewModel::onRequestDeleteSelected,
        onSwipeDelete = viewModel::onSwipeDelete,
        onArrivalHandled = viewModel::onArrivalHandled,
        onClearAll = viewModel::onClearAllVisible,
        onCancelDelete = viewModel::onCancelDelete,
        onConfirmDelete = viewModel::onConfirmDelete,
        onEditorAmountChanged = viewModel::onEditorAmountChanged,
        onEditorNoteChanged = viewModel::onEditorNoteChanged,
        onEditorCategoryChanged = viewModel::onEditorCategoryChanged,
        onCancelEditor = viewModel::onCancelEditor,
        onConfirmEditor = viewModel::onConfirmEditor,
        onBackClick = onBackClick
    )
}

/**
 * Content: a pure function of [uiState] and callbacks — no ViewModel, no state
 * collection — so it renders in previews and in tests without Hilt.
 */
@Composable
private fun SmsInboxContent(
    uiState: SmsInboxUiState,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    availableCategories: List<CategoryType> = emptyList(),
    onFilterSelected: (SmsInboxFilter) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onDetectionOpened: (SmsInboxItemUi) -> Unit = {},
    onDetectionsVisible: (List<String>) -> Unit = {},
    onToggleSelection: (String) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onAdd: (String) -> Unit = {},
    onAddAll: () -> Unit = {},
    onCancelAddAll: () -> Unit = {},
    onConfirmAddAll: () -> Unit = {},
    onEditBeforeAdd: (SmsInboxItemUi) -> Unit = {},
    onRequestDelete: (String) -> Unit = {},
    onRequestDeleteSelected: () -> Unit = {},
    onSwipeDelete: (String) -> Unit = {},
    onArrivalHandled: (String) -> Unit = {},
    onClearAll: () -> Unit = {},
    onCancelDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onEditorAmountChanged: (String) -> Unit = {},
    onEditorNoteChanged: (String) -> Unit = {},
    onEditorCategoryChanged: (Int) -> Unit = {},
    onCancelEditor: () -> Unit = {},
    onConfirmEditor: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.isSelectionMode) {
                // Every row on screen counts as selected -> the chip offers deselect-all.
                val allSelected = uiState.detections.let { it.isNotEmpty() && it.all { row -> row.id in uiState.selectedIds } }
                BulkActionBar(
                    selectedCount = uiState.selectedIds.size,
                    allSelected = allSelected,
                    onClose = onClearSelection,
                    onToggleSelectAll = { if (allSelected) onClearSelection() else onSelectAll() },
                    onAddAll = onAddAll,
                    onDelete = onRequestDeleteSelected
                )
            }
        },
        // "Clear all" is only offered when there is something to clear, and it steps
        // aside in selection mode — that bar already carries its own Delete. It wipes in
        // from the start edge and wipes back out the same way.
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.hasItems && !uiState.isSelectionMode,
                enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = onClearAll,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.action_clear_all)) }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AppHeader(
                title = stringResource(id = R.string.label_sms_inbox_title),
                onBackClick = onBackClick
            )

            SearchField(
                query = uiState.searchQuery,
                onQueryChanged = onSearchQueryChanged
            )

            FilterRow(
                selected = uiState.filter,
                onFilterSelected = onFilterSelected
            )

            // The cards carry no decision buttons of their own any more, so the one thing a
            // user has to know is what a tap does. It sits under the filters, where it is
            // read once, instead of being repeated on every card.
            InboxHint()

            when {
                uiState.isEmpty -> EmptyInbox(filter = uiState.filter)
                else -> InboxList(
                    uiState = uiState,
                    onLoadMore = onLoadMore,
                    onDetectionOpened = onDetectionOpened,
                    onDetectionsVisible = onDetectionsVisible,
                    onToggleSelection = onToggleSelection,
                    onDelete = onRequestDelete,
                    onSwipeDelete = onSwipeDelete,
                    onArrivalHandled = onArrivalHandled
                )
            }
        }
    }

    if (uiState.pendingDeleteIds.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text(stringResource(id = R.string.label_sms_inbox_delete_title)) },
            text = { Text(stringResource(id = R.string.label_sms_inbox_delete_body)) },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(stringResource(id = R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            }
        )
    }

    // Bulk add writes real transactions for a whole screen at once and has no undo, so the
    // count and the consequences are confirmed before anything is written.
    if (uiState.pendingAddAllIds.isNotEmpty()) {
        val pendingCount = uiState.pendingAddAllIds.size
        AlertDialog(
            onDismissRequest = onCancelAddAll,
            title = {
                Text(
                    pluralStringResource(
                        R.plurals.label_sms_inbox_add_all_title,
                        pendingCount,
                        pendingCount
                    )
                )
            },
            text = { Text(stringResource(id = R.string.label_sms_inbox_add_all_body)) },
            confirmButton = {
                TextButton(onClick = onConfirmAddAll) {
                    Text(stringResource(id = R.string.action_add_all))
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelAddAll) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            }
        )
    }

    uiState.editor?.let { editor ->
        EditDetectionDialog(
            editor = editor,
            categories = availableCategories,
            onAmountChanged = onEditorAmountChanged,
            onNoteChanged = onEditorNoteChanged,
            onCategoryChanged = onEditorCategoryChanged,
            onDismiss = onCancelEditor,
            onConfirm = onConfirmEditor
        )
    }
}

/**
 * Correct a detection before it becomes a transaction.
 *
 * Only the fields a bank message cannot be trusted on are editable here: the amount
 * (prose parsing can mis-read it), the category (a guess) and the note (absent from the
 * SMS). Everything else — type, sender, time — stays as the bank stated it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDetectionDialog(
    editor: SmsEditorState,
    categories: List<CategoryType>,
    onAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onCategoryChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = stringResource(id = R.string.label_sms_inbox_edit_title))
                Text(
                    text = editor.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = editor.amountText,
                    onValueChange = onAmountChanged,
                    label = { Text(stringResource(id = R.string.label_sms_inbox_edit_amount)) },
                    singleLine = true,
                    isError = editor.amountText.replace(",", "").trim().toDoubleOrNull() == null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editor.note,
                    onValueChange = onNoteChanged,
                    label = { Text(stringResource(id = R.string.label_sms_inbox_edit_note)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(id = R.string.label_sms_inbox_edit_category),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(items = categories, key = { it.id }) { category ->
                            FilterChip(
                                selected = category.id == editor.categoryId,
                                onClick = { onCategoryChanged(category.id) },
                                label = { Text(text = category.name) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(id = R.string.action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun SearchField(query: String, onQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        placeholder = { Text(stringResource(id = R.string.label_sms_inbox_search_hint)) },
        shape = RoundedCornerShape(14.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(selected: SmsInboxFilter, onFilterSelected: (SmsInboxFilter) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SmsInboxFilter.entries) { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onFilterSelected(filter) },
                label = { Text(text = filter.label()) }
            )
        }
    }
}

/**
 * The one line explaining the interaction the cards no longer spell out.
 *
 * A card used to carry its own Add / Edit / Ignore buttons; now a tap opens the Add
 * Transaction screen, which is not something a card can show by itself. An info glyph
 * above the list says it once, instead of repeating a hint on every row.
 */
@Composable
private fun InboxHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            // Decorative: the sentence beside it is the content, and it is read out as text.
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.label_sms_inbox_card_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InboxList(
    uiState: SmsInboxUiState,
    onLoadMore: () -> Unit,
    onDetectionOpened: (SmsInboxItemUi) -> Unit,
    onDetectionsVisible: (List<String>) -> Unit,
    onToggleSelection: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSwipeDelete: (String) -> Unit,
    onArrivalHandled: (String) -> Unit
) {
    val listState = rememberLazyListState()

    // A new detection only plays its entrance while the list is at the beginning: a card
    // appearing above a list the user has scrolled down would animate out of sight, and
    // the inbox must never jump under them.
    val isAtListStart by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex <= 1 &&
                listState.firstVisibleItemScrollOffset <= LIST_START_OFFSET_PX
        }
    }

    // Bring a notification's row into view. It may not be in the loaded page yet, in
    // which case this simply does nothing — the user still sees the inbox rather than
    // an empty screen. The index is looked up in the render list, since a date separator
    // means a row's position in the list is no longer its position among the detections.
    LaunchedEffect(uiState.focusedId, uiState.listItems) {
        val focusedId = uiState.focusedId ?: return@LaunchedEffect
        val index = uiState.listItems.indexOfFirst { it.id == focusedId }
        if (index >= 0) listState.animateScrollToItem(index)
    }

    // Seeing is reading. Reported from what the list is actually showing rather than from
    // what was fetched, so a page loaded off-screen does not mark itself read, and the
    // bell count empties exactly as fast as the user scrolls through the cards.
    LaunchedEffect(listState, uiState.listItems) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .mapNotNull { visible -> uiState.listItems.getOrNull(visible.index) }
        }
            .distinctUntilChanged()
            .collect { rows ->
                onDetectionsVisible(
                    rows.filterIsInstance<SmsInboxListItemUi.Detection>().map { it.item.id }
                )
            }
    }

    // Load the next page a little before the user reaches the bottom, so scrolling
    // never stalls on a spinner.
    LaunchedEffect(listState, uiState.canLoadMore) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisible ->
                if (uiState.canLoadMore && lastVisible >= uiState.listItems.size - PREFETCH_DISTANCE) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        // The bottom inset keeps the last row clear of the floating Clear all button.
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = uiState.listItems, key = { it.id }) { row ->
            when (row) {
                is SmsInboxListItemUi.DateHeader -> TransactionDateHeader(
                    dayLabel = row.dayLabel.asString(),
                    dateLabel = row.dateLabel
                )

                is SmsInboxListItemUi.Detection -> {
                    val item = row.item
                    // Swiping is off while picking rows for a bulk action: a horizontal
                    // drag there would delete something the user only meant to select.
                    SwipeToDeleteCard(
                        enabled = !uiState.isSelectionMode,
                        onDelete = { onSwipeDelete(item.id) },
                        modifier = Modifier.animateItem()
                    ) {
                        DetectionCard(
                            item = item,
                            isFocused = item.id == uiState.focusedId,
                            // Arriving and visible here: play it. Arriving but off-screen:
                            // just consume it, so scrolling back up later cannot re-run it.
                            isArriving = item.id in uiState.arrivalIds && isAtListStart,
                            isArrivalPending = item.id in uiState.arrivalIds,
                            onArrivalHandled = { onArrivalHandled(item.id) },
                            onClick = {
                                if (uiState.isSelectionMode) {
                                    onToggleSelection(item.id)
                                } else {
                                    onDetectionOpened(item)
                                }
                            },
                            onLongClick = { onToggleSelection(item.id) },
                            onDelete = { onDelete(item.id) }
                        )
                    }
                }
            }
        }

        if (uiState.canLoadMore || uiState.isWorking) {
            item(key = "loading") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

/**
 * Swipe a detection either way to delete it.
 *
 * Both directions are accepted on purpose: the inbox has only one swipe action, so
 * refusing one side would just leave a dead gesture. The delete is reversible through the
 * snackbar's Undo, which is the safety net here instead of a confirmation dialog — a
 * mis-swipe costs a tap, not a row.
 *
 * On commit the card does not simply disappear: it keeps travelling in the swipe
 * direction until it has cleared the screen edge, so the gesture is seen finishing what
 * it started. The removal then closes the gap via the list's placement animation.
 */
@Composable
private fun SwipeToDeleteCard(
    enabled: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val maxRevealPx = with(density) { SWIPE_MAX_REVEAL_DP.dp.toPx() }
    val minRevealPx = with(density) { SWIPE_MIN_REVEAL_DP.dp.toPx() }
    val thresholdPx = with(density) { SWIPE_TRIGGER_THRESHOLD_DP.dp.toPx() }
    val exitMarginPx = with(density) { SWIPE_EXIT_MARGIN_DP.dp.toPx() }
    val offset = remember { Animatable(0f) }

    // While the card is flying out it is no longer following a finger, and the delete has
    // to land exactly once — even if the list drops the card mid-flight (it can, since
    // the row is removed as soon as the write is queued).
    var isExiting by remember { mutableStateOf(false) }
    var exitFired by remember { mutableStateOf(false) }
    var cardWidthPx by remember { mutableStateOf(0f) }
    val currentOnDelete by rememberUpdatedState(onDelete)

    DisposableEffect(Unit) {
        onDispose {
            // Safety net for the card leaving composition before its exit finished.
            if (isExiting && !exitFired) currentOnDelete()
        }
    }

    val gesturesEnabled = enabled && !isExiting

    /**
     * Sends the card off the screen in [direction] (+1 right, -1 left) before the delete
     * is reported, so the row is never removed while the card is still visible.
     */
    val flyOut: (Float) -> Unit = { direction ->
        if (!isExiting) {
            isExiting = true
            scope.launch {
                offset.animateTo(
                    targetValue = direction * (cardWidthPx + exitMarginPx),
                    animationSpec = tween(
                        durationMillis = SWIPE_EXIT_DURATION_MS,
                        easing = FastOutSlowInEasing
                    )
                )
                exitFired = true
                currentOnDelete()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { cardWidthPx = it.width.toFloat() }
    ) {
        // One hint per side. Their opacity is read from the drag inside graphicsLayer, so
        // following the finger never recomposes the card.
        SwipeDeleteHint(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    alpha = if (isExiting) 0f else revealAlpha(offset.value, true, maxRevealPx, minRevealPx)
                }
        )
        SwipeDeleteHint(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .graphicsLayer {
                    alpha = if (isExiting) 0f else revealAlpha(offset.value, false, maxRevealPx, minRevealPx)
                }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                // A row is flat, so the hint behind it needs something opaque to hide
                // behind. The card used to be that; without this the delete label would
                // show through the row as it slides.
                .background(MaterialTheme.colorScheme.background)
                .graphicsLayer {
                    // Undamped while flying out: the card must clear the screen edge,
                    // not stop at the reveal point the drag is limited to.
                    translationX = if (isExiting) {
                        offset.value
                    } else {
                        dampedTranslation(offset.value, maxRevealPx)
                    }
                }
                .horizontalSwipe(
                    key = gesturesEnabled,
                    threshold = thresholdPx,
                    flingVelocityThreshold = SWIPE_FLING_VELOCITY_PX_PER_SECOND,
                    onDragOffset = { raw ->
                        if (gesturesEnabled) scope.launch { offset.snapTo(raw) }
                    },
                    onThresholdCrossed = {
                        // Fires once per gesture, at the point the row would be deleted.
                        if (gesturesEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    // Right-to-left sends the card off the left edge, left-to-right off
                    // the right edge; the delete is reported once it is gone.
                    onSwipeLeft = { if (gesturesEnabled) flyOut(-1f) },
                    onSwipeRight = { if (gesturesEnabled) flyOut(1f) },
                    onDragEnd = {
                        if (!isExiting) scope.launch { offset.animateTo(0f) }
                    },
                    onDragCancel = {
                        if (!isExiting) scope.launch { offset.animateTo(0f) }
                    }
                )
        ) {
            content()
        }
    }
}

/** The delete affordance peeking out from behind a swiped-away card. */
@Composable
private fun SwipeDeleteHint(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.action_delete),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * One detection, laid out as a flat row.
 *
 * The shape follows the sender rather than the app chrome: a pastel initial disc, then
 * the name with the clock on the same line, the amount under it with its currency in a
 * badge, and the parser's guesses — category, then the message itself — below that. No
 * border, no container and no chips, so the eye runs down the avatar column and the list
 * reads as one column of text.
 */
@Composable
private fun DetectionCard(
    item: SmsInboxItemUi,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    /** True for the row a notification tap asked for, briefly after arrival. */
    isFocused: Boolean = false,
    /** True when this card must play the "a new message just arrived" entrance. */
    isArriving: Boolean = false,
    /** True while this row still counts as an unrevealed arrival. */
    isArrivalPending: Boolean = false,
    onArrivalHandled: () -> Unit = {}
) {
    val currentOnArrivalHandled by rememberUpdatedState(onArrivalHandled)

    LaunchedEffect(isArrivalPending) {
        if (isArrivalPending) {
            currentOnArrivalHandled()
        }
    }

    // The entrance is decided once, at first composition: the ViewModel clears the arrival
    // flag as soon as the row has been reported seen, so following it live would cut the
    // animation off on its first frame.
    val playsArrival = remember { isArriving }
    val arrival = remember { Animatable(if (playsArrival) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (playsArrival) {
            arrival.animateTo(1f, tween(ARRIVAL_ANIMATION_MS, easing = FastOutSlowInEasing))
        }
    }

    val rowBackground = when {
        item.isSelected -> MaterialTheme.colorScheme.secondaryContainer
        isFocused -> MaterialTheme.colorScheme.primaryContainer
        else -> transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val entrance = arrival.value
                alpha = ARRIVAL_START_ALPHA + (1f - ARRIVAL_START_ALPHA) * entrance
                val scale = ARRIVAL_START_SCALE + (1f - ARRIVAL_START_SCALE) * entrance
                scaleX = scale
                scaleY = scale
                translationY = -ARRIVAL_SLIDE_DP.dp.toPx() * (1f - entrance)
            }
            .clip(RoundedCornerShape(12.dp))
            .background(rowBackground)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Who it came from, and when. The clock is a time and nothing more: the list is
            // already grouped into days, so a date here would only repeat the separator.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.timeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // The money line: the currency rides in the badge, coloured by which way the
            // money went, so the number beside it can stay plain text.
            val amountDescription = stringResource(
                id = if (item.isIncome) {
                    R.string.label_sms_inbox_income_amount
                } else {
                    R.string.label_sms_inbox_expense_amount
                },
                item.amountText
            )
            Row(
                modifier = Modifier.semantics(mergeDescendants = true) {
                    // Badge, number and verb are one fact when read aloud, and the full
                    // formatted amount is what it should say — symbol included.
                    contentDescription = amountDescription
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AmountBadge(isIncome = item.isIncome, symbol = item.currencySymbol)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.amountValueText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(
                        id = if (item.isIncome) {
                            R.string.label_sms_inbox_income
                        } else {
                            R.string.label_sms_inbox_expense
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // The parser's guess, said plainly: it is a starting point for the Add screen,
            // not a decision the user has made here.
            Text(
                text = item.categoryLabel.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.messagePreview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // The row's only button. Adding is what tapping the row does now, and ignoring
            // lives on the notification; deleting has nowhere else to live.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDelete,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.action_delete),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/** The currency mark beside an amount: money in is green, money out is red. */
@Composable
private fun AmountBadge(isIncome: Boolean, symbol: String) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(if (isIncome) BadgeIncomeGreen else BadgeExpenseRed),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.labelSmall,
            color = BadgeOnColor,
            maxLines = 1
        )
    }
}

/**
 * The bar shown while rows are selected.
 *
 * It only offers what a selection is for now: everything here is either "act on all of
 * these" (add) or destructive (delete). Marking read left the bar along with the status
 * words — a card is read as soon as it is on screen, so there is nothing left to mark.
 *
 * Laid out in the same visual language as [SelectionHeader] so bulk selection looks the
 * same everywhere in the app, with one action the inbox needs on top: "Add all". The
 * select-all chip is a toggle — once every row on screen is selected it becomes a
 * deselect-all chip, so one corner both takes and releases the whole list.
 */
@Composable
private fun BulkActionBar(
    selectedCount: Int,
    allSelected: Boolean,
    onClose: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onAddAll: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.desc_exit_selection),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(R.string.label_val_selected, selectedCount),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Select all <-> deselect all.
            BulkActionChip(
                onClick = onToggleSelectAll,
                icon = if (allSelected) Icons.Rounded.Deselect else Icons.Rounded.DoneAll,
                contentDescription = stringResource(
                    if (allSelected) R.string.desc_deselect_all else R.string.desc_select_all
                ),
                contentColor = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            BulkActionChip(
                onClick = onAddAll,
                icon = Icons.Rounded.PlaylistAddCheck,
                contentDescription = stringResource(R.string.desc_add_all),
                contentColor = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            BulkActionChip(
                onClick = onDelete,
                icon = Icons.Rounded.Delete,
                contentDescription = stringResource(R.string.desc_delete_selected),
                contentColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * One icon button in the bulk bar (no background container).
 */
@Composable
private fun BulkActionChip(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    contentColor: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun EmptyInbox(filter: SmsInboxFilter) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(
                id = when (filter) {
                    SmsInboxFilter.ALL -> R.string.label_sms_inbox_empty_title
                    else -> R.string.label_sms_inbox_empty_filtered_title
                }
            ),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.label_sms_inbox_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SmsInboxFilter.label(): String = stringResource(
    id = when (this) {
        SmsInboxFilter.ALL -> R.string.label_sms_inbox_filter_all
        SmsInboxFilter.INCOME -> R.string.label_sms_inbox_filter_income
        SmsInboxFilter.EXPENSE -> R.string.label_sms_inbox_filter_expense
    }
)

/**
 * Renders a ViewModel-supplied [UiText] outside composition, for snackbar messages
 * raised from a coroutine. In-composition text still goes through `UiText.asString()`.
 */
private fun Context.resolve(text: UiText): String = when (text) {
    is UiText.DynamicString -> text.value
    is UiText.StringResource -> getString(text.resId, *text.args.toTypedArray())
}

/** How close to the end of the loaded page triggers the next fetch. */
private const val PREFETCH_DISTANCE = 5

/** How long a newly detected card takes to settle into the list. */
private const val ARRIVAL_ANIMATION_MS = 420

/** How far above its place a new card starts before sliding down into it. */
private const val ARRIVAL_SLIDE_DP = 28f

/** A new card is never fully invisible — it arrives, it does not blink in. */
private const val ARRIVAL_START_ALPHA = 0.2f

/** Slightly smaller at the start, so the card grows as it lands. */
private const val ARRIVAL_START_SCALE = 0.94f

/** Scroll offset still counted as "the list is at its beginning". */
private const val LIST_START_OFFSET_PX = 24

/** How long a notification-arrival highlight stays on its row. */
private const val FOCUS_HIGHLIGHT_MS = 2_500L

/** How far a swiped card travels before the movement is damped. */
private const val SWIPE_MAX_REVEAL_DP = 96f

/** How long a committed card takes to leave the screen. */
private const val SWIPE_EXIT_DURATION_MS = 200

/** Extra travel beyond the card's own width so it clears the screen edge completely. */
private const val SWIPE_EXIT_MARGIN_DP = 48f

/** Drag distance at which the delete is armed and the haptic fires. */
private const val SWIPE_TRIGGER_THRESHOLD_DP = 56f

/** The hint only starts to appear once the card has moved this far. */
private const val SWIPE_MIN_REVEAL_DP = 24f

/** A quick flick counts as a swipe even when the distance threshold was not reached. */
private const val SWIPE_FLING_VELOCITY_PX_PER_SECOND = 600f

/** Movement past [SWIPE_MAX_REVEAL_DP] is resisted, so a card never flies off-screen. */
private const val SWIPE_OVERSHOOT_RESISTANCE = 0.15f

/** Card follows the finger 1:1 up to [maxRevealPx], then resists further movement. */
private fun dampedTranslation(rawOffsetPx: Float, maxRevealPx: Float): Float {
    val clamped = rawOffsetPx.coerceIn(-maxRevealPx, maxRevealPx)
    return clamped + (rawOffsetPx - clamped) * SWIPE_OVERSHOOT_RESISTANCE
}

/**
 * Opacity of the hint revealed by the card's movement: [revealsWhenPositive] is true for
 * the start-side hint (shown while the card slides right) and false for the end side.
 */
private fun revealAlpha(
    offsetPx: Float,
    revealsWhenPositive: Boolean,
    maxRevealPx: Float,
    minRevealPx: Float
): Float {
    val revealed = if (revealsWhenPositive) offsetPx else -offsetPx
    if (revealed <= minRevealPx) return 0f
    val clamped = revealed.coerceIn(0f, maxRevealPx)
    val damped = clamped + (revealed - clamped) * SWIPE_OVERSHOOT_RESISTANCE
    return ((damped - minRevealPx) / (maxRevealPx - minRevealPx)).coerceIn(0f, 1f)
}

private val previewItems = listOf(
    SmsInboxItemUi(
        id = "1",
        title = "Swiggy",
        amountText = "₹450.00",
        amountValueText = "450.00",
        currencySymbol = "₹",
        isIncome = false,
        timeText = "9:20 pm",
        messagePreview = "Rs.450 debited from A/c XX1234 to VPA swiggy@ybl. Avl Bal Rs.12,000",
        categoryLabel = UiText.res(R.string.label_sms_inbox_category, "Food"),
        confidence = SmsConfidence.HIGH,
        isSelected = false,
        isUnread = true,
        isActionable = true,
        isFiled = false,
        linkedTransactionId = null,
        amountMinor = 45_000L,
        amountInput = "450",
        note = "Rs.450 debited from A/c XX1234 to VPA swiggy@ybl. Avl Bal Rs.12,000",
        categoryId = 1,
        transactionTypeId = 2
    ),
    SmsInboxItemUi(
        id = "2",
        title = "VM-HDFCBK",
        amountText = "₹25,000.00",
        amountValueText = "25,000.00",
        currencySymbol = "₹",
        isIncome = true,
        timeText = "10:02 am",
        messagePreview = "Rs.25000 credited to A/c XX1234 by salary",
        categoryLabel = UiText.res(R.string.label_sms_inbox_category, "Salary"),
        confidence = SmsConfidence.MEDIUM,
        isSelected = false,
        isUnread = false,
        isActionable = true,
        isFiled = false,
        linkedTransactionId = null,
        amountMinor = 2_500_000L,
        amountInput = "25000",
        note = "Rs.25000 credited to A/c XX1234 by salary",
        categoryId = 105,
        transactionTypeId = 1
    )
)

/** The rows as the screen really receives them: detections wrapped with their day header. */
private val previewListItems = buildList {
    add(
        SmsInboxListItemUi.DateHeader(
            id = "sms_day_preview",
            dayLabel = UiText.res(R.string.label_today),
            dateLabel = "18/09/2026"
        )
    )
    previewItems.forEach { add(SmsInboxListItemUi.Detection(it)) }
}

@Preview(showBackground = true)
@Composable
private fun SmsInboxContentPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        SmsInboxContent(uiState = SmsInboxUiState(listItems = previewListItems, isLoading = false))
    }
}

@Preview(showBackground = true)
@Composable
private fun SmsInboxEmptyPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        SmsInboxContent(uiState = SmsInboxUiState(isLoading = false))
    }
}

@Preview(showBackground = true)
@Composable
private fun SmsInboxSelectionPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        SmsInboxContent(
            uiState = SmsInboxUiState(
                listItems = previewListItems.map { row ->
                    when (row) {
                        is SmsInboxListItemUi.DateHeader -> row
                        is SmsInboxListItemUi.Detection ->
                            SmsInboxListItemUi.Detection(
                                row.item.copy(isSelected = row.item.id == "1")
                            )
                    }
                },
                selectedIds = setOf("1"),
                isLoading = false
            )
        )
    }
}
