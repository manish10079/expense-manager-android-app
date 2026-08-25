package com.mknlabs.expensetracker.ai

import com.mknlabs.expensetracker.data.local.PaymentMethodLearningStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PaymentMethodPredictorTest {

    private lateinit var store: FakePaymentMethodLearningStore
    private lateinit var predictor: PaymentMethodPredictor

    @Before
    fun setUp() {
        store = FakePaymentMethodLearningStore()
        predictor = PaymentMethodPredictor(store)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Predict
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun predict_returns_null_for_unknown_merchant() = runTest {
        assertNull(predictor.predict("Starbucks"))
    }

    @Test
    fun predict_returns_null_for_blank_input() = runTest {
        assertNull(predictor.predict(""))
        assertNull(predictor.predict("   "))
    }

    @Test
    fun predict_returns_payment_method_for_learned_merchant() = runTest {
        store.setOverride("swiggy", 1) // UPI
        assertEquals(1, predictor.predict("Swiggy"))
    }

    @Test
    fun predict_is_case_insensitive() = runTest {
        store.setOverride("swiggy", 1)
        assertEquals(1, predictor.predict("SWIGGY"))
        assertEquals(1, predictor.predict("Swiggy"))
        assertEquals(1, predictor.predict("swiggy"))
    }

    @Test
    fun predict_handles_partial_match() = runTest {
        store.setOverride("starbucks", 4) // Card
        assertEquals(4, predictor.predict("Starbucks coffee downtown"))
    }

    @Test
    fun predict_returns_null_for_short_merchant_keys() = runTest {
        // Very short keys (< 3 chars) should not match partially
        store.setOverride("ab", 1)
        assertNull(predictor.predict("abc store"))
    }

    @Test
    fun predict_returns_null_after_forget() = runTest {
        store.setOverride("swiggy", 1)
        predictor.forget("swiggy")
        assertNull(predictor.predict("Swiggy"))
    }

    // ──────────────────────────────────────────────────────────────────────
    // Learn
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun learn_stores_merchant_payment_association() = runTest {
        predictor.learn("Swiggy", 1)
        assertEquals(1, predictor.predict("Swiggy"))
    }

    @Test
    fun learn_overwrites_previous_association() = runTest {
        predictor.learn("Swiggy", 1) // UPI
        predictor.learn("Swiggy", 4) // Card
        assertEquals(4, predictor.predict("Swiggy"))
    }

    @Test
    fun learn_ignores_blank_merchant() = runTest {
        predictor.learn("", 1)
        predictor.learn("   ", 1)
        assertNull(predictor.predict("anything"))
    }

    @Test
    fun learn_normalizes_merchant() = runTest {
        predictor.learn("  Swiggy  ", 1)
        assertEquals(1, predictor.predict("swiggy"))
    }

    // ──────────────────────────────────────────────────────────────────────
    // Forget
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun forget_removes_association() = runTest {
        predictor.learn("Swiggy", 1)
        predictor.forget("Swiggy")
        assertNull(predictor.predict("Swiggy"))
    }

    @Test
    fun forget_ignores_unknown_merchant() = runTest {
        // Should not throw
        predictor.forget("UnknownMerchant")
    }

    @Test
    fun forget_ignores_blank_merchant() = runTest {
        predictor.forget("")
        predictor.forget("   ")
    }

    // ──────────────────────────────────────────────────────────────────────
    // Multiple merchants
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun different_merchants_have_independent_predictions() = runTest {
        predictor.learn("Swiggy", 1) // UPI
        predictor.learn("Amazon", 4) // Card
        predictor.learn("Local Store", 2) // Cash

        assertEquals(1, predictor.predict("Swiggy"))
        assertEquals(4, predictor.predict("Amazon"))
        assertEquals(2, predictor.predict("Local Store"))
        assertNull(predictor.predict("Unknown"))
    }

    // ──────────────────────────────────────────────────────────────────────
    // Fake implementation for testing (avoids DataStore dependency)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * In-memory fake for [PaymentMethodOverrideStore] used in unit tests.
     * Mirrors the API surface without requiring Android Context or DataStore.
     */
    private class FakePaymentMethodLearningStore : PaymentMethodOverrideStore {
        private val overrides = mutableMapOf<String, Int>()

        override suspend fun getOverrides(): Map<String, Int> = overrides.toMap()

        override suspend fun setOverride(merchant: String, paymentMethodId: Int) {
            overrides[merchant] = paymentMethodId
        }

        override suspend fun removeOverride(merchant: String) {
            overrides.remove(merchant)
        }
    }
}
