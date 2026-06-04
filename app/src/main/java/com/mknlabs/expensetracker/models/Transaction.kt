package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mknlabs.expensetracker.utils.getCategoryIcon
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits

@Immutable
data class Transaction(
    val id: String,
    val note: String,
    val createdAt: Long,
    val amountMinor: Long,
    val transactionTypeId: Int,
    val paymentTypeId: Int,
    val categoryId: Int,
    val contentHash: String? = null,
    val syncState: SyncState = SyncState.LOCAL_ONLY,
    val isDeleted: Boolean = false,
    val updatedAt: Long = createdAt,
    val sourceRecurringRuleId: String? = null
) {
    constructor(
        id: Long,
        note: String,
        createdAt: Long,
        amount: Double,
        categoryIcon: ImageVector,
        transactionTypeId: Int,
        paymentTypeId: Int,
        categoryId: Int,
        hash: String = "NA",
        cloudSync: Boolean = false,
        localSync: Boolean = false,
        isDeleted: Boolean = false
    ) : this(
        id = id.toString(),
        note = note,
        createdAt = createdAt,
        amountMinor = amount.toMinorUnits(),
        transactionTypeId = transactionTypeId,
        paymentTypeId = paymentTypeId,
        categoryId = categoryId,
        contentHash = hash.takeUnless { it == "NA" },
        syncState = when {
            cloudSync -> SyncState.SYNCED
            localSync -> SyncState.PENDING_UPLOAD
            else -> SyncState.LOCAL_ONLY
        },
        isDeleted = isDeleted,
        updatedAt = createdAt,
        sourceRecurringRuleId = null
    )

    val amount: Double
        get() = amountMinor.toMajorUnits()

    val categoryIcon: ImageVector
        get() = getCategoryIcon(categoryId)

    val hash: String
        get() = contentHash ?: "NA"

    val cloudSync: Boolean
        get() = syncState == SyncState.SYNCED

    val localSync: Boolean
        get() = syncState == SyncState.LOCAL_ONLY || syncState == SyncState.PENDING_UPLOAD
}
