package com.mkn0079.expensetracker.utils

import com.mkn0079.expensetracker.models.OrderOptionItem
import com.mkn0079.expensetracker.models.SortType
import com.mkn0079.expensetracker.R

fun getOrderOptions(sort: String): List<OrderOptionItem> {
    return when (sort) {

        "Date" -> listOf(
            OrderOptionItem(R.string.label_newest_first, SortType.NEWEST),
            OrderOptionItem(R.string.label_oldest_first, SortType.OLDEST)
        )

        "Amount" -> listOf(
            OrderOptionItem(R.string.label_highest_first, SortType.HIGHEST),
            OrderOptionItem(R.string.label_lowest_first, SortType.LOWEST)
        )

        "Category" -> listOf(
            OrderOptionItem(R.string.label_income_first, SortType.INCOME_FIRST),
            OrderOptionItem(R.string.label_expense_first, SortType.EXPENSE_FIRST)
        )

        else -> emptyList()
    }
}

fun getDefaultOrder(sort: String): SortType {
    return when (sort) {
        "Amount" -> SortType.HIGHEST
        "Category" -> SortType.INCOME_FIRST
        else -> SortType.NEWEST
    }
}
