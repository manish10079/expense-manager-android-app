package com.mkn0079.expensetracker.ui.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

enum class CategoryManagementTab(val title: String) {
    Income("Income"),
    Expense("Expense"),
    Payment("Payment");

    companion object {
        fun fromName(name: String): CategoryManagementTab {
            return entries.firstOrNull { it.name == name } ?: Expense
        }
    }
}

@Immutable
data class CategoryManagementItemUi(
    val id: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isUserCreated: Boolean
)

@Immutable
data class CategoryIconOption(
    val id: String,
    val label: String,
    val icon: ImageVector
)
