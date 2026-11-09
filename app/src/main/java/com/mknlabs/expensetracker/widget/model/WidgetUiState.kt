package com.mknlabs.expensetracker.widget.model

internal data class WidgetUiState(
    val todaySpendingMinor: Long = 0L,
    val monthLabel: String = "",
    val monthIncomeMinor: Long = 0L,
    val monthExpenseMinor: Long = 0L,
    val monthBudgetCapMinor: Long = 0L,
    val recentItems: List<WidgetRecentItem> = emptyList(),
    val currencySymbol: String = "₹"
)

internal data class WidgetRecentItem(
    val title: String,
    val subtitle: String,
    val amountText: String,
    val isIncome: Boolean
)
