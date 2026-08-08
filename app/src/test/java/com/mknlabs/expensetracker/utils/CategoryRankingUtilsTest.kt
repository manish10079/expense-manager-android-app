package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.Transaction
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryRankingUtilsTest {

    @Test
    fun ranksExpenseCategoriesByUsageAndPlacesUnusedLast() {
        val categories = listOf(
            testCategory(id = 1, transactionTypeId = 2),
            testCategory(id = 2, transactionTypeId = 2),
            testCategory(id = 3, transactionTypeId = 2),
            testCategory(id = 101, transactionTypeId = 1)
        )
        val transactions = listOf(
            testTransaction(categoryId = 2, transactionTypeId = 2),
            testTransaction(categoryId = 2, transactionTypeId = 2),
            testTransaction(categoryId = 1, transactionTypeId = 2),
            testTransaction(categoryId = 101, transactionTypeId = 1)
        )

        val rankedCategories = getRankedCategories(
            categories = categories,
            transactions = transactions,
            transactionTypeId = 2
        )

        assertEquals(listOf(2, 1, 3), rankedCategories.map { it.id })
    }

    @Test
    fun ignoresOtherTransactionTypesWhenRanking() {
        val categories = listOf(
            testCategory(id = 101, transactionTypeId = 1),
            testCategory(id = 102, transactionTypeId = 1),
            testCategory(id = 1, transactionTypeId = 2)
        )
        val transactions = listOf(
            testTransaction(categoryId = 1, transactionTypeId = 2),
            testTransaction(categoryId = 1, transactionTypeId = 2),
            testTransaction(categoryId = 102, transactionTypeId = 1)
        )

        val rankedCategories = getRankedCategories(
            categories = categories,
            transactions = transactions,
            transactionTypeId = 1
        )

        assertEquals(listOf(102, 101), rankedCategories.map { it.id })
    }

    @Test
    fun keepsStableIdOrderingWhenUsageCountsMatch() {
        val categories = listOf(
            testCategory(id = 4, transactionTypeId = 2),
            testCategory(id = 2, transactionTypeId = 2),
            testCategory(id = 3, transactionTypeId = 2)
        )

        val rankedCategories = getRankedCategories(
            categories = categories,
            transactions = emptyList(),
            transactionTypeId = 2
        )

        assertEquals(listOf(2, 3, 4), rankedCategories.map { it.id })
    }

    @Test
    fun categoryRankingIgnoresTransactionsOlderThanWindow() {
        val now = System.currentTimeMillis()
        val old = now - 61L * 24L * 60L * 60L * 1000L // 61 days ago
        val categories = listOf(
            testCategory(id = 1, transactionTypeId = 2),
            testCategory(id = 2, transactionTypeId = 2)
        )
        val transactions = listOf(
            testTransaction(categoryId = 1, transactionTypeId = 2, createdAt = now),
            testTransaction(categoryId = 2, transactionTypeId = 2, createdAt = now),
            testTransaction(categoryId = 2, transactionTypeId = 2, createdAt = old)
        )

        val rankedCategories = getRankedCategories(
            categories = categories,
            transactions = transactions,
            transactionTypeId = 2,
            sinceMillis = now - 60L * 24L * 60L * 60L * 1000L
        )

        // The 61-day-old transaction for category 2 is outside the window, so
        // categories 1 and 2 both have 1 in-window use → stable ID order.
        assertEquals(listOf(1, 2), rankedCategories.map { it.id })
    }

    @Test
    fun ranksPaymentMethodsByUsage() {
        val paymentMethods = listOf(
            testPaymentMethod(id = 1),
            testPaymentMethod(id = 2),
            testPaymentMethod(id = 3)
        )
        val transactions = listOf(
            testTransaction(paymentTypeId = 2),
            testTransaction(paymentTypeId = 2),
            testTransaction(paymentTypeId = 1)
        )

        val rankedPaymentMethods = getRankedPaymentMethods(
            paymentMethods = paymentMethods,
            transactions = transactions
        )

        assertEquals(listOf(2, 1, 3), rankedPaymentMethods.map { it.id })
    }

    @Test
    fun paymentMethodRankingIgnoresTransactionsOlderThanWindow() {
        val now = System.currentTimeMillis()
        val old = now - 61L * 24L * 60L * 60L * 1000L // 61 days ago
        val paymentMethods = listOf(
            testPaymentMethod(id = 1),
            testPaymentMethod(id = 2)
        )
        val transactions = listOf(
            testTransaction(paymentTypeId = 1, createdAt = now),
            testTransaction(paymentTypeId = 2, createdAt = now),
            testTransaction(paymentTypeId = 2, createdAt = old)
        )

        val rankedPaymentMethods = getRankedPaymentMethods(
            paymentMethods = paymentMethods,
            transactions = transactions,
            sinceMillis = now - 60L * 24L * 60L * 60L * 1000L
        )

        // The 61-day-old transaction for payment 2 is outside the window, so
        // both have 1 in-window use → stable ID order.
        assertEquals(listOf(1, 2), rankedPaymentMethods.map { it.id })
    }

    @Test
    fun paymentMethodRankingKeepsStableIdOrderingForTies() {
        val paymentMethods = listOf(
            testPaymentMethod(id = 3),
            testPaymentMethod(id = 1),
            testPaymentMethod(id = 2)
        )

        val rankedPaymentMethods = getRankedPaymentMethods(
            paymentMethods = paymentMethods,
            transactions = emptyList()
        )

        assertEquals(listOf(1, 2, 3), rankedPaymentMethods.map { it.id })
    }

    private fun testCategory(
        id: Int,
        transactionTypeId: Int
    ): CategoryType {
        return CategoryType(
            id = id,
            name = "Category $id",
            iconKey = "category",
            transactionTypeId = transactionTypeId
        )
    }

    private fun testPaymentMethod(id: Int): PaymentType {
        return PaymentType(
            id = id,
            name = "Payment $id",
            iconKey = "payments"
        )
    }

    private fun testTransaction(
        categoryId: Int = 0,
        paymentTypeId: Int = 0,
        transactionTypeId: Int = 2,
        createdAt: Long = 0L
    ): Transaction {
        return Transaction(
            id = "$categoryId-$paymentTypeId-$createdAt",
            note = "",
            createdAt = createdAt,
            amountMinor = 0L,
            transactionTypeId = transactionTypeId,
            paymentTypeId = paymentTypeId,
            categoryId = categoryId
        )
    }
}
