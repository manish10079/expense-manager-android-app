package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction

fun getRankedCategories(
    categories: Collection<CategoryType>,
    transactions: List<Transaction>,
    transactionTypeId: Int
): List<CategoryType> {
    val usageCountByCategoryId = transactions
        .asSequence()
        .filter { it.transactionTypeId == transactionTypeId }
        .groupingBy { it.categoryId }
        .eachCount()

    return categories
        .asSequence()
        .filter { it.transactionTypeId == transactionTypeId }
        .sortedWith(
            compareByDescending<CategoryType> { usageCountByCategoryId[it.id] ?: 0 }
                .thenBy { it.id }
        )
        .toList()
}
