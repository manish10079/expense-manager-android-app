package com.mknlabs.expensetracker.widget.model

/**
 * Immutable model for a parsed transaction displayed in the widget preview.
 *
 * Converted from [ParsedVoiceTransaction] by [WidgetVoiceProcessor].
 * Contains pre-formatted display strings so the widget UI has zero logic.
 *
 * @see com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
 */
internal data class WidgetParsedTransaction(
    /** Formatted amount string (e.g., "₹250"). */
    val amountText: String,
    /** Raw amount in minor units for saving. */
    val amountMinor: Long,
    /** Category name (e.g., "Food"). */
    val categoryName: String,
    /** Category ID for saving to Room. */
    val categoryId: Int,
    /** Merchant name if detected (e.g., "Swiggy"). */
    val merchant: String?,
    /** Note/description text. */
    val note: String,
    /** Transaction type: 1 = Income, 2 = Expense. */
    val transactionTypeId: Int,
    /** Payment type ID if detected. */
    val paymentTypeId: Int?,
    /** Confidence level for display purposes. */
    val confidenceText: String?,
    /** Timestamp when this transaction occurred. */
    val createdAt: Long
)
