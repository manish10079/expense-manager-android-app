package com.mkn0079.expensetracker.models

import androidx.compose.runtime.Immutable
import com.mkn0079.expensetracker.utils.toMajorUnits
import com.mkn0079.expensetracker.utils.toMinorUnits

@Immutable
data class Budget(
    val id: String,
    val categoryId: Int,
    val monthStart: Long,
    val limitMinor: Long,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val syncState: SyncState = SyncState.LOCAL_ONLY,
    val isDeleted: Boolean = false
) {
    constructor(
        id: String,
        categoryId: Int,
        monthStart: Long,
        limitAmount: Double,
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = createdAt,
        syncState: SyncState = SyncState.LOCAL_ONLY,
        isDeleted: Boolean = false
    ) : this(
        id = id,
        categoryId = categoryId,
        monthStart = monthStart,
        limitMinor = limitAmount.toMinorUnits(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncState = syncState,
        isDeleted = isDeleted
    )

    val limitAmount: Double
        get() = limitMinor.toMajorUnits()
}
