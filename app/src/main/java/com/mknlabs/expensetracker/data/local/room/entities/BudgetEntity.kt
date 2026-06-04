package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.SyncState

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["month_start"]),
        Index(value = ["category_id", "month_start"]),
        Index(value = ["category_id", "month_start", "is_deleted"], unique = false)
    ]
)
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "category_id")
    val categoryId: Int,
    @ColumnInfo(name = "month_start")
    val monthStart: Long,
    @ColumnInfo(name = "limit_minor")
    val limitMinor: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState,
    @ColumnInfo(name = "edit_count")
    val editCount: Int = 0,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean
)
