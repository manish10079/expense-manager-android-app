package com.mkn0079.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["transaction_type_id", "sort_order", "name"]),
        Index(value = ["name", "transaction_type_id", "is_deleted"], unique = false)
    ]
)
data class CategoryEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    @ColumnInfo(name = "transaction_type_id")
    val transactionTypeId: Int,
    @ColumnInfo(name = "icon_key")
    val iconKey: String,
    @ColumnInfo(name = "is_system")
    val isSystem: Boolean,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
