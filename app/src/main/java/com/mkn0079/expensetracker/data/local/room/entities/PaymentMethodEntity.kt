package com.mkn0079.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mkn0079.expensetracker.models.SyncState

@Entity(
    tableName = "payment_methods",
    indices = [
        Index(value = ["sort_order", "name"]),
        Index(value = ["name", "is_deleted"], unique = false)
    ]
)
data class PaymentMethodEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    @ColumnInfo(name = "icon_key")
    val iconKey: String,
    @ColumnInfo(name = "is_system")
    val isSystem: Boolean,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
