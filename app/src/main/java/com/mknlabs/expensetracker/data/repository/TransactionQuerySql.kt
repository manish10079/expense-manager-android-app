package com.mknlabs.expensetracker.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.mknlabs.expensetracker.domain.repository.TransactionQuery
import com.mknlabs.expensetracker.models.SortType
import com.mknlabs.expensetracker.models.TagMatchMode
import java.util.Locale

/** A SQL statement plus its ordered bind arguments, before Room sees it. */
data class TransactionSqlQuery(
    val sql: String,
    val args: List<Any>
)

/**
 * Builds the SQLite statements behind the Transactions screen from a
 * [TransactionQuery].
 *
 * The paging, id and totals statements all share [selection], which is what keeps
 * the list, the "select all in view" ids and the summary totals from ever
 * disagreeing about which rows they cover.
 *
 * Deliberately free of Room and Android types so the generated SQL and its bind
 * arguments can be unit tested without a database.
 */
object TransactionQuerySql {

    /**
     * Shared `WHERE` clause and its bind args.
     *
     * Every user-supplied value is bound (`?`), never interpolated; the only
     * interpolated fragments are `?` placeholder runs, whose length comes from the
     * size of an id list rather than its contents.
     */
    fun selection(query: TransactionQuery): Pair<String, List<Any>> {
        val selection = StringBuilder("is_deleted = 0")
        val args = mutableListOf<Any>()

        if (query.startMillis != TransactionQuery.NO_START) {
            selection.append(" AND occurred_at >= ?")
            args += query.startMillis
        }
        if (query.endMillis != TransactionQuery.NO_END) {
            selection.append(" AND occurred_at < ?")
            args += query.endMillis
        }

        val search = query.search?.trim()?.takeIf { it.isNotEmpty() }
        if (search != null) {
            selection.append(" AND (")
            selection.append("LOWER(note) LIKE ? OR CAST(amount_minor AS TEXT) LIKE ?")
            args += "%${search.lowercase(Locale.getDefault())}%"
            args += "%$search%"
            if (query.searchCategoryIds.isNotEmpty()) {
                selection.append(" OR category_id IN (${query.searchCategoryIds.placeholders()})")
                args.addAll(query.searchCategoryIds)
            }
            if (query.searchPaymentTypeIds.isNotEmpty()) {
                selection.append(" OR payment_method_id IN (${query.searchPaymentTypeIds.placeholders()})")
                args.addAll(query.searchPaymentTypeIds)
            }
            if (query.searchTagIds.isNotEmpty()) {
                // Tag names live in the tags table, so a name match is resolved to tag ids
                // upstream and matched here through the join table. Soft-deleted tags are
                // excluded; the surrounding `is_deleted = 0` already guards the transaction.
                selection.append(
                    " OR id IN (SELECT tt.transaction_id FROM transaction_tags tt " +
                        "JOIN tags ON tags.id = tt.tag_id " +
                        "WHERE tags.is_deleted = 0 AND tt.tag_id IN (${query.searchTagIds.tagPlaceholders()}))"
                )
                args.addAll(query.searchTagIds)
            }
            selection.append(")")
        }

        if (query.transactionTypeIds.isEmpty()) {
            // The user deselected every transaction type. That must mean "match
            // nothing" — NOT "no restriction" — otherwise switching both Income and
            // Expense off would silently show every row.
            selection.append(" AND 1 = 0")
        } else {
            selection.append(" AND transaction_type_id IN (${query.transactionTypeIds.placeholders()})")
            args.addAll(query.transactionTypeIds)
        }

        if (query.categoryIds.isNotEmpty()) {
            // Unlike the type filter, an empty category/payment selection means
            // "any category", which is the historical behaviour.
            selection.append(" AND category_id IN (${query.categoryIds.placeholders()})")
            args.addAll(query.categoryIds)
        }
        if (query.paymentTypeIds.isNotEmpty()) {
            selection.append(" AND payment_method_id IN (${query.paymentTypeIds.placeholders()})")
            args.addAll(query.paymentTypeIds)
        }

        query.minAmountMinor?.let {
            selection.append(" AND amount_minor >= ?")
            args += it
        }
        query.maxAmountMinor?.let {
            selection.append(" AND amount_minor <= ?")
            args += it
        }

        if (query.tagIds.isNotEmpty()) {
            appendTagFilter(selection, args, query)
        }

        return selection.toString() to args
    }

    /**
     * Restricts the selection to transactions carrying the chosen tags.
     *
     * The join table is queried with a sub-select on `transactions.id` rather than a
     * `JOIN` in the outer statement. Two reasons: the same `selection()` feeds the
     * paging, id and totals statements, and a `JOIN` would change the row count the
     * totals aggregate sees (a transaction with two matching tags would be counted
     * twice); and the sub-select keeps the outer query's columns and `ORDER BY`
     * untouched.
     *
     * `OR` asks for any one of the tags; `AND` asks for all of them, which is expressed
     * as "the number of distinct chosen tags on this transaction equals the number
     * chosen". `COUNT(DISTINCT tag_id)` is the count that cannot be inflated by a
     * duplicate join row, so the two modes cannot disagree about a transaction that
     * carries a tag twice.
     *
     * Both branches exclude soft-deleted tags and transactions, so a filter can never
     * match through a row the ledger would not show.
     */
    private fun appendTagFilter(
        selection: StringBuilder,
        args: MutableList<Any>,
        query: TransactionQuery
    ) {
        when (query.tagMatchMode) {
            TagMatchMode.OR -> {
                selection.append(
                    " AND id IN (" +
                        "SELECT tt.transaction_id FROM transaction_tags tt " +
                        "JOIN tags ON tags.id = tt.tag_id " +
                        "JOIN transactions t2 ON t2.id = tt.transaction_id " +
                        "WHERE tags.is_deleted = 0 AND t2.is_deleted = 0 " +
                        "AND tt.tag_id IN (${query.tagIds.tagPlaceholders()}))"
                )
                args.addAll(query.tagIds)
            }
            TagMatchMode.AND -> {
                selection.append(
                    " AND id IN (" +
                        "SELECT tt.transaction_id FROM transaction_tags tt " +
                        "JOIN tags ON tags.id = tt.tag_id " +
                        "JOIN transactions t2 ON t2.id = tt.transaction_id " +
                        "WHERE tags.is_deleted = 0 AND t2.is_deleted = 0 " +
                        "AND tt.tag_id IN (${query.tagIds.tagPlaceholders()}) " +
                        "GROUP BY tt.transaction_id HAVING COUNT(DISTINCT tt.tag_id) = ?)"
                )
                args.addAll(query.tagIds)
                args += query.tagIds.size
            }
        }
    }

    fun paging(query: TransactionQuery): TransactionSqlQuery {
        val (selection, args) = selection(query)
        val sql = "SELECT * FROM transactions WHERE $selection ORDER BY ${orderBy(query.sort)}, id ASC"
        return TransactionSqlQuery(sql, args)
    }

    fun ids(query: TransactionQuery): TransactionSqlQuery {
        val (selection, args) = selection(query)
        return TransactionSqlQuery("SELECT id FROM transactions WHERE $selection", args)
    }

    /**
     * Income, expense and row count for exactly the rows the list shows.
     * `ORDER BY` is intentionally absent — ordering a single aggregate row is
     * meaningless work.
     */
    fun totals(query: TransactionQuery): TransactionSqlQuery {
        val (selection, args) = selection(query)
        val sql = "SELECT " +
            "COALESCE(SUM(CASE WHEN transaction_type_id = 1 THEN amount_minor ELSE 0 END), 0) AS income_minor, " +
            "COALESCE(SUM(CASE WHEN transaction_type_id != 1 THEN amount_minor ELSE 0 END), 0) AS expense_minor, " +
            "COUNT(*) AS total_count " +
            "FROM transactions WHERE $selection"
        return TransactionSqlQuery(sql, args)
    }

    fun toSQLiteQuery(query: TransactionSqlQuery): SupportSQLiteQuery =
        SimpleSQLiteQuery(query.sql, query.args.toTypedArray())

    private fun orderBy(sort: SortType): String = when (sort) {
        SortType.NEWEST -> "occurred_at DESC"
        SortType.OLDEST -> "occurred_at ASC"
        SortType.HIGHEST -> "amount_minor DESC, occurred_at DESC"
        SortType.LOWEST -> "amount_minor ASC, occurred_at DESC"
        SortType.INCOME_FIRST -> "transaction_type_id ASC, occurred_at DESC"
        SortType.EXPENSE_FIRST -> "transaction_type_id DESC, occurred_at DESC"
    }
}

/** `?,?,?` — one placeholder per id, used to build `IN (...)` with bind args. */
private fun List<Int>.placeholders(): String = joinToString(separator = ",") { "?" }

/**
 * The same, for the string tag ids. Named differently from the int overload rather than
 * overloaded, because both erase to the same JVM signature and the compiler rejects the
 * pair.
 */
private fun List<String>.tagPlaceholders(): String = joinToString(separator = ",") { "?" }
