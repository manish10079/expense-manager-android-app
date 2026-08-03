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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.smsLearningDataStore: DataStore<Preferences> by preferencesDataStore(name = "sms_learning")

/**
 * Qualifier for the dedicated SMS learning DataStore (plan §10). Kept separate
 * from the app-settings/user-profile stores so merchant mappings stay private
 * to the Smart SMS Import feature.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SmsLearningDataStore

/**
 * The Smart SMS learning system (plan §10) — a merchant → category override map
 * persisted in DataStore.
 *
 * Future-ready by design: the Change sheet records an override whenever the user
 * explicitly corrects a detected category, and [SmsCategoryDetector] consults it
 * BEFORE its static rule table, so the next identical SMS is suggested correctly.
 *
 * Merchant keys are normalized (trimmed + lowercased with [Locale.ROOT]) so the
 * detector can match them against lowercased SMS body text.
 */
@Singleton
class SmsLearningStore @Inject constructor(
    @SmsLearningDataStore private val dataStore: DataStore<Preferences>
) {

    /** Observable snapshot of all stored merchant → categoryId overrides. */
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

    /** Records (or replaces) a merchant → categoryId mapping. */
    suspend fun setOverride(merchant: String, categoryId: Int) {
        val normalized = normalizeMerchant(merchant)
        if (normalized.isEmpty()) return
        dataStore.edit { it[intPreferencesKey(keyFor(normalized))] = categoryId }
    }

    /** Removes a previously recorded mapping (no-op when none exists). */
    suspend fun removeOverride(merchant: String) {
        val normalized = normalizeMerchant(merchant)
        if (normalized.isEmpty()) return
        dataStore.edit { it.remove(intPreferencesKey(keyFor(normalized))) }
    }

    private fun normalizeMerchant(merchant: String): String =
        merchant.trim().lowercase(Locale.ROOT)

    private fun keyFor(normalizedMerchant: String): String = "$KEY_PREFIX$normalizedMerchant"

    private companion object {
        const val KEY_PREFIX = "learning."
    }
}
