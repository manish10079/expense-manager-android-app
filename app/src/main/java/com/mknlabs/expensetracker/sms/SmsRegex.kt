package com.mknlabs.expensetracker.sms

/**
 * Centralized regex management for SMS transaction parsing (GEMINI.md:
 * no regex scattered across call sites).
 *
 * All patterns are intentionally IGNORE_CASE so callers can match against
 * the raw body without pre-lowercasing.
 */
object SmsRegex {

    /**
     * Default AMOUNT regex for Indian Rupees (backward compatibility).
     * Matches: `Rs 520`, `₹1,234.50`, `Rs. 15,000`, `INR 1,23,456.50`.
     * Group 1 holds the raw digits (commas included).
     */
    val AMOUNT: Regex = getAmountRegex("₹")

    /**
     * Generates an AMOUNT regex that matches the user's selected currency
     * symbol/code plus the standard Indian formats (Rs., INR, ₹).
     *
     * @param currencySymbol The user's currency symbol (e.g. "$", "€", "£", "RM").
     *                       If null or blank, falls back to ₹-only matching.
     *
     * Examples:
     * - `$` → matches `$45`, `$1,234.50`, `USD 100`
     * - `€` → matches `€50`, `EUR 200`
     * - `£` → matches `£25`, `GBP 150`
     * - `RM` → matches `RM 100`, `RM50`
     */
    fun getAmountRegex(currencySymbol: String?): Regex {
        // Build the currency prefix pattern dynamically
        val currencyPrefix = buildString {
            // Always include standard Indian formats
            append("(?:\\bRs\\.?|\\bINR\\.?|\\u20B9)")
            // Add user's currency if provided
            if (!currencySymbol.isNullOrBlank()) {
                val escaped = Regex.escape(currencySymbol)
                val isLetterBased = currencySymbol.all { it.isLetter() }
                append("|")
                if (isLetterBased) {
                    append("\\b")
                }
                append(escaped)
                if (isLetterBased) {
                    append("\\.?")
                }
                append("\\s*")
            }
        }
        val amountPattern = "(?:$currencyPrefix)(\\d[\\d,]*(?:\\.\\d{1,2})?)"
        return Regex(
            pattern = amountPattern,
            option = RegexOption.IGNORE_CASE
        )
    }

    /**
     * Fallback: matches bare amounts after transaction keywords like
     * `debited by 1984.00`, `paid 500`, `sent Rs.1000`. Used when the
     * primary [AMOUNT] regex (which requires a currency prefix) doesn't match.
     * Some banks (e.g. SBI) omit the Rs./INR prefix in their SMS format.
     * Group 1 holds the raw digits.
     */
    val BARE_AMOUNT: Regex = Regex(
        pattern = """\b(?:debited|credited|paid|sent|received|by|for|of|amount)\s+(?:Rs\.?\s*)?(?:INR\s*)?(?:₹\s*)?(\d[\d,]*(?:\.\d{1,2})?)""",
        option = RegexOption.IGNORE_CASE
    )

    /**
     * Expense verbs → transactionTypeId 2 (expense).
     */
    val EXPENSE_VERBS: Regex = Regex(
        pattern = """\b(?:debited|purchased|purchase|spent|withdrawn|withdrawal|paid|sent|atm|upi payment|upi transfer)\b""",
        option = RegexOption.IGNORE_CASE
    )

    /**
     * Income verbs → transactionTypeId 1 (income).
     */
    val INCOME_VERBS: Regex = Regex(
        pattern = """\b(?:credited|received|refund(?:ed)?|cashback|salary|deposit(?:ed)?)\b""",
        option = RegexOption.IGNORE_CASE
    )

    /**
     * TRAI (Telecom Regulatory Authority of India) commercial sender header suffix check:
     * Headers end with a 1-letter suffix after a hyphen denoting message category:
     * - P = Promotional (marketing, sales, loan offers, discounts)
     * - T = Transactional (OTPs, bank debit/credit alerts)
     * - S = Service (account updates, delivery notices)
     * - G = Government
     *
     * Example: `AD-DMIFNC-P` -> ends with `-P`, meaning promotional.
     */
    val PROMOTIONAL_SENDER_HEADER: Regex = Regex(
        pattern = """^[A-Z]{2}-[A-Z0-9]+-P$""",
        option = RegexOption.IGNORE_CASE
    )

    /**
     * Patterns that mark a message as NOT a financial transaction:
     * OTP / verification / login alerts, promotional & marketing content,
     * loan/credit card offers, spam, and recharge offers. Any match rejects the message.
     */
    val REJECTION_PATTERNS: List<Regex> = listOf(
        Regex("""\botp\b""", RegexOption.IGNORE_CASE),
        Regex("""one[- ]?time password""", RegexOption.IGNORE_CASE),
        Regex("""verification code""", RegexOption.IGNORE_CASE),
        Regex("""\bverify(?:ing)?\b""", RegexOption.IGNORE_CASE),
        Regex("""login alert""", RegexOption.IGNORE_CASE),
        Regex("""authentication""", RegexOption.IGNORE_CASE),
        Regex("""do not share""", RegexOption.IGNORE_CASE),
        Regex("""\boffer\b""", RegexOption.IGNORE_CASE),
        Regex("""\bpromo(?:tional)?\b""", RegexOption.IGNORE_CASE),
        Regex("""\bdiscount\b""", RegexOption.IGNORE_CASE),
        Regex("""\bwin(?:ner)?\b""", RegexOption.IGNORE_CASE),
        Regex("""\bprize\b""", RegexOption.IGNORE_CASE),
        Regex("""\blottery\b""", RegexOption.IGNORE_CASE),
        Regex("""\bunsubscribe\b""", RegexOption.IGNORE_CASE),
        Regex("""recharge offer""", RegexOption.IGNORE_CASE),
        Regex("""cashback offer""", RegexOption.IGNORE_CASE),
        Regex("""top[- ]?up offer""", RegexOption.IGNORE_CASE),
        Regex("""\bloan(?:s)?\b""", RegexOption.IGNORE_CASE),
        Regex("""pre[- ]?approved""", RegexOption.IGNORE_CASE),
        Regex("""avail\s+(?:a\s+)?loan""", RegexOption.IGNORE_CASE),
        Regex("""apply\s+(?:now|today|online)""", RegexOption.IGNORE_CASE),
        Regex("""loan\s+(?:approved|disbursed|offer|limit|amount|sanctioned)""", RegexOption.IGNORE_CASE),
        Regex("""instant\s+(?:loan|credit|cash)""", RegexOption.IGNORE_CASE),
        Regex("""personal\s+loan""", RegexOption.IGNORE_CASE),
        Regex("""home\s+loan""", RegexOption.IGNORE_CASE),
        Regex("""car\s+loan""", RegexOption.IGNORE_CASE),
        Regex("""gold\s+loan""", RegexOption.IGNORE_CASE),
        Regex("""business\s+loan""", RegexOption.IGNORE_CASE),
        Regex("""credit\s+limit\s+increase""", RegexOption.IGNORE_CASE),
        Regex("""zero\s+interest""", RegexOption.IGNORE_CASE),
        Regex("""low\s+interest\s+rate""", RegexOption.IGNORE_CASE),
        Regex("""easy\s+emi""", RegexOption.IGNORE_CASE),
        Regex("""no\s+cost\s+emi""", RegexOption.IGNORE_CASE)
    )
}
