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
     * Matches a currency amount with Indian digit grouping, e.g.
     * `Rs 520`, `₹1,234.50`, `Rs. 15,000`, `INR 1,23,456.50`.
     * Group 1 holds the raw digits (commas included).
     *
     * Note: `\b` cannot precede `₹` (a non-word char), so the word boundary is
     * only applied to the letter-based currency markers.
     */
    val AMOUNT: Regex = Regex(
        pattern = """(?:\bRs\.?|\bINR\.?|₹)\s*(\d[\d,]*(?:\.\d{1,2})?)""",
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
     * Patterns that mark a message as NOT a financial transaction:
     * OTP / verification / login alerts, promotional & marketing content,
     * spam, and recharge offers. Any match rejects the message.
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
        Regex("""top[- ]?up offer""", RegexOption.IGNORE_CASE)
    )
}
