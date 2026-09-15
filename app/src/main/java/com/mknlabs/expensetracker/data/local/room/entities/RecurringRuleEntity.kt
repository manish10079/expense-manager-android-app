package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.InstallmentStatus
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringType
import com.mknlabs.expensetracker.models.SyncState

import com.google.firebase.firestore.PropertyName

@Entity(
    tableName = "recurring_rules",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["is_enabled", "next_run_at"]),
        Index(value = ["transaction_id", "is_deleted"], unique = false)
    ]
)
data class RecurringRuleEntity(
    @PrimaryKey
    val id: String = "",
    @ColumnInfo(name = "transaction_id")
    val transactionId: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.Monthly,
    @ColumnInfo(name = "interval_count")
    val intervalCount: Int = 1,
    @ColumnInfo(name = "repeat_count")
    val repeatCount: Int = 0,
    @ColumnInfo(name = "remaining_count")
    val remainingCount: Int? = null,
    @ColumnInfo(name = "anchor_at")
    val anchorAt: Long = 0L,
    @ColumnInfo(name = "next_run_at")
    val nextRunAt: Long = 0L,
    @ColumnInfo(name = "last_run_at")
    val lastRunAt: Long? = null,
    @ColumnInfo(name = "last_notified_occurrence_at")
    val lastNotifiedOccurrenceAt: Long? = null,
    @get:PropertyName("isEnabled")
    @field:PropertyName("isEnabled")
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,
    @get:PropertyName("notificationsEnabled")
    @field:PropertyName("notificationsEnabled")
    @ColumnInfo(name = "notifications_enabled")
    val notificationsEnabled: Boolean = true,
    // Which advance-alert window (7 / 3 / 1 / 0 days-before) already fired for
    // the CURRENT occurrence; null = nothing notified yet. Reset whenever the
    // occurrence advances so the next cycle alerts again.
    @get:PropertyName("lastNotifiedWindowDays")
    @field:PropertyName("lastNotifiedWindowDays")
    @ColumnInfo(name = "last_notified_window_days")
    val lastNotifiedWindowDays: Int? = null,
    // ── Installment (EMI) plan terms ────────────────────────────────────────
    // All defaulted/nullable, so every row that predates installments remains a
    // valid REGULAR rule and no existing rule changes behavior. Progress
    // (paid count / remaining balance) is deliberately NOT stored here — it is
    // counted from installment_occurrences so the plan and its progress cannot
    // drift apart.
    @get:PropertyName("recurringType")
    @field:PropertyName("recurringType")
    @ColumnInfo(name = "recurring_type")
    val recurringType: RecurringType = RecurringType.REGULAR,
    /** Principal + interest being paid off. Null for REGULAR rules. */
    @ColumnInfo(name = "installment_total_minor")
    val installmentTotalMinor: Long? = null,
    /** Amount due per installment. Null for REGULAR rules. */
    @ColumnInfo(name = "installment_amount_minor")
    val installmentAmountMinor: Long? = null,
    /** Number of installments the plan is split into. Null for REGULAR rules. */
    @ColumnInfo(name = "installment_total_count")
    val installmentTotalCount: Int? = null,
    @get:PropertyName("installmentStatus")
    @field:PropertyName("installmentStatus")
    @ColumnInfo(name = "installment_status")
    val installmentStatus: InstallmentStatus? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.SYNCED,
    @get:PropertyName("isDeleted")
    @field:PropertyName("isDeleted")
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
