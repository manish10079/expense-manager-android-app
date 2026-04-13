package com.mkn0079.expensetracker

import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.getAppLockSecurityQuestionPrompt
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.data.local.AppLockPreferences
import com.mkn0079.expensetracker.data.local.UserProfileDataStore
import com.mkn0079.expensetracker.models.AppSettings
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.RecurringTransactionDraft
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.models.avatarInitials
import com.mkn0079.expensetracker.ui.components.AppBottomBar
import com.mkn0079.expensetracker.ui.navigation.bottomNavBarItems
import com.mkn0079.expensetracker.ui.screens.AddTransactionScreen
import com.mkn0079.expensetracker.ui.screens.AboutScreen
import com.mkn0079.expensetracker.ui.screens.AppLockScreen
import com.mkn0079.expensetracker.ui.screens.AppLockScreenMode
import com.mkn0079.expensetracker.ui.screens.AnalyticsScreen
import com.mkn0079.expensetracker.ui.screens.BudgetScreen
import com.mkn0079.expensetracker.ui.screens.CalendarScreen
import com.mkn0079.expensetracker.ui.screens.CategoryManagementScreen
import com.mkn0079.expensetracker.ui.screens.DataManagementScreen
import com.mkn0079.expensetracker.ui.screens.HomeScreen
import com.mkn0079.expensetracker.ui.screens.ItemizedCalculatorScreen
import com.mkn0079.expensetracker.ui.screens.NotificationSettingsScreen
import com.mkn0079.expensetracker.ui.screens.OnboardingScreen
import com.mkn0079.expensetracker.ui.screens.PreferencesScreen
import com.mkn0079.expensetracker.ui.screens.ProfileScreen
import com.mkn0079.expensetracker.ui.screens.SecurityPrivacyScreen
import com.mkn0079.expensetracker.ui.screens.SettingsScreen
import com.mkn0079.expensetracker.ui.screens.SplashScreen
import com.mkn0079.expensetracker.ui.screens.TransactionScreen
import com.mkn0079.expensetracker.ui.screens.TransactionCardCustomizeScreen
import com.mkn0079.expensetracker.utils.BiometricAuthManager
import com.mkn0079.expensetracker.utils.findFragmentActivity
import com.mkn0079.expensetracker.ui.viewmodels.AnalyticsViewModel
import com.mkn0079.expensetracker.ui.viewmodels.BudgetViewModel
import com.mkn0079.expensetracker.ui.viewmodels.CalendarViewModel
import com.mkn0079.expensetracker.ui.viewmodels.MainViewModel
import com.mkn0079.expensetracker.ui.viewmodels.SettingsViewModel
import com.mkn0079.expensetracker.ui.viewmodels.TransactionsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val routeOrder = listOf(
    "home",
    "analytics",
    "budget",
    "calendar",
    "transactions",
    "settings",
    "preferences",
    "security_privacy",
    "transaction_card_customize",
    "category_management",
    "data_management",
    "profile",
    "add_transaction",
    "itemized_calculator"
)

private val primaryNavigationRoutes = setOf(
    "home",
    "analytics",
    "budget",
    "calendar",
    "transactions"
)

private val bottomTabRoutes = bottomNavBarItems.map { it.route }
private val routesRequiringFullTransactions = setOf(
    "analytics",
    "budget",
    "calendar",
    "transactions",
    "add_transaction"
)
private val routesKeepingTransactionsWarm = routesRequiringFullTransactions + "home"

private enum class AppLockFlow {
    Setup,
    Unlock
}

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

private fun resolveBackNavigationRoute(
    currentRoute: String,
    profileOriginRoute: String,
    previousRoute: String
): String? {
    return when (currentRoute) {
        "analytics",
        "budget",
        "calendar",
        "transactions",
        "settings" -> "home"
        "preferences",
        "security_privacy",
        "transaction_card_customize",
        "category_management",
        "data_management",
        "about",
        "notification_settings" -> "settings"
        "profile" -> profileOriginRoute
        "add_transaction" -> previousRoute
        "itemized_calculator" -> "add_transaction"
        else -> null
    }
}

private fun screenTransition(fromRoute: String, toRoute: String): ContentTransform {
    val isPrimaryNavigationTransition = fromRoute in primaryNavigationRoutes && toRoute in primaryNavigationRoutes
    val duration = if (isPrimaryNavigationTransition) 160 else 220
    val isForward = routeOrder.indexOf(toRoute) >= routeOrder.indexOf(fromRoute)

    if (isPrimaryNavigationTransition) {
        return fadeIn(animationSpec = tween(duration)) togetherWith
            fadeOut(animationSpec = tween(duration))
    }

    return if (isForward) {
        slideInHorizontally(
            animationSpec = tween(duration),
            initialOffsetX = { width -> width / 5 }
        ) + fadeIn(animationSpec = tween(duration)) togetherWith
            slideOutHorizontally(
                animationSpec = tween(duration),
                targetOffsetX = { width -> -width / 6 }
            ) + fadeOut(animationSpec = tween(duration))
    } else {
        slideInHorizontally(
            animationSpec = tween(duration),
            initialOffsetX = { width -> -width / 5 }
        ) + fadeIn(animationSpec = tween(duration)) togetherWith
            slideOutHorizontally(
                animationSpec = tween(duration),
                targetOffsetX = { width -> width / 6 }
            ) + fadeOut(animationSpec = tween(duration))
    }
}

@Composable
fun MainScreen(
    appSettings: AppSettings,
    userProfile: UserProfile
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
    var hasHandledLaunchSplash by remember { mutableStateOf(false) }
    val showSplash = appSettings.showSplashScreen && !showOnboarding && !hasHandledLaunchSplash
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

    val updateBiometricLockEnabled: (Boolean) -> Unit = { enabled ->
        AppLockPreferences.setBiometricEnabled(context, enabled)
        appLockState = AppLockPreferences.getCachedState()
        coroutineScope.launch {
            AppSettingsDataStore.updateAppSettings(context) { settings ->
                settings.copy(biometricLockEnabled = enabled)
            }
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
        showSplash,
        isAppLockEnabled,
        hasAppLockPin,
        isAppUnlocked,
        autoLockDurationMinutes
    ) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                event == Lifecycle.Event.ON_STOP &&
                !showOnboarding &&
                !showSplash &&
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
                !showSplash &&
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

    if (showSplash) {
        SplashScreen(
            onNavigate = {
                hasHandledLaunchSplash = true
                currentRoute = "home"
                isBottomBarVisible = false
            }
        )
        return
    }

    LaunchedEffect(
        showOnboarding,
        showSplash,
        isAppLockEnabled,
        hasAppLockPin,
        isAppUnlocked,
        autoLockDurationMinutes
    ) {
        if (!showOnboarding && !showSplash && isAppLockEnabled && hasAppLockPin && !isAppUnlocked && appLockFlow == null) {
            appLockFlow = AppLockFlow.Unlock
        }
    }

    LaunchedEffect(appLockFlow, canUseBiometricOnLockScreen) {
        if (
            appLockFlow == AppLockFlow.Unlock &&
            canUseBiometricOnLockScreen &&
            !hasPromptedBiometricForCurrentUnlock
        ) {
            hasPromptedBiometricForCurrentUnlock = true
            unlockWithBiometric()
        }
    }

    if (appLockFlow != null) {
        AppLockScreen(
            mode = if (appLockFlow == AppLockFlow.Setup) AppLockScreenMode.Setup else AppLockScreenMode.Unlock,
            biometricEnabled = canUseBiometricOnLockScreen,
            isBiometricAvailable = canUseBiometricOnLockScreen,
            securityQuestionPrompt = getAppLockSecurityQuestionPrompt(
                appLockState.securityQuestionId
            ),
            onBackClick = if (appLockFlow == AppLockFlow.Setup) {
                { appLockFlow = null }
            } else {
                null
            },
            onBiometricClick = if (
                appLockFlow == AppLockFlow.Unlock &&
                canUseBiometricOnLockScreen
            ) {
                unlockWithBiometric
            } else {
                null
            },
            onSetupComplete = { pin, questionId, answer ->
                AppLockPreferences.savePin(context, pin)
                AppLockPreferences.saveSecurityQuestion(context, questionId, answer)
                appLockState = AppLockPreferences.getCachedState()
                completeUnlock()
                coroutineScope.launch {
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
        return
    }

    val mainViewModel: MainViewModel = viewModel()
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(currentRoute) {
        mainViewModel.setTransactionObservationEnabled(
            currentRoute in routesKeepingTransactionsWarm
        )
    }

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
        autoLockDurationMinutes = autoLockDurationMinutes,
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
        onAddRecurring = mainViewModel::addRecurring,
        onUpdateRecurring = mainViewModel::updateRecurring,
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
        isDailyReminderEnabled = isDailyReminderEnabled,
        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
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
        onBiometricLockChange = { enabled ->
            if (!enabled) {
                updateBiometricLockEnabled(false)
            } else if (!isAppLockEnabled || !hasAppLockPin) {
                showToast("Create an app lock PIN before enabling biometric unlock.")
            } else {
                if (biometricAuthenticator == null) {
                    showToast("Biometric authentication is unavailable on this screen.")
                } else if (!biometricAvailability.isAvailable) {
                    showToast(
                        biometricAvailability.message
                            ?: "Biometric authentication is not available right now."
                    )
                } else {
                    biometricAuthenticator.authenticate(
                        title = "Enable Biometric Lock",
                        subtitle = "Verify your biometric once to turn on biometric unlock.",
                        negativeButtonText = "Cancel",
                        onSuccess = {
                            updateBiometricLockEnabled(true)
                            showToast("Biometric lock enabled.")
                        },
                        onFailure = { errorMessage ->
                            showToast(errorMessage.ifBlank { "Biometric verification failed." })
                        }
                    )
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

@Composable
private fun MainScaffold(
    currentRoute: String,
    previousRoute: String,
    profileOriginRoute: String,
    isBottomBarVisible: Boolean,
    transactions: List<Transaction>,
    transactionCount: Int,
    recurringRules: List<RecurringTransactionRule>,
    selectedTransaction: Transaction?,
    addTransactionDraftAmount: String?,
    addTransactionDraftNote: String?,
    categories: List<CategoryType>,
    paymentMethods: List<PaymentType>,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings,
    userProfile: UserProfile,
    selectedCurrencyId: Int,
    selectedDateFormatPattern: String,
    selectedTimeFormat: String,
    isAppLockEnabled: Boolean,
    hasAppLockPin: Boolean,
    isBiometricEnabled: Boolean,
    isBlurInRecentsEnabled: Boolean,
    isScreenshotProtectionEnabled: Boolean,
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    autoLockDurationMinutes: Int,
    onRouteChange: (String) -> Unit,
    onProfileOriginRouteChange: (String) -> Unit,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    onSelectedTransactionChange: (Transaction?) -> Unit,
    onAddTransactionDraftAmountChange: (String?) -> Unit,
    onAddTransactionDraftNoteChange: (String?) -> Unit,
    onSaveTransaction: (Transaction, RecurringTransactionDraft?, RecurringTransactionRule?) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onAddRecurring: (String, com.mkn0079.expensetracker.models.RecurringFrequency, Int) -> Unit,
    onUpdateRecurring: (String, String, com.mkn0079.expensetracker.models.RecurringFrequency, Int) -> Unit,
    onDeleteRecurring: (String) -> Unit,
    onRecurringEnabledChange: (String, Boolean) -> Unit,
    onCreateCustomCategory: (String, String, Int) -> Unit,
    onCreateCustomPaymentType: (String, String) -> Unit,
    onDeleteCustomCategory: (Int) -> Unit,
    onDeleteCustomPaymentType: (Int) -> Unit,
    onTransactionCardCustomizationSettingsChange: (TransactionCardCustomizationSettings) -> Unit,
    onUserProfileChange: (UserProfile) -> Unit,
    onSelectedCurrencyIdChange: (Int) -> Unit,
    onSelectedDateFormatPatternChange: (String) -> Unit,
    onSelectedTimeFormatChange: (String) -> Unit,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit,
    onLegacyImportFileSelected: (Uri) -> Unit,
    onDeleteAllTransactionsClick: () -> Unit,
    onBiometricLockChange: (Boolean) -> Unit,
    onBlurInRecentsChange: (Boolean) -> Unit,
    onScreenshotProtectionChange: (Boolean) -> Unit,
    onAutoLockDurationChange: (Int) -> Unit,
    onAppLockToggleChange: (Boolean) -> Unit,
    onPrepareForExternalActivity: () -> Unit
) {
    val showFixedBottomNavBar = currentRoute != "add_transaction" &&
        currentRoute != "itemized_calculator" &&
        currentRoute != "category_management" &&
        currentRoute != "security_privacy" &&
        currentRoute != "transaction_card_customize" &&
        currentRoute != "settings" &&
        currentRoute != "preferences" &&
        currentRoute != "data_management" &&
        currentRoute != "about" &&
        currentRoute != "notification_settings" &&
        currentRoute != "profile"
    val isBottomTabRoute = currentRoute in bottomTabRoutes
    val initialBottomTabPage = remember {
        bottomTabRoutes.indexOf("home").takeIf { it >= 0 } ?: 0
    }
    val pagerState = rememberPagerState(
        initialPage = bottomTabRoutes.indexOf(currentRoute).takeIf { it >= 0 } ?: initialBottomTabPage,
        pageCount = { bottomTabRoutes.size }
    )
    val backNavigationRoute = resolveBackNavigationRoute(
        currentRoute = currentRoute,
        profileOriginRoute = profileOriginRoute,
        previousRoute = previousRoute
    )
    val colorScheme = MaterialTheme.colorScheme

    val exitAddTransactionScreen: (String) -> Unit = { destinationRoute ->
        onBottomBarVisibilityChange(false)
        onRouteChange(destinationRoute)
    }
    val selectedRecurringRule = selectedTransaction?.let { transaction ->
        recurringRules.firstOrNull { it.transactionId == transaction.id }
    }
    val persistTransactionAndRecurring: (Transaction, RecurringTransactionDraft?) -> Unit = { draftTransaction, recurringDraft ->
        val transactionToSave = if (selectedTransaction != null) {
            draftTransaction.copy(id = selectedTransaction.id)
        } else {
            draftTransaction
        }
        onSaveTransaction(transactionToSave, recurringDraft, selectedRecurringRule)
        exitAddTransactionScreen(previousRoute)
    }
    val deleteSelectedTransaction: () -> Unit = {
        val transactionToDelete = selectedTransaction
        if (transactionToDelete == null) {
            exitAddTransactionScreen(previousRoute)
        } else {
            onDeleteTransaction(transactionToDelete.id)
            exitAddTransactionScreen(previousRoute)
        }
    }

    LaunchedEffect(currentRoute) {
        val targetPage = bottomTabRoutes.indexOf(currentRoute)
        if (targetPage >= 0 && pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute != "add_transaction" && currentRoute != "itemized_calculator") {
            onSelectedTransactionChange(null)
            onAddTransactionDraftAmountChange(null)
            onAddTransactionDraftNoteChange(null)
        }
    }

    BackHandler(enabled = backNavigationRoute != null) {
        when {
            currentRoute == "add_transaction" -> {
                exitAddTransactionScreen(previousRoute)
            }
            currentRoute == "itemized_calculator" -> {
                onBottomBarVisibilityChange(false)
                onRouteChange("add_transaction")
            }
            backNavigationRoute != null -> {
                onBottomBarVisibilityChange(false)
                onRouteChange(backNavigationRoute)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        PreloadSecondaryScreenData(
            transactions = transactions,
            categories = categories,
            recurringRules = recurringRules,
            selectedCurrencyId = selectedCurrencyId,
            selectedDateFormatPattern = selectedDateFormatPattern,
            selectedTimeFormat = selectedTimeFormat,
            transactionCount = transactionCount,
            autoLockDurationMinutes = autoLockDurationMinutes,
            transactionCardCustomizationSettings = transactionCardCustomizationSettings
        )

        PrimaryTabPager(
            pagerState = pagerState,
            userProfile = userProfile,
            transactions = transactions,
            recurringRules = recurringRules,
            categories = categories,
            selectedCurrencyId = selectedCurrencyId,
            selectedDateFormatPattern = selectedDateFormatPattern,
            selectedTimeFormat = selectedTimeFormat,
            transactionCardCustomizationSettings = transactionCardCustomizationSettings,
            onRouteChange = onRouteChange,
            onProfileOriginRouteChange = onProfileOriginRouteChange,
            onBottomBarVisibilityChange = onBottomBarVisibilityChange,
            onSelectedTransactionChange = onSelectedTransactionChange,
            onAddTransactionDraftAmountChange = onAddTransactionDraftAmountChange,
            onAddTransactionDraftNoteChange = onAddTransactionDraftNoteChange,
            onAddRecurring = onAddRecurring,
            onUpdateRecurring = onUpdateRecurring,
            onDeleteRecurring = onDeleteRecurring,
            onRecurringEnabledChange = onRecurringEnabledChange
        )

        AnimatedContent(
            targetState = currentRoute.takeUnless { it in bottomTabRoutes },
            transitionSpec = {
                val fromRoute = initialState
                val toRoute = targetState
                if (fromRoute == null || toRoute == null) {
                    fadeIn(animationSpec = tween(120)) togetherWith
                        fadeOut(animationSpec = tween(120))
                } else {
                    screenTransition(fromRoute, toRoute)
                }
            },
            label = "secondary_screen_transition",
            modifier = Modifier.fillMaxSize()
        ) { route ->
            val pointerModifier = if (route != null) {
                Modifier.fillMaxSize().pointerInput(Unit) {}
            } else {
                Modifier.fillMaxSize()
            }
            Box(modifier = pointerModifier) {
                when (route) {
                    null -> Box(modifier = Modifier.fillMaxSize())

                    "transactions" -> TransactionScreen(
                        currencyId = selectedCurrencyId,
                        dateFormatPattern = selectedDateFormatPattern,
                        timeFormat = selectedTimeFormat,
                        transactions = transactions,
                        transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                        onBackClick = {
                            onRouteChange("home")
                        },
                        onAddTransactionClick = {
                            onSelectedTransactionChange(null)
                            onAddTransactionDraftAmountChange(null)
                            onAddTransactionDraftNoteChange(null)
                            onBottomBarVisibilityChange(false)
                            onRouteChange("add_transaction")
                        },
                        onTransactionClick = { transaction ->
                            onSelectedTransactionChange(transaction)
                            onAddTransactionDraftAmountChange(null)
                            onAddTransactionDraftNoteChange(null)
                            onBottomBarVisibilityChange(false)
                            onRouteChange("add_transaction")
                        }
                    )

                    "settings" -> SettingsScreen(
                        userProfile = userProfile,
                        isDailyReminderEnabled = isDailyReminderEnabled,
                        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                        transactionCount = transactionCount,
                        onDailyReminderChange = onDailyReminderChange,
                        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                        onMissedEntryReminderChange = onMissedEntryReminderChange,
                        onProfileClick = {
                            onProfileOriginRouteChange("settings")
                            onBottomBarVisibilityChange(false)
                            onRouteChange("profile")
                        },
                        onPreferencesClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange("preferences")
                        },
                        onSecurityPrivacyClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange("security_privacy")
                        },
                        onTransactionCardCustomizeClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange("transaction_card_customize")
                        },
                        onDataManagementClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange("data_management")
                        },
                        onAboutClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange("about")
                        },
                        onNotificationsClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange("notification_settings")
                        },
                        onBackClick = {
                            backNavigationRoute?.let { onRouteChange(it) } ?: onRouteChange("home")
                        }
                    )

                    "preferences" -> PreferencesScreen(
                        currentCurrencyId = selectedCurrencyId,
                        currentDateFormatPattern = selectedDateFormatPattern,
                        currentTimeFormat = selectedTimeFormat,
                        onCurrencyChange = onSelectedCurrencyIdChange,
                        onDateFormatChange = onSelectedDateFormatPatternChange,
                        onTimeFormatChange = onSelectedTimeFormatChange,
                        onManageCategoryClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange("category_management")
                        },
                        onBackClick = {
                            backNavigationRoute?.let { onRouteChange(it) } ?: onRouteChange("settings")
                        }
                    )

                    "notification_settings" -> NotificationSettingsScreen(
                        isDailyReminderEnabled = isDailyReminderEnabled,
                        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                        onDailyReminderChange = onDailyReminderChange,
                        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                        onMissedEntryReminderChange = onMissedEntryReminderChange,
                        onBackClick = {
                            backNavigationRoute?.let { onRouteChange(it) } ?: onRouteChange("settings")
                        }
                    )

                    "security_privacy" -> SecurityPrivacyScreen(
                        isAppLockEnabled = isAppLockEnabled,
                        hasAppLockPin = hasAppLockPin,
                        isBiometricEnabled = isBiometricEnabled,
                        isBlurInRecentsEnabled = isBlurInRecentsEnabled,
                        isScreenshotProtectionEnabled = isScreenshotProtectionEnabled,
                        autoLockDurationMinutes = autoLockDurationMinutes,
                        onAppLockChange = onAppLockToggleChange,
                        onBiometricChange = onBiometricLockChange,
                        onBlurInRecentsChange = onBlurInRecentsChange,
                        onScreenshotProtectionChange = onScreenshotProtectionChange,
                        onAutoLockDurationChange = onAutoLockDurationChange,
                        onBackClick = {
                            backNavigationRoute?.let { onRouteChange(it) } ?: onRouteChange("settings")
                        }
                    )

                    "category_management" -> CategoryManagementScreen(
                        userProfile = userProfile,
                        customCategories = categories.filter { !it.isSystem },
                        customPaymentTypes = paymentMethods.filter { !it.isSystem },
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange("settings")
                        },
                        onCreateCustomCategory = { name, iconKey, transactionTypeId ->
                            onCreateCustomCategory(name, iconKey, transactionTypeId)
                        },
                        onCreateCustomPaymentType = { name, iconKey ->
                            onCreateCustomPaymentType(name, iconKey)
                        },
                        onDeleteCustomCategory = { categoryId ->
                            onDeleteCustomCategory(categoryId)
                        },
                        onDeleteCustomPaymentType = { paymentTypeId ->
                            onDeleteCustomPaymentType(paymentTypeId)
                        }
                    )

                    "transaction_card_customize" -> TransactionCardCustomizeScreen(
                        settings = transactionCardCustomizationSettings,
                        currencyId = selectedCurrencyId,
                        dateFormatPattern = selectedDateFormatPattern,
                        timeFormat = selectedTimeFormat,
                        onSettingsChange = onTransactionCardCustomizationSettingsChange,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange("settings")
                        }
                    )

                    "profile" -> ProfileScreen(
                        userProfile = userProfile,
                        dateFormatPattern = selectedDateFormatPattern,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(profileOriginRoute)
                        },
                        onSaveClick = { updatedProfile ->
                            onUserProfileChange(updatedProfile)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(profileOriginRoute)
                        },
                        onPrepareForExternalActivity = onPrepareForExternalActivity
                    )

                    "data_management" -> DataManagementScreen(
                        transactionCount = transactionCount,
                        onLegacyImportFileSelected = onLegacyImportFileSelected,
                        onDeleteAllTransactionsClick = onDeleteAllTransactionsClick,
                        onPrepareForExternalActivity = onPrepareForExternalActivity,
                        onBackClick = {
                            backNavigationRoute?.let { onRouteChange(it) } ?: onRouteChange("settings")
                        }
                    )

                    "about" -> AboutScreen(
                        onBackClick = {
                            backNavigationRoute?.let { onRouteChange(it) } ?: onRouteChange("settings")
                        },
                        onPrepareForExternalActivity = onPrepareForExternalActivity
                    )

                    "add_transaction" -> AddTransactionScreen(
                        currencyId = selectedCurrencyId,
                        dateFormatPattern = selectedDateFormatPattern,
                        transactions = transactions,
                        availableCategories = categories.filterNot { it.isDeleted },
                        availablePaymentMethods = paymentMethods.filterNot { it.isDeleted }.sortedBy { it.id },
                        existingTransaction = selectedTransaction,
                        existingRecurringRule = selectedRecurringRule,
                        initialAmountInput = addTransactionDraftAmount,
                        initialNote = addTransactionDraftNote,
                        onBackClick = {
                            exitAddTransactionScreen(previousRoute)
                        },
                        onDeleteClick = deleteSelectedTransaction,
                        onCalculatorClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange("itemized_calculator")
                        },
                        onAmountInputChange = { onAddTransactionDraftAmountChange(it) },
                        onNoteChange = { onAddTransactionDraftNoteChange(it) },
                        onSaveClick = { transaction, recurringDraft ->
                            persistTransactionAndRecurring(transaction, recurringDraft)
                        }
                    )

                    "itemized_calculator" -> ItemizedCalculatorScreen(
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange("add_transaction")
                        },
                        onApplyToNoteClick = { calculatedAmount, calculatedNote ->
                            onAddTransactionDraftAmountChange(calculatedAmount)
                            onAddTransactionDraftNoteChange(calculatedNote)
                            onBottomBarVisibilityChange(false)
                            onRouteChange("add_transaction")
                        }
                    )

                    else -> Box(modifier = Modifier.fillMaxSize())
                }
            }
        }

        if (showFixedBottomNavBar) {
            AppBottomBar(
                currentRoute = if (isBottomTabRoute) currentRoute else null,
                modifier = Modifier.align(Alignment.BottomCenter),
                onItemClick = {
                    onBottomBarVisibilityChange(false)
                    onRouteChange(it)
                },
                onAddClick = {
                    onSelectedTransactionChange(null)
                    onAddTransactionDraftAmountChange(null)
                    onAddTransactionDraftNoteChange(null)
                    onBottomBarVisibilityChange(false)
                    onRouteChange("add_transaction")
                }
            )
        }
    }
}

@Composable
private fun PreloadSecondaryScreenData(
    transactions: List<Transaction>,
    categories: List<CategoryType>,
    recurringRules: List<RecurringTransactionRule>,
    selectedCurrencyId: Int,
    selectedDateFormatPattern: String,
    selectedTimeFormat: String,
    transactionCount: Int,
    autoLockDurationMinutes: Int,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings
) {
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val budgetViewModel: BudgetViewModel = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val transactionsViewModel: TransactionsViewModel = viewModel()

    LaunchedEffect(transactions, categories, selectedCurrencyId) {
        analyticsViewModel.updateInputs(
            transactions = transactions,
            categories = categories,
            currencyId = selectedCurrencyId
        )
    }

    LaunchedEffect(transactions, categories, selectedCurrencyId, recurringRules) {
        budgetViewModel.updateInputs(
            transactions = transactions,
            categories = categories,
            currencyId = selectedCurrencyId,
            recurringRules = recurringRules
        )
    }

    LaunchedEffect(
        transactions,
        selectedCurrencyId,
        selectedDateFormatPattern,
        selectedTimeFormat,
        transactionCardCustomizationSettings
    ) {
        calendarViewModel.updateInputs(
            transactions = transactions,
            currencyId = selectedCurrencyId,
            dateFormatPattern = selectedDateFormatPattern,
            timeFormat = selectedTimeFormat,
            customizationSettings = transactionCardCustomizationSettings
        )
        transactionsViewModel.updateInputs(
            transactions = transactions,
            currencyId = selectedCurrencyId,
            dateFormatPattern = selectedDateFormatPattern,
            timeFormat = selectedTimeFormat,
            customizationSettings = transactionCardCustomizationSettings
        )
    }

    LaunchedEffect(
        selectedCurrencyId,
        selectedDateFormatPattern,
        selectedTimeFormat,
        transactionCount,
        autoLockDurationMinutes
    ) {
        settingsViewModel.updateInputs(
            currentCurrencyId = selectedCurrencyId,
            currentDateFormatPattern = selectedDateFormatPattern,
            currentTimeFormat = selectedTimeFormat,
            autoLockDurationMinutes = autoLockDurationMinutes,
            transactionCount = transactionCount
        )
    }
}

@Composable
private fun BoxScope.PrimaryTabPager(
    pagerState: androidx.compose.foundation.pager.PagerState,
    userProfile: UserProfile,
    transactions: List<Transaction>,
    recurringRules: List<RecurringTransactionRule>,
    categories: List<CategoryType>,
    selectedCurrencyId: Int,
    selectedDateFormatPattern: String,
    selectedTimeFormat: String,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings,
    onRouteChange: (String) -> Unit,
    onProfileOriginRouteChange: (String) -> Unit,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    onSelectedTransactionChange: (Transaction?) -> Unit,
    onAddTransactionDraftAmountChange: (String?) -> Unit,
    onAddTransactionDraftNoteChange: (String?) -> Unit,
    onAddRecurring: (String, com.mkn0079.expensetracker.models.RecurringFrequency, Int) -> Unit,
    onUpdateRecurring: (String, String, com.mkn0079.expensetracker.models.RecurringFrequency, Int) -> Unit,
    onDeleteRecurring: (String) -> Unit,
    onRecurringEnabledChange: (String, Boolean) -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.TopStart),
        userScrollEnabled = false,
        beyondViewportPageCount = bottomTabRoutes.lastIndex,
        key = { page -> bottomTabRoutes[page] }
    ) { page ->
        when (bottomTabRoutes[page]) {
            "home" -> HomeScreen(
                userProfile = userProfile,
                currencyId = selectedCurrencyId,
                timeFormat = selectedTimeFormat,
                transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                onViewAllClick = {
                    onRouteChange("transactions")
                },
                onTodaySpendingClick = {
                    onRouteChange("calendar")
                },
                onProfileClick = {
                    onProfileOriginRouteChange("home")
                    onBottomBarVisibilityChange(false)
                    onRouteChange("profile")
                },
                onSettingsClick = {
                    onBottomBarVisibilityChange(false)
                    onRouteChange("settings")
                },
                onTransactionClick = { transaction ->
                    onSelectedTransactionChange(transaction)
                    onAddTransactionDraftAmountChange(null)
                    onAddTransactionDraftNoteChange(null)
                    onBottomBarVisibilityChange(false)
                    onRouteChange("add_transaction")
                }
            )

            "analytics" -> AnalyticsScreen(
                currencyId = selectedCurrencyId,
                transactions = transactions,
                categories = categories,
                onBackClick = {
                    onBottomBarVisibilityChange(false)
                    onRouteChange("home")
                }
            )

            "budget" -> BudgetScreen(
                currencyId = selectedCurrencyId,
                transactions = transactions,
                availableCategories = categories,
                recurringRules = recurringRules,
                onAddRecurring = onAddRecurring,
                onUpdateRecurring = onUpdateRecurring,
                onDeleteRecurring = onDeleteRecurring,
                onRecurringEnabledChange = onRecurringEnabledChange,
                onBackClick = {
                    onBottomBarVisibilityChange(false)
                    onRouteChange("home")
                }
            )

            "calendar" -> CalendarScreen(
                transactions = transactions,
                currencyId = selectedCurrencyId,
                dateFormatPattern = selectedDateFormatPattern,
                timeFormat = selectedTimeFormat,
                transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                onBackClick = {
                    onBottomBarVisibilityChange(false)
                    onRouteChange("home")
                },
                onTransactionClick = { transaction ->
                    onSelectedTransactionChange(transaction)
                    onAddTransactionDraftAmountChange(null)
                    onAddTransactionDraftNoteChange(null)
                    onBottomBarVisibilityChange(false)
                    onRouteChange("add_transaction")
                }
            )
        }
    }
}
