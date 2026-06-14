package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.SyncState

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "target_amount_minor")
    val targetAmountMinor: Long,
    @ColumnInfo(name = "current_amount_minor")
    val currentAmountMinor: Long,
    @ColumnInfo(name = "deadline_at")
    val deadlineAt: Long?,
    @ColumnInfo(name = "icon_key")
    val iconKey: String,
    @ColumnInfo(name = "color_hex")
    val colorHex: String,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState
)
