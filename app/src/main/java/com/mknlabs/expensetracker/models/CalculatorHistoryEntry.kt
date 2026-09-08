package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable

/**
 * A single completed Normal-mode calculator calculation.
 *
 * [expression] uses the display symbols used across the calculator UI
 * (e.g. "12 + 5", "9 × 3", "20 − 4", "12 ÷ 4"), and [result] is the formatted
 * result the display showed when the user pressed "=" (e.g. "17").
 */
@Immutable
data class CalculatorHistoryEntry(
    val expression: String,
    val result: String,
    val timestampMillis: Long
)
