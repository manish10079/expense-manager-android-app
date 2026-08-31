package com.mknlabs.expensetracker.ui.viewmodels

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.domain.mapper.toTransactionCardItemUi
import com.mknlabs.expensetracker.domain.repository.GoalRepository
import com.mknlabs.expensetracker.domain.repository.RecurringRuleRepository
import com.mknlabs.expensetracker.domain.repository.SyncRepository
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Goal
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.models.firstName
import com.mknlabs.expensetracker.ui.models.TransactionCardItemUi
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toMajorUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

// ──────────────────────────────────────────────────────────
// Upcoming Recurring UI model (self-contained for HomeScreen)
// ──────────────────────────────────────────────────────────

@Immutable
data class UpcomingRecurringUi(
    val id: String,
    val title: String,
    val dueLabel: UiText,
    val dueAmountLabel: String,
    val icon: ImageVector,
    val categoryLabel: String,
    val nextDueAt: Long
)

// ──────────────────────────────────────────────────────────
// Home UI State
// ──────────────────────────────────────────────────────────

@Immutable
data class HomeScreenUiState(
    val greetingName: String = defaultUserProfile.firstName(),
    val totalBalance: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val previousMonthBalance: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val totalIncome: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val totalExpense: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val todaySpending: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val recentTransactions: List<TransactionCardItemUi> = emptyList(),
    val activeGoalsSaved: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val goalCount: Int = 0,
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    val isBalanceHidden: Boolean = true,
    val isSyncing: Boolean = false,
    val userTier: UserTier = UserTier.FREE,
    // Monthly Summary (shown to ad-free/premium users in place of the home ad slot).
    val monthlyNetDisplay: String = "",
    val monthlyNetDeltaPercent: Float = 0f,
    val monthlyNetDeltaDisplay: UiText? = null,
    val monthlyIncomeFraction: Float = 0.5f,
    val upcomingRecurring: List<UpcomingRecurringUi> = emptyList()
)

private data class HomeInputState(
    val userProfile: UserProfile = defaultUserProfile,
    val userTier: UserTier = UserTier.FREE,
    val currencyId: Int = DEFAULT_CURRENCY_ID,
    val amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    val dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    val timeFormat: String = DEFAULT_TIME_FORMAT,
    val categories: List<CategoryType> = emptyList(),
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings()
)

private const val HOME_RECENT_TRANSACTION_LIMIT = 10

internal fun activeGoalsSavedMinor(allGoals: List<Goal>): Long =
    allGoals.filter { !it.isCompleted }.sumOf { it.currentAmountMinor }

/**
 * Computed values for the home Monthly Summary card (shown to ad-free users in
 * place of the home ad slot). Pure function, unit-tested in HomeViewModelTest.
 *
 * @param incomeMinor this month's income in minor units
 * @param expenseMinor this month's expense in minor units
 * @param previousIncomeMinor last month's income in minor units
 * @param previousExpenseMinor last month's expense in minor units
 */
internal data class MonthlySummaryUi(
    val netMinor: Long,
    val deltaPercent: Float,
    val hasBaseline: Boolean,
    val incomeFraction: Float
)

internal fun buildMonthlySummary(
    incomeMinor: Long,
    expenseMinor: Long,
    previousIncomeMinor: Long,
    previousExpenseMinor: Long
): MonthlySummaryUi {
    val netMinor = incomeMinor - expenseMinor
    val previousNetMinor = previousIncomeMinor - previousExpenseMinor
    return MonthlySummaryUi(
        netMinor = netMinor,
        deltaPercent = percentageChange(netMinor.toDouble(), previousNetMinor.toDouble()),
        hasBaseline = previousNetMinor != 0L,
        // Income share of the month's total flow; neutral split when there is no activity.
        incomeFraction = if (incomeMinor + expenseMinor > 0L) {
            incomeMinor.toFloat() / (incomeMinor + expenseMinor).toFloat()
        } else {
            0.5f
        }
    )
}

/** Percent change of [current] vs [previous]; 100% when there was no previous baseline. */
private fun percentageChange(current: Double, previous: Double): Float {
    if (previous == 0.0) {
        return if (current == 0.0) 0f else 100f
    }
    return (((current - previous) / previous) * 100.0).toFloat()
}

/**
 * Builds a human-readable due label ("Today", "Tomorrow", "In X days") for a
 * recurring transaction's next run timestamp.
 */
private fun dueLabelForHome(nextDueAt: Long): UiText {
    val now = System.currentTimeMillis()
    val daysUntil = TimeUnit.MILLISECONDS.toDays(nextDueAt - now)
    return when {
        daysUntil < 0L -> UiText.res(R.string.format_days_overdue, abs(daysUntil.toInt()))
        daysUntil == 0L -> UiText.res(R.string.label_today)
        daysUntil == 1L -> UiText.res(R.string.label_tomorrow)
        else -> UiText.res(R.string.format_days_left, daysUntil.toInt())
    }
}

/**
 * Maps enabled recurring rules + their source transactions into a display-ready
 * list of [UpcomingRecurringUi], sorted by soonest due first, limited to [limit].
 */
private fun buildUpcomingRecurring(
    rules: List<RecurringTransactionRule>,
    transactions: List<Transaction>,
    categories: Map<Int, CategoryType>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    limit: Int = 2
): List<UpcomingRecurringUi> {
    val txById = transactions.associateBy { it.id }
    return rules
        .filter { it.isEnabled && !it.isDeleted }
        .mapNotNull { rule ->
            val tx = txById[rule.transactionId] ?: return@mapNotNull null
            val category = categories[tx.categoryId]
            UpcomingRecurringUi(
                id = rule.id,
                title = tx.note.ifBlank { category?.name ?: "" },
                dueLabel = dueLabelForHome(rule.nextRunAt),
                dueAmountLabel = formatCurrencyValue(tx.amount, currencyId, amountFormatPreferences),
                icon = category?.icon ?: Icons.Default.Repeat,
                categoryLabel = category?.name ?: "",
                nextDueAt = rule.nextRunAt
            )
        }
        .sortedBy { it.nextDueAt }
        .take(limit)
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository,
    private val recurringRuleRepository: RecurringRuleRepository,
    private val syncRepository: SyncRepository,
    private val appPreferencesRepository: com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
) : ViewModel() {

    private val inputState = MutableStateFlow(HomeInputState())

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    private var smartHideJob: Job? = null
    private var currentMonthStartDay: Int = 1

    init {
        startDataObservation()
    }

    private fun startDataObservation() {
        viewModelScope.launch {
            com.mknlabs.expensetracker.data.local.AppSettingsDataStore
                .getAppSettingsFlow(application)
                .flatMapLatest { settings ->
                    currentMonthStartDay = settings.monthStartDay
                    val now = System.currentTimeMillis()
                    val (currentMonthStart, currentMonthEnd) = com.mknlabs.expensetracker.utils.CustomMonthUtils.getCustomMonthRange(now, currentMonthStartDay, 0)
                    val (prevMonthStart, prevMonthEnd) = com.mknlabs.expensetracker.utils.CustomMonthUtils.getCustomMonthRange(now, currentMonthStartDay, -1)
                    val todayStart = java.util.Calendar.getInstance().apply {
                        timeInMillis = now
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val todayEnd = java.util.Calendar.getInstance().apply {
                        timeInMillis = now
                        set(java.util.Calendar.HOUR_OF_DAY, 23)
                        set(java.util.Calendar.MINUTE, 59)
                        set(java.util.Calendar.SECOND, 59)
                        set(java.util.Calendar.MILLISECOND, 999)
                    }.timeInMillis

                    combine(
                        transactionRepository.observeHomeSummary(
                            currentMonthStartMillis = currentMonthStart,
                            currentMonthEndMillis = currentMonthEnd,
                            previousMonthStartMillis = prevMonthStart,
                            previousMonthEndMillis = prevMonthEnd,
                            todayStartMillis = todayStart,
                            todayEndMillis = todayEnd
                        ),
                        transactionRepository.observeRecentTransactions(HOME_RECENT_TRANSACTION_LIMIT),
                        goalRepository.observeAllGoals(),
                        recurringRuleRepository.observeActiveRecurringRules(),
                        transactionRepository.observeActiveTransactions(),
                        syncRepository.isSyncing,
                        inputState
                    ) { flows ->
                        @Suppress("UNCHECKED_CAST")
                        val summary = flows[0] as com.mknlabs.expensetracker.domain.repository.TransactionSummary
                        @Suppress("UNCHECKED_CAST")
                        val recentTransactions = flows[1] as List<com.mknlabs.expensetracker.domain.repository.RecentTransaction>
                        @Suppress("UNCHECKED_CAST")
                        val allGoals = flows[2] as List<Goal>
                        @Suppress("UNCHECKED_CAST")
                        val recurringRules = flows[3] as List<RecurringTransactionRule>
                        @Suppress("UNCHECKED_CAST")
                        val allActiveTransactions = flows[4] as List<Transaction>
                        val isSyncing = flows[5] as Boolean
                        val inputs = flows[6] as HomeInputState

                        val categoriesMap = inputs.categories.associateBy { it.id }
                        val upcomingRecurring = buildUpcomingRecurring(
                            rules = recurringRules,
                            transactions = allActiveTransactions,
                            categories = categoriesMap,
                            currencyId = inputs.currencyId,
                            amountFormatPreferences = inputs.amountFormatPreferences
                        )

                        val monthlySummary = buildMonthlySummary(
                            incomeMinor = summary.totalIncomeMinor,
                            expenseMinor = summary.totalExpenseMinor,
                            previousIncomeMinor = summary.previousMonthIncomeMinor,
                            previousExpenseMinor = summary.previousMonthExpenseMinor
                        )
                        HomeScreenUiState(
                            greetingName = inputs.userProfile.firstName().replaceFirstChar { it.uppercase() },
                            totalBalance = formatCurrencyValue(
                                (summary.totalIncomeMinor - summary.totalExpenseMinor).toMajorUnits(),
                                currencyId = inputs.currencyId,
                                amountFormatPreferences = inputs.amountFormatPreferences
                            ),
                            previousMonthBalance = formatCurrencyValue(
                                (summary.previousMonthIncomeMinor - summary.previousMonthExpenseMinor).toMajorUnits(),
                                currencyId = inputs.currencyId,
                                amountFormatPreferences = inputs.amountFormatPreferences
                            ),
                            totalIncome = formatCurrencyValue(
                                summary.totalIncomeMinor.toMajorUnits(),
                                currencyId = inputs.currencyId,
                                amountFormatPreferences = inputs.amountFormatPreferences
                            ),
                            totalExpense = formatCurrencyValue(
                                summary.totalExpenseMinor.toMajorUnits(),
                                currencyId = inputs.currencyId,
                                amountFormatPreferences = inputs.amountFormatPreferences
                            ),
                            todaySpending = formatCurrencyValue(
                                summary.highlightedExpenseMinor.toMajorUnits(),
                                currencyId = inputs.currencyId,
                                amountFormatPreferences = inputs.amountFormatPreferences
                            ),
                            monthlyNetDisplay = formatCurrencyValue(
                                monthlySummary.netMinor.toMajorUnits(),
                                currencyId = inputs.currencyId,
                                amountFormatPreferences = inputs.amountFormatPreferences
                            ),
                            monthlyNetDeltaPercent = monthlySummary.deltaPercent,
                            monthlyNetDeltaDisplay = if (monthlySummary.hasBaseline) {
                                formatPercent(monthlySummary.deltaPercent)
                            } else {
                                null
                            },
                            monthlyIncomeFraction = monthlySummary.incomeFraction,
                            upcomingRecurring = upcomingRecurring,
                            recentTransactions = recentTransactions.map { recentTransaction ->
                                recentTransaction.transaction.toTransactionCardItemUi(
                                    currencyId = inputs.currencyId,
                                    amountFormatPreferences = inputs.amountFormatPreferences,
                                    dateFormatPattern = inputs.dateFormatPattern,
                                    timeFormat = inputs.timeFormat,
                                    paymentTypeName = recentTransaction.paymentTypeName,
                                    categories = inputs.categories,
                                    fallbackCategoryName = application.getString(R.string.label_other)
                                )
                            },
                            activeGoalsSaved = formatCurrencyValue(
                                activeGoalsSavedMinor(allGoals).toMajorUnits(),
                                currencyId = inputs.currencyId,
                                amountFormatPreferences = inputs.amountFormatPreferences
                            ),
                            goalCount = allGoals.count { !it.isCompleted },
                            customizationSettings = inputs.customizationSettings,
                            isBalanceHidden = _uiState.value.isBalanceHidden,
                            isSyncing = isSyncing,
                            userTier = inputs.userTier
                        )
                    }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun updateInputs(
        userProfile: UserProfile,
        userTier: UserTier,
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences,
        dateFormatPattern: String,
        timeFormat: String,
        categories: List<CategoryType>,
        customizationSettings: TransactionCardCustomizationSettings
    ) {
        inputState.update {
            it.copy(
                userProfile = userProfile,
                userTier = userTier,
                currencyId = currencyId,
                amountFormatPreferences = amountFormatPreferences,
                dateFormatPattern = dateFormatPattern,
                timeFormat = timeFormat,
                categories = categories,
                customizationSettings = customizationSettings
            )
        }
    }

    /** Signed percent display like "+12%" / "-8%" (mirrors the Analytics helper). */
    private fun formatPercent(value: Float): UiText {
        val formatter = DecimalFormat("0.#")
        val absoluteValue = formatter.format(abs(value))
        val prefixRes = if (value >= 0f) R.string.label_plus else R.string.label_minus
        return UiText.res(R.string.format_percent_signed, UiText.res(prefixRes), absoluteValue)
    }

    fun toggleBalanceVisibility() {
        val newState = !_uiState.value.isBalanceHidden
        _uiState.update { it.copy(isBalanceHidden = newState) }
        
        smartHideJob?.cancel()
        if (!newState) {
            // If we just showed the balance, start a 10-second timer to hide it again
            smartHideJob = viewModelScope.launch {
                delay(10000)
                _uiState.update { it.copy(isBalanceHidden = true) }
            }
        }
    }
}
