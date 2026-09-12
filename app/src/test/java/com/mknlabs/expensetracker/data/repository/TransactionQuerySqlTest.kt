package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.domain.repository.TransactionQuery
import com.mknlabs.expensetracker.models.SortType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the SQL and bind arguments produced for the Transactions screen. This is
 * the code that decides what the user actually sees, so it is asserted literally
 * rather than through a fake.
 */
class TransactionQuerySqlTest {

    @Test
    fun `default query filters deleted rows and orders newest first`() {
        val result = TransactionQuerySql.paging(TransactionQuery())

        assertEquals(
            "SELECT * FROM transactions WHERE is_deleted = 0 AND transaction_type_id IN (?,?) " +
                "ORDER BY occurred_at DESC, id ASC",
            result.sql
        )
        assertEquals(listOf<Any>(1, 2), result.args)
    }

    @Test
    fun `window bounds are bound as arguments`() {
        val result = TransactionQuerySql.paging(TransactionQuery(startMillis = 1_000L, endMillis = 2_000L))

        assertTrue(result.sql.contains("occurred_at >= ?"))
        assertTrue(result.sql.contains("occurred_at < ?"))
        assertEquals(listOf<Any>(1_000L, 2_000L, 1, 2), result.args)
    }

    @Test
    fun `open ended window omits the bound entirely`() {
        val sql = TransactionQuerySql.paging(
            TransactionQuery(
                startMillis = TransactionQuery.NO_START,
                endMillis = TransactionQuery.NO_END
            )
        ).sql

        assertFalse(sql.contains("occurred_at >="))
        assertFalse(sql.contains("occurred_at <"))
    }

    @Test
    fun `search matches note and amount`() {
        val result = TransactionQuerySql.paging(TransactionQuery(search = "Coffee"))

        assertTrue(result.sql.contains("LOWER(note) LIKE ? OR CAST(amount_minor AS TEXT) LIKE ?"))
        // Note is matched case-insensitively; the amount cast is digits only.
        assertEquals(listOf<Any>("%coffee%", "%Coffee%", 1, 2), result.args)
    }

    @Test
    fun `blank search is ignored`() {
        val result = TransactionQuerySql.paging(TransactionQuery(search = "   "))

        assertFalse(result.sql.contains("LIKE"))
        assertEquals(listOf<Any>(1, 2), result.args)
    }

    @Test
    fun `advanced search ORs the resolved category and payment ids into the search clause`() {
        val result = TransactionQuerySql.paging(
            TransactionQuery(
                search = "fuel",
                searchCategoryIds = listOf(7, 8),
                searchPaymentTypeIds = listOf(3)
            )
        )

        assertTrue(result.sql.contains("LOWER(note) LIKE ? OR CAST(amount_minor AS TEXT) LIKE ?"))
        assertTrue(result.sql.contains("OR category_id IN (?,?)"))
        assertTrue(result.sql.contains("OR payment_method_id IN (?)"))
        assertEquals(
            listOf<Any>("%fuel%", "%fuel%", 7, 8, 3, 1, 2),
            result.args
        )
    }

    @Test
    fun `type filter uses an IN clause with one placeholder per type`() {
        val result = TransactionQuerySql.paging(TransactionQuery(transactionTypeIds = listOf(1)))

        assertTrue(result.sql.contains("transaction_type_id IN (?)"))
        assertEquals(listOf<Any>(1), result.args)
    }

    @Test
    fun `deselecting every transaction type matches nothing instead of everything`() {
        val result = TransactionQuerySql.paging(TransactionQuery(transactionTypeIds = emptyList()))

        assertTrue(result.sql.contains("AND 1 = 0"))
        assertFalse(result.sql.contains("transaction_type_id IN"))
        // Crucially, no args were dropped in a way that would widen the query.
        assertEquals(emptyList<Any>(), result.args)

        // The aggregate agrees, so the summary cannot claim rows the list hides.
        val totals = TransactionQuerySql.totals(TransactionQuery(transactionTypeIds = emptyList()))
        assertTrue(totals.sql.contains("AND 1 = 0"))
    }

    @Test
    fun `empty category and payment filters mean no restriction`() {
        val result = TransactionQuerySql.paging(TransactionQuery())

        assertFalse(result.sql.contains("category_id IN"))
        assertFalse(result.sql.contains("payment_method_id IN"))
    }

    @Test
    fun `category and payment filters use IN clauses`() {
        val result = TransactionQuerySql.paging(
            TransactionQuery(categoryIds = listOf(4, 5), paymentTypeIds = listOf(2))
        )

        assertTrue(result.sql.contains("category_id IN (?,?)"))
        assertTrue(result.sql.contains("payment_method_id IN (?)"))
        assertEquals(listOf<Any>(1, 2, 4, 5, 2), result.args)
    }

    @Test
    fun `amount range is inclusive`() {
        val result = TransactionQuerySql.paging(
            TransactionQuery(minAmountMinor = 1_000L, maxAmountMinor = 2_500L)
        )

        assertTrue(result.sql.contains("amount_minor >= ?"))
        assertTrue(result.sql.contains("amount_minor <= ?"))
        assertEquals(listOf<Any>(1, 2, 1_000L, 2_500L), result.args)
    }

    @Test
    fun `sort orders map to SQL`() {
        fun orderBy(sort: SortType): String =
            TransactionQuerySql.paging(TransactionQuery(sort = sort)).sql

        assertTrue(orderBy(SortType.NEWEST).endsWith("ORDER BY occurred_at DESC, id ASC"))
        assertTrue(orderBy(SortType.OLDEST).endsWith("ORDER BY occurred_at ASC, id ASC"))
        assertTrue(orderBy(SortType.HIGHEST).endsWith("ORDER BY amount_minor DESC, occurred_at DESC, id ASC"))
        assertTrue(orderBy(SortType.LOWEST).endsWith("ORDER BY amount_minor ASC, occurred_at DESC, id ASC"))
        assertTrue(orderBy(SortType.INCOME_FIRST).endsWith("ORDER BY transaction_type_id ASC, occurred_at DESC, id ASC"))
        assertTrue(orderBy(SortType.EXPENSE_FIRST).endsWith("ORDER BY transaction_type_id DESC, occurred_at DESC, id ASC"))
    }

    @Test
    fun `ids query projects only the id`() {
        val result = TransactionQuerySql.ids(TransactionQuery(categoryIds = listOf(9)))

        assertTrue(result.sql.startsWith("SELECT id FROM transactions WHERE "))
        assertFalse(result.sql.contains("ORDER BY"))
        assertEquals(listOf<Any>(1, 2, 9), result.args)
    }

    @Test
    fun `totals query aggregates income expense and count`() {
        val result = TransactionQuerySql.totals(TransactionQuery(startMillis = 10L, endMillis = 20L))

        assertTrue(result.sql.contains("CASE WHEN transaction_type_id = 1 THEN amount_minor ELSE 0 END"))
        assertTrue(result.sql.contains("AS income_minor"))
        assertTrue(result.sql.contains("AS expense_minor"))
        assertTrue(result.sql.contains("COUNT(*) AS total_count"))
        assertFalse(result.sql.contains("ORDER BY"))
        assertEquals(listOf<Any>(10L, 20L, 1, 2), result.args)
    }

    @Test
    fun `every placeholder has a matching bind argument`() {
        val queries = listOf(
            TransactionQuery(),
            TransactionQuery(
                startMillis = 1L,
                endMillis = 2L,
                search = "x",
                searchCategoryIds = listOf(1, 2),
                searchPaymentTypeIds = listOf(3),
                transactionTypeIds = listOf(1),
                categoryIds = listOf(4, 5, 6),
                paymentTypeIds = listOf(7),
                minAmountMinor = 8L,
                maxAmountMinor = 9L
            )
        )

        queries.forEach { query ->
            listOf(
                TransactionQuerySql.paging(query),
                TransactionQuerySql.ids(query),
                TransactionQuerySql.totals(query)
            ).forEach { built ->
                assertEquals(
                    "placeholder count must match bind args for: ${built.sql}",
                    built.args.size,
                    built.sql.count { it == '?' }
                )
            }
        }
    }
}
