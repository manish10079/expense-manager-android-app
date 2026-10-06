package com.mknlabs.expensetracker.data.local.room.query

import androidx.room.ColumnInfo

/**
 * One fund's total spending, summed in SQL across the transactions linked to it.
 *
 * A projection rather than a column on `funds`: the balance is derived on read, so no
 * device can hold a stale copy of it.
 */
data class FundSpendRow(
    @ColumnInfo(name = "fund_id")
    val fundId: String,
    @ColumnInfo(name = "spent_minor")
    val spentMinor: Long
)
