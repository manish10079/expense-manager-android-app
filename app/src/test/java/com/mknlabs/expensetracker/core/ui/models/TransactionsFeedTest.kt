package com.mknlabs.expensetracker.core.ui.models

import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.models.SortType
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.monetization.AdPlacement
import com.mknlabs.expensetracker.core.ui.components.TransactionPeriodFilter
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toMajorUnits
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The feed builder owns the list shape that used to live inside
 * TransactionsViewModel: pinned summaries, per-month summaries, grouping and
 * ad injection. It is pure, so it is tested directly.
 */
class TransactionsFeedTest {

    @Test
    fun `daily view pins summary above the lazy list`() {
        val now = System.currentTimeMillis()

        val feed = buildFeed(
            transactions = listOf(
                transaction("t_income", now, 1_000L, typeId = 1),
                transaction("t_expense", now, 4_000L, typeId = 2)
            ),
            periodFilter = TransactionPeriodFilter.DAILY
        )

        assertNotNull(feed.pinnedSummary)
        assertEquals("summary_daily", feed.pinnedSummary?.id)
        assertTrue(feed.items.none { it is TransactionListItemUi.SummaryCard })
        assertEquals(2, feed.items.filterIsInstance<TransactionListItemUi.TransactionRow>().size)
    }

    @Test
    fun `monthly view pins summary above the lazy list`() {
        val now = System.currentTimeMillis()

        val feed = buildFeed(
            transactions = listOf(
                transaction("t_income", now, 2_000L, typeId = 1),
                transaction("t_expense", now, 3_000L, typeId = 2)
            ),
            periodFilter = TransactionPeriodFilter.MONTHLY
        )

        assertEquals("summary_monthly", feed.pinnedSummary?.id)
        assertTrue(feed.items.none { it is TransactionListItemUi.SummaryCard })
        assertEquals(2, feed.items.filterIsInstance<TransactionListItemUi.TransactionRow>().size)
    }

    @Test
    fun `yearly view pins per-year summary and keeps per-month summaries in the list`() {
        val now = System.currentTimeMillis()
        val previousMonth = previousMonthInCurrentYear()

        val feed = buildFeed(
            transactions = listOf(
                transaction("t_income", now, 2_000L, typeId = 1),
                transaction("t_expense", now, 3_000L, typeId = 2),
                transaction("t_old_expense", previousMonth, 1_500L, typeId = 2)
            ),
            periodFilter = TransactionPeriodFilter.YEARLY
        )

        assertEquals("summary_yearly", feed.pinnedSummary?.id)
        assertTrue(feed.items.any { it is TransactionListItemUi.SummaryCard })
        assertEquals(3, feed.items.filterIsInstance<TransactionListItemUi.TransactionRow>().size)
    }

    @Test
    fun `ad items are injected after every 5th transaction row with stable keys and alternating placements`() {
        val now = System.currentTimeMillis()
        val previousMonth = previousMonthInCurrentYear()

        val feed = buildFeed(
            transactions = (1..25).map { i ->
                transaction(
                    id = "t_$i",
                    createdAt = if (i % 2 == 0) previousMonth else now,
                    amountMinor = i * 1_000L,
                    typeId = 2
                )
            },
            periodFilter = TransactionPeriodFilter.YEARLY
        )

        val items = feed.items
        val adSlots = items.filterIsInstance<TransactionListItemUi.Ad>()
        assertEquals(listOf("ad_5", "ad_10", "ad_15", "ad_20", "ad_25"), adSlots.map { it.id })
        assertEquals(
            listOf(
                AdPlacement.TRANSACTIONS_LIST,
                AdPlacement.TRANSACTIONS_LIST_2,
                AdPlacement.TRANSACTIONS_LIST,
                AdPlacement.TRANSACTIONS_LIST_2,
                AdPlacement.TRANSACTIONS_LIST
            ),
            adSlots.map { it.placement }
        )
        assertEquals(25, items.filterIsInstance<TransactionListItemUi.TransactionRow>().size)

        var rowCount = 0
        items.forEach { item ->
            when (item) {
                is TransactionListItemUi.TransactionRow -> rowCount++
                is TransactionListItemUi.Ad -> assertTrue(rowCount in listOf(5, 10, 15, 20, 25))
                else -> Unit
            }
        }
    }

    @Test
    fun `summaries disabled leaves pinned summary null`() {
        val now = System.currentTimeMillis()

        val feed = buildFeed(
            transactions = listOf(transaction("t_expense", now, 4_000L, typeId = 2)),
            periodFilter = TransactionPeriodFilter.MONTHLY,
            customizationSettings = TransactionCardCustomizationSettings(
                showTransactionListSummaries = false
            )
        )

        assertNull(feed.pinnedSummary)
        assertTrue(feed.items.none { it is TransactionListItemUi.SummaryCard })
    }

    @Test
    fun `pinned card uses the database totals instead of the loaded rows`() {
        val now = System.currentTimeMillis()

        val feed = buildFeed(
            // The loaded page happens to hold only one small expense...
            transactions = listOf(transaction("t_expense", now, 1_000L, typeId = 2)),
            periodFilter = TransactionPeriodFilter.MONTHLY,
            // ...while the database aggregate covers the entire filtered set.
            pinnedIncomeMinor = 400_000L,
            pinnedExpenseMinor = 650_000L
        )

        fun format(minor: Long) = formatCurrencyValue(
            amount = minor.toMajorUnits(),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences
        )

        assertEquals(format(650_000L), feed.pinnedSummary?.totalExpense)
        assertEquals(format(400_000L), feed.pinnedSummary?.totalIncome)
        // i.e. it is NOT the sum of the loaded rows, which would grow while scrolling.
        assertNotEquals(format(1_000L), feed.pinnedSummary?.totalExpense)
    }

    @Test
    fun `active filter pins the filtered-summary card instead of the period one`() {
        val now = System.currentTimeMillis()

        val feed = buildFeed(
            transactions = listOf(transaction("t_expense", now, 4_000L, typeId = 2)),
            periodFilter = TransactionPeriodFilter.MONTHLY,
            isFilterActive = true
        )

        assertEquals("summary_filtered_search", feed.pinnedSummary?.id)
        assertTrue(feed.items.none { it is TransactionListItemUi.SummaryCard })
    }

    @Test
    fun `date separators insert a header per day when enabled`() {
        val dayOne = startOfToday()
        val dayTwo = dayOne - 24 * 60 * 60 * 1000L

        val feed = buildFeed(
            transactions = listOf(
                transaction("t_a", dayOne, 1_000L),
                transaction("t_b", dayTwo, 2_000L)
            ),
            periodFilter = TransactionPeriodFilter.MONTHLY,
            customizationSettings = TransactionCardCustomizationSettings(
                showTransactionListSummaries = false,
                showDateSeparators = true
            )
        )

        assertEquals(2, feed.items.filterIsInstance<TransactionListItemUi.Header>().size)
    }

    private fun buildFeed(
        transactions: List<Transaction>,
        periodFilter: TransactionPeriodFilter,
        isFilterActive: Boolean = false,
        pinnedIncomeMinor: Long = 0L,
        pinnedExpenseMinor: Long = 0L,
        customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(
            showTransactionListSummaries = true
        )
    ): TransactionsFeed {
        return buildTransactionsFeed(
            transactions = transactions,
            periodFilter = periodFilter,
            sortType = SortType.NEWEST,
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences,
            dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
            timeFormat = DEFAULT_TIME_FORMAT,
            paymentTypeNames = emptyMap(),
            categories = emptyList(),
            customizationSettings = customizationSettings,
            isFilterActive = isFilterActive,
            pinnedIncomeMinor = pinnedIncomeMinor,
            pinnedExpenseMinor = pinnedExpenseMinor,
            fallbackCategoryName = "Other",
            todayLabel = "Today",
            yesterdayLabel = "Yesterday",
            tomorrowLabel = "Tomorrow"
        )
    }

    private fun transaction(
        id: String,
        createdAt: Long,
        amountMinor: Long,
        typeId: Int = 2
    ): Transaction {
        return Transaction(
            id = id,
            note = "test",
            createdAt = createdAt,
            amountMinor = amountMinor,
            transactionTypeId = typeId,
            paymentTypeId = 1,
            categoryId = 1,
            syncState = SyncState.LOCAL_ONLY
        )
    }

    private fun startOfToday(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /** A timestamp in the previous month, clamped into the current year. */
    private fun previousMonthInCurrentYear(): Long {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, if (currentMonth == Calendar.JANUARY) currentMonth else currentMonth - 1)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
