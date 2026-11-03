package com.mknlabs.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.permissionNudgeDataStore: DataStore<Preferences> by preferencesDataStore(name = "permission_nudge")

data class PermissionNudgeState(
    val notificationAskCount: Int = 0,
    val notificationLastAskAtMillis: Long = 0L,
    val smsAskCount: Int = 0,
    val smsLastAskAtMillis: Long = 0L,
    val lastCardAtMillis: Long = 0L,
    val nextCardIntervalDays: Int = 0,
)

object PermissionNudgeStore {

    private object Keys {
        val notificationAskCount = intPreferencesKey("notification_ask_count")
        val notificationLastAskAtMillis = longPreferencesKey("notification_last_ask_at")
        val smsAskCount = intPreferencesKey("sms_ask_count")
        val smsLastAskAtMillis = longPreferencesKey("sms_last_ask_at")
        val lastCardAtMillis = longPreferencesKey("permission_card_last_at")
        val nextCardIntervalDays = intPreferencesKey("permission_card_interval_days")
    }

    fun flow(context: Context): Flow<PermissionNudgeState> =
        context.permissionNudgeDataStore.data.map { prefs ->
            PermissionNudgeState(
                notificationAskCount = prefs[Keys.notificationAskCount] ?: 0,
                notificationLastAskAtMillis = prefs[Keys.notificationLastAskAtMillis] ?: 0L,
                smsAskCount = prefs[Keys.smsAskCount] ?: 0,
                smsLastAskAtMillis = prefs[Keys.smsLastAskAtMillis] ?: 0L,
                lastCardAtMillis = prefs[Keys.lastCardAtMillis] ?: 0L,
                nextCardIntervalDays = prefs[Keys.nextCardIntervalDays] ?: 0,
            )
        }

    suspend fun markNotificationAsked(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        context.permissionNudgeDataStore.edit { prefs ->
            val count = (prefs[Keys.notificationAskCount] ?: 0) + 1
            prefs[Keys.notificationAskCount] = count
            prefs[Keys.notificationLastAskAtMillis] = nowMillis
            ensureCardInterval(prefs)
        }
    }

    suspend fun markSmsAsked(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        context.permissionNudgeDataStore.edit { prefs ->
            val count = (prefs[Keys.smsAskCount] ?: 0) + 1
            prefs[Keys.smsAskCount] = count
            prefs[Keys.smsLastAskAtMillis] = nowMillis
            ensureCardInterval(prefs)
        }
    }

    suspend fun markCardShown(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        context.permissionNudgeDataStore.edit { prefs ->
            prefs[Keys.lastCardAtMillis] = nowMillis
            prefs[Keys.nextCardIntervalDays] = kotlin.random.Random.nextInt(
                com.mknlabs.expensetracker.utils.PermissionNudge.CARD_INTERVAL_MIN_DAYS,
                com.mknlabs.expensetracker.utils.PermissionNudge.CARD_INTERVAL_MAX_DAYS + 1,
            )
        }
    }

    private fun ensureCardInterval(prefs: androidx.datastore.preferences.core.MutablePreferences) {
        if ((prefs[Keys.nextCardIntervalDays] ?: 0) > 0) return
        prefs[Keys.nextCardIntervalDays] = kotlin.random.Random.nextInt(
            com.mknlabs.expensetracker.utils.PermissionNudge.CARD_INTERVAL_MIN_DAYS,
            com.mknlabs.expensetracker.utils.PermissionNudge.CARD_INTERVAL_MAX_DAYS + 1,
        )
    }
}
