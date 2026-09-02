package com.mknlabs.expensetracker.data.local.room

import androidx.room.TypeConverter
import com.mknlabs.expensetracker.models.BudgetPeriod
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.SyncState

class RoomConverters {

    @TypeConverter
    fun fromSyncState(value: SyncState): String = value.name

    @TypeConverter
    fun toSyncState(value: String): SyncState {
        return SyncState.entries.firstOrNull { it.name == value } ?: SyncState.PENDING_UPLOAD
    }

    @TypeConverter
    fun fromRecurringFrequency(value: RecurringFrequency): String = value.name

    @TypeConverter
    fun toRecurringFrequency(value: String): RecurringFrequency {
        return RecurringFrequency.entries.firstOrNull { it.name == value } ?: RecurringFrequency.Monthly
    }

    @TypeConverter
    fun fromBudgetPeriod(value: BudgetPeriod): String = value.name

    @TypeConverter
    fun toBudgetPeriod(value: String): BudgetPeriod {
        return BudgetPeriod.entries.firstOrNull { it.name == value } ?: BudgetPeriod.MONTHLY
    }

    @TypeConverter
    fun fromIntList(list: List<Int>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toIntList(data: String): List<Int> {
        if (data.isBlank()) return emptyList()
        return data.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}
