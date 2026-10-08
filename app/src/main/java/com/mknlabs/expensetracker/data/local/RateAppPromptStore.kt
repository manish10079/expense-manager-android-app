package com.mknlabs.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.rateAppPromptDataStore: DataStore<Preferences> by preferencesDataStore(name = "rate_app_prompt")

data class RateAppPromptState(
    val firstOpenAtMillis: Long = 0L,
    val completed: Boolean = false,
    val dismissCount: Int = 0,
    val lastDismissAtMillis: Long = 0L
)

object RateAppPromptStore {

    private object Keys {
        val firstOpenAtMillis = longPreferencesKey("first_open_at_millis")
        val completed = booleanPreferencesKey("completed")
        val dismissCount = intPreferencesKey("dismiss_count")
        val lastDismissAtMillis = longPreferencesKey("last_dismiss_at_millis")
    }

    fun flow(context: Context): Flow<RateAppPromptState> =
        context.rateAppPromptDataStore.data.map { prefs ->
            RateAppPromptState(
                firstOpenAtMillis = prefs[Keys.firstOpenAtMillis] ?: 0L,
                completed = prefs[Keys.completed] ?: false,
                dismissCount = prefs[Keys.dismissCount] ?: 0,
                lastDismissAtMillis = prefs[Keys.lastDismissAtMillis] ?: 0L
            )
        }

    suspend fun ensureFirstOpen(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        context.rateAppPromptDataStore.edit { prefs ->
            if ((prefs[Keys.firstOpenAtMillis] ?: 0L) <= 0L) {
                prefs[Keys.firstOpenAtMillis] = nowMillis
            }
        }
    }

    suspend fun markDismissed(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        context.rateAppPromptDataStore.edit { prefs ->
            prefs[Keys.dismissCount] = (prefs[Keys.dismissCount] ?: 0) + 1
            prefs[Keys.lastDismissAtMillis] = nowMillis
        }
    }

    suspend fun markCompleted(context: Context) {
        context.rateAppPromptDataStore.edit { prefs ->
            prefs[Keys.completed] = true
        }
    }
}
