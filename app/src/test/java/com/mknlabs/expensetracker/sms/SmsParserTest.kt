package com.mknlabs.expensetracker.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmsParserTest {

    @Test
    fun parses_upi_expense_amount_and_type() {
        val body = "Rs 520 debited from A/c XX1234 via UPI. UPI Ref 412345678901. Avl Bal: Rs 4,780.00 - HDFC Bank"
        val parsed = SmsParser.parse(body, sender = "HDFCBK", smsTimestamp = 1_720_000_000_000L)

        assertNotNull(parsed)
        assertEquals(52_000L, parsed!!.amountMinor)
        assertEquals(2, parsed.transactionTypeId)
        assertEquals(1_720_000_000_000L, parsed.smsTimestamp)
        assertEquals("HDFCBK", parsed.sender)
        assertEquals(body, parsed.body)
    }

    @Test
    fun parses_income_amount_with_indian_digit_grouping() {
        val body = "Rs 1,23,456.50 credited to A/c XX5678 by NEFT - SBI"
        val parsed = SmsParser.parse(body, sender = "SBIINB", smsTimestamp = 0L)

        assertNotNull(parsed)
        assertEquals(1_23_456_50L, parsed!!.amountMinor)
        assertEquals(1, parsed.transactionTypeId)
    }

    @Test
    fun parses_inr_income_message() {
        val body = "INR 15,000 credited to A/c XX5678 - SBI"
        val parsed = SmsParser.parse(body, sender = "SBIINB", smsTimestamp = 0L)

        assertNotNull(parsed)
        assertEquals(1_500_000L, parsed!!.amountMinor)
        assertEquals(1, parsed.transactionTypeId)
    }

    @Test
    fun parses_rupee_symbol_expense_message_with_merchant() {
        val body = "₹1,299 debited at Amazon.in - ICICI Bank"
        val parsed = SmsParser.parse(body, sender = "ICICIB", smsTimestamp = 0L)

        assertNotNull(parsed)
        assertEquals(129_900L, parsed!!.amountMinor)
        assertEquals(2, parsed.transactionTypeId)
        assertEquals(3, parsed.categoryId) // Shopping
        assertEquals("Amazon", parsed.merchant)
        assertEquals(SmsConfidence.MEDIUM, parsed.confidence)
    }

    @Test
    fun detects_salary_as_high_confidence_income() {
        val body = "Salary for July has been credited to your account. Rs 75,000 credited - HDFC Bank"
        val parsed = SmsParser.parse(body, sender = "HDFCBK", smsTimestamp = 0L)

        assertNotNull(parsed)
        assertEquals(1, parsed!!.transactionTypeId)
        assertEquals(101, parsed.categoryId) // Salary
        assertEquals(SmsConfidence.HIGH, parsed.confidence)
    }

    @Test
    fun detects_food_merchant() {
        val body = "Rs 349.50 debited at Swiggy on card XX2345 - HDFC Bank"
        val parsed = SmsParser.parse(body, sender = "HDFCBK", smsTimestamp = 0L)

        assertNotNull(parsed)
        assertEquals(34_950L, parsed!!.amountMinor)
        assertEquals(1, parsed.categoryId) // Food
        assertEquals("Swiggy", parsed.merchant)
        assertEquals(SmsConfidence.HIGH, parsed.confidence)
    }

    @Test
    fun detects_cashback_as_income_other() {
        val body = "Cashback of Rs 50 credited to your card XX2345 - HDFC Bank"
        val parsed = SmsParser.parse(body, sender = "HDFCBK", smsTimestamp = 0L)

        assertNotNull(parsed)
        assertEquals(1, parsed!!.transactionTypeId)
        assertEquals(105, parsed.categoryId) // Income Other
    }

    @Test
    fun falls_back_to_other_category_for_unknown_expense() {
        val body = "Rs 250 debited for miscellaneous purchase - HDFC Bank"
        val parsed = SmsParser.parse(body, sender = "HDFCBK", smsTimestamp = 0L)

        assertNotNull(parsed)
        assertEquals(2, parsed!!.transactionTypeId)
        assertEquals(23, parsed.categoryId) // Expense Other
        assertEquals(SmsConfidence.LOW, parsed.confidence)
    }

    @Test
    fun returns_null_for_otp_message() {
        val body = "Your OTP for HDFC Bank transaction is 456789. Do not share with anyone."
        assertNull(SmsParser.parse(body, sender = "HDFCBK", smsTimestamp = 0L))
    }

    @Test
    fun returns_null_for_promotional_message() {
        val body = "Get flat 50% off on your next order. Download the app now!"
        assertNull(SmsParser.parse(body, sender = "ADVERT", smsTimestamp = 0L))
    }

    @Test
    fun returns_null_when_amount_is_missing() {
        val body = "Your debit card transaction was successful."
        assertNull(SmsParser.parse(body, sender = "HDFCBK", smsTimestamp = 0L))
    }

    @Test
    fun returns_null_for_trai_promotional_header_suffix() {
        val body = "Dear Customer, pre-approved personal loan of Rs 5,00,000 is ready for instant disbursal. Apply now!"
        assertNull(SmsParser.parse(body, sender = "AD-DMIFNC-P", smsTimestamp = 0L))
    }

    @Test
    fun returns_null_for_loan_marketing_keywords() {
        val body = "Rs 2,50,000 personal loan approved. Click here to avail loan now."
        assertNull(SmsParser.parse(body, sender = "AD-BAJAJ", smsTimestamp = 0L))
    }
}
