package com.mknlabs.expensetracker.ui.viewmodels

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
import org.junit.Assert.assertTrue
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
    fun `backspace after equals edits the expression instead of wiping it`() = runTest {
        listOf("1", "0", "+", "5", "=").forEach { viewModel.handleNormalAction(it) }
        assertEquals("15", viewModel.uiState.value.normalDisplay)
        assertTrue(viewModel.uiState.value.shouldResetNormalDisplay)

        viewModel.handleNormalAction("BACKSPACE")

        assertEquals("10 +", viewModel.uiState.value.normalRawExpression)
        assertEquals("10", viewModel.uiState.value.normalDisplay)
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

        assertEquals("1,250 × 450", viewModel.uiState.value.normalRawExpression)
        assertEquals("562,500", viewModel.uiState.value.normalDisplay)
    }

    @Test
    fun `deleteHistoryEntry removes matching entry`() = runTest {
        historyRepository.addEntry("12 + 5", "17")
        val entry = historyRepository.entries.first()

        viewModel.deleteHistoryEntry(entry.timestampMillis)

        assertTrue(historyRepository.entries.isEmpty())
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
