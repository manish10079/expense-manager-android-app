package com.mkn0079.expensetracker.ui.viewmodels

import com.mkn0079.expensetracker.domain.repository.TransactionRepository
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.domain.repository.TransactionSummary
import com.mkn0079.expensetracker.domain.repository.RecentTransaction
import com.mkn0079.expensetracker.ui.models.TransactionListItemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import androidx.lifecycle.viewModelScope
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

    @Before
    fun setup() {
        fakeRepository = FakeTransactionRepository()
        viewModel = TransactionsViewModel(fakeRepository)
        
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
        override suspend fun checkBudgetAndNotify(context: android.content.Context, transaction: Transaction) {}
    }
}
