package com.mknlabs.expensetracker.sms

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests the learning-system hook in [SmsCategoryDetector] (plan §10): user
 * merchant→category overrides must win over static rules, but only when they
 * resolve to a valid category of the detected transaction type.
 */
class SmsCategoryDetectorOverrideTest {

    @Test
    fun override_beatsStaticRule() {
        val detection = SmsCategoryDetector.detect(
            body = "Rs 520 debited at Swiggy via UPI",
            transactionTypeId = 2,
            userOverrides = mapOf("swiggy" to 22) // user remapped Swiggy → Transport
        )

        assertEquals(22, detection.categoryId)
        assertEquals(SmsConfidence.HIGH, detection.confidence)
        assertEquals("Swiggy", detection.merchant)
    }

    @Test
    fun override_matchesCaseInsensitively_inBody() {
        val detection = SmsCategoryDetector.detect(
            body = "SWIGGY ORDER CONFIRMED Rs 520 debited",
            transactionTypeId = 2,
            userOverrides = mapOf("swiggy" to 22)
        )

        assertEquals(22, detection.categoryId)
    }

    @Test
    fun override_ignored_whenMerchantAbsentFromBody() {
        val detection = SmsCategoryDetector.detect(
            body = "Rs 520 debited at Zomato via UPI",
            transactionTypeId = 2,
            userOverrides = mapOf("swiggy" to 22)
        )

        // Zomato's static Food rule applies (override mentions a different merchant).
        assertEquals(1, detection.categoryId)
    }

    @Test
    fun override_fallsThroughToStaticRules_whenCategoryIdUnknown() {
        val detection = SmsCategoryDetector.detect(
            body = "Rs 520 debited at Swiggy via UPI",
            transactionTypeId = 2,
            userOverrides = mapOf("swiggy" to 999)
        )

        // Invalid id can't be resolved → static Food rule wins.
        assertEquals(1, detection.categoryId)
    }

    @Test
    fun override_fallsThroughToStaticRules_whenCategoryIsWrongType() {
        val detection = SmsCategoryDetector.detect(
            body = "Rs 520 debited at Swiggy via UPI",
            transactionTypeId = 2, // expense
            userOverrides = mapOf("swiggy" to 101) // 101 = Salary (income only)
        )

        assertEquals(1, detection.categoryId) // static Food
    }

    @Test
    fun override_applies_whenCategoryMatchesTransactionType() {
        val detection = SmsCategoryDetector.detect(
            body = "Rs 15,000 credited from Swiggy", // income body
            transactionTypeId = 1,
            userOverrides = mapOf("swiggy" to 101)
        )

        assertEquals(101, detection.categoryId)
        assertEquals(SmsConfidence.HIGH, detection.confidence)
    }

    @Test
    fun noOverrides_keepsStaticBehavior() {
        val detection = SmsCategoryDetector.detect(
            body = "Rs 520 debited at Swiggy via UPI",
            transactionTypeId = 2
        )

        assertEquals(1, detection.categoryId) // static Food
        assertEquals(SmsConfidence.HIGH, detection.confidence)
    }
}
