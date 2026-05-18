package com.mkn0079.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.monetizationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "monetization_settings"
)

object MonetizationDataStore {

    private object Keys {
        val globalAdAccessExpiry = longPreferencesKey("global_ad_access_expiry")
    }

    fun getGlobalAdAccessExpiry(context: Context): Flow<Long> {
        return context.applicationContext.monetizationDataStore.data
            .map { preferences -> preferences[Keys.globalAdAccessExpiry] ?: 0L }
    }

    suspend fun updateGlobalAdAccessExpiry(context: Context, expiryMillis: Long) {
        context.applicationContext.monetizationDataStore.edit { preferences ->
            preferences[Keys.globalAdAccessExpiry] = expiryMillis
        }
    }
}
