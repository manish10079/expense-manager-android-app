package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits

@Immutable
data class Budget(
    val id: String,
    val categoryId: Int,
    val monthStart: Long,
    val limitMinor: Long,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val syncState: SyncState = SyncState.PENDING_UPLOAD,
    val editCount: Int = 0,
    val isDeleted: Boolean = false
) {
    constructor(
        id: String,
        categoryId: Int,
        monthStart: Long,
        limitAmount: Double,
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = createdAt,
        syncState: SyncState = SyncState.PENDING_UPLOAD,
        editCount: Int = 0,
        isDeleted: Boolean = false
    ) : this(
        id = id,
        categoryId = categoryId,
        monthStart = monthStart,
        limitMinor = limitAmount.toMinorUnits(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncState = syncState,
        editCount = editCount,
        isDeleted = isDeleted
    )

    val limitAmount: Double
        get() = limitMinor.toMajorUnits()
}
