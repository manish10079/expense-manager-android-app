package com.mknlabs.expensetracker.ui.viewmodels

import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.domain.repository.TransactionSummary
import com.mknlabs.expensetracker.domain.repository.RecentTransaction
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.domain.usecase.ObserveAccessStatusUseCase
import com.mknlabs.expensetracker.ui.components.TransactionPeriodFilter
import com.mknlabs.expensetracker.ui.models.TransactionListItemUi
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import androidx.lifecycle.viewModelScope
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: TransactionsViewModel
    private lateinit var fakeRepository: FakeTransactionRepository
    private lateinit var fakeMonetizationRepository: FakeMonetizationRepository
    private lateinit var observeAccessStatusUseCase: ObserveAccessStatusUseCase

    @Before
    fun setup() {
        fakeRepository = FakeTransactionRepository()
        fakeMonetizationRepository = FakeMonetizationRepository()
        observeAccessStatusUseCase = ObserveAccessStatusUseCase(fakeMonetizationRepository)
        viewModel = TransactionsViewModel(
            application = android.app.Application(),
            transactionRepository = fakeRepository,
            observeAccessStatusUseCase = observeAccessStatusUseCase
        )
        
        // Start collecting the flow to keep it active during tests
        viewModel.viewModelScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect { }
        }
    }

    @Test
    fun `toggleSelection activates selection mode and adds id`() {
        // Arrange
        val transactionId = "test_id_1"

        // Act
        viewModel.toggleSelection(transactionId)

        // Assert
        assertTrue(viewModel.uiState.value.isSelectionMode)
        assertTrue(viewModel.uiState.value.selectedTransactionIds.contains(transactionId))
    }

    @Test
    fun `toggleSelection second time removes id and deactivates mode if empty`() {
        // Arrange
        val transactionId = "test_id_1"
        viewModel.toggleSelection(transactionId)

        // Act
        viewModel.toggleSelection(transactionId)

        // Assert
        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertFalse(viewModel.uiState.value.selectedTransactionIds.contains(transactionId))
    }

    @Test
    fun `clearSelection resets all selection state`() {
        // Arrange
        viewModel.toggleSelection("id1")
        viewModel.toggleSelection("id2")

        // Act
        viewModel.clearSelection()

        // Assert
        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertTrue(viewModel.uiState.value.selectedTransactionIds.isEmpty())
    }

    @Test
    fun `updatePeriodFilter clears selection`() {
        // Arrange
        viewModel.toggleSelection("id1")
        assertTrue(viewModel.uiState.value.isSelectionMode)

        // Act
        viewModel.updatePeriodFilter(TransactionPeriodFilter.DAILY)

        // Assert
        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertTrue(viewModel.uiState.value.selectedTransactionIds.isEmpty())
    }

    @Test
    fun `navigatePeriod clears selection`() {
        // Arrange
        viewModel.toggleSelection("id1")
        assertTrue(viewModel.uiState.value.isSelectionMode)

        // Act
        viewModel.navigatePeriod(-1)

        // Assert
        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertTrue(viewModel.uiState.value.selectedTransactionIds.isEmpty())
    }

    @Test
    fun `jumpToPeriod clears selection`() {
        // Arrange
        viewModel.toggleSelection("id1")
        assertTrue(viewModel.uiState.value.isSelectionMode)

        // Act
        viewModel.jumpToPeriod(System.currentTimeMillis())

        // Assert
        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertTrue(viewModel.uiState.value.selectedTransactionIds.isEmpty())
    }

    @Test
    fun `daily view pins summary above the lazy list`() {
        // Arrange
        val now = System.currentTimeMillis()
        updateInputsWithSummaries(
            transactions = listOf(
                transaction("t_income", now, 1_000L, typeId = 1),
                transaction("t_expense", now, 4_000L, typeId = 2)
            )
        )
        viewModel.updatePeriodFilter(TransactionPeriodFilter.DAILY)

        // Act
        awaitPinnedSummary("summary_daily", expectedRows = 2)

        // Assert: summary is pinned (out of the lazy list), no summary inside the list
        val state = viewModel.uiState.value
        assertNotNull(state.pinnedSummary)
        assertEquals("summary_daily", state.pinnedSummary?.id)
        assertTrue(state.transactionItems.none { it is TransactionListItemUi.SummaryCard })
        assertEquals(2, state.transactionItems.filterIsInstance<TransactionListItemUi.TransactionRow>().size)
    }

    @Test
    fun `monthly view pins summary above the lazy list`() {
        // Arrange
        val now = System.currentTimeMillis()
        updateInputsWithSummaries(
            transactions = listOf(
                transaction("t_income", now, 2_000L, typeId = 1),
                transaction("t_expense", now, 3_000L, typeId = 2)
            )
        )
        viewModel.updatePeriodFilter(TransactionPeriodFilter.MONTHLY)

        // Act
        awaitPinnedSummary("summary_monthly", expectedRows = 2)

        // Assert: summary is pinned, not inside the lazy list
        val state = viewModel.uiState.value
        assertNotNull(state.pinnedSummary)
        assertEquals("summary_monthly", state.pinnedSummary?.id)
        assertTrue(state.transactionItems.none { it is TransactionListItemUi.SummaryCard })
        assertEquals(2, state.transactionItems.filterIsInstance<TransactionListItemUi.TransactionRow>().size)
    }

    @Test
    fun `yearly view pins per-year summary and keeps per-month summaries in the list`() {
        // Arrange
        val now = System.currentTimeMillis()
        // Use a second-month timestamp that is guaranteed to stay within the current year
        // (a fixed -5 days offset could cross into the previous year when run in early January).
        val previousMonthTimestamp = previousMonthInCurrentYear()
        updateInputsWithSummaries(
            transactions = listOf(
                transaction("t_income", now, 2_000L, typeId = 1),
                transaction("t_expense", now, 3_000L, typeId = 2),
                transaction("t_old_expense", previousMonthTimestamp, 1_500L, typeId = 2)
            )
        )
        viewModel.updatePeriodFilter(TransactionPeriodFilter.YEARLY)

        // Act
        awaitPinnedSummary("summary_yearly", expectedRows = 3)

        // Assert: per-year summary is pinned, per-month summaries still inside the list
        val state = viewModel.uiState.value
        assertNotNull(state.pinnedSummary)
        assertEquals("summary_yearly", state.pinnedSummary?.id)
        assertTrue(state.transactionItems.any { it is TransactionListItemUi.SummaryCard })
        assertEquals(3, state.transactionItems.filterIsInstance<TransactionListItemUi.TransactionRow>().size)
    }

    @Test
    fun `ad items are injected after every 5th transaction row with stable keys`() {
        // Arrange: 12 transactions spanning multiple months -> ad slots after rows 5 and 10
        val now = System.currentTimeMillis()
        val previousMonth = previousMonthInCurrentYear()
        updateInputsWithSummaries(
            transactions = (1..12).map { i ->
                transaction(
                    id = "t_$i",
                    createdAt = if (i % 2 == 0) previousMonth else now,
                    amountMinor = (i * 1_000L),
                    typeId = 2
                )
            }
        )
        viewModel.updatePeriodFilter(TransactionPeriodFilter.YEARLY)

        // Act
        awaitUiState { it.transactionItems.any { item -> item is TransactionListItemUi.Ad } }

        // Assert: Ad items are own list entries with stable keys, placed after every 5th row
        val state = viewModel.uiState.value
        val items = state.transactionItems
        val adSlots = items.filterIsInstance<TransactionListItemUi.Ad>()
        assertEquals(listOf("ad_5", "ad_10"), adSlots.map { it.id })
        assertEquals(12, items.filterIsInstance<TransactionListItemUi.TransactionRow>().size)

        // The list order interleaves ads after the 5th and 10th transaction rows
        var rowCount = 0
        items.forEach { item ->
            when (item) {
                is TransactionListItemUi.TransactionRow -> rowCount++
                is TransactionListItemUi.Ad -> assertTrue(rowCount == 5 || rowCount == 10)
                else -> Unit
            }
        }
    }

    /** Returns a timestamp in a month before the current one, guaranteed to be in the current year. */
    private fun previousMonthInCurrentYear(): Long {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(
                Calendar.MONTH,
                if (currentMonth == Calendar.JANUARY) currentMonth else currentMonth - 1
            )
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun `summaries disabled leaves pinned summary null`() {
        // Arrange: summaries disabled (default settings)
        val now = System.currentTimeMillis()
        viewModel.updateInputs(
            transactions = listOf(
                transaction("t_expense", now, 4_000L, typeId = 2)
            ),
            categories = emptyList(),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences,
            dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
            timeFormat = DEFAULT_TIME_FORMAT,
            customizationSettings = TransactionCardCustomizationSettings(showTransactionListSummaries = false)
        )
        viewModel.updatePeriodFilter(TransactionPeriodFilter.MONTHLY)

        // Act
        awaitUiState { it.transactionItems.isNotEmpty() }

        // Assert
        assertNull(viewModel.uiState.value.pinnedSummary)
        assertTrue(viewModel.uiState.value.transactionItems.none { it is TransactionListItemUi.SummaryCard })
    }

    private fun updateInputsWithSummaries(transactions: List<Transaction>) {
        viewModel.updateInputs(
            transactions = transactions,
            categories = emptyList(),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences,
            dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
            timeFormat = DEFAULT_TIME_FORMAT,
            customizationSettings = TransactionCardCustomizationSettings(showTransactionListSummaries = true)
        )
    }

    private fun transaction(
        id: String,
        createdAt: Long,
        amountMinor: Long,
        typeId: Int = 2
    ): Transaction {
        return Transaction(
            id = id,
            note = "test",
            createdAt = createdAt,
            amountMinor = amountMinor,
            transactionTypeId = typeId,
            paymentTypeId = 1,
            categoryId = 1,
            syncState = com.mknlabs.expensetracker.models.SyncState.LOCAL_ONLY
        )
    }

    /** Rebuilds run on Dispatchers.Default asynchronously, so poll the state until the summary appears. */
    private fun awaitPinnedSummary(expectedId: String, expectedRows: Int, timeoutMs: Long = 5_000) {
        awaitUiState(
            timeoutMs = timeoutMs,
            condition = { 
                it.pinnedSummary?.id == expectedId && 
                it.transactionItems.filterIsInstance<TransactionListItemUi.TransactionRow>().size == expectedRows 
            }
        )
    }

    private fun awaitUiState(timeoutMs: Long = 5_000, condition: (TransactionsScreenUiState) -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition(viewModel.uiState.value)) return
            Thread.sleep(10)
        }
        assertTrue("UI state condition not met within ${timeoutMs}ms", condition(viewModel.uiState.value))
    }

    @Test
    fun `selectAll selects all visible transaction rows`() {
        // Act
        viewModel.selectAll()

        // Assert
        assertTrue(viewModel.uiState.value.selectedTransactionIds.isEmpty())
    }

    // Manual Fake implementation
    private class FakeTransactionRepository : TransactionRepository {
        override fun observeActiveTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
        override fun observeHomeSummary(): Flow<TransactionSummary> = flowOf(TransactionSummary(0,0,0,0,0))
        override fun observeRecentTransactions(limit: Int): Flow<List<RecentTransaction>> = flowOf(emptyList())
        override fun observeActiveTransactionCount(): Flow<Int> = flowOf(0)
        override suspend fun getTransactionById(id: String): Transaction? = null
        override suspend fun upsertTransaction(transaction: Transaction): Transaction = transaction
        override suspend fun softDeleteTransaction(id: String) {}
        override suspend fun softDeleteTransactions(ids: List<String>) {}
        override suspend fun deleteAllTransactions() {}
    }

    private class FakeMonetizationRepository : MonetizationRepository {
        override fun observeAccessStatus(feature: Feature, optionId: String?): Flow<AccessStatus> = 
            flowOf(AccessStatus.Granted)
        override suspend fun grantTemporaryAccess(feature: Feature, optionId: String?, durationMillis: Long) {}
        override suspend fun becomePremium() {}
        override val isAdsEnabled: Flow<Boolean> = flowOf(true)
        override val globalAdAccessExpiry: Flow<Long> = flowOf(0L)
        override val userTier: Flow<com.mknlabs.expensetracker.models.UserTier> = flowOf(com.mknlabs.expensetracker.models.UserTier.FREE)
    }
}
