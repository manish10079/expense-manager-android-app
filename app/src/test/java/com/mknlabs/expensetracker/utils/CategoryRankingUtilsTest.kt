package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.models.CategoryType
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

    private fun testTransaction(
        categoryId: Int,
        transactionTypeId: Int
    ): Transaction {
        return Transaction(
            id = categoryId.toString(),
            note = "",
            createdAt = 0L,
            amountMinor = 0L,
            transactionTypeId = transactionTypeId,
            paymentTypeId = 0,
            categoryId = categoryId
        )
    }
}
