package com.mkn0079.expensetracker.ui.viewmodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import com.mkn0079.expensetracker.utils.formatCurrencyValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import com.mkn0079.expensetracker.R

enum class AnalyticsPeriod(val labelRes: Int) {
    WEEK(R.string.label_week),
    MONTH(R.string.label_month_period),
    YEAR(R.string.label_year_period),
    CUSTOM(R.string.label_custom)
}

@Immutable
data class CategoryBreakdownUi(
    val id: Int,
    val label: String,
    val amountDisplay: String,
    val fraction: Float,
    val percentLabel: Int,
    val colorIndex: Int,
    val isOther: Boolean = false
)

@Immutable
data class PaymentTypeBreakdownUi(
    val id: Int,
    val label: String,
    val amountDisplay: String,
    val fraction: Float,
    val percentLabel: Int,
    val colorIndex: Int,
    val icon: ImageVector,
    val isOther: Boolean = false
)

@Immutable
data class TopSpendingItemUi(
    val id: String,
    val note: String,
    val amountDisplay: String,
    val categoryLabel: String,
    val icon: ImageVector,
    val createdAt: Long,
    val isGeneral: Boolean = false
)

@Immutable
data class SummaryLabelUi(
    val resId: Int? = null,
    val datePattern: String? = null,
    val timestamp: Long? = null,
    val customRange: String? = null
)

@Immutable
data class SmartTipUi(
    val resId: Int,
    val flowChange: String? = null,
    val directionResId: Int? = null,
    val topCategory: String? = null,
    val savingAmount: String? = null,
    val hasSpendingData: Boolean = false
)

@Immutable
data class AnalyticsSnapshotUi(
    val summaryLabel: SummaryLabelUi = SummaryLabelUi(resId = R.string.label_this_month),
    val totalDisplay: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val changeDisplay: String = "+0%",
    val changePercent: Float = 0f,
    val avgDailyDisplay: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val dailyDeltaDisplay: String = "+0%",
    val dailyDeltaPercent: Float = 0f,
    val savingsDisplay: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val savingsDeltaDisplay: String = "+0%",
    val savingsDeltaPercent: Float = 0f,
    val incomeDisplay: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val expenseDisplay: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val incomeFraction: Float = 0f,
    val expenseChartPoints: List<Float> = emptyList(),
    val incomeChartPoints: List<Float> = emptyList(),
    val chartLabels: List<ChartLabelUi> = emptyList(),
    val categoryBreakdown: List<CategoryBreakdownUi> = emptyList(),
    val allCategoryBreakdown: List<CategoryBreakdownUi> = emptyList(),
    val paymentTypeBreakdown: List<PaymentTypeBreakdownUi> = emptyList(),
    val allPaymentTypeBreakdown: List<PaymentTypeBreakdownUi> = emptyList(),
    val topTransactions: List<TopSpendingItemUi> = emptyList(),
    val allTopTransactions: List<TopSpendingItemUi> = emptyList(),
    val smartTip: SmartTipUi = SmartTipUi(resId = R.string.msg_unlock_insights_hint),
    val hasSpendingData: Boolean = false
)

@Immutable
data class ChartLabelUi(
    val label: String? = null,
    val resId: Int? = null,
    val index: Int? = null // for array lookups
)

@Immutable
data class AnalyticsScreenUiState(
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.MONTH,
    val customRangeStart: Long? = null,
    val customRangeEnd: Long? = null,
    val activeRange: LongRange = System.currentTimeMillis()..System.currentTimeMillis(),
    val snapshot: AnalyticsSnapshotUi = AnalyticsSnapshotUi()
) {
    val customRange: LongRange?
        get() = customRangeStart?.let { start ->
            customRangeEnd?.let { end ->
                startOfDay(start)..endOfDay(end)
            }
        }
}

private data class ChartBucket(
    val label: ChartLabelUi,
    val expenseValue: Double,
    val incomeValue: Double
)

class AnalyticsViewModel : ViewModel() {

    private var currentTransactions: List<Transaction> = emptyList()
    private var currentCategories: List<CategoryType> = emptyList()
    private var currentPaymentTypes: List<PaymentType> = emptyList()
    private var currentCurrencyId: Int = DEFAULT_CURRENCY_ID
    private var currentAmountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences

    private var selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.MONTH
    private var customRangeStart: Long? = null
    private var customRangeEnd: Long? = null

    private val _uiState = MutableStateFlow(AnalyticsScreenUiState())
    val uiState: StateFlow<AnalyticsScreenUiState> = _uiState.asStateFlow()

    fun updateInputs(
        transactions: List<Transaction>,
        categories: List<CategoryType>,
        paymentTypes: List<PaymentType>,
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences
    ) {
        currentTransactions = transactions
        currentCategories = categories
        currentPaymentTypes = paymentTypes
        currentCurrencyId = currencyId
        currentAmountFormatPreferences = amountFormatPreferences
        rebuildUiState()
    }

    fun selectPeriod(period: AnalyticsPeriod) {
        selectedPeriod = period
        rebuildUiState()
    }

    fun applyCustomRange(
        startMillis: Long,
        endMillis: Long
    ) {
        customRangeStart = startMillis
        customRangeEnd = endMillis
        selectedPeriod = AnalyticsPeriod.CUSTOM
        rebuildUiState()
    }

    fun clearCustomRange() {
        customRangeStart = null
        customRangeEnd = null
        selectedPeriod = AnalyticsPeriod.MONTH
        rebuildUiState()
    }

    private fun rebuildUiState() {
        val latestTimestamp = System.currentTimeMillis()
        val range = customRangeStart?.let { start ->
            customRangeEnd?.let { end ->
                startOfDay(start)..endOfDay(end)
            }
        } ?: periodRangeFor(latestTimestamp, selectedPeriod)

        _uiState.update {
            it.copy(
                selectedPeriod = selectedPeriod,
                customRangeStart = customRangeStart,
                customRangeEnd = customRangeEnd,
                activeRange = range,
                snapshot = buildAnalyticsSnapshot(
                    period = selectedPeriod,
                    currencyId = currentCurrencyId,
                    amountFormatPreferences = currentAmountFormatPreferences,
                    transactions = currentTransactions,
                    categories = currentCategories,
                    paymentTypes = currentPaymentTypes,
                    customRange = range
                )
            )
        }
    }
}

private fun buildAnalyticsSnapshot(
    period: AnalyticsPeriod,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    transactions: List<Transaction>,
    categories: List<CategoryType>,
    paymentTypes: List<PaymentType>,
    customRange: LongRange? = null
): AnalyticsSnapshotUi {
    val latestTimestamp = System.currentTimeMillis()
    val range = customRange ?: periodRangeFor(latestTimestamp, period)
    val previousRange = previousPeriodRange(range, period, customRange)
    val currentTransactions = transactions.filter { it.createdAt in range.first..range.last }
    val previousTransactions = transactions.filter { it.createdAt in previousRange.first..previousRange.last }
    val categoryMap = categories.associateBy { it.id }
    val paymentTypeMap = paymentTypes.associateBy { it.id }

    val income = currentTransactions.filter { it.transactionTypeId == 1 }.sumOf { it.amount }
    val expense = currentTransactions.filter { it.transactionTypeId == 2 }.sumOf { it.amount }
    val totalFlow = income + expense
    val savings = income - expense
    val previousFlow = previousTransactions.sumOf { it.amount }
    val previousSavings = previousTransactions.filter { it.transactionTypeId == 1 }.sumOf { it.amount } -
        previousTransactions.filter { it.transactionTypeId == 2 }.sumOf { it.amount }
    val avgDailyExpense = expense / daysInPeriod(range, period).coerceAtLeast(1.0)
    val previousAvgDailyExpense = previousTransactions.filter { it.transactionTypeId == 2 }.sumOf { it.amount } /
        daysInPeriod(previousRange, period).coerceAtLeast(1.0)
    val chartBuckets = buildChartBuckets(
        transactions = currentTransactions,
        period = period,
        activeRange = range,
        referenceTimestamp = latestTimestamp
    )
    val categoryTotals = currentTransactions
        .filter { it.transactionTypeId == 2 }
        .groupBy { it.categoryId }
        .mapValues { (_, items) -> items.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
    val totalExpenseForShare = categoryTotals.sumOf { it.second }.takeIf { it > 0.0 } ?: 1.0
    val allBreakdown = categoryTotals.mapIndexed { index, (categoryId, amount) ->
        val category = categoryMap[categoryId]
        CategoryBreakdownUi(
            id = categoryId,
            label = category?.name ?: "",
            isOther = category == null,
            amountDisplay = formatCurrencyValue(amount, currencyId, amountFormatPreferences),
            fraction = (amount / totalExpenseForShare).toFloat(),
            percentLabel = ((amount / totalExpenseForShare) * 100).toInt(),
            colorIndex = index
        )
    }
    val breakdown = allBreakdown.take(3)

    val paymentTotals = currentTransactions
        .filter { it.transactionTypeId == 2 }
        .groupBy { it.paymentTypeId }
        .mapValues { (_, items) -> items.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
    
    val allPaymentBreakdown = paymentTotals.mapIndexed { index, (paymentId, amount) ->
        val paymentType = paymentTypeMap[paymentId]
        PaymentTypeBreakdownUi(
            id = paymentId,
            label = paymentType?.name ?: "",
            isOther = paymentType == null,
            amountDisplay = formatCurrencyValue(amount, currencyId, amountFormatPreferences),
            fraction = (amount / totalExpenseForShare).toFloat(),
            percentLabel = ((amount / totalExpenseForShare) * 100).toInt(),
            colorIndex = index,
            icon = paymentType?.icon ?: Icons.Filled.Analytics
        )
    }
    val paymentBreakdown = allPaymentBreakdown.take(3)

    val sortedTransactions = currentTransactions
        .filter { it.transactionTypeId == 2 }
        .sortedByDescending { it.amount }

    val topTransactions = sortedTransactions
        .take(3)
        .map { transaction ->
            val category = categoryMap[transaction.categoryId]
            TopSpendingItemUi(
                id = transaction.id,
                note = transaction.note,
                amountDisplay = formatCurrencyValue(
                    transaction.amount,
                    currencyId,
                    amountFormatPreferences,
                    prefix = "-"
                ),
                categoryLabel = category?.name ?: "",
                isGeneral = category == null,
                icon = category?.icon ?: categoryFallbackIcon,
                createdAt = transaction.createdAt
            )
        }

    val allTopTransactions = sortedTransactions
        .take(10)
        .map { transaction ->
            val category = categoryMap[transaction.categoryId]
            TopSpendingItemUi(
                id = transaction.id,
                note = transaction.note,
                amountDisplay = formatCurrencyValue(
                    transaction.amount,
                    currencyId,
                    amountFormatPreferences,
                    prefix = "-"
                ),
                categoryLabel = category?.name ?: "",
                isGeneral = category == null,
                icon = category?.icon ?: categoryFallbackIcon,
                createdAt = transaction.createdAt
            )
        }
    val flowChange = percentageChange(totalFlow, previousFlow)
    val savingsChange = percentageChange(savings, previousSavings)
    val dailyChange = percentageChange(avgDailyExpense, previousAvgDailyExpense)
    return AnalyticsSnapshotUi(
        summaryLabel = buildSummaryLabel(period, range, customRange),
        totalDisplay = formatCurrencyValue(totalFlow, currencyId, amountFormatPreferences),
        changeDisplay = formatPercent(flowChange),
        changePercent = flowChange,
        avgDailyDisplay = formatCurrencyValue(avgDailyExpense, currencyId, amountFormatPreferences),
        dailyDeltaDisplay = formatPercent(-dailyChange),
        dailyDeltaPercent = -dailyChange,
        savingsDisplay = formatCurrencyValue(savings, currencyId, amountFormatPreferences),
        savingsDeltaDisplay = formatPercent(savingsChange),
        savingsDeltaPercent = savingsChange,
        incomeDisplay = formatCurrencyValue(income, currencyId, amountFormatPreferences),
        expenseDisplay = formatCurrencyValue(expense, currencyId, amountFormatPreferences),
        incomeFraction = (income / max(income + expense, 1.0)).toFloat(),
        expenseChartPoints = chartBuckets.map { it.expenseValue.toFloat() },
        incomeChartPoints = chartBuckets.map { it.incomeValue.toFloat() },
        chartLabels = chartBuckets.map { it.label },
        categoryBreakdown = breakdown,
        allCategoryBreakdown = allBreakdown,
        paymentTypeBreakdown = paymentBreakdown,
        allPaymentTypeBreakdown = allPaymentBreakdown,
        topTransactions = topTransactions,
        allTopTransactions = allTopTransactions,
        smartTip = buildSmartTip(
            flowChange = flowChange,
            avgDailyExpense = avgDailyExpense,
            topCategory = breakdown.firstOrNull()?.label ?: "",
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            hasSpendingData = currentTransactions.isNotEmpty()
        ),
        hasSpendingData = currentTransactions.any { it.transactionTypeId == 2 }
    )
}

private fun buildChartBuckets(
    transactions: List<Transaction>,
    period: AnalyticsPeriod,
    activeRange: LongRange,
    referenceTimestamp: Long
): List<ChartBucket> {
    return when (period) {
        AnalyticsPeriod.WEEK -> {
            val weekStart = startOfWeek(referenceTimestamp)
            (0 until 7).map { index ->
                val start = shiftByDays(weekStart, index)
                val end = shiftByDays(start, 1) - 1
                ChartBucket(
                    label = ChartLabelUi(index = index, resId = R.array.days_of_week_short),
                    expenseValue = transactions
                        .filter { it.transactionTypeId == 2 && it.createdAt in start..end }
                        .sumOf { it.amount },
                    incomeValue = transactions
                        .filter { it.transactionTypeId == 1 && it.createdAt in start..end }
                        .sumOf { it.amount }
                )
            }
        }

        AnalyticsPeriod.MONTH -> {
            val monthStart = activeRange.first
            val monthEnd = activeRange.last
            val totalDays = (daysBetween(monthStart, monthEnd) + 1).toInt()
            
            (0 until totalDays).map { dayIndex ->
                val start = shiftByDays(monthStart, dayIndex)
                val end = endOfDay(start)
                
                // Show labels only for roughly every 7 days to avoid crowding
                val label = if (dayIndex % 7 == 0 || dayIndex == totalDays - 1) {
                    val dayOfMonth = dayIndex + 1
                    when {
                        dayOfMonth <= 7 -> ChartLabelUi(label = "W1")
                        dayOfMonth <= 14 -> ChartLabelUi(label = "W2")
                        dayOfMonth <= 21 -> ChartLabelUi(label = "W3")
                        else -> ChartLabelUi(label = "W4")
                    }
                } else ChartLabelUi(label = "")
                
                ChartBucket(
                    label = label,
                    expenseValue = transactions
                        .filter { it.transactionTypeId == 2 && it.createdAt in start..end }
                        .sumOf { it.amount },
                    incomeValue = transactions
                        .filter { it.transactionTypeId == 1 && it.createdAt in start..end }
                        .sumOf { it.amount }
                )
            }
        }

        AnalyticsPeriod.YEAR -> {
            val yearStart = startOfYear(referenceTimestamp)
            
            (0 until 12).map { monthIndex ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = yearStart
                    add(Calendar.MONTH, monthIndex)
                }
                val start = startOfMonth(calendar.timeInMillis)
                val end = endOfMonth(calendar.timeInMillis)
                
                // Show labels for every 2nd month to keep it readable
                val label = if (monthIndex % 2 == 0) {
                    ChartLabelUi(index = monthIndex, resId = R.array.months_short)
                } else ChartLabelUi(label = "")
                
                ChartBucket(
                    label = label,
                    expenseValue = transactions
                        .filter { it.transactionTypeId == 2 && it.createdAt in start..end }
                        .sumOf { it.amount },
                    incomeValue = transactions
                        .filter { it.transactionTypeId == 1 && it.createdAt in start..end }
                        .sumOf { it.amount }
                )
            }
        }

        AnalyticsPeriod.CUSTOM -> buildCustomRangeBuckets(transactions, activeRange)
    }
}

private fun buildSmartTip(
    flowChange: Float,
    avgDailyExpense: Double,
    topCategory: String,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    hasSpendingData: Boolean
): SmartTipUi {
    if (!hasSpendingData) {
        return SmartTipUi(resId = R.string.msg_unlock_insights_hint)
    }
    
    return SmartTipUi(
        resId = R.string.msg_spending_trend,
        flowChange = formatPercent(flowChange),
        directionResId = if (flowChange >= 0f) R.string.label_up else R.string.label_down,
        topCategory = topCategory.ifEmpty { null },
        savingAmount = formatCurrencyValue(avgDailyExpense * 4, currencyId, amountFormatPreferences),
        hasSpendingData = true
    )
}

private fun formatPercent(value: Float): String {
    val formatter = DecimalFormat("0.#")
    val prefix = if (value >= 0f) "+" else ""
    return "$prefix${formatter.format(value)}%"
}

private fun percentageChange(current: Double, previous: Double): Float {
    if (previous == 0.0) {
        return if (current == 0.0) 0f else 100f
    }
    return (((current - previous) / previous) * 100.0).toFloat()
}

private fun periodRangeFor(timestamp: Long, period: AnalyticsPeriod): LongRange {
    return when (period) {
        AnalyticsPeriod.WEEK -> startOfWeek(timestamp)..endOfWeek(timestamp)
        AnalyticsPeriod.MONTH -> startOfMonth(timestamp)..endOfMonth(timestamp)
        AnalyticsPeriod.YEAR -> startOfYear(timestamp)..endOfYear(timestamp)
        AnalyticsPeriod.CUSTOM -> startOfMonth(timestamp)..endOfMonth(timestamp)
    }
}

private fun previousPeriodRange(
    range: LongRange,
    period: AnalyticsPeriod,
    customRange: LongRange? = null
): LongRange {
    if (period == AnalyticsPeriod.CUSTOM || customRange != null) {
        val dayCount = daysBetween(range.first, range.last) + 1
        val start = shiftByDays(range.first, -dayCount)
        return start..(shiftByDays(start, dayCount) - 1)
    }
    return when (period) {
        AnalyticsPeriod.WEEK -> {
            val start = shiftByDays(range.first, -7)
            start..(shiftByDays(start, 7) - 1)
        }

        AnalyticsPeriod.MONTH -> {
            val calendar = Calendar.getInstance().apply { timeInMillis = range.first }
            calendar.add(Calendar.MONTH, -1)
            val start = startOfMonth(calendar.timeInMillis)
            start..endOfMonth(calendar.timeInMillis)
        }

        AnalyticsPeriod.YEAR -> {
            val calendar = Calendar.getInstance().apply { timeInMillis = range.first }
            calendar.add(Calendar.YEAR, -1)
            val start = startOfYear(calendar.timeInMillis)
            start..endOfYear(calendar.timeInMillis)
        }

        AnalyticsPeriod.CUSTOM -> range
    }
}

private fun daysInPeriod(range: LongRange, period: AnalyticsPeriod): Double {
    return when (period) {
        AnalyticsPeriod.WEEK -> 7.0
        AnalyticsPeriod.MONTH, AnalyticsPeriod.YEAR, AnalyticsPeriod.CUSTOM -> {
            (daysBetween(range.first, range.last) + 1).toDouble()
        }
    }
}

private fun buildSummaryLabel(
    period: AnalyticsPeriod,
    range: LongRange,
    customRange: LongRange?
): SummaryLabelUi {
    return if (period == AnalyticsPeriod.CUSTOM && customRange != null) {
        SummaryLabelUi(customRange = formatCustomRangeLabel(customRange))
    } else {
        when (period) {
            AnalyticsPeriod.WEEK -> SummaryLabelUi(resId = R.string.label_this_week)
            AnalyticsPeriod.MONTH -> SummaryLabelUi(datePattern = "MMMM yyyy", timestamp = range.first)
            AnalyticsPeriod.YEAR -> SummaryLabelUi(datePattern = "yyyy", timestamp = range.first)
            AnalyticsPeriod.CUSTOM -> SummaryLabelUi(customRange = formatCustomRangeLabel(range))
        }
    }
}

fun buildCustomRangeHeadline(
    startMillis: Long?,
    endMillis: Long?
): String? {
    if (startMillis == null || endMillis == null) {
        return null
    }
    return "${formatShortDate(startMillis)} - ${formatShortDate(endMillis)}"
}

fun formatCustomRangeLabel(range: LongRange): String {
    return "${formatShortDate(range.first)} - ${formatShortDate(range.last)}"
}

private fun formatShortDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
}

private fun buildCustomRangeBuckets(
    transactions: List<Transaction>,
    activeRange: LongRange
): List<ChartBucket> {
    val totalDays = (daysBetween(activeRange.first, activeRange.last) + 1).coerceAtLeast(1)
    val bucketCount = min(totalDays, 4)
    val chunkSize = max(ceil(totalDays / bucketCount.toDouble()).toInt(), 1)
    return (0 until bucketCount).map { index ->
        val start = shiftByDays(activeRange.first, index * chunkSize)
        val end = min(shiftByDays(start, chunkSize) - 1, activeRange.last)
        ChartBucket(
            label = buildCustomBucketLabel(index, bucketCount, start),
            expenseValue = transactions
                .filter { it.transactionTypeId == 2 && it.createdAt in start..end }
                .sumOf { it.amount },
            incomeValue = transactions
                .filter { it.transactionTypeId == 1 && it.createdAt in start..end }
                .sumOf { it.amount }
        )
    }
}

private fun buildCustomBucketLabel(
    index: Int,
    bucketCount: Int,
    start: Long
): ChartLabelUi {
    return if (bucketCount <= 4) {
        ChartLabelUi(label = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(start)))
    } else {
        ChartLabelUi(label = "P${index + 1}")
    }
}

private fun startOfWeek(timestamp: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    firstDayOfWeek = Calendar.MONDAY
    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun startOfDay(timestamp: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun endOfDay(timestamp: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis

private fun endOfWeek(timestamp: Long): Long = shiftByDays(startOfWeek(timestamp), 7) - 1

private fun startOfMonth(timestamp: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    set(Calendar.DAY_OF_MONTH, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun endOfMonth(timestamp: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis

private fun startOfYear(timestamp: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    set(Calendar.DAY_OF_YEAR, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun endOfYear(timestamp: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    set(Calendar.MONTH, Calendar.DECEMBER)
    set(Calendar.DAY_OF_MONTH, 31)
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis

private fun shiftByDays(
    timestamp: Long,
    days: Int
): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    add(Calendar.DAY_OF_YEAR, days)
}.timeInMillis

private fun daysBetween(
    start: Long,
    end: Long
): Int {
    val millisPerDay = 24 * 60 * 60 * 1000L
    return ((end - start) / millisPerDay).toInt()
}

private val categoryFallbackIcon: ImageVector
    get() = Icons.Filled.Analytics
