package com.mkn0079.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.domain.repository.BudgetRepository
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.Budget
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.RecurringFrequency
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import com.mkn0079.expensetracker.utils.formatCurrencyValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min
enum class BudgetPeriodFilter {
    ThisMonth,
    LastMonth,
    CustomMonth
}

enum class BudgetAccent {
    Primary,
    Warning,
    Overspent,
    Disabled,
    Daily,
    Yearly
}

@Immutable
data class BudgetSummaryUi(
    val monthLabel: String = monthFormatter.format(Date(System.currentTimeMillis())),
    val totalBudgetAmount: Double = 0.0,
    val spentAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val totalBudgetLabel: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val spentLabel: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val remainingLabel: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val usageFraction: Float = 0f,
    val usageLabel: String = "NO BUDGET",
    val limitLabel: String = "ADD A BUDGET"
)

@Immutable
data class BudgetCategoryBudgetUi(
    val id: String,
    val categoryId: Int,
    val title: String,
    val summaryLabel: String,
    val statusValueLabel: String,
    val statusCaption: String,
    val totalCaption: String,
    val progressFraction: Float,
    val spentAmount: Double,
    val limitAmount: Double,
    val icon: ImageVector,
    val accent: BudgetAccent
)

@Immutable
data class BudgetRecurringExpenseUi(
    val id: String,
    val transactionId: String,
    val title: String,
    val amountLabel: String,
    val categoryLabel: String,
    val frequency: RecurringFrequency,
    val frequencyLabel: String,
    val repeatCount: Int,
    val currentInstallment: Int,
    val totalInstallments: Int,
    val sourceDateLabel: String,
    val dueLabel: String,
    val dueAmountLabel: String,
    val icon: ImageVector,
    val accent: BudgetAccent,
    val nextDueAt: Long,
    val isEnabled: Boolean
)

@Immutable
data class BudgetInsightUi(
    val title: String,
    val body: String,
    val supportingLabel: String,
    val accent: BudgetAccent
)

@Immutable
data class BudgetScreenUiState(
    val selectedPeriod: BudgetPeriodFilter = BudgetPeriodFilter.ThisMonth,
    val summary: BudgetSummaryUi = BudgetSummaryUi(),
    val categoryBudgets: List<BudgetCategoryBudgetUi> = emptyList(),
    val recurringExpenses: List<BudgetRecurringExpenseUi> = emptyList(),
    val recurringDueItems: List<BudgetRecurringExpenseUi> = emptyList(),
    val insight: BudgetInsightUi = BudgetInsightUi(
        title = "Budget Insight",
        body = "Start tracking how your spending is pacing against your monthly limits.",
        supportingLabel = "WAITING FOR BUDGETS",
        accent = BudgetAccent.Primary
    ),
    val emptyCategoryMessage: String? = null,
    val emptyRecurringMessage: String? = null,
    val customMonthStart: Long = startOfMonth(System.currentTimeMillis())
)

private data class BudgetEntry(
    val id: String,
    val categoryId: Int,
    val monthStart: Long,
    val limitAmount: Double
)

private data class RecurringEntry(
    val id: String,
    val transactionId: String,
    val frequency: RecurringFrequency,
    val repeatCount: Int,
    val isEnabled: Boolean
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private var currentTransactions: List<Transaction> = emptyList()
    private var currentCategories: Map<Int, CategoryType> = emptyMap()
    private var currentCurrencyId: Int = DEFAULT_CURRENCY_ID
    private var currentAmountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences
    private var currentRecurringEntries: List<RecurringEntry> = emptyList()
    private var anchorMonthStart: Long = startOfMonth(System.currentTimeMillis())
    private var selectedPeriod: BudgetPeriodFilter = BudgetPeriodFilter.ThisMonth
    private var customMonthStart: Long = anchorMonthStart
    private var budgetEntries: List<BudgetEntry> = emptyList()

    private val _uiState = MutableStateFlow(BudgetScreenUiState())
    val uiState: StateFlow<BudgetScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            budgetRepository.observeActiveBudgets().collect { budgets ->
                budgetEntries = budgets.map(Budget::toBudgetEntry)
                rebuildUiState()
            }
        }
        rebuildUiState()
    }

    fun updateInputs(
        transactions: List<Transaction>,
        categories: List<CategoryType>,
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences,
        recurringRules: List<RecurringTransactionRule>
    ) {
        currentTransactions = transactions
        currentCategories = categories.associateBy { it.id }
        currentCurrencyId = currencyId
        currentAmountFormatPreferences = amountFormatPreferences
        currentRecurringEntries = recurringRules.map { rule ->
            RecurringEntry(
                id = rule.id,
                transactionId = rule.transactionId,
                frequency = rule.frequency,
                repeatCount = rule.repeatCount,
                isEnabled = rule.isEnabled
            )
        }
        anchorMonthStart = resolveAnchorMonthStart(transactions)
        if (customMonthStart == 0L) {
            customMonthStart = anchorMonthStart
        }
        rebuildUiState()
    }

    fun selectPeriod(period: BudgetPeriodFilter) {
        selectedPeriod = period
        rebuildUiState()
    }

    fun selectCustomMonth(timestamp: Long) {
        customMonthStart = startOfMonth(timestamp)
        selectedPeriod = BudgetPeriodFilter.CustomMonth
        rebuildUiState()
    }

    fun addBudget(categoryId: Int, limitAmount: Double) {
        upsertBudget(
            budgetId = null,
            categoryId = categoryId,
            limitAmount = limitAmount
        )
    }

    fun updateBudget(
        budgetId: String,
        categoryId: Int,
        limitAmount: Double
    ) {
        upsertBudget(
            budgetId = budgetId,
            categoryId = categoryId,
            limitAmount = limitAmount
        )
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budgetId)
        }
    }

    private fun upsertBudget(
        budgetId: String?,
        categoryId: Int,
        limitAmount: Double
    ) {
        if (limitAmount <= 0.0) return

        viewModelScope.launch {
            val monthStart = currentSelectedMonthStart()
            val conflictingBudget = budgetEntries.firstOrNull {
                it.monthStart == monthStart &&
                    it.categoryId == categoryId &&
                    it.id != budgetId
            }
            budgetRepository.upsertBudget(
                Budget(
                    id = conflictingBudget?.id ?: budgetId.orEmpty(),
                    categoryId = categoryId,
                    monthStart = monthStart,
                    limitAmount = limitAmount
                )
            )
        }
    }

    private fun currentSelectedMonthStart(): Long {
        return when (selectedPeriod) {
            BudgetPeriodFilter.ThisMonth -> anchorMonthStart
            BudgetPeriodFilter.LastMonth -> addMonths(anchorMonthStart, -1)
            BudgetPeriodFilter.CustomMonth -> customMonthStart
        }
    }

    private fun rebuildUiState() {
        val selectedMonthStart = currentSelectedMonthStart()
        val selectedMonthEnd = endOfMonth(selectedMonthStart)
        val expenseTransactions = currentTransactions.filter {
            it.transactionTypeId != 1 && it.createdAt in selectedMonthStart..selectedMonthEnd
        }
        val monthlyBudgets = budgetEntries.filter { it.monthStart == selectedMonthStart }
        val categoryBudgets = buildCategoryBudgets(
            monthlyBudgets = monthlyBudgets,
            selectedMonthExpenses = expenseTransactions,
            categories = currentCategories,
            currencyId = currentCurrencyId,
            amountFormatPreferences = currentAmountFormatPreferences
        )
        val allRecurring = buildRecurringExpenses(
            recurringEntries = currentRecurringEntries,
            transactions = currentTransactions,
            categories = currentCategories,
            currencyId = currentCurrencyId,
            amountFormatPreferences = currentAmountFormatPreferences
        )
        val activeRecurring = allRecurring.filter { it.currentInstallment <= it.totalInstallments }

        val summary = buildSummary(
            monthStart = selectedMonthStart,
            expenseTransactions = expenseTransactions,
            categoryBudgets = categoryBudgets,
            currencyId = currentCurrencyId,
            amountFormatPreferences = currentAmountFormatPreferences
        )
        val dueItems = activeRecurring
            .filter { it.isEnabled }
            .sortedBy { it.nextDueAt }
            .take(3)
        val insight = buildInsight(
            summary = summary,
            categoryBudgets = categoryBudgets,
            recurringExpenses = activeRecurring.filter { it.isEnabled },
            currencyId = currentCurrencyId,
            amountFormatPreferences = currentAmountFormatPreferences
        )

        _uiState.update {
            it.copy(
                selectedPeriod = selectedPeriod,
                summary = summary,
                categoryBudgets = categoryBudgets,
                recurringExpenses = activeRecurring,
                recurringDueItems = dueItems,
                insight = insight,
                emptyCategoryMessage = if (monthlyBudgets.isEmpty()) {
                    "No budgets added for ${monthFormatter.format(Date(selectedMonthStart))} yet. Tap ADD NEW BUDGET to start tracking this month."
                } else {
                    null
                },
                emptyRecurringMessage = if (activeRecurring.isEmpty()) {
                    "No recurring expenses added yet. Tap ADD RECURRING and choose an existing expense transaction."
                } else {
                    null
                },
                customMonthStart = customMonthStart
            )
        }
    }
}

private fun buildSummary(
    monthStart: Long,
    expenseTransactions: List<Transaction>,
    categoryBudgets: List<BudgetCategoryBudgetUi>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences
): BudgetSummaryUi {
    val spentAmount = expenseTransactions.sumOf { it.amount }
    val totalBudgetAmount = categoryBudgets.sumOf { it.limitAmount }
    val remainingAmount = totalBudgetAmount - spentAmount
    val usageFraction = if (totalBudgetAmount <= 0.0) {
        0f
    } else {
        (spentAmount / totalBudgetAmount).toFloat().coerceIn(0f, 1f)
    }
    val usagePercent = if (totalBudgetAmount <= 0.0) {
        0
    } else {
        ((spentAmount / totalBudgetAmount) * 100).toInt().coerceAtLeast(0)
    }
    val remainingLabel = when {
        totalBudgetAmount <= 0.0 -> formatCurrencyValue(0.0, currencyId, amountFormatPreferences)
        remainingAmount >= 0.0 -> formatCurrencyValue(remainingAmount, currencyId, amountFormatPreferences)
        else -> "Over ${formatCurrencyValue(abs(remainingAmount), currencyId, amountFormatPreferences)}"
    }

    return BudgetSummaryUi(
        monthLabel = monthFormatter.format(Date(monthStart)),
        totalBudgetAmount = totalBudgetAmount,
        spentAmount = spentAmount,
        remainingAmount = remainingAmount,
        totalBudgetLabel = formatCurrencyValue(totalBudgetAmount, currencyId, amountFormatPreferences),
        spentLabel = formatCurrencyValue(spentAmount, currencyId, amountFormatPreferences),
        remainingLabel = remainingLabel,
        usageFraction = usageFraction,
        usageLabel = if (totalBudgetAmount <= 0.0) "NO BUDGET" else "$usagePercent% USED",
        limitLabel = if (totalBudgetAmount <= 0.0) {
            "ADD A BUDGET"
        } else {
            "LIMIT ${formatCurrencyValue(totalBudgetAmount, currencyId, amountFormatPreferences)}"
        }
    )
}

private fun buildCategoryBudgets(
    monthlyBudgets: List<BudgetEntry>,
    selectedMonthExpenses: List<Transaction>,
    categories: Map<Int, CategoryType>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences
): List<BudgetCategoryBudgetUi> {
    return monthlyBudgets
        .mapNotNull { budgetEntry ->
            val category = categories[budgetEntry.categoryId] ?: return@mapNotNull null
            val spentAmount = selectedMonthExpenses
                .filter { it.categoryId == budgetEntry.categoryId }
                .sumOf { it.amount }
            val progress = if (budgetEntry.limitAmount <= 0.0) {
                0f
            } else {
                (spentAmount / budgetEntry.limitAmount).toFloat()
            }
            val remainingAmount = budgetEntry.limitAmount - spentAmount
            val accent = categoryAccent(progress)
            val (statusValueLabel, statusCaption, totalCaption) = when {
                spentAmount > budgetEntry.limitAmount -> Triple(
                    "${formatCurrencyValue(spentAmount - budgetEntry.limitAmount, currencyId, amountFormatPreferences)} OVER",
                    "BUDGET",
                    "EXCEEDED"
                )

                progress >= 0.85f -> Triple(
                    "${(progress * 100).toInt()}% USED",
                    "NEAR LIMIT",
                    "SPENT / LIMIT"
                )

                else -> Triple(
                    "${formatCurrencyValue(remainingAmount, currencyId, amountFormatPreferences)} LEFT",
                    "SAFE",
                    "SPENT / LIMIT"
                )
            }

            BudgetCategoryBudgetUi(
                id = budgetEntry.id,
                categoryId = budgetEntry.categoryId,
                title = category.name,
                summaryLabel = "${formatCurrencyValue(spentAmount, currencyId, amountFormatPreferences)} / ${formatCurrencyValue(budgetEntry.limitAmount, currencyId, amountFormatPreferences)}",
                statusValueLabel = statusValueLabel,
                statusCaption = statusCaption,
                totalCaption = totalCaption,
                progressFraction = progress.coerceIn(0f, 1f),
                spentAmount = spentAmount,
                limitAmount = budgetEntry.limitAmount,
                icon = category.icon,
                accent = accent
            )
        }
        .sortedWith(
            compareByDescending<BudgetCategoryBudgetUi> { it.spentAmount }
                .thenBy { it.title }
        )
}

private fun buildRecurringExpenses(
    recurringEntries: List<RecurringEntry>,
    transactions: List<Transaction>,
    categories: Map<Int, CategoryType>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences
): List<BudgetRecurringExpenseUi> {
    val referenceTime = System.currentTimeMillis()
    return recurringEntries
        .mapNotNull { recurringEntry ->
            val transaction = transactions.firstOrNull {
                it.id == recurringEntry.transactionId && it.transactionTypeId != 1
            } ?: return@mapNotNull null
            val category = categories[transaction.categoryId] ?: return@mapNotNull null
            val (nextDueAt, nextIndex) = calculateNextInstallmentInfo(
                baseTimestamp = transaction.createdAt,
                frequency = recurringEntry.frequency,
                referenceTime = referenceTime
            )
            val accent = recurringAccent(
                isEnabled = recurringEntry.isEnabled,
                frequency = recurringEntry.frequency
            )

            BudgetRecurringExpenseUi(
                id = recurringEntry.id,
                transactionId = transaction.id,
                title = transaction.note.ifBlank { category.name },
                amountLabel = "${formatCurrencyValue(transaction.amount, currencyId, amountFormatPreferences)} / ${recurringEntry.frequency.periodUnit}",
                categoryLabel = category.name.uppercase(Locale.getDefault()),
                frequency = recurringEntry.frequency,
                frequencyLabel = recurringEntry.frequency.label.uppercase(Locale.getDefault()),
                repeatCount = recurringEntry.repeatCount,
                currentInstallment = nextIndex,
                totalInstallments = recurringEntry.repeatCount,
                sourceDateLabel = "Started ${recurringDateFormatter.format(Date(transaction.createdAt))}",
                dueLabel = dueLabelFor(nextDueAt, referenceTime),
                dueAmountLabel = formatCurrencyValue(transaction.amount, currencyId, amountFormatPreferences),
                icon = category.icon,
                accent = accent,
                nextDueAt = nextDueAt,
                isEnabled = recurringEntry.isEnabled
            )
        }
        .sortedWith(
            compareBy<BudgetRecurringExpenseUi> { !it.isEnabled }
                .thenBy { it.repeatCount <= 0 }
                .thenBy { it.nextDueAt }
                .thenBy { it.title.lowercase(Locale.getDefault()) }
        )
}

private fun buildInsight(
    summary: BudgetSummaryUi,
    categoryBudgets: List<BudgetCategoryBudgetUi>,
    recurringExpenses: List<BudgetRecurringExpenseUi>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences
): BudgetInsightUi {
    if (categoryBudgets.isEmpty() && recurringExpenses.isEmpty()) {
        return BudgetInsightUi(
            title = "Budget Insight",
            body = "You have not added any budgets or recurring expenses yet. Start with a monthly budget or mark one transaction as weekly, monthly, or yearly.",
            supportingLabel = "",
            accent = BudgetAccent.Primary
        )
    }

    val overspentCategory = categoryBudgets.firstOrNull { it.spentAmount > it.limitAmount }
    val highestSpendCategory = categoryBudgets.maxByOrNull { it.spentAmount }
    val nextRecurring = recurringExpenses.minByOrNull { it.nextDueAt }
    val recurringMonthlyEquivalent = recurringExpenses.sumOf { recurring ->
        val recurringAmount = parseAmountValue(
            recurring.dueAmountLabel,
            currencyId,
            amountFormatPreferences
        )
        when (recurring.frequency) {
            RecurringFrequency.Daily -> recurringAmount * 30.0
            RecurringFrequency.Weekly -> recurringAmount * 4.0
            RecurringFrequency.Monthly -> recurringAmount
            RecurringFrequency.Yearly -> recurringAmount / 12.0
        }
    }

    return when {
        overspentCategory != null -> {
            BudgetInsightUi(
                title = "Budget Insight",
                body = "${overspentCategory.title} is over budget by ${formatCurrencyValue(overspentCategory.spentAmount - overspentCategory.limitAmount, currencyId, amountFormatPreferences)}. Tightening that category first will give you the quickest recovery.",
                supportingLabel = "OVER BUDGET",
                accent = BudgetAccent.Overspent
            )
        }

        recurringExpenses.isNotEmpty() && nextRecurring != null -> {
            BudgetInsightUi(
                title = "Budget Insight",
                body = "You have ${recurringExpenses.size} recurring expense${if (recurringExpenses.size == 1) "" else "s"} tracked. ${nextRecurring.title} is ${nextRecurring.dueLabel.lowercase(Locale.getDefault())}, and your recurring commitments average about ${formatCurrencyValue(recurringMonthlyEquivalent, currencyId, amountFormatPreferences)} per month.",
                supportingLabel = "RECURRING WATCH",
                accent = nextRecurring.accent
            )
        }

        summary.totalBudgetAmount > 0.0 && summary.usageFraction >= 0.85f -> {
            BudgetInsightUi(
                title = "Budget Insight",
                body = "You have used ${summary.usageLabel.lowercase(Locale.getDefault())} for ${summary.monthLabel}. ${highestSpendCategory?.title ?: "Your top category"} is carrying the most spend, so trimming there will protect your remaining ${summary.remainingLabel}.",
                supportingLabel = "WATCH YOUR RUNWAY",
                accent = BudgetAccent.Warning
            )
        }

        else -> {
            BudgetInsightUi(
                title = "Budget Insight",
                body = "Your budget pacing looks steady for ${summary.monthLabel}. You still have ${summary.remainingLabel} available, with ${highestSpendCategory?.title ?: "your biggest category"} leading spend so far.",
                supportingLabel = "BUDGET ON TRACK",
                accent = BudgetAccent.Primary
            )
        }
    }
}

private fun recurringAccent(
    isEnabled: Boolean,
    frequency: RecurringFrequency
): BudgetAccent {
    if (!isEnabled) return BudgetAccent.Disabled
    return when (frequency) {
        RecurringFrequency.Daily -> BudgetAccent.Daily
        RecurringFrequency.Weekly -> BudgetAccent.Warning
        RecurringFrequency.Monthly -> BudgetAccent.Primary
        RecurringFrequency.Yearly -> BudgetAccent.Yearly
    }
}

private fun categoryAccent(progress: Float): BudgetAccent {
    return when {
        progress > 1f -> BudgetAccent.Overspent
        progress >= 0.85f -> BudgetAccent.Warning
        else -> BudgetAccent.Primary
    }
}

private fun parseAmountValue(
    formattedValue: String,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences
): Double {
    val currencySymbol = formatCurrencyValue(0.0, currencyId, amountFormatPreferences)
        .replace("0", "")
        .trim()
    return formattedValue
        .replace(currencySymbol, "")
        .replace(",", "")
        .replace("Over", "")
        .trim()
        .toDoubleOrNull()
        ?: 0.0
}

private fun resolveAnchorMonthStart(transactions: List<Transaction>): Long {
    val expenseTransactions = transactions.filter { it.transactionTypeId != 1 }
    if (expenseTransactions.isEmpty()) {
        return startOfMonth(System.currentTimeMillis())
    }

    val currentMonthStart = startOfMonth(System.currentTimeMillis())
    val currentMonthEnd = endOfMonth(currentMonthStart)
    return if (expenseTransactions.any { it.createdAt in currentMonthStart..currentMonthEnd }) {
        currentMonthStart
    } else {
        startOfMonth(expenseTransactions.maxOf { it.createdAt })
    }
}

private fun calculateNextInstallmentInfo(
    baseTimestamp: Long,
    frequency: RecurringFrequency,
    referenceTime: Long
): Pair<Long, Int> {
    val referenceCalendar = Calendar.getInstance().apply {
        timeInMillis = referenceTime
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val baseCalendar = Calendar.getInstance().apply {
        timeInMillis = baseTimestamp
    }
    
    val nextCalendar = Calendar.getInstance().apply {
        timeInMillis = baseTimestamp
    }
    
    var index = 1
    
    // Catch up to reference time
    while (nextCalendar.timeInMillis <= referenceTime) {
        when (frequency) {
            RecurringFrequency.Daily -> {
                nextCalendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            RecurringFrequency.Weekly -> {
                nextCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
            RecurringFrequency.Monthly -> {
                val preferredDay = baseCalendar.get(Calendar.DAY_OF_MONTH).coerceIn(1, 28)
                nextCalendar.add(Calendar.MONTH, 1)
                val maxDay = nextCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                nextCalendar.set(Calendar.DAY_OF_MONTH, min(preferredDay, maxDay))
            }
            RecurringFrequency.Yearly -> {
                nextCalendar.add(Calendar.YEAR, 1)
                val preferredDay = baseCalendar.get(Calendar.DAY_OF_MONTH).coerceIn(1, 28)
                val maxDay = nextCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                nextCalendar.set(Calendar.DAY_OF_MONTH, min(preferredDay, maxDay))
            }
        }
        index++
    }
    
    return nextCalendar.timeInMillis to index
}

private fun dueLabelFor(
    nextDueAt: Long,
    referenceTime: Long
): String {
    val diffDays = ((startOfDay(nextDueAt) - startOfDay(referenceTime)) / DAY_IN_MILLIS).toInt()
    return when {
        diffDays <= 0 -> "DUE TODAY"
        diffDays == 1 -> "DUE TOMORROW"
        diffDays in 2..6 -> "DUE IN $diffDays DAYS"
        else -> "NEXT ${dueFormatter.format(Date(nextDueAt)).uppercase(Locale.getDefault())}"
    }
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

private fun endOfMonth(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}

private fun addMonths(timestamp: Long, months: Int): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        add(Calendar.MONTH, months)
    }.let { startOfMonth(it.timeInMillis) }
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

private fun Budget.toBudgetEntry(): BudgetEntry {
    return BudgetEntry(
        id = id,
        categoryId = categoryId,
        monthStart = monthStart,
        limitAmount = limitAmount
    )
}

private const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
private val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
private val recurringDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
private val dueFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
