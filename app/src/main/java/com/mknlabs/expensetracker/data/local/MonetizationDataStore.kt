package com.mknlabs.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
}
