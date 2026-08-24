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
     *
     * [currencySymbol] is the user's selected currency symbol (e.g. "$", "€", "£").
     * When provided, the parser also matches amounts with that currency.
     * Defaults to "₹" for backward compatibility.
     */
    fun parse(
        body: String,
        sender: String,
        smsTimestamp: Long,
        userOverrides: Map<String, Int> = emptyMap(),
        currencySymbol: String? = "₹"
    ): ParsedSms? {
        if (!SmsDetector.isFinancialTransaction(body, sender, currencySymbol)) return null

        val amountMinor = extractAmountMinor(body, currencySymbol) ?: return null

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

    // First match wins: bank SMS list the transaction amount before the
    // "Avl Bal" trailing balance, so AMOUNT.find() returns the transaction amount.
    // Uses user's currency symbol for international support.
    // Falls back to BARE_AMOUNT for banks that omit the currency prefix (e.g. SBI).
    private fun extractAmountMinor(body: String, currencySymbol: String? = "₹"): Long {
        val amountRegex = SmsRegex.getAmountRegex(currencySymbol)
        val match = amountRegex.find(body) ?: SmsRegex.BARE_AMOUNT.find(body) ?: return 0L
        val digits = match.groupValues.getOrNull(1)?.replace(",", "") ?: return 0L
        return digits.toDoubleOrNull()?.toMinorUnits() ?: 0L
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
