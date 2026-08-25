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
 * Tracks daily AI voice parse usage for rate limiting.
 *
 * Free tier: 10 parses/day (configurable via [dailyLimit]).
 * Pro tier: unlimited (check [UserTier] before calling [recordUsage]).
 *
 * Resets count automatically at midnight (local timezone).
 * Stored in DataStore — no Room migration needed.
 */
@Singleton
class AiUsageTracker @Inject constructor(
    @AiUsageDataStore private val dataStore: DataStore<Preferences>
) {
    /** Default daily limit for free users. */
    val dailyLimit: Int = DEFAULT_DAILY_LIMIT

    /** Current parse count for today. */
    val todayCount: Flow<Int> = dataStore.data.map { prefs ->
        val countDate = prefs[KEY_COUNT_DATE] ?: 0L
        if (isToday(countDate)) {
            prefs[KEY_COUNT] ?: 0
        } else {
            0 // New day, count resets
        }
    }

    /** Whether the user has remaining parses today. */
    val hasRemainingParses: Flow<Boolean> = todayCount.map { it < dailyLimit }

    /** Remaining parses today. */
    val remainingParses: Flow<Int> = todayCount.map { count ->
        (dailyLimit - count).coerceAtLeast(0)
    }

    /**
     * Records a successful AI parse. Returns true if within limit, false if limit reached.
     * The caller should check [hasRemainingParses] BEFORE calling this.
     */
    suspend fun recordUsage(): Boolean {
        var withinLimit = false
        dataStore.edit { prefs ->
            val countDate = prefs[KEY_COUNT_DATE] ?: 0L
            val currentCount = if (isToday(countDate)) {
                prefs[KEY_COUNT] ?: 0
            } else {
                0 // New day
            }

            if (currentCount < dailyLimit) {
                prefs[KEY_COUNT] = currentCount + 1
                prefs[KEY_COUNT_DATE] = System.currentTimeMillis()
                withinLimit = true
            } else {
                withinLimit = false
            }
        }
        return withinLimit
    }

    /**
     * Resets the daily counter (e.g., for testing or manual reset).
     */
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

    private companion object {
        const val DEFAULT_DAILY_LIMIT = 10
        val KEY_COUNT = intPreferencesKey("ai_parse_count")
        val KEY_COUNT_DATE = longPreferencesKey("ai_parse_count_date")
    }
}
