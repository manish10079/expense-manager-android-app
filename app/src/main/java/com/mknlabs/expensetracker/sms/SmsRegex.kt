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

    fun getAmountRegex(currencySymbol: String?): Regex {
        val currencyPrefix = buildString {
            append("(?:\\bRs\\.?\\s*|\\bINR\\.?\\s*|\\u20B9\\s*)")
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

    val BARE_AMOUNT: Regex = Regex(
        pattern = """(?:\b(?:debited|credited|paid|spent|sent|received|withdrawn|added|transferred|deducted|credit|debit|by|for|of|amount|sum of)\s+(?:Rs\.?\s*)?(?:INR\s*)?(?:₹\s*)?(\d[\d,]*(?:\.\d{1,2})?))|(?:\b(\d[\d,]*(?:\.\d{1,2})?)\s+(?:debited|credited|paid|spent|sent|received|withdrawn|added|transferred|deducted|cr|dr))""",
        option = RegexOption.IGNORE_CASE
    )

    val EXPENSE_VERBS: Regex = Regex(
        pattern = """\b(?:debited|purchased|purchase|spent|withdrawn|withdrawal|paid|payment|sent|atm|upi payment|upi transfer|deducted|dr|autopay|emi|ecs|nach|successful)\b""",
        option = RegexOption.IGNORE_CASE
    )

    val INCOME_VERBS: Regex = Regex(
        pattern = """\b(?:credited|received|refund(?:ed)?|cashback|salary|deposit(?:ed)?|cr|reversal|added)\b""",
        option = RegexOption.IGNORE_CASE
    )

    val BANK_SENDER_SUFFIX: Regex = Regex(
        pattern = """^[A-Z]{2}-([A-Z0-9]{5,10})(?:-[TSG])?$""",
        option = RegexOption.IGNORE_CASE
    )

    val PROMOTIONAL_SENDER_HEADER: Regex = Regex(
        pattern = """^[A-Z]{2}-[A-Z0-9]+-P$""",
        option = RegexOption.IGNORE_CASE
    )

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
        Regex("""avail\s+(?:a\s+)?loan""", RegexOption.IGNORE_CASE),
        Regex("""apply\s+(?:now|today|online)""", RegexOption.IGNORE_CASE),
        Regex("""loan\s+(?:approved|offer|sanctioned)""", RegexOption.IGNORE_CASE),
        Regex("""instant\s+(?:loan|credit|cash)""", RegexOption.IGNORE_CASE),
        Regex("""home\s+loan""", RegexOption.IGNORE_CASE),
        Regex("""car\s+loan""", RegexOption.IGNORE_CASE),
        Regex("""gold\s+loan""", RegexOption.IGNORE_CASE),
        Regex("""business\s+loan""", RegexOption.IGNORE_CASE),
        Regex("""credit\s+limit\s+increase""", RegexOption.IGNORE_CASE),
        Regex("""zero\s+interest""", RegexOption.IGNORE_CASE),
        Regex("""low\s+interest\s+rate""", RegexOption.IGNORE_CASE),
        Regex("""easy\s+emi""", RegexOption.IGNORE_CASE),
        Regex("""no\s+cost\s+emi""", RegexOption.IGNORE_CASE),
        // Additional spam, reminders & limit alerts
        Regex("""due\s+date""", RegexOption.IGNORE_CASE),
        Regex("""reminder""", RegexOption.IGNORE_CASE),
        Regex("""is\s+due""", RegexOption.IGNORE_CASE),
        Regex("""available\s+limit""", RegexOption.IGNORE_CASE),
        Regex("""credit\s+limit""", RegexOption.IGNORE_CASE),
        Regex("""statement\s+for""", RegexOption.IGNORE_CASE),
        Regex("""claim\s+now""", RegexOption.IGNORE_CASE),
        Regex("""reward\s+points""", RegexOption.IGNORE_CASE),
        Regex("""congratulations""", RegexOption.IGNORE_CASE),
        Regex("""flat\s+\d+%\s+off""", RegexOption.IGNORE_CASE)
    )
}
