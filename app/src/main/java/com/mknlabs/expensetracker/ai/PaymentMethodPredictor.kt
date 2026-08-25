package com.mknlabs.expensetracker.ai

import com.mknlabs.expensetracker.domain.repository.PaymentMethodPredictorRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for the merchant → payment method learning store.
 * Abstraction allows unit testing without Android DataStore dependency.
 */
interface PaymentMethodOverrideStore {
    suspend fun getOverrides(): Map<String, Int>
    suspend fun setOverride(merchant: String, paymentMethodId: Int)
    suspend fun removeOverride(merchant: String)
}

/**
 * Offline, DataStore-backed payment method predictor.
 *
 * Learns from user transaction history: when a user saves a transaction
 * with a merchant and a payment method, the association is recorded.
 * On the next transaction at the same merchant, this predictor returns
 * the remembered payment method.
 *
 * Hilt-managed singleton: injected via [PaymentMethodPredictorRepository]
 * binding in [com.mknlabs.expensetracker.di.PaymentMethodPredictorModule].
 */
@Singleton
class PaymentMethodPredictor @Inject constructor(
    private val overrideStore: PaymentMethodOverrideStore
) : PaymentMethodPredictorRepository {

    override suspend fun predict(merchantText: String): Int? {
        val normalized = normalizeMerchant(merchantText)
        if (normalized.isEmpty()) return null

        val overrides = overrideStore.getOverrides()

        // Exact match first
        overrides[normalized]?.let { return it }

        // Partial match: check if any learned merchant is contained in the input
        // or if the input contains a learned merchant
        for ((merchant, paymentMethodId) in overrides) {
            if (merchant.length < 3) continue // Skip very short keys to avoid false positives
            if (normalized.contains(merchant) || merchant.contains(normalized)) {
                return paymentMethodId
            }
        }

        return null
    }

    override suspend fun learn(merchantText: String, paymentMethodId: Int) {
        val normalized = normalizeMerchant(merchantText)
        if (normalized.isEmpty()) return
        overrideStore.setOverride(normalized, paymentMethodId)
    }

    override suspend fun forget(merchantText: String) {
        val normalized = normalizeMerchant(merchantText)
        if (normalized.isEmpty()) return
        overrideStore.removeOverride(normalized)
    }

    private fun normalizeMerchant(merchant: String): String =
        merchant.trim().lowercase(Locale.ROOT)
}
