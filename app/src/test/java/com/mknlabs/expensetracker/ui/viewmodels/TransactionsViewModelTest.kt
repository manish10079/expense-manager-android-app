package com.mknlabs.expensetracker.ui.viewmodels

import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.domain.repository.TransactionQuery
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.domain.repository.TransactionSummary
import com.mknlabs.expensetracker.domain.repository.TransactionTotals
import com.mknlabs.expensetracker.domain.repository.RecentTransaction
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.domain.usecase.ObserveAccessStatusUseCase
import com.mknlabs.expensetracker.ui.components.TransactionPeriodFilter
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
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
        // Run filter updates without a debounce delay.
        viewModel.filterDebounceMillis = 0

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
    fun `selectAll selects every loaded id and selecting again clears`() {
        // Act
        viewModel.selectAll(setOf("a", "b"))

        // Assert
        assertEquals(setOf("a", "b"), viewModel.uiState.value.selectedTransactionIds)
        assertTrue(viewModel.uiState.value.isSelectionMode)

        // Act: selecting the same set again toggles off
        viewModel.selectAll(setOf("a", "b"))

        // Assert
        assertTrue(viewModel.uiState.value.selectedTransactionIds.isEmpty())
        assertFalse(viewModel.uiState.value.isSelectionMode)
    }

    @Test
    fun `selectAllInQuery selects ids returned by the repository query`() {
        // Arrange
        fakeRepository.stubTransactions = listOf(
            transaction("t_1", System.currentTimeMillis()),
            transaction("t_2", System.currentTimeMillis())
        )

        // Act
        viewModel.selectAllInQuery()

        // Assert
        awaitUiState { it.selectedTransactionIds == setOf("t_1", "t_2") }
        assertTrue(viewModel.uiState.value.isSelectionMode)
    }

    // ─── Query pushdown ───────────────────────────────────────────────

    @Test
    fun `search query is pushed into the paging query`() {
        viewModel.updateSearchQuery("coffee")

        awaitQuery { it.search == "coffee" }
    }

    @Test
    fun `category filter is pushed into the paging query`() {
        viewModel.toggleCategory(7)

        awaitQuery { it.categoryIds == listOf(7) }
    }

    @Test
    fun `amount range is pushed into the paging query in minor units`() {
        viewModel.updateMinAmount("10")
        viewModel.updateMaxAmount("25.5")

        awaitQuery { it.minAmountMinor == 1_000L && it.maxAmountMinor == 2_550L }
    }

    @Test
    fun `monthly period bounds the query window`() {
        viewModel.updatePeriodFilter(TransactionPeriodFilter.MONTHLY)

        awaitQuery {
            it.startMillis != TransactionQuery.NO_START && it.endMillis != TransactionQuery.NO_END
        }
    }

    @Test
    fun `all period leaves the query window unbounded`() {
        viewModel.updatePeriodFilter(TransactionPeriodFilter.ALL)

        awaitQuery {
            it.startMillis == TransactionQuery.NO_START && it.endMillis == TransactionQuery.NO_END
        }
    }

    @Test
    fun `total count comes from the repository aggregate`() {
        fakeRepository.stubTransactions = listOf(
            transaction("t_1", System.currentTimeMillis()),
            transaction("t_2", System.currentTimeMillis())
        )
        viewModel.updateSearchQuery("x")

        awaitUiState { it.totalTransactionCount == 2 }
    }

    @Test
    fun `summary totals come from the repository aggregate not the loaded rows`() {
        // The repository reports totals for the whole filtered set; the loaded rows
        // are deliberately different so a page-summing bug would be caught here.
        fakeRepository.stubTransactions = listOf(transaction("t_1", System.currentTimeMillis(), 9_999L))
        fakeRepository.stubIncomeMinor = 5_000L
        fakeRepository.stubExpenseMinor = 2_500L

        viewModel.updateSearchQuery("x")

        awaitUiState {
            it.summaryIncomeMinor == 5_000L &&
                it.summaryExpenseMinor == 2_500L &&
                it.totalTransactionCount == 1 &&
                !it.isSummaryLoading
        }
    }

    @Test
    fun `free text filters are debounced`() {
        // A delay this long never elapses under the test scheduler, so the query
        // must still be the one from before the keystroke.
        viewModel.filterDebounceMillis = 60_000L

        viewModel.updateSearchQuery("coffee")

        // The text field (and its chip) update straight away...
        assertEquals("coffee", viewModel.uiState.value.searchQuery)
        // ...while the query rebuild waits for the user to stop typing.
        assertTrue(fakeRepository.lastQuery?.search != "coffee")
    }

    @Test
    fun `resetFilters restores the default query`() {
        viewModel.updateSearchQuery("coffee")
        viewModel.toggleCategory(7)
        awaitQuery { it.search == "coffee" && it.categoryIds == listOf(7) }

        viewModel.resetFilters()

        awaitQuery { it.search == null && it.categoryIds.isEmpty() && it.transactionTypeIds == listOf(1, 2) }
    }

    private fun transaction(
        id: String,
        createdAt: Long,
        amountMinor: Long = 1_000L,
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

    /**
     * Query updates are published from a `Dispatchers.IO` coroutine, so poll until
     * the repository has seen the expected query.
     */
    private fun awaitQuery(timeoutMs: Long = 5_000, predicate: (TransactionQuery) -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val query = fakeRepository.lastQuery
            if (query != null && predicate(query)) return
            Thread.sleep(10)
        }
        assertTrue(
            "Query condition not met within ${timeoutMs}ms (last=${fakeRepository.lastQuery})",
            fakeRepository.lastQuery?.let(predicate) == true
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

    // Manual Fake implementation
    private class FakeTransactionRepository : TransactionRepository {
        var stubTransactions: List<Transaction> = emptyList()
        var stubIncomeMinor: Long = 0L
        var stubExpenseMinor: Long = 0L
        var lastQuery: TransactionQuery? = null

        override fun observeActiveTransactions(): Flow<List<Transaction>> = flowOf(stubTransactions)
        override fun observeHomeSummary(
            currentMonthStartMillis: Long,
            currentMonthEndMillis: Long,
            previousMonthStartMillis: Long,
            previousMonthEndMillis: Long,
            todayStartMillis: Long,
            todayEndMillis: Long
        ): Flow<TransactionSummary> = flowOf(TransactionSummary(0, 0, 0, 0, 0))
        override fun observeRecentTransactions(limit: Int): Flow<List<RecentTransaction>> = flowOf(emptyList())
        override fun observeActiveTransactionCount(): Flow<Int> = flowOf(stubTransactions.size)
        override suspend fun getTransactionById(id: String): Transaction? = null
        override suspend fun upsertTransaction(transaction: Transaction): Transaction = transaction
        override suspend fun softDeleteTransaction(id: String) {}
        override suspend fun softDeleteTransactions(ids: List<String>) {}
        override suspend fun deleteAllTransactions() {}

        override fun getTransactionsPaging(query: TransactionQuery): Flow<PagingData<Transaction>> {
            lastQuery = query
            return flowOf(PagingData.from(stubTransactions))
        }

        override suspend fun getTransactionIds(query: TransactionQuery): List<String> {
            lastQuery = query
            return stubTransactions.map { it.id }
        }

        override fun observeTransactionTotals(query: TransactionQuery): Flow<TransactionTotals> {
            lastQuery = query
            return flowOf(
                TransactionTotals(
                    incomeMinor = stubIncomeMinor,
                    expenseMinor = stubExpenseMinor,
                    totalCount = stubTransactions.size
                )
            )
        }

        override suspend fun getRangeSummary(startMillis: Long, endMillis: Long): TransactionSummary =
            TransactionSummary(0, 0, 0, 0, 0)

        override suspend fun hasTransactionsInRange(startMillis: Long, endMillis: Long): Boolean {
            return stubTransactions.any { it.createdAt in startMillis until endMillis }
        }
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
