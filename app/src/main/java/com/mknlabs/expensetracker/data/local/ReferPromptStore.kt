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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.referPromptDataStore: DataStore<Preferences> by preferencesDataStore(name = "refer_prompt")

object ReferPromptStore {

    private val lastShownAtMillis = longPreferencesKey("last_shown_at_millis")
    private val successCount = intPreferencesKey("success_count")
    private val pendingQualify = booleanPreferencesKey("pending_qualify")
    private val lastCelebratedSuccessCount = intPreferencesKey("last_celebrated_success_count")
    private const val INTERVAL_MS = 7L * 24 * 60 * 60 * 1000

    data class State(val lastShownAtMillis: Long = 0L, val successCount: Int = 0)

    fun flow(context: Context): Flow<State> =
        context.referPromptDataStore.data.map {
            State(
                lastShownAtMillis = it[lastShownAtMillis] ?: 0L,
                successCount = it[successCount] ?: 0
            )
        }

    fun shouldShow(lastShownAtMillis: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (lastShownAtMillis <= 0L) return true
        return nowMillis - lastShownAtMillis >= INTERVAL_MS
    }

    suspend fun markShown(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        context.referPromptDataStore.edit { it[lastShownAtMillis] = nowMillis }
    }

    suspend fun setSuccessCount(context: Context, count: Int) {
        context.referPromptDataStore.edit { it[successCount] = count }
    }

    suspend fun isPendingQualify(context: Context): Boolean =
        context.referPromptDataStore.data.map { it[pendingQualify] ?: false }.first()

    suspend fun setPendingQualify(context: Context, pending: Boolean) {
        context.referPromptDataStore.edit { it[pendingQualify] = pending }
    }

    suspend fun lastCelebratedSuccessCount(context: Context): Int =
        context.referPromptDataStore.data.map { it[lastCelebratedSuccessCount] ?: 0 }.first()

    suspend fun setLastCelebratedSuccessCount(context: Context, count: Int) {
        context.referPromptDataStore.edit { it[lastCelebratedSuccessCount] = count }
    }
}
