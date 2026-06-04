package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.models.SortType
import com.mknlabs.expensetracker.models.Transaction


fun sortTransactions(
    list: List<Transaction>,
    sortType: SortType
): List<Transaction> {

    return when (sortType) {

        SortType.HIGHEST -> {
            list.sortedByDescending { it.amount }
        }

        SortType.LOWEST -> {
            list.sortedBy { it.amount }
        }

        SortType.NEWEST -> {
            list.sortedByDescending { it.createdAt }
        }

        SortType.OLDEST -> {
            list.sortedBy { it.createdAt }
        }
        SortType.INCOME_FIRST -> {
            list.sortedBy { it.transactionTypeId }
            // 1 (Income) comes before 2 (Expense)
        }

        SortType.EXPENSE_FIRST -> {
            list.sortedByDescending { it.transactionTypeId }
            // 2 (Expense) comes before 1 (Income)
        }
    }
}