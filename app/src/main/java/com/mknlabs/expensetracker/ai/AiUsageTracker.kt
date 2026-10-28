package com.mknlabs.expensetracker.ai

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

val Context.aiUsageDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ai_usage_tracker"
)

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AiUsageDataStore

/**
 * Tracks daily AI parse usage for rate limiting.
 *
 * Guest and free: [FREE_OR_GUEST_DAILY_LIMIT] parses/day.
 * Pro: [PRO_DAILY_LIMIT] parses/day.
 *
 * Resets count automatically at midnight (local timezone).
 */
@Singleton
class AiUsageTracker @Inject constructor(
    @AiUsageDataStore private val dataStore: DataStore<Preferences>
) {
    val todayCount: Flow<Int> = dataStore.data.map { prefs ->
        val countDate = prefs[KEY_COUNT_DATE] ?: 0L
        if (isToday(countDate)) {
            prefs[KEY_COUNT] ?: 0
        } else {
            0
        }
    }

    fun dailyLimit(isPro: Boolean): Int =
        if (isPro) PRO_DAILY_LIMIT else FREE_OR_GUEST_DAILY_LIMIT

    fun hasRemainingParses(isPro: Boolean): Flow<Boolean> =
        todayCount.map { it < dailyLimit(isPro) }

    fun remainingParses(isPro: Boolean): Flow<Int> =
        todayCount.map { count -> (dailyLimit(isPro) - count).coerceAtLeast(0) }

    /**
     * Records a successful AI parse. Returns true if within limit, false if limit reached.
     */
    suspend fun recordUsage(isPro: Boolean): Boolean {
        val limit = dailyLimit(isPro)
        var withinLimit = false
        dataStore.edit { prefs ->
            val countDate = prefs[KEY_COUNT_DATE] ?: 0L
            val currentCount = if (isToday(countDate)) {
                prefs[KEY_COUNT] ?: 0
            } else {
                0
            }

            if (currentCount < limit) {
                prefs[KEY_COUNT] = currentCount + 1
                prefs[KEY_COUNT_DATE] = System.currentTimeMillis()
                withinLimit = true
            } else {
                withinLimit = false
            }
        }
        return withinLimit
    }

    suspend fun reset() {
        dataStore.edit { prefs ->
            prefs[KEY_COUNT] = 0
            prefs[KEY_COUNT_DATE] = 0L
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        if (timestamp == 0L) return false
        val now = System.currentTimeMillis()
        val todayStart = now - (now % (24 * 60 * 60 * 1000))
        return timestamp >= todayStart
    }

    companion object {
        const val FREE_OR_GUEST_DAILY_LIMIT = 4
        const val PRO_DAILY_LIMIT = 10
        private val KEY_COUNT = intPreferencesKey("ai_parse_count")
        private val KEY_COUNT_DATE = longPreferencesKey("ai_parse_count_date")
    }
}
