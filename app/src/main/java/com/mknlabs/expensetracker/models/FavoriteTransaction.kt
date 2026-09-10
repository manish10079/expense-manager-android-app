package com.mknlabs.expensetracker.models

import java.util.UUID

data class FavoriteTransaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amountMinor: Long,
    val transactionTypeId: Int = 2,
    val categoryId: Int = 0,
    val paymentTypeId: Int = 0,
    val note: String = "",
    val isPinned: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
