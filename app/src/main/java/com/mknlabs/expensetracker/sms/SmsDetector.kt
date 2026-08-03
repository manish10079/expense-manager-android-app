package com.mknlabs.expensetracker.sms

/**
 * Decides whether an SMS is a genuine financial transaction message worth
 * parsing. Rejects OTP / verification codes / login alerts, promotional and
 * marketing content, spam, recharge offers, and messages without a currency
 * amount or a transaction verb.
 */
object SmsDetector {

    /**
     * Returns `true` when the [body] looks like a real financial transaction SMS.
     */
    fun isFinancialTransaction(body: String): Boolean {
        if (body.isBlank()) return false

        // Defense-in-depth: rejection patterns win even if an amount + verb appear.
        if (SmsRegex.REJECTION_PATTERNS.any { it.containsMatchIn(body) }) return false

        // A genuine transaction message must state a currency amount...
        if (!SmsRegex.AMOUNT.containsMatchIn(body)) return false

        // ...and must contain an expense or income verb.
        return SmsRegex.EXPENSE_VERBS.containsMatchIn(body) ||
            SmsRegex.INCOME_VERBS.containsMatchIn(body)
    }
}
