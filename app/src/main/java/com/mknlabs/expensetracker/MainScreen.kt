package com.mknlabs.expensetracker

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.AppLockPreferences
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.domain.repository.JsonImportResult
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.InterstitialPlacement
import com.mknlabs.expensetracker.ui.components.AppLockOverlay
import com.mknlabs.expensetracker.ui.components.MainScaffold
import com.mknlabs.expensetracker.ui.components.PremiumGateSheet
import com.mknlabs.expensetracker.ui.components.ProPassRedeemDialog
import com.mknlabs.expensetracker.ui.navigation.AppRoute
import com.mknlabs.expensetracker.ui.navigation.AppLockFlow
import com.mknlabs.expensetracker.ui.navigation.rememberMainNavigationState
import com.mknlabs.expensetracker.ui.navigation.routesKeepingTransactionsWarm
import com.mknlabs.expensetracker.notifications.NotificationScheduler
import com.mknlabs.expensetracker.ui.screens.OnboardingScreen
import com.mknlabs.expensetracker.ui.viewmodels.MainViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.ui.viewmodels.AuthViewModel
import com.mknlabs.expensetracker.ui.screens.AuthContent
import com.mknlabs.expensetracker.workers.SyncWorker
import com.mknlabs.expensetracker.utils.toAmountFormatPreferences
import com.mknlabs.expensetracker.utils.BiometricAuthManager
import com.mknlabs.expensetracker.utils.findFragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.mknlabs.expensetracker.workers.AutoBackupScheduler
import com.mknlabs.expensetracker.utils.AppRestartUtils
import com.mknlabs.expensetracker.ui.theme.AdLoadingScrim
import com.mknlabs.expensetracker.ui.theme.AdLoadingText

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

@OptIn(ExperimentalMaterial3Api::class)
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
    val mainViewModel: MainViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val activity = rawContext.findFragmentActivity()
    val biometricAuthenticator = remember(activity) {
        activity?.let(BiometricAuthManager::createAuthenticator)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()
    val isAdLoading by monetizationViewModel.isAdLoading.collectAsStateWithLifecycle()
    var appLockState by remember { mutableStateOf(AppLockPreferences.getCachedState()) }
    val showOnboarding = appSettings.showOnboardingScreen
    val navigationState = rememberMainNavigationState()
    
    var showAuthSheet by remember { mutableStateOf(false) }
    var showPremiumSheet by remember { mutableStateOf(false) }
    var showAccountCreatedPopup by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAdExpiryWarningDialog by remember { mutableStateOf(false) }
    var showProPassRedeemDialog by remember { mutableStateOf(false) }
    var adExpiryMinutesRemaining by remember { mutableStateOf(0) }

    val authSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(showAccountCreatedPopup) {
        if (showAccountCreatedPopup) {
            kotlinx.coroutines.delay(2200)
            showAccountCreatedPopup = false
        }
    }

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
                showToast(rawContext.getString(R.string.toast_biometric_initialization_faile))
            } else if (!biometricAvailability.isAvailable) {
                showToast(biometricAvailability.messageRes?.let { rawContext.getString(it) } ?: rawContext.getString(R.string.msg_biometric_authentication_is_no))
            } else {
                biometricAuthenticator.authenticate(
                    title = rawContext.getString(R.string.title_confirm_identity),
                    subtitle = rawContext.getString(R.string.title_verify_biometric_to_enable_bio),
                    onSuccess = { performBiometricUpdate(true) },
                    onFailure = { showToast(rawContext.getString(R.string.toast_authentication_failed)) }
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
            showToast(rawContext.getString(R.string.toast_biometric_authentication_is_un))
        } else if (!biometricAvailability.isAvailable) {
            showToast(
                biometricAvailability.messageRes?.let { rawContext.getString(it) }
                    ?: rawContext.getString(R.string.msg_biometric_authentication_is_no)
            )
        } else {
            biometricAuthenticator.authenticate(
                title = rawContext.getString(R.string.title_unlock_expense_tracker),
                subtitle = rawContext.getString(R.string.title_verify_your_biometric_to_conti),
                negativeButtonText = rawContext.getString(R.string.btn_use_pin),
                onSuccess = completeUnlock,
                onFailure = { errorMessage ->
                    showToast(errorMessage.ifBlank { rawContext.getString(R.string.toast_authentication_failed) })
                }
            )
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {
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
                onFinish = { name, gender, dobMillis, financialGoal ->
                    navigationState.navigateTo(AppRoute.Home)
                    navigationState.updateBottomBarVisibility(false)
                    coroutineScope.launch {
                        // 1. Update Profile
                        UserProfileDataStore.updateUserProfile(context) { profile ->
                            profile.copy(
                                fullName = name.ifBlank { "Guest User" },
                                gender = gender,
                                dateOfBirthMillis = dobMillis,
                                financialGoal = financialGoal
                            )
                        }

                        // 2. Hide Onboarding
                        AppSettingsDataStore.updateAppSettings(context) { settings ->
                            settings.copy(
                                showOnboardingScreen = false
                            )
                        }
                    }
                },
                onSignUpSuccess = {
                    showAccountCreatedPopup = true
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

            LaunchedEffect(Unit) {
                mainViewModel.uiEvent.collect { event ->
                    android.util.Log.d("MainScreen", "Received UI Event: $event")
                    when (event) {
                        is com.mknlabs.expensetracker.ui.viewmodels.MainUiEvent.TransactionOperationCompleted -> {
                            // Handled internally in screens
                        }
                        is com.mknlabs.expensetracker.ui.viewmodels.MainUiEvent.ShowAdExpiryWarning -> {
                            android.util.Log.d("MainScreen", "Showing Expiry Warning Dialog")
                            adExpiryMinutesRemaining = event.minutesRemaining
                            showAdExpiryWarningDialog = true
                        }
                    }
                }
            }

            // Sync Firebase User with local UserProfileDataStore
            val firebaseUser by authViewModel.currentUser.collectAsState()
            LaunchedEffect(firebaseUser) {
                if (firebaseUser != null && !firebaseUser!!.isAnonymous) {
                    firebaseUser?.let { user -> 
                        val remotePhotoUrl = user.photoUrl
                        val currentProfile = UserProfileDataStore.getUserProfileFlow(context).first()
                        if (
                            currentProfile.fullName.isBlank() ||
                            currentProfile.fullName == "Guest User" ||
                            currentProfile.emailAddress.isBlank() ||
                            currentProfile.photoUri.isNullOrBlank()
                        ) {
                            UserProfileDataStore.updateUserProfile(context) { profile ->
                                profile.copy(
                                    fullName = if (profile.fullName.isBlank() || profile.fullName == "Guest User") {
                                        user.displayName ?: profile.fullName
                                    } else {
                                        profile.fullName
                                    },
                                    emailAddress = if (profile.emailAddress.isBlank()) {
                                        user.email ?: profile.emailAddress
                                    } else {
                                        profile.emailAddress
                                    },
                                    // Initially set remote, then localize in background
                                    photoUri = profile.photoUri ?: remotePhotoUrl?.toString()
                                )
                            }
                        }

                        // 1. Logic to localize network photo to prevent blinking
                        if (remotePhotoUrl != null && currentProfile.photoUri?.startsWith("http") == true) {
                            coroutineScope.launch {
                                val localUri = com.mknlabs.expensetracker.utils.ProfilePhotoManager.localizePhoto(context, remotePhotoUrl)
                                if (localUri != null) {
                                    UserProfileDataStore.updateUserProfile(context) { it.copy(photoUri = localUri) }
                                }
                            }
                        }

                        SyncWorker.startImmediate(context)
                    }
                } else if (firebaseUser == null) {
                    // Note: Clearing profile is now handled ONLY on explicit sign-out in the logout dialog.
                    // This prevents wiping the Guest profile created during onboarding.
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(if (shouldBlurForAppLock || isAdLoading) 24.dp else 0.dp)
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
                        appSettings = appSettings,
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
                        isAdsEnabled = isAdsEnabled,
                        autoLockDurationMinutes = autoLockDurationMinutes,
                        isAutoBackupEnabled = isAutoBackupEnabled,
                        autoBackupFrequencyDays = autoBackupFrequencyDays,
                        userTier = appSettings.userTier,
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
                        onGoalsClick = {
                            navigationState.navigateTo(AppRoute.Goals)
                            navigationState.updateBottomBarVisibility(false)
                        },
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
                                SyncWorker.startImmediate(context)
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
                                            "${result.exportedBudgets} budgets, " +
                                            "${result.exportedGoals} goals."
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
                                    if (isAdsEnabled && activity != null) {
                                        monetizationViewModel.showInterstitial(activity, InterstitialPlacement.DATA_ACTION)
                                    }
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
                        onLinkAccountClick = { 
                            authViewModel.resetState()
                        showAuthSheet = true 
                    },
                        onLogoutClick = { showLogoutDialog = true },
                        onShowUpgradeSheet = { showPremiumSheet = true },
                        onPrepareForExternalActivity = { isAppLockSuppressed = true }
                    )
                }
            }
        }
    }

    val isProPassEnabled by mainViewModel.isProPassEnabled.collectAsStateWithLifecycle()

    if (showProPassRedeemDialog) {
        ProPassRedeemDialog(
            viewModel = monetizationViewModel,
            onDismiss = { showProPassRedeemDialog = false }
        )
    }

    if (showPremiumSheet) {
        PremiumGateSheet(
            financialGoal = userProfile.financialGoal,
            onDismiss = { showPremiumSheet = false },
            onUpgradeClick = {
                // monetizationViewModel.onPurchaseSimulated() // Disabled until Google Play Billing is implemented
                showToast("Premium billing coming soon!")
                showPremiumSheet = false
            },
            onRedeemClick = if (isProPassEnabled) {
                {
                    showPremiumSheet = false
                    showProPassRedeemDialog = true
                }
            } else null
        )
    }

    if (showAdExpiryWarningDialog) {
            AlertDialog(
                onDismissRequest = { /* No-op to make persistent */ },
                properties = androidx.compose.ui.window.DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = stringResource(id = R.string.title_ad_expiry_warning),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.msg_ad_expiry_warning, adExpiryMinutesRemaining),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAdExpiryWarningDialog = false
                            if (activity != null) {
                                monetizationViewModel.onWatchAdFreeClicked(activity)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(id = R.string.btn_extend_now))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAdExpiryWarningDialog = false }) {
                        Text(stringResource(id = R.string.btn_maybe_later))
                    }
                }
            )
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = stringResource(id = R.string.label_logout),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.msg_logout_confirm),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            coroutineScope.launch {
                                // Clear local profile info on explicit sign out
                                val currentProfile = UserProfileDataStore.getUserProfileFlow(context).first()
                                com.mknlabs.expensetracker.utils.ProfilePhotoManager.deleteManagedPhoto(currentProfile.photoUri)
                                
                                UserProfileDataStore.updateUserProfile(context) { profile ->
                                    profile.copy(
                                        fullName = "Guest User",
                                        emailAddress = "",
                                        photoUri = null
                                    )
                                }
                                
                                // Reset Last Sync Time to prevent data pollution
                                AppSettingsDataStore.updateAppSettings(context) { it.copy(lastSyncTimeMillis = 0L) }
                                
                                authViewModel.signOut()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = stringResource(id = R.string.label_logout), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text(text = stringResource(id = R.string.label_cancel))
                    }
                }
            )
        }

        if (showAccountCreatedPopup) {
            AnimatedVisibility(
                visible = showAccountCreatedPopup,
                enter = fadeIn(animationSpec = tween(250)) + scaleIn(animationSpec = tween(250), initialScale = 0.96f),
                exit = fadeOut(animationSpec = tween(450)) + scaleOut(animationSpec = tween(450), targetScale = 0.96f),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                        tonalElevation = 8.dp,
                        shadowElevation = 10.dp,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.title_account_created),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(id = R.string.msg_account_created_successfully),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (showAuthSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showAuthSheet = false
                    authViewModel.resetState()
                },
                sheetState = authSheetState,
                containerColor = MaterialTheme.colorScheme.background,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(32.dp, 4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            ) {
                Box(modifier = Modifier.padding(bottom = 32.dp)) {
                    AuthContent(
                        viewModel = authViewModel,
                        onAuthSuccess = {
                            showAuthSheet = false
                        },
                        onGuestContinue = {
                            showAuthSheet = false
                        },
                        onSignUpSuccess = {
                            showAuthSheet = false
                            showAccountCreatedPopup = true
                        }
                    )
                }
            }
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

        if (isAdLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AdLoadingScrim)
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = stringResource(id = R.string.msg_preparing_pro_experience),
                        color = AdLoadingText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.msg_wont_take_long),
                        color = AdLoadingText.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun JsonImportResult.toJsonImportMessage(): String {
    return "Imported $importedTransactions tx, $importedBudgets budgets, " +
        "$importedRecurringRules rules, $importedGoals goals. Skipped $skippedTransactions tx duplicates."
}
