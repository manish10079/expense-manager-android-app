package com.mkn0079.expensetracker.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.domain.repository.AuthRepository
import com.mkn0079.expensetracker.domain.repository.CategoryRepository
import com.mkn0079.expensetracker.domain.repository.DataManagementRepository
import com.mkn0079.expensetracker.domain.repository.JsonExportResult
import com.mkn0079.expensetracker.domain.repository.JsonImportResult
import com.mkn0079.expensetracker.domain.repository.LegacyImportRepository
import com.mkn0079.expensetracker.domain.repository.LegacyImportResult
import com.mkn0079.expensetracker.domain.repository.PaymentMethodRepository
import com.mkn0079.expensetracker.domain.repository.RecurringRuleRepository
import com.mkn0079.expensetracker.domain.repository.SecurityRepository
import com.mkn0079.expensetracker.domain.repository.TransactionRepository
import com.mkn0079.expensetracker.domain.repository.MonetizationRepository
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.RecurringFrequency
import com.mkn0079.expensetracker.models.RecurringTransactionDraft
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.min

sealed class MainUiEvent {
    object TransactionOperationCompleted : MainUiEvent()
    data class ShowAdExpiryWarning(val minutesRemaining: Int) : MainUiEvent()
}

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
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val recurringRuleRepository: RecurringRuleRepository,
    private val dataManagementRepository: DataManagementRepository,
    private val legacyImportRepository: LegacyImportRepository,
    private val securityRepository: SecurityRepository,
    private val authRepository: AuthRepository,
    private val monetizationRepository: MonetizationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainDataUiState())
    val uiState: StateFlow<MainDataUiState> = _uiState.asStateFlow()
    
    private val _uiEvent = MutableSharedFlow<MainUiEvent>()
    val uiEvent: SharedFlow<MainUiEvent> = _uiEvent.asSharedFlow()

    val currentUser = authRepository.currentUser
    
    private val observeTransactions = MutableStateFlow(false)

    init {
        viewModelScope.launch {
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

        // Monitor Ad Expiry for 15-minute warning and Snap-Back
        viewModelScope.launch {
            var hasShownWarning = false
            
            monetizationRepository.globalAdAccessExpiry.collect { expiry ->
                if (expiry == 0L) {
                    hasShownWarning = false
                    return@collect
                }

                while (true) {
                    val remainingMillis = expiry - System.currentTimeMillis()
                    val minutes = (remainingMillis / 1000) / 60
                    
                    // 1. Snap-Back Logic (Timer hit 0)
                    if (remainingMillis <= 0) {
                        val settings = AppSettingsDataStore.getAppSettingsFlow(appContext).first()
                        val currentTimeout = settings.appLockTimeoutMinutes
                        if (currentTimeout in listOf(5, 10, 15)) {
                            AppSettingsDataStore.updateAppSettings(appContext) { it.copy(appLockTimeoutMinutes = 1) }
                            // Also update legacy preferences for consistency
                            com.mkn0079.expensetracker.data.local.AppLockPreferences.setAutoLockDurationMinutes(appContext, 1)
                        }
                        hasShownWarning = false
                        break
                    }
                    
                    // 2. 15-Minute Warning Logic
                    if (minutes <= 15 && !hasShownWarning) {
                        val settings = AppSettingsDataStore.getAppSettingsFlow(appContext).first()
                        val currentTimeout = settings.appLockTimeoutMinutes
                        // Only warn if they are actually using an ad-supported setting
                        if (currentTimeout in listOf(5, 10, 15)) {
                            _uiEvent.emit(MainUiEvent.ShowAdExpiryWarning(minutes.toInt().coerceAtLeast(1)))
                            hasShownWarning = true
                        }
                    }

                    if (remainingMillis > 1000) {
                        delay(1000)
                    } else {
                        delay(remainingMillis.coerceAtLeast(0))
                        // Re-run loop one last time to hit the Snap-Back logic at exactly <= 0
                    }
                }
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
                    val initialNextRun = calculateInitialNextRun(
                        savedTransaction.createdAt,
                        recurringDraft.frequency
                    )
                    recurringRuleRepository.upsertRule(
                        RecurringTransactionRule(
                            id = existingRule?.id.orEmpty(),
                            transactionId = savedTransaction.id,
                            frequency = recurringDraft.frequency,
                            repeatCount = recurringDraft.repeatCount,
                            isEnabled = existingRule?.isEnabled ?: true,
                            intervalCount = existingRule?.intervalCount ?: 1,
                            remainingCount = recurringDraft.repeatCount - 1, // First one is already saved
                            anchorAt = existingRule?.anchorAt ?: savedTransaction.createdAt,
                            nextRunAt = existingRule?.nextRunAt ?: initialNextRun,
                            lastRunAt = existingRule?.lastRunAt ?: savedTransaction.createdAt,
                            createdAt = existingRule?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            syncState = existingRule?.syncState ?: savedTransaction.syncState,
                            isDeleted = false
                        )
                    )
                }

                existingRule != null -> recurringRuleRepository.deleteRule(existingRule.id)
            }
            
            // Check budget and notify if needed
            transactionRepository.checkBudgetAndNotify(appContext, savedTransaction)

            // If a recurring rule was added/updated, trigger immediate processing
            if (recurringDraft != null) {
                com.mkn0079.expensetracker.workers.RecurringTransactionWorker.enqueueImmediate(appContext)
            }
            _uiEvent.emit(MainUiEvent.TransactionOperationCompleted)
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            transactionRepository.softDeleteTransaction(transactionId)
            recurringRuleRepository.getActiveByTransactionId(transactionId)?.let { rule ->
                recurringRuleRepository.deleteRule(rule.id)
            }
            _uiEvent.emit(MainUiEvent.TransactionOperationCompleted)
        }
    }

    fun deleteAllTransactions(
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Delete Firebase Account if logged in
                val user = authRepository.currentUser.value
                if (user != null) {
                    authRepository.deleteAccount()
                }

                // 2. Wipe Local Database
                withContext(Dispatchers.IO) {
                    transactionRepository.deleteAllTransactions()
                }

                // 3. Clear Local Preferences (DataStore)
                com.mkn0079.expensetracker.data.local.AppSettingsDataStore.clearAll(appContext)
                com.mkn0079.expensetracker.data.local.UserProfileDataStore.clearAll(appContext)
                com.mkn0079.expensetracker.data.local.AppLockPreferences.clearAll(appContext)

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

    fun updateRecurringRule(
        ruleId: String,
        frequency: RecurringFrequency,
        totalInstallments: Int
    ) {
        viewModelScope.launch {
            val existingRule = _uiState.value.recurringRules.find { it.id == ruleId } ?: return@launch
            recurringRuleRepository.upsertRule(
                existingRule.copy(
                    frequency = frequency,
                    repeatCount = totalInstallments,
                    updatedAt = System.currentTimeMillis()
                )
            )
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

    fun disableAppLock() {
        viewModelScope.launch {
            securityRepository.disableLock()
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

    fun backupDatabase(
        uri: Uri,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dataManagementRepository.backupDatabase(uri)
                }
                onComplete()
            } catch (throwable: Throwable) {
                onError(throwable)
            }
        }
    }

    fun restoreDatabase(
        uri: Uri,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dataManagementRepository.restoreDatabase(uri)
                }
                onComplete()
            } catch (throwable: Throwable) {
                onError(throwable)
            }
        }
    }

    fun exportJson(
        uri: Uri,
        onComplete: (JsonExportResult) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    dataManagementRepository.exportJson(uri)
                }
                onComplete(result)
            } catch (throwable: Throwable) {
                onError(throwable)
            }
        }
    }

    fun importJson(
        uri: Uri,
        onComplete: (JsonImportResult) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    dataManagementRepository.importJson(uri)
                }
                onComplete(result)
            } catch (throwable: Throwable) {
                onError(throwable)
            }
        }
    }

    private fun calculateInitialNextRun(
        baseAnchor: Long,
        frequency: RecurringFrequency
    ): Long {
        val baseCalendar = Calendar.getInstance().apply { timeInMillis = baseAnchor }
        val nextCalendar = Calendar.getInstance().apply { timeInMillis = baseAnchor }

        when (frequency) {
            RecurringFrequency.Daily -> nextCalendar.add(Calendar.DAY_OF_YEAR, 1)
            RecurringFrequency.Weekly -> nextCalendar.add(Calendar.WEEK_OF_YEAR, 1)
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
        return nextCalendar.timeInMillis
    }
}
