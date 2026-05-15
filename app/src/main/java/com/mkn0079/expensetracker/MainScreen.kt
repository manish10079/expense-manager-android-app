package com.mkn0079.expensetracker

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.data.local.AppLockPreferences
import com.mkn0079.expensetracker.data.local.UserProfileDataStore
import com.mkn0079.expensetracker.domain.repository.JsonImportResult
import com.mkn0079.expensetracker.models.AppSettings
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.ui.components.AppLockOverlay
import com.mkn0079.expensetracker.ui.components.MainScaffold
import com.mkn0079.expensetracker.ui.navigation.AppRoute
import com.mkn0079.expensetracker.ui.navigation.AppLockFlow
import com.mkn0079.expensetracker.ui.navigation.rememberMainNavigationState
import com.mkn0079.expensetracker.ui.navigation.routesKeepingTransactionsWarm
import com.mkn0079.expensetracker.notifications.NotificationScheduler
import com.mkn0079.expensetracker.ui.screens.OnboardingScreen
import com.mkn0079.expensetracker.ui.viewmodels.MainViewModel
import com.mkn0079.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mkn0079.expensetracker.utils.toAmountFormatPreferences
import com.mkn0079.expensetracker.utils.BiometricAuthManager
import com.mkn0079.expensetracker.utils.findFragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.mkn0079.expensetracker.workers.AutoBackupScheduler
import com.mkn0079.expensetracker.utils.AppRestartUtils

private fun AppSettings.toTransactionCardCustomizationSettings(): TransactionCardCustomizationSettings {
    return TransactionCardCustomizationSettings(
        showIncomeExpenseLabels = transactionCardShowIncomeExpenseLabels,
        showTransactionDate = transactionCardShowTransactionDate,
        showPaymentMethod = transactionCardShowPaymentMethod,
        showTransactionTime = transactionCardShowTransactionTime,
        showCategoryIcon = transactionCardShowCategoryIcon,
        showCategoryLabel = transactionCardShowCategoryLabel,
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
        transactionCardShowCategoryLabel = settings.showCategoryLabel,
        transactionCardShowDateSeparators = settings.showDateSeparators
    )
}

@Composable
fun MainScreen(
    isReady: Boolean,
    appSettings: AppSettings,
    userProfile: UserProfile,
    initialNavDestination: String? = null,
    isRecoveryPerformed: Boolean = false,
    onRecoveryConsumed: () -> Unit = {}
) {
    val rawContext = LocalContext.current
    val context = rawContext.applicationContext
    val mainViewModel: MainViewModel = viewModel()
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val activity = rawContext.findFragmentActivity()
    val biometricAuthenticator = remember(activity) {
        activity?.let(BiometricAuthManager::createAuthenticator)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val monetizationViewModel: MonetizationViewModel = viewModel()
    var appLockState by remember { mutableStateOf(AppLockPreferences.getCachedState()) }
    val showOnboarding = appSettings.showOnboardingScreen
    val navigationState = rememberMainNavigationState()
    val selectedCurrencyId = appSettings.currencyId
    val amountFormatPreferences = remember(appSettings) {
        appSettings.toAmountFormatPreferences()
    }
    val selectedDateFormatPattern = appSettings.dateFormatPattern
    val selectedTimeFormat = appSettings.timeFormat
    val isAppLockEnabled = appSettings.appLockEnabled
    val isBiometricEnabled = appSettings.biometricLockEnabled
    val isScrambledPinKeypadEnabled = appSettings.scrambledPinKeypadEnabled
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
    val scrambledPinKeypadAccessStatus by monetizationViewModel
        .getAccessStatus(Feature.SCRAMBLED_PIN_KEYPAD)
        .collectAsStateWithLifecycle()
    val isScrambledPinKeypadAccessGranted = scrambledPinKeypadAccessStatus is AccessStatus.Granted
    val isScrambledPinKeypadEffective =
        isScrambledPinKeypadEnabled && isScrambledPinKeypadAccessGranted
    val biometricAvailability = BiometricAuthManager.getAvailability(rawContext)
    val hasAppLockPin = remember(appLockState) {
        AppLockPreferences.hasPin(context)
    }
    var appLockFlow by remember { mutableStateOf<AppLockFlow?>(null) }
    var isAppLockSuppressed by remember { mutableStateOf(false) }
    var hasPromptedBiometricForCurrentUnlock by remember(appLockFlow) {
        mutableStateOf(false)
    }

    val shouldBlurForAppLock = appLockFlow != null



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
        mainViewModel.disableAppLock()
        appLockState = AppLockPreferences.getCachedState()
        appLockFlow = null
        if (navigateHome) {
            navigationState.navigateTo(AppRoute.Home)
            navigationState.updateBottomBarVisibility(false)
        }
    }

    val completeUnlock: () -> Unit = {
        appLockFlow = null
        navigationState.updateBottomBarVisibility(false)
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



    AnimatedContent(
        targetState = showOnboarding,
        transitionSpec = {
            if (!targetState) {
                // Premium Onboarding -> Home Transition
                (fadeIn(animationSpec = tween(500, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.95f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    ) +
                    slideInVertically(
                        initialOffsetY = { it / 20 }, // Subtle 5% upward movement
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    ))
                    .togetherWith(
                        fadeOut(animationSpec = tween(350, easing = LinearOutSlowInEasing)) +
                        scaleOut(
                            targetScale = 1.05f,
                            animationSpec = tween(350, easing = LinearOutSlowInEasing)
                        )
                    )
            } else {
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
            }
        },
        label = "onboarding_to_main_transition",
        modifier = Modifier.fillMaxSize()
    ) { isShowingOnboarding ->
        if (isShowingOnboarding) {
            OnboardingScreen(
                onFinish = { name, gender, dobMillis ->
                    navigationState.navigateTo(AppRoute.Home)
                    navigationState.updateBottomBarVisibility(false)
                    coroutineScope.launch {
                        // 1. Update Profile
                        UserProfileDataStore.updateUserProfile(context) { profile ->
                            profile.copy(
                                fullName = name.ifBlank { "Guest User" },
                                gender = gender,
                                dateOfBirthMillis = dobMillis
                            )
                        }

                        // 2. Hide Onboarding
                        AppSettingsDataStore.updateAppSettings(context) { settings ->
                            settings.copy(
                                showOnboardingScreen = false
                            )
                        }
                    }
                }
            )
        } else {
            LaunchedEffect(initialNavDestination) {
                if (AppRoute.fromRoute(initialNavDestination) == AppRoute.AddTransaction) {
                    navigationState.navigateTo(AppRoute.AddTransaction)
                    navigationState.updateBottomBarVisibility(false)
                }
            }

            LaunchedEffect(isRecoveryPerformed) {
                if (isRecoveryPerformed) {
                    navigationState.navigateTo(AppRoute.Home)
                    navigationState.clearTransactionDraftContext()
                    onRecoveryConsumed()
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

            LaunchedEffect(isScrambledPinKeypadEnabled, isScrambledPinKeypadAccessGranted) {
                if (isScrambledPinKeypadEnabled && !isScrambledPinKeypadAccessGranted) {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(scrambledPinKeypadEnabled = false)
                    }
                }
            }

            LaunchedEffect(navigationState.currentRoute) {
                mainViewModel.setTransactionObservationEnabled(
                    navigationState.currentRoute in routesKeepingTransactionsWarm
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(if (shouldBlurForAppLock) 24.dp else 0.dp)
                ) {
                    MainScaffold(
                        currentRoute = navigationState.currentRoute,
                        previousRoute = navigationState.previousRoute,
                        profileOriginRoute = navigationState.profileOriginRoute,
                        isBottomBarVisible = navigationState.isBottomBarVisible,
                        transactions = mainUiState.transactions,
                        transactionCount = mainUiState.transactionCount,
                        recurringRules = mainUiState.recurringRules,
                        selectedTransaction = navigationState.selectedTransaction,
                        addTransactionDraftAmount = navigationState.addTransactionDraftAmount,
                        addTransactionDraftNote = navigationState.addTransactionDraftNote,
                        categories = mainUiState.categories,
                        paymentMethods = mainUiState.paymentMethods,
                        transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                        userProfile = userProfile,
                        selectedCurrencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        selectedDateFormatPattern = selectedDateFormatPattern,
                        selectedTimeFormat = selectedTimeFormat,
                        isAppLockEnabled = isAppLockEnabled,
                        hasAppLockPin = hasAppLockPin,
                        isBiometricEnabled = isBiometricEnabled,
                        isScrambledPinKeypadEnabled = isScrambledPinKeypadEnabled,
                        isBlurInRecentsEnabled = isBlurInRecentsEnabled,
                        isScreenshotProtectionEnabled = isScreenshotProtectionEnabled,
                        isDailyReminderEnabled = isDailyReminderEnabled,
                        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                        autoLockDurationMinutes = autoLockDurationMinutes,
                        isAutoBackupEnabled = isAutoBackupEnabled,
                        autoBackupFrequencyDays = autoBackupFrequencyDays,
                        onRouteChange = navigationState::navigateTo,
                        onProfileOriginRouteChange = navigationState::updateProfileOriginRoute,
                        onBottomBarVisibilityChange = navigationState::updateBottomBarVisibility,
                        onSelectedTransactionChange = navigationState::updateSelectedTransaction,
                        onAddTransactionDraftAmountChange = navigationState::updateAddTransactionDraftAmount,
                        onAddTransactionDraftNoteChange = navigationState::updateAddTransactionDraftNote,
                        onSaveTransaction = mainViewModel::saveTransaction,
                        onDeleteTransaction = mainViewModel::deleteTransaction,
                        onDeleteRecurring = mainViewModel::deleteRecurring,
                        onRecurringEnabledChange = mainViewModel::setRecurringEnabled,
                        onUpdateRecurringRule = mainViewModel::updateRecurringRule,
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
                                    navigationState.clearTransactionDraftContext()
                                    showToast("Database restored. Reloading app.")
                                    AppRestartUtils.restartApp(rawContext)
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
                                    navigationState.clearTransactionDraftContext()
                                    showToast("All transactions deleted.")
                                },
                                onError = {
                                    showToast("Unable to delete transactions. Please try again.")
                                }
                            )
                        },
                        onBiometricLockChange = updateBiometricLockEnabled,
                        onScrambledPinKeypadChange = { enabled ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(scrambledPinKeypadEnabled = enabled)
                                }
                            }
                        },
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
                }

                if (appLockFlow != null) {
                    AppLockOverlay(
                        isReady = isReady,
                        appSettings = appSettings,
                        initialFlow = appLockFlow!!,
                        isAppUnlocked = true, // MainScreen only exists in Unlocked state
                        biometricEnabled = isBiometricEnabled && biometricAvailability.isAvailable,
                        scrambledPinKeypadEnabled = isScrambledPinKeypadEffective,
                        isBiometricAvailable = biometricAvailability.isAvailable,
                        securityQuestionPrompt = null, // Handled internally by AppLockOverlay
                        onBackClick = { appLockFlow = null },
                        autoTriggerBiometricOnShow = appLockFlow == AppLockFlow.Unlock,
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
        }
    }
}

private fun JsonImportResult.toJsonImportMessage(): String {
    return "Imported $importedTransactions tx, $importedBudgets budgets, " +
        "$importedRecurringRules rules. Skipped $skippedTransactions tx duplicates."
}
