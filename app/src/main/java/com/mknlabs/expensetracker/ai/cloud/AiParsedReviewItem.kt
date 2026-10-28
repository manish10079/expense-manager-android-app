package com.mknlabs.expensetracker.ai.cloud

data class AiParsedReviewItem(
    val id: String,
    val amount: Double,
    val amountMinor: Long,
    val categoryName: String,
    val categoryId: Int,
    val paymentName: String,
    val paymentTypeId: Int,
    val note: String,
    val typeId: Int,
)
