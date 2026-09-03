package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.SyncState

@Entity(
    tableName = "goal_fund_entries",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["goal_id", "funded_at"])
    ]
)
data class GoalFundEntryEntity(
    @PrimaryKey
    val id: String = "",
    @ColumnInfo(name = "goal_id")
    val goalId: String = "",
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long = 0L,
    @ColumnInfo(name = "note")
    val note: String = "",
    @ColumnInfo(name = "funded_at")
    val fundedAt: Long = 0L,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.SYNCED
)
