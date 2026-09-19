package com.mknlabs.expensetracker.data.local.room

import androidx.room.TypeConverter
import com.mknlabs.expensetracker.models.BudgetPeriod
import com.mknlabs.expensetracker.models.InstallmentOccurrenceStatus
import com.mknlabs.expensetracker.models.InstallmentStatus
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringType
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsDetectionSource
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxStatus

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

    /**
     * Recurring rule kind. The fallback is deliberately [RecurringType.REGULAR]:
     * every row written before installments existed has no value, and a value
     * pushed by a newer client must never be mistaken for a loan. Falling back to
     * REGULAR degrades to exactly the pre-installment behavior.
     */
    @TypeConverter
    fun fromRecurringType(value: RecurringType): String = value.name

    @TypeConverter
    fun toRecurringType(value: String): RecurringType {
        return RecurringType.entries.firstOrNull { it.name == value } ?: RecurringType.REGULAR
    }

    /**
     * Plan lifecycle. Nullable on purpose — a REGULAR rule has no plan at all, so
     * the column and this converter both pass null through rather than inventing
     * a status for a rule that has no installments.
     */
    @TypeConverter
    fun fromInstallmentStatus(value: InstallmentStatus?): String? = value?.name

    @TypeConverter
    fun toInstallmentStatus(value: String?): InstallmentStatus? =
        value?.let { raw -> InstallmentStatus.entries.firstOrNull { it.name == raw } }

    /**
     * Occurrence state. OVERDUE is a presentation-only derivation and is never
     * persisted, but parsing it is harmless if a future client writes it;
     * anything unrecognised falls back to PENDING rather than losing the slot.
     */
    @TypeConverter
    fun fromInstallmentOccurrenceStatus(value: InstallmentOccurrenceStatus): String = value.name

    @TypeConverter
    fun toInstallmentOccurrenceStatus(value: String): InstallmentOccurrenceStatus {
        return InstallmentOccurrenceStatus.entries.firstOrNull { it.name == value }
            ?: InstallmentOccurrenceStatus.PENDING
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

    /**
     * Detection inbox status. An unrecognised value falls back to NEW rather than
     * throwing: losing an inbox row because a future client wrote a status this build
     * does not know would defeat the point of storing detections at all.
     */
    @TypeConverter
    fun fromSmsInboxStatus(value: SmsInboxStatus): String = value.name

    @TypeConverter
    fun toSmsInboxStatus(value: String): SmsInboxStatus =
        SmsInboxStatus.entries.firstOrNull { it.name == value } ?: SmsInboxStatus.NEW

    /**
     * How a detection entered the inbox. Falls back to SMS — the overwhelmingly
     * common origin, and the one whose behaviour is most conservative.
     */
    @TypeConverter
    fun fromSmsDetectionSource(value: SmsDetectionSource): String = value.name

    @TypeConverter
    fun toSmsDetectionSource(value: String): SmsDetectionSource =
        SmsDetectionSource.entries.firstOrNull { it.name == value } ?: SmsDetectionSource.SMS
}
