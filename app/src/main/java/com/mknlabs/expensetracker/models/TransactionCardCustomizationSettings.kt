package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable

@Immutable
data class TransactionCardCustomizationSettings(
    val showIncomeExpenseLabels: Boolean = false,
    val showTransactionDate: Boolean = true,
    val showPaymentMethod: Boolean = true,
    val showTransactionTime: Boolean = true,
    val showCategoryIcon: Boolean = true,
    val showCategoryLabel: Boolean = true,
    val showDateSeparators: Boolean = false,
    val showTransactionListSummaries: Boolean = true
)
