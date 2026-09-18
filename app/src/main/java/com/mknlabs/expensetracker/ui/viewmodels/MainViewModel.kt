package com.mknlabs.expensetracker.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import com.mknlabs.expensetracker.domain.repository.CategoryRepository
import com.mknlabs.expensetracker.domain.repository.DataManagementRepository
import com.mknlabs.expensetracker.domain.repository.JsonExportResult
import com.mknlabs.expensetracker.domain.repository.JsonImportResult
import com.mknlabs.expensetracker.domain.repository.LegacyImportRepository
import com.mknlabs.expensetracker.domain.repository.LegacyImportResult
import com.mknlabs.expensetracker.domain.repository.PaymentMethodRepository
import com.mknlabs.expensetracker.domain.repository.RecurringRuleRepository
import com.mknlabs.expensetracker.domain.repository.SecurityRepository
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.domain.repository.FavoriteTransactionRepository
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.FavoriteTransaction
import com.mknlabs.expensetracker.models.InstallmentOccurrence
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringPlanEdit
import com.mknlabs.expensetracker.models.RecurringTransactionDraft
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
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

    /** Installments genuinely settled by the ledger sheet, for the confirmation toast. */
    data class InstallmentsPaid(val count: Int) : MainUiEvent()
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
    private val monetizationRepository: MonetizationRepository,
    private val configurationRepository: com.mknlabs.expensetracker.domain.repository.ConfigurationRepository,
    private val checkBudgetUseCase: com.mknlabs.expensetracker.domain.usecase.CheckBudgetUseCase,
    private val favoriteTransactionRepository: FavoriteTransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainDataUiState())
    val uiState: StateFlow<MainDataUiState> = _uiState.asStateFlow()
    
    val isProPassEnabled: StateFlow<Boolean> = configurationRepository.isProPassEnabled

    val favorites: StateFlow<List<FavoriteTransaction>> = favoriteTransactionRepository.getAllFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
                categoryRepository.observeAllCategories(),
                paymentMethodRepository.observeAllPaymentMethods()
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
            monetizationRepository.globalAdAccessExpiry.collectLatest { expiry ->
                android.util.Log.d("MainVM", "Monitoring new Expiry: $expiry")
                if (expiry <= System.currentTimeMillis()) {
                    return@collectLatest
                }

                var hasShownWarning = false
                
                while (true) {
                    val remainingMillis = expiry - System.currentTimeMillis()
                    val minutes = (remainingMillis / 1000) / 60
                    
                    // 1. Snap-Back Logic (Timer hit 0)
                    if (remainingMillis <= 0) {
                        android.util.Log.d("MainVM", "Timer Expired. Triggering Snap-Back.")
                        val settings = AppSettingsDataStore.getAppSettingsFlow(appContext).first()
                        val currentTimeout = settings.appLockTimeoutMinutes
                        if (currentTimeout in listOf(5, 10, 15)) {
                            AppSettingsDataStore.updateAppSettings(appContext) { it.copy(appLockTimeoutMinutes = 1) }
                            com.mknlabs.expensetracker.data.local.AppLockPreferences.setAutoLockDurationMinutes(appContext, 1)
                        }
                        break
                    }
                    
                    // 2. 15-Minute Warning Logic
                    if (minutes <= 15 && !hasShownWarning) {
                        android.util.Log.d("MainVM", "Emitting ShowAdExpiryWarning event.")
                        _uiEvent.emit(MainUiEvent.ShowAdExpiryWarning(minutes.toInt().coerceAtLeast(1)))
                        hasShownWarning = true
                    }

                    delay(1000)
                }
            }
        }

        // Monitor Premium/Pro Expiry to perform automatic local downgrade
        viewModelScope.launch {
            combine(
                com.mknlabs.expensetracker.data.local.UserProfileDataStore.getUserProfileFlow(appContext),
                AppSettingsDataStore.getAppSettingsFlow(appContext)
            ) { profile, settings ->
                profile to settings
            }.collectLatest { (profile, settings) ->
                val now = System.currentTimeMillis()
                val isExpired = profile.accountTier == "PREMIUM" && profile.proExpiryTimestamp in 1..<now
                if (isExpired) {
                    android.util.Log.d("MainVM", "Premium has expired. Downgrading user locally.")
                    // 1. Downgrade locally in AppSettings
                    AppSettingsDataStore.updateAppSettings(appContext) { current ->
                        current.copy(userTier = com.mknlabs.expensetracker.models.UserTier.FREE)
                    }
                    // 2. Downgrade locally in UserProfile
                    com.mknlabs.expensetracker.data.local.UserProfileDataStore.updateUserProfile(appContext) { currentProfile ->
                        currentProfile.copy(
                            accountTier = "FREE",
                            updatedAtMillis = now
                        )
                    }
                    // 3. Trigger a sync worker to push this downgraded status to Firestore
                    com.mknlabs.expensetracker.workers.SyncWorker.startImmediate(appContext)
                } else if (profile.accountTier == "PREMIUM" && profile.proExpiryTimestamp > now) {
                    if (settings.userTier != com.mknlabs.expensetracker.models.UserTier.PREMIUM) {
                        AppSettingsDataStore.updateUserTier(appContext, com.mknlabs.expensetracker.models.UserTier.PREMIUM)
                    }
                    // If the subscription is active, but we haven't reached the expiry yet,
                    // we can schedule a delay until the expiry time, then trigger a recheck!
                    val delayMillis = profile.proExpiryTimestamp - now
                    if (delayMillis > 0) {
                        android.util.Log.d("MainVM", "Scheduling expiry recheck in ${delayMillis / 1000} seconds")
                        delay(delayMillis + 1000) // add 1 second padding
                        
                        val currentProfile = com.mknlabs.expensetracker.data.local.UserProfileDataStore.getUserProfileFlow(appContext).first()
                        val currentNow = System.currentTimeMillis()
                        if (currentProfile.accountTier == "PREMIUM" && currentProfile.proExpiryTimestamp in 1..<currentNow) {
                            android.util.Log.d("MainVM", "Premium expired during session. Downgrading user locally.")
                            AppSettingsDataStore.updateAppSettings(appContext) { current ->
                                current.copy(userTier = com.mknlabs.expensetracker.models.UserTier.FREE)
                            }
                            com.mknlabs.expensetracker.data.local.UserProfileDataStore.updateUserProfile(appContext) { currentP ->
                                currentP.copy(
                                    accountTier = "FREE",
                                    updatedAtMillis = currentNow
                                )
                            }
                            com.mknlabs.expensetracker.workers.SyncWorker.startImmediate(appContext)
                        }
                    }
                }
            }
        }
    }

    fun setTransactionObservationEnabled(enabled: Boolean) {
        observeTransactions.value = enabled
    }

    /**
     * Persists [transaction] as a quick-entry favorite template. The display
     * title prefers the note (merchant/description) and falls back to the
     * category name, then the category id — so a blank-note favorite is still
     * recognizable in the carousel.
     *
     * Used by the edit-screen star (the transaction already has its real id).
     * The add-mode star is handled inside [saveTransaction] instead, so the
     * favorite is created only after the transaction received its assigned id.
     */
    fun saveAsFavorite(transaction: Transaction) {
        viewModelScope.launch {
            favoriteTransactionRepository.saveFavorite(toFavorite(transaction))
        }
    }

    /**
     * Maps a transaction to its favorite template. [FavoriteTransaction.transactionId]
     * links the favorite to the source transaction — the unique index on it dedupes
     * re-favoriting. A blank id is stored as null so unsaved/legacy favorites never
     * collide with real ones (SQLite unique indexes treat NULLs as distinct).
     */
    private fun toFavorite(transaction: Transaction): FavoriteTransaction {
        val categoryName = _uiState.value.categories
            .firstOrNull { it.id == transaction.categoryId }
            ?.name.orEmpty()
        val title = transaction.note.trim()
            .ifBlank { categoryName.ifBlank { transaction.categoryId.toString() } }
        return FavoriteTransaction(
            title = title,
            amountMinor = transaction.amountMinor,
            transactionTypeId = transaction.transactionTypeId,
            categoryId = transaction.categoryId,
            paymentTypeId = transaction.paymentTypeId,
            note = transaction.note,
            transactionId = transaction.id.takeIf { it.isNotBlank() },
            isPinned = true,
            createdAt = System.currentTimeMillis()
        )
    }

    fun removeFavorite(id: String) {
        viewModelScope.launch {
            favoriteTransactionRepository.removeFavoriteById(id)
        }
    }

    fun saveTransaction(
        transaction: Transaction,
        recurringDraft: RecurringTransactionDraft?,
        existingRule: RecurringTransactionRule?
    ) {
        viewModelScope.launch {
            val savedTransaction = transactionRepository.upsertTransaction(transaction)
            when {
                recurringDraft != null -> {
                    val initialNextRun = calculateInitialNextRun(
                        savedTransaction.createdAt,
                        recurringDraft.frequency
                    )
                    val rule = if (existingRule == null) {
                        recurringRuleRepository.upsertRule(
                            RecurringTransactionRule(
                                id = "",
                                transactionId = savedTransaction.id,
                                frequency = recurringDraft.frequency,
                                repeatCount = recurringDraft.repeatCount,
                                isEnabled = true,
                                remainingCount = recurringDraft.repeatCount - 1, // First one is already saved
                                anchorAt = savedTransaction.createdAt,
                                nextRunAt = initialNextRun,
                                lastRunAt = savedTransaction.createdAt,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                syncState = savedTransaction.syncState,
                                isDeleted = false
                            )
                        )
                    } else {
                        // copy() rather than a fresh rule: everything this screen
                        // does not edit has to survive the save — the EMI plan
                        // terms, the per-rule notification mute, the fired-alert
                        // marker. Re-saving the template must never downgrade a
                        // loan to REGULAR.
                        recurringRuleRepository.upsertRule(
                            existingRule.copy(
                                transactionId = savedTransaction.id,
                                frequency = recurringDraft.frequency,
                                repeatCount = recurringDraft.repeatCount,
                                // An EMI rule's remaining count and next run come
                                // from its slot ledger, re-derived below — never
                                // from the repeat count.
                                remainingCount = if (existingRule.isInstallment) {
                                    existingRule.remainingCount
                                } else {
                                    recurringDraft.repeatCount - 1
                                },
                                lastRunAt = existingRule.lastRunAt ?: savedTransaction.createdAt,
                                updatedAt = System.currentTimeMillis(),
                                isDeleted = false
                            )
                        )
                    }

                    when {
                        // An EMI draft (Add Transaction) materializes the plan on
                        // the rule just created (deterministic slots, idempotent),
                        // then adopts the transaction the user just saved as
                        // installment #1: the plan's first due date defaults to that
                        // transaction's date, so without this the worker's due
                        // reconciliation would immediately pay the same installment a
                        // second time with a transaction of its own.
                        recurringDraft.plan != null -> recurringDraft.plan.let { plan ->
                            val converted = recurringRuleRepository.convertToInstallment(
                                ruleId = rule.id,
                                totalAmountMinor = plan.totalAmountMinor,
                                installmentAmountMinor = plan.installmentAmountMinor,
                                totalInstallments = plan.totalInstallments,
                                firstDueAt = plan.firstDueAt
                            )
                            if (converted != null) {
                                recurringRuleRepository.settleOccurrenceWithTransaction(
                                    occurrenceId = InstallmentOccurrence.idFor(converted.id, 1),
                                    transactionId = savedTransaction.id,
                                    paidAt = savedTransaction.createdAt
                                )
                            }
                        }

                        // Re-timing an existing loan: rebuild the slots from the
                        // retained terms so the ledger matches the new frequency
                        // instead of silently disagreeing with the rule. Paid and
                        // skipped slots are revived by their deterministic ids,
                        // so only the pending dates move.
                        rule.isInstallment && existingRule != null &&
                            (rule.frequency != existingRule.frequency ||
                                rule.repeatCount != existingRule.repeatCount) -> {
                            val perInstallment = rule.installmentAmountMinor
                            // First due date lives on the slots, not the rule: the
                            // earliest one keeps the plan's start where it was.
                            val firstDueAt = recurringRuleRepository.getOccurrences(rule.id)
                                .minOfOrNull { it.dueAt }
                            if (perInstallment != null && firstDueAt != null && rule.repeatCount > 0) {
                                recurringRuleRepository.convertToInstallment(
                                    ruleId = rule.id,
                                    // The editor's invariant is total = per × count,
                                    // so a changed count re-derives the total rather
                                    // than keeping a now-inconsistent one.
                                    totalAmountMinor = perInstallment * rule.repeatCount,
                                    installmentAmountMinor = perInstallment,
                                    totalInstallments = rule.repeatCount,
                                    firstDueAt = firstDueAt
                                )
                            }
                        }
                    }
                }

                existingRule != null -> recurringRuleRepository.deleteRule(existingRule.id)
            }
            
            // Check budget and notify if needed
            checkBudgetUseCase(savedTransaction)
            // Large-expense heads-up (spec category 3, Free tier) now fires inside
            // TransactionRepository.upsertTransaction so SMS imports and recurring
            // auto-adds get it too — no double-fire here.

            // If a recurring rule was added/updated, trigger immediate processing
            if (recurringDraft != null) {
                com.mknlabs.expensetracker.workers.RecurringTransactionWorker.enqueueImmediate(appContext)
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

    /**
     * Replicates [source] as a brand-new transaction: every field is copied
     * except the identity/timestamp ones — the repository assigns a fresh id,
     * the date/time becomes "now", and the recurring link is dropped so the
     * copy is a standalone one-time transaction. Callers must gate this on the
     * source not being part of a recurring series.
     *
     * @param onDuplicated Invoked once the copy has been persisted, with the
     *   created transaction (carrying its fresh id) — lets the UI offer an
     *   Undo action that can soft-delete exactly that copy.
     */
    fun duplicateTransaction(
        source: Transaction,
        onDuplicated: (Transaction) -> Unit = {}
    ) {
        viewModelScope.launch {
            // Recurring transactions (the main one or auto-created instances) are
            // never duplicated. The UI gates this, but guard here as a backstop.
            if (source.sourceRecurringRuleId != null) return@launch
            val now = System.currentTimeMillis()
            val created = transactionRepository.upsertTransaction(
                source.copy(
                    id = "",
                    createdAt = now,
                    updatedAt = now,
                    isDeleted = false,
                    sourceRecurringRuleId = null
                )
            )
            _uiEvent.emit(MainUiEvent.TransactionOperationCompleted)
            onDuplicated(created)
        }
    }

    /**
     * Brings a soft-deleted transaction back (Undo for swipe-to-delete): the
     * transaction is re-upserted with [Transaction.isDeleted] = false, and if it
     * was a recurring template ([rule] captured before the delete), the rule is
     * restored as well. The repository re-assigns a pending-upload sync state.
     */
    fun restoreTransaction(
        transaction: Transaction,
        rule: RecurringTransactionRule?
    ) {
        viewModelScope.launch {
            transactionRepository.upsertTransaction(
                transaction.copy(
                    isDeleted = false,
                    updatedAt = System.currentTimeMillis()
                )
            )
            rule?.let {
                recurringRuleRepository.upsertRule(
                    it.copy(
                        isDeleted = false,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
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
                com.mknlabs.expensetracker.data.local.AppSettingsDataStore.clearAll(appContext)
                com.mknlabs.expensetracker.data.local.UserProfileDataStore.clearAll(appContext)
                com.mknlabs.expensetracker.data.local.AppLockPreferences.clearAll(appContext)

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

    fun setRecurringNotificationsEnabled(ruleId: String, enabled: Boolean) {
        viewModelScope.launch {
            recurringRuleRepository.setNotificationsEnabled(ruleId, enabled)
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

    /**
     * Save (or re-save) an EMI plan on an existing rule. The rule's schedule is
     * updated first because slot materialization reads the rule's frequency —
     * convertToInstallment then (re)builds the occurrence list from the plan
     * terms, reviving slots of a previously converted plan with paid state intact.
     */
    fun saveRecurringPlan(
        ruleId: String,
        frequency: RecurringFrequency,
        plan: RecurringPlanEdit
    ) {
        viewModelScope.launch {
            val existingRule = _uiState.value.recurringRules.find { it.id == ruleId } ?: return@launch
            recurringRuleRepository.upsertRule(
                existingRule.copy(
                    frequency = frequency,
                    repeatCount = plan.totalInstallments
                )
            )
            recurringRuleRepository.convertToInstallment(
                ruleId = ruleId,
                totalAmountMinor = plan.totalAmountMinor,
                installmentAmountMinor = plan.installmentAmountMinor,
                totalInstallments = plan.totalInstallments,
                firstDueAt = plan.firstDueAt
            )
            // Reconcile immediately so an already-due first installment does not
            // wait for the next heartbeat.
            com.mknlabs.expensetracker.workers.RecurringTransactionWorker.enqueueImmediate(appContext)
        }
    }

    /** Hide an EMI plan (soft-delete the schedule, keep terms — fully reversible). */
    fun convertRecurringToRegular(ruleId: String) {
        viewModelScope.launch {
            recurringRuleRepository.convertToRegular(ruleId)
        }
    }

    // ── Installment (EMI) ledger actions ────────────────────────────────────

    /**
     * Settle the installments selected in the ledger sheet. Every slot goes
     * through the repository, which is idempotent per slot — one already settled
     * by the worker (or by a second tap) is skipped rather than double-paid, and
     * only genuinely new payments are reported for the toast.
     */
    fun payInstallments(occurrenceIds: List<String>) {
        if (occurrenceIds.isEmpty()) return
        viewModelScope.launch {
            val paid = recurringRuleRepository.payInstallments(
                occurrenceIds = occurrenceIds,
                paidAt = System.currentTimeMillis()
            )
            if (paid > 0) {
                _uiEvent.emit(MainUiEvent.InstallmentsPaid(paid))
            }
        }
    }

    /** Waive a pending installment for this cycle: no transaction, amount stays owed. */
    fun skipInstallment(occurrenceId: String) {
        viewModelScope.launch {
            recurringRuleRepository.skipInstallment(occurrenceId)
        }
    }

    /** Return a settled installment to pending — undoing a payment withdraws its transaction. */
    fun undoInstallment(occurrenceId: String) {
        viewModelScope.launch {
            recurringRuleRepository.undoInstallment(occurrenceId)
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
