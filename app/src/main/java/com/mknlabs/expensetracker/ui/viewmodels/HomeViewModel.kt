package com.mknlabs.expensetracker.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.domain.mapper.toTransactionCardItemUi
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.models.firstName
import com.mknlabs.expensetracker.ui.models.TransactionCardItemUi
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toMajorUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    val isBalanceHidden: Boolean = true
)

private data class HomeInputState(
    val userProfile: UserProfile = defaultUserProfile,
    val currencyId: Int = DEFAULT_CURRENCY_ID,
    val amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    val dateFormatPattern: String = "dd MMM",
    val timeFormat: String = DEFAULT_TIME_FORMAT,
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    val categories: List<CategoryType> = emptyList()
)

private const val HOME_RECENT_TRANSACTION_LIMIT = 10

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val inputState = MutableStateFlow(HomeInputState())

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    private var smartHideJob: Job? = null

    init {
        startDataObservation()
    }

    private fun startDataObservation() {
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
                            dateFormatPattern = inputs.dateFormatPattern,
                            timeFormat = inputs.timeFormat,
                            paymentTypeName = recentTransaction.paymentTypeName,
                            categories = inputs.categories,
                            fallbackCategoryName = application.getString(R.string.label_other)
                        )
                    },
                    customizationSettings = inputs.customizationSettings,
                    isBalanceHidden = _uiState.value.isBalanceHidden
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
        dateFormatPattern: String,
        timeFormat: String,
        categories: List<CategoryType>,
        customizationSettings: TransactionCardCustomizationSettings
    ) {
        inputState.update {
            it.copy(
                userProfile = userProfile,
                currencyId = currencyId,
                amountFormatPreferences = amountFormatPreferences,
                dateFormatPattern = dateFormatPattern,
                timeFormat = timeFormat,
                categories = categories,
                customizationSettings = customizationSettings
            )
        }
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
