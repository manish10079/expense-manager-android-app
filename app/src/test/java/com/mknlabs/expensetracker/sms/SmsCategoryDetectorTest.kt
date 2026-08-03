package com.mknlabs.expensetracker.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmsCategoryDetectorTest {

    // Transaction type context
    private val expense = 2
    private val income = 1

    @Test
    fun food_merchants_map_to_food_with_high_confidence() {
        val swiggy = SmsCategoryDetector.detect("Rs 349.50 debited at Swiggy", expense)
        assertEquals(1, swiggy.categoryId)
        assertEquals(SmsConfidence.HIGH, swiggy.confidence)
        assertEquals("Swiggy", swiggy.merchant)

        val zomato = SmsCategoryDetector.detect("Rs 400 paid at Zomato", expense)
        assertEquals(1, zomato.categoryId)
        assertEquals(SmsConfidence.HIGH, zomato.confidence)
    }

    @Test
    fun fuel_merchants_map_to_fuel() {
        listOf("IndianOil", "HPCL", "BPCL", "IOCL").forEach { merchant ->
            val detection = SmsCategoryDetector.detect("Rs 1,000 debited at $merchant", expense)
            assertEquals(14, detection.categoryId)
            assertEquals(SmsConfidence.HIGH, detection.confidence)
        }
    }

    @Test
    fun shopping_merchants_map_to_shopping_with_medium_confidence() {
        listOf("Amazon", "Flipkart", "Myntra").forEach { merchant ->
            val detection = SmsCategoryDetector.detect("Rs 1,299 debited at $merchant.in", expense)
            assertEquals(3, detection.categoryId)
            assertEquals(SmsConfidence.MEDIUM, detection.confidence)
        }
    }

    @Test
    fun transport_merchants_map_to_transport() {
        val uber = SmsCategoryDetector.detect("Rs 185.00 paid to Uber India via UPI", expense)
        assertEquals(22, uber.categoryId)
        assertEquals(SmsConfidence.HIGH, uber.confidence)

        val ola = SmsCategoryDetector.detect("Rs 220 paid to Ola Cabs", expense)
        assertEquals(22, ola.categoryId)
    }

    @Test
    fun entertainment_merchants_map_to_entertainment() {
        listOf("Netflix", "Spotify", "BookMyShow").forEach { merchant ->
            val detection = SmsCategoryDetector.detect("Rs 199 debited for $merchant subscription", expense)
            assertEquals(6, detection.categoryId)
        }
    }

    @Test
    fun grocery_merchants_map_to_groceries() {
        listOf("BigBasket", "Blinkit", "Zepto", "D Mart", "DMart").forEach { merchant ->
            val detection = SmsCategoryDetector.detect("Rs 850 debited at $merchant", expense)
            assertEquals(8, detection.categoryId)
            assertEquals(SmsConfidence.HIGH, detection.confidence)
        }
    }

    @Test
    fun bill_keywords_map_to_bills() {
        val electricity = SmsCategoryDetector.detect("Rs 1,120 debited for electricity bill", expense)
        assertEquals(4, electricity.categoryId)

        val gas = SmsCategoryDetector.detect("Rs 540 paid for gas bill", expense)
        assertEquals(4, gas.categoryId)
    }

    @Test
    fun salary_maps_to_salary_with_high_confidence() {
        val detection = SmsCategoryDetector.detect("Salary for July credited", income)
        assertEquals(101, detection.categoryId)
        assertEquals(SmsConfidence.HIGH, detection.confidence)
        assertNull(detection.merchant)
    }

    @Test
    fun cashback_maps_to_income_other() {
        val detection = SmsCategoryDetector.detect("Cashback of Rs 50 credited", income)
        assertEquals(105, detection.categoryId)
        assertEquals(SmsConfidence.MEDIUM, detection.confidence)
    }

    @Test
    fun refund_maps_to_income_other() {
        val detection = SmsCategoryDetector.detect("Refund of Rs 300 credited to card", income)
        assertEquals(105, detection.categoryId)
    }

    @Test
    fun interest_maps_to_investment() {
        val detection = SmsCategoryDetector.detect("Interest of Rs 25 credited to account", income)
        assertEquals(103, detection.categoryId)
    }

    @Test
    fun unknown_expense_falls_back_to_other_with_low_confidence() {
        val detection = SmsCategoryDetector.detect("Rs 90 debited for random charge", expense)
        assertEquals(23, detection.categoryId)
        assertEquals(SmsConfidence.LOW, detection.confidence)
        assertNull(detection.merchant)
    }

    @Test
    fun unknown_income_falls_back_to_income_other() {
        val detection = SmsCategoryDetector.detect("Rs 500 received from unknown sender", income)
        assertEquals(105, detection.categoryId)
        assertEquals(SmsConfidence.LOW, detection.confidence)
    }

    @Test
    fun income_keyword_on_expense_transaction_falls_back_to_other() {
        // "interest" resolves to Investment (income-only); on an expense message
        // it must fall back to expense Other instead of a wrong category.
        val detection = SmsCategoryDetector.detect("Rs 50 interest charge debited", expense)
        assertEquals(23, detection.categoryId)
    }

    @Test
    fun merchant_detection_is_case_insensitive() {
        val detection = SmsCategoryDetector.detect("Rs 400 debited at SWIGGY", expense)
        assertEquals(1, detection.categoryId)
        assertNotNull(detection.merchant)
    }
}
