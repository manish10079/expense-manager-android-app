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
    val icon: ImageVector,
    val isUserCreated: Boolean,
    /**
     * The user's own colour for this row, or `null` for one that draws from the palette.
     *
     * Carried as the stored string rather than a resolved [androidx.compose.ui.graphics.Color]
     * for the same reason [com.mknlabs.expensetracker.core.ui.components.TransactionCard] does:
     * resolution needs the active theme, and a `Color` resolved in the ViewModel would freeze
     * whichever theme happened to be active when the list was built.
     */
    val colorHex: String? = null,
    /**
     * Which palette this row resolves against. Categories and payment methods are numbered
     * from 1 independently — id 1 is Food *and* UPI — so the two cannot share one lookup.
     */
    val isPaymentMethod: Boolean = false
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
