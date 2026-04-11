package com.mkn0079.expensetracker.data.local.room

import androidx.room.TypeConverter
import com.mkn0079.expensetracker.models.RecurringFrequency
import com.mkn0079.expensetracker.models.SyncState

class RoomConverters {

    @TypeConverter
    fun fromSyncState(value: SyncState): String = value.name

    @TypeConverter
    fun toSyncState(value: String): SyncState {
        return SyncState.entries.firstOrNull { it.name == value } ?: SyncState.LOCAL_ONLY
    }

    @TypeConverter
    fun fromRecurringFrequency(value: RecurringFrequency): String = value.name

    @TypeConverter
    fun toRecurringFrequency(value: String): RecurringFrequency {
        return RecurringFrequency.entries.firstOrNull { it.name == value } ?: RecurringFrequency.Monthly
    }
}
