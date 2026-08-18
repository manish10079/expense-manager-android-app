package com.mknlabs.expensetracker.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.domain.mapper.toTransactionCardItemUi
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.ui.models.CalendarDayUi
import com.mknlabs.expensetracker.ui.models.CalendarMonthFinancialSummaryUi
import com.mknlabs.expensetracker.ui.models.TransactionCardItemUi
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

import com.mknlabs.expensetracker.utils.UiText

@Immutable
data class CalendarScreenUiState(
    val isYearView: Boolean = false,
    val displayedMonthStart: Long = 0L,
    val selectedDate: Long = 0L,
    val displayedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val monthDays: List<CalendarDayUi> = emptyList(),
    val selectedDayTransactions: List<TransactionCardItemUi> = emptyList(),
    val selectedDayExpenseLabel: UiText = UiText.dynamic(""),
    val selectedDayIncomeLabel: UiText = UiText.dynamic(""),
    val selectedDayTitle: String = "",
    val emptyTransactionsMessage: UiText = UiText.dynamic(""),
    val yearSummaries: List<CalendarMonthFinancialSummaryUi> = emptyList(),
    val yearlyIncomeLabel: String = "",
    val yearlyExpenseLabel: String = "",
    val calendarYearRange: IntRange = 2024..2026,
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private var currentTransactions: List<Transaction> = emptyList()
    private var currentCategories: List<CategoryType> = emptyList()
    private var currentCurrencyId: Int = DEFAULT_CURRENCY_ID
    private var currentAmountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences
    private var currentDateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN
    private var currentTimeFormat: String = DEFAULT_TIME_FORMAT
    private var currentCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings()

    private var isYearView: Boolean = false
    private var displayedMonthStart: Long = 0L
    private var selectedDate: Long = 0L
    private var todayDate: Long = startOfDay(System.currentTimeMillis())
    private var todayMonthStart: Long = startOfMonth(todayDate)

    private val _uiState = MutableStateFlow(CalendarScreenUiState())
    val uiState: StateFlow<CalendarScreenUiState> = _uiState.asStateFlow()

    init {
        rebuildUiState()
    }

    fun updateInputs(
        transactions: List<Transaction>,
        categories: List<CategoryType>,
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences,
        dateFormatPattern: String,
        timeFormat: String,
        customizationSettings: TransactionCardCustomizationSettings
    ) {
        currentTransactions = transactions
        currentCategories = categories
        currentCurrencyId = currencyId
        currentAmountFormatPreferences = amountFormatPreferences
        currentDateFormatPattern = dateFormatPattern
        currentTimeFormat = timeFormat
        currentCustomizationSettings = customizationSettings

        val latestTransactionDay = startOfDay(transactions.maxOfOrNull { it.createdAt } ?: System.currentTimeMillis())
        val latestTransactionMonth = startOfMonth(latestTransactionDay)
        if (displayedMonthStart == 0L) {
            displayedMonthStart = latestTransactionMonth
        }
        if (selectedDate == 0L) {
            selectedDate = latestTransactionDay
        }
        rebuildUiState()
    }

    fun setYearView(enabled: Boolean) {
        isYearView = enabled
        rebuildUiState()
    }

    fun goToPreviousMonth() {
        val targetMonth = addMonths(displayedMonthStart, -1)
        displayedMonthStart = targetMonth
        selectedDate = alignDateToMonth(selectedDate, targetMonth)
        rebuildUiState()
    }

    fun goToNextMonth() {
        val targetMonth = addMonths(displayedMonthStart, 1)
        displayedMonthStart = targetMonth
        selectedDate = alignDateToMonth(selectedDate, targetMonth)
        rebuildUiState()
    }

    fun goToPreviousYear() {
        val targetMonth = addMonths(displayedMonthStart, -12)
        displayedMonthStart = targetMonth
        selectedDate = alignDateToMonth(selectedDate, targetMonth)
        rebuildUiState()
    }

    fun goToNextYear() {
        val targetMonth = addMonths(displayedMonthStart, 12)
        displayedMonthStart = targetMonth
        selectedDate = alignDateToMonth(selectedDate, targetMonth)
        rebuildUiState()
    }

    fun jumpToToday() {
        refreshToday()
        isYearView = false
        displayedMonthStart = todayMonthStart
        selectedDate = todayDate
        rebuildUiState()
    }

    /**
     * Recomputes "today" from the system clock. Called when the screen resumes
     * and when the user taps Today, so the calendar's current-day anchor follows
     * a date change that happened while the app was in the background.
     */
    fun refreshToday() {
        val result = computeTodayRefresh(
            currentTodayDate = todayDate,
            currentTodayMonthStart = todayMonthStart,
            selectedDate = selectedDate,
            displayedMonthStart = displayedMonthStart,
            newToday = startOfDay(System.currentTimeMillis())
        )
        if (!result.changed) return
        todayDate = result.todayDate
        todayMonthStart = result.todayMonthStart
        displayedMonthStart = result.displayedMonthStart
        selectedDate = result.selectedDate
        rebuildUiState()
    }

    fun selectDay(day: CalendarDayUi) {
        selectedDate = day.timestamp
        if (!day.isCurrentMonth) {
            displayedMonthStart = startOfMonth(day.timestamp)
        }
        rebuildUiState()
    }

    fun selectMonth(monthStart: Long) {
        displayedMonthStart = monthStart
        selectedDate = defaultSelectedDateForMonth(monthStart, currentTransactions, todayDate)
        rebuildUiState()
    }

    fun selectYear(year: Int) {
        val targetMonth = createDate(year, Calendar.JANUARY, 1)
        displayedMonthStart = targetMonth
        selectedDate = defaultSelectedDateForMonth(targetMonth, currentTransactions, todayDate)
        rebuildUiState()
    }

    private fun rebuildUiState() {
        val safeDisplayedMonthStart = displayedMonthStart.takeIf { it != 0L } ?: todayMonthStart
        val safeSelectedDate = selectedDate.takeIf { it != 0L } ?: todayDate
        val latestTransactionDay = startOfDay(currentTransactions.maxOfOrNull { it.createdAt } ?: System.currentTimeMillis())
        val displayedYear = getField(safeDisplayedMonthStart, Calendar.YEAR)
        val monthDays = buildMonthGrid(safeDisplayedMonthStart, currentTransactions)
        val selectedDayTransactions = currentTransactions
            .filter { isSameDay(it.createdAt, safeSelectedDate) }
            .sortedByDescending { it.createdAt }
        val selectedDayExpenseTotal = selectedDayTransactions
            .filter { it.transactionTypeId != 1 }
            .sumOf { it.amount }
        val selectedDayIncomeTotal = selectedDayTransactions
            .filter { it.transactionTypeId == 1 }
            .sumOf { it.amount }
        val yearSummaries = buildYearSummaries(
            year = displayedYear,
            transactions = currentTransactions,
            latestTransactionDay = latestTransactionDay,
            currencyId = currentCurrencyId,
            amountFormatPreferences = currentAmountFormatPreferences
        )
        val yearlyIncome = currentTransactions
            .filter { getField(it.createdAt, Calendar.YEAR) == displayedYear && it.transactionTypeId == 1 }
            .sumOf { it.amount }
        val yearlyExpense = currentTransactions
            .filter { getField(it.createdAt, Calendar.YEAR) == displayedYear && it.transactionTypeId != 1 }
            .sumOf { it.amount }
        val calendarYearRange = buildCalendarYearRange(currentTransactions, todayDate)
        val paymentTypeNames = paymentTypeMap.mapValues { it.value.name }

        _uiState.update {
            it.copy(
                isYearView = isYearView,
                displayedMonthStart = safeDisplayedMonthStart,
                selectedDate = safeSelectedDate,
                displayedYear = displayedYear,
                monthDays = monthDays,
                selectedDayTransactions = selectedDayTransactions.map { transaction ->
                    transaction.toTransactionCardItemUi(
                        currencyId = currentCurrencyId,
                        amountFormatPreferences = currentAmountFormatPreferences,
                        dateFormatPattern = "dd MMM",
                        timeFormat = currentTimeFormat,
                        paymentTypeName = paymentTypeNames[transaction.paymentTypeId].orEmpty(),
                        categories = currentCategories,
                        fallbackCategoryName = application.getString(R.string.label_other)
                    )
                },
                selectedDayExpenseLabel = UiText.res(R.string.label_expense_with_val, formatConfiguredCurrency(-selectedDayExpenseTotal, signed = true, currencyId = currentCurrencyId, amountFormatPreferences = currentAmountFormatPreferences)),
                selectedDayIncomeLabel = UiText.res(R.string.label_income_with_val, formatConfiguredCurrency(selectedDayIncomeTotal, signed = true, currencyId = currentCurrencyId, amountFormatPreferences = currentAmountFormatPreferences)),
                selectedDayTitle = formatDate(safeSelectedDate, currentDateFormatPattern),
                emptyTransactionsMessage = UiText.res(R.string.msg_no_calendar_entries, formatDate(safeSelectedDate, currentDateFormatPattern)),
                yearSummaries = yearSummaries,
                yearlyIncomeLabel = formatConfiguredCurrency(yearlyIncome, currencyId = currentCurrencyId, amountFormatPreferences = currentAmountFormatPreferences),
                yearlyExpenseLabel = formatConfiguredCurrency(yearlyExpense, currencyId = currentCurrencyId, amountFormatPreferences = currentAmountFormatPreferences),
                calendarYearRange = calendarYearRange,
                customizationSettings = currentCustomizationSettings
            )
        }
    }
}

/**
 * Pure decision logic behind [CalendarViewModel.refreshToday] — extracted so it
 * can be unit-tested without an Android Application (project convention).
 * Re-anchors the selection to the new today only when the user was sitting on
 * the previous today; otherwise the chosen day/month is preserved.
 */
internal data class TodayRefreshResult(
    val todayDate: Long,
    val todayMonthStart: Long,
    val displayedMonthStart: Long,
    val selectedDate: Long,
    val changed: Boolean
)

internal fun computeTodayRefresh(
    currentTodayDate: Long,
    currentTodayMonthStart: Long,
    selectedDate: Long,
    displayedMonthStart: Long,
    newToday: Long
): TodayRefreshResult {
    if (newToday == currentTodayDate) {
        return TodayRefreshResult(
            todayDate = currentTodayDate,
            todayMonthStart = currentTodayMonthStart,
            displayedMonthStart = displayedMonthStart,
            selectedDate = selectedDate,
            changed = false
        )
    }
    val newTodayMonthStart = startOfMonth(newToday)
    val wasOnToday = selectedDate == currentTodayDate
    return if (wasOnToday) {
        TodayRefreshResult(
            todayDate = newToday,
            todayMonthStart = newTodayMonthStart,
            displayedMonthStart = newTodayMonthStart,
            selectedDate = newToday,
            changed = true
        )
    } else {
        TodayRefreshResult(
            todayDate = newToday,
            todayMonthStart = newTodayMonthStart,
            displayedMonthStart = displayedMonthStart,
            selectedDate = selectedDate,
            changed = true
        )
    }
}

private fun buildCalendarYearRange(
    transactions: List<Transaction>,
    todayDate: Long
): IntRange {
    val transactionYears = transactions.map { getField(it.createdAt, Calendar.YEAR) }
    val todayYear = getField(todayDate, Calendar.YEAR)
    val minYear = minOf(transactionYears.minOrNull() ?: todayYear, todayYear) - 2
    val maxYear = maxOf(transactionYears.maxOrNull() ?: todayYear, todayYear) + 2
    return minYear..maxYear
}

private fun buildMonthGrid(
    monthStart: Long,
    transactions: List<Transaction>
): List<CalendarDayUi> {
    val dayTransactionMap = transactions.groupBy { startOfDay(it.createdAt) }
    val calendar = Calendar.getInstance().apply { timeInMillis = monthStart }
    val leadingDays = mondayFirstOffset(calendar.get(Calendar.DAY_OF_WEEK))
    val daysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val previousMonthStart = addMonths(monthStart, -1)
    val previousMonthDays = Calendar.getInstance().apply { timeInMillis = previousMonthStart }
        .getActualMaximum(Calendar.DAY_OF_MONTH)
    val result = mutableListOf<CalendarDayUi>()

    if (leadingDays > 0) {
        for (day in (previousMonthDays - leadingDays + 1)..previousMonthDays) {
            val timestamp = createDate(
                getField(previousMonthStart, Calendar.YEAR),
                getField(previousMonthStart, Calendar.MONTH),
                day
            )
            val dayTransactions = dayTransactionMap[startOfDay(timestamp)].orEmpty()
            result += CalendarDayUi(
                timestamp = timestamp,
                dayNumber = day,
                isCurrentMonth = false,
                hasIncome = dayTransactions.any { it.transactionTypeId == 1 },
                hasExpense = dayTransactions.any { it.transactionTypeId != 1 }
            )
        }
    }

    for (day in 1..daysInCurrentMonth) {
        val timestamp = createDate(
            getField(monthStart, Calendar.YEAR),
            getField(monthStart, Calendar.MONTH),
            day
        )
        val dayTransactions = dayTransactionMap[startOfDay(timestamp)].orEmpty()
        result += CalendarDayUi(
            timestamp = timestamp,
            dayNumber = day,
            isCurrentMonth = true,
            hasIncome = dayTransactions.any { it.transactionTypeId == 1 },
            hasExpense = dayTransactions.any { it.transactionTypeId != 1 }
        )
    }

    val trailingDays = (7 - (result.size % 7)) % 7
    val nextMonthStart = addMonths(monthStart, 1)

    for (day in 1..trailingDays) {
        val timestamp = createDate(
            getField(nextMonthStart, Calendar.YEAR),
            getField(nextMonthStart, Calendar.MONTH),
            day
        )
        val dayTransactions = dayTransactionMap[startOfDay(timestamp)].orEmpty()
        result += CalendarDayUi(
            timestamp = timestamp,
            dayNumber = day,
            isCurrentMonth = false,
            hasIncome = dayTransactions.any { it.transactionTypeId == 1 },
            hasExpense = dayTransactions.any { it.transactionTypeId != 1 }
        )
    }

    return result
}

private fun buildYearSummaries(
    year: Int,
    transactions: List<Transaction>,
    latestTransactionDay: Long,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences
): List<CalendarMonthFinancialSummaryUi> {
    val monthLabels = listOf(
        R.string.month_jan_short, R.string.month_feb_short, R.string.month_mar_short,
        R.string.month_apr_short, R.string.month_may_short, R.string.month_jun_short,
        R.string.month_jul_short, R.string.month_aug_short, R.string.month_sep_short,
        R.string.month_oct_short, R.string.month_nov_short, R.string.month_dec_short
    )
    val latestYear = getField(latestTransactionDay, Calendar.YEAR)
    val latestMonth = getField(latestTransactionDay, Calendar.MONTH)
    val completedMonths = max(1, if (year == latestYear) latestMonth + 1 else 12)
    val actualMonths = transactions
        .filter { getField(it.createdAt, Calendar.YEAR) == year }
        .groupBy { getField(it.createdAt, Calendar.MONTH) }
    val averageNet = (0 until completedMonths).sumOf { month ->
        actualMonths[month].orEmpty().sumOf(::signedAmount)
    } / completedMonths.toDouble()

    return monthLabels.mapIndexed { monthIndex, monthResId ->
        val monthTransactions = actualMonths[monthIndex].orEmpty()
        val income = monthTransactions.filter { it.transactionTypeId == 1 }.sumOf { it.amount }
        val expense = monthTransactions.filter { it.transactionTypeId != 1 }.sumOf { it.amount }
        val isFutureProjection = year >= latestYear && monthTransactions.isEmpty() &&
            (year > latestYear || monthIndex > latestMonth)
        val renderedIncome = if (isFutureProjection) 0.0 else income
        val renderedExpense = if (isFutureProjection) 0.0 else expense
        val renderedNet = if (isFutureProjection) averageNet else income - expense

        CalendarMonthFinancialSummaryUi(
            monthIndex = monthIndex,
            label = UiText.res(monthResId),
            income = renderedIncome,
            expense = renderedExpense,
            net = renderedNet,
            isProjection = isFutureProjection,
            incomeLabel = formatConfiguredCurrency(
                renderedIncome,
                signed = true,
                currencyId = currencyId,
                amountFormatPreferences = amountFormatPreferences
            ),
            expenseLabel = formatConfiguredCurrency(
                -renderedExpense,
                signed = true,
                currencyId = currencyId,
                amountFormatPreferences = amountFormatPreferences
            ),
            netLabel = formatConfiguredCurrency(
                renderedNet,
                signed = true,
                currencyId = currencyId,
                amountFormatPreferences = amountFormatPreferences
            )
        )
    }
}

private fun defaultSelectedDateForMonth(
    monthStart: Long,
    transactions: List<Transaction>,
    todayDate: Long
): Long {
    val targetYear = getField(monthStart, Calendar.YEAR)
    val targetMonth = getField(monthStart, Calendar.MONTH)
    val monthTransactions = transactions
        .filter {
            getField(it.createdAt, Calendar.YEAR) == targetYear &&
                getField(it.createdAt, Calendar.MONTH) == targetMonth
        }
        .sortedBy { it.createdAt }

    return when {
        monthTransactions.isNotEmpty() -> startOfDay(monthTransactions.first().createdAt)
        startOfMonth(todayDate) == startOfMonth(monthStart) -> todayDate
        else -> startOfMonth(monthStart)
    }
}

private fun alignDateToMonth(
    sourceDate: Long,
    targetMonthStart: Long
): Long {
    val desiredDay = getField(sourceDate, Calendar.DAY_OF_MONTH)
    val targetCalendar = Calendar.getInstance().apply { timeInMillis = targetMonthStart }
    val lastDay = targetCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    return createDate(
        targetCalendar.get(Calendar.YEAR),
        targetCalendar.get(Calendar.MONTH),
        desiredDay.coerceAtMost(lastDay)
    )
}

private fun startOfDay(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun startOfMonth(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun addMonths(timestamp: Long, months: Int): Long {
    return Calendar.getInstance().apply {
        timeInMillis = startOfMonth(timestamp)
        add(Calendar.MONTH, months)
    }.timeInMillis
}

private fun createDate(
    year: Int,
    month: Int,
    dayOfMonth: Int
): Long {
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun isSameDay(first: Long, second: Long): Boolean {
    return startOfDay(first) == startOfDay(second)
}

private fun getField(timestamp: Long, field: Int): Int {
    return Calendar.getInstance().apply { timeInMillis = timestamp }.get(field)
}

private fun mondayFirstOffset(dayOfWeek: Int): Int {
    return (dayOfWeek + 5) % 7
}

private fun signedAmount(transaction: Transaction): Double {
    return if (transaction.transactionTypeId == 1) transaction.amount else -transaction.amount
}

private fun formatConfiguredCurrency(
    amount: Double,
    signed: Boolean = false,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences
): String {
    val prefix = when {
        signed && amount > 0 -> "+"
        amount < 0 -> "-"
        else -> ""
    }
    return formatCurrencyValue(
        amount = amount,
        currencyId = currencyId,
        amountFormatPreferences = amountFormatPreferences,
        prefix = prefix
    )
}


fun calendarMonthTitle(monthStart: Long): String {
    val formatted = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(monthStart))
    return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
