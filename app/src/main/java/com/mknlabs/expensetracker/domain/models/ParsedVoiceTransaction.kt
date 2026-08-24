package com.mknlabs.expensetracker.domain.models

/**
 * A financial transaction parsed from voice input.
 *
 * Follows the same convention as [com.mknlabs.expensetracker.sms.ParsedSms]:
 * ephemeral in-memory model passed between the voice parser and the
 * transaction confirmation UI. Persisted only after user confirmation.
 *
 * @see com.mknlabs.expensetracker.domain.repository.VoiceParserRepository
 */
data class ParsedVoiceTransaction(
    val amountMinor: Long,
    val transactionTypeId: Int,  // 1 = Income, 2 = Expense (from transactionTypeMap)
    val categoryId: Int,         // From categoryMap
    val note: String,
    val merchant: String? = null,
    val paymentTypeId: Int? = null, // From paymentTypeMap (null if not detected)
    val createdAt: Long = System.currentTimeMillis(),
    val confidence: VoiceConfidence
)

/**
 * How confident the parser is about the parsed result.
 */
enum class VoiceConfidence {
    /** Amount + category + date all detected with high confidence. */
    HIGH,
    /** Amount detected, category or date inferred with medium confidence. */
    MEDIUM,
    /** Partial parse — user will need to fill in missing fields. */
    LOW
}
