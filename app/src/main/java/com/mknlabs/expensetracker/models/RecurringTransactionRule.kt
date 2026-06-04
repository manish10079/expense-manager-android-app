package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable

enum class RecurringFrequency(val label: String, val periodUnit: String) {
    Daily("Daily", "day"),
    Weekly("Weekly", "week"),
    Monthly("Monthly", "month"),
    Yearly("Yearly", "year")
}

@Immutable
data class RecurringTransactionRule(
    val id: String,
    val transactionId: String,
    val frequency: RecurringFrequency,
    val repeatCount: Int,
    val isEnabled: Boolean,
    val intervalCount: Int = 1,
    val remainingCount: Int? = repeatCount,
    val anchorAt: Long = System.currentTimeMillis(),
    val nextRunAt: Long = anchorAt,
    val lastRunAt: Long? = null,
    val createdAt: Long = anchorAt,
    val updatedAt: Long = createdAt,
    val syncState: SyncState = SyncState.LOCAL_ONLY,
    val isDeleted: Boolean = false
)

@Immutable
data class RecurringTransactionDraft(
    val frequency: RecurringFrequency,
    val repeatCount: Int
)
