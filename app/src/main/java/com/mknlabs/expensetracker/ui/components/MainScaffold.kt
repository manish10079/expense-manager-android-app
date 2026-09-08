package com.mknlabs.expensetracker.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.RecurringTransactionDraft
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.ui.navigation.AppNavigationHost
import com.mknlabs.expensetracker.ui.navigation.AppRoute
import com.mknlabs.expensetracker.ui.navigation.resolveBackNavigationRoute
import com.mknlabs.expensetracker.ui.viewmodels.AnalyticsViewModel
import com.mknlabs.expensetracker.ui.viewmodels.BudgetAndRecurringViewModel
import com.mknlabs.expensetracker.ui.viewmodels.CalendarViewModel
import com.mknlabs.expensetracker.ui.viewmodels.HomeViewModel
import com.mknlabs.expensetracker.ui.viewmodels.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.ui.viewmodels.TransactionsViewModel
import androidx.compose.ui.unit.dp

@Composable
fun MainScaffold(
    currentRoute: AppRoute,
    previousRoute: AppRoute,
    profileOriginRoute: AppRoute,
    isBottomBarVisible: Boolean,
    transactions: List<Transaction>,
    transactionCount: Int,
    recurringRules: List<RecurringTransactionRule>,
    selectedTransaction: Transaction?,
    addTransactionDraftAmount: String?,
    addTransactionDraftNote: String?,
    addTransactionDraftCategoryId: Int? = null,
    addTransactionDraftTypeId: Int? = null,
    categories: List<CategoryType>,
    paymentMethods: List<PaymentType>,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings,
    userProfile: UserProfile,
    selectedCurrencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    selectedDateFormatPattern: String,
    selectedTimeFormat: String,
    isAppLockEnabled: Boolean,
    isLockOverlayActive: Boolean = false,
    hasAppLockPin: Boolean,
    isBiometricEnabled: Boolean,
    isScrambledPinKeypadEnabled: Boolean,
    isBlurInRecentsEnabled: Boolean,
    isScreenshotProtectionEnabled: Boolean,
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    isGoalRemindersEnabled: Boolean,
    isLargeTransactionAlertsEnabled: Boolean,
    isWeeklySummaryEnabled: Boolean,
    isBillRemindersEnabled: Boolean,
    isFinancialInsightsEnabled: Boolean,
    isCloudSecurityEnabled: Boolean,
    largeTransactionThresholdMinor: Long,
    weeklySummaryTimeMillis: Long,
    reminderMorningStartHour: Int,
    reminderMorningEndHour: Int,
    reminderEveningStartHour: Int,
    reminderEveningEndHour: Int,
    isAdsEnabled: Boolean,
    autoLockDurationMinutes: Int,
    isAutoBackupEnabled: Boolean,
    autoBackupFrequencyDays: Int,
    userTier: com.mknlabs.expensetracker.models.UserTier,
    appSettings: com.mknlabs.expensetracker.models.AppSettings,
    onRouteChange: (AppRoute) -> Unit,
    onProfileOriginRouteChange: (AppRoute) -> Unit,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    onSelectedTransactionChange: (Transaction?) -> Unit,
    onAddTransactionDraftAmountChange: (String?) -> Unit,
    onAddTransactionDraftNoteChange: (String?) -> Unit,
    onAddTransactionDraftCategoryIdChange: (Int?) -> Unit = {},
    onAddTransactionDraftTypeIdChange: (Int?) -> Unit = {},
    onSaveTransaction: (Transaction, RecurringTransactionDraft?, RecurringTransactionRule?) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onSwipeDeleteTransaction: (Transaction) -> Unit = {},
    onRestoreTransaction: (Transaction, RecurringTransactionRule?) -> Unit = { _, _ -> },
    onDuplicateTransaction: (Transaction, (Transaction) -> Unit) -> Unit = { _, _ -> },
    onDeleteRecurring: (String) -> Unit,
    onRecurringEnabledChange: (String, Boolean) -> Unit,
    onRecurringNotificationsEnabledChange: (String, Boolean) -> Unit,
    onUpdateRecurringRule: (String, RecurringFrequency, Int) -> Unit,
    onCreateCustomCategory: (String, String, Int) -> Unit,
    onCreateCustomPaymentType: (String, String) -> Unit,
    onDeleteCustomCategory: (Int) -> Unit,
    onDeleteCustomPaymentType: (Int) -> Unit,
    onGoalsClick: () -> Unit,
    onTransactionCardCustomizationSettingsChange: (TransactionCardCustomizationSettings) -> Unit,
    onUserProfileChange: (UserProfile) -> Unit,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit,
    onGoalRemindersChange: (Boolean) -> Unit,
    onReminderWindowChange: (com.mknlabs.expensetracker.models.ReminderWindow, Int, Int) -> Unit,
    onExpenseRemindersChange: (Boolean) -> Unit,
    onLargeTransactionAlertsChange: (Boolean) -> Unit,
    onWeeklySummaryChange: (Boolean) -> Unit,
    onBillRemindersChange: (Boolean) -> Unit,
    onFinancialInsightsChange: (Boolean) -> Unit,
    onCloudSecurityChange: (Boolean) -> Unit,
    onLargeTransactionThresholdChange: (Long) -> Unit,
    onWeeklySummaryTimeChange: (Long) -> Unit,
    onPremiumCardClick: () -> Unit,
    onTestNotification: () -> Unit,
    onDatabaseBackupFileSelected: (Uri) -> Unit,
    onDatabaseRestoreFileSelected: (Uri) -> Unit,
    onJsonExportFileSelected: (Uri) -> Unit,
    onJsonImportFileSelected: (Uri) -> Unit,
    onLegacyImportFileSelected: (Uri) -> Unit,
    onDeleteAllTransactionsClick: () -> Unit,
    onBiometricLockChange: (Boolean) -> Unit,
    onScrambledPinKeypadChange: (Boolean) -> Unit,
    onBlurInRecentsChange: (Boolean) -> Unit,
    onScreenshotProtectionChange: (Boolean) -> Unit,
    onAutoLockDurationChange: (Int) -> Unit,
    onAppLockToggleChange: (Boolean) -> Unit,
    onAutoBackupEnabledChange: (Boolean) -> Unit,
    onAutoBackupFrequencyChange: (Int) -> Unit,
    onCloudSyncEnabledChange: (Boolean) -> Unit,
    onLinkAccountClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDirectSignOut: () -> Unit = {},
    onAdFreeAccessClick: () -> Unit = {},
    onShowUpgradeSheet: () -> Unit,
    onPrepareForExternalActivity: () -> Unit
) {
    val transactionsViewModel: TransactionsViewModel = hiltViewModel()
    val transactionsUiState by transactionsViewModel.uiState.collectAsStateWithLifecycle()
    val isSelectionMode = currentRoute == AppRoute.Transactions && transactionsUiState.isSelectionMode
    val saveableStateHolder = androidx.compose.runtime.saveable.rememberSaveableStateHolder()

    val showFixedBottomNavBar = currentRoute.showsFixedBottomBar && !isSelectionMode

    // Adaptive shell: the branded rail replaces the floating bottom bar everywhere
    // except the classic Compact portrait phone footprint (incl. phone landscape,
    // where width is sufficient for a rail).
    val useNavigationRail = showFixedBottomNavBar && !LocalAppWindowInfo.current.isCompactPortrait
    val backNavigationRoute = resolveBackNavigationRoute(
        currentRoute = currentRoute,
        profileOriginRoute = profileOriginRoute,
        previousRoute = previousRoute
    )
    val colorScheme = MaterialTheme.colorScheme

    // Shared visibility controller for the standalone AddTransactionFab. Tab
    // screens flip this from their list's scroll direction; the slot composable
    // reads only this value so bar flips stay scoped.
    val addFabVisibility = remember { mutableStateOf(true) }

    LaunchedEffect(currentRoute) {
        if (currentRoute != AppRoute.AddTransaction && currentRoute != AppRoute.ItemizedCalculator) {
            saveableStateHolder.removeState(AppRoute.AddTransaction)
            onSelectedTransactionChange(null)
            onAddTransactionDraftAmountChange(null)
            onAddTransactionDraftNoteChange(null)
            onAddTransactionDraftCategoryIdChange(null)
            onAddTransactionDraftTypeIdChange(null)
        }
        if (currentRoute != AppRoute.Transactions) {
            transactionsViewModel.clearSelection()
        }
    }

    BackHandler(enabled = backNavigationRoute != null) {
        when {
            currentRoute == AppRoute.AddTransaction -> {
                onBottomBarVisibilityChange(false)
                onRouteChange(previousRoute)
            }
            currentRoute == AppRoute.ItemizedCalculator -> {
                onBottomBarVisibilityChange(false)
                onRouteChange(AppRoute.AddTransaction)
            }
            backNavigationRoute != null -> {
                onBottomBarVisibilityChange(false)
                onRouteChange(backNavigationRoute)
            }
        }
    }

    CompositionLocalProvider(LocalAddFabVisibility provides addFabVisibility) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
        ) {
        PreloadSecondaryScreenData(
            transactions = transactions,
            categories = categories,
            recurringRules = recurringRules,
            selectedCurrencyId = selectedCurrencyId,
            amountFormatPreferences = amountFormatPreferences,
            selectedDateFormatPattern = selectedDateFormatPattern,
            selectedTimeFormat = selectedTimeFormat,
            transactionCount = transactionCount,
            isAdsEnabled = isAdsEnabled,
            autoLockDurationMinutes = autoLockDurationMinutes,
            userProfile = userProfile,
            transactionCardCustomizationSettings = transactionCardCustomizationSettings,
            paymentMethods = paymentMethods,
            isCloudSyncEnabled = appSettings.isCloudSyncEnabled,
            userTier = userTier
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = if (useNavigationRail) AppNavigationRailWidth + 12.dp else 0.dp)
        ) {
            AppNavigationHost(
            saveableStateHolder = saveableStateHolder,
            isAdsEnabled = isAdsEnabled,
            currentRoute = currentRoute,
            previousRoute = previousRoute,
            profileOriginRoute = profileOriginRoute,
            transactions = transactions,
            transactionCount = transactionCount,
            recurringRules = recurringRules,
            selectedTransaction = selectedTransaction,
            addTransactionDraftAmount = addTransactionDraftAmount,
            addTransactionDraftNote = addTransactionDraftNote,
            categories = categories,
            paymentMethods = paymentMethods,
            transactionCardCustomizationSettings = transactionCardCustomizationSettings,
            userProfile = userProfile,
            appSettings = appSettings,
            selectedCurrencyId = selectedCurrencyId,
            amountFormatPreferences = amountFormatPreferences,
            selectedDateFormatPattern = selectedDateFormatPattern,
            selectedTimeFormat = selectedTimeFormat,
            isAppLockEnabled = isAppLockEnabled,
            isLockOverlayActive = isLockOverlayActive,
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
            autoLockDurationMinutes = autoLockDurationMinutes,
            isAutoBackupEnabled = isAutoBackupEnabled,
            autoBackupFrequencyDays = autoBackupFrequencyDays,
            isCloudSyncEnabled = appSettings.isCloudSyncEnabled,
            userTier = userTier,
            onRouteChange = onRouteChange,
            onProfileOriginRouteChange = onProfileOriginRouteChange,
            onBottomBarVisibilityChange = onBottomBarVisibilityChange,
            onSelectedTransactionChange = onSelectedTransactionChange,
            onAddTransactionDraftAmountChange = onAddTransactionDraftAmountChange,
            onAddTransactionDraftNoteChange = onAddTransactionDraftNoteChange,
            onSaveTransaction = onSaveTransaction,
            onDeleteTransaction = onDeleteTransaction,
            onSwipeDeleteTransaction = onSwipeDeleteTransaction,
            onRestoreTransaction = onRestoreTransaction,
            onDuplicateTransaction = onDuplicateTransaction,
            onDeleteRecurring = onDeleteRecurring,
            onRecurringEnabledChange = onRecurringEnabledChange,
            onRecurringNotificationsEnabledChange = onRecurringNotificationsEnabledChange,
            onUpdateRecurringRule = onUpdateRecurringRule,
            onCreateCustomCategory = onCreateCustomCategory,
            onCreateCustomPaymentType = onCreateCustomPaymentType,
            onDeleteCustomCategory = onDeleteCustomCategory,
            onDeleteCustomPaymentType = onDeleteCustomPaymentType,
            onGoalsClick = onGoalsClick,
            onTransactionCardCustomizationSettingsChange = onTransactionCardCustomizationSettingsChange,
            onUserProfileChange = onUserProfileChange,
            onDailyReminderChange = onDailyReminderChange,
            onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
            onMissedEntryReminderChange = onMissedEntryReminderChange,
            onGoalRemindersChange = onGoalRemindersChange,
            onReminderWindowChange = onReminderWindowChange,
            onExpenseRemindersChange = onExpenseRemindersChange,
            onLargeTransactionAlertsChange = onLargeTransactionAlertsChange,
            onWeeklySummaryChange = onWeeklySummaryChange,
            onBillRemindersChange = onBillRemindersChange,
            onFinancialInsightsChange = onFinancialInsightsChange,
            onCloudSecurityChange = onCloudSecurityChange,
            onLargeTransactionThresholdChange = onLargeTransactionThresholdChange,
            onWeeklySummaryTimeChange = onWeeklySummaryTimeChange,
            onPremiumCardClick = onPremiumCardClick,
            onTestNotification = onTestNotification,
            onDatabaseBackupFileSelected = onDatabaseBackupFileSelected,
            onDatabaseRestoreFileSelected = onDatabaseRestoreFileSelected,
            onJsonExportFileSelected = onJsonExportFileSelected,
            onJsonImportFileSelected = onJsonImportFileSelected,
            onLegacyImportFileSelected = onLegacyImportFileSelected,
            onDeleteAllTransactionsClick = onDeleteAllTransactionsClick,
            onBiometricLockChange = onBiometricLockChange,
            onScrambledPinKeypadChange = onScrambledPinKeypadChange,
            onBlurInRecentsChange = onBlurInRecentsChange,
            onScreenshotProtectionChange = onScreenshotProtectionChange,
            onAutoLockDurationChange = onAutoLockDurationChange,
            onAppLockToggleChange = onAppLockToggleChange,
            onAutoBackupEnabledChange = onAutoBackupEnabledChange,
            onAutoBackupFrequencyChange = onAutoBackupFrequencyChange,
            onCloudSyncEnabledChange = onCloudSyncEnabledChange,
            onLinkAccountClick = onLinkAccountClick,
            onLogoutClick = onLogoutClick,
            onDirectSignOut = onDirectSignOut,
            onAdFreeAccessClick = onAdFreeAccessClick,
            onShowUpgradeSheet = onShowUpgradeSheet,
            onPrepareForExternalActivity = onPrepareForExternalActivity
            )
        }

        if (useNavigationRail) {
            AppNavigationRail(
                modifier = Modifier.align(Alignment.CenterStart),
                currentRoute = currentRoute,
                onItemClick = { route ->
                    onBottomBarVisibilityChange(false)
                    onRouteChange(route)
                },
                onAddClick = {
                    onBottomBarVisibilityChange(false)
                    onRouteChange(AppRoute.AddTransaction)
                }
            )
        } else if (showFixedBottomNavBar) {
            // Floating capsule bottom bar — margins handled internally (12dp).
            AppBottomBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                currentRoute = currentRoute,
                onItemClick = { route ->
                    onBottomBarVisibilityChange(false)
                    onRouteChange(route)
                }
            )

            // Standard 56dp FAB floating above the bar's top-right corner.
            // Auto-hides while the user scrolls down and reappears on scroll up
            // (bound per screen via rememberBindAddFabToScroll). Shown only on Home tab.
            if (currentRoute == AppRoute.Home) {
                AddTransactionFabSlot(
                    onClick = {
                        onBottomBarVisibilityChange(false)
                        onRouteChange(AppRoute.AddTransaction)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 112.dp, end = 35.dp)
                )
            }
        }
    }
    }
}

@Composable
private fun BoxScope.PreloadSecondaryScreenData(
    transactions: List<Transaction>,
    categories: List<CategoryType>,
    recurringRules: List<RecurringTransactionRule>,
    selectedCurrencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    selectedDateFormatPattern: String,
    selectedTimeFormat: String,
    transactionCount: Int,
    isAdsEnabled: Boolean,
    autoLockDurationMinutes: Int,
    userProfile: UserProfile,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings,
    paymentMethods: List<PaymentType>,
    isCloudSyncEnabled: Boolean,
    userTier: com.mknlabs.expensetracker.models.UserTier
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val transactionsViewModel: TransactionsViewModel = hiltViewModel()
    val analyticsViewModel: AnalyticsViewModel = hiltViewModel()
    val budgetViewModel: BudgetAndRecurringViewModel = hiltViewModel()
    val calendarViewModel: CalendarViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    LaunchedEffect(
        userProfile,
        transactions,
        categories,
        recurringRules,
        selectedCurrencyId,
        amountFormatPreferences,
        selectedDateFormatPattern,
        selectedTimeFormat,
        transactionCount,
        isAdsEnabled,
        autoLockDurationMinutes,
        transactionCardCustomizationSettings,
        paymentMethods,
        isCloudSyncEnabled,
        userTier
    ) {
        homeViewModel.updateInputs(
            userProfile = userProfile,
            userTier = userTier,
            currencyId = selectedCurrencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = selectedDateFormatPattern,
            timeFormat = selectedTimeFormat,
            categories = categories,
            customizationSettings = transactionCardCustomizationSettings
        )
        analyticsViewModel.updateInputs(
            transactions,
            categories,
            paymentMethods,
            selectedCurrencyId,
            amountFormatPreferences
        )
        budgetViewModel.updateInputs(
            transactions,
            categories,
            selectedCurrencyId,
            amountFormatPreferences,
            recurringRules
        )
        calendarViewModel.updateInputs(
            transactions,
            categories,
            selectedCurrencyId,
            amountFormatPreferences,
            selectedDateFormatPattern,
            selectedTimeFormat,
            transactionCardCustomizationSettings
        )
        transactionsViewModel.updateInputs(
            categories = categories,
            paymentMethods = paymentMethods,
            currencyId = selectedCurrencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = selectedDateFormatPattern,
            timeFormat = selectedTimeFormat,
            customizationSettings = transactionCardCustomizationSettings
        )
        settingsViewModel.updateInputs(transactionCount, isAdsEnabled, userTier, isCloudSyncEnabled)
    }
}
