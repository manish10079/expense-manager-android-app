package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.FavoriteTransaction

@Entity(
    tableName = "favorite_transactions",
    indices = [
        Index(value = ["is_pinned", "title"])
    ]
)
data class FavoriteTransactionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long,
    @ColumnInfo(name = "transaction_type_id")
    val transactionTypeId: Int = 2,
    @ColumnInfo(name = "category_id")
    val categoryId: Int = 0,
    @ColumnInfo(name = "payment_type_id")
    val paymentTypeId: Int = 0,
    val note: String = "",
    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): FavoriteTransaction = FavoriteTransaction(
        id = id,
        title = title,
        amountMinor = amountMinor,
        transactionTypeId = transactionTypeId,
        categoryId = categoryId,
        paymentTypeId = paymentTypeId,
        note = note,
        isPinned = isPinned,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: FavoriteTransaction): FavoriteTransactionEntity = FavoriteTransactionEntity(
            id = domain.id,
            title = domain.title,
            amountMinor = domain.amountMinor,
            transactionTypeId = domain.transactionTypeId,
            categoryId = domain.categoryId,
            paymentTypeId = domain.paymentTypeId,
            note = domain.note,
            isPinned = domain.isPinned,
            createdAt = domain.createdAt
        )
    }
}
