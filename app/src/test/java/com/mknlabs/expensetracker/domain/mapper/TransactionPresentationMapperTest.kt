package com.mknlabs.expensetracker.domain.mapper

import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material.icons.filled.QuestionMark
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionPresentationMapperTest {

    @Test
    fun `toTransactionCardItemUi resolves custom category icon correctly`() {
        // Arrange
        val customCategoryId = 999
        val customIconKey = "restaurant" // Should map to Flatware in registry
        val categories = listOf(
            CategoryType(
                id = customCategoryId,
                name = "Custom Food",
                iconKey = customIconKey,
                transactionTypeId = 2,
                isSystem = false
            )
        )
        
        val transaction = Transaction(
            id = "t1",
            note = "Custom category test",
            createdAt = System.currentTimeMillis(),
            amountMinor = 1000L,
            transactionTypeId = 2,
            paymentTypeId = 1,
            categoryId = customCategoryId,
            syncState = SyncState.LOCAL_ONLY
        )

        // Act
        val result = transaction.toTransactionCardItemUi(
            currencyId = DEFAULT_CURRENCY_ID,
            dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
            timeFormat = DEFAULT_TIME_FORMAT,
            paymentTypeName = "Cash",
            categories = categories
        )

        // Assert
        // "restaurant" is mapped to Icons.Filled.Flatware in ExpenseTrackerIconRegistry
        assertEquals(Icons.Filled.Flatware, result.icon)
    }

    @Test
    fun `toTransactionCardItemUi falls back to default icon if category not found`() {
        // Arrange
        val unknownCategoryId = 888
        val categories = emptyList<CategoryType>()
        
        val transaction = Transaction(
            id = "t2",
            note = "Unknown category test",
            createdAt = System.currentTimeMillis(),
            amountMinor = 1000L,
            transactionTypeId = 2,
            paymentTypeId = 1,
            categoryId = unknownCategoryId,
            syncState = SyncState.LOCAL_ONLY
        )

        // Act
        val result = transaction.toTransactionCardItemUi(
            currencyId = DEFAULT_CURRENCY_ID,
            dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
            timeFormat = DEFAULT_TIME_FORMAT,
            paymentTypeName = "Cash",
            categories = categories
        )

        // Assert
        assertEquals(Icons.Filled.QuestionMark, result.icon)
    }

    @Test
    fun `toTransactionCardItemUi uppercases payment type and category label`() {
        // Arrange
        val customCategoryId = 999
        val categories = listOf(
            CategoryType(
                id = customCategoryId,
                name = "Custom Food",
                iconKey = "restaurant",
                transactionTypeId = 2,
                isSystem = false
            )
        )
        
        val transaction = Transaction(
            id = "t3",
            note = "Uppercase test",
            createdAt = System.currentTimeMillis(),
            amountMinor = 1000L,
            transactionTypeId = 2,
            paymentTypeId = 1,
            categoryId = customCategoryId,
            syncState = SyncState.LOCAL_ONLY
        )

        // Act
        val result = transaction.toTransactionCardItemUi(
            currencyId = DEFAULT_CURRENCY_ID,
            dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
            timeFormat = DEFAULT_TIME_FORMAT,
            paymentTypeName = "Cash",
            categories = categories
        )

        // Assert
        assertEquals("CASH", result.paymentType)
        assertEquals("CUSTOM FOOD", result.categoryLabel)
    }
}
