package com.mknlabs.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mknlabs.expensetracker.utils.SecureValueCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.monetizationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "monetization_settings"
)

/**
 * Security plan Item 5: the ad-pass expiry is stored encrypted at rest via
 * [SecureValueCipher]. The legacy plaintext long key is still read as a
 * fallback (migration); new writes go to the encrypted string key.
 */
object MonetizationDataStore {

    private object Keys {
        // The store's own verdict on the `premium` entitlement, latched by BillingManager
        // whenever a CustomerInfo snapshot arrives. Plain rather than encrypted: it states
        // no user data, only which entitlement the store currently reports.
        val premiumEntitlementActive = booleanPreferencesKey("premium_entitlement_active")
        // One-shot claim: has cloud sync already been switched on automatically for this
        // install's first store entitlement? Released by nothing — see claimCloudSyncAutoEnable.
        val cloudSyncAutoEnabled = booleanPreferencesKey("cloud_sync_auto_enabled")
        // Legacy (plaintext) long key — still read until migrated.
        val globalAdAccessExpiry = longPreferencesKey("global_ad_access_expiry")
        // Encrypted replacement (string key, AES-GCM envelope).
        val globalAdAccessExpiryEnc = stringPreferencesKey("global_ad_access_expiry_enc")
    }

    fun getGlobalAdAccessExpiry(context: Context): Flow<Long> {
        val appContext = context.applicationContext
        return appContext.monetizationDataStore.data
            .map { preferences ->
                val enc = preferences[Keys.globalAdAccessExpiryEnc]
                if (enc != null) {
                    SecureValueCipher.decryptOrNull(enc)?.toLongOrNull()
                        ?: preferences[Keys.globalAdAccessExpiry]
                        ?: 0L
                } else {
                    preferences[Keys.globalAdAccessExpiry] ?: 0L
                }
            }
    }

    suspend fun updateGlobalAdAccessExpiry(context: Context, expiryMillis: Long) {
        val appContext = context.applicationContext
        appContext.monetizationDataStore.edit { preferences ->
            preferences[Keys.globalAdAccessExpiryEnc] =
                SecureValueCipher.encrypt(expiryMillis.toString())
            preferences.remove(Keys.globalAdAccessExpiry)
        }
    }

    /**
     * The store's last-known verdict on the `premium` entitlement.
     *
     * Persisted rather than read live because [AppSettingsDataStore] needs an answer on
     * every settings write and has no BillingRepository to ask. It exists because a store
     * subscription never writes `accountTier = "PREMIUM"` locally — only the `redeemProPass`
     * Cloud Function does — so the profile alone reads a paying subscriber as free.
     *
     * `false` only until the first CustomerInfo snapshot ever arrives (a fresh install has
     * no entitlement); after that the last verdict survives the cold-start window before
     * RevenueCat answers, so a restart cannot re-derive a subscriber as free.
     */
    fun getPremiumEntitlementActive(context: Context): Flow<Boolean> {
        val appContext = context.applicationContext
        return appContext.monetizationDataStore.data
            .map { preferences -> preferences[Keys.premiumEntitlementActive] ?: false }
    }

    /** Latches the store's verdict. Callers must pass a real snapshot, never "unknown". */
    suspend fun setPremiumEntitlementActive(context: Context, active: Boolean) {
        val appContext = context.applicationContext
        appContext.monetizationDataStore.edit { preferences ->
            preferences[Keys.premiumEntitlementActive] = active
        }
    }

    /**
     * Claims the one automatic switch-on of cloud sync, and answers whether this call won it.
     *
     * Called only when the store reports Pro, and returns true exactly once per install: the
     * first subscriber activation gets sync turned on for them, and every later one is refused.
     *
     * The claim is deliberately never released. An inactive -> active transition happens again
     * on a renewal, a re-login and a restored entitlement, so gating on the transition alone
     * would switch sync back on for someone who has since turned it off. Latching instead makes
     * "never re-enable after the user opted out" a property of the data rather than a hope
     * about how often RevenueCat's verdict flips.
     */
    suspend fun claimCloudSyncAutoEnable(context: Context): Boolean {
        val appContext = context.applicationContext
        var claimed = false
        appContext.monetizationDataStore.edit { preferences ->
            if (preferences[Keys.cloudSyncAutoEnabled] != true) {
                preferences[Keys.cloudSyncAutoEnabled] = true
                claimed = true
            }
        }
        return claimed
    }
}
