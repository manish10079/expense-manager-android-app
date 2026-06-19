package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.RecurringFrequency
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
