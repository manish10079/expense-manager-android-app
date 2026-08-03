package com.mknlabs.expensetracker.sms

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsDetectorTest {

    @Test
    fun accepts_upi_expense_message() {
        val body = "Rs 520 debited from A/c XX1234 via UPI. UPI Ref 412345678901. Avl Bal: Rs 4,780.00 - HDFC Bank"
        assertTrue(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun accepts_neft_income_message() {
        val body = "Rs 15,000.00 credited to A/c XX5678 on 02-08-26 by NEFT. Ref No 123456789012 - SBI"
        assertTrue(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun accepts_card_purchase_message() {
        val body = "Rs 349.50 debited at Swiggy on card XX2345 - HDFC Bank"
        assertTrue(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun accepts_salary_credited_message() {
        val body = "Salary for July has been credited to your account. Rs 75,000 credited - HDFC Bank"
        assertTrue(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun accepts_atm_withdrawal_message() {
        val body = "Rs 2,000 withdrawn from ATM at MG Road. Avl Bal: Rs 12,340.00 - HDFC Bank"
        assertTrue(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun rejects_otp_message() {
        val body = "Your OTP for HDFC Bank transaction is 456789. Do not share with anyone."
        assertFalse(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun rejects_verification_code_message() {
        val body = "Your verification code is 1234. Never share it with anyone."
        assertFalse(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun rejects_login_alert_message() {
        val body = "Login alert: Your account was accessed from a new device. Verify now at our site."
        assertFalse(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun rejects_promotional_message() {
        val body = "Get flat 50% off on your next order. Download the app now!"
        assertFalse(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun rejects_recharge_offer_message() {
        val body = "Special recharge offer! Get 20% cashback offer on recharge above Rs 199."
        assertFalse(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun rejects_link_only_message() {
        val body = "Your order has been shipped. Track here: https://example.com/track/12345"
        assertFalse(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun rejects_blank_message() {
        assertFalse(SmsDetector.isFinancialTransaction(""))
        assertFalse(SmsDetector.isFinancialTransaction("   "))
    }

    @Test
    fun rejects_amount_without_transaction_verb() {
        val body = "Your available balance is Rs 4,780.00. - HDFC Bank"
        assertFalse(SmsDetector.isFinancialTransaction(body))
    }

    @Test
    fun rejects_verb_without_amount() {
        val body = "Your debit card transaction was successful."
        assertFalse(SmsDetector.isFinancialTransaction(body))
    }
}
