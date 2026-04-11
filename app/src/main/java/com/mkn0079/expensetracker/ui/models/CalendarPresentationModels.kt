package com.mkn0079.expensetracker.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class CalendarDayUi(
    val timestamp: Long,
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val hasIncome: Boolean,
    val hasExpense: Boolean
)

@Immutable
data class CalendarMonthFinancialSummaryUi(
    val monthIndex: Int,
    val label: String,
    val income: Double,
    val expense: Double,
    val net: Double,
    val isProjection: Boolean,
    val incomeLabel: String,
    val expenseLabel: String,
    val netLabel: String
)
