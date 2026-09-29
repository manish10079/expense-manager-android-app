package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.SyncState

import com.google.firebase.firestore.PropertyName

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["transaction_type_id", "sort_order", "name"]),
        Index(value = ["name", "transaction_type_id", "is_deleted"], unique = false)
    ]
)
data class CategoryEntity(
    @PrimaryKey
    val id: Int = 0,
    val name: String = "",
    @ColumnInfo(name = "transaction_type_id")
    val transactionTypeId: Int = 0,
    @ColumnInfo(name = "icon_key")
    val iconKey: String = "",
    /**
     * The user's colour for this category, as `#RRGGBB`, or null to take the palette colour
     * for [id] instead — see
     * [categoryColor][com.mknlabs.expensetracker.core.ui.theme.categoryColor].
     *
     * NULL rather than "" on purpose: an empty string cannot be told apart from a truncated
     * write, so `null` is what makes "this row has no override" a fact rather than a guess.
     * Every seeded row stays null forever — only a category the user made and coloured stores
     * a value — which is what lets
     * [ExpenseTrackerDatabaseInitializer][com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabaseInitializer]
     * rewrite the seeded rows on every launch without destroying anything.
     *
     * Alpha is not stored. A pick is normalised to six digits on write, and the transparency
     * a surface needs is applied at draw time.
     */
    @ColumnInfo(name = "color_hex")
    val colorHex: String? = null,
    @get:PropertyName("isSystem")
    @field:PropertyName("isSystem")
    @ColumnInfo(name = "is_system")
    val isSystem: Boolean = false,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    @get:PropertyName("isDeleted")
    @field:PropertyName("isDeleted")
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.SYNCED,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L
)
