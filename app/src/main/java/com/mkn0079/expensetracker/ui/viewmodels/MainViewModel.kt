package com.mkn0079.expensetracker.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.data.legacy.LegacyImportRepository
import com.mkn0079.expensetracker.data.legacy.LegacyImportResult
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabaseInitializer
import com.mkn0079.expensetracker.data.repository.CategoryRepository
import com.mkn0079.expensetracker.data.repository.ExpenseTrackerRepositoryProvider
import com.mkn0079.expensetracker.data.repository.PaymentMethodRepository
import com.mkn0079.expensetracker.data.repository.RecurringRuleRepository
import com.mkn0079.expensetracker.data.repository.TransactionRepository
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.RecurringTransactionDraft
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MainDataUiState(
    val transactions: List<Transaction> = emptyList(),
    val transactionCount: Int = 0,
    val recurringRules: List<RecurringTransactionRule> = emptyList(),
    val categories: List<CategoryType> = emptyList(),
    val paymentMethods: List<PaymentType> = emptyList(),
    val customCategories: List<CategoryType> = emptyList(),
    val customPaymentMethods: List<PaymentType> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val transactionRepository: TransactionRepository =
        ExpenseTrackerRepositoryProvider.transactionRepository(appContext)
    private val categoryRepository: CategoryRepository =
        ExpenseTrackerRepositoryProvider.categoryRepository(appContext)
    private val paymentMethodRepository: PaymentMethodRepository =
        ExpenseTrackerRepositoryProvider.paymentMethodRepository(appContext)
    private val recurringRuleRepository: RecurringRuleRepository =
        ExpenseTrackerRepositoryProvider.recurringRuleRepository(appContext)
    private val legacyImportRepository = LegacyImportRepository(appContext)

    private val _uiState = MutableStateFlow(MainDataUiState())
    val uiState: StateFlow<MainDataUiState> = _uiState.asStateFlow()
    private val observeTransactions = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            ExpenseTrackerDatabaseInitializer.initialize(appContext)
            combine(
                observeTransactions.flatMapLatest { shouldObserve ->
                    if (shouldObserve) {
                        transactionRepository.observeActiveTransactions()
                    } else {
                        flowOf(_uiState.value.transactions)
                    }
                },
                transactionRepository.observeActiveTransactionCount(),
                recurringRuleRepository.observeActiveRecurringRules(),
                categoryRepository.observeActiveCategories(),
                paymentMethodRepository.observeActivePaymentMethods()
            ) { transactions, transactionCount, recurringRules, categories, paymentMethods ->
                MainDataUiState(
                    transactions = transactions,
                    transactionCount = transactionCount,
                    recurringRules = recurringRules,
                    categories = categories,
                    paymentMethods = paymentMethods,
                    customCategories = categories.filter { !it.isSystem },
                    customPaymentMethods = paymentMethods.filter { !it.isSystem }
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setTransactionObservationEnabled(enabled: Boolean) {
        observeTransactions.value = enabled
    }

    fun saveTransaction(
        transaction: Transaction,
        recurringDraft: RecurringTransactionDraft?,
        existingRule: RecurringTransactionRule?
    ) {
        viewModelScope.launch {
            val savedTransaction = transactionRepository.upsertTransaction(transaction)
            when {
                recurringDraft != null && savedTransaction.transactionTypeId == 2 -> {
                    recurringRuleRepository.upsertRule(
                        RecurringTransactionRule(
                            id = existingRule?.id.orEmpty(),
                            transactionId = savedTransaction.id,
                            frequency = recurringDraft.frequency,
                            repeatCount = recurringDraft.repeatCount,
                            isEnabled = existingRule?.isEnabled ?: true,
                            intervalCount = existingRule?.intervalCount ?: 1,
                            remainingCount = recurringDraft.repeatCount,
                            anchorAt = existingRule?.anchorAt ?: savedTransaction.createdAt,
                            nextRunAt = existingRule?.nextRunAt ?: savedTransaction.createdAt,
                            lastRunAt = existingRule?.lastRunAt,
                            createdAt = existingRule?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            syncState = existingRule?.syncState ?: savedTransaction.syncState,
                            isDeleted = false
                        )
                    )
                }

                existingRule != null -> recurringRuleRepository.deleteRule(existingRule.id)
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            transactionRepository.softDeleteTransaction(transactionId)
            recurringRuleRepository.getActiveByTransactionId(transactionId)?.let { rule ->
                recurringRuleRepository.deleteRule(rule.id)
            }
        }
    }

    fun deleteAllTransactions(
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    transactionRepository.deleteAllTransactions()
                }
                _uiState.update {
                    it.copy(
                        transactions = emptyList(),
                        transactionCount = 0,
                        recurringRules = emptyList()
                    )
                }
                onComplete()
            } catch (throwable: Throwable) {
                onError(throwable)
            }
        }
    }

    fun addRecurring(
        transactionId: String,
        frequency: com.mkn0079.expensetracker.models.RecurringFrequency,
        repeatCount: Int
    ) {
        viewModelScope.launch {
            val existingRule = recurringRuleRepository.getActiveByTransactionId(transactionId)
            val transaction = transactionRepository.getTransactionById(transactionId) ?: return@launch
            recurringRuleRepository.upsertRule(
                RecurringTransactionRule(
                    id = existingRule?.id.orEmpty(),
                    transactionId = transactionId,
                    frequency = frequency,
                    repeatCount = repeatCount,
                    isEnabled = existingRule?.isEnabled ?: true,
                    intervalCount = existingRule?.intervalCount ?: 1,
                    remainingCount = repeatCount,
                    anchorAt = existingRule?.anchorAt ?: transaction.createdAt,
                    nextRunAt = existingRule?.nextRunAt ?: transaction.createdAt,
                    lastRunAt = existingRule?.lastRunAt,
                    createdAt = existingRule?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    syncState = existingRule?.syncState ?: transaction.syncState,
                    isDeleted = false
                )
            )
        }
    }

    fun updateRecurring(
        ruleId: String,
        transactionId: String,
        frequency: com.mkn0079.expensetracker.models.RecurringFrequency,
        repeatCount: Int
    ) {
        viewModelScope.launch {
            val existingRule = uiState.value.recurringRules.firstOrNull { it.id == ruleId } ?: return@launch
            recurringRuleRepository.upsertRule(
                existingRule.copy(
                    id = ruleId,
                    transactionId = transactionId,
                    frequency = frequency,
                    repeatCount = repeatCount,
                    remainingCount = repeatCount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteRecurring(ruleId: String) {
        viewModelScope.launch {
            recurringRuleRepository.deleteRule(ruleId)
        }
    }

    fun setRecurringEnabled(ruleId: String, enabled: Boolean) {
        viewModelScope.launch {
            recurringRuleRepository.setEnabled(ruleId, enabled)
        }
    }

    fun createCustomCategory(
        name: String,
        iconKey: String,
        transactionTypeId: Int
    ) {
        viewModelScope.launch {
            categoryRepository.createCustomCategory(
                name = name,
                iconKey = iconKey,
                transactionTypeId = transactionTypeId
            )
        }
    }

    fun createCustomPaymentMethod(
        name: String,
        iconKey: String
    ) {
        viewModelScope.launch {
            paymentMethodRepository.createCustomPaymentMethod(
                name = name,
                iconKey = iconKey
            )
        }
    }

    fun deleteCustomCategory(categoryId: Int) {
        viewModelScope.launch {
            categoryRepository.deleteCustomCategory(categoryId)
        }
    }

    fun deleteCustomPaymentMethod(paymentMethodId: Int) {
        viewModelScope.launch {
            paymentMethodRepository.deleteCustomPaymentMethod(paymentMethodId)
        }
    }

    fun importLegacyBackup(
        uri: Uri,
        onComplete: (LegacyImportResult) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    legacyImportRepository.importBackup(uri)
                }
                onComplete(result)
            } catch (throwable: Throwable) {
                onError(throwable)
            }
        }
    }
}
