package com.mkn0079.expensetracker.ui.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mkn0079.expensetracker.R

enum class CategoryManagementTab(@StringRes val titleRes: Int) {
    Income(R.string.title_income),
    Expense(R.string.title_expense),
    Payment(R.string.title_payment);

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
    val subtitle: String? = null,
    @StringRes val subtitleRes: Int? = null,
    val icon: ImageVector,
    val isUserCreated: Boolean
)

@Immutable
data class CategoryIconOption(
    val id: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)
