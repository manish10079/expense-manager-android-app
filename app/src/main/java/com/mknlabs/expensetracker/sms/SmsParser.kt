package com.mknlabs.expensetracker.sms

import com.mknlabs.expensetracker.utils.toMinorUnits
import java.util.Locale

/**
 * Extracts a [ParsedSms] from a financial transaction SMS.
 *
 * Pure Kotlin and side-effect free so it is unit-testable on the JVM.
 * The BroadcastReceiver delegates here — never parses inline (plan §12).
 */
object SmsParser {

    private const val INCOME_TRANSACTION_TYPE_ID = 1
    private const val EXPENSE_TRANSACTION_TYPE_ID = 2

    /**
     * Parses [body] into a [ParsedSms], or returns `null` when the message
     * is not a genuine financial transaction (OTP, promo, no amount, etc.).
     *
     * [smsTimestamp] is the SMS receipt timestamp in epoch millis and becomes
     * the transaction's `createdAt` (used later for duplicate detection, plan D7).
     *
     * [userOverrides] feeds the learning system (plan §10): merchant → category
     * mappings recorded from past user corrections, consulted before static rules.
     */
    fun parse(
        body: String,
        sender: String,
        smsTimestamp: Long,
        userOverrides: Map<String, Int> = emptyMap()
    ): ParsedSms? {
        if (!SmsDetector.isFinancialTransaction(body, sender)) return null

        val amountMinor = extractAmountMinor(body) ?: return null

        val transactionTypeId = detectTransactionType(body)
        val detection = SmsCategoryDetector.detect(body, transactionTypeId, userOverrides)

        return ParsedSms(
            amountMinor = amountMinor,
            sender = sender,
            body = body,
            smsTimestamp = smsTimestamp,
            transactionTypeId = transactionTypeId,
            categoryId = detection.categoryId,
            merchant = detection.merchant,
            confidence = detection.confidence
        )
    }

    // First match wins: Indian bank SMS list the transaction amount before the
    // "Avl Bal" trailing balance, so AMOUNT.find() returns the transaction amount.
    private fun extractAmountMinor(body: String): Long? {
        val match = SmsRegex.AMOUNT.find(body) ?: return null
        val digits = match.groupValues.getOrNull(1)?.replace(",", "") ?: return null
        return digits.toDoubleOrNull()?.toMinorUnits()
    }

    private fun detectTransactionType(body: String): Int {
        val text = body.lowercase(Locale.ROOT)
        return when {
            // Income wins on mixed messages (e.g. "refund credited"); income is the
            // safer default since refunds/cashbacks almost always say "credited".
            SmsRegex.INCOME_VERBS.containsMatchIn(text) -> INCOME_TRANSACTION_TYPE_ID
            SmsRegex.EXPENSE_VERBS.containsMatchIn(text) -> EXPENSE_TRANSACTION_TYPE_ID
            else -> EXPENSE_TRANSACTION_TYPE_ID
        }
    }
}
