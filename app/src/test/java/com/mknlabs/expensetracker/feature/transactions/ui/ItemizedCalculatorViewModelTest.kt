package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.lifecycle.SavedStateHandle
import com.mknlabs.expensetracker.domain.repository.CalculatorHistoryRepository
import com.mknlabs.expensetracker.domain.usecase.BuildBreakdownNoteUseCase
import com.mknlabs.expensetracker.domain.usecase.ParseBreakdownNoteUseCase
import com.mknlabs.expensetracker.models.CalculatorHistoryEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import com.mknlabs.expensetracker.utils.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItemizedCalculatorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var historyRepository: FakeCalculatorHistoryRepository
    private lateinit var viewModel: ItemizedCalculatorViewModel

    @Before
    fun setup() {
        historyRepository = FakeCalculatorHistoryRepository()
        viewModel = ItemizedCalculatorViewModel(
            parseBreakdownNoteUseCase = ParseBreakdownNoteUseCase(),
            buildBreakdownNoteUseCase = BuildBreakdownNoteUseCase(),
            calculatorHistoryRepository = historyRepository,
            savedStateHandle = SavedStateHandle()
        )
    }

    @org.junit.After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `pressEquals on multi operand calculation records full expression`() = runTest {
        // Act: 1 + 2 + 3 =
        listOf("1", "+", "2", "+", "3", "=").forEach { viewModel.handleNormalAction(it) }

        // Assert
        assertEquals(1, historyRepository.entries.size)
        val entry = historyRepository.entries.single()
        assertEquals("1 + 2 + 3", entry.expression)
        assertEquals("6", entry.result)
    }

    @Test
    fun `pressEquals with no pending operator does not record`() = runTest {
        viewModel.handleNormalAction("=")

        assertTrue(historyRepository.entries.isEmpty())
    }

    @Test
    fun `backspace after equals edits the result and re-enables input`() = runTest {
        listOf("1", "0", "+", "5", "=").forEach { viewModel.handleNormalAction(it) }
        assertEquals("15", viewModel.uiState.value.normalDisplay)
        assertTrue(viewModel.uiState.value.shouldResetNormalDisplay)

        viewModel.handleNormalAction("BACKSPACE")

        // Equals collapses the expression to a single result token ("15"), so
        // backspace edits that result rather than restoring "10 + 5".
        assertEquals("1", viewModel.uiState.value.normalRawExpression)
        assertEquals("1", viewModel.uiState.value.normalDisplay)
        assertTrue(!viewModel.uiState.value.shouldResetNormalDisplay)
    }

    @Test
    fun `backspace after equals then editing the expression evaluates correctly`() = runTest {
        listOf("1", "0", "+", "5", "=").forEach { viewModel.handleNormalAction(it) }

        viewModel.handleNormalAction("BACKSPACE") // 10 + 5 -> 10 +
        viewModel.handleNormalAction("7")        // 10 + 7
        viewModel.handleNormalAction("=")

        assertEquals("17", viewModel.uiState.value.normalDisplay)
    }

    @Test
    fun `operator after equals continues from the result`() = runTest {
        listOf("1", "0", "+", "5", "=").forEach { viewModel.handleNormalAction(it) }

        viewModel.handleNormalAction("+")
        viewModel.handleNormalAction("3")
        viewModel.handleNormalAction("=")

        assertEquals("18", viewModel.uiState.value.normalDisplay)
    }

    @Test
    fun `divide by zero records nothing`() = runTest {
        listOf("8", "/", "0", "=").forEach { viewModel.handleNormalAction(it) }

        assertTrue(historyRepository.entries.isEmpty())
    }

    @Test
    fun `recorded entries are exposed in ui state newest first`() = runTest {
        // 12 + 5 = 17
        listOf("1", "2", "+", "5", "=").forEach { viewModel.handleNormalAction(it) }
        // 9 × 3 = 27
        listOf("9", "*", "3", "=").forEach { viewModel.handleNormalAction(it) }

        // Repository flow has propagated into ui state, newest first.
        assertEquals(2, viewModel.uiState.value.historyEntries.size)
        assertEquals("9 × 3", viewModel.uiState.value.historyEntries.first().expression)
        assertEquals("27", viewModel.uiState.value.historyEntries.first().result)
    }

    @Test
    fun `clearHistory empties recorded entries`() = runTest {
        listOf("1", "2", "+", "5", "=").forEach { viewModel.handleNormalAction(it) }
        assertEquals(1, historyRepository.entries.size)

        viewModel.clearHistory()

        assertTrue(historyRepository.entries.isEmpty())
    }

    @Test
    fun `restoreHistoryExpression restores full expression string and evaluates display`() = runTest {
        viewModel.restoreHistoryExpression("1,250 × 450")

        // normalRawExpression is the evaluator's ASCII form (no separators/spaces).
        assertEquals("1250*450", viewModel.uiState.value.normalRawExpression)
        assertEquals("562,500", viewModel.uiState.value.normalDisplay)
    }

    @Test
    fun `deleteHistoryEntry removes matching entry`() = runTest {
        historyRepository.addEntry("12 + 5", "17")
        val entry = historyRepository.entries.first()

        viewModel.deleteHistoryEntry(entry.timestampMillis)

        assertTrue(historyRepository.entries.isEmpty())
    }

    // Itemized rows: the add and edit paths share one draft, so the flags that pick which
    // one addItem performs are worth pinning down.

    @Test
    fun `startEditingItem prefills the draft from the row`() {
        addRow("Coffee", "4.50")
        val row = viewModel.uiState.value.items.single()

        viewModel.startEditingItem(row)

        val state = viewModel.uiState.value
        assertTrue(state.isAddingItem)
        assertEquals(row.id, state.editingItemId)
        assertEquals("Coffee", state.descriptionInput)
        assertEquals("4.5", state.amountInput)
        assertTrue(state.canAddItem)
    }

    @Test
    fun `editing a row replaces it in place and keeps its id and position`() {
        addRow("Coffee", "4.50")
        addRow("Sandwich", "9")

        val first = viewModel.uiState.value.items.first()
        viewModel.startEditingItem(first)
        viewModel.updateDescriptionInput("Flat white")
        viewModel.updateAmountInput("5")
        viewModel.addItem()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertEquals(listOf(first.id, first.id + 1), state.items.map { it.id })
        assertEquals("Flat white", state.items[0].description)
        assertEquals(5.0, state.items[0].amount, 0.0)
        assertEquals("Sandwich", state.items[1].description)
        assertEquals(14.0, state.totalAmount, 0.0)
        assertNull(state.editingItemId)
        assertTrue(!state.isAddingItem)
    }

    @Test
    fun `cancelling an edit clears the pending id so the next add appends`() {
        addRow("Coffee", "4.50")
        val row = viewModel.uiState.value.items.single()

        viewModel.startEditingItem(row)
        viewModel.cancelAddingItem()

        val cancelled = viewModel.uiState.value
        assertNull(cancelled.editingItemId)
        assertTrue(!cancelled.isAddingItem)
        assertEquals("", cancelled.descriptionInput)

        // A stale editingItemId here would silently overwrite the cancelled row.
        addRow("Sandwich", "9")

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertEquals("Coffee", state.items[0].description)
        assertEquals("Sandwich", state.items[1].description)
        assertEquals(13.5, state.totalAmount, 0.0)
    }

    @Test
    fun `editing the only row keeps its id when it is re-saved`() {
        addRow("Coffee", "4.50")
        val row = viewModel.uiState.value.items.single()

        viewModel.startEditingItem(row)
        viewModel.updateAmountInput("6.25")
        viewModel.addItem()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(row.id, state.items.single().id)
        assertEquals("Coffee", state.items.single().description)
        assertEquals(6.25, state.totalAmount, 0.0)
    }

    private fun addRow(description: String, amount: String) {
        viewModel.startAddingItem()
        viewModel.updateDescriptionInput(description)
        viewModel.updateAmountInput(amount)
        viewModel.addItem()
    }
}

private class FakeCalculatorHistoryRepository : CalculatorHistoryRepository {

    val entries = mutableListOf<CalculatorHistoryEntry>()

    private val _history = MutableStateFlow<List<CalculatorHistoryEntry>>(emptyList())
    override fun observeHistory(): Flow<List<CalculatorHistoryEntry>> = _history.asStateFlow()

    override suspend fun addEntry(expression: String, result: String) {
        entries.add(0, CalculatorHistoryEntry(expression, result, System.currentTimeMillis()))
        _history.value = entries.toList()
    }

    override suspend fun clearHistory() {
        entries.clear()
        _history.value = emptyList()
    }

    override suspend fun deleteEntry(timestampMillis: Long) {
        entries.removeAll { it.timestampMillis == timestampMillis }
        _history.value = entries.toList()
    }
}
