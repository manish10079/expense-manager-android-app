package com.mknlabs.expensetracker.domain.models

import com.google.firebase.Timestamp

data class ProPass(
    val code: String,
    val durationDays: Int,
    val isActive: Boolean,
    val maxUses: Long,
    val currentUses: Long,
    val expiryTimestamp: Timestamp?
)
