package com.mknlabs.expensetracker.core.ui.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.utils.ExpenseTrackerIconRegistry

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
    @StringRes val labelRes: Int
) {
    /**
     * Resolved through [ExpenseTrackerIconRegistry] instead of being carried on the option.
     *
     * The catalog used to hold a vector table of its own beside the registry's, and nothing
     * kept the two in step: 49 options were pickable while the registry had never heard of
     * them, so choosing one stored a key that drew as a question mark in every list row.
     * Deriving the vector here makes that drift impossible to express.
     */
    val icon: ImageVector
        get() = ExpenseTrackerIconRegistry.iconForKey(id)
}
