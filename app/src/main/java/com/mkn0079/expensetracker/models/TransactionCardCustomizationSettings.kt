package com.mkn0079.expensetracker.models

import androidx.compose.runtime.Immutable

@Immutable
data class TransactionCardCustomizationSettings(
    val showIncomeExpenseLabels: Boolean = true,
    val showTransactionDate: Boolean = true,
    val showPaymentMethod: Boolean = true,
    val showTransactionTime: Boolean = true,
    val showCategoryIcon: Boolean = true,
    val showCategoryLabel: Boolean = true,
    val showDateSeparators: Boolean = false
)
