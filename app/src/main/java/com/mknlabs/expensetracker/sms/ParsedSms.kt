package com.mknlabs.expensetracker.sms

/**
 * A financial transaction parsed from an incoming SMS.
 *
 * This is an ephemeral, in-memory model — the payload is passed around via
 * notification PendingIntent extras and bottom-sheet state, and is NEVER
 * persisted to the database (see SMART_SMS_IMPORT_IMPLEMENTATION_PLAN.md D2).
 */
data class ParsedSms(
    val amountMinor: Long,
    val sender: String,
    val body: String,
    val smsTimestamp: Long,
    val transactionTypeId: Int,
    val categoryId: Int,
    val merchant: String? = null,
    val confidence: SmsConfidence
)

/**
 * How confident the parser is about the detected category.
 */
enum class SmsConfidence {
    HIGH,
    MEDIUM,
    LOW
}
