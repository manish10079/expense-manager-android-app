package com.mknlabs.expensetracker.data.local.room.query

import androidx.room.ColumnInfo

/**
 * One tag's usage, as the Tag Management screen shows it: how many live transactions
 * carry it and how much expense those add up to.
 *
 * `expenseMinor` counts only expense-type transactions (`transaction_type_id != 1`),
 * matching the app's convention everywhere else — income tagged with a trip is not
 * "spent". Income rows still count toward `transactionCount`, because "how many
 * transactions use this tag" is a question about usage, not about money.
 *
 * The join to `transactions` is an inner join on `is_deleted = 0`, so a tag's
 * statistics never include a transaction the user deleted. A tag with no live
 * transactions still comes back, via the `LEFT JOIN`, with both values zero.
 */
data class TagStatsRow(
    @ColumnInfo(name = "tag_id")
    val tagId: String,
    @ColumnInfo(name = "transaction_count")
    val transactionCount: Int,
    @ColumnInfo(name = "expense_minor")
    val expenseMinor: Long
)

/**
 * One transaction's tag, denormalised for the ledger: the join row flattened with the
 * tag's name and colour so a list of transactions can be tagged in one query instead
 * of one query per row.
 *
 * Soft-deleted tags are excluded by the DAO's query, so a transaction whose tag was
 * deleted renders without it even though its join row is still there.
 */
data class TransactionTagRow(
    @ColumnInfo(name = "transaction_id")
    val transactionId: String,
    @ColumnInfo(name = "tag_id")
    val tagId: String,
    val name: String,
    @ColumnInfo(name = "color_hex")
    val colorHex: String?
)