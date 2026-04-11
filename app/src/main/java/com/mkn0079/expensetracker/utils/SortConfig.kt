package com.mkn0079.expensetracker.utils

import com.mkn0079.expensetracker.models.OrderOptionItem
import com.mkn0079.expensetracker.models.SortType

fun getOrderOptions(sort: String): List<OrderOptionItem> {
    return when (sort) {

        "Date" -> listOf(
            OrderOptionItem("Newest First", SortType.NEWEST),
            OrderOptionItem("Oldest First", SortType.OLDEST)
        )

        "Amount" -> listOf(
            OrderOptionItem("Highest First", SortType.HIGHEST),
            OrderOptionItem("Lowest First", SortType.LOWEST)
        )

        "Category" -> listOf(
            OrderOptionItem("Income First", SortType.INCOME_FIRST),
            OrderOptionItem("Expense First", SortType.EXPENSE_FIRST)
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
