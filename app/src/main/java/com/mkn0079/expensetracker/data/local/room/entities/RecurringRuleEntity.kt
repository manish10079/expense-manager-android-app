package com.mkn0079.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mkn0079.expensetracker.models.RecurringFrequency
import com.mkn0079.expensetracker.models.SyncState

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
        Index(value = ["transaction_id", "is_deleted"], unique = true)
    ]
)
data class RecurringRuleEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "transaction_id")
    val transactionId: String,
    val frequency: RecurringFrequency,
    @ColumnInfo(name = "interval_count")
    val intervalCount: Int,
    @ColumnInfo(name = "repeat_count")
    val repeatCount: Int,
    @ColumnInfo(name = "remaining_count")
    val remainingCount: Int?,
    @ColumnInfo(name = "anchor_at")
    val anchorAt: Long,
    @ColumnInfo(name = "next_run_at")
    val nextRunAt: Long,
    @ColumnInfo(name = "last_run_at")
    val lastRunAt: Long?,
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean
)
