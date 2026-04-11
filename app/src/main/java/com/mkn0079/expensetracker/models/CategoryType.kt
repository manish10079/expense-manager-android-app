package com.mkn0079.expensetracker.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mkn0079.expensetracker.utils.ExpenseTrackerIconRegistry

@Immutable
data class CategoryType(
    val id: Int,
    val name: String,
    val iconKey: String,
    val transactionTypeId: Int,
    val isSystem: Boolean = true,
    val sortOrder: Int = id,
    val isDeleted: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = createdAt
) {
    val icon: ImageVector
        get() = ExpenseTrackerIconRegistry.iconForKey(iconKey)
}
