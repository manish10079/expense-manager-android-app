package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable

@Immutable
data class TransactionCardCustomizationSettings(
    val showIncomeExpenseLabels: Boolean = false,
    // Date on, clock off: the card leads with the day the money moved and keeps the
    // second row quiet. Mirrors DEFAULT_TRANSACTION_CARD_SHOW_* in DefaultSettings.
    val showTransactionDate: Boolean = true,
    val showPaymentMethod: Boolean = true,
    val showTransactionTime: Boolean = false,
    val showCategoryIcon: Boolean = true,
    val showCategoryLabel: Boolean = true,
    val showDateSeparators: Boolean = false,
    val showTransactionListSummaries: Boolean = true
)
