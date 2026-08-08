package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.Transaction

/**
 * Shared usage-ranking window (the last 60 days) used by the Add Transaction
 * pickers and the Smart SMS Import quick-action categories, so "most used"
 * reflects recent behaviour everywhere consistently.
 */
const val USAGE_RANKING_WINDOW_MS = 60L * 24L * 60L * 60L * 1000L

/**
 * Ranks categories of [transactionTypeId] by how often each was used in
 * [transactions] (most-used first), falling back to a stable [it.id] order for
 * ties. When [sinceMillis] is provided, only transactions on or after that
 * timestamp count towards the usage — e.g. the last 60 days.
 */
fun getRankedCategories(
    categories: Collection<CategoryType>,
    transactions: List<Transaction>,
    transactionTypeId: Int,
    sinceMillis: Long? = null
): List<CategoryType> {
    val usageCountByCategoryId = transactions
        .asSequence()
        .filter { it.transactionTypeId == transactionTypeId }
        .filter { sinceMillis == null || it.createdAt >= sinceMillis }
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

/**
 * Ranks payment methods by how often each was used in [transactions]
 * (most-used first), falling back to a stable [it.id] order for ties. When
 * [sinceMillis] is provided, only transactions on or after that timestamp count
 * towards the usage — e.g. the last 60 days.
 */
fun getRankedPaymentMethods(
    paymentMethods: Collection<PaymentType>,
    transactions: List<Transaction>,
    sinceMillis: Long? = null
): List<PaymentType> {
    val usageCountByPaymentId = transactions
        .asSequence()
        .filter { sinceMillis == null || it.createdAt >= sinceMillis }
        .groupingBy { it.paymentTypeId }
        .eachCount()

    return paymentMethods
        .asSequence()
        .sortedWith(
            compareByDescending<PaymentType> { usageCountByPaymentId[it.id] ?: 0 }
                .thenBy { it.id }
        )
        .toList()
}
