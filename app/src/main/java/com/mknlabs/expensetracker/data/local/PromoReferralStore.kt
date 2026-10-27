package com.mknlabs.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.promoReferralDataStore: DataStore<Preferences> by preferencesDataStore(name = "promo_referral")

object PromoReferralStore {

    private object Keys {
        val pendingPromoCode = stringPreferencesKey("pending_promo_code")
        val installReferrerCaptured = booleanPreferencesKey("install_referrer_captured")
    }

    fun pendingCode(context: Context): Flow<String> =
        context.promoReferralDataStore.data.map { it[Keys.pendingPromoCode].orEmpty() }

    suspend fun pendingCodeOnce(context: Context): String =
        pendingCode(context).first()

    suspend fun savePendingCode(context: Context, code: String) {
        if (code.isBlank()) return
        context.promoReferralDataStore.edit { it[Keys.pendingPromoCode] = code }
    }

    suspend fun clearPendingCode(context: Context) {
        context.promoReferralDataStore.edit { it.remove(Keys.pendingPromoCode) }
    }

    suspend fun wasInstallReferrerCaptured(context: Context): Boolean =
        context.promoReferralDataStore.data.first()[Keys.installReferrerCaptured] == true

    suspend fun markInstallReferrerCaptured(context: Context) {
        context.promoReferralDataStore.edit { it[Keys.installReferrerCaptured] = true }
    }
}
