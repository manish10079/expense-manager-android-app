package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.SyncState

import com.google.firebase.firestore.PropertyName

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PaymentMethodEntity::class,
            parentColumns = ["id"],
            childColumns = ["payment_method_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = RecurringRuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["source_recurring_rule_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["occurred_at"]),
        Index(value = ["transaction_type_id", "occurred_at"]),
        Index(value = ["category_id", "occurred_at"]),
        Index(value = ["payment_method_id", "occurred_at"]),
        Index(value = ["is_deleted", "occurred_at"]),
        Index(value = ["source_recurring_rule_id"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String = "",
    val note: String = "",
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long = 0L,
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long = 0L,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L,
    @ColumnInfo(name = "transaction_type_id")
    val transactionTypeId: Int = 0,
    @ColumnInfo(name = "category_id")
    val categoryId: Int = 0,
    @ColumnInfo(name = "payment_method_id")
    val paymentMethodId: Int = 0,
    @get:PropertyName("isDeleted")
    @field:PropertyName("isDeleted")
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.SYNCED,
    @ColumnInfo(name = "content_hash")
    val contentHash: String? = null,
    @ColumnInfo(name = "source_recurring_rule_id")
    val sourceRecurringRuleId: String? = null
)
