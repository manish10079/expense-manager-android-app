package com.mknlabs.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.domain.repository.BudgetRepository
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.Budget
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.R
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

enum class BudgetTab {
    Budgets,
    Recurring
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
    val remainingLabel: UiText = UiText.dynamic(""),
    val dailyAllowanceLabel: UiText? = null,
    val usageFraction: Float = 0f,
    val usageLabel: UiText = UiText.dynamic(""),
    val limitLabel: UiText = UiText.dynamic("")
)

@Immutable
data class BudgetCategoryBudgetUi(
    val id: String,
    val categoryId: Int,
    val title: String,
    val summaryLabel: String,
    val statusValueLabel: UiText,
    val statusCaption: UiText,
    val totalCaption: UiText,
    val progressFraction: Float,
    val spentAmount: Double,
    val limitAmount: Double,
    val icon: ImageVector,
    val accent: BudgetAccent,
    val canEdit: Boolean = true,
    val remainingEdits: Int? = null,
    val editCount: Int = 0
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
    val sourceDateLabel: UiText,
    val dueLabel: UiText,
    val dueAmountLabel: String,
    val icon: ImageVector,
    val accent: BudgetAccent,
    val nextDueAt: Long,
    val isEnabled: Boolean,
    val notificationsEnabled: Boolean = true
)

@Immutable
data class BudgetAndRecurringScreenUiState(
    val selectedTab: BudgetTab = BudgetTab.Budgets,
    val selectedPeriod: BudgetPeriodFilter = BudgetPeriodFilter.ThisMonth,
    val summary: BudgetSummaryUi = BudgetSummaryUi(),
    val categoryBudgets: List<BudgetCategoryBudgetUi> = emptyList(),
    val recurringExpenses: List<BudgetRecurringExpenseUi> = emptyList(),
    val emptyCategoryMessage: UiText? = null,
    val emptyRecurringMessage: UiText? = null,
    val customMonthStart: Long = startOfMonth(System.currentTimeMillis()),
    val isMonthLocked: Boolean = false,
    val canAddBudget: Boolean = true
)


private data class BudgetEntry(
    val id: String,
    val categoryId: Int,
    val monthStart: Long,
    val limitAmount: Double,
    val editCount: Int
)

private data class RecurringEntry(
    val id: String,
    val transactionId: String,
    val frequency: RecurringFrequency,
    val repeatCount: Int,
    val isEnabled: Boolean,
    val notificationsEnabled: Boolean = true,
    val nextRunAt: Long = 0L
)

@HiltViewModel
class BudgetAndRecurringViewModel @Inject constructor(
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

    private val _uiState = MutableStateFlow(BudgetAndRecurringScreenUiState())
    val uiState: StateFlow<BudgetAndRecurringScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            budgetRepository.observeActiveBudgets().collect { budgets ->
                budgetEntries = budgets.map(Budget::toBudgetEntry)
                rebuildUiState()
            }
        }
        rebuildUiState()
    }

    private var currentMonthStartDay: Int = 1

    fun updateInputs(
        transactions: List<Transaction>,
        categories: List<CategoryType>,
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences,
        recurringRules: List<RecurringTransactionRule>,
        monthStartDay: Int = 1
    ) {
        currentTransactions = transactions
        currentCategories = categories.associateBy { it.id }
        currentCurrencyId = currencyId
        currentAmountFormatPreferences = amountFormatPreferences
        currentMonthStartDay = monthStartDay
        currentRecurringEntries = recurringRules.map { rule ->
            RecurringEntry(
                id = rule.id,
                transactionId = rule.transactionId,
                frequency = rule.frequency,
                repeatCount = rule.repeatCount,
                isEnabled = rule.isEnabled,
                notificationsEnabled = rule.notificationsEnabled,
                nextRunAt = rule.nextRunAt
            )
        }
        anchorMonthStart = resolveAnchorMonthStart(transactions, currentMonthStartDay)
        if (customMonthStart == 0L) {
            customMonthStart = anchorMonthStart
        }
        rebuildUiState()
    }

    fun selectPeriod(period: BudgetPeriodFilter) {
        selectedPeriod = period
        rebuildUiState()
    }

    fun selectTab(tab: BudgetTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectCustomMonth(timestamp: Long) {
        customMonthStart = startOfMonth(timestamp, currentMonthStartDay)
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
            val currentMonthStart = startOfMonth(System.currentTimeMillis(), currentMonthStartDay)
            val prevMonthStart = addMonths(currentMonthStart, -1)

            val existingBudget = budgetEntries.firstOrNull { it.id == budgetId }
            val conflictingBudget = budgetEntries.firstOrNull {
                it.monthStart == monthStart &&
                    it.categoryId == categoryId &&
                    it.id != budgetId
            }

            // Logic:
            // 1. Current/Future Month: Unlimited edits (editCount stays 0 or doesn't matter)
            // 2. Previous Month: Limit 3 edits.
            // 3. Older than Previous: No edits (should be blocked by UI, but guard here)

            if (monthStart < prevMonthStart) {
                // Strictly no edits for older months
                return@launch
            }

            var newEditCount = existingBudget?.editCount ?: 0
            if (monthStart == prevMonthStart) {
                if (newEditCount >= 3) return@launch
                newEditCount++
            }

            // If we are changing category and it conflicts with an existing budget
            if (budgetId != null && conflictingBudget != null) {
                budgetRepository.deleteBudget(budgetId)
            }

            budgetRepository.upsertBudget(
                Budget(
                    id = conflictingBudget?.id ?: budgetId.orEmpty(),
                    categoryId = categoryId,
                    monthStart = monthStart,
                    limitAmount = limitAmount,
                    editCount = if (monthStart >= currentMonthStart) 0 else newEditCount
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
        val currentMonthStart = startOfMonth(System.currentTimeMillis(), currentMonthStartDay)
        val prevMonthStart = addMonths(currentMonthStart, -1)

        val isMonthLocked = selectedMonthStart < prevMonthStart
        val canAddBudget = when {
            selectedMonthStart >= currentMonthStart -> true
            selectedMonthStart == prevMonthStart -> {
                // If any budget in the previous month has already reached 3 edits, 
                // we might want to block adding NEW budgets too to keep it consistent.
                // Or we can say "adding a new budget" is itself an edit.
                // Let's check the existing budget with highest edit count for this month.
                val maxEdits = budgetEntries.filter { it.monthStart == selectedMonthStart }
                    .maxOfOrNull { it.editCount } ?: 0
                maxEdits < 3
            }
            else -> false
        }

        val selectedMonthEnd = endOfMonth(selectedMonthStart, currentMonthStartDay)
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
            amountFormatPreferences = currentAmountFormatPreferences,
            monthStartDay = currentMonthStartDay
        )
        _uiState.update {
            it.copy(
                selectedPeriod = selectedPeriod,
                summary = summary,
                categoryBudgets = categoryBudgets,
                recurringExpenses = activeRecurring,
                emptyCategoryMessage = if (monthlyBudgets.isEmpty()) {
                    val formattedMonth = monthFormatter.format(Date(selectedMonthStart))
                    when {
                        isMonthLocked -> UiText.res(R.string.msg_no_budgets_locked, formattedMonth)
                        !canAddBudget -> UiText.res(R.string.msg_no_budgets_limit_reached, formattedMonth)
                        else -> UiText.res(R.string.msg_no_budgets_start_tracking, formattedMonth)
                    }
                } else {
                    null
                },
                emptyRecurringMessage = if (activeRecurring.isEmpty()) {
                    UiText.res(R.string.msg_no_recurring_start_tracking)
                } else {
                    null
                },
                customMonthStart = customMonthStart,
                isMonthLocked = isMonthLocked,
                canAddBudget = canAddBudget
            )
        }
    }
}

private fun buildSummary(
    monthStart: Long,
    expenseTransactions: List<Transaction>,
    categoryBudgets: List<BudgetCategoryBudgetUi>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    monthStartDay: Int = 1
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
        totalBudgetAmount <= 0.0 -> UiText.dynamic(formatCurrencyValue(0.0, currencyId, amountFormatPreferences))
        remainingAmount >= 0.0 -> UiText.dynamic(formatCurrencyValue(remainingAmount, currencyId, amountFormatPreferences))
        else -> UiText.res(R.string.format_over_amount, formatCurrencyValue(abs(remainingAmount), currencyId, amountFormatPreferences))
    }

    // Daily Allowance Calculation
    val now = System.currentTimeMillis()
    val endOfMonth = endOfMonth(monthStart, monthStartDay)
    val dailyAllowanceLabel = if (remainingAmount > 0.0 && now < endOfMonth) {
        val daysInMonth = Calendar.getInstance().apply { timeInMillis = monthStart }.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.DAY_OF_MONTH)
        val remainingDays = (daysInMonth - currentDay + 1).coerceAtLeast(1)
        val dailyAmount = remainingAmount / remainingDays
        UiText.res(R.string.format_daily_allowance, formatCurrencyValue(dailyAmount, currencyId, amountFormatPreferences))
    } else null

    return BudgetSummaryUi(
        monthLabel = monthFormatter.format(Date(monthStart)),
        totalBudgetAmount = totalBudgetAmount,
        spentAmount = spentAmount,
        remainingAmount = remainingAmount,
        totalBudgetLabel = formatCurrencyValue(totalBudgetAmount, currencyId, amountFormatPreferences),
        spentLabel = formatCurrencyValue(spentAmount, currencyId, amountFormatPreferences),
        remainingLabel = remainingLabel,
        dailyAllowanceLabel = dailyAllowanceLabel,
        usageFraction = usageFraction,
        usageLabel = if (totalBudgetAmount <= 0.0) UiText.res(R.string.label_no_budget) else UiText.res(R.string.format_percent_used, usagePercent),
        limitLabel = if (totalBudgetAmount <= 0.0) {
            UiText.res(R.string.label_add_a_budget)
        } else {
            UiText.res(R.string.format_limit_amount, formatCurrencyValue(totalBudgetAmount, currencyId, amountFormatPreferences))
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
    val currentMonthStart = startOfMonth(System.currentTimeMillis())
    val prevMonthStart = addMonths(currentMonthStart, -1)

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
                    UiText.res(R.string.format_amount_over, formatCurrencyValue(spentAmount - budgetEntry.limitAmount, currencyId, amountFormatPreferences)),
                    UiText.res(R.string.label_budget_status_label),
                    UiText.res(R.string.label_exceeded)
                )

                progress >= 0.85f -> Triple(
                    UiText.res(R.string.format_percent_used, (progress * 100).toInt()),
                    UiText.res(R.string.label_near_limit),
                    UiText.res(R.string.label_spent_limit)
                )

                else -> Triple(
                    UiText.res(R.string.format_amount_left, formatCurrencyValue(remainingAmount, currencyId, amountFormatPreferences)),
                    UiText.res(R.string.label_safe),
                    UiText.res(R.string.label_spent_limit)
                )
            }

            // Logic:
            // 1. Current/Future: canEdit = true
            // 2. Previous: canEdit = editCount < 3
            // 3. Older: canEdit = false
            val canEdit = when {
                budgetEntry.monthStart >= currentMonthStart -> true
                budgetEntry.monthStart == prevMonthStart -> budgetEntry.editCount < 3
                else -> false
            }

            val remainingEdits = if (budgetEntry.monthStart == prevMonthStart) {
                (3 - budgetEntry.editCount).coerceAtLeast(0)
            } else {
                null
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
                accent = accent,
                canEdit = canEdit,
                remainingEdits = remainingEdits,
                editCount = budgetEntry.editCount
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
            // Prefer the rule's real schedule over re-deriving it from the anchor:
            // the tab then shows an occurrence as due until the worker has actually
            // added it, and only advances to the next date after the add. This keeps
            // the screen in sync with the background worker (and the backfill) so a
            // missed/overdue occurrence is visible instead of showing a future date.
            val anchorDerived = calculateNextInstallmentInfo(
                baseTimestamp = transaction.createdAt,
                frequency = recurringEntry.frequency,
                referenceTime = referenceTime
            )
            val nextDueAt = recurringEntry.nextRunAt.takeIf { it > 0L } ?: anchorDerived.first
            val nextIndex = anchorDerived.second
            val accent = recurringAccent(
                isEnabled = recurringEntry.isEnabled,
                frequency = recurringEntry.frequency,
                nextDueAt = nextDueAt,
                referenceTime = referenceTime
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
                sourceDateLabel = UiText.res(R.string.format_started_date, recurringDateFormatter.format(Date(transaction.createdAt))),
                dueLabel = dueLabelFor(nextDueAt, referenceTime),
                dueAmountLabel = formatCurrencyValue(transaction.amount, currencyId, amountFormatPreferences),
                icon = category.icon,
                accent = accent,
                nextDueAt = nextDueAt,
                isEnabled = recurringEntry.isEnabled,
                notificationsEnabled = recurringEntry.notificationsEnabled
            )
        }
        .sortedWith(
            compareBy<BudgetRecurringExpenseUi> { !it.isEnabled }
                .thenBy { it.repeatCount <= 0 }
                .thenBy { it.nextDueAt }
                .thenBy { it.title.lowercase(Locale.getDefault()) }
        )
}private fun recurringAccent(
    isEnabled: Boolean,
    frequency: RecurringFrequency,
    nextDueAt: Long,
    referenceTime: Long
): BudgetAccent {
    if (!isEnabled) return BudgetAccent.Disabled
    
    val diffDays = ((startOfDay(nextDueAt) - startOfDay(referenceTime)) / DAY_IN_MILLIS).toInt()
    if (diffDays <= 0) return BudgetAccent.Overspent // Urgent/Today
    if (diffDays <= 3) return BudgetAccent.Warning  // Soon
    
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

private fun resolveAnchorMonthStart(transactions: List<Transaction>, monthStartDay: Int = 1): Long {
    return startOfMonth(System.currentTimeMillis(), monthStartDay)
}

private fun calculateNextInstallmentInfo(
    baseTimestamp: Long,
    frequency: RecurringFrequency,
    referenceTime: Long
): Pair<Long, Int> {
    val baseCalendar = Calendar.getInstance().apply {
        timeInMillis = baseTimestamp
    }

    val nextCalendar = Calendar.getInstance().apply {
        timeInMillis = baseTimestamp
    }

    var index = 1

    // If the base transaction itself is in the future or exactly now, its index is 1
    // and the next due date is the base date.
    // However, usually baseTimestamp is the "first" occurrence that already happened.
    // The user wants to see "1 out of 6" when the first one is added.
    // In our UI, "currentInstallment" seems to represent the NEXT one to be paid,
    // or the one currently being tracked.

    // If we want "1 out of 6" immediately after adding, and it represents the *next* due:
    // Then if nextCalendar (base) <= referenceTime, it means the base one is 'processed' (or is the current one).

    // Let's adjust the logic:
    // Installment 1 is at baseTimestamp.
    // If baseTimestamp > referenceTime, then next is installment 1 at baseTimestamp.
    // If baseTimestamp <= referenceTime, then installment 1 is "done", and we look for installment 2.

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

    // If the user says "it shows 2 out of 6 but i only added the first main transaction",
    // and our logic currently results in index=2 because baseTimestamp <= referenceTime (which is true for a newly added transaction),
    // then 'index' represents the next upcoming installment.
    // To show "1 out of 6", we should probably subtract 1 if we consider the 'currently active' installment
    // or the one that was just created.

    // However, if the UI says "1/6", it usually means "we are on the 1st installment".
    // If the loop ran once, index became 2.
    // If the user expects 1, then we should return index - 1.

    return nextCalendar.timeInMillis to (index - 1).coerceAtLeast(1)
}
private fun dueLabelFor(
    nextDueAt: Long,
    referenceTime: Long
): UiText {
    val diffDays = ((startOfDay(nextDueAt) - startOfDay(referenceTime)) / DAY_IN_MILLIS).toInt()
    return when {
        diffDays <= 0 -> UiText.res(R.string.label_due_today)
        diffDays == 1 -> UiText.res(R.string.label_due_tomorrow)
        diffDays in 2..6 -> UiText.res(R.string.format_due_in_days, diffDays)
        else -> UiText.res(R.string.format_next_due, dueFormatter.format(Date(nextDueAt)).uppercase(Locale.getDefault()))
    }
}

private fun startOfMonth(timestamp: Long, monthStartDay: Int = 1): Long =
    com.mknlabs.expensetracker.utils.CustomMonthUtils.getStartOfCustomMonth(timestamp, monthStartDay)

private fun endOfMonth(timestamp: Long, monthStartDay: Int = 1): Long =
    com.mknlabs.expensetracker.utils.CustomMonthUtils.getEndOfCustomMonth(timestamp, monthStartDay)

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
        limitAmount = limitAmount,
        editCount = editCount
    )
}

private const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
private val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
private val recurringDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
private val dueFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
