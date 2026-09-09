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

enum class CalculatorMode(val title: String) {
    ITEMIZED("Itemized"),
    NORMAL("Normal")
}

data class NormalCalculatorResult(
    val display: String,
    val storedValue: Double?,
    val pendingOperator: String?,
    val shouldResetDisplay: Boolean
)

data class ItemizedCalculatorUiState(
    val selectedMode: CalculatorMode = CalculatorMode.ITEMIZED,
    val items: List<CalculatorLineItem> = emptyList(),
    val isAddingItem: Boolean = false,
    val descriptionInput: String = "",
    val amountInput: String = "",
    val normalDisplay: String = "0",
    val normalRawExpression: String = "",
    val normalStoredValue: Double? = null,
    val normalPendingOperator: String? = null,
    val shouldResetNormalDisplay: Boolean = false,
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

    init {
        viewModelScope.launch {
            calculatorHistoryRepository.observeHistory().collect { entries ->
                _uiState.update { it.copy(historyEntries = entries) }
            }
        }
    }

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

    fun handleNormalAction(action: String) {
        val state = _uiState.value
        var rawExpr = state.normalRawExpression
        var display = state.normalDisplay
        var shouldReset = state.shouldResetNormalDisplay

        when (action) {
            "AC" -> {
                rawExpr = ""
                display = "0"
                shouldReset = false
            }
            "BACKSPACE" -> {
                if (shouldReset) {
                    // After "=" the expression stays editable: backspace removes
                    // the last character of the completed expression (entering
                    // edit mode) instead of wiping the whole calculation.
                    shouldReset = false
                }
                if (rawExpr.isNotEmpty()) {
                    val trimmed = rawExpr.trimEnd()
                    rawExpr = if (trimmed.length > 1) trimmed.dropLast(1).trimEnd() else ""
                    val eval = previewExpressionValue(rawExpr)
                    display = if (rawExpr.isEmpty()) "0" else formatNormalCalculatorValue(eval ?: 0.0)
                } else if (display.length > 1) {
                    display = display.dropLast(1)
                } else {
                    display = "0"
                }
            }
            "=" -> {
                val exprToEval = if (rawExpr.isNotBlank()) rawExpr else display
                if (exprToEval.isNotBlank()) {
                    val eval = CalculatorExpressionEvaluator.evaluate(exprToEval)
                    if (eval != null && eval.isFinite()) {
                        val resultStr = formatNormalCalculatorValue(eval)
                        val historyExpr = formatDisplayExpression(if (rawExpr.isNotBlank()) rawExpr else display)
                        val hasOperator = rawExpr.any { it in "×÷−+-*/%" }
                        if (resultStr != "Error" && historyExpr.isNotBlank() && hasOperator) {
                            viewModelScope.launch {
                                calculatorHistoryRepository.addEntry(expression = historyExpr, result = resultStr)
                            }
                        }
                        display = resultStr
                        shouldReset = true
                    } else {
                        display = "Error"
                        shouldReset = true
                    }
                }
            }
            "%" -> {
                val eval = CalculatorExpressionEvaluator.evaluate(if (rawExpr.isNotBlank()) rawExpr else display)
                if (eval != null && eval.isFinite()) {
                    val pct = eval / 100.0
                    display = formatNormalCalculatorValue(pct)
                    rawExpr = display
                    shouldReset = true
                }
            }
            "+", "-", "*", "/" -> {
                val symbol = when (action) {
                    "*" -> "×"
                    "/" -> "÷"
                    "-" -> "−"
                    else -> action
                }
                if (shouldReset) {
                    rawExpr = "$display $symbol "
                    shouldReset = false
                } else if (rawExpr.isBlank()) {
                    rawExpr = "$display $symbol "
                } else {
                    val trimmed = rawExpr.trimEnd()
                    val lastChar = trimmed.lastOrNull()
                    if (lastChar != null && lastChar in "×÷−+-") {
                        val base = trimmed.dropLast(1).trimEnd()
                        rawExpr = "$base $symbol "
                    } else {
                        rawExpr = "$rawExpr $symbol "
                    }
                }
                val eval = CalculatorExpressionEvaluator.evaluate(rawExpr)
                if (eval != null && eval.isFinite()) {
                    display = formatNormalCalculatorValue(eval)
                }
            }
            "(", ")" -> {
                if (shouldReset) {
                    rawExpr = if (action == "(") "(" else ""
                    shouldReset = false
                } else {
                    rawExpr = if (rawExpr.isEmpty()) action else "$rawExpr $action"
                }
                val eval = CalculatorExpressionEvaluator.evaluate(rawExpr)
                if (eval != null && eval.isFinite()) {
                    display = formatNormalCalculatorValue(eval)
                }
            }
            "." -> {
                if (shouldReset) {
                    rawExpr = "0."
                    display = "0."
                    shouldReset = false
                } else {
                    rawExpr = if (rawExpr.isEmpty()) "0." else "$rawExpr."
                }
            }
            else -> { // Digits 0-9
                if (shouldReset) {
                    rawExpr = action
                    display = action
                    shouldReset = false
                } else {
                    rawExpr = if (rawExpr == "0") action else rawExpr + action
                    val eval = CalculatorExpressionEvaluator.evaluate(rawExpr)
                    display = if (eval != null && eval.isFinite()) formatNormalCalculatorValue(eval) else rawExpr
                }
            }
        }

        _uiState.update {
            it.copy(
                normalDisplay = display,
                normalRawExpression = rawExpr,
                shouldResetNormalDisplay = shouldReset
            )
        }
    }

    fun selectHistoryResult(result: String) {
        val cleanResult = result.replace(",", "")
        _uiState.update {
            it.copy(
                normalDisplay = cleanResult,
                normalRawExpression = "",
                shouldResetNormalDisplay = true
            )
        }
    }

    /**
     * Restores a full calculation expression into the active normal calculator state.
     */
    fun restoreHistoryExpression(expression: String) {
        val clean = expression.trim()
        val eval = CalculatorExpressionEvaluator.evaluate(clean)
        val displayVal = if (eval != null && eval.isFinite()) formatNormalCalculatorValue(eval) else "0"
        _uiState.update {
            it.copy(
                normalRawExpression = clean,
                normalDisplay = displayVal,
                shouldResetNormalDisplay = false
            )
        }
    }

    fun deleteHistoryEntry(timestampMillis: Long) {
        viewModelScope.launch {
            calculatorHistoryRepository.deleteEntry(timestampMillis)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            calculatorHistoryRepository.clearHistory()
        }
    }

    fun getFinalResult(
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences
    ): Pair<String, String> {
        val state = _uiState.value
        val finalAmount = formatEditableTotal(state.totalAmount)
        val finalNote = buildBreakdownNoteUseCase(state.items, currencyId, amountFormatPreferences)
        return Pair(finalAmount, finalNote)
    }

    private fun sanitizeAmountInput(input: String): String {
        val filtered = input.filterIndexed { index, char ->
            char.isDigit() || (char == '.' && index == input.indexOf('.'))
        }
        val decimalIndex = filtered.indexOf('.')

        return if (decimalIndex >= 0) {
            val whole = filtered.substring(0, decimalIndex + 1)
            val decimals = filtered.substring(decimalIndex + 1).take(2)
            whole + decimals
        } else {
            filtered
        }
    }

    private fun formatEditableTotal(amount: Double): String {
        return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString()
    }

    private fun formatNormalCalculatorValue(value: Double): String {
        return if (value.isFinite()) {
            normalCalculatorFormatter.format(value)
        } else {
            "Error"
        }
    }

    /**
     * Best-effort live preview for a possibly-incomplete expression (e.g. one
     * that ends with an operator): evaluates the full expression first, and if
     * that fails, keeps dropping trailing characters until a value is found.
     * Keeps the running total visible while the user edits the expression.
     */
    private fun previewExpressionValue(expression: String): Double? {
        var candidate = expression.trimEnd()
        while (candidate.isNotEmpty()) {
            val eval = CalculatorExpressionEvaluator.evaluate(candidate)
            if (eval != null && eval.isFinite()) return eval
            candidate = candidate.dropLast(1).trimEnd()
        }
        return null
    }
    
    fun calculatePreview(): String {
        val state = _uiState.value
        if (state.normalDisplay == "Error") return "Error"
        return state.normalDisplay
    }
    
    fun buildExpression(): String? {
        val state = _uiState.value
        if (state.normalRawExpression.isBlank()) return null
        return formatDisplayExpression(state.normalRawExpression)
    }

    private fun formatDisplayExpression(raw: String): String {
        return raw.replace("*", "×").replace("/", "÷").replace("-", "−")
    }
}

private object CalculatorExpressionEvaluator {
    fun evaluate(expression: String): Double? {
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) return null
        val rpn = toRPN(tokens) ?: return null
        return evalRPN(rpn)
    }

    private fun tokenize(expr: String): List<String> {
        val sanitized = expr.replace("×", "*").replace("÷", "/").replace("−", "-").replace(",", "")
        val result = mutableListOf<String>()
        var i = 0
        while (i < sanitized.length) {
            val c = sanitized[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < sanitized.length && (sanitized[i].isDigit() || sanitized[i] == '.')) {
                        sb.append(sanitized[i])
                        i++
                    }
                    result.add(sb.toString())
                }
                c in "+-*/()" -> {
                    if (c == '-' && (result.isEmpty() || result.last() in "+-*/(")) {
                        val sb = StringBuilder("-")
                        i++
                        while (i < sanitized.length && (sanitized[i].isDigit() || sanitized[i] == '.')) {
                            sb.append(sanitized[i])
                            i++
                        }
                        if (sb.length > 1) {
                            result.add(sb.toString())
                        } else {
                            result.add("-")
                        }
                    } else {
                        result.add(c.toString())
                        i++
                    }
                }
                else -> i++
            }
        }
        return result
    }

    private fun precedence(op: String?): Int = when (op) {
        "+", "-" -> 1
        "*", "/" -> 2
        else -> 0
    }

    private fun toRPN(tokens: List<String>): List<String>? {
        val output = mutableListOf<String>()
        val stack = java.util.ArrayDeque<String>()

        for (token in tokens) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                output.add(token)
            } else if (token in listOf("+", "-", "*", "/")) {
                while (!stack.isEmpty() && stack.peek() != "(" && precedence(stack.peek()) >= precedence(token)) {
                    output.add(stack.pop())
                }
                stack.push(token)
            } else if (token == "(") {
                stack.push(token)
            } else if (token == ")") {
                while (!stack.isEmpty() && stack.peek() != "(") {
                    output.add(stack.pop())
                }
                if (stack.isEmpty() || stack.peek() != "(") return null
                stack.pop()
            }
        }
        while (!stack.isEmpty()) {
            val top = stack.pop()
            if (top == "(" || top == ")") return null
            output.add(top)
        }
        return output
    }

    private fun evalRPN(tokens: List<String>): Double? {
        val stack = java.util.ArrayDeque<Double>()
        for (token in tokens) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.push(num)
            } else if (token in listOf("+", "-", "*", "/")) {
                if (stack.size < 2) return null
                val b = stack.pop()
                val a = stack.pop()
                val res = when (token) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> if (b == 0.0) Double.NaN else a / b
                    else -> 0.0
                }
                stack.push(res)
            }
        }
        return if (stack.size == 1) stack.pop() else null
    }
}

