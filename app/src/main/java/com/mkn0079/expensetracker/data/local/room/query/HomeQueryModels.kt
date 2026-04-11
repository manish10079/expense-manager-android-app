package com.mkn0079.expensetracker.data.local.room.query

import androidx.room.ColumnInfo
import com.mkn0079.expensetracker.models.SyncState

data class HomeSummaryRow(
    @ColumnInfo(name = "income_minor")
    val incomeMinor: Long,
    @ColumnInfo(name = "expense_minor")
    val expenseMinor: Long,
    @ColumnInfo(name = "highlighted_expense_minor")
    val highlightedExpenseMinor: Long
)

data class HomeRecentTransactionRow(
    val id: String,
    val note: String,
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long,
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "transaction_type_id")
    val transactionTypeId: Int,
    @ColumnInfo(name = "category_id")
    val categoryId: Int,
    @ColumnInfo(name = "payment_method_id")
    val paymentMethodId: Int,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState,
    @ColumnInfo(name = "content_hash")
    val contentHash: String?,
    @ColumnInfo(name = "source_recurring_rule_id")
    val sourceRecurringRuleId: String?,
    @ColumnInfo(name = "payment_method_name")
    val paymentMethodName: String
)
