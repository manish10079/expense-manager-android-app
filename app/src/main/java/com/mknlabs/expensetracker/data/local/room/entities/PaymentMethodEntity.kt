package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.SyncState

@Entity(
    tableName = "payment_methods",
    indices = [
        Index(value = ["sort_order", "name"]),
        Index(value = ["name", "is_deleted"], unique = false)
    ]
)
data class PaymentMethodEntity(
    @PrimaryKey
    val id: Int = 0,
    val name: String = "",
    @ColumnInfo(name = "icon_key")
    val iconKey: String = "",
    @ColumnInfo(name = "is_system")
    val isSystem: Boolean = false,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.SYNCED,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L
)
