package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mknlabs.expensetracker.utils.ExpenseTrackerIconRegistry

@Immutable
data class PaymentType(
    val id: Int,
    val name: String,
    val iconKey: String,
    /**
     * The user's colour as `#RRGGBB`, or null to derive one from [id]. The same contract as
     * [CategoryType.colorHex].
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
