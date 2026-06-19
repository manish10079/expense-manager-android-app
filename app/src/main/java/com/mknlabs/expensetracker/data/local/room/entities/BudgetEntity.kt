package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.SyncState

import com.google.firebase.firestore.PropertyName

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
    val id: String = "",
    @ColumnInfo(name = "category_id")
    val categoryId: Int = 0,
    @ColumnInfo(name = "month_start")
    val monthStart: Long = 0L,
    @ColumnInfo(name = "limit_minor")
    val limitMinor: Long = 0L,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.SYNCED,
    @ColumnInfo(name = "edit_count")
    val editCount: Int = 0,
    @get:PropertyName("isDeleted")
    @field:PropertyName("isDeleted")
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
