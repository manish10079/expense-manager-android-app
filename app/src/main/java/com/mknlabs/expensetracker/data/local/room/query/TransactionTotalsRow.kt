package com.mknlabs.expensetracker.data.local.room.query

import androidx.room.ColumnInfo

/**
 * Aggregate row for the Transactions screen: the income/expense totals and the
 * row count for the *whole* filtered result set, not just the pages Paging has
 * loaded.
 */
data class TransactionTotalsRow(
    @ColumnInfo(name = "income_minor")
    val incomeMinor: Long,
    @ColumnInfo(name = "expense_minor")
    val expenseMinor: Long,
    @ColumnInfo(name = "total_count")
    val totalCount: Int
)
