package com.mkn0079.expensetracker.domain.mapper

import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.SortType
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.ui.models.TransactionCardItemUi
import com.mkn0079.expensetracker.ui.models.TransactionListItemUi
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import com.mkn0079.expensetracker.utils.formatAmount
import com.mkn0079.expensetracker.utils.formatDate
import com.mkn0079.expensetracker.utils.formatTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Transaction.toTransactionCardItemUi(
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    dateFormatPattern: String,
    timeFormat: String,
    paymentTypeName: String,
    categories: List<CategoryType>
): TransactionCardItemUi {
    val category = categories.find { it.id == categoryId }
    val resolvedIcon = category?.icon ?: categoryIcon
    return TransactionCardItemUi(
        id = id,
        transaction = this,
        note = note,
        transactionDate = formatDate(createdAt, dateFormatPattern),
        transactionTime = formatTime(createdAt, timeFormat),
        amount = formatAmount(
            amount = amount,
            transactionTypeId = transactionTypeId,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences
        ),
        icon = resolvedIcon,
        transactionTypeId = transactionTypeId,
        paymentType = paymentTypeName
    )
}

fun buildTransactionListItems(
    transactions: List<TransactionCardItemUi>,
    groupByDate: Boolean,
    sortType: SortType
): List<TransactionListItemUi> {
    if (!groupByDate) {
        return transactions.map { transaction ->
            TransactionListItemUi.TransactionRow(transaction)
        }
    }

    val groupedTransactions = transactions.groupBy { transaction ->
        getStartOfDayTimestamp(transaction.transaction.createdAt)
    }
    val sortedDays = groupedTransactions.keys.sortedWith { first, second ->
        when (sortType) {
            SortType.OLDEST -> first.compareTo(second)
            else -> second.compareTo(first)
        }
    }

    return buildList {
        sortedDays.forEach { dayTimestamp ->
            add(
                TransactionListItemUi.Header(
                    id = "header_$dayTimestamp",
                    timestamp = dayTimestamp,
                    dayLabel = getTransactionDayLabel(dayTimestamp),
                    dateLabel = formatTransactionHeaderDate(dayTimestamp)
                )
            )
            groupedTransactions[dayTimestamp].orEmpty().forEach { transaction ->
                add(TransactionListItemUi.TransactionRow(transaction))
            }
        }
    }
}

fun getStartOfDayTimestamp(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun getTransactionDayLabel(
    timestamp: Long,
    referenceTimestamp: Long = System.currentTimeMillis()
): String {
    val dayDifference = getDayDifference(
        firstTimestamp = getStartOfDayTimestamp(timestamp),
        secondTimestamp = getStartOfDayTimestamp(referenceTimestamp)
    )

    return when (dayDifference) {
        0L -> "Today"
        -1L -> "Yesterday"
        1L -> "Tomorrow"
        else -> SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun formatTransactionHeaderDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
}

private fun getDayDifference(firstTimestamp: Long, secondTimestamp: Long): Long {
    val millisPerDay = 24 * 60 * 60 * 1000L
    return (firstTimestamp - secondTimestamp) / millisPerDay
}
