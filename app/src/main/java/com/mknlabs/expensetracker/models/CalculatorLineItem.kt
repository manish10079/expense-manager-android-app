package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable

@Immutable
data class CalculatorLineItem(
    val id: Int,
    val description: String,
    val amount: Double,
    val highlighted: Boolean = false
)
