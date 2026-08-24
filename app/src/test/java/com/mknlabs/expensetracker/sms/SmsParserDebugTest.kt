package com.mknlabs.expensetracker.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Debug tests for the two messages reported by the user.
 */
class SmsParserDebugTest {

    @Test
    fun debug_message1_should_detect() {
        val body = "Received Rs.797.00 in your Kotak Bank AC 3773 from Manish Kumar Nayak on 24-08-26.UPI Ref:000101953226"
        val sender = "KOTAK"

        println("=== Message 1 Debug ===")
        println("AMOUNT regex find: ${SmsRegex.AMOUNT.find(body)}")
        println("INCOME_VERBS containsMatchIn: ${SmsRegex.INCOME_VERBS.containsMatchIn(body)}")
        println("EXPENSE_VERBS containsMatchIn: ${SmsRegex.EXPENSE_VERBS.containsMatchIn(body)}")
        println("isFinancialTransaction: ${SmsDetector.isFinancialTransaction(body, sender)}")

        val parsed = SmsParser.parse(body, sender, System.currentTimeMillis())
        println("Parsed result: $parsed")

        assertNotNull("Message 1 should be detected", parsed)
        assertEquals(797_00L, parsed!!.amountMinor)
        assertEquals(1, parsed.transactionTypeId) // Income
    }

    @Test
    fun debug_message2_should_detect() {
        val body = "Dear UPI user A/C X5289 debited by 1984.00 on date 24Aug26 trf to SNAPMINT FINANCI Refno 300272338278 If not u? call-1800111109 for other services-18001234-SBI"
        val sender = "SBI"

        println("=== Message 2 Debug ===")
        println("AMOUNT regex find: ${SmsRegex.AMOUNT.find(body)}")
        println("BARE_AMOUNT regex find: ${SmsRegex.BARE_AMOUNT.find(body)}")
        println("INCOME_VERBS containsMatchIn: ${SmsRegex.INCOME_VERBS.containsMatchIn(body)}")
        println("EXPENSE_VERBS containsMatchIn: ${SmsRegex.EXPENSE_VERBS.containsMatchIn(body)}")
        println("isFinancialTransaction: ${SmsDetector.isFinancialTransaction(body, sender)}")

        // Check rejection patterns
        for ((i, pattern) in SmsRegex.REJECTION_PATTERNS.withIndex()) {
            if (pattern.containsMatchIn(body)) {
                println("REJECTION PATTERN $i matched: ${pattern.pattern}")
            }
        }

        val parsed = SmsParser.parse(body, sender, System.currentTimeMillis())
        println("Parsed result: $parsed")

        // After fix: BARE_AMOUNT fallback should now detect this message
        assertNotNull("Message 2 should be detected as a financial transaction", parsed)
        assertEquals(1984_00L, parsed!!.amountMinor)
        assertEquals(2, parsed.transactionTypeId) // Expense
    }

    @Test
    fun analyze_message2_amount_matching() {
        val body = "Dear UPI user A/C X5289 debited by 1984.00 on date 24Aug26 trf to SNAPMINT FINANCI Refno 300272338278 If not u? call-1800111109 for other services-18001234-SBI"

        // The AMOUNT regex requires Rs./INR/₹ prefix
        // Message 2 has "debited by 1984.00" without any currency prefix
        val amountMatch = SmsRegex.AMOUNT.find(body)
        println("Amount match result: $amountMatch")
        println("Amount match is null: ${amountMatch == null}")

        // This is the root cause - no currency prefix in message 2
        assertTrue("AMOUNT regex should fail without currency prefix", amountMatch == null)
    }
}
