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
        assertEquals("1+2+3", entry.expression)
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
        // backspace edits that result rather than restoring "10+5".
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
    fun `backspace deletes the whole highlighted token range in one press`() = runTest {
        // Expression "1+2+3" renders as "1+2+3"; display offsets 1..3
        // cover the tokens "+", "2" (index 1..2).
        listOf("1", "+", "2", "+", "3").forEach { viewModel.handleNormalAction(it) }

        viewModel.setSelectionFromDisplay(selectionStart = 1, selectionEnd = 3)
        assertTrue(viewModel.uiState.value.tokenState.hasSelection)

        viewModel.handleNormalAction("BACKSPACE")

        assertEquals("1+3", viewModel.uiState.value.normalRawExpression)
        assertEquals(1, viewModel.uiState.value.tokenState.cursorPosition)
        assertEquals(4, viewModel.uiState.value.normalDisplay.toDouble().toInt())
    }

    @Test
    fun `typing a digit overwrites the highlighted selection`() = runTest {
        listOf("1", "+", "2", "+", "3").forEach { viewModel.handleNormalAction(it) }
        viewModel.setSelectionFromDisplay(selectionStart = 1, selectionEnd = 3)

        viewModel.handleNormalAction("9")

        // "1+3" with the run replaced by 9 -> 19+3
        assertEquals("19+3", viewModel.uiState.value.normalRawExpression)
        assertEquals(22, viewModel.uiState.value.normalDisplay.toDouble().toInt())
    }

    @Test
    fun `out of range selection is clamped and still safe to delete`() = runTest {
        listOf("1", "+", "2").forEach { viewModel.handleNormalAction(it) }

        // Negative start and past-the-end end: both ends clamp to 0..tokens.size.
        viewModel.setSelectionFromDisplay(selectionStart = -40, selectionEnd = 999)
        viewModel.handleNormalAction("BACKSPACE")

        assertEquals("", viewModel.uiState.value.normalRawExpression)
        assertEquals("0", viewModel.uiState.value.normalDisplay)
        assertEquals(0, viewModel.uiState.value.tokenState.cursorPosition)
    }

    @Test
    fun `backspace with the caret parked before a token removes that whole token`() = runTest {
        // 12 + 3, then CURSOR_LEFT moves the caret in front of "3".
        listOf("1", "2", "+", "3").forEach { viewModel.handleNormalAction(it) }
        viewModel.handleNormalAction("CURSOR_LEFT")

        viewModel.handleNormalAction("BACKSPACE")

        assertEquals("123", viewModel.uiState.value.normalRawExpression)
    }

    @Test
    fun `backspace on an empty expression is a no-op`() = runTest {
        viewModel.handleNormalAction("AC")

        viewModel.handleNormalAction("BACKSPACE")

        assertEquals("0", viewModel.uiState.value.normalDisplay)
        assertTrue(viewModel.uiState.value.tokenState.tokens.isEmpty())
    }

    @Test
    fun `deleting a token keeps the caret at the selection's left edge`() = runTest {
        listOf("1", "+", "2", "+", "3").forEach { viewModel.handleNormalAction(it) }

        // Highlight the trailing "+3" run (token indices 3..4).
        viewModel.setSelectionFromDisplay(selectionStart = 3, selectionEnd = 5)
        viewModel.handleNormalAction("BACKSPACE")

        // "1+2" remains and the caret sits immediately after "2".
        assertEquals("1+2", viewModel.uiState.value.normalRawExpression)
        assertEquals(3, viewModel.uiState.value.tokenState.cursorPosition)
        // "1" (1) + "+" (1) + "2" (1) = 3 - the field renders no "= " prefix.
        assertEquals(3, viewModel.getDisplayPositionFromCursor())
    }

    @Test
    fun `a tap keeps the caret inside a number and snaps to boundaries elsewhere`() = runTest {
        // "12+345": tokens are Number(12), +, Number(345) at display offsets
        // 0..2, 2..3 and 3..6.
        listOf("1", "2", "+", "3", "4", "5").forEach { viewModel.handleNormalAction(it) }

        // A tap at the end of "12" lands on that boundary (token index 1).
        viewModel.setCursorPositionFromDisplay(2)
        assertEquals(1, viewModel.uiState.value.tokenState.cursorPosition)
        assertEquals(0, viewModel.uiState.value.tokenState.cursorOffset)
        assertEquals(2, viewModel.getDisplayPositionFromCursor())

        // A tap one character into "345" is inside the number, so the caret
        // stays exactly there instead of jumping to either end - a digit typed
        // next has to insert at the tap, not at the number's edge.
        viewModel.setCursorPositionFromDisplay(4)
        assertEquals(2, viewModel.uiState.value.tokenState.cursorPosition)
        assertEquals(1, viewModel.uiState.value.tokenState.cursorOffset)
        assertEquals(4, viewModel.getDisplayPositionFromCursor())

        // A tap at the very end stays at the end.
        viewModel.setCursorPositionFromDisplay(6)
        assertEquals(3, viewModel.uiState.value.tokenState.cursorPosition)
        assertEquals(0, viewModel.uiState.value.tokenState.cursorOffset)
        assertEquals(6, viewModel.getDisplayPositionFromCursor())

        // The single-character "+" has no interior, so its tap still snaps to
        // the nearer boundary (index 2 is the "+", the caret goes before it).
        viewModel.setCursorPositionFromDisplay(2)
        assertEquals(1, viewModel.uiState.value.tokenState.cursorPosition)
        assertEquals(2, viewModel.getDisplayPositionFromCursor())
    }

    @Test
    fun `typing a digit with the caret inside a number inserts it at the caret`() = runTest {
        // "125+13-6", then tap between the "1" and the "2" of 125.
        listOf("1", "2", "5", "+", "1", "3", "-", "6").forEach {
            viewModel.handleNormalAction(it)
        }
        viewModel.setCursorPositionFromDisplay(1)
        assertEquals(0, viewModel.uiState.value.tokenState.cursorPosition)
        assertEquals(1, viewModel.uiState.value.tokenState.cursorOffset)

        viewModel.handleNormalAction("6")

        // The digit joins the left half of the number, so it reads "1625", not
        // "6125" (inserted before the whole number) and not "1256".
        assertEquals("1625+13-6", viewModel.uiState.value.normalRawExpression)
        // The caret follows the digit: immediately after "16".
        assertEquals(2, viewModel.getDisplayPositionFromCursor())
    }

    @Test
    fun `typing a digit inside a number splits it exactly once`() = runTest {
        listOf("1", "2", "5").forEach { viewModel.handleNormalAction(it) }

        // Tap after the "2" and type 9: "125" -> "1295".
        viewModel.setCursorPositionFromDisplay(2)
        viewModel.handleNormalAction("9")
        assertEquals("1295", viewModel.uiState.value.normalRawExpression)
        assertEquals(3, viewModel.getDisplayPositionFromCursor())

        // Typing again appends to the same run, so the split does not multiply.
        viewModel.handleNormalAction("7")
        assertEquals("12975", viewModel.uiState.value.normalRawExpression)
    }

    @Test
    fun `backspace with the caret inside a number removes one character at a time`() = runTest {
        listOf("1", "2", "5").forEach { viewModel.handleNormalAction(it) }

        // Caret between the "2" and the "5".
        viewModel.setCursorPositionFromDisplay(2)
        viewModel.handleNormalAction("BACKSPACE")

        // Only the "2" is gone, and the caret followed it to the left.
        assertEquals("15", viewModel.uiState.value.normalRawExpression)
        assertEquals(1, viewModel.getDisplayPositionFromCursor())

        viewModel.handleNormalAction("BACKSPACE")
        assertEquals("5", viewModel.uiState.value.normalRawExpression)
        assertEquals(0, viewModel.getDisplayPositionFromCursor())

        // The caret is at the very start now, so nothing is left to delete and
        // the number must survive.
        viewModel.handleNormalAction("BACKSPACE")
        assertEquals("5", viewModel.uiState.value.normalRawExpression)
    }

    @Test
    fun `backspace after a number still eats one digit at a time`() = runTest {
        listOf("1", "2", "5").forEach { viewModel.handleNormalAction(it) }

        // The caret sits after the number, which is the pre-existing path and
        // must keep shrinking the number rather than deleting it whole.
        viewModel.handleNormalAction("BACKSPACE")
        assertEquals("12", viewModel.uiState.value.normalRawExpression)
        viewModel.handleNormalAction("BACKSPACE")
        assertEquals("1", viewModel.uiState.value.normalRawExpression)
        viewModel.handleNormalAction("BACKSPACE")
        assertEquals("0", viewModel.uiState.value.normalDisplay)
        assertTrue(viewModel.uiState.value.tokenState.tokens.isEmpty())
    }

    @Test
    fun `cursor keys step across a number from a mid-number caret`() = runTest {
        listOf("1", "2", "5", "+", "1").forEach { viewModel.handleNormalAction(it) }
        viewModel.setCursorPositionFromDisplay(1)

        // Right: past the whole number, not one digit at a time.
        viewModel.handleNormalAction("CURSOR_RIGHT")
        assertEquals(0, viewModel.uiState.value.tokenState.cursorOffset)
        assertEquals(3, viewModel.getDisplayPositionFromCursor())

        // Left: back to the number's own start, then over the whole token.
        viewModel.setCursorPositionFromDisplay(1)
        viewModel.handleNormalAction("CURSOR_LEFT")
        assertEquals(0, viewModel.getDisplayPositionFromCursor())
        viewModel.handleNormalAction("CURSOR_LEFT")
        assertEquals(0, viewModel.getDisplayPositionFromCursor())
    }

    @Test
    fun `operator with a mid-number caret appends after the whole number`() = runTest {
        listOf("1", "2", "5").forEach { viewModel.handleNormalAction(it) }
        viewModel.setCursorPositionFromDisplay(1)

        viewModel.handleNormalAction("+")

        // Operators are token-granular, so "125" is finished first and the
        // "+" lands after it - it never rewrites the token before the number.
        assertEquals("125+", viewModel.uiState.value.normalRawExpression)
        assertEquals(4, viewModel.getDisplayPositionFromCursor())
    }

    @Test
    fun `expression box renders operators and brackets tight with no padding spaces`() = runTest {
        // This is the string the display actually shows (buildExpression feeds it),
        // so the spacing is pinned here rather than inferred from the token model.
        listOf("1", "+", "2", "*", "3").forEach { viewModel.handleNormalAction(it) }

        assertEquals("1+2×3", viewModel.buildExpression())

        viewModel.handleNormalAction("AC")
        listOf("2", "*", "(", "3", "+", "4", ")").forEach { viewModel.handleNormalAction(it) }

        assertEquals("2×(3+4)", viewModel.buildExpression())
    }

    @Test
    fun `recorded entries are exposed in ui state newest first`() = runTest {
        // 12 + 5 = 17
        listOf("1", "2", "+", "5", "=").forEach { viewModel.handleNormalAction(it) }
        // 9 × 3 = 27
        listOf("9", "*", "3", "=").forEach { viewModel.handleNormalAction(it) }

        // Repository flow has propagated into ui state, newest first.
        assertEquals(2, viewModel.uiState.value.historyEntries.size)
        assertEquals("9×3", viewModel.uiState.value.historyEntries.first().expression)
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

    // ------------------------------------------------------------------
    // Behaviour checklist: the scenarios the spec calls out by name, pinned so
    // a regression cannot quietly change them.
    // ------------------------------------------------------------------

    @Test
    fun `spec scenario B - a digit typed with the caret between two digits lands at the caret`() = runTest {
        // "1|2" + 5 must give "152" with the caret after the 5 ("15|2").
        listOf("1", "2").forEach { viewModel.handleNormalAction(it) }
        viewModel.setCursorPositionFromDisplay(1)

        viewModel.handleNormalAction("5")

        assertEquals("152", viewModel.uiState.value.normalRawExpression)
        assertEquals(2, viewModel.getDisplayPositionFromCursor())
    }

    @Test
    fun `spec scenario A - backspace with the caret between two digits deletes the character to its left`() = runTest {
        // "1|2" + backspace must delete the 1, leaving "|2".
        listOf("1", "2").forEach { viewModel.handleNormalAction(it) }
        viewModel.setCursorPositionFromDisplay(1)

        viewModel.handleNormalAction("BACKSPACE")

        assertEquals("2", viewModel.uiState.value.normalRawExpression)
        assertEquals(0, viewModel.getDisplayPositionFromCursor())
    }

    @Test
    fun `spec digits and operators keep a live preview of a valid expression`() = runTest {
        listOf("1", "2", "+", "3").forEach { viewModel.handleNormalAction(it) }

        // The expression stays in the field while the running total is previewed.
        assertEquals("12+3", viewModel.uiState.value.normalRawExpression)
        assertEquals("15", viewModel.uiState.value.tokenState.previewResult)
    }

    @Test
    fun `spec a new operator replaces the previous one instead of stacking`() = runTest {
        listOf("5", "+", "*").forEach { viewModel.handleNormalAction(it) }

        assertEquals("5*", viewModel.uiState.value.normalRawExpression)
    }

    @Test
    fun `spec parenthesis key after a number multiplies implicitly`() = runTest {
        // Spec: "5(2) evaluates as 5 x 2".
        listOf("5", "(", "2", ")").forEach { viewModel.handleNormalAction(it) }
        assertEquals("5(2)", viewModel.uiState.value.normalRawExpression)

        viewModel.handleNormalAction("=")

        assertEquals("10", viewModel.uiState.value.normalDisplay)
    }

    @Test
    fun `spec implicit multiplication works between two groups too`() = runTest {
        listOf("(", "1", "+", "2", ")", "(", "3", ")").forEach {
            viewModel.handleNormalAction(it)
        }

        viewModel.handleNormalAction("=")

        assertEquals("9", viewModel.uiState.value.normalDisplay)
    }

    @Test
    fun `spec the smart bracket still inserts an explicit multiply after a number`() = runTest {
        // onBracketClicked() is what the "(" key sends now: after a number it
        // writes the "x" out, and evaluates the same way.
        viewModel.handleNormalAction("5")
        viewModel.onBracketClicked()
        listOf("2", ")").forEach { viewModel.handleNormalAction(it) }

        assertEquals("5*(2)", viewModel.uiState.value.normalRawExpression)
        viewModel.handleNormalAction("=")
        assertEquals("10", viewModel.uiState.value.normalDisplay)
    }

    @Test
    fun `spec the decimal point lands at the caret`() = runTest {
        // Was token-granular ("1|2" + "." gave "12."); the spec groups "." with
        // the digits, so it now inserts exactly where the caret is.
        listOf("1", "2").forEach { viewModel.handleNormalAction(it) }
        viewModel.setCursorPositionFromDisplay(1)

        viewModel.handleNormalAction(".")

        assertEquals("1.2", viewModel.uiState.value.normalRawExpression)
        assertEquals(2, viewModel.getDisplayPositionFromCursor())
    }

    @Test
    fun `a second decimal point inside one number is refused`() = runTest {
        listOf("1", ".", "5").forEach { viewModel.handleNormalAction(it) }

        // Caret between the "." and the "5" - the number already carries its one
        // point, so the key is a no-op rather than producing "1..5".
        viewModel.setCursorPositionFromDisplay(2)
        viewModel.handleNormalAction(".")

        assertEquals("1.5", viewModel.uiState.value.normalRawExpression)
        assertEquals(2, viewModel.getDisplayPositionFromCursor())
    }

    @Test
    fun `spec equals leaves the expression in the panel and the result underneath`() = runTest {
        listOf("5", "+", "3").forEach { viewModel.handleNormalAction(it) }

        viewModel.handleNormalAction("=")

        // The panel keeps the expression; the result moves to the "= ..." line.
        assertEquals("5+3", viewModel.buildExpression())
        assertEquals("8", viewModel.calculatePreview())
        // The caret sits at the end of the text that is actually on screen.
        assertEquals(3, viewModel.getDisplayPositionFromCursor())
    }

    @Test
    fun `spec an uneven expression stays visible when equals reports Error`() = runTest {
        listOf("5", "+").forEach { viewModel.handleNormalAction(it) }

        viewModel.handleNormalAction("=")

        assertEquals("5+", viewModel.buildExpression())
        assertEquals("Error", viewModel.calculatePreview())
    }

    @Test
    fun `after equals an operator still resumes from the result`() = runTest {
        listOf("5", "+", "3").forEach { viewModel.handleNormalAction(it) }
        viewModel.handleNormalAction("=")

        viewModel.handleNormalAction("+")

        // Unchanged behaviour: continuing a calculation builds on the result,
        // and the panel drops back to the live expression.
        assertEquals("8+", viewModel.buildExpression())
    }

    @Test
    fun `after equals a digit starts a fresh expression`() = runTest {
        listOf("5", "+", "3").forEach { viewModel.handleNormalAction(it) }
        viewModel.handleNormalAction("=")

        viewModel.handleNormalAction("7")

        assertEquals("7", viewModel.buildExpression())
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
