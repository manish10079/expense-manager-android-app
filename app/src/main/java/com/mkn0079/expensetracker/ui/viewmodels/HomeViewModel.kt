package com.mkn0079.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mkn0079.expensetracker.domain.mapper.toTransactionCardItemUi
import com.mkn0079.expensetracker.domain.repository.TransactionRepository
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.models.firstName
import com.mkn0079.expensetracker.ui.models.TransactionCardItemUi
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import com.mkn0079.expensetracker.utils.formatCurrencyValue
import com.mkn0079.expensetracker.utils.toMajorUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class HomeScreenUiState(
    val greetingName: String = defaultUserProfile.firstName(),
    val totalBalance: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val previousMonthBalance: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val totalIncome: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val totalExpense: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val todaySpending: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val recentTransactions: List<TransactionCardItemUi> = emptyList(),
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings()
)

private data class HomeInputState(
    val userProfile: UserProfile = defaultUserProfile,
    val currencyId: Int = DEFAULT_CURRENCY_ID,
    val amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    val timeFormat: String = DEFAULT_TIME_FORMAT,
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    val categories: List<CategoryType> = emptyList()
)

private const val HOME_RECENT_TRANSACTION_LIMIT = 10

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val inputState = MutableStateFlow(HomeInputState())

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.observeHomeSummary(),
                transactionRepository.observeRecentTransactions(HOME_RECENT_TRANSACTION_LIMIT),
                inputState
            ) { summary, recentTransactions, inputs ->
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
                    recentTransactions = recentTransactions.map { recentTransaction ->
                        recentTransaction.transaction.toTransactionCardItemUi(
                            currencyId = inputs.currencyId,
                            amountFormatPreferences = inputs.amountFormatPreferences,
                            dateFormatPattern = "dd MMM",
                            timeFormat = inputs.timeFormat,
                            paymentTypeName = recentTransaction.paymentTypeName,
                            categories = inputs.categories
                        )
                    },
                    customizationSettings = inputs.customizationSettings
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateInputs(
        userProfile: UserProfile,
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences,
        timeFormat: String,
        categories: List<CategoryType>,
        customizationSettings: TransactionCardCustomizationSettings
    ) {
        inputState.update {
            it.copy(
                userProfile = userProfile,
                currencyId = currencyId,
                amountFormatPreferences = amountFormatPreferences,
                timeFormat = timeFormat,
                categories = categories,
                customizationSettings = customizationSettings
            )
        }
    }
}
