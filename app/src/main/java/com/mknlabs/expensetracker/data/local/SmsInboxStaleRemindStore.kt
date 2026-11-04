package com.mknlabs.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.smsInboxStaleRemindDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sms_inbox_stale_remind",
)

object SmsInboxStaleRemindStore {

    private val scheduledShowAt = longPreferencesKey("scheduled_show_at")
    private val dismissedDayStart = longPreferencesKey("dismissed_day_start")

    data class State(
        val scheduledShowAt: Long = 0L,
        val dismissedDayStart: Long = 0L,
    )

    fun flow(context: Context): Flow<State> =
        context.smsInboxStaleRemindDataStore.data.map {
            State(
                scheduledShowAt = it[scheduledShowAt] ?: 0L,
                dismissedDayStart = it[dismissedDayStart] ?: 0L,
            )
        }

    suspend fun setScheduledShowAt(context: Context, atMillis: Long) {
        context.smsInboxStaleRemindDataStore.edit { it[scheduledShowAt] = atMillis }
    }

    suspend fun markHandledToday(context: Context, dayStartMillis: Long) {
        context.smsInboxStaleRemindDataStore.edit {
            it[dismissedDayStart] = dayStartMillis
            it[scheduledShowAt] = 0L
        }
    }
}
