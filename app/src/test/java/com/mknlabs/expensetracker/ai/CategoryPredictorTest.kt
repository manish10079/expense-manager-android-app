package com.mknlabs.expensetracker.ai

import com.mknlabs.expensetracker.domain.models.PredictionConfidence
import com.mknlabs.expensetracker.domain.models.PredictionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CategoryPredictorTest {

    private lateinit var predictor: CategoryPredictor

    @Before
    fun setUp() {
        predictor = CategoryPredictor()
    }

    // ──────────────────────────────────────────────────────────────────────
    // Merchant rules — exact merchant matches
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun merchant_swiggy_maps_to_food() {
        val result = predictor.predict("Paid 349 at Swiggy", 2)
        assertEquals(1, result.categoryId) // Food
        assertEquals(PredictionSource.MERCHANT_MATCH, result.source)
    }

    @Test
    fun merchant_zomato_maps_to_food() {
        val result = predictor.predict("Rs 400 paid at Zomato", 2)
        assertEquals(1, result.categoryId) // Food
        assertEquals(PredictionSource.MERCHANT_MATCH, result.source)
    }

    @Test
    fun merchant_amazon_maps_to_shopping() {
        val result = predictor.predict("Bought electronics on Amazon", 2)
        assertEquals(3, result.categoryId) // Shopping
        assertEquals(PredictionSource.MERCHANT_MATCH, result.source)
    }

    @Test
    fun merchant_uber_maps_to_transport() {
        val result = predictor.predict("Uber ride 185", 2)
        assertEquals(22, result.categoryId) // Transport
        assertEquals(PredictionSource.MERCHANT_MATCH, result.source)
    }

    @Test
    fun merchant_ola_maps_to_transport() {
        val result = predictor.predict("Paid 220 to Ola Cabs", 2)
        assertEquals(22, result.categoryId) // Transport
    }

    @Test
    fun merchant_netflix_maps_to_entertainment() {
        val result = predictor.predict("Netflix subscription 500", 2)
        assertEquals(6, result.categoryId) // Entertainment
    }

    @Test
    fun merchant_bigbasket_maps_to_groceries() {
        val result = predictor.predict("Bigbasket order 800", 2)
        assertEquals(8, result.categoryId) // Groceries
    }

    @Test
    fun merchant_blinkit_maps_to_groceries() {
        val result = predictor.predict("Blinkit delivery 350", 2)
        assertEquals(8, result.categoryId) // Groceries
    }

    @Test
    fun merchant_walmart_maps_to_groceries() {
        val result = predictor.predict("Paid 150 at Walmart", 2)
        assertEquals(8, result.categoryId) // Groceries
    }

    @Test
    fun merchant_hpcl_maps_to_fuel() {
        val result = predictor.predict("HPCL petrol 2000", 2)
        assertEquals(14, result.categoryId) // Fuel
    }

    @Test
    fun merchant_spotify_maps_to_entertainment() {
        val result = predictor.predict("Spotify premium 199", 2)
        assertEquals(6, result.categoryId) // Entertainment
    }

    @Test
    fun merchant_flipkart_maps_to_shopping() {
        val result = predictor.predict("Flipkart order 2999", 2)
        assertEquals(3, result.categoryId) // Shopping
    }

    @Test
    fun merchant_dmart_maps_to_groceries() {
        val result = predictor.predict("Dmart purchase 1200", 2)
        assertEquals(8, result.categoryId) // Groceries
    }

    @Test
    fun merchant_case_insensitive_match() {
        val result = predictor.predict("SWIGGY order 350", 2)
        assertEquals(1, result.categoryId) // Food
    }

    @Test
    fun merchant_returns_high_confidence() {
        val result = predictor.predict("Paid 349 at Swiggy", 2)
        assertEquals(PredictionConfidence.HIGH, result.confidence)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Keyword rules — broader text matches
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun keyword_food() {
        val result = predictor.predict("Spent 50 on food", 2)
        assertEquals(1, result.categoryId) // Food
        assertEquals(PredictionSource.KEYWORD_MATCH, result.source)
    }

    @Test
    fun keyword_coffee() {
        val result = predictor.predict("Bought coffee for 5", 2)
        assertEquals(1, result.categoryId) // Food
    }

    @Test
    fun keyword_restaurant() {
        val result = predictor.predict("Dinner at restaurant 500", 2)
        assertEquals(1, result.categoryId) // Food
    }

    @Test
    fun keyword_travel() {
        val result = predictor.predict("Travel expenses 5000", 2)
        assertEquals(2, result.categoryId) // Travel
    }

    @Test
    fun keyword_hotel() {
        val result = predictor.predict("Hotel stay 3000", 2)
        assertEquals(2, result.categoryId) // Travel
    }

    @Test
    fun keyword_flight() {
        val result = predictor.predict("Flight booking 8000", 2)
        assertEquals(2, result.categoryId) // Travel
    }

    @Test
    fun keyword_shopping() {
        val result = predictor.predict("Shopping spree 2000", 2)
        assertEquals(3, result.categoryId) // Shopping
    }

    @Test
    fun keyword_electricity_bill() {
        val result = predictor.predict("Electricity bill 1120", 2)
        assertEquals(4, result.categoryId) // Bills
    }

    @Test
    fun keyword_internet() {
        val result = predictor.predict("Internet bill 800", 2)
        assertEquals(4, result.categoryId) // Bills
    }

    @Test
    fun keyword_gym() {
        val result = predictor.predict("Gym membership 2000", 2)
        assertEquals(5, result.categoryId) // Health
    }

    @Test
    fun keyword_medicine() {
        val result = predictor.predict("Medicine 500", 2)
        assertEquals(5, result.categoryId) // Health
    }

    @Test
    fun keyword_movie() {
        val result = predictor.predict("Movie tickets 400", 2)
        assertEquals(6, result.categoryId) // Entertainment
    }

    @Test
    fun keyword_rent() {
        val result = predictor.predict("Rent payment 15000", 2)
        assertEquals(7, result.categoryId) // Rent
    }

    @Test
    fun keyword_groceries() {
        val result = predictor.predict("Groceries 800", 2)
        assertEquals(8, result.categoryId) // Groceries
    }

    @Test
    fun keyword_tuition() {
        val result = predictor.predict("Tuition fees 5000", 2)
        assertEquals(9, result.categoryId) // Education
    }

    @Test
    fun keyword_subscription() {
        val result = predictor.predict("Subscription renewal 200", 2)
        assertEquals(10, result.categoryId) // Subscriptions
    }

    @Test
    fun keyword_insurance() {
        val result = predictor.predict("Insurance premium 3000", 2)
        assertEquals(11, result.categoryId) // Insurance
    }

    @Test
    fun keyword_gift() {
        val result = predictor.predict("Gift for birthday 500", 2)
        assertEquals(12, result.categoryId) // Gifts
    }

    @Test
    fun keyword_haircut() {
        val result = predictor.predict("Haircut 300", 2)
        assertEquals(13, result.categoryId) // Personal Care
    }

    @Test
    fun keyword_petrol() {
        val result = predictor.predict("Petrol 2000", 2)
        assertEquals(14, result.categoryId) // Fuel
    }

    @Test
    fun keyword_repair() {
        val result = predictor.predict("Repair 500", 2)
        assertEquals(15, result.categoryId) // Maintenance
    }

    @Test
    fun keyword_tax() {
        val result = predictor.predict("Tax payment 5000", 2)
        assertEquals(16, result.categoryId) // Taxes
    }

    @Test
    fun keyword_pet() {
        val result = predictor.predict("Pet food 500", 2)
        assertEquals(17, result.categoryId) // Pets
    }

    @Test
    fun keyword_childcare() {
        val result = predictor.predict("Daycare 3000", 2)
        assertEquals(18, result.categoryId) // Childcare
    }

    @Test
    fun keyword_donation() {
        val result = predictor.predict("Donation 1000", 2)
        assertEquals(19, result.categoryId) // Donations
    }

    @Test
    fun keyword_taxi() {
        val result = predictor.predict("Taxi 200", 2)
        assertEquals(22, result.categoryId) // Transport
    }

    // ──────────────────────────────────────────────────────────────────────
    // Income categories (transactionTypeId = 1)
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun keyword_salary_income() {
        val result = predictor.predict("Received salary 75000", 1)
        assertEquals(101, result.categoryId) // Salary
    }

    @Test
    fun keyword_freelance_income() {
        val result = predictor.predict("Freelance payment 5000", 1)
        assertEquals(104, result.categoryId) // Freelance
    }

    @Test
    fun keyword_dividend_income() {
        val result = predictor.predict("Dividend received 2000", 1)
        assertEquals(103, result.categoryId) // Investment
    }

    @Test
    fun keyword_cashback_income() {
        val result = predictor.predict("Cashback 50 received", 1)
        assertEquals(105, result.categoryId) // Other (income)
    }

    @Test
    fun keyword_refund_income() {
        val result = predictor.predict("Refund 300 received", 1)
        assertEquals(105, result.categoryId) // Other (income)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Fallback behavior
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun falls_back_to_other_expense_for_unknown_input() {
        val result = predictor.predict("random text 100", 2)
        assertEquals(23, result.categoryId) // Other (expense)
        assertEquals(PredictionSource.FALLBACK, result.source)
        assertEquals(PredictionConfidence.LOW, result.confidence)
    }

    @Test
    fun falls_back_to_other_income_for_unknown_income_input() {
        val result = predictor.predict("something 5000", 1)
        assertEquals(105, result.categoryId) // Other (income)
        assertEquals(PredictionSource.FALLBACK, result.source)
    }

    @Test
    fun blank_input_returns_fallback() {
        val result = predictor.predict("", 2)
        assertEquals(23, result.categoryId)
        assertEquals(PredictionSource.FALLBACK, result.source)
    }

    @Test
    fun blank_input_income_returns_fallback() {
        val result = predictor.predict("   ", 1)
        assertEquals(105, result.categoryId)
    }

    // ──────────────────────────────────────────────────────────────────────
    // User overrides (learning system)
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun user_override_takes_priority() {
        val overrides = mapOf("swiggy" to 4) // Remap Swiggy → Bills
        val result = predictor.predict("Paid 349 at Swiggy", 2, overrides)
        assertEquals(4, result.categoryId) // Bills (user override)
        assertEquals(PredictionSource.USER_LEARNED, result.source)
        assertEquals(PredictionConfidence.HIGH, result.confidence)
    }

    @Test
    fun user_override_ignores_wrong_transaction_type() {
        // Override maps "salary" to category 101 (Salary, income type=1)
        // but predict is called with transactionTypeId=2 (expense)
        // So override should be ignored (type mismatch), fall through to keyword
        val overrides = mapOf("salary" to 101) // Salary is income category
        val result = predictor.predict("Salary 75000", 2, overrides)
        // Override ignored (101 is income, but transaction is expense)
        // Keyword "salary" → Salary (101) also won't match expense type
        // So falls back to Other expense (23)
        assertEquals(23, result.categoryId) // Other (expense) fallback
    }

    @Test
    fun user_override_empty_map_falls_through() {
        val result = predictor.predict("Paid 349 at Swiggy", 2, emptyMap())
        assertEquals(1, result.categoryId) // Food via merchant rule
        assertEquals(PredictionSource.MERCHANT_MATCH, result.source)
    }

    @Test
    fun user_override_blank_merchant_ignored() {
        val overrides = mapOf("" to 4)
        val result = predictor.predict("Paid 349 at Swiggy", 2, overrides)
        assertEquals(1, result.categoryId) // Food via merchant rule
    }

    // ──────────────────────────────────────────────────────────────────────
    // Priority: merchant rules > keyword rules
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun merchant_rule_wins_over_keyword_rule() {
        // "Swiggy" matches merchant rule (Food) AND keyword rule (Food via "food")
        // Merchant should win because it's checked first
        val result = predictor.predict("Swiggy food order 350", 2)
        assertEquals(1, result.categoryId) // Food
        assertEquals(PredictionSource.MERCHANT_MATCH, result.source)
    }

    @Test
    fun keyword_rule_used_when_no_merchant_match() {
        val result = predictor.predict("Lunch 200", 2)
        assertEquals(1, result.categoryId) // Food via keyword
        assertEquals(PredictionSource.KEYWORD_MATCH, result.source)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Multi-word keywords (specific before generic)
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun pet_food_maps_to_pets_not_food() {
        val result = predictor.predict("Bought pet food 500", 2)
        assertEquals(17, result.categoryId) // Pets (not Food)
    }

    @Test
    fun electricity_bill_maps_to_bills_not_electricity() {
        val result = predictor.predict("Electricity bill 1120", 2)
        assertEquals(4, result.categoryId) // Bills
    }

    @Test
    fun gas_station_maps_to_fuel() {
        val result = predictor.predict("Gas station 2000", 2)
        assertEquals(14, result.categoryId) // Fuel
    }

    // ──────────────────────────────────────────────────────────────────────
    // Case insensitivity
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun handles_uppercase_input() {
        val result = predictor.predict("SPENT 50 ON FOOD", 2)
        assertEquals(1, result.categoryId) // Food
    }

    @Test
    fun handles_mixed_case_input() {
        val result = predictor.predict("Paid 300 at NETFLIX", 2)
        assertEquals(6, result.categoryId) // Entertainment
    }

    // ──────────────────────────────────────────────────────────────────────
    // Confidence scoring
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun merchant_match_returns_medium_or_high() {
        val result = predictor.predict("Amazon order 500", 2)
        assertTrue(
            "Merchant confidence should be MEDIUM or HIGH",
            result.confidence == PredictionConfidence.MEDIUM ||
                result.confidence == PredictionConfidence.HIGH
        )
    }

    @Test
    fun keyword_match_returns_medium_or_low() {
        val result = predictor.predict("Something food 100", 2)
        assertTrue(
            "Keyword confidence should be MEDIUM or LOW",
            result.confidence == PredictionConfidence.MEDIUM ||
                result.confidence == PredictionConfidence.LOW
        )
    }

    @Test
    fun fallback_returns_low() {
        val result = predictor.predict("xyz 100", 2)
        assertEquals(PredictionConfidence.LOW, result.confidence)
    }
}
