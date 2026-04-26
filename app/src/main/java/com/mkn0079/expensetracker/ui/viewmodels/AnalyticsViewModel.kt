package com.mkn0079.expensetracker.ui.viewmodels

import com.mkn0079.expensetracker.ui.theme.IncomeGreen
import com.mkn0079.expensetracker.ui.theme.ExpenseRed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
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

enum class AnalyticsPeriod(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year"),
    CUSTOM("Custom")
}

@Immutable
data class CategoryBreakdownUi(
    val label: String,
    val amountDisplay: String,
    val fraction: Float,
    val percentLabel: Int,
    val color: Color
)

@Immutable
data class PaymentTypeBreakdownUi(
    val label: String,
    val amountDisplay: String,
    val fraction: Float,
    val percentLabel: Int,
    val color: Color,
    val icon: ImageVector
)

@Immutable
data class TopSpendingItemUi(
    val id: String,
    val note: String,
    val amountDisplay: String,
    val categoryLabel: String,
    val icon: ImageVector
)

@Immutable
data class AnalyticsSnapshotUi(
    val summaryLabel: String = "This month",
    val totalDisplay: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val changeDisplay: String = "+0%",
    val changePercent: Float = 0f,
    val avgDailyDisplay: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val dailyDeltaDisplay: String = "+0%",
    val savingsDisplay: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val savingsDeltaDisplay: String = "+0%",
    val incomeDisplay: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val expenseDisplay: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val incomeFraction: Float = 0f,
    val expenseChartPoints: List<Float> = emptyList(),
    val incomeChartPoints: List<Float> = emptyList(),
    val chartLabels: List<String> = emptyList(),
    val categoryBreakdown: List<CategoryBreakdownUi> = emptyList(),
    val allCategoryBreakdown: List<CategoryBreakdownUi> = emptyList(),
    val paymentTypeBreakdown: List<PaymentTypeBreakdownUi> = emptyList(),
    val allPaymentTypeBreakdown: List<PaymentTypeBreakdownUi> = emptyList(),
    val topTransactions: List<TopSpendingItemUi> = emptyList(),
    val allTopTransactions: List<TopSpendingItemUi> = emptyList(),
    val smartTip: String = "Pick a different range or add more transactions to unlock tailored spending insights.",
    val hasSpendingData: Boolean = false
)

@Immutable
data class AnalyticsScreenUiState(
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.MONTH,
    val customRangeStart: Long? = null,
    val customRangeEnd: Long? = null,
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
    val label: String,
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
        val customRange = customRangeStart?.let { start ->
            customRangeEnd?.let { end ->
                startOfDay(start)..endOfDay(end)
            }
        }

        _uiState.update {
            it.copy(
                selectedPeriod = selectedPeriod,
                customRangeStart = customRangeStart,
                customRangeEnd = customRangeEnd,
                snapshot = buildAnalyticsSnapshot(
                    period = selectedPeriod,
                    currencyId = currentCurrencyId,
                    amountFormatPreferences = currentAmountFormatPreferences,
                    transactions = currentTransactions,
                    categories = currentCategories,
                    paymentTypes = currentPaymentTypes,
                    customRange = customRange
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
    val categoryColors = listOf(Color(0xFFC9B3FF), Color(0xFFFFB482), Color(0xFF8F8A9A))
    val categoryTotals = currentTransactions
        .filter { it.transactionTypeId == 2 }
        .groupBy { it.categoryId }
        .mapValues { (_, items) -> items.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
    val totalExpenseForShare = categoryTotals.sumOf { it.second }.takeIf { it > 0.0 } ?: 1.0
    val allBreakdown = categoryTotals.mapIndexed { index, (categoryId, amount) ->
        CategoryBreakdownUi(
            label = categoryMap[categoryId]?.name ?: "Other",
            amountDisplay = formatCurrencyValue(amount, currencyId, amountFormatPreferences),
            fraction = (amount / totalExpenseForShare).toFloat(),
            percentLabel = ((amount / totalExpenseForShare) * 100).toInt(),
            color = categoryColors[index % categoryColors.size]
        )
    }
    val breakdown = allBreakdown.take(3)

    val paymentColors = listOf(IncomeGreen, Color(0xFF64B5F6), Color(0xFFFFD54F))
    val paymentTotals = currentTransactions
        .filter { it.transactionTypeId == 2 }
        .groupBy { it.paymentTypeId }
        .mapValues { (_, items) -> items.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
    
    val allPaymentBreakdown = paymentTotals.mapIndexed { index, (paymentId, amount) ->
        val paymentType = paymentTypeMap[paymentId]
        PaymentTypeBreakdownUi(
            label = paymentType?.name ?: "Wallet",
            amountDisplay = formatCurrencyValue(amount, currencyId, amountFormatPreferences),
            fraction = (amount / totalExpenseForShare).toFloat(),
            percentLabel = ((amount / totalExpenseForShare) * 100).toInt(),
            color = paymentColors[index % paymentColors.size],
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
                categoryLabel = category?.name ?: "General",
                icon = category?.icon ?: categoryFallbackIcon
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
                categoryLabel = category?.name ?: "General",
                icon = category?.icon ?: categoryFallbackIcon
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
        savingsDisplay = formatCurrencyValue(savings, currencyId, amountFormatPreferences),
        savingsDeltaDisplay = formatPercent(savingsChange),
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
            topCategory = breakdown.firstOrNull()?.label ?: "spending",
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
            val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val weekStart = startOfWeek(referenceTimestamp)
            labels.mapIndexed { index, label ->
                val start = shiftByDays(weekStart, index)
                val end = shiftByDays(start, 1) - 1
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
                        dayOfMonth <= 7 -> "W1"
                        dayOfMonth <= 14 -> "W2"
                        dayOfMonth <= 21 -> "W3"
                        else -> "W4"
                    }
                } else ""
                
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
            val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            
            (0 until 12).map { monthIndex ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = yearStart
                    add(Calendar.MONTH, monthIndex)
                }
                val start = startOfMonth(calendar.timeInMillis)
                val end = endOfMonth(calendar.timeInMillis)
                
                // Show labels for every 2nd month to keep it readable
                val label = if (monthIndex % 2 == 0) monthNames[monthIndex] else ""
                
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
): String {
    if (!hasSpendingData) {
        return "Pick a different range or add more transactions to unlock tailored spending insights."
    }
    val direction = if (flowChange >= 0f) "up" else "down"
    return "Your spending trend is ${formatPercent(flowChange)} $direction this period. Keep an eye on $topCategory and you can save about ${
        formatCurrencyValue(avgDailyExpense * 4, currencyId, amountFormatPreferences)
    } next cycle."
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
): String {
    return if (period == AnalyticsPeriod.CUSTOM && customRange != null) {
        formatCustomRangeLabel(customRange)
    } else {
        when (period) {
            AnalyticsPeriod.WEEK -> "This week"
            AnalyticsPeriod.MONTH -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(range.first))
            AnalyticsPeriod.YEAR -> SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(range.first))
            AnalyticsPeriod.CUSTOM -> formatCustomRangeLabel(range)
        }
    }
}

fun buildCustomRangeHeadline(
    startMillis: Long?,
    endMillis: Long?
): String {
    if (startMillis == null || endMillis == null) {
        return "Choose start and end dates"
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
): String {
    return if (bucketCount <= 4) {
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(start))
    } else {
        "P${index + 1}"
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
