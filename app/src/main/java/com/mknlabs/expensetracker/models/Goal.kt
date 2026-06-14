package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mknlabs.expensetracker.utils.ExpenseTrackerIconRegistry

@Immutable
data class Goal(
    val id: String,
    val name: String,
    val targetAmountMinor: Long,
    val currentAmountMinor: Long,
    val deadlineAt: Long?,
    val iconKey: String,
    val colorHex: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState = SyncState.LOCAL_ONLY
) {
    val icon: ImageVector
        get() = ExpenseTrackerIconRegistry.iconForKey(iconKey)
        
    val progress: Float
        get() = if (targetAmountMinor > 0) {
            (currentAmountMinor.toFloat() / targetAmountMinor.toFloat()).coerceIn(0f, 1f)
        } else 0f
}
