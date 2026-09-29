package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mknlabs.expensetracker.utils.ExpenseTrackerIconRegistry

@Immutable
data class CategoryType(
    val id: Int,
    val name: String,
    val iconKey: String,
    val transactionTypeId: Int,
    /**
     * The user's colour as `#RRGGBB`, or null to derive one from [id].
     *
     * Placed here rather than beside [iconKey] so the four positional arguments every call
     * site in `categoryTypeData.kt` already passes keep meaning what they meant.
     *
     * Only ever set for a category the user created and coloured. A seeded category is null
     * and resolves from the palette, which is what stops the per-launch reseed of those rows
     * from overwriting a choice the user made.
     */
    val colorHex: String? = null,
    val isSystem: Boolean = true,
    val sortOrder: Int = id,
    val isDeleted: Boolean = false,
    val syncState: SyncState = SyncState.PENDING_UPLOAD,
    val createdAt: Long = 0L,
    val updatedAt: Long = createdAt
) {
    val icon: ImageVector
        get() = ExpenseTrackerIconRegistry.iconForKey(iconKey)
}
