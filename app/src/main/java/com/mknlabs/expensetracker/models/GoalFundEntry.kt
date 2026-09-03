package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable

@Immutable
data class GoalFundEntry(
    val id: String,
    val goalId: String,
    val amountMinor: Long,
    val note: String,
    val fundedAt: Long,
    val syncState: SyncState = SyncState.PENDING_UPLOAD
)
