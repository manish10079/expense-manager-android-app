package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.InstallmentOccurrenceStatus
import com.mknlabs.expensetracker.models.SyncState

/**
 * One scheduled installment of a loan/EMI plan.
 *
 * This table is the *schedule* (the intent to pay on a date); `transactions` stay
 * the immutable *history* (the fact that a payment happened). The two are linked
 * by [transactionId] once an installment is settled, which is what lets an
 * installment be unpaid, skipped, or paid late — none of which the old model
 * could express, because there the generated transaction WAS the payment.
 *
 * [id] is deterministic (`"{ruleId}_occ_{installmentIndex}"`), so the worker can
 * re-derive any slot without ever inserting a duplicate — the same idempotency
 * strategy already used for auto-generated recurring transactions.
 *
 * Rows are soft-deleted ([isDeleted]) rather than dropped when a plan is
 * converted back to REGULAR, so the conversion stays reversible and no history
 * is lost.
 */
@Entity(
    tableName = "installment_occurrences",
    foreignKeys = [
        ForeignKey(
            entity = RecurringRuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["rule_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["rule_id", "due_at"]),
        Index(value = ["rule_id", "status"]),
        Index(value = ["transaction_id"])
    ]
)
data class InstallmentOccurrenceEntity(
    @PrimaryKey
    val id: String = "",
    @ColumnInfo(name = "rule_id")
    val ruleId: String = "",
    /** 1-based position of this installment within the plan. */
    @ColumnInfo(name = "installment_index")
    val installmentIndex: Int = 0,
    @ColumnInfo(name = "due_at")
    val dueAt: Long = 0L,
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long = 0L,
    /** When the installment was actually paid; null while pending or skipped. */
    @ColumnInfo(name = "paid_at")
    val paidAt: Long? = null,
    @ColumnInfo(name = "status")
    val status: InstallmentOccurrenceStatus = InstallmentOccurrenceStatus.PENDING,
    /** The transaction that settled this installment, when one exists. */
    @ColumnInfo(name = "transaction_id")
    val transactionId: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.PENDING_UPLOAD,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
