package com.mknlabs.expensetracker.ui.models

import com.mknlabs.expensetracker.domain.mapper.buildTransactionListItems
import com.mknlabs.expensetracker.domain.mapper.toTransactionCardItemUi
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.SortType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.monetization.AdPlacement
import com.mknlabs.expensetracker.ui.components.TransactionPeriodFilter
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toMajorUnits
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** The renderable list plus the summary pinned above the list. */
data class TransactionsFeed(
    val items: List<TransactionListItemUi>,
    val pinnedSummary: TransactionListItemUi.SummaryCard?
)

/**
 * Turns the transactions that Paging 3 has loaded so far into the list the screen
 * renders: optional date headers, per-period summaries, the pinned summary and
 * the interleaved ad slots.
 *
 * This is presentation-only and pure, so it can run in a `remember` keyed on the
 * loaded page snapshot — which is exactly what the screen does. Because it always
 * rebuilds from every loaded page, date headers stay correct across page
 * boundaries.
 *
 * The pinned card's totals are passed in from the database aggregate, NOT summed
 * from [transactions]: summarising the loaded pages would produce a figure that
 * silently grows while the user scrolls.
 */
fun buildTransactionsFeed(
    transactions: List<Transaction>,
    periodFilter: TransactionPeriodFilter,
    sortType: SortType,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    timeFormat: String,
    paymentTypeNames: Map<Int, String>,
    categories: List<CategoryType>,
    customizationSettings: TransactionCardCustomizationSettings,
    isFilterActive: Boolean,
    /** Income for the whole filtered result set, from the database. */
    pinnedIncomeMinor: Long,
    /** Expense for the whole filtered result set, from the database. */
    pinnedExpenseMinor: Long,
    fallbackCategoryName: String,
    todayLabel: String,
    yesterdayLabel: String,
    tomorrowLabel: String
): TransactionsFeed {
    val formatVal = { value: Double ->
        formatCurrencyValue(
            amount = value,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences
        )
    }

    val mappedCardItems = transactions.map { transaction ->
        transaction.toTransactionCardItemUi(
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat,
            paymentTypeName = paymentTypeNames[transaction.paymentTypeId].orEmpty(),
            categories = categories,
            fallbackCategoryName = fallbackCategoryName
        )
    }

    val shouldGroupTransactions = customizationSettings.showDateSeparators &&
        periodFilter != TransactionPeriodFilter.DAILY
    val showSummaries = customizationSettings.showTransactionListSummaries

    val transactionItems = mutableListOf<TransactionListItemUi>()
    var pinnedSummary: TransactionListItemUi.SummaryCard? = null

    // Formatted once for every pinned-card branch below.
    val pinnedIncome = formatVal(pinnedIncomeMinor.toMajorUnits())
    val pinnedExpense = formatVal(pinnedExpenseMinor.toMajorUnits())

    if (isFilterActive) {
        // A filter or search is narrowing the list, so the header summary describes
        // the filtered set rather than the period.
        if (showSummaries) {
            pinnedSummary = TransactionListItemUi.SummaryCard(
                id = "summary_filtered_search",
                totalIncome = pinnedIncome,
                totalExpense = pinnedExpense,
                periodLabel = null
            )
        }
        transactionItems.addAll(
            buildTransactionListItems(
                transactions = mappedCardItems,
                groupByDate = shouldGroupTransactions,
                sortType = sortType,
                todayLabel = todayLabel,
                yesterdayLabel = yesterdayLabel,
                tomorrowLabel = tomorrowLabel
            )
        )
    } else {
        when (periodFilter) {
            TransactionPeriodFilter.DAILY -> {
                if (showSummaries) {
                    pinnedSummary = TransactionListItemUi.SummaryCard(
                        id = "summary_daily",
                        totalIncome = pinnedIncome,
                        totalExpense = pinnedExpense,
                        periodLabel = null
                    )
                }
                transactionItems.addAll(
                    buildTransactionListItems(
                        transactions = mappedCardItems,
                        groupByDate = false,
                        sortType = sortType,
                        todayLabel = todayLabel,
                        yesterdayLabel = yesterdayLabel,
                        tomorrowLabel = tomorrowLabel
                    )
                )
            }

            TransactionPeriodFilter.MONTHLY -> {
                if (showSummaries) {
                    pinnedSummary = TransactionListItemUi.SummaryCard(
                        id = "summary_monthly",
                        totalIncome = pinnedIncome,
                        totalExpense = pinnedExpense,
                        periodLabel = null
                    )
                }
                transactionItems.addAll(
                    buildTransactionListItems(
                        transactions = mappedCardItems,
                        groupByDate = shouldGroupTransactions,
                        sortType = sortType,
                        todayLabel = todayLabel,
                        yesterdayLabel = yesterdayLabel,
                        tomorrowLabel = tomorrowLabel
                    )
                )
            }

            TransactionPeriodFilter.YEARLY -> {
                if (showSummaries) {
                    pinnedSummary = TransactionListItemUi.SummaryCard(
                        id = "summary_yearly",
                        totalIncome = pinnedIncome,
                        totalExpense = pinnedExpense,
                        periodLabel = null
                    )
                }
                val calendar = Calendar.getInstance()
                val groupedByMonth = mappedCardItems.groupBy { item ->
                    calendar.timeInMillis = item.transaction.createdAt
                    calendar.get(Calendar.MONTH)
                }
                val sortedMonthKeys = if (sortType == SortType.OLDEST) {
                    groupedByMonth.keys.sorted()
                } else {
                    groupedByMonth.keys.sortedDescending()
                }

                sortedMonthKeys.forEach { monthKey ->
                    // NOTE: per-month cards are still summarised from the loaded
                    // pages only; making them exact needs a grouped aggregate.
                    val monthTransactions = groupedByMonth[monthKey].orEmpty()
                    val monthIncome = monthTransactions
                        .filter { it.transactionTypeId == 1 }
                        .sumOf { it.transaction.amount }
                    val monthExpense = monthTransactions
                        .filter { it.transactionTypeId != 1 }
                        .sumOf { it.transaction.amount }

                    calendar.set(Calendar.MONTH, monthKey)
                    val monthLabel = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)

                    if (showSummaries) {
                        transactionItems.add(
                            TransactionListItemUi.SummaryCard(
                                id = "summary_yearly_month_$monthKey",
                                totalIncome = formatVal(monthIncome),
                                totalExpense = formatVal(monthExpense),
                                periodLabel = monthLabel
                            )
                        )
                    }

                    transactionItems.addAll(
                        buildTransactionListItems(
                            transactions = monthTransactions,
                            groupByDate = shouldGroupTransactions,
                            sortType = sortType,
                            todayLabel = todayLabel,
                            yesterdayLabel = yesterdayLabel,
                            tomorrowLabel = tomorrowLabel
                        )
                    )
                }
            }

            TransactionPeriodFilter.ALL -> {
                transactionItems.addAll(
                    buildTransactionListItems(
                        transactions = mappedCardItems,
                        groupByDate = shouldGroupTransactions,
                        sortType = sortType,
                        todayLabel = todayLabel,
                        yesterdayLabel = yesterdayLabel,
                        tomorrowLabel = tomorrowLabel
                    )
                )
            }
        }
    }

    // Ad injection: insert ads after every 5th transaction row. Ads are their own
    // keyed items so their AndroidView is recycled independently of card rows.
    val itemsWithAds = ArrayList<TransactionListItemUi>(
        transactionItems.size + transactionItems.size / 5 + 1
    )
    var rowIndex = 0
    var adCount = 0
    transactionItems.forEach { item ->
        itemsWithAds.add(item)
        if (item is TransactionListItemUi.TransactionRow) {
            rowIndex++
            if (rowIndex % 5 == 0) {
                adCount++
                val placement = if (adCount % 2 == 1) {
                    AdPlacement.TRANSACTIONS_LIST
                } else {
                    AdPlacement.TRANSACTIONS_LIST_2
                }
                itemsWithAds.add(TransactionListItemUi.Ad(id = "ad_$rowIndex", placement = placement))
            }
        }
    }

    return TransactionsFeed(items = itemsWithAds, pinnedSummary = pinnedSummary)
}
