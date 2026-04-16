package com.mkn0079.expensetracker

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.getAppLockSecurityQuestionPrompt
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.data.local.AppLockPreferences
import com.mkn0079.expensetracker.data.local.UserProfileDataStore
import com.mkn0079.expensetracker.data.repository.JsonImportResult
import com.mkn0079.expensetracker.models.AppSettings
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.ui.components.AppLockOverlay
import com.mkn0079.expensetracker.ui.components.MainScaffold
import com.mkn0079.expensetracker.ui.navigation.AppLockFlow
import com.mkn0079.expensetracker.ui.navigation.routesKeepingTransactionsWarm
import com.mkn0079.expensetracker.notifications.NotificationHelper
import com.mkn0079.expensetracker.notifications.NotificationScheduler
import com.mkn0079.expensetracker.ui.screens.OnboardingScreen
import com.mkn0079.expensetracker.ui.viewmodels.MainViewModel
import com.mkn0079.expensetracker.utils.BiometricAuthManager
import com.mkn0079.expensetracker.utils.findFragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.mkn0079.expensetracker.workers.AutoBackupScheduler

private fun AppSettings.toTransactionCardCustomizationSettings(): TransactionCardCustomizationSettings {
    return TransactionCardCustomizationSettings(
        showIncomeExpenseLabels = transactionCardShowIncomeExpenseLabels,
        showTransactionDate = transactionCardShowTransactionDate,
        showPaymentMethod = transactionCardShowPaymentMethod,
        showTransactionTime = transactionCardShowTransactionTime,
        showCategoryIcon = transactionCardShowCategoryIcon,
        showDateSeparators = transactionCardShowDateSeparators
    )
}

private fun AppSettings.withTransactionCardCustomizationSettings(
    settings: TransactionCardCustomizationSettings
): AppSettings {
    return copy(
        transactionCardShowIncomeExpenseLabels = settings.showIncomeExpenseLabels,
        transactionCardShowTransactionDate = settings.showTransactionDate,
        transactionCardShowPaymentMethod = settings.showPaymentMethod,
        transactionCardShowTransactionTime = settings.showTransactionTime,
        transactionCardShowCategoryIcon = settings.showCategoryIcon,
        transactionCardShowDateSeparators = settings.showDateSeparators
    )
}

@Composable
fun MainScreen(
    isReady: Boolean,
    appSettings: AppSettings,
    userProfile: UserProfile,
    initialNavDestination: String? = null
) {
    val rawContext = LocalContext.current
    val context = rawContext.applicationContext
    val activity = rawContext.findFragmentActivity()
    val biometricAuthenticator = remember(activity) {
        activity?.let(BiometricAuthManager::createAuthenticator)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var appLockState by remember { mutableStateOf(AppLockPreferences.getCachedState()) }
    val showOnboarding = appSettings.showOnboardingScreen
    var currentRoute by remember { mutableStateOf("home") }
    var previousRoute by remember { mutableStateOf("home") }
    var profileOriginRoute by remember { mutableStateOf("home") }
    var isBottomBarVisible by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var addTransactionDraftAmount by remember { mutableStateOf<String?>(null) }
    var addTransactionDraftNote by remember { mutableStateOf<String?>(null) }
    val selectedCurrencyId = appSettings.currencyId
    val selectedDateFormatPattern = appSettings.dateFormatPattern
    val selectedTimeFormat = appSettings.timeFormat
    val isAppLockEnabled = appSettings.appLockEnabled
    val isBiometricEnabled = appSettings.biometricLockEnabled
    val isBlurInRecentsEnabled = appSettings.blurInRecentsEnabled
    val isScreenshotProtectionEnabled = appSettings.screenshotProtectionEnabled
    val autoLockDurationMinutes = appSettings.appLockTimeoutMinutes
    val isDailyReminderEnabled = appSettings.notificationsEnabled
    val isBudgetLimitAlertsEnabled = appSettings.budgetLimitAlertsEnabled
    val isMissedEntryReminderEnabled = appSettings.missedEntryReminderEnabled
    val isAutoBackupEnabled = appSettings.isAutoBackupEnabled
    val autoBackupFrequencyDays = appSettings.autoBackupFrequencyDays
    val transactionCardCustomizationSettings = remember(appSettings) {
        appSettings.toTransactionCardCustomizationSettings()
    }
    val biometricAvailability = BiometricAuthManager.getAvailability(rawContext)
    val hasAppLockPin = appLockState.hasPin
    val canUseBiometricOnLockScreen = isBiometricEnabled && hasAppLockPin && biometricAvailability.isAvailable
    val initiallyRequiresUnlock = remember(
        isAppLockEnabled,
        hasAppLockPin,
        autoLockDurationMinutes,
        appLockState.lastBackgroundedAtMillis,
        appLockState.lastUnlockedAtMillis
    ) {
        isAppLockEnabled &&
            hasAppLockPin &&
            AppLockPreferences.shouldRequireUnlockFromMemory(
                autoLockDurationMinutes = autoLockDurationMinutes
            )
    }
    var appLockFlow by remember {
        mutableStateOf<AppLockFlow?>(
            if (initiallyRequiresUnlock) AppLockFlow.Unlock else null
        )
    }
    var isAppUnlocked by remember {
        mutableStateOf(!initiallyRequiresUnlock)
    }
    var hasPromptedBiometricForCurrentUnlock by remember(appLockFlow, canUseBiometricOnLockScreen) {
        mutableStateOf(false)
    }
    var isAppLockSuppressed by remember { mutableStateOf(false) }

    val showToast: (String) -> Unit = { message ->
        Toast.makeText(rawContext, message, Toast.LENGTH_SHORT).show()
    }

    val performBiometricUpdate: (Boolean) -> Unit = { enabled ->
        AppLockPreferences.setBiometricEnabled(context, enabled)
        appLockState = AppLockPreferences.getCachedState()
        coroutineScope.launch {
            AppSettingsDataStore.updateAppSettings(context) { settings ->
                settings.copy(biometricLockEnabled = enabled)
            }
        }
    }

    val updateBiometricLockEnabled: (Boolean) -> Unit = { enabled ->
        if (enabled) {
            // Turning ON: Require verification
            if (biometricAuthenticator == null) {
                showToast("Biometric initialization failed.")
            } else if (!biometricAvailability.isAvailable) {
                showToast(biometricAvailability.message ?: "Biometrics unavailable.")
            } else {
                biometricAuthenticator.authenticate(
                    title = "Confirm Identity",
                    subtitle = "Verify biometric to enable biometric lock.",
                    onSuccess = { performBiometricUpdate(true) },
                    onFailure = { showToast("Authentication failed.") }
                    // onCancel: do nothing (switch stays off)
                )
            }
        } else {
            // Turning OFF: Immediate
            performBiometricUpdate(false)
        }
    }

    val disableAppLock: (Boolean) -> Unit = { navigateHome ->
        AppLockPreferences.clear(context)
        appLockState = AppLockPreferences.getCachedState()
        isAppUnlocked = true
        appLockFlow = null
        if (navigateHome) {
            currentRoute = "home"
            isBottomBarVisible = false
        }
        coroutineScope.launch {
            AppSettingsDataStore.updateAppSettings(context) { settings ->
                settings.copy(
                    appLockEnabled = false,
                    biometricLockEnabled = false
                )
            }
        }
    }

    val completeUnlock: () -> Unit = {
        isAppUnlocked = true
        appLockFlow = null
        isBottomBarVisible = false
        val unlockedAtMillis = AppLockPreferences.markUnlockedInMemory()
        appLockState = AppLockPreferences.getCachedState()
        coroutineScope.launch(Dispatchers.IO) {
            AppLockPreferences.persistUnlocked(context, unlockedAtMillis)
        }
    }

    val unlockWithBiometric: () -> Unit = {
        if (biometricAuthenticator == null) {
            showToast("Biometric authentication is unavailable on this screen.")
        } else if (!biometricAvailability.isAvailable) {
            showToast(
                biometricAvailability.message
                    ?: "Biometric authentication is not available right now."
            )
        } else {
            biometricAuthenticator.authenticate(
                title = "Unlock Expense Tracker",
                subtitle = "Verify your biometric to continue.",
                negativeButtonText = "Use PIN",
                onSuccess = completeUnlock,
                onFailure = { errorMessage ->
                    showToast(errorMessage.ifBlank { "Biometric verification failed." })
                }
            )
        }
    }

    DisposableEffect(
        lifecycleOwner,
        showOnboarding,
        isAppLockEnabled,
        hasAppLockPin,
        isAppUnlocked,
        autoLockDurationMinutes
    ) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                event == Lifecycle.Event.ON_STOP &&
                !showOnboarding &&
                isAppLockEnabled &&
                hasAppLockPin
            ) {
                val backgroundedAtMillis = AppLockPreferences.markBackgroundedInMemory()
                appLockState = AppLockPreferences.getCachedState()
                coroutineScope.launch(Dispatchers.IO) {
                    AppLockPreferences.persistBackgrounded(context, backgroundedAtMillis)
                }
                if (autoLockDurationMinutes <= 0 && !isAppLockSuppressed) {
                    isAppUnlocked = false
                    appLockFlow = AppLockFlow.Unlock
                }
            }

            if (
                event == Lifecycle.Event.ON_START &&
                !showOnboarding &&
                isAppLockEnabled &&
                hasAppLockPin
            ) {
                if (isAppLockSuppressed) {
                    isAppLockSuppressed = false
                } else if (
                    isAppUnlocked &&
                    appLockFlow == null &&
                    AppLockPreferences.shouldRequireUnlockFromMemory(
                        autoLockDurationMinutes = autoLockDurationMinutes
                    )
                ) {
                    isAppUnlocked = false
                    appLockFlow = AppLockFlow.Unlock
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showOnboarding) {
        OnboardingScreen(
            onFinish = {
                currentRoute = "home"
                isBottomBarVisible = false
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(
                            showOnboardingScreen = false
                        )
                    }
                }
            }
        )
        return
    }

    LaunchedEffect(initialNavDestination) {
        if (initialNavDestination == NotificationHelper.DESTINATION_ADD_TRANSACTION) {
            currentRoute = "add_transaction"
            isBottomBarVisible = false
        }
    }

    LaunchedEffect(isDailyReminderEnabled) {
        if (isDailyReminderEnabled) {
            NotificationScheduler.startDailyReminders(context)
        } else {
            NotificationScheduler.stopDailyReminders(context)
        }
    }

    LaunchedEffect(isAutoBackupEnabled, autoBackupFrequencyDays) {
        AutoBackupScheduler.scheduleOrUpdate(context, isAutoBackupEnabled, autoBackupFrequencyDays)
    }

    LaunchedEffect(
        showOnboarding,
        isAppLockEnabled,
        hasAppLockPin,
        isAppUnlocked,
        autoLockDurationMinutes
    ) {
        if (!showOnboarding && isAppLockEnabled && hasAppLockPin && !isAppUnlocked && appLockFlow == null) {
            appLockFlow = AppLockFlow.Unlock
        }
    }

    LaunchedEffect(appLockFlow, canUseBiometricOnLockScreen, isReady) {
        if (
            isReady &&
            appLockFlow == AppLockFlow.Unlock &&
            canUseBiometricOnLockScreen &&
            !hasPromptedBiometricForCurrentUnlock
        ) {
            hasPromptedBiometricForCurrentUnlock = true
            unlockWithBiometric()
        }
    }

    val mainViewModel: MainViewModel = viewModel()
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(currentRoute) {
        mainViewModel.setTransactionObservationEnabled(
            currentRoute in routesKeepingTransactionsWarm
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MainScaffold(
            currentRoute = currentRoute,
            previousRoute = previousRoute,
            profileOriginRoute = profileOriginRoute,
            isBottomBarVisible = isBottomBarVisible,
            transactions = mainUiState.transactions,
            transactionCount = mainUiState.transactionCount,
            recurringRules = mainUiState.recurringRules,
            selectedTransaction = selectedTransaction,
            addTransactionDraftAmount = addTransactionDraftAmount,
            addTransactionDraftNote = addTransactionDraftNote,
            categories = mainUiState.categories,
            paymentMethods = mainUiState.paymentMethods,
            transactionCardCustomizationSettings = transactionCardCustomizationSettings,
            userProfile = userProfile,
            selectedCurrencyId = selectedCurrencyId,
            selectedDateFormatPattern = selectedDateFormatPattern,
            selectedTimeFormat = selectedTimeFormat,
            isAppLockEnabled = isAppLockEnabled,
            hasAppLockPin = hasAppLockPin,
            isBiometricEnabled = isBiometricEnabled,
            isBlurInRecentsEnabled = isBlurInRecentsEnabled,
            isScreenshotProtectionEnabled = isScreenshotProtectionEnabled,
            isDailyReminderEnabled = isDailyReminderEnabled,
            isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
            isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
            autoLockDurationMinutes = autoLockDurationMinutes,
            isAutoBackupEnabled = isAutoBackupEnabled,
            autoBackupFrequencyDays = autoBackupFrequencyDays,
            onRouteChange = { route ->
                if (route == "add_transaction" && currentRoute != "itemized_calculator") {
                    previousRoute = currentRoute
                }
                currentRoute = route
            },
            onProfileOriginRouteChange = { profileOriginRoute = it },
            onBottomBarVisibilityChange = { isBottomBarVisible = it },
            onSelectedTransactionChange = { selectedTransaction = it },
            onAddTransactionDraftAmountChange = { addTransactionDraftAmount = it },
            onAddTransactionDraftNoteChange = { addTransactionDraftNote = it },
            onSaveTransaction = mainViewModel::saveTransaction,
            onDeleteTransaction = mainViewModel::deleteTransaction,
            onDeleteRecurring = mainViewModel::deleteRecurring,
            onRecurringEnabledChange = mainViewModel::setRecurringEnabled,
            onCreateCustomCategory = mainViewModel::createCustomCategory,
            onCreateCustomPaymentType = mainViewModel::createCustomPaymentMethod,
            onDeleteCustomCategory = mainViewModel::deleteCustomCategory,
            onDeleteCustomPaymentType = mainViewModel::deleteCustomPaymentMethod,
            onTransactionCardCustomizationSettingsChange = { updatedSettings ->
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.withTransactionCardCustomizationSettings(updatedSettings)
                    }
                }
            },
            onUserProfileChange = { updatedProfile ->
                coroutineScope.launch {
                    UserProfileDataStore.updateUserProfile(context) {
                        updatedProfile
                    }
                }
            },
            onSelectedCurrencyIdChange = { currencyId ->
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(currencyId = currencyId)
                    }
                }
            },
            onSelectedDateFormatPatternChange = { dateFormatPattern ->
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(dateFormatPattern = dateFormatPattern)
                    }
                }
            },
            onSelectedTimeFormatChange = { timeFormat ->
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(timeFormat = timeFormat)
                    }
                }
            },
            onDailyReminderChange = { isEnabled ->
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(notificationsEnabled = isEnabled)
                    }
                }
            },
            onBudgetLimitAlertsChange = { isEnabled ->
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(budgetLimitAlertsEnabled = isEnabled)
                    }
                }
            },
            onMissedEntryReminderChange = { isEnabled ->
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(missedEntryReminderEnabled = isEnabled)
                    }
                }
            },
            onDatabaseBackupFileSelected = { uri ->
                mainViewModel.backupDatabase(
                    uri = uri,
                    onComplete = {
                        showToast("Database backup saved.")
                    },
                    onError = {
                        showToast("Unable to save the database backup. Please try again.")
                    }
                )
            },
            onDatabaseRestoreFileSelected = { uri ->
                mainViewModel.restoreDatabase(
                    uri = uri,
                    onComplete = {
                        selectedTransaction = null
                        addTransactionDraftAmount = null
                        addTransactionDraftNote = null
                        showToast("Database restored. Reloading app.")
                        activity?.recreate()
                    },
                    onError = {
                        showToast("Unable to restore the database backup. Please try again.")
                    }
                )
            },
            onJsonExportFileSelected = { uri ->
                mainViewModel.exportJson(
                    uri = uri,
                    onComplete = { result ->
                        showToast(
                            "JSON exported: ${result.exportedTransactions} transactions, " +
                                "${result.exportedBudgets} budgets."
                        )
                    },
                    onError = {
                        showToast("Unable to export JSON. Please try again.")
                    }
                )
            },
            onJsonImportFileSelected = { uri ->
                mainViewModel.importJson(
                    uri = uri,
                    onComplete = { result ->
                        showToast(result.toJsonImportMessage())
                    },
                    onError = {
                        showToast("Unable to import JSON. Check the file and try again.")
                    }
                )
            },
            onLegacyImportFileSelected = { uri ->
                mainViewModel.importLegacyBackup(
                    uri = uri,
                    onComplete = { result ->
                        showToast(
                            "Imported ${result.importedTransactions} legacy transactions. " +
                                "Skipped ${result.skippedTransactions} existing."
                        )
                    },
                    onError = {
                        showToast("Legacy import failed. Check the backup file and try again.")
                    }
                )
            },
            onDeleteAllTransactionsClick = {
                mainViewModel.deleteAllTransactions(
                    onComplete = {
                        selectedTransaction = null
                        addTransactionDraftAmount = null
                        addTransactionDraftNote = null
                        showToast("All transactions deleted.")
                    },
                    onError = {
                        showToast("Unable to delete transactions. Please try again.")
                    }
                )
            },
            onBiometricLockChange = updateBiometricLockEnabled,
            onBlurInRecentsChange = { enabled ->
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(blurInRecentsEnabled = enabled)
                    }
                }
            },
            onScreenshotProtectionChange = { enabled ->
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(screenshotProtectionEnabled = enabled)
                    }
                }
            },
            onAutoLockDurationChange = { minutes ->
                AppLockPreferences.setAutoLockDurationMinutes(context, minutes)
                appLockState = AppLockPreferences.getCachedState()
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(appLockTimeoutMinutes = minutes)
                    }
                }
            },
            onAutoBackupEnabledChange = { enabled ->
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(isAutoBackupEnabled = enabled)
                    }
                }
            },
            onAutoBackupFrequencyChange = { days ->
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(autoBackupFrequencyDays = days)
                    }
                }
            },
            onAppLockToggleChange = { shouldEnable ->
                if (shouldEnable) {
                    if (hasAppLockPin) {
                        coroutineScope.launch {
                            AppSettingsDataStore.updateAppSettings(context) { settings ->
                                settings.copy(appLockEnabled = true)
                            }
                        }
                    } else {
                        appLockFlow = AppLockFlow.Setup
                    }
                } else {
                    disableAppLock(false)
                }
            },
            onPrepareForExternalActivity = { isAppLockSuppressed = true }
        )

        AppLockOverlay(
            appLockFlow = appLockFlow,
            isAppUnlocked = isAppUnlocked,
            biometricEnabled = canUseBiometricOnLockScreen,
            isBiometricAvailable = canUseBiometricOnLockScreen,
            securityQuestionPrompt = getAppLockSecurityQuestionPrompt(
                appLockState.securityQuestionId
            ).orEmpty(),
            onBackClick = { appLockFlow = null },
            onBiometricClick = unlockWithBiometric,
            onSetupComplete = { pin, questionId, answer ->
                // 1. Dismiss UI immediately
                completeUnlock()

                // 2. Background heavy work
                coroutineScope.launch(Dispatchers.Default) {
                    AppLockPreferences.savePin(context, pin)
                    AppLockPreferences.saveSecurityQuestion(context, questionId, answer)
                    appLockState = AppLockPreferences.getCachedState()

                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(appLockEnabled = true)
                    }
                }
            },
            onUnlockSuccess = completeUnlock,
            validateUnlockPin = { pin ->
                AppLockPreferences.validatePinForUnlock(context, pin).also {
                    appLockState = AppLockPreferences.getCachedState()
                }
            },
            onForgotPinRecovery = {
                disableAppLock(true)
            },
            validateSecurityAnswer = { answer ->
                AppLockPreferences.validateSecurityAnswer(context, answer)
            }
        )
    }
}

private fun JsonImportResult.toJsonImportMessage(): String {
    return "Imported $importedTransactions tx, $importedBudgets budgets, " +
        "$importedRecurringRules rules. Skipped $skippedTransactions tx duplicates."
}
