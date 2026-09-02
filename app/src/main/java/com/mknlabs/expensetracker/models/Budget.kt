package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits

@Immutable
data class Budget(
    val id: String,
    val categoryId: Int = 0,
    val categoryIds: List<Int> = if (categoryId != 0) listOf(categoryId) else emptyList(),
    val name: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
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
        isDeleted: Boolean = false,
        categoryIds: List<Int> = if (categoryId != 0) listOf(categoryId) else emptyList(),
        name: String = "",
        period: BudgetPeriod = BudgetPeriod.MONTHLY
    ) : this(
        id = id,
        categoryId = categoryId,
        categoryIds = categoryIds,
        name = name,
        period = period,
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

    val effectiveCategoryIds: List<Int>
        get() = categoryIds.ifEmpty { if (categoryId != 0) listOf(categoryId) else emptyList() }
}
