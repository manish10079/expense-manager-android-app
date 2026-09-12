package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.CalculatorHistoryRepository
import com.mknlabs.expensetracker.domain.usecase.BuildBreakdownNoteUseCase
import com.mknlabs.expensetracker.domain.usecase.ParseBreakdownNoteUseCase
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CalculatorHistoryEntry
import com.mknlabs.expensetracker.models.CalculatorLineItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DecimalFormat
import javax.inject.Inject

// Enums & lightweight data types

enum class CalculatorMode(val title: String) {
    ITEMIZED("Itemized"),
    NORMAL("Normal")
}

/** Retained for source-compatibility. Active state lives in NormalCalculatorTokenState. */
data class NormalCalculatorResult(
    val display: String,
    val storedValue: Double?,
    val pendingOperator: String?,
    val shouldResetDisplay: Boolean
)

/**
 * Internal state of the MIUI-style token-based normal calculator.
 * @param tokens         Expression as a list of atomic CalculatorTokens.
 * @param cursorPosition Index 0..tokens.size.
 * @param isEvaluated    True while displaying the result of pressing "=".
 * @param previewResult  Live result string ("" when expression is incomplete).
 */
data class NormalCalculatorTokenState(
    val tokens: List<CalculatorToken> = emptyList(),
    val cursorPosition: Int = 0,
    val isEvaluated: Boolean = false,
    val previewResult: String = ""
)

data class ItemizedCalculatorUiState(
    val selectedMode: CalculatorMode = CalculatorMode.ITEMIZED,
    val items: List<CalculatorLineItem> = emptyList(),
    val isAddingItem: Boolean = false,
    val descriptionInput: String = "",
    val amountInput: String = "",
    // Normal calculator
    val normalDisplay: String = "0",
    val normalRawExpression: String = "",
    val normalStoredValue: Double? = null,
    val normalPendingOperator: String? = null,
    val shouldResetNormalDisplay: Boolean = false,
    // Token engine
    val tokenState: NormalCalculatorTokenState = NormalCalculatorTokenState(),
    // Shared
    val totalAmount: Double = 0.0,
    val canAddItem: Boolean = false,
    val historyEntries: List<CalculatorHistoryEntry> = emptyList()
)

@HiltViewModel
class ItemizedCalculatorViewModel @Inject constructor(
    private val parseBreakdownNoteUseCase: ParseBreakdownNoteUseCase,
    private val buildBreakdownNoteUseCase: BuildBreakdownNoteUseCase,
    private val calculatorHistoryRepository: CalculatorHistoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemizedCalculatorUiState())
    val uiState: StateFlow<ItemizedCalculatorUiState> = _uiState.asStateFlow()

    private val normalCalculatorFormatter = DecimalFormat("#,##0.########")

    // Operator display glyphs (avoids raw Unicode literals that get mojibake'd by some editors)
    private val SYM_MULTIPLY = "\u00D7" // x
    private val SYM_DIVIDE   = "\u00F7" // o/
    private val SYM_MINUS    = "\u2212" // minus sign

    init {
        viewModelScope.launch {
            calculatorHistoryRepository.observeHistory().collect { entries ->
                _uiState.update { it.copy(historyEntries = entries) }
            }
        }
    }

    // Itemized calculator

    fun initialize(initialNote: String?) {
        val restoredItems = parseBreakdownNoteUseCase(initialNote)
        _uiState.update {
            it.copy(
                items = restoredItems,
                totalAmount = restoredItems.sumOf { item -> item.amount }
            )
        }
    }

    fun setMode(mode: CalculatorMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun startAddingItem() {
        _uiState.update { it.copy(isAddingItem = true) }
    }

    fun cancelAddingItem() {
        _uiState.update {
            it.copy(
                isAddingItem = false,
                descriptionInput = "",
                amountInput = "",
                canAddItem = false
            )
        }
    }

    fun updateDescriptionInput(input: String) {
        _uiState.update {
            it.copy(
                descriptionInput = input,
                canAddItem = input.isNotBlank() && (it.amountInput.toDoubleOrNull() ?: 0.0) > 0
            )
        }
    }

    fun updateAmountInput(input: String) {
        val sanitized = sanitizeAmountInput(input)
        _uiState.update {
            it.copy(
                amountInput = sanitized,
                canAddItem = it.descriptionInput.isNotBlank() && (sanitized.toDoubleOrNull() ?: 0.0) > 0
            )
        }
    }

    fun addItem() {
        val state = _uiState.value
        val parsedAmount = state.amountInput.toDoubleOrNull()
        if (parsedAmount != null) {
            val nextId = (state.items.maxOfOrNull { it.id } ?: 0) + 1
            val newItem = CalculatorLineItem(
                id = nextId,
                description = state.descriptionInput.trim(),
                amount = parsedAmount
            )
            val updatedItems = state.items + newItem
            _uiState.update {
                it.copy(
                    items = updatedItems,
                    totalAmount = updatedItems.sumOf { item -> item.amount },
                    isAddingItem = false,
                    descriptionInput = "",
                    amountInput = "",
                    canAddItem = false
                )
            }
        }
    }

    fun deleteItem(itemId: Int) {
        _uiState.update { state ->
            val updatedItems = state.items.filter { it.id != itemId }
            state.copy(
                items = updatedItems,
                totalAmount = updatedItems.sumOf { item -> item.amount }
            )
        }
    }

    // Normal calculator: public entry point (routes all UI actions)

    /**
     * Single dispatch point used by the UI.
     * Supported action values:
     * - Digits: "0".."9"
     * - Decimal: "."
     * - Operators: "+", "-", "*", "/"
     * - Percent: "%"
     * - Brackets: "(", ")", "BRACKET" (smart MIUI combined bracket)
     * - Control: "AC", "BACKSPACE", "="
     * - Cursor: "CURSOR_LEFT", "CURSOR_RIGHT"
     */
    fun handleNormalAction(action: String) {
        val ts = _uiState.value.tokenState
        val newTs: NormalCalculatorTokenState = when (action) {
            "AC"           -> handleAc()
            "BACKSPACE"    -> handleBackspace(ts)
            "="            -> handleEquals(ts)
            "%"            -> handlePercent(ts)
            "("            -> handleBracketOpen(ts)
            ")"            -> handleBracketClose(ts)
            "BRACKET"      -> handleSmartBracket(ts)
            "."            -> handleDecimal(ts)
            "CURSOR_LEFT"  -> handleCursorLeft(ts)
            "CURSOR_RIGHT" -> handleCursorRight(ts)
            "+", "-", "*", "/" -> handleOperator(action, ts)
            else           -> handleDigit(action, ts)
        }
        syncTokenStateToLegacy(newTs)
    }

    // MIUI smart bracket (single () key)

    /** Fires the MIUI smart () key. */
    fun onBracketClicked() = handleNormalAction("BRACKET")

    /** Toggle between ITEMIZED and NORMAL mode. */
    fun onToggleExpandClicked() {
        val current = _uiState.value.selectedMode
        setMode(if (current == CalculatorMode.NORMAL) CalculatorMode.ITEMIZED else CalculatorMode.NORMAL)
    }

    fun onDigitClicked(digit: String) = handleNormalAction(digit)
    fun onDeleteClicked()             = handleNormalAction("BACKSPACE")
    fun onEqualsClicked()             = handleNormalAction("=")

    // Token mutation handlers

    private fun handleAc(): NormalCalculatorTokenState =
        NormalCalculatorTokenState()

    private fun handleBackspace(ts: NormalCalculatorTokenState): NormalCalculatorTokenState {
        if (ts.tokens.isEmpty() || ts.cursorPosition == 0) return ts
        val tokenToRemove = ts.tokens[ts.cursorPosition - 1]
        val newTokens: MutableList<CalculatorToken> = ts.tokens.toMutableList()
        return if (tokenToRemove is CalculatorToken.Number && tokenToRemove.value.length > 1) {
            newTokens[ts.cursorPosition - 1] =
                CalculatorToken.Number(tokenToRemove.value.dropLast(1))
            ts.copy(tokens = newTokens, isEvaluated = false, previewResult = computePreview(newTokens))
        } else {
            newTokens.removeAt(ts.cursorPosition - 1)
            ts.copy(
                tokens = newTokens,
                cursorPosition = ts.cursorPosition - 1,
                isEvaluated = false,
                previewResult = computePreview(newTokens)
            )
        }
    }

    private fun handleEquals(ts: NormalCalculatorTokenState): NormalCalculatorTokenState {
        if (ts.tokens.isEmpty()) return ts
        val evalStr = ts.tokens.toEvalString()
        val result  = TokenExpressionEvaluator.evaluate(evalStr)
        return if (result != null && result.isFinite()) {
            val resultStr = formatValue(result)
            val hasOp = ts.tokens.any { it is CalculatorToken.Operator }
            if (hasOp && resultStr != "Error") {
                viewModelScope.launch {
                    calculatorHistoryRepository.addEntry(
                        expression = ts.tokens.toDisplayString(),
                        result = resultStr
                    )
                }
            }
            val resultTokens = listOf(CalculatorToken.Number(resultStr.replace(",", "")))
            NormalCalculatorTokenState(
                tokens = resultTokens,
                cursorPosition = resultTokens.size,
                isEvaluated = true,
                previewResult = resultStr
            )
        } else {
            ts.copy(previewResult = "Error", isEvaluated = true)
        }
    }

    private fun handlePercent(ts: NormalCalculatorTokenState): NormalCalculatorTokenState {
        val result = TokenExpressionEvaluator.evaluate(ts.tokens.toEvalString()) ?: return ts
        if (!result.isFinite()) return ts
        val pctStr = formatValue(result / 100.0)
        val newTokens = listOf(CalculatorToken.Number(pctStr.replace(",", "")))
        return NormalCalculatorTokenState(
            tokens = newTokens, cursorPosition = newTokens.size,
            isEvaluated = true, previewResult = pctStr
        )
    }

    private fun handleBracketOpen(ts: NormalCalculatorTokenState): NormalCalculatorTokenState =
        insertTokens(ts, listOf(CalculatorToken.ParenthesisOpen), advanceCursor = 1)

    private fun handleBracketClose(ts: NormalCalculatorTokenState): NormalCalculatorTokenState =
        if (ts.tokens.unclosedParenCount > 0)
            insertTokens(ts, listOf(CalculatorToken.ParenthesisClose), advanceCursor = 1)
        else ts

    /**
     * MIUI smart bracket logic:
     * - null / Operator / "(" at cursor -> insert "("
     * - Number / ")" at cursor AND unclosed > 0 -> insert ")"
     * - Number / ")" at cursor AND unclosed == 0 -> insert "x("
     */
    private fun handleSmartBracket(ts: NormalCalculatorTokenState): NormalCalculatorTokenState {
        val base      = if (ts.isEvaluated) ts.copy(isEvaluated = false) else ts
        val lastToken = if (base.cursorPosition > 0) base.tokens[base.cursorPosition - 1] else null
        val openCount = base.tokens.unclosedParenCount

        return when {
            lastToken == null
                || lastToken is CalculatorToken.Operator
                || lastToken is CalculatorToken.ParenthesisOpen ->
                insertTokens(base, listOf(CalculatorToken.ParenthesisOpen), advanceCursor = 1)

            (lastToken is CalculatorToken.Number || lastToken is CalculatorToken.ParenthesisClose)
                && openCount > 0 ->
                insertTokens(base, listOf(CalculatorToken.ParenthesisClose), advanceCursor = 1)

            else ->
                insertTokens(
                    base,
                    listOf(
                        CalculatorToken.Operator(SYM_MULTIPLY, "*"),
                        CalculatorToken.ParenthesisOpen
                    ),
                    advanceCursor = 2
                )
        }
    }

    private fun handleDecimal(ts: NormalCalculatorTokenState): NormalCalculatorTokenState {
        val base      = clearIfEvaluated(ts)
        val lastToken = if (base.cursorPosition > 0) base.tokens[base.cursorPosition - 1] else null
        return when {
            lastToken is CalculatorToken.Number && !lastToken.value.contains('.') -> {
                val newTokens = base.tokens.toMutableList()
                newTokens[base.cursorPosition - 1] = CalculatorToken.Number(lastToken.value + ".")
                base.copy(tokens = newTokens, previewResult = computePreview(newTokens))
            }
            lastToken == null
                || lastToken is CalculatorToken.Operator
                || lastToken is CalculatorToken.ParenthesisOpen ->
                insertTokens(base, listOf(CalculatorToken.Number("0.")), advanceCursor = 1)
            else -> base
        }
    }

    private fun handleOperator(op: String, ts: NormalCalculatorTokenState): NormalCalculatorTokenState {
        // After pressing "=", keep the result and continue with the operator
        // (e.g. 5 + 3 = 8, press + → 8 +, type 7 → 8 + 7)
        val base = if (ts.isEvaluated) {
            ts.copy(isEvaluated = false, previewResult = "")
        } else {
            ts
        }
        val newToken  = toOperatorToken(op)
        val lastToken = if (base.cursorPosition > 0) base.tokens[base.cursorPosition - 1] else null
        return when {
            lastToken is CalculatorToken.Operator -> {
                val newTokens = base.tokens.toMutableList()
                newTokens[base.cursorPosition - 1] = newToken
                base.copy(tokens = newTokens, previewResult = computePreview(newTokens))
            }
            (lastToken == null || lastToken is CalculatorToken.ParenthesisOpen) && op == "-" ->
                insertTokens(base, listOf(CalculatorToken.Number(SYM_MINUS)), advanceCursor = 1)
            lastToken == null || lastToken is CalculatorToken.ParenthesisOpen -> base
            else -> insertTokens(base, listOf(newToken), advanceCursor = 1)
        }
    }

    private fun handleDigit(digit: String, ts: NormalCalculatorTokenState): NormalCalculatorTokenState {
        val base      = if (ts.isEvaluated) NormalCalculatorTokenState() else ts
        val lastToken = if (base.cursorPosition > 0) base.tokens[base.cursorPosition - 1] else null
        return when {
            lastToken is CalculatorToken.Number -> {
                val newValue = if (lastToken.value == "0" && digit != ".") digit
                               else lastToken.value + digit
                val newTokens = base.tokens.toMutableList()
                newTokens[base.cursorPosition - 1] = CalculatorToken.Number(newValue)
                base.copy(tokens = newTokens, previewResult = computePreview(newTokens))
            }
            else -> insertTokens(base, listOf(CalculatorToken.Number(digit)), advanceCursor = 1)
        }
    }

    private fun handleCursorLeft(ts: NormalCalculatorTokenState): NormalCalculatorTokenState =
        ts.copy(cursorPosition = (ts.cursorPosition - 1).coerceAtLeast(0))

    private fun handleCursorRight(ts: NormalCalculatorTokenState): NormalCalculatorTokenState =
        ts.copy(cursorPosition = (ts.cursorPosition + 1).coerceAtMost(ts.tokens.size))

    // ── Cursor ↔ Display position mapping ─────────────────────────────────

    /**
     * Converts a character position inside the display string
     * ("= 1 + 2 + 3") to the nearest token-boundary cursor index.
     * Called when the user taps the expression area.
     */
    fun setCursorPositionFromDisplay(displayPosition: Int) {
        val ts = _uiState.value.tokenState
        if (ts.tokens.isEmpty()) return

        // The display text is "= " + tokenDisplayString
        val prefixLen = "= ".length
        val adjusted  = (displayPosition - prefixLen).coerceAtLeast(0)

        // Walk tokens and find the boundary closest to `adjusted`.
        var charPos  = 0
        var bestIdx  = 0
        var bestDist = Int.MAX_VALUE
        for ((i, token) in ts.tokens.withIndex()) {
            val tokenLen = token.displaySymbol.length
            // Distance to the boundary BEFORE this token
            val distBefore = (adjusted - charPos).coerceAtLeast(0)
            if (distBefore < bestDist) {
                bestDist = distBefore
                bestIdx  = i
            }
            charPos += tokenLen
            // Distance to the boundary AFTER this token
            val distAfter = (adjusted - charPos).coerceAtLeast(0)
            if (distAfter < bestDist) {
                bestDist = distAfter
                bestIdx  = i + 1
            }
        }

        val clamped = bestIdx.coerceIn(0, ts.tokens.size)
        if (clamped != ts.cursorPosition) {
            _uiState.update { it.copy(tokenState = ts.copy(cursorPosition = clamped)) }
        }
    }

    /**
     * Returns the character index inside the display string that corresponds
     * to the current [NormalCalculatorTokenState.cursorPosition].
     * Used by the UI to keep the BasicTextField cursor in sync.
     */
    fun getDisplayPositionFromCursor(): Int {
        val ts       = _uiState.value.tokenState
        val prefixLen = "= ".length
        var pos = 0
        for (i in 0 until ts.cursorPosition.coerceAtMost(ts.tokens.size)) {
            pos += ts.tokens[i].displaySymbol.length
        }
        return pos + prefixLen
    }

    // History management

    fun selectHistoryResult(result: String) {
        val resultTokens = listOf(CalculatorToken.Number(result.replace(",", "")))
        syncTokenStateToLegacy(NormalCalculatorTokenState(
            tokens = resultTokens, cursorPosition = resultTokens.size,
            isEvaluated = true, previewResult = result
        ))
    }

    fun restoreHistoryExpression(expression: String) {
        val tokens  = parseDisplayExpressionToTokens(expression.trim())
        val preview = computePreview(tokens)
        syncTokenStateToLegacy(NormalCalculatorTokenState(
            tokens = tokens, cursorPosition = tokens.size,
            isEvaluated = false, previewResult = preview
        ))
    }

    fun deleteHistoryEntry(timestampMillis: Long) {
        viewModelScope.launch { calculatorHistoryRepository.deleteEntry(timestampMillis) }
    }

    fun clearHistory() {
        viewModelScope.launch { calculatorHistoryRepository.clearHistory() }
    }

    // Compatibility shims used by the existing Screen composable

    fun calculatePreview(): String {
        val ts = _uiState.value.tokenState
        return ts.previewResult.ifBlank { _uiState.value.normalDisplay }
    }

    fun buildExpression(): String? {
        val ts = _uiState.value.tokenState
        if (ts.tokens.isEmpty()) return null
        return ts.tokens.toDisplayString().trim()
    }

    // Itemized result export

    fun getFinalResult(
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences
    ): Pair<String, String> {
        val state = _uiState.value
        return Pair(
            formatEditableTotal(state.totalAmount),
            buildBreakdownNoteUseCase(state.items, currencyId, amountFormatPreferences)
        )
    }

    // Private helpers

    private fun insertTokens(
        ts: NormalCalculatorTokenState,
        newTokens: List<CalculatorToken>,
        advanceCursor: Int
    ): NormalCalculatorTokenState {
        val updated = ts.tokens.toMutableList()
        updated.addAll(ts.cursorPosition, newTokens)
        val finalTokens = updated.toList()
        return ts.copy(
            tokens = finalTokens,
            cursorPosition = ts.cursorPosition + advanceCursor,
            isEvaluated = false,
            previewResult = computePreview(finalTokens)
        )
    }

    private fun clearIfEvaluated(ts: NormalCalculatorTokenState): NormalCalculatorTokenState =
        if (ts.isEvaluated) NormalCalculatorTokenState() else ts

    private fun computePreview(tokens: List<CalculatorToken>): String {
        if (tokens.isEmpty()) return ""
        val result = TokenExpressionEvaluator.evaluateSafe(tokens.toEvalString()) ?: return ""
        return formatValue(result)
    }

    private fun syncTokenStateToLegacy(ts: NormalCalculatorTokenState) {
        val displayStr = if (ts.tokens.isEmpty()) "0" else ts.tokens.toDisplayString().trim()
        val previewStr = ts.previewResult.ifBlank { if (ts.tokens.isEmpty()) "0" else displayStr }
        _uiState.update {
            it.copy(
                tokenState = ts,
                normalDisplay = if (ts.isEvaluated) ts.previewResult else previewStr,
                normalRawExpression = ts.tokens.toEvalString(),
                shouldResetNormalDisplay = ts.isEvaluated
            )
        }
    }

    private fun formatValue(value: Double): String =
        if (value.isFinite()) normalCalculatorFormatter.format(value) else "Error"

    private fun formatEditableTotal(amount: Double): String =
        BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString()

    private fun sanitizeAmountInput(input: String): String {
        val filtered = input.filterIndexed { index, char ->
            char.isDigit() || (char == '.' && index == input.indexOf('.'))
        }
        val di = filtered.indexOf('.')
        return if (di >= 0) filtered.substring(0, di + 1) + filtered.substring(di + 1).take(2)
               else filtered
    }

    private fun toOperatorToken(op: String): CalculatorToken.Operator = when (op) {
        "*"  -> CalculatorToken.Operator(SYM_MULTIPLY, "*")
        "/"  -> CalculatorToken.Operator(SYM_DIVIDE, "/")
        "-"  -> CalculatorToken.Operator(SYM_MINUS, "-")
        else -> CalculatorToken.Operator(op, op)
    }

    /**
     * Parses a human-readable expression string (from history) back into tokens.
     * Recognises: numbers with optional decimals, operator glyphs x/o-/minus-sign,
     * ASCII operators (+, -, *, /), and parentheses.
     */
    private fun parseDisplayExpressionToTokens(expr: String): List<CalculatorToken> {
        val tokens = mutableListOf<CalculatorToken>()
        var i = 0
        while (i < expr.length) {
            val ch = expr[i]
            val cp = ch.code
            when {
                ch.isWhitespace() -> i++
                ch.isDigit() || ch == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i]); i++
                    }
                    tokens += CalculatorToken.Number(sb.toString())
                }
                // 0xD7 = multiply sign, 0x2A = *
                cp == 0xD7 || ch == '*' -> { tokens += CalculatorToken.Operator(SYM_MULTIPLY, "*"); i++ }
                // 0xF7 = divide sign, 0x2F = /
                cp == 0xF7 || ch == '/' -> { tokens += CalculatorToken.Operator(SYM_DIVIDE, "/"); i++ }
                // 0x2212 = minus sign, 0x2D = hyphen-minus
                cp == 0x2212 || ch == '-' -> { tokens += CalculatorToken.Operator(SYM_MINUS, "-"); i++ }
                ch == '+' -> { tokens += CalculatorToken.Operator("+", "+"); i++ }
                ch == '(' -> { tokens += CalculatorToken.ParenthesisOpen; i++ }
                ch == ')' -> { tokens += CalculatorToken.ParenthesisClose; i++ }
                else -> i++
            }
        }
        return tokens
    }
}

// Token-Aware Shunting-Yard Expression Evaluator

/**
 * Custom Shunting-yard evaluator working on a flat ASCII expression string.
 * Handles: integers, decimals, unary minus, +, -, *, / operators, parentheses.
 * Division by zero returns null.
 */
private object TokenExpressionEvaluator {

    /** Full evaluation -- null if expression is syntactically invalid. */
    fun evaluate(expression: String): Double? {
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) return null
        val rpn = toRPN(tokens) ?: return null
        return evalRPN(rpn)
    }

    /**
     * Safe evaluation for live-preview: drops trailing tokens until a valid
     * result is found, or returns null if nothing evaluates.
     */
    fun evaluateSafe(expression: String): Double? {
        var candidate = expression.trimEnd()
        while (candidate.isNotEmpty()) {
            val result = evaluate(candidate)
            if (result != null && result.isFinite()) return result
            candidate = candidate.dropLast(1).trimEnd()
        }
        return null
    }

    private fun tokenize(expr: String): List<String> {
        // Normalise display glyphs to ASCII for the evaluator
        val s = expr
            .replace("\u00D7", "*")
            .replace("\u00F7", "/")
            .replace("\u2212", "-")
            .replace(",", "")
        val result = mutableListOf<String>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) { sb.append(s[i]); i++ }
                    result.add(sb.toString())
                }
                c in "+-*/()%" -> {
                    if (c == '-' && (result.isEmpty() || result.last() in listOf("+", "-", "*", "/", "("))) {
                        val sb = StringBuilder("-")
                        i++
                        while (i < s.length && (s[i].isDigit() || s[i] == '.')) { sb.append(s[i]); i++ }
                        result.add(if (sb.length > 1) sb.toString() else "-")
                    } else {
                        result.add(c.toString()); i++
                    }
                }
                else -> i++
            }
        }
        return result
    }

    private fun precedence(op: String): Int = when (op) {
        "+", "-"      -> 1
        "*", "/", "%" -> 2
        else          -> 0
    }

    private fun toRPN(tokens: List<String>): List<String>? {
        val output = mutableListOf<String>()
        val stack  = ArrayDeque<String>()
        for (token in tokens) {
            val num = token.toDoubleOrNull()
            when {
                num != null -> output.add(token)
                token in listOf("+", "-", "*", "/", "%") -> {
                    while (stack.isNotEmpty() && stack.last() != "(" &&
                           precedence(stack.last()) >= precedence(token)) {
                        output.add(stack.removeLast())
                    }
                    stack.addLast(token)
                }
                token == "(" -> stack.addLast(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.last() != "(") output.add(stack.removeLast())
                    if (stack.isEmpty() || stack.last() != "(") return null
                    stack.removeLast()
                }
            }
        }
        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top == "(" || top == ")") return null
            output.add(top)
        }
        return output
    }

    private fun evalRPN(tokens: List<String>): Double? {
        val stack = ArrayDeque<Double>()
        for (token in tokens) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.addLast(num)
            } else if (token in listOf("+", "-", "*", "/", "%")) {
                if (stack.size < 2) return null
                val b = stack.removeLast()
                val a = stack.removeLast()
                val res = when (token) {
                    "+"  -> a + b
                    "-"  -> a - b
                    "*"  -> a * b
                    "/"  -> if (b == 0.0) return null else a / b
                    "%"  -> a % b
                    else -> 0.0
                }
                stack.addLast(res)
            }
        }
        return if (stack.size == 1) stack.removeLast() else null
    }
}