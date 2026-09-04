package com.mknlabs.expensetracker

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.AppLockPreferences
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.domain.repository.JsonImportResult
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.InterstitialPlacement
import com.mknlabs.expensetracker.ui.components.AppLockOverlay
import com.mknlabs.expensetracker.models.PinVisualMode
import com.mknlabs.expensetracker.ui.components.MainScaffold
import com.mknlabs.expensetracker.ui.components.ComingSoonDialog
import com.mknlabs.expensetracker.ui.components.PremiumGateSheet
import com.mknlabs.expensetracker.ui.components.ProPassRedeemDialog
import com.mknlabs.expensetracker.ui.navigation.AppRoute
import com.mknlabs.expensetracker.ui.navigation.AppLockFlow
import com.mknlabs.expensetracker.ui.navigation.rememberMainNavigationState
import com.mknlabs.expensetracker.ui.navigation.routesKeepingTransactionsWarm
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.notifications.NotificationScheduler
import com.mknlabs.expensetracker.sms.ParsedSms
import com.mknlabs.expensetracker.sms.SmsNotificationManager
import com.mknlabs.expensetracker.ui.screens.OnboardingScreen
import com.mknlabs.expensetracker.ui.screens.SmsChangeRoute
import com.mknlabs.expensetracker.ui.viewmodels.MainViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.ui.viewmodels.AuthViewModel
import com.mknlabs.expensetracker.ui.screens.AuthRoute
import com.mknlabs.expensetracker.workers.SyncWorker
import com.mknlabs.expensetracker.utils.toAmountFormatPreferences
import com.mknlabs.expensetracker.utils.BiometricAuthManager
import com.mknlabs.expensetracker.utils.findFragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import com.mknlabs.expensetracker.workers.AutoBackupScheduler
import com.mknlabs.expensetracker.utils.DeviceIntegrityUtils
import com.mknlabs.expensetracker.utils.AppRestartUtils
import com.mknlabs.expensetracker.utils.BackupDecryptionException
import com.mknlabs.expensetracker.ui.theme.AdLoadingScrim
import com.mknlabs.expensetracker.ui.theme.AdLoadingText

import com.mknlabs.expensetracker.monetization.FeatureRegistry
import com.mknlabs.expensetracker.monetization.AccessLevel

private fun sanitizeCardSettings(
    settings: TransactionCardCustomizationSettings,
    userTier: UserTier
): TransactionCardCustomizationSettings {
    if (userTier == UserTier.PREMIUM) return settings
    return settings.copy(
        showTransactionTime = if (FeatureRegistry.getAccessLevel(Feature.CARD_CUSTOMIZATION, "showTransactionTime") == AccessLevel.PREMIUM) false else settings.showTransactionTime,
        showDateSeparators = if (FeatureRegistry.getAccessLevel(Feature.CARD_CUSTOMIZATION, "showDateSeparators") == AccessLevel.PREMIUM) false else settings.showDateSeparators,
        showPaymentMethod = if (FeatureRegistry.getAccessLevel(Feature.CARD_CUSTOMIZATION, "showPaymentMethod") == AccessLevel.PREMIUM) false else settings.showPaymentMethod,
        showTransactionListSummaries = if (FeatureRegistry.getAccessLevel(Feature.CARD_CUSTOMIZATION, "showTransactionListSummaries") == AccessLevel.PREMIUM) false else settings.showTransactionListSummaries
    )
}

private fun AppSettings.toTransactionCardCustomizationSettings(): TransactionCardCustomizationSettings {
    return TransactionCardCustomizationSettings(
        showIncomeExpenseLabels = transactionCardShowIncomeExpenseLabels,
        showTransactionDate = transactionCardShowTransactionDate,
        showPaymentMethod = transactionCardShowPaymentMethod,
        showTransactionTime = transactionCardShowTransactionTime,
        showCategoryIcon = transactionCardShowCategoryIcon,
        showCategoryLabel = transactionCardShowCategoryLabel,
        showDateSeparators = transactionCardShowDateSeparators,
        showTransactionListSummaries = transactionCardShowListSummaries
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
        transactionCardShowDateSeparators = settings.showDateSeparators,
        transactionCardShowListSummaries = settings.showTransactionListSummaries
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isReady: Boolean,
    appSettings: AppSettings,
    userProfile: UserProfile,
    initialNavDestination: String? = null,
    initialAddTransactionAmount: String? = null,
    initialAddTransactionNote: String? = null,
    initialParsedSms: ParsedSms? = null,
    notificationIntent: Intent? = null,
    isRecoveryPerformed: Boolean = false,
    onRecoveryConsumed: () -> Unit = {},
    // True while the cold-start / auto-lock overlay (hosted by MainActivity) is
    // active. While set, MainScreen suppresses its root-level bottom sheets and
    // dialogs so no window can ever be created on top of the lock.
    isAppLockActive: Boolean = false
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
    val firebaseUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    var showAuthSheet by remember { mutableStateOf(false) }
    var showPremiumSheet by remember { mutableStateOf(false) }
    var showAccountCreatedPopup by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAdExpiryWarningDialog by remember { mutableStateOf(false) }
    var showProPassRedeemDialog by remember { mutableStateOf(false) }
    var adExpiryMinutesRemaining by remember { mutableStateOf(0) }
    // One-time, non-blocking notice when the device is rooted or an emulator
    // (security plan Phase 2, Items 7 & 8).
    var showDeviceIntegrityNotice by remember { mutableStateOf(false) }
    // Smart SMS Import "Change" sheet request — set when the app is opened via
    // the notification's Change action (DESTINATION_SMS_CHANGE, plan §8/Phase 4).
    var smsChangeRequest by remember { mutableStateOf<ParsedSms?>(null) }

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
    val isGoalRemindersEnabled = appSettings.goalRemindersEnabled
    val isLargeTransactionAlertsEnabled = appSettings.largeTransactionAlertsEnabled
    val isWeeklySummaryEnabled = appSettings.weeklySummaryEnabled
    val isBillRemindersEnabled = appSettings.billRemindersEnabled
    val isFinancialInsightsEnabled = appSettings.financialInsightsEnabled
    val isCloudSecurityEnabled = appSettings.cloudSecurityEnabled
    val largeTransactionThresholdMinor = appSettings.largeTransactionThresholdMinor
    val weeklySummaryTimeMillis = appSettings.weeklySummaryTimeMillis
    val reminderMorningStartHour = appSettings.reminderMorningStartHour
    val reminderMorningEndHour = appSettings.reminderMorningEndHour
    val reminderEveningStartHour = appSettings.reminderEveningStartHour
    val reminderEveningEndHour = appSettings.reminderEveningEndHour
    val isAutoBackupEnabled = appSettings.isAutoBackupEnabled
    val autoBackupFrequencyDays = appSettings.autoBackupFrequencyDays
    val effectiveUserTier by monetizationViewModel.userTier.collectAsStateWithLifecycle()
    val transactionCardCustomizationSettings = remember(appSettings, effectiveUserTier) {
        val rawSettings = appSettings.toTransactionCardCustomizationSettings()
        sanitizeCardSettings(rawSettings, effectiveUserTier)
    }
    val effectiveAppSettings = remember(appSettings, effectiveUserTier) {
        appSettings.copy(userTier = effectiveUserTier)
    }
    val scrambledPinKeypadAccessStatus by monetizationViewModel
        .getAccessStatus(Feature.SCRAMBLED_PIN_KEYPAD)
        .collectAsStateWithLifecycle()
    val isScrambledPinKeypadAccessGranted = scrambledPinKeypadAccessStatus is AccessStatus.Granted
    val isScrambledPinKeypadEffective = effectiveUserTier == UserTier.PREMIUM
    val biometricAvailability = BiometricAuthManager.getAvailability(rawContext)
    val hasAppLockPin = remember(appLockState) {
        AppLockPreferences.hasPin(context)
    }
    var appLockFlow by remember { mutableStateOf<AppLockFlow?>(null) }
    var hasPromptedBiometricForCurrentUnlock by remember(appLockFlow) {
        mutableStateOf(false)
    }

    val shouldBlurForAppLock = appLockFlow != null

    // While the app lock is active — the cold-start/auto-lock overlay from
    // MainActivity (isAppLockActive) or the in-app Setup/Unlock flow
    // (appLockFlow) — no bottom sheet or dialog may be on screen: each renders
    // in its own window, and any window created after the lock's own window
    // would cover it. Suppressing them here guarantees the lock always has the
    // highest visual priority.
    val isUiInteractive = !isAppLockActive && appLockFlow == null



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
        // A successful unlock (PIN, biometric, or setup completion) clears the
        // persisted brute-force counter/lockout window.
        AppLockPreferences.resetFailedAttempts(context)
        appLockState = AppLockPreferences.getCachedState()
        coroutineScope.launch(Dispatchers.IO) {
            AppLockPreferences.persistUnlocked(context, unlockedAtMillis)
        }
    }

    val handleSuccessfulAuth: () -> Unit = {
        coroutineScope.launch {
            // Wait a moment for Firebase Auth session/provider data to settle/flush
            kotlinx.coroutines.delay(600)
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val remotePhotoUri = user.photoUrl
                val currentProfile = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    UserProfileDataStore.getUserProfileFlow(context).first()
                }
                
                UserProfileDataStore.updateUserProfile(context) { profile ->
                    val newProvider = if (user.isAnonymous) "anonymous" else {
                        user.providerData.firstOrNull { it.providerId != "firebase" }?.providerId ?: "email"
                    }
                    val providerEmail = user.email ?: user.providerData.firstOrNull { !it.email.isNullOrBlank() }?.email
                    
                    profile.copy(
                        fullName = if (profile.fullName.isBlank() || profile.fullName == "Guest User") {
                            user.displayName ?: profile.fullName
                        } else {
                            profile.fullName
                        },
                        emailAddress = if (!user.isAnonymous && !providerEmail.isNullOrBlank()) {
                            providerEmail
                        } else {
                            profile.emailAddress
                        },
                        photoUri = if (profile.photoUri.isNullOrBlank()) {
                            remotePhotoUri?.toString() ?: profile.photoUri
                        } else {
                            profile.photoUri
                        },
                        authProvider = newProvider,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                }

                // Localize photo if needed
                if (remotePhotoUri != null && (currentProfile.photoUri.isNullOrBlank() || currentProfile.photoUri.startsWith("http"))) {
                    val localUri = com.mknlabs.expensetracker.utils.ProfilePhotoManager.localizePhoto(context, remotePhotoUri)
                    if (localUri != null) {
                        UserProfileDataStore.updateUserProfile(context) { 
                            it.copy(
                                photoUri = localUri,
                                updatedAtMillis = System.currentTimeMillis()
                            ) 
                        }
                    }
                }

                // Trigger Sync immediately
                android.util.Log.d("Sync", "Triggering sync post-auth")
                SyncWorker.startImmediate(context)
            }
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
                        val now = System.currentTimeMillis()
                        // 1. Update Profile (Initializing creation time if it's 0)
                        UserProfileDataStore.updateUserProfile(context) { profile ->
                            profile.copy(
                                fullName = name.ifBlank { "Guest User" },
                                gender = gender,
                                dateOfBirthMillis = dobMillis,
                                financialGoal = financialGoal,
                                accountCreatedMillis = if (profile.accountCreatedMillis == 0L) now else profile.accountCreatedMillis,
                                updatedAtMillis = now,
                                authProvider = if (firebaseUser?.isAnonymous == true) "anonymous" else {
                                    firebaseUser?.providerData?.firstOrNull { it.providerId != "firebase" }?.providerId ?: "email"
                                }
                            )
                        }

                        // 2. Hide Onboarding
                        AppSettingsDataStore.updateAppSettings(context) { settings ->
                            settings.copy(
                                showOnboardingScreen = false
                            )
                        }

                        // 3. Stabilization: Wait a moment for DataStore to flush and Auth to settle
                        kotlinx.coroutines.delay(1000)
                    }
                },
                onSignUpSuccess = {
                    showAccountCreatedPopup = true
                }
            )
        } else {
            // Key on the prefill extras too: a second "Open"/"Change" tap while the
            // app is already foregrounded (onNewIntent) refreshes the payload for
            // the new SMS.
            // Keyed on the raw intent reference (not just the extras) so a second
            // tap of the same notification after onNewIntent — e.g. dismissing the
            // Change sheet and tapping Change again — re-triggers this block even
            // when the ParsedSms extras are value-equal to the previous one.
            LaunchedEffect(notificationIntent, initialNavDestination, initialAddTransactionAmount, initialAddTransactionNote, initialParsedSms) {
                // Notification analytics: the type extra is present only when
                // the app was opened by tapping a local notification, so this
                // fires once per tap (cold start or onNewIntent).
                val openedNotificationType = notificationIntent?.getStringExtra(
                    com.mknlabs.expensetracker.notifications.NotificationAnalytics.EXTRA_NOTIFICATION_TYPE
                )
                if (openedNotificationType != null) {
                    com.mknlabs.expensetracker.notifications.NotificationAnalytics.logOpened(rawContext, openedNotificationType)
                }
                when (initialNavDestination) {
                    NotificationHelper.DESTINATION_ADD_TRANSACTION -> {
                        // Smart SMS Import "Open" action: prefill the Add Transaction draft
                        // (amount + note = sender · SMS body) before navigating (plan §8).
                        initialAddTransactionAmount?.let { navigationState.updateAddTransactionDraftAmount(it) }
                        initialAddTransactionNote?.let { navigationState.updateAddTransactionDraftNote(it) }
                        navigationState.navigateTo(AppRoute.AddTransaction)
                        navigationState.updateBottomBarVisibility(false)
                    }

                    NotificationHelper.DESTINATION_SMS_CHANGE -> {
                        // Smart SMS Import "Change" action: land on Home and pop the
                        // lightweight category+note sheet with the parsed payload.
                        initialParsedSms?.let { smsChangeRequest = it }
                        navigationState.navigateTo(AppRoute.Home)
                        navigationState.updateBottomBarVisibility(false)
                    }

                    NotificationHelper.DESTINATION_GOALS -> {
                        // Savings-goal reminder: land on the Goals screen.
                        navigationState.navigateTo(AppRoute.Goals)
                        navigationState.updateBottomBarVisibility(false)
                    }

                    NotificationHelper.DESTINATION_BUDGET -> {
                        // Budget alert: land on the Budget screen so the user can
                        // see and act on the category that triggered the alert.
                        navigationState.navigateTo(AppRoute.Budget)
                        navigationState.updateBottomBarVisibility(false)
                    }

                    NotificationHelper.DESTINATION_ANALYTICS -> {
                        // Weekly summary: land on the Analytics screen.
                        navigationState.navigateTo(AppRoute.Analytics)
                        navigationState.updateBottomBarVisibility(false)
                    }
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

            LaunchedEffect(isGoalRemindersEnabled) {
                // Savings-goal reminders have their own dedicated toggle, independent
                // from the daily-reminder master toggle (plan §Goals).
                if (isGoalRemindersEnabled) {
                    NotificationScheduler.startGoalReminders(context)
                } else {
                    NotificationScheduler.stopGoalReminders(context)
                }
            }

            LaunchedEffect(isWeeklySummaryEnabled) {
                // Sunday weekly summary — own toggle (spec category 4, Free tier).
                if (isWeeklySummaryEnabled) {
                    NotificationScheduler.startWeeklySummary(context)
                } else {
                    NotificationScheduler.stopWeeklySummary(context)
                }
            }

            LaunchedEffect(isAutoBackupEnabled, autoBackupFrequencyDays) {
                AutoBackupScheduler.scheduleOrUpdate(context, isAutoBackupEnabled, autoBackupFrequencyDays)
            }

            LaunchedEffect(effectiveUserTier) {
                AppSettingsDataStore.updateAppSettings(context) { settings ->
                    settings.copy(scrambledPinKeypadEnabled = effectiveUserTier == UserTier.PREMIUM)
                }
            }

            // Auto-reset custom font when Pro status is lost
            LaunchedEffect(effectiveUserTier) {
                if (effectiveUserTier != UserTier.PREMIUM &&
                    (appSettings.fontMode == com.mknlabs.expensetracker.models.FontMode.CUSTOM ||
                        appSettings.activeCustomFontFileName != null)
                ) {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(
                            fontMode = com.mknlabs.expensetracker.models.FontMode.APP,
                            activeCustomFontFileName = null
                        )
                    }
                }
            }

            LaunchedEffect(navigationState.currentRoute) {
                mainViewModel.setTransactionObservationEnabled(
                    navigationState.currentRoute in routesKeepingTransactionsWarm
                )
            }

            LaunchedEffect(Unit) {
                // Skip detection entirely once acknowledged or in benchmark builds
                // (baseline-profile capture runs on an emulator — the notice would
                // otherwise pollute the profile).
                if (appSettings.deviceIntegrityNoticeAcknowledged ||
                    BuildConfig.BUILD_TYPE == "benchmark"
                ) {
                    return@LaunchedEffect
                }
                val isRooted = withContext(Dispatchers.IO) {
                    DeviceIntegrityUtils.isRooted(context)
                }
                val isEmulator = DeviceIntegrityUtils.isEmulator()
                if (DeviceIntegrityUtils.shouldShowIntegrityNotice(
                        isRooted = isRooted,
                        isEmulator = isEmulator,
                        acknowledged = false,
                        benchmarkBuild = false
                    )
                ) {
                    showDeviceIntegrityNotice = true
                }
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
            LaunchedEffect(firebaseUser) {
                if (firebaseUser != null) {
                    firebaseUser?.let { user -> 
                        // If user just linked an anonymous account, wait a tiny bit for provider data to flush
                        if (!user.isAnonymous) {
                            kotlinx.coroutines.delay(400)
                        }
                        
                        val remotePhotoUri = user.photoUrl
                        val currentProfile = kotlinx.coroutines.withContext(Dispatchers.IO) {
                            UserProfileDataStore.getUserProfileFlow(context).first()
                        }
                        
                        // Precise Sync Decision:
                        // 1. If anonymous, only initialize if provider is completely blank
                        // 2. If permanent, sync if details are blank, or if transitioning from anonymous guest mode
                        val shouldUpdateProfile = if (user.isAnonymous) {
                            currentProfile.authProvider.isBlank()
                        } else {
                            currentProfile.fullName.isBlank() ||
                            (currentProfile.fullName == "Guest User" && !user.isAnonymous) ||
                            currentProfile.emailAddress.isBlank() ||
                            currentProfile.authProvider.isBlank() ||
                            currentProfile.authProvider == "anonymous"
                        }

                        if (shouldUpdateProfile) {
                            UserProfileDataStore.updateUserProfile(context) { profile ->
                                val newProvider = if (user.isAnonymous) "anonymous" else {
                                    user.providerData.firstOrNull { it.providerId != "firebase" }?.providerId ?: "email"
                                }
                                
                                val providerEmail = user.email ?: user.providerData.firstOrNull { !it.email.isNullOrBlank() }?.email
                                
                                profile.copy(
                                    fullName = if (profile.fullName.isBlank() || profile.fullName == "Guest User") {
                                        user.displayName ?: profile.fullName
                                    } else {
                                        profile.fullName
                                    },
                                    emailAddress = if (profile.emailAddress.isBlank() && !user.isAnonymous) {
                                        providerEmail ?: profile.emailAddress
                                    } else {
                                        profile.emailAddress
                                    },
                                    photoUri = if (profile.photoUri.isNullOrBlank()) {
                                        remotePhotoUri?.toString() ?: profile.photoUri
                                    } else {
                                        profile.photoUri
                                    },
                                    authProvider = newProvider,
                                    updatedAtMillis = System.currentTimeMillis()
                                )
                            }
                        }

                        // Localize network photo to prevent blinking
                        if (remotePhotoUri != null && currentProfile.photoUri?.startsWith("http") == true) {
                            coroutineScope.launch {
                                val localUri = com.mknlabs.expensetracker.utils.ProfilePhotoManager.localizePhoto(context, remotePhotoUri)
                                if (localUri != null) {
                                    UserProfileDataStore.updateUserProfile(context) { 
                                        it.copy(
                                            photoUri = localUri,
                                            updatedAtMillis = System.currentTimeMillis()
                                        ) 
                                    }
                                }
                            }
                        }

                        if (!showOnboarding) {
                            android.util.Log.d("Sync", "Triggering sync for ${if (user.isAnonymous) "Guest" else "Auth"} user")
                            SyncWorker.startImmediate(context)
                        }
                    }
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
                        appSettings = effectiveAppSettings,
                        selectedCurrencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        selectedDateFormatPattern = selectedDateFormatPattern,
                        selectedTimeFormat = selectedTimeFormat,
                        isAppLockEnabled = isAppLockEnabled,
                        isLockOverlayActive = isAppLockActive || appLockFlow != null,
                        hasAppLockPin = hasAppLockPin,
                        isBiometricEnabled = isBiometricEnabled,
                        isScrambledPinKeypadEnabled = isScrambledPinKeypadEnabled,
                        isBlurInRecentsEnabled = isBlurInRecentsEnabled,
                        isScreenshotProtectionEnabled = isScreenshotProtectionEnabled,
                        isDailyReminderEnabled = isDailyReminderEnabled,
                        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                        isGoalRemindersEnabled = isGoalRemindersEnabled,
                        isLargeTransactionAlertsEnabled = isLargeTransactionAlertsEnabled,
                        isWeeklySummaryEnabled = isWeeklySummaryEnabled,
                        isBillRemindersEnabled = isBillRemindersEnabled,
                        isFinancialInsightsEnabled = isFinancialInsightsEnabled,
                        isCloudSecurityEnabled = isCloudSecurityEnabled,
                        largeTransactionThresholdMinor = largeTransactionThresholdMinor,
                        weeklySummaryTimeMillis = weeklySummaryTimeMillis,
                        reminderMorningStartHour = reminderMorningStartHour,
                        reminderMorningEndHour = reminderMorningEndHour,
                        reminderEveningStartHour = reminderEveningStartHour,
                        reminderEveningEndHour = reminderEveningEndHour,
                        isAdsEnabled = isAdsEnabled,
                        autoLockDurationMinutes = autoLockDurationMinutes,
                        isAutoBackupEnabled = isAutoBackupEnabled,
                        autoBackupFrequencyDays = autoBackupFrequencyDays,
                        userTier = effectiveUserTier,
                        onRouteChange = navigationState::navigateTo,
                        onProfileOriginRouteChange = navigationState::updateProfileOriginRoute,
                        onBottomBarVisibilityChange = navigationState::updateBottomBarVisibility,
                        onSelectedTransactionChange = navigationState::updateSelectedTransaction,
                        onAddTransactionDraftAmountChange = navigationState::updateAddTransactionDraftAmount,
                        onAddTransactionDraftNoteChange = navigationState::updateAddTransactionDraftNote,
                        onSaveTransaction = mainViewModel::saveTransaction,
                        onDeleteTransaction = { id ->
                            mainViewModel.deleteTransaction(id)
                            showToast(rawContext.getString(R.string.toast_transaction_deleted))
                        },
                        // Swipe-delete path: no toast — the Transactions list shows an
                        // Undo snackbar instead, so the two never double up.
                        onSwipeDeleteTransaction = { transaction ->
                            mainViewModel.deleteTransaction(transaction.id)
                        },
                        onRestoreTransaction = mainViewModel::restoreTransaction,
                        // Swipe-duplicate path: no toast — the Transactions list
                        // shows a "Transaction duplicated" Undo snackbar instead,
                        // so the two never double up (same convention as
                        // swipe-to-delete).
                        onDuplicateTransaction = { transaction, onDuplicated ->
                            mainViewModel.duplicateTransaction(transaction, onDuplicated)
                        },
                        onDeleteRecurring = mainViewModel::deleteRecurring,
                        onRecurringEnabledChange = mainViewModel::setRecurringEnabled,
                        onRecurringNotificationsEnabledChange = mainViewModel::setRecurringNotificationsEnabled,
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
                                    updatedProfile.copy(updatedAtMillis = System.currentTimeMillis())
                                }
                                SyncWorker.startImmediate(context)
                            }
                        },
                        // Legacy individual toggles — still used by quick toggles
                        // on the Settings list screen.
                        onDailyReminderChange = { isEnabled ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(notificationsEnabled = isEnabled)
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
                        // Merged Expense Reminders parent toggle: drives both the
                        // daily reminder and the missed-entry reminder.
                        onExpenseRemindersChange = { isEnabled ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(
                                        notificationsEnabled = isEnabled,
                                        missedEntryReminderEnabled = isEnabled
                                    )
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
                        onLargeTransactionAlertsChange = { isEnabled ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(largeTransactionAlertsEnabled = isEnabled)
                                }
                            }
                        },
                        onWeeklySummaryChange = { isEnabled ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(weeklySummaryEnabled = isEnabled)
                                }
                            }
                        },
                        onGoalRemindersChange = { isEnabled ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(goalRemindersEnabled = isEnabled)
                                }
                            }
                        },
                        onBillRemindersChange = { isEnabled ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(billRemindersEnabled = isEnabled)
                                }
                            }
                        },
                        onFinancialInsightsChange = { isEnabled ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(financialInsightsEnabled = isEnabled)
                                }
                            }
                        },
                        onCloudSecurityChange = { isEnabled ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(cloudSecurityEnabled = isEnabled)
                                }
                            }
                        },
                        onLargeTransactionThresholdChange = { thresholdMinor ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(largeTransactionThresholdMinor = thresholdMinor)
                                }
                            }
                        },
                        onWeeklySummaryTimeChange = { timeMillis ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(weeklySummaryTimeMillis = timeMillis)
                                }
                                // Re-arm the periodic work to the new day/time.
                                com.mknlabs.expensetracker.notifications.NotificationScheduler.startWeeklySummary(context)
                            }
                        },
                        onReminderWindowChange = { window, startHour, endHour ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    when (window) {
                                        com.mknlabs.expensetracker.models.ReminderWindow.MORNING -> settings.copy(
                                            reminderMorningStartHour = startHour,
                                            reminderMorningEndHour = endHour
                                        )
                                        com.mknlabs.expensetracker.models.ReminderWindow.EVENING -> settings.copy(
                                            reminderEveningStartHour = startHour,
                                            reminderEveningEndHour = endHour
                                        )
                                    }
                                }
                            }
                        },
                        onPremiumCardClick = { showPremiumSheet = true },
                        onTestNotification = {
                            com.mknlabs.expensetracker.notifications.NotificationWorker.enqueueTest(context)
                        },
                        onDatabaseBackupFileSelected = { uri ->
                            mainViewModel.backupDatabase(
                                uri = uri,
                                onComplete = {
                                    showToast(rawContext.getString(R.string.toast_database_backup_saved))
                                },
                                onError = {
                                    showToast(rawContext.getString(R.string.toast_database_backup_save_failed))
                                }
                            )
                        },
                        onDatabaseRestoreFileSelected = { uri ->
                            mainViewModel.restoreDatabase(
                                uri = uri,
                                onComplete = {
                                    navigationState.clearTransactionDraftContext()
                                    showToast(rawContext.getString(R.string.toast_database_restored_reloading_ap))
                                    AppRestartUtils.restartApp(rawContext)
                                },
                                onError = { error ->
                                    val message = if (error is BackupDecryptionException) {
                                        rawContext.getString(R.string.toast_backup_restore_key_mismatch)
                                    } else {
                                        rawContext.getString(R.string.toast_database_restore_failed)
                                    }
                                    showToast(message)
                                }
                            )
                        },
                        onJsonExportFileSelected = { uri ->
                            mainViewModel.exportJson(
                                uri = uri,
                                onComplete = { result ->
                                    showToast(
                                        rawContext.getString(
                                            R.string.toast_json_exported,
                                            result.exportedTransactions,
                                            result.exportedBudgets,
                                            result.exportedGoals
                                        )
                                    )
                                },
                                onError = {
                                    showToast(rawContext.getString(R.string.toast_json_export_failed))
                                }
                            )
                        },
                        onJsonImportFileSelected = { uri ->
                            mainViewModel.importJson(
                                uri = uri,
                                onComplete = { result ->
                                    showToast(result.toJsonImportMessage(rawContext))
                                },
                                onError = {
                                    showToast(rawContext.getString(R.string.toast_json_import_failed))
                                }
                            )
                        },
                        onLegacyImportFileSelected = { uri ->
                            mainViewModel.importLegacyBackup(
                                uri = uri,
                                onComplete = { result ->
                                    showToast(
                                        rawContext.getString(
                                            R.string.toast_legacy_imported,
                                            result.importedTransactions,
                                            result.skippedTransactions
                                        )
                                    )
                                    if (isAdsEnabled && activity != null) {
                                        monetizationViewModel.showInterstitial(activity, InterstitialPlacement.DATA_ACTION)
                                    }
                                },
                                onError = {
                                    showToast(rawContext.getString(R.string.toast_legacy_import_failed_check_the))
                                }
                            )
                        },
                        onDeleteAllTransactionsClick = {
                            mainViewModel.deleteAllTransactions(
                                onComplete = {
                                    navigationState.clearTransactionDraftContext()
                                    showToast(rawContext.getString(R.string.toast_all_transactions_deleted))
                                },
                                onError = {
                                    showToast(rawContext.getString(R.string.toast_delete_transactions_failed))
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
                        onCloudSyncEnabledChange = { enabled ->
                            coroutineScope.launch {
                                AppSettingsDataStore.updateAppSettings(context) { settings ->
                                    settings.copy(isCloudSyncEnabled = enabled)
                                }
                                if (enabled) {
                                    com.mknlabs.expensetracker.workers.SyncWorker.startImmediate(context)
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
                        onDirectSignOut = { authViewModel.signOut() },
                        onShowUpgradeSheet = { showPremiumSheet = true },
                        // External activities (photo/file pickers, browser, system
                        // settings) background the app; arm the lock suppression so
                        // returning from them doesn't trigger the auto-lock.
                        onPrepareForExternalActivity = { AppLockPreferences.setLockSuppressed(true) }
                    )
                }
            }
        }
    }

    val isProPassEnabled by mainViewModel.isProPassEnabled.collectAsStateWithLifecycle()

    if (showProPassRedeemDialog && isUiInteractive) {
        ProPassRedeemDialog(
            viewModel = monetizationViewModel,
            onDismiss = { showProPassRedeemDialog = false }
        )
    }

    var showComingSoonDialog by remember { mutableStateOf(false) }

    if (showPremiumSheet && isUiInteractive) {
        PremiumGateSheet(
            onDismiss = { showPremiumSheet = false },
            onUpgradeClick = {
                showPremiumSheet = false
                showComingSoonDialog = true
            }
        )
    }

    if (showComingSoonDialog && isUiInteractive) {
        ComingSoonDialog(
            onDismiss = { showComingSoonDialog = false }
        )
    }

    if (showAdExpiryWarningDialog && isUiInteractive) {
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

        if (showDeviceIntegrityNotice && isUiInteractive) {
            val acknowledgeIntegrityNotice: () -> Unit = {
                showDeviceIntegrityNotice = false
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(deviceIntegrityNoticeAcknowledged = true)
                    }
                }
            }
            AlertDialog(
                onDismissRequest = acknowledgeIntegrityNotice,
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = stringResource(id = R.string.title_device_integrity_notice),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.msg_device_integrity_notice),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = acknowledgeIntegrityNotice) {
                        Text(stringResource(id = R.string.btn_got_it))
                    }
                }
            )
        }

        if (showLogoutDialog && isUiInteractive) {
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
                            authViewModel.signOut()
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

        if (showAuthSheet && isUiInteractive) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showAuthSheet = false
                    authViewModel.resetState()
                },
                sheetState = authSheetState,
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
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
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(bottom = 32.dp)
                ) {
                    AuthRoute(
                        viewModel = authViewModel,
                        onAuthSuccess = {
                            showAuthSheet = false
                            handleSuccessfulAuth()
                        },
                        onGuestContinue = {
                            showAuthSheet = false
                        },
                        onSignUpSuccess = {
                            showAuthSheet = false
                            showAccountCreatedPopup = true
                            handleSuccessfulAuth()
                        },
                        // Add-Account flow launches the system settings screen.
                        onPrepareForExternalActivity = { AppLockPreferences.setLockSuppressed(true) }
                    )
                }
            }
        }

        // Smart SMS Import "Change" bottom sheet (plan §8 / Phase 4): shown when
        // the app was opened via the notification's Change action. On save, the
        // notification is dismissed and Room flows refresh everything.
        smsChangeRequest?.takeIf { isUiInteractive }?.let { parsed ->
            SmsChangeRoute(
                parsedSms = parsed,
                categories = mainUiState.categories,
                onDismiss = { smsChangeRequest = null },
                onSaved = {
                    SmsNotificationManager.cancel(context)
                    smsChangeRequest = null
                    showToast(context.getString(R.string.toast_sms_transaction_saved))
                }
            )
        }

        if (appLockFlow != null) {
            AppLockOverlay(
                isReady = isReady,
                appSettings = effectiveAppSettings,
                initialFlow = appLockFlow!!,
                isAppUnlocked = true, // MainScreen only exists in Unlocked state
                // Non-null onDismiss switches AppLockOverlay into its fullscreen
                // Dialog mode so the Setup/Unlock flow renders in its own window
                // ABOVE any open ModalBottomSheet / AlertDialog (bottom sheets and
                // dialogs live in separate windows and would otherwise cover an
                // inline overlay). The flow itself still only closes via its own
                // back button (onBackClick); the dialog is not dismissible by back
                // press or outside tap.
                onDismiss = { appLockFlow = null },
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
                },
                pinVisualMode = if (effectiveUserTier == UserTier.PREMIUM) PinVisualMode.PRO_ANIMATED else PinVisualMode.NORMAL
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

private fun JsonImportResult.toJsonImportMessage(context: Context): String {
    return context.getString(
        R.string.toast_json_import_message,
        importedTransactions,
        importedBudgets,
        importedRecurringRules,
        importedGoals,
        skippedTransactions
    )
}
