package com.mkn0079.expensetracker.ui.components

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
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.RecurringTransactionDraft
import com.mkn0079.expensetracker.models.RecurringFrequency
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.ui.navigation.AppNavigationHost
import com.mkn0079.expensetracker.ui.navigation.AppRoute
import com.mkn0079.expensetracker.ui.navigation.resolveBackNavigationRoute
import com.mkn0079.expensetracker.ui.viewmodels.AnalyticsViewModel
import com.mkn0079.expensetracker.ui.viewmodels.BudgetViewModel
import com.mkn0079.expensetracker.ui.viewmodels.CalendarViewModel
import com.mkn0079.expensetracker.ui.viewmodels.HomeViewModel
import com.mkn0079.expensetracker.ui.viewmodels.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mkn0079.expensetracker.ui.viewmodels.TransactionsViewModel

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
    onPrepareForExternalActivity: () -> Unit
) {
    val transactionsViewModel: TransactionsViewModel = viewModel()
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
            paymentMethods = paymentMethods
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
    paymentMethods: List<PaymentType>
) {
    val homeViewModel: HomeViewModel = viewModel()
    val transactionsViewModel: TransactionsViewModel = viewModel()
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val budgetViewModel: BudgetViewModel = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
androidx.compose.runtime.LaunchedEffect(
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
    paymentMethods
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
        settingsViewModel.updateInputs(transactionCount, isAdsEnabled)
    }
}
