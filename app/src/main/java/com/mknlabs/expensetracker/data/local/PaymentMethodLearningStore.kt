package com.mknlabs.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.Locale
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import com.mknlabs.expensetracker.ai.PaymentMethodOverrideStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.paymentMethodLearningDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "payment_method_learning"
)

/**
 * Qualifier for the dedicated payment method learning DataStore.
 * Kept separate from app-settings and SMS learning stores so merchant→payment
 * mappings stay private to the auto-payment-method feature.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PaymentMethodLearningDataStore

/**
 * The auto payment method learning system — a merchant → paymentMethodId
 * override map persisted in DataStore.
 *
 * When a user saves a transaction with a merchant and a payment method,
 * this store records the association. On the next transaction at the same
 * merchant, [PaymentMethodPredictor] consults this store to auto-fill
 * the payment method.
 *
 * Merchant keys are normalized (trimmed + lowercased with [Locale.ROOT])
 * so the predictor can match them against lowercased text input.
 */
@Singleton
class PaymentMethodLearningStore @Inject constructor(
    @PaymentMethodLearningDataStore private val dataStore: DataStore<Preferences>
) : PaymentMethodOverrideStore {

    /** Observable snapshot of all stored merchant → paymentMethodId mappings. */
    fun observeOverrides(): Flow<Map<String, Int>> =
        dataStore.data.map { preferences ->
            preferences.asMap()
                .filterKeys { it.name.startsWith(KEY_PREFIX) }
                .mapNotNull { (key, value) ->
                    val merchant = key.name.removePrefix(KEY_PREFIX)
                    (value as? Int)?.let { merchant to it }
                }
                .toMap()
        }

    /** Snapshot of all stored overrides (non-reactive). */
    override suspend fun getOverrides(): Map<String, Int> {
        return dataStore.data.first().let { preferences ->
            preferences.asMap()
                .filterKeys { it.name.startsWith(KEY_PREFIX) }
                .mapNotNull { (key, value) ->
                    val merchant = key.name.removePrefix(KEY_PREFIX)
                    (value as? Int)?.let { merchant to it }
                }
                .toMap()
        }
    }

    /** Records (or replaces) a merchant → paymentMethodId mapping. */
    override suspend fun setOverride(merchant: String, paymentMethodId: Int) {
        val normalized = normalizeMerchant(merchant)
        if (normalized.isEmpty()) return
        dataStore.edit { it[intPreferencesKey(keyFor(normalized))] = paymentMethodId }
    }

    /** Removes a previously recorded mapping (no-op when none exists). */
    override suspend fun removeOverride(merchant: String) {
        val normalized = normalizeMerchant(merchant)
        if (normalized.isEmpty()) return
        dataStore.edit { it.remove(intPreferencesKey(keyFor(normalized))) }
    }

    private fun normalizeMerchant(merchant: String): String =
        merchant.trim().lowercase(Locale.ROOT)

    private fun keyFor(normalizedMerchant: String): String = "$KEY_PREFIX$normalizedMerchant"

    private companion object {
        const val KEY_PREFIX = "pm_learning."
    }
}
