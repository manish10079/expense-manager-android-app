package com.mknlabs.expensetracker.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.AmountFormatPreferences
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mknlabs.expensetracker.ui.viewmodels.TransactionsViewModel

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
    categories: List<CategoryType>,
    paymentMethods: List<PaymentType>,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings,
    userProfile: UserProfile,
    selectedCurrencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    selectedDateFormatPattern: String,
    selectedTimeFormat: String,
    isAppLockEnabled: Boolean,
    hasAppLockPin: Boolean,
    isBiometricEnabled: Boolean,
    isScrambledPinKeypadEnabled: Boolean,
    isBlurInRecentsEnabled: Boolean,
    isScreenshotProtectionEnabled: Boolean,
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    isAdsEnabled: Boolean,
    autoLockDurationMinutes: Int,
    isAutoBackupEnabled: Boolean,
    autoBackupFrequencyDays: Int,
    userTier: com.mknlabs.expensetracker.models.UserTier,
    onRouteChange: (AppRoute) -> Unit,
    onProfileOriginRouteChange: (AppRoute) -> Unit,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    onSelectedTransactionChange: (Transaction?) -> Unit,
    onAddTransactionDraftAmountChange: (String?) -> Unit,
    onAddTransactionDraftNoteChange: (String?) -> Unit,
    onSaveTransaction: (Transaction, RecurringTransactionDraft?, RecurringTransactionRule?) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onDeleteRecurring: (String) -> Unit,
    onRecurringEnabledChange: (String, Boolean) -> Unit,
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
    onLinkAccountClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onShowUpgradeSheet: () -> Unit,
    onPrepareForExternalActivity: () -> Unit
) {
    val transactionsViewModel: TransactionsViewModel = hiltViewModel()
    val transactionsUiState by transactionsViewModel.uiState.collectAsState()
    val isSelectionMode = currentRoute == AppRoute.Transactions && transactionsUiState.isSelectionMode
    val saveableStateHolder = androidx.compose.runtime.saveable.rememberSaveableStateHolder()

    val showFixedBottomNavBar = currentRoute.showsFixedBottomBar && !isSelectionMode
    val backNavigationRoute = resolveBackNavigationRoute(
        currentRoute = currentRoute,
        profileOriginRoute = profileOriginRoute,
        previousRoute = previousRoute
    )
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(currentRoute) {
        if (currentRoute != AppRoute.AddTransaction && currentRoute != AppRoute.ItemizedCalculator) {
            saveableStateHolder.removeState(AppRoute.AddTransaction)
            onSelectedTransactionChange(null)
            onAddTransactionDraftAmountChange(null)
            onAddTransactionDraftNoteChange(null)
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
            amountFormatPreferences = amountFormatPreferences,
            selectedDateFormatPattern = selectedDateFormatPattern,
            selectedTimeFormat = selectedTimeFormat,
            transactionCount = transactionCount,
            isAdsEnabled = isAdsEnabled,
            autoLockDurationMinutes = autoLockDurationMinutes,
            userProfile = userProfile,
            transactionCardCustomizationSettings = transactionCardCustomizationSettings,
            paymentMethods = paymentMethods,
            userTier = userTier
        )

        AppNavigationHost(
            saveableStateHolder = saveableStateHolder,
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
            userTier = userTier,
            onRouteChange = onRouteChange,
            onProfileOriginRouteChange = onProfileOriginRouteChange,
            onBottomBarVisibilityChange = onBottomBarVisibilityChange,
            onSelectedTransactionChange = onSelectedTransactionChange,
            onAddTransactionDraftAmountChange = onAddTransactionDraftAmountChange,
            onAddTransactionDraftNoteChange = onAddTransactionDraftNoteChange,
            onSaveTransaction = onSaveTransaction,
            onDeleteTransaction = onDeleteTransaction,
            onDeleteRecurring = onDeleteRecurring,
            onRecurringEnabledChange = onRecurringEnabledChange,
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
            onLinkAccountClick = onLinkAccountClick,
            onLogoutClick = onLogoutClick,
            onShowUpgradeSheet = onShowUpgradeSheet,
            onPrepareForExternalActivity = onPrepareForExternalActivity
        )

        if (showFixedBottomNavBar) {
            AppBottomBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
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
        userTier
    ) {
        homeViewModel.updateInputs(
            userProfile = userProfile,
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
            transactions,
            categories,
            selectedCurrencyId,
            amountFormatPreferences,
            selectedDateFormatPattern,
            selectedTimeFormat,
            transactionCardCustomizationSettings
        )
        settingsViewModel.updateInputs(transactionCount, isAdsEnabled, userTier)
    }
}
