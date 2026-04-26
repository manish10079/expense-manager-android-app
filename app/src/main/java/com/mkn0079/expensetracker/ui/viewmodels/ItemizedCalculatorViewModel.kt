package com.mkn0079.expensetracker.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.domain.usecase.BuildBreakdownNoteUseCase
import com.mkn0079.expensetracker.domain.usecase.ParseBreakdownNoteUseCase
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.CalculatorLineItem
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val normalStoredValue: Double? = null,
    val normalPendingOperator: String? = null,
    val shouldResetNormalDisplay: Boolean = false,
    val totalAmount: Double = 0.0,
    val canAddItem: Boolean = false
)

@HiltViewModel
class ItemizedCalculatorViewModel @Inject constructor(
    private val parseBreakdownNoteUseCase: ParseBreakdownNoteUseCase,
    private val buildBreakdownNoteUseCase: BuildBreakdownNoteUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemizedCalculatorUiState())
    val uiState: StateFlow<ItemizedCalculatorUiState> = _uiState.asStateFlow()

    private val normalCalculatorFormatter = DecimalFormat("#,##0.########")

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
        val result = handleNormalCalculatorAction(
            action = action,
            currentDisplay = state.normalDisplay,
            storedValue = state.normalStoredValue,
            pendingOperator = state.normalPendingOperator,
            shouldResetDisplay = state.shouldResetNormalDisplay
        )
        _uiState.update { 
            it.copy(
                normalDisplay = result.display,
                normalStoredValue = result.storedValue,
                normalPendingOperator = result.pendingOperator,
                shouldResetNormalDisplay = result.shouldResetDisplay
            )
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

    // Normal Calculator Logic moved from Screen
    private fun handleNormalCalculatorAction(
        action: String,
        currentDisplay: String,
        storedValue: Double?,
        pendingOperator: String?,
        shouldResetDisplay: Boolean
    ): NormalCalculatorResult {
        if (currentDisplay == "Error" && action !in setOf("AC", "BACKSPACE")) {
            return handleNormalCalculatorAction(action, "0", null, null, false)
        }

        return when (action) {
            "AC" -> NormalCalculatorResult("0", null, null, false)
            "BACKSPACE" -> {
                val updated = if (shouldResetDisplay || currentDisplay.length <= 1) {
                    "0"
                } else {
                    currentDisplay.dropLast(1)
                }
                NormalCalculatorResult(updated, storedValue, pendingOperator, false)
            }
            "%" -> {
                val currentValue = currentDisplay.toDoubleOrNull() ?: 0.0
                NormalCalculatorResult(
                    display = formatNormalCalculatorValue(currentValue / 100.0),
                    storedValue = storedValue,
                    pendingOperator = pendingOperator,
                    shouldResetDisplay = true
                )
            }
            "+", "-", "*", "/" -> {
                val currentValue = currentDisplay.toDoubleOrNull() ?: 0.0
                val updatedStoredValue = if (storedValue != null && pendingOperator != null && !shouldResetDisplay) {
                    performNormalCalculation(storedValue, currentValue, pendingOperator)
                } else {
                    storedValue ?: currentValue
                }
                NormalCalculatorResult(
                    display = formatNormalCalculatorValue(updatedStoredValue),
                    storedValue = updatedStoredValue,
                    pendingOperator = action,
                    shouldResetDisplay = true
                )
            }
            "=" -> {
                if (storedValue == null || pendingOperator == null) {
                    NormalCalculatorResult(currentDisplay, null, null, true)
                } else {
                    val currentValue = currentDisplay.toDoubleOrNull() ?: 0.0
                    val result = performNormalCalculation(storedValue, currentValue, pendingOperator)
                    NormalCalculatorResult(
                        display = formatNormalCalculatorValue(result),
                        storedValue = null,
                        pendingOperator = null,
                        shouldResetDisplay = true
                    )
                }
            }
            "." -> {
                when {
                    shouldResetDisplay -> NormalCalculatorResult("0.", storedValue, pendingOperator, false)
                    currentDisplay.contains(".") -> NormalCalculatorResult(currentDisplay, storedValue, pendingOperator, false)
                    else -> NormalCalculatorResult("$currentDisplay.", storedValue, pendingOperator, false)
                }
            }
            else -> {
                val updatedDisplay = if (shouldResetDisplay || currentDisplay == "0") {
                    action
                } else {
                    currentDisplay + action
                }
                NormalCalculatorResult(updatedDisplay, storedValue, pendingOperator, false)
            }
        }
    }

    private fun performNormalCalculation(left: Double, right: Double, operator: String): Double {
        return when (operator) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> if (right == 0.0) Double.NaN else left / right
            else -> right
        }
    }

    private fun formatNormalCalculatorValue(value: Double): String {
        return if (value.isFinite()) {
            normalCalculatorFormatter.format(value)
        } else {
            "Error"
        }
    }
    
    fun calculatePreview(): String {
        val state = _uiState.value
        if (state.normalDisplay == "Error") return "Error"
        val currentValue = state.normalDisplay.toDoubleOrNull()
        return when {
            state.normalStoredValue != null && state.normalPendingOperator != null && currentValue != null && !state.shouldResetNormalDisplay ->
                formatNormalCalculatorValue(performNormalCalculation(state.normalStoredValue, currentValue, state.normalPendingOperator))
            currentValue != null -> formatNormalCalculatorValue(currentValue)
            state.normalStoredValue != null -> formatNormalCalculatorValue(state.normalStoredValue)
            else -> "0"
        }
    }
    
    fun buildExpression(): String? {
        val state = _uiState.value
        if (state.normalStoredValue == null || state.normalPendingOperator == null) return null
        val leftValue = formatNormalCalculatorValue(state.normalStoredValue)
        val operatorSymbol = when (state.normalPendingOperator) {
            "*" -> "×"
            "/" -> "÷"
            "-" -> "−"
            else -> state.normalPendingOperator
        }
        val rightValue = if (state.shouldResetNormalDisplay) "" else state.normalDisplay
        return listOf(leftValue, operatorSymbol, rightValue).filter { it.isNotBlank() }.joinToString(" ")
    }
}
