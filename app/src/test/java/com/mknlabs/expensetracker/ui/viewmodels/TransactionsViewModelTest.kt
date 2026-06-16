package com.mknlabs.expensetracker.ui.viewmodels

import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.domain.repository.TransactionSummary
import com.mknlabs.expensetracker.domain.repository.RecentTransaction
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.domain.usecase.ObserveAccessStatusUseCase
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
    }
}
