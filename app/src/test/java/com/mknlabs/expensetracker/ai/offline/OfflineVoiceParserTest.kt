package com.mknlabs.expensetracker.ai.offline

import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.models.VoiceConfidence
import com.mknlabs.expensetracker.domain.repository.VoiceParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OfflineVoiceParserTest {

    private lateinit var parser: OfflineVoiceParser

    @Before
    fun setUp() {
        parser = OfflineVoiceParser()
    }

    // ──────────────────────────────────────────────────────────────────────
    // Amount extraction
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun parses_dollar_amount_with_symbol() {
        val result = parser.parse("Add $45 for groceries")
        assertSuccess(result)
        assertEquals(4_500L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun parses_dollar_amount_with_decimal() {
        val result = parser.parse("Spent $12.50 on coffee")
        assertSuccess(result)
        assertEquals(1_250L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun parses_dollar_amount_with_commas() {
        val result = parser.parse("Paid $1,234.56 for electronics")
        assertSuccess(result)
        assertEquals(123_456L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun parses_dollars_word_format() {
        val result = parser.parse("Spent 45 dollars on food")
        assertSuccess(result)
        assertEquals(4_500L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun parses_rupee_symbol() {
        val result = parser.parse("₹500 for groceries")
        assertSuccess(result)
        assertEquals(50_000L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun parses_rs_prefix() {
        val result = parser.parse("Rs 500 for food")
        assertSuccess(result)
        assertEquals(50_000L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun parses_rs_dot_prefix() {
        val result = parser.parse("Rs. 1,200 for travel")
        assertSuccess(result)
        assertEquals(120_000L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun parses_inr_prefix() {
        val result = parser.parse("INR 2500 for shopping")
        assertSuccess(result)
        assertEquals(250_000L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun parses_rupees_word_format() {
        val result = parser.parse("500 rupees for lunch")
        assertSuccess(result)
        assertEquals(50_000L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun parses_bare_number_after_for_keyword() {
        val result = parser.parse("for 45 groceries")
        assertSuccess(result)
        assertEquals(4_500L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun parses_bare_number_after_on_keyword() {
        val result = parser.parse("on 200 taxi")
        assertSuccess(result)
        assertEquals(20_000L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun parses_bare_number_at_end() {
        val result = parser.parse("groceries 500")
        assertSuccess(result)
        assertEquals(50_000L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Transaction type detection
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun detects_expense_from_spent_keyword() {
        val result = parser.parse("Spent 50 on food")
        assertSuccess(result)
        assertEquals(2, (result as VoiceParseResult.Success).transaction.transactionTypeId)
    }

    @Test
    fun detects_expense_from_paid_keyword() {
        val result = parser.parse("Paid 100 for taxi")
        assertSuccess(result)
        assertEquals(2, (result as VoiceParseResult.Success).transaction.transactionTypeId)
    }

    @Test
    fun detects_expense_from_bought_keyword() {
        val result = parser.parse("Bought coffee for 5")
        assertSuccess(result)
        assertEquals(2, (result as VoiceParseResult.Success).transaction.transactionTypeId)
    }

    @Test
    fun detects_expense_from_ordered_keyword() {
        val result = parser.parse("Ordered pizza for 20")
        assertSuccess(result)
        assertEquals(2, (result as VoiceParseResult.Success).transaction.transactionTypeId)
    }

    @Test
    fun detects_income_from_received_keyword() {
        val result = parser.parse("Received 50000 salary")
        assertSuccess(result)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.transactionTypeId)
    }

    @Test
    fun detects_income_from_earned_keyword() {
        val result = parser.parse("Earned 5000 from freelance")
        assertSuccess(result)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.transactionTypeId)
    }

    @Test
    fun detects_income_from_salary_keyword() {
        val result = parser.parse("Salary 75000")
        assertSuccess(result)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.transactionTypeId)
    }

    @Test
    fun detects_income_from_cashback_keyword() {
        val result = parser.parse("Cashback 50 received")
        assertSuccess(result)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.transactionTypeId)
    }

    @Test
    fun detects_income_from_refund_keyword() {
        val result = parser.parse("Refund 300 received")
        assertSuccess(result)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.transactionTypeId)
    }

    @Test
    fun defaults_to_expense_when_no_type_keyword() {
        val result = parser.parse("45 groceries")
        assertSuccess(result)
        assertEquals(2, (result as VoiceParseResult.Success).transaction.transactionTypeId)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Category detection
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun detects_food_category() {
        val result = parser.parse("Spent 50 on food")
        assertSuccess(result)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_food_category_from_restaurant_keyword() {
        val result = parser.parse("Paid 30 at restaurant")
        assertSuccess(result)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_food_category_from_coffee_keyword() {
        val result = parser.parse("Bought coffee for 5")
        assertSuccess(result)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_food_category_from_swiggy() {
        val result = parser.parse("Paid 350 at Swiggy")
        assertSuccess(result)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_food_category_from_zomato() {
        val result = parser.parse("Spent 400 on Zomato")
        assertSuccess(result)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_travel_category() {
        val result = parser.parse("Booked flight for 5000")
        assertSuccess(result)
        assertEquals(2, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_travel_category_from_hotel() {
        val result = parser.parse("Paid 3000 for hotel")
        assertSuccess(result)
        assertEquals(2, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_shopping_category() {
        val result = parser.parse("Spent 2000 on shopping")
        assertSuccess(result)
        assertEquals(3, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_shopping_category_from_amazon() {
        val result = parser.parse("Bought electronics on Amazon for 500")
        assertSuccess(result)
        assertEquals(3, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_bills_category() {
        val result = parser.parse("Paid 1000 for electricity bill")
        assertSuccess(result)
        assertEquals(4, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_bills_category_from_internet() {
        val result = parser.parse("Internet bill 800")
        assertSuccess(result)
        assertEquals(4, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_health_category() {
        val result = parser.parse("Paid 500 for medicine")
        assertSuccess(result)
        assertEquals(5, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_health_category_from_gym() {
        val result = parser.parse("Gym membership 2000")
        assertSuccess(result)
        assertEquals(5, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_entertainment_category() {
        val result = parser.parse("Paid 200 for movie")
        assertSuccess(result)
        assertEquals(6, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_entertainment_category_from_netflix() {
        val result = parser.parse("Netflix subscription 500")
        assertSuccess(result)
        assertEquals(6, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_rent_category() {
        val result = parser.parse("Paid 15000 for rent")
        assertSuccess(result)
        assertEquals(7, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_groceries_category() {
        val result = parser.parse("Spent 800 on groceries")
        assertSuccess(result)
        assertEquals(8, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_groceries_category_from_walmart() {
        val result = parser.parse("Paid 150 at Walmart")
        assertSuccess(result)
        assertEquals(8, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_education_category() {
        val result = parser.parse("Paid 5000 for course")
        assertSuccess(result)
        assertEquals(9, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_subscriptions_category() {
        val result = parser.parse("Subscription 200")
        assertSuccess(result)
        assertEquals(10, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_insurance_category() {
        val result = parser.parse("Insurance premium 3000")
        assertSuccess(result)
        assertEquals(11, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_gifts_category() {
        val result = parser.parse("Gift for birthday 500")
        assertSuccess(result)
        assertEquals(12, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_personal_care_category() {
        val result = parser.parse("Haircut 300")
        assertSuccess(result)
        assertEquals(13, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_fuel_category() {
        val result = parser.parse("Petrol 2000")
        assertSuccess(result)
        assertEquals(14, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_maintenance_category() {
        val result = parser.parse("Repair 500")
        assertSuccess(result)
        assertEquals(15, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_transport_category() {
        val result = parser.parse("Uber ride 150")
        assertSuccess(result)
        assertEquals(22, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_transport_category_from_taxi() {
        val result = parser.parse("Taxi 200")
        assertSuccess(result)
        assertEquals(22, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_donations_category() {
        val result = parser.parse("Donation 1000")
        assertSuccess(result)
        assertEquals(19, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_pets_category() {
        val result = parser.parse("Pet food 500")
        assertSuccess(result)
        assertEquals(17, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_childcare_category() {
        val result = parser.parse("Daycare 3000")
        assertSuccess(result)
        assertEquals(18, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun detects_taxes_category() {
        val result = parser.parse("Tax payment 5000")
        assertSuccess(result)
        assertEquals(16, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun falls_back_to_other_expense_for_unknown_category() {
        val result = parser.parse("Spent 100 random")
        assertSuccess(result)
        assertEquals(23, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun falls_back_to_salary_for_income_with_salary_keyword() {
        val result = parser.parse("Received salary 50000")
        assertSuccess(result)
        val transaction = (result as VoiceParseResult.Success).transaction
        assertEquals(1, transaction.transactionTypeId)
        assertEquals(101, transaction.categoryId)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Merchant extraction
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun extracts_merchant_from_at_pattern() {
        val result = parser.parse("Paid 50 at Starbucks")
        assertSuccess(result)
        assertEquals("Starbucks", (result as VoiceParseResult.Success).transaction.merchant)
    }

    @Test
    fun extracts_merchant_from_at_pattern_with_category() {
        val result = parser.parse("Spent 30 at McDonalds for food")
        assertSuccess(result)
        assertEquals("McDonalds", (result as VoiceParseResult.Success).transaction.merchant)
    }

    @Test
    fun extracts_merchant_from_from_pattern() {
        val result = parser.parse("Received 100 from John")
        assertSuccess(result)
        assertEquals("John", (result as VoiceParseResult.Success).transaction.merchant)
    }

    @Test
    fun extracts_merchant_from_to_pattern() {
        val result = parser.parse("Sent 500 to Alice")
        assertSuccess(result)
        assertEquals("Alice", (result as VoiceParseResult.Success).transaction.merchant)
    }

    @Test
    fun returns_null_merchant_when_no_pattern_matches() {
        val result = parser.parse("Spent 50 on food")
        assertSuccess(result)
        assertEquals(null, (result as VoiceParseResult.Success).transaction.merchant)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Date extraction
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun detects_today_keyword() {
        val result = parser.parse("Spent 50 on food today")
        assertSuccess(result)
        val transaction = (result as VoiceParseResult.Success).transaction
        val now = System.currentTimeMillis()
        // Should be within 1 second of now
        assertTrue("Date should be today", kotlin.math.abs(transaction.createdAt - now) < 1000)
    }

    @Test
    fun detects_yesterday_keyword() {
        val result = parser.parse("Spent 50 on food yesterday")
        assertSuccess(result)
        val transaction = (result as VoiceParseResult.Success).transaction
        val now = System.currentTimeMillis()
        val yesterday = now - 86_400_000L // 24 hours ago
        // Should be within 1 minute of yesterday
        assertTrue("Date should be yesterday", kotlin.math.abs(transaction.createdAt - yesterday) < 60_000)
    }

    @Test
    fun detects_days_ago_keyword() {
        val result = parser.parse("Spent 50 on food 3 days ago")
        assertSuccess(result)
        val transaction = (result as VoiceParseResult.Success).transaction
        val now = System.currentTimeMillis()
        val threeDaysAgo = now - 3 * 86_400_000L
        // Should be within 1 minute of 3 days ago
        assertTrue("Date should be 3 days ago", kotlin.math.abs(transaction.createdAt - threeDaysAgo) < 60_000)
    }

    @Test
    fun detects_weeks_ago_keyword() {
        val result = parser.parse("Spent 50 on food 2 weeks ago")
        assertSuccess(result)
        val transaction = (result as VoiceParseResult.Success).transaction
        val now = System.currentTimeMillis()
        val twoWeeksAgo = now - 14 * 86_400_000L
        // Should be within 1 minute of 2 weeks ago
        assertTrue("Date should be 2 weeks ago", kotlin.math.abs(transaction.createdAt - twoWeeksAgo) < 60_000)
    }

    @Test
    fun defaults_to_now_when_no_date_keyword() {
        val result = parser.parse("Spent 50 on food")
        assertSuccess(result)
        val transaction = (result as VoiceParseResult.Success).transaction
        val now = System.currentTimeMillis()
        // Should be within 1 second of now
        assertTrue("Date should be now", kotlin.math.abs(transaction.createdAt - now) < 1000)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Confidence calculation
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun high_confidence_when_all_fields_detected() {
        val result = parser.parse("Spent 50 on food yesterday")
        assertSuccess(result)
        assertEquals(VoiceConfidence.HIGH, (result as VoiceParseResult.Success).transaction.confidence)
    }

    @Test
    fun medium_confidence_when_amount_and_category_detected() {
        val result = parser.parse("Spent 50 on food")
        assertSuccess(result)
        val confidence = (result as VoiceParseResult.Success).transaction.confidence
        assertTrue("Should be MEDIUM or HIGH", confidence == VoiceConfidence.MEDIUM || confidence == VoiceConfidence.HIGH)
    }

    @Test
    fun low_confidence_when_only_amount_detected() {
        val result = parser.parse("100")
        assertSuccess(result)
        // With only a number, confidence should be LOW or MEDIUM
        val confidence = (result as VoiceParseResult.Success).transaction.confidence
        assertTrue("Should be LOW or MEDIUM", confidence == VoiceConfidence.LOW || confidence == VoiceConfidence.MEDIUM)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Note generation
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun generates_clean_note_from_input() {
        val result = parser.parse("Add 50 for groceries at Walmart yesterday")
        assertSuccess(result)
        val note = (result as VoiceParseResult.Success).transaction.note
        assertTrue("Note should not contain 'Add'", !note.contains("Add", ignoreCase = true))
        assertTrue("Note should not contain 'for'", !note.contains("for", ignoreCase = true))
        assertTrue("Note should not contain 'Walmart'", !note.contains("Walmart", ignoreCase = true))
    }

    @Test
    fun capitalizes_note_first_letter() {
        val result = parser.parse("spent 50 on food")
        assertSuccess(result)
        val note = (result as VoiceParseResult.Success).transaction.note
        assertTrue("Note should start with uppercase", note[0].isUpperCase())
    }

    @Test
    fun returns_transaction_when_note_is_empty() {
        val result = parser.parse("Add 50")
        assertSuccess(result)
        val note = (result as VoiceParseResult.Success).transaction.note
        assertTrue("Note should be 'Transaction' or similar", note.isNotEmpty())
    }

    // ──────────────────────────────────────────────────────────────────────
    // Edge cases
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun returns_failed_for_empty_input() {
        val result = parser.parse("")
        assertTrue(result is VoiceParseResult.Failed)
        assertEquals(R.string.msg_voice_error_empty_input, (result as VoiceParseResult.Failed).errorMessageResId)
    }

    @Test
    fun returns_failed_for_blank_input() {
        val result = parser.parse("   ")
        assertTrue(result is VoiceParseResult.Failed)
        assertEquals(R.string.msg_voice_error_empty_input, (result as VoiceParseResult.Failed).errorMessageResId)
    }

    @Test
    fun returns_failed_when_no_amount_detected() {
        val result = parser.parse("buy groceries")
        assertTrue(result is VoiceParseResult.Failed)
        assertEquals(R.string.msg_voice_error_no_amount, (result as VoiceParseResult.Failed).errorMessageResId)
    }

    @Test
    fun handles_case_insensitive_input() {
        val result = parser.parse("SPENT 50 ON FOOD")
        assertSuccess(result)
        assertEquals(2, (result as VoiceParseResult.Success).transaction.transactionTypeId)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun handles_mixed_case_input() {
        val result = parser.parse("Spent 50 on Food")
        assertSuccess(result)
        assertEquals(1, (result as VoiceParseResult.Success).transaction.categoryId)
    }

    @Test
    fun handles_extra_whitespace() {
        val result = parser.parse("  Spent   50   on   food  ")
        assertSuccess(result)
        assertEquals(5_000L, (result as VoiceParseResult.Success).transaction.amountMinor)
    }

    @Test
    fun complex_voice_input_with_all_fields() {
        val result = parser.parse("Add $45 for groceries at Walmart yesterday")
        assertSuccess(result)
        val transaction = (result as VoiceParseResult.Success).transaction
        assertEquals(4_500L, transaction.amountMinor)
        assertEquals(2, transaction.transactionTypeId)
        assertEquals(8, transaction.categoryId) // Groceries
        assertEquals("Walmart", transaction.merchant)
        assertEquals(VoiceConfidence.HIGH, transaction.confidence)
    }

    @Test
    fun complex_voice_input_income() {
        val result = parser.parse("Received salary 75000 today")
        assertSuccess(result)
        val transaction = (result as VoiceParseResult.Success).transaction
        assertEquals(7_500_000L, transaction.amountMinor)
        assertEquals(1, transaction.transactionTypeId)
        assertEquals(101, transaction.categoryId) // Salary
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────────────────────────────

    private fun assertSuccess(result: VoiceParseResult) {
        assertTrue("Expected Success but got $result", result is VoiceParseResult.Success)
    }
}
