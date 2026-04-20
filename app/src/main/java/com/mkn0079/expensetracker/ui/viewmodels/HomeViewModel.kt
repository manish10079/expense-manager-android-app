package com.mkn0079.expensetracker.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mkn0079.expensetracker.data.repository.ExpenseTrackerRepositoryProvider
import com.mkn0079.expensetracker.data.repository.TransactionRepository
import com.mkn0079.expensetracker.domain.mapper.toTransactionCardItemUi
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.models.firstName
import com.mkn0079.expensetracker.ui.models.TransactionCardItemUi
import com.mkn0079.expensetracker.utils.formatCurrencyValue
import com.mkn0079.expensetracker.utils.toMajorUnits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class HomeScreenUiState(
    val greetingName: String = defaultUserProfile.firstName(),
    val totalBalance: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val totalIncome: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val totalExpense: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val todaySpending: String = formatCurrencyValue(0.0, DEFAULT_CURRENCY_ID),
    val recentTransactions: List<TransactionCardItemUi> = emptyList(),
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings()
)

private data class HomeInputState(
    val userProfile: UserProfile = defaultUserProfile,
    val currencyId: Int = DEFAULT_CURRENCY_ID,
    val timeFormat: String = DEFAULT_TIME_FORMAT,
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings()
)

private const val HOME_RECENT_TRANSACTION_LIMIT = 10

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionRepository: TransactionRepository =
        ExpenseTrackerRepositoryProvider.transactionRepository(application.applicationContext)
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
                        currencyId = inputs.currencyId
                    ),
                    totalIncome = formatCurrencyValue(
                        summary.totalIncomeMinor.toMajorUnits(),
                        currencyId = inputs.currencyId
                    ),
                    totalExpense = formatCurrencyValue(
                        summary.totalExpenseMinor.toMajorUnits(),
                        currencyId = inputs.currencyId
                    ),
                    todaySpending = formatCurrencyValue(
                        summary.highlightedExpenseMinor.toMajorUnits(),
                        currencyId = inputs.currencyId
                    ),
                    recentTransactions = recentTransactions.map { recentTransaction ->
                        recentTransaction.transaction.toTransactionCardItemUi(
                            currencyId = inputs.currencyId,
                            dateFormatPattern = "dd MMM",
                            timeFormat = inputs.timeFormat,
                            paymentTypeName = recentTransaction.paymentTypeName
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
        timeFormat: String,
        customizationSettings: TransactionCardCustomizationSettings
    ) {
        inputState.update {
            it.copy(
                userProfile = userProfile,
                currencyId = currencyId,
                timeFormat = timeFormat,
                customizationSettings = customizationSettings
            )
        }
    }
}

class HomeViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val application = context.applicationContext as Application
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
