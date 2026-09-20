package com.mknlabs.expensetracker.feature.smsinbox.ui

import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.DetectedSmsNotification
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.EXPENSE_TRANSACTION_TYPE_ID
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.INCOME_TRANSACTION_TYPE_ID
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsConfidenceScore
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsDetectionSource
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxFilter
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxStatus
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.NewSmsDetection
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.RecordSmsOutcome
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsInboxCleanupResult
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsInboxRepository
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsNotificationCleaner
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsTransactionWriter
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.DeleteSmsDetectionsUseCase
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.FileSmsDetectionAsTransactionUseCase
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.GetSmsDetectionsUseCase
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.GetSmsInboxUnreadCountUseCase
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.MarkSmsDetectionsViewedUseCase
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.ObserveSmsInboxUseCase
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.sms.ParsedSms
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * The inbox ViewModel's contract: filters and search reach the query, selections track
 * the rows they name, and every action either delegates or reports honestly.
 *
 * Depends only on the feature's own ports, so no Room, Hilt or device is involved.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SmsInboxViewModelTest {

    private lateinit var repository: FakeInboxRepository
    private lateinit var writer: FakeTransactionWriter
    private lateinit var cleaner: FakeNotificationCleaner
    private lateinit var viewModel: SmsInboxViewModel

    /**
     * The ViewModel's own time. Held here (rather than taken from `runTest`) because the
     * swipe-delete Undo window is a real delay on the ViewModel's dispatcher, and the test
     * has to be able to run it out instead of waiting ten seconds.
     */
    private lateinit var scheduler: TestCoroutineScheduler

    @Before
    fun setUp() {
        scheduler = TestCoroutineScheduler()
        Dispatchers.setMain(UnconfinedTestDispatcher(scheduler))
        repository = FakeInboxRepository()
        writer = FakeTransactionWriter()
        cleaner = FakeNotificationCleaner()
        viewModel = SmsInboxViewModel(
            observeInbox = ObserveSmsInboxUseCase(repository),
            getUnreadCount = GetSmsInboxUnreadCountUseCase(repository),
            markViewed = MarkSmsDetectionsViewedUseCase(repository),
            deleteDetections = DeleteSmsDetectionsUseCase(repository, cleaner),
            fileDetection = FileSmsDetectionAsTransactionUseCase(repository, writer, cleaner),
            getDetections = GetSmsDetectionsUseCase(repository)
        )
        viewModel.updateDisplayContext(
            categories = emptyList(),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences,
            dateFormatPattern = TEST_DATE_PATTERN,
            timeFormat = DEFAULT_TIME_FORMAT
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `rows are projected from the repository with unread count`() = runTest {
        repository.items.value = listOf(detection(id = "a"), detection(id = "b"))
        repository.unread.value = 2

        val state = viewModel.uiState.value

        assertEquals(2, state.detections.size)
        assertEquals(2, state.unreadCount)
        assertEquals("Swiggy", state.detections.first().title)
        assertFalse(state.isLoading)
        assertTrue(state.detections.first().isActionable)
    }

    @Test
    fun `choosing a filter re-queries and drops the selection`() = runTest {
        repository.items.value = listOf(detection(id = "a"))
        viewModel.onToggleSelection("a")
        assertTrue(viewModel.uiState.value.isSelectionMode)

        viewModel.onFilterSelected(SmsInboxFilter.INCOME)

        assertEquals(SmsInboxFilter.INCOME, repository.lastFilter)
        assertEquals(INCOME_TRANSACTION_TYPE_ID, repository.lastFilter?.transactionTypeId)
        assertFalse(viewModel.uiState.value.isSelectionMode)
    }

    @Test
    fun `search restarts paging instead of inheriting a scrolled limit`() = runTest {
        repository.items.value = (1..40).map { detection(id = "id-$it") }
        viewModel.onLoadMore()
        assertTrue(repository.lastLimit > 30)

        viewModel.onSearchQueryChanged("swiggy")

        assertEquals("swiggy", repository.lastSearchQuery)
        assertEquals(30, repository.lastLimit)
    }

    @Test
    fun `selecting a detection also marks it seen`() = runTest {
        repository.items.value = listOf(detection(id = "a"))

        viewModel.onToggleSelection("a")

        assertTrue(viewModel.uiState.value.selectedIds.contains("a"))
        assertTrue(viewModel.uiState.value.detections.single().isSelected)
        assertEquals(listOf("a"), repository.viewedIds)
    }

    @Test
    fun `filing a detection writes a transaction and links it`() = runTest {
        repository.items.value = listOf(detection(id = "a"))

        viewModel.onAddDetection("a")

        assertEquals(1, writer.saved.size)
        assertEquals("a", repository.addedLinks.single().first)
        assertEquals(
            "the inbox links the very transaction that was stored",
            writer.returnedIds.single(),
            repository.addedLinks.single().second
        )
    }

    @Test
    fun `a detection already in the ledger is not filed twice`() = runTest {
        repository.items.value = listOf(detection(id = "a"))
        writer.alreadyExists = true

        viewModel.onAddDetection("a")

        assertTrue("no transaction may be written", writer.saved.isEmpty())
        assertTrue(repository.addedLinks.isEmpty())
    }

    @Test
    fun `the duplicate guard can be overridden deliberately`() = runTest {
        repository.items.value = listOf(detection(id = "a"))
        writer.alreadyExists = true

        viewModel.onAddDetection("a", allowOverride = true)

        assertEquals(1, writer.saved.size)
    }

    @Test
    fun `editing before adding files the corrected amount and category`() = runTest {
        repository.items.value = listOf(detection(id = "a"))
        val item = viewModel.uiState.value.detections.single()

        viewModel.onOpenEditor(item)
        assertNotNull(viewModel.uiState.value.editor)

        viewModel.onEditorAmountChanged("500.50")
        viewModel.onEditorNoteChanged("Team lunch")
        viewModel.onEditorCategoryChanged(7)
        viewModel.onConfirmEditor()

        assertNull("the editor closes once submitted", viewModel.uiState.value.editor)
        val saved = writer.saved.single()
        assertEquals(50_050L, saved.amountMinor)
        assertEquals(7, saved.categoryId)
        assertEquals("Team lunch", writer.savedNotes.single())
    }

    @Test
    fun `an unusable amount is rejected instead of writing a zero-value expense`() = runTest {
        repository.items.value = listOf(detection(id = "a"))
        viewModel.onOpenEditor(viewModel.uiState.value.detections.single())

        viewModel.onEditorAmountChanged("abc")
        viewModel.onConfirmEditor()

        assertTrue(writer.saved.isEmpty())
        assertNotNull("the editor stays open so the mistake can be fixed", viewModel.uiState.value.editor)
    }

    @Test
    fun `deleting asks first and only then removes the rows`() = runTest {
        repository.items.value = listOf(detection(id = "a"))

        viewModel.onRequestDelete("a")
        assertEquals(setOf("a"), viewModel.uiState.value.pendingDeleteIds)
        assertTrue("nothing is removed before confirmation", repository.deletedIds.isEmpty())

        viewModel.onCancelDelete()
        assertTrue(viewModel.uiState.value.pendingDeleteIds.isEmpty())

        viewModel.onRequestDelete("a")
        viewModel.onConfirmDelete()
        assertEquals(listOf("a"), repository.deletedIds)
    }

    @Test
    fun `add all asks first and only writes after confirmation`() = runTest {
        repository.items.value = listOf(detection(id = "a"), detection(id = "b"))

        viewModel.onRequestAddAll()
        assertEquals(setOf("a", "b"), viewModel.uiState.value.pendingAddAllIds)
        assertTrue("nothing is filed before confirmation", writer.saved.isEmpty())

        viewModel.onCancelAddAll()
        assertTrue(viewModel.uiState.value.pendingAddAllIds.isEmpty())
        assertTrue("cancelling writes nothing", writer.saved.isEmpty())

        viewModel.onRequestAddAll()
        viewModel.onConfirmAddAll()

        assertTrue(viewModel.uiState.value.pendingAddAllIds.isEmpty())
        assertEquals(2, writer.saved.size)
    }

    @Test
    fun `a swipe hides the row at once but only deletes it when the undo window closes`() = runTest {
        repository.items.value = listOf(detection(id = "a"))

        viewModel.onSwipeDelete("a")

        assertTrue("the row leaves the list immediately", viewModel.uiState.value.detections.isEmpty())
        assertTrue("but nothing is deleted yet", repository.deletedIds.isEmpty())
        assertEquals(setOf("a"), viewModel.uiState.value.pendingRemovalIds)

        scheduler.advanceTimeBy(30_000)

        assertEquals(listOf("a"), repository.deletedIds)
    }

    @Test
    fun `undo brings the swiped row back and cancels the delete itself`() = runTest {
        repository.items.value = listOf(detection(id = "a"))
        viewModel.onSwipeDelete("a")

        viewModel.onUndoDelete()

        assertEquals("the row is shown again", 1, viewModel.uiState.value.detections.size)
        assertTrue(viewModel.uiState.value.pendingRemovalIds.isEmpty())

        scheduler.advanceTimeBy(30_000)
        assertTrue("undo must cancel the delete, not race it", repository.deletedIds.isEmpty())
    }

    @Test
    fun `several swipes in one window are undone together`() = runTest {
        repository.items.value = listOf(detection(id = "a"), detection(id = "b"))

        viewModel.onSwipeDelete("a")
        viewModel.onSwipeDelete("b")

        assertEquals(setOf("a", "b"), viewModel.uiState.value.pendingRemovalIds)
        assertTrue(viewModel.uiState.value.detections.isEmpty())

        viewModel.onUndoDelete()
        assertEquals(2, viewModel.uiState.value.detections.size)

        scheduler.advanceTimeBy(30_000)
        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test
    fun `clear all takes every visible row and undo brings them back`() = runTest {
        repository.items.value = listOf(detection(id = "a"), detection(id = "b"))

        viewModel.onClearAllVisible()

        assertTrue("the list empties at once", viewModel.uiState.value.detections.isEmpty())
        assertTrue("nothing is deleted while Undo is on offer", repository.deletedIds.isEmpty())

        viewModel.onUndoDelete()

        assertEquals(2, viewModel.uiState.value.detections.size)
        scheduler.advanceTimeBy(30_000)
        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test
    fun `clear all commits the deletes once the undo window closes`() = runTest {
        repository.items.value = listOf(detection(id = "a"), detection(id = "b"))

        viewModel.onClearAllVisible()
        scheduler.advanceTimeBy(30_000)

        assertEquals(listOf("a", "b"), repository.deletedIds)
    }

    @Test
    fun `clear all on an empty list does nothing`() = runTest {
        viewModel.onClearAllVisible()
        scheduler.advanceTimeBy(30_000)
        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test
    fun `a card the user is shown is read, and only once`() = runTest {
        repository.items.value = listOf(detection(id = "a"), detection(id = "b"))

        viewModel.onDetectionsVisible(listOf("a"))
        assertEquals(listOf("a"), repository.viewedIds)

        // The list keeps reporting what is on screen, so a second report of the same row
        // must not write again — the row is no longer unread.
        repository.items.value = listOf(detection(id = "a", status = SmsInboxStatus.VIEWED), detection(id = "b"))
        viewModel.onDetectionsVisible(listOf("a"))

        assertEquals(listOf("a"), repository.viewedIds)
    }

    @Test
    fun `tapping a live card asks for the Add Transaction screen instead of filing it`() = runTest {
        repository.items.value = listOf(detection(id = "a"))
        val item = viewModel.uiState.value.detections.single()
        val events = mutableListOf<SmsInboxEvent>()
        // Collected eagerly: the shared flow only hands an event to a subscriber that is
        // already attached when it is emitted.
        val collector = launch(UnconfinedTestDispatcher(scheduler)) {
            viewModel.events.collect { events += it }
        }

        viewModel.onDetectionOpened(item)

        assertTrue("nothing may be written before the user presses Add", writer.saved.isEmpty())
        assertTrue(repository.addedLinks.isEmpty())
        val event = events.filterIsInstance<SmsInboxEvent.ReviewInAddTransaction>().single()
        assertEquals("a", event.item.id)
        assertEquals("450", event.item.amountInput)
        collector.cancel()
    }

    @Test
    fun `tapping an already filed card opens the transaction it created`() = runTest {
        repository.items.value = listOf(
            detection(id = "a", status = SmsInboxStatus.ADDED, linkedTransactionId = "tx-9")
        )
        val item = viewModel.uiState.value.detections.single()
        val events = mutableListOf<SmsInboxEvent>()
        val collector = launch(UnconfinedTestDispatcher(scheduler)) {
            viewModel.events.collect { events += it }
        }

        viewModel.onDetectionOpened(item)

        assertEquals(
            "tx-9",
            events.filterIsInstance<SmsInboxEvent.OpenFiledTransaction>().single().transactionId
        )
        collector.cancel()
    }

    @Test
    fun `tapping an ignored card offers nothing`() = runTest {
        repository.items.value = listOf(detection(id = "a", status = SmsInboxStatus.IGNORED))
        val item = viewModel.uiState.value.detections.single()
        val events = mutableListOf<SmsInboxEvent>()
        val collector = launch(UnconfinedTestDispatcher(scheduler)) {
            viewModel.events.collect { events += it }
        }

        viewModel.onDetectionOpened(item)

        assertTrue(
            "a decided row has nothing to review",
            events.none { it is SmsInboxEvent.ReviewInAddTransaction || it is SmsInboxEvent.OpenFiledTransaction }
        )
        collector.cancel()
    }

    @Test
    fun `select all takes every row on screen`() = runTest {
        repository.items.value = listOf(detection(id = "a"), detection(id = "b"))

        viewModel.onSelectAll()

        assertEquals(setOf("a", "b"), viewModel.uiState.value.selectedIds)
    }

    @Test
    fun `a detection newer than anything on screen arrives with the list`() = runTest {
        repository.items.value = listOf(detection(id = "old", detectedAt = 1_000L))
        assertTrue(
            "the first projection is the baseline, not an arrival",
            viewModel.uiState.value.arrivalIds.isEmpty()
        )

        repository.items.value = listOf(
            detection(id = "fresh", detectedAt = 2_000L),
            detection(id = "old", detectedAt = 1_000L)
        )

        assertEquals(setOf("fresh"), viewModel.uiState.value.arrivalIds)

        viewModel.onArrivalHandled("fresh")
        assertTrue("an arrival plays once", viewModel.uiState.value.arrivalIds.isEmpty())
    }

    @Test
    fun `paging in older rows is not an arrival`() = runTest {
        repository.items.value = listOf(detection(id = "new", detectedAt = 2_000L))

        repository.items.value = listOf(
            detection(id = "new", detectedAt = 2_000L),
            detection(id = "older-page", detectedAt = 500L)
        )

        assertTrue(viewModel.uiState.value.arrivalIds.isEmpty())
    }

    @Test
    fun `rows are separated by the day the bank sent them, in the user's own date style`() = runTest {
        repository.items.value = listOf(
            detection(id = "today", detectedAt = NOW),
            detection(id = "yesterday", detectedAt = NOW - MILLIS_PER_DAY),
            detection(id = "older", detectedAt = NOW - 3 * MILLIS_PER_DAY)
        )

        val list = viewModel.uiState.value.listItems
        val headers = list.filterIsInstance<SmsInboxListItemUi.DateHeader>()

        assertEquals(3, headers.size)
        assertTrue(headers[0].dateLabel.isNotEmpty())
        assertTrue(
            "the date follows the preference, not a fixed pattern",
            headers[0].dateLabel.contains("/")
        )
        assertTrue(
            "cards stay under the date they belong to",
            list.first() is SmsInboxListItemUi.DateHeader
        )
    }

    @Test
    fun `filing a detection clears the notification that announced it`() = runTest {
        repository.items.value = listOf(detection(id = "a", notificationId = 4242))

        viewModel.onAddDetection("a")

        assertEquals(listOf(4242), cleaner.cleared)
    }

    @Test
    fun `a notification tap shows the row it named, whatever was filtered or searched`() = runTest {
        repository.items.value = listOf(detection(id = "a"))
        viewModel.onFilterSelected(SmsInboxFilter.INCOME)
        viewModel.onSearchQueryChanged("swiggy")

        viewModel.onFocusRequested("a", openEditor = false)

        val state = viewModel.uiState.value
        assertEquals("the row must be reachable", SmsInboxFilter.ALL, state.filter)
        assertEquals("", state.searchQuery)
        assertEquals("a", state.focusedId)
        assertTrue("an opened detection counts as seen", repository.viewedIds.contains("a"))
        assertNull("a plain tap does not open the editor", state.editor)
    }

    @Test
    fun `the Edit action arrives with the correction dialog already open`() = runTest {
        repository.items.value = listOf(detection(id = "a"))

        viewModel.onFocusRequested("a", openEditor = true)

        assertEquals("a", viewModel.uiState.value.editor?.id)
    }

    @Test
    fun `a purged detection is reported instead of opening nothing`() = runTest {
        viewModel.onFocusRequested("gone", openEditor = false)

        assertNull(viewModel.uiState.value.focusedId)
    }

    @Test
    fun `the arrival highlight releases itself`() = runTest {
        repository.items.value = listOf(detection(id = "a"))
        viewModel.onFocusRequested("a", openEditor = false)
        assertEquals("a", viewModel.uiState.value.focusedId)

        viewModel.onFocusHighlightFinished()

        assertNull(viewModel.uiState.value.focusedId)
    }

    private fun detection(
        id: String,
        merchant: String = "Swiggy",
        status: SmsInboxStatus = SmsInboxStatus.NEW,
        transactionTypeId: Int = EXPENSE_TRANSACTION_TYPE_ID,
        notificationId: Int? = null,
        linkedTransactionId: String? = null,
        detectedAt: Long = NOW
    ) = DetectedSmsNotification(
        id = id,
        smsHash = "hash-$id",
        sender = "VM-HDFCBK",
        messageBody = "Rs.450 debited from A/c XX1234 to VPA swiggy@ybl",
        amountMinor = 45_000L,
        transactionTypeId = transactionTypeId,
        merchantName = merchant,
        detectedAt = detectedAt,
        notificationCreatedAt = detectedAt,
        status = status,
        linkedTransactionId = linkedTransactionId,
        source = SmsDetectionSource.SMS,
        confidenceScore = SmsConfidenceScore.HIGH,
        suggestedCategoryId = 1,
        notificationId = notificationId
    )

    private companion object {
        /** A fixed "now" so day separators are deterministic. */
        val NOW = 1_700_000_000_000L
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
        const val TEST_DATE_PATTERN = "dd/MM/yyyy"
    }
}

/** Records which shade notifications the inbox took down. */
private class FakeNotificationCleaner : SmsNotificationCleaner {

    val cleared = mutableListOf<Int>()

    override fun clear(notificationId: Int) {
        cleared += notificationId
    }
}

/** In-memory inbox that records how the ViewModel queried and mutated it. */
private class FakeInboxRepository : SmsInboxRepository {

    val items = MutableStateFlow<List<DetectedSmsNotification>>(emptyList())
    val unread = MutableStateFlow(0)

    var lastFilter: SmsInboxFilter? = null
    var lastSearchQuery: String? = null
    var lastLimit: Int = 0
    val viewedIds = mutableListOf<String>()
    val ignoredIds = mutableListOf<String>()
    val deletedIds = mutableListOf<String>()
    val addedLinks = mutableListOf<Pair<String, String>>()

    override suspend fun recordDetection(detection: NewSmsDetection): RecordSmsOutcome =
        RecordSmsOutcome.Duplicate(
            items.value.firstOrNull() ?: error("no seeded detection to return")
        )

    override fun observeInbox(
        filter: SmsInboxFilter,
        searchQuery: String?,
        limit: Int,
        offset: Int
    ): Flow<List<DetectedSmsNotification>> {
        lastFilter = filter
        lastSearchQuery = searchQuery
        lastLimit = limit
        return items
    }

    override suspend fun getPage(
        filter: SmsInboxFilter,
        searchQuery: String?,
        limit: Int,
        offset: Int
    ): List<DetectedSmsNotification> {
        lastFilter = filter
        lastSearchQuery = searchQuery
        lastLimit = limit
        return items.value
    }

    override fun observeUnreadCount(): Flow<Int> = unread

    override suspend fun getById(id: String): DetectedSmsNotification? =
        items.value.firstOrNull { it.id == id }

    override suspend fun getByIds(ids: List<String>): List<DetectedSmsNotification> =
        items.value.filter { it.id in ids }

    override suspend fun markViewed(ids: List<String>) {
        viewedIds += ids
    }

    override suspend fun markAdded(id: String, transactionId: String) {
        addedLinks += id to transactionId
    }

    override suspend fun markIgnored(ids: List<String>) {
        ignoredIds += ids
    }

    override suspend fun delete(ids: List<String>) {
        deletedIds += ids
    }

    override suspend fun attachNotification(id: String, notificationId: Int) = Unit

    override suspend fun runCleanup(now: Long): SmsInboxCleanupResult =
        SmsInboxCleanupResult(expired = 0, purged = 0)
}

/** Records what the inbox asked the ledger to do, and can simulate a duplicate. */
private class FakeTransactionWriter : SmsTransactionWriter {

    var alreadyExists = false
    val saved = mutableListOf<ParsedSms>()
    val savedNotes = mutableListOf<String>()
    val returnedIds = mutableListOf<String>()

    override suspend fun transactionExistsFor(amountMinor: Long, occurredAt: Long): Boolean =
        alreadyExists

    override suspend fun saveDetectionAsTransaction(
        parsed: ParsedSms,
        note: String,
        categoryId: Int
    ): Transaction {
        saved += parsed.copy(categoryId = categoryId)
        savedNotes += note
        val transactionId = "tx-${saved.size}"
        returnedIds += transactionId
        return Transaction(
            id = transactionId,
            note = note,
            createdAt = parsed.smsTimestamp,
            amountMinor = parsed.amountMinor,
            transactionTypeId = parsed.transactionTypeId,
            paymentTypeId = 1,
            categoryId = categoryId
        )
    }
}
