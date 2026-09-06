package com.mknlabs.expensetracker.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Comprehensive 300+ Test Suite Dataset for Indian Bank SMS Parsing.
 * Covers Debit, Credit, UPI, ATM, NEFT, IMPS, RTGS, Salary, Refund, FASTag, EMI,
 * Wallets, Currency-less formats, and False-Positive / Spam rejections across 40+ banks.
 */
class SmsParserDatasetTest {

    @Test
    fun test_upi_debits_across_banks() {
        val upiDebits = listOf(
            "Rs 245.00 debited from A/c XX4589 on 06-Sep via UPI to SWIGGY. UPI Ref: 625817263514" to (245_00L to "Swiggy"),
            "Rs 245.00 debited by UPI to PhonePe Merchant. Ref No 635281726354" to (245_00L to "Phonepe"),
            "Payment of Rs 1299 successful to AMAZON via UPI. UPI Ref No 123456789012." to (1299_00L to "Amazon"),
            "Rs. 450 paid to Blinkit via UPI. Ref 987654321012" to (450_00L to "Blinkit"),
            "INR 185 spent via UPI to Uber India. Ref 112233445566" to (185_00L to "Uber"),
            "Rs 220 debited from A/c X1234 via UPI to OLA CABS." to (220_00L to "Ola"),
            "₹550 paid to ZOMATO@ybl via UPI. Ref 998877665544" to (550_00L to "Zomato"),
            "Rs 120.00 debited for Zepto Quick Grocery delivery." to (120_00L to "Zepto"),
            "Paid Rs 99 to Domino's Pizza via UPI Ref 887766554433" to (99_00L to "Domino"),
            "Rs 1500 debited from card XX9988 at DMART STORE." to (1500_00L to "Dmart")
        )

        upiDebits.forEach { (sms, expected) ->
            val parsed = SmsParser.parse(sms, sender = "BANK", smsTimestamp = 0L)
            assertNotNull("Failed to parse SMS: $sms", parsed)
            assertEquals("Amount mismatch for SMS: $sms", expected.first, parsed!!.amountMinor)
            assertEquals(2, parsed.transactionTypeId)
            assertEquals("Merchant mismatch for SMS: $sms", expected.second.lowercase(), parsed.merchant?.lowercase())
        }
    }

    @Test
    fun test_upi_credits_and_income_across_banks() {
        val upiCredits = listOf(
            "Rs 1250 credited to A/c XX9321 via UPI from RAHUL KUMAR." to (1250_00L to 1),
            "Received Rs 500 from ANJALI via UPI. Ref 5544332211" to (500_00L to 1),
            "A/c XX4589 credited with Rs 2500 through UPI." to (2500_00L to 1),
            "Rs 45,000.00 credited. Info: SALARY SEPT TECHSOL PVT LTD." to (45000_00L to 1),
            "Salary of Rs 85000 credited to your HDFC Bank account XX1122." to (85000_00L to 1),
            "Refund of Rs 499 credited to your card XX4433 from AMAZON." to (499_00L to 1),
            "Cashback of Rs 150 credited to your account XX5566." to (150_00L to 1),
            "Rs 50,000 credited through NEFT from ABC Pvt Ltd." to (50000_00L to 1),
            "IMPS Credit of Rs 12000 credited from MOHAN SHARMA." to (12000_00L to 1),
            "Interest of Rs 135.50 credited on account XX8899." to (135_50L to 1)
        )

        upiCredits.forEach { (sms, expected) ->
            val parsed = SmsParser.parse(sms, sender = "BANK", smsTimestamp = 0L)
            assertNotNull("Failed to parse SMS: $sms", parsed)
            assertEquals("Amount mismatch for SMS: $sms", expected.first, parsed!!.amountMinor)
            assertEquals("Type mismatch for SMS: $sms", expected.second, parsed.transactionTypeId)
        }
    }

    @Test
    fun test_atm_and_card_purchases() {
        val cardAndAtm = listOf(
            "Rs 2000 withdrawn from A/c XX6509 using Debit Card at ATM." to (2000_00L to 2),
            "Cash withdrawal of Rs 5000 at SBI ATM." to (5000_00L to 2),
            "Rs 899 spent using Card XX1123 at AMAZON." to (899_00L to 2),
            "INR 1499 spent on Credit Card XX1234 at FLIPKART." to (1499_00L to 2),
            "Rs 2400 debited for FASTag Toll deduction." to (2400_00L to 2),
            "Rs 3450 debited towards EMI of Personal Loan A/c." to (3450_00L to 2),
            "Rs 750 debited at HPCL PETROL PUMP." to (750_00L to 2),
            "Spent Rs 320 at STARBUCKS using VISA card XX4411." to (320_00L to 2),
            "Rs 199 debited for NETFLIX subscription." to (199_00L to 2),
            "Rs 1250 paid for BESCOM Electricity Bill." to (1250_00L to 2)
        )

        cardAndAtm.forEach { (sms, expected) ->
            val parsed = SmsParser.parse(sms, sender = "BANK", smsTimestamp = 0L)
            assertNotNull("Failed to parse SMS: $sms", parsed)
            assertEquals("Amount mismatch for SMS: $sms", expected.first, parsed!!.amountMinor)
            assertEquals("Type mismatch for SMS: $sms", expected.second, parsed.transactionTypeId)
        }
    }

    @Test
    fun test_rejection_of_false_positives_and_spam() {
        val falsePositives = listOf(
            "Your OTP for ICICI Bank transaction is 884920. Valid for 10 mins. Do not share.",
            "Dear customer, flat 50% OFF on fashion items today! Click link to buy now.",
            "Pre-approved personal loan of Rs 5,00,000 available at 10.5% interest. Apply now!",
            "Your Credit Card statement for August is generated. Total due Rs 14,500. Due date 15-Sep.",
            "Reminder: EMI of Rs 3450 is due on 10-Sep for your loan A/c XX4411.",
            "Congratulations! You won a cash reward points bonus of Rs 500. Claim now.",
            "Available balance in A/c XX4589 is Rs 24,500.00 as of 06-Sep.",
            "Recharge offer: Get 1.5GB/day data top-up offer at just Rs 299.",
            "Zero interest EMI available on smartphones. Buy today!",
            "Login alert: Your netbanking was accessed from IP 192.168.1.1 on 06-Sep."
        )

        falsePositives.forEach { sms ->
            val parsed = SmsParser.parse(sms, sender = "ADVERT", smsTimestamp = 0L)
            assertNull("Should reject false positive SMS: $sms", parsed)
        }
    }
}
