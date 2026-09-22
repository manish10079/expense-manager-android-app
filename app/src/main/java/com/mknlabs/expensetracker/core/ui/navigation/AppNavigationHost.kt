package com.mknlabs.expensetracker.core.ui.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.core.ui.models.CategoryManagementTab
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.RecurringPlanEdit
import com.mknlabs.expensetracker.models.RecurringTransactionDraft
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.feature.settings.ui.AboutScreen
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsInboxItemUi
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsInboxRoute
import com.mknlabs.expensetracker.feature.settings.ui.FeedbackRoute
import com.mknlabs.expensetracker.feature.transactions.ui.AddTransactionScreen
import com.mknlabs.expensetracker.feature.analytics.ui.AnalyticsScreen
import com.mknlabs.expensetracker.feature.budget.ui.BudgetAndRecurringScreen
import com.mknlabs.expensetracker.feature.settings.ui.AddCategoryScreen
import com.mknlabs.expensetracker.feature.calendar.ui.CalendarScreen
import com.mknlabs.expensetracker.feature.settings.ui.CategoryManagementScreen
import com.mknlabs.expensetracker.feature.settings.ui.DataManagementScreen
import com.mknlabs.expensetracker.feature.goals.ui.GoalsScreen
import com.mknlabs.expensetracker.feature.home.ui.HomeScreen
import com.mknlabs.expensetracker.feature.transactions.ui.ItemizedCalculatorScreen
import com.mknlabs.expensetracker.feature.settings.ui.NotificationSettingsScreen
import com.mknlabs.expensetracker.feature.settings.ui.PreferencesScreen
import com.mknlabs.expensetracker.feature.profile.ui.ProfileScreen
import com.mknlabs.expensetracker.feature.settings.ui.ConnectedDevicesScreen
import com.mknlabs.expensetracker.feature.settings.ui.SecurityPrivacyScreen
import com.mknlabs.expensetracker.feature.settings.ui.SettingsScreen
import com.mknlabs.expensetracker.feature.transactions.ui.TransactionCardCustomizeScreen
import com.mknlabs.expensetracker.feature.transactions.ui.TransactionScreen
import com.mknlabs.expensetracker.feature.profile.ui.MembershipDetailsScreen
import java.util.UUID
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.feature.transactions.ui.ItemizedCalculatorViewModel

@Composable
fun AppNavigationHost(
    saveableStateHolder: androidx.compose.runtime.saveable.SaveableStateHolder,
    currentRoute: AppRoute,
    previousRoute: AppRoute,
    profileOriginRoute: AppRoute,
    transactions: List<Transaction>,
    transactionCount: Int,
    recurringRules: List<RecurringTransactionRule>,
    selectedTransaction: Transaction?,
    addTransactionDraftAmount: String?,
    addTransactionDraftNote: String?,
    addTransactionDraftCategoryId: Int? = null,
    addTransactionDraftTypeId: Int? = null,
    addTransactionDraftAutoStartVoice: Boolean = false,
    onVoiceAutoStarted: () -> Unit = {},
    /**
     * A detected-SMS notification asked for one inbox row: [smsInboxFocusId] is the row,
     * [smsInboxOpenEditor] says its Edit action was used, and [onSmsInboxFocusConsumed]
     * releases the request once the inbox has taken it.
     */
    smsInboxFocusId: String? = null,
    smsInboxOpenEditor: Boolean = false,
    onSmsInboxFocusConsumed: () -> Unit = {},
    /**
     * The detection the Add Transaction draft was built from, when the inbox opened it.
     * Null for every other way into that screen (Home, shortcuts, voice, a notification).
     */
    smsInboxDraftDetectionId: String? = null,
    /** A live detection card was tapped: open the Add Transaction screen prefilled with it. */
    onSmsInboxReview: (SmsInboxItemUi) -> Unit = {},
    /** A filed detection card was tapped: open the transaction it created. */
    onSmsInboxOpenTransaction: (String) -> Unit = {},
    /** The draft was saved: stamp the detection with the transaction that came out of it. */
    onSmsDetectionSaved: (String, String) -> Unit = { _, _ -> },
    categories: List<CategoryType>,
    paymentMethods: List<PaymentType>,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings,
    userProfile: UserProfile,
    appSettings: com.mknlabs.expensetracker.models.AppSettings,
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
    autoLockDurationMinutes: Int,
    isAutoBackupEnabled: Boolean,
    autoBackupFrequencyDays: Int,
    isCloudSyncEnabled: Boolean,
    userTier: com.mknlabs.expensetracker.models.UserTier,
    isAdsEnabled: Boolean,
    onRouteChange: (AppRoute) -> Unit,
    onProfileOriginRouteChange: (AppRoute) -> Unit,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    onSelectedTransactionChange: (Transaction?) -> Unit,
    onAddTransactionDraftAmountChange: (String?) -> Unit,
    onAddTransactionDraftNoteChange: (String?) -> Unit,
    onSaveTransaction: (Transaction, RecurringTransactionDraft?, RecurringTransactionRule?) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onSwipeDeleteTransaction: (Transaction) -> Unit = {},
    onRestoreTransaction: (Transaction, RecurringTransactionRule?) -> Unit = { _, _ -> },
    onDuplicateTransaction: (Transaction, (Transaction) -> Unit) -> Unit,
    onDeleteRecurring: (String) -> Unit,
    onRecurringEnabledChange: (String, Boolean) -> Unit,
    onRecurringNotificationsEnabledChange: (String, Boolean) -> Unit,
    onUpdateRecurringRule: (String, RecurringFrequency, Int) -> Unit,
    onSaveRecurringPlan: (String, RecurringFrequency, RecurringPlanEdit) -> Unit = { _, _, _ -> },
    onConvertRecurringToRegular: (String) -> Unit = {},
    onPayInstallments: (List<String>) -> Unit = {},
    onSkipInstallment: (String) -> Unit = {},
    onUndoInstallment: (String) -> Unit = {},
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
    var addingCategoryTargetTab by remember { mutableStateOf(CategoryManagementTab.Expense) }
    var showEmailUpdateSuccessDialog by remember { mutableStateOf(false) }
    
    val exitAddTransactionScreen: (AppRoute) -> Unit = { destinationRoute ->
        onBottomBarVisibilityChange(false)
        onRouteChange(destinationRoute)
    }
    val selectedRecurringRule = selectedTransaction?.let { transaction ->
        recurringRules.firstOrNull { it.transactionId == transaction.id }
    }
    // Canonical Pro check (effective tier already accounts for expiry / passes).
    // Gates the transaction-card note tooltip (a Pro feature).
    val isProUser = userTier == com.mknlabs.expensetracker.models.UserTier.PREMIUM

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            screenTransition(
                fromRoute = initialState,
                toRoute = targetState
            )
        },
        label = "main_navigation",
        modifier = Modifier.fillMaxSize()
    ) { route ->
        saveableStateHolder.SaveableStateProvider(route) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (route) {
                AppRoute.Home -> {
                    HomeScreen(
                        isAdsEnabled = isAdsEnabled,
                        isLockOverlayActive = isLockOverlayActive,
                        userProfile = userProfile,
                        appSettings = appSettings,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = selectedDateFormatPattern,
                        timeFormat = selectedTimeFormat,
                        categories = categories,
                        transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                        onTransactionClick = { transaction: Transaction ->
                            onSelectedTransactionChange(transaction)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.AddTransaction)
                        },
                        onViewAllClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Transactions)
                        },
                        onProfileClick = {
                            onProfileOriginRouteChange(AppRoute.Home)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Profile)
                        },
                        onSettingsClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        },
                        onSmsInboxClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.DetectedSms)
                        },
                        onTodaySpendingClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Calendar)
                        },
                        onGoalsClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Goals)
                        },
                        onPrepareForExternalActivity = onPrepareForExternalActivity
                    )
                }

                AppRoute.Analytics -> {
                    AnalyticsScreen(
                        isAdsEnabled = isAdsEnabled,
                        isProUser = isProUser,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = selectedDateFormatPattern,
                        transactions = transactions,
                        categories = categories,
                        paymentMethods = paymentMethods,
                        monthStartDay = appSettings.monthStartDay,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Home)
                        }
                    )
                }

                AppRoute.Budget -> {
                    BudgetAndRecurringScreen(
                        isAdsEnabled = isAdsEnabled,
                        isProUser = isProUser,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        transactions = transactions,
                        availableCategories = categories.filter { !it.isDeleted },
                        recurringRules = recurringRules,
                        monthStartDay = appSettings.monthStartDay,
                        onDeleteRecurring = onDeleteRecurring,
                        onRecurringEnabledChange = onRecurringEnabledChange,
                        onRecurringNotificationsEnabledChange = onRecurringNotificationsEnabledChange,
                        onUpdateRecurringRule = onUpdateRecurringRule,
                        onSaveRecurringPlan = onSaveRecurringPlan,
                        onConvertRecurringToRegular = onConvertRecurringToRegular,
                        onPayInstallments = onPayInstallments,
                        onSkipInstallment = onSkipInstallment,
                        onUndoInstallment = onUndoInstallment,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Home)
                        }
                    )
                }

                AppRoute.DetectedSms -> {
                    // Detected SMS inbox: every bank message that looked like a payment,
                    // kept independently of the transactions it may become.
                    SmsInboxRoute(
                        categories = categories.filter { !it.isDeleted },
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = selectedDateFormatPattern,
                        timeFormat = selectedTimeFormat,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Home)
                        },
                        focusDetectionId = smsInboxFocusId,
                        focusOpenEditor = smsInboxOpenEditor,
                        onFocusConsumed = onSmsInboxFocusConsumed,
                        onReviewInAddTransaction = onSmsInboxReview,
                        onOpenFiledTransaction = onSmsInboxOpenTransaction
                    )
                }

                AppRoute.Calendar -> {
                    CalendarScreen(
                        isAdsEnabled = isAdsEnabled,
                        isProUser = isProUser,
                        transactions = transactions,
                        categories = categories,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = selectedDateFormatPattern,
                        timeFormat = selectedTimeFormat,
                        monthStartDay = appSettings.monthStartDay,
                        transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Home)
                        },
                        onTransactionClick = { transaction: Transaction ->
                            onSelectedTransactionChange(transaction)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.AddTransaction)
                        }
                    )
                }

                AppRoute.Transactions -> {
                    TransactionScreen(
                        isAdsEnabled = isAdsEnabled,
                        isProUser = isProUser,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = selectedDateFormatPattern,
                        timeFormat = selectedTimeFormat,
                        categories = categories,
                        paymentMethods = paymentMethods,
                        transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                        recurringRules = recurringRules,
                        onDuplicateTransaction = onDuplicateTransaction,
                        onDeleteTransaction = onSwipeDeleteTransaction,
                        onRestoreTransaction = onRestoreTransaction,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Home)
                        },
                        onAddTransactionClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.AddTransaction)
                        },
                        onTransactionClick = { transaction: Transaction ->
                            onSelectedTransactionChange(transaction)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.AddTransaction)
                        }
                    )
                }

                AppRoute.Settings -> {
                    SettingsScreen(
                        isAdsEnabled = isAdsEnabled,
                        userProfile = userProfile,
                        userTier = userTier,
                        isDailyReminderEnabled = isDailyReminderEnabled,
                        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                        transactionCount = transactionCount,
                        onDailyReminderChange = onDailyReminderChange,
                        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                        onMissedEntryReminderChange = onMissedEntryReminderChange,
                        onProfileClick = {
                            onProfileOriginRouteChange(AppRoute.Settings)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Profile)
                        },
                        onPreferencesClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Preferences)
                        },
                        onSecurityPrivacyClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.SecurityPrivacy)
                        },
                        onDataManagementClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.DataManagement)
                        },
                        onAboutClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.About)
                        },
                        onNotificationsClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.NotificationSettings)
                        },
                        onTransactionCardCustomizeClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.TransactionCardCustomize)
                        },
                        onManageCategoryClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.CategoryManagement)
                        },
                        onGoalsClick = onGoalsClick,
                        onConnectedDevicesClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.ConnectedDevices)
                        },
                        onAdFreeAccessClick = onAdFreeAccessClick,
                        onShowUpgradeSheet = onShowUpgradeSheet,
                        onMembershipClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.MembershipDetails)
                        },
                        onLinkAccountClick = onLinkAccountClick,
                        onLogoutClick = onLogoutClick,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Home)
                        }
                    )
                }

                AppRoute.ConnectedDevices -> {
                    ConnectedDevicesScreen(
                        userTier = userTier,
                        isSyncEnabled = isCloudSyncEnabled,
                        onSyncEnabledChange = onCloudSyncEnabledChange,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        },
                        onUpgradeClick = {
                            // This will trigger the AdFreeAccess flow which doubles as our current 'Premium' upsell
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                            // We trigger the AdFree flow in the next turn via SettingsActionId handling
                        }
                    )
                }

                AppRoute.About -> {
                    AboutScreen(
                        isAdsEnabled = isAdsEnabled,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        },
                        onFeedbackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Feedback)
                        },
                        onPrepareForExternalActivity = onPrepareForExternalActivity
                    )
                }

                AppRoute.Feedback -> {
                    FeedbackRoute(
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.About)
                        }
                    )
                }

                AppRoute.NotificationSettings -> {
                    NotificationSettingsScreen(
                        isAdsEnabled = isAdsEnabled,
                        isExpenseRemindersEnabled = isDailyReminderEnabled,
                        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                        isLargeTransactionAlertsEnabled = isLargeTransactionAlertsEnabled,
                        isWeeklySummaryEnabled = isWeeklySummaryEnabled,
                        isGoalRemindersEnabled = isGoalRemindersEnabled,
                        isBillRemindersEnabled = isBillRemindersEnabled,
                        isFinancialInsightsEnabled = isFinancialInsightsEnabled,
                        isCloudSecurityEnabled = isCloudSecurityEnabled,
                        largeTransactionThresholdMinor = largeTransactionThresholdMinor,
                        weeklySummaryTimeMillis = weeklySummaryTimeMillis,
                        reminderMorningStartHour = reminderMorningStartHour,
                        reminderMorningEndHour = reminderMorningEndHour,
                        reminderEveningStartHour = reminderEveningStartHour,
                        reminderEveningEndHour = reminderEveningEndHour,
                        timeFormat = selectedTimeFormat,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        isPremium = userTier == com.mknlabs.expensetracker.models.UserTier.PREMIUM,
                        onExpenseRemindersChange = onExpenseRemindersChange,
                        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                        onLargeTransactionAlertsChange = onLargeTransactionAlertsChange,
                        onWeeklySummaryChange = onWeeklySummaryChange,
                        onGoalRemindersChange = onGoalRemindersChange,
                        onBillRemindersChange = onBillRemindersChange,
                        onFinancialInsightsChange = onFinancialInsightsChange,
                        onCloudSecurityChange = onCloudSecurityChange,
                        onLargeTransactionThresholdChange = onLargeTransactionThresholdChange,
                        onWeeklySummaryTimeChange = onWeeklySummaryTimeChange,
                        onReminderWindowChange = onReminderWindowChange,
                        onPremiumCardClick = onPremiumCardClick,
                        onTestNotification = onTestNotification,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }

                AppRoute.Preferences -> {
                    PreferencesScreen(
                        isAdsEnabled = isAdsEnabled,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }

                AppRoute.SecurityPrivacy -> {
                    SecurityPrivacyScreen(
                        isAdsEnabled = isAdsEnabled,
                        isAppLockEnabled = isAppLockEnabled,
                        hasAppLockPin = hasAppLockPin,
                        isBiometricEnabled = isBiometricEnabled,
                        isScrambledPinKeypadEnabled = isScrambledPinKeypadEnabled,
                        isBlurInRecentsEnabled = isBlurInRecentsEnabled,
                        isScreenshotProtectionEnabled = isScreenshotProtectionEnabled,
                        autoLockDurationMinutes = autoLockDurationMinutes,
                        onAppLockChange = onAppLockToggleChange,
                        onBiometricChange = onBiometricLockChange,
                        onScrambledPinKeypadChange = onScrambledPinKeypadChange,
                        onBlurInRecentsChange = onBlurInRecentsChange,
                        onScreenshotProtectionChange = onScreenshotProtectionChange,
                        onAutoLockDurationChange = onAutoLockDurationChange,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }

                AppRoute.DataManagement -> {
                    DataManagementScreen(
                        isAdsEnabled = isAdsEnabled,
                        transactionCount = transactionCount,
                        onDatabaseBackupFileSelected = onDatabaseBackupFileSelected,
                        onDatabaseRestoreFileSelected = onDatabaseRestoreFileSelected,
                        onJsonExportFileSelected = onJsonExportFileSelected,
                        onJsonImportFileSelected = onJsonImportFileSelected,
                        onLegacyImportFileSelected = onLegacyImportFileSelected,
                        onDeleteAllTransactionsClick = onDeleteAllTransactionsClick,
                        isAutoBackupEnabled = isAutoBackupEnabled,
                        autoBackupFrequencyDays = autoBackupFrequencyDays,
                        onAutoBackupEnabledChange = onAutoBackupEnabledChange,
                        onAutoBackupFrequencyChange = onAutoBackupFrequencyChange,
                        onPrepareForExternalActivity = onPrepareForExternalActivity,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }

                AppRoute.CategoryManagement -> {
                    CategoryManagementScreen(
                        isAdsEnabled = isAdsEnabled,
                        onCreateCustomCategory = onCreateCustomCategory,
                        onCreateCustomPaymentType = onCreateCustomPaymentType,
                        onDeleteCustomCategory = onDeleteCustomCategory,
                        onDeleteCustomPaymentType = onDeleteCustomPaymentType,
                        onAddCategoryClick = { targetTab ->
                            addingCategoryTargetTab = targetTab
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.AddCategory)
                        },
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }

                AppRoute.AddCategory -> {
                    val addCategoryViewModel: com.mknlabs.expensetracker.feature.settings.ui.AddCategoryViewModel = hiltViewModel()
                    androidx.compose.runtime.LaunchedEffect(addingCategoryTargetTab) {
                        addCategoryViewModel.setTargetTab(addingCategoryTargetTab)
                    }
                    AddCategoryScreen(
                        viewModel = addCategoryViewModel,
                        existingCategories = categories,
                        existingPaymentMethods = paymentMethods,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.CategoryManagement)
                        },
                        onCategoryCreated = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.CategoryManagement)
                        }
                    )
                }

                AppRoute.TransactionCardCustomize -> {
                    TransactionCardCustomizeScreen(
                        isAdsEnabled = isAdsEnabled,
                        isProUser = isProUser,
                        settings = transactionCardCustomizationSettings,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = selectedDateFormatPattern,
                        timeFormat = selectedTimeFormat,
                        onSettingsChange = onTransactionCardCustomizationSettingsChange,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }

                AppRoute.Profile -> {
                    ProfileScreen(
                        isAdsEnabled = isAdsEnabled,
                        userProfile = userProfile,
                        onSaveClick = { updatedProfile ->
                            onUserProfileChange(updatedProfile)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(profileOriginRoute)
                        },
                        onPrepareForExternalActivity = onPrepareForExternalActivity,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(profileOriginRoute)
                        },
                        onEmailUpdateSuccess = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(profileOriginRoute)
                            showEmailUpdateSuccessDialog = true
                        }
                    )
                }

                AppRoute.AddTransaction -> {
                    val mainViewModel: com.mknlabs.expensetracker.core.ui.MainViewModel = hiltViewModel()
                    val favorites by mainViewModel.favorites.collectAsStateWithLifecycle()
                    val favoritesContext = LocalContext.current
                    
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        mainViewModel.uiEvent.collect { event ->
                            if (event is com.mknlabs.expensetracker.core.ui.MainUiEvent.TransactionOperationCompleted) {
                                exitAddTransactionScreen(previousRoute)
                            }
                        }
                    }
                    
                    AddTransactionScreen(
                        currencyId = selectedCurrencyId,
                        transactions = transactions,
                        availableCategories = categories.filter { !it.isDeleted },
                        availablePaymentMethods = paymentMethods.filter { !it.isDeleted },
                        existingTransaction = selectedTransaction,
                        existingRecurringRule = selectedRecurringRule,
                        activeRecurringRuleCount = recurringRules.count { !it.isDeleted },
                        allRecurringRules = recurringRules,
                        initialAmountInput = addTransactionDraftAmount,
                        initialNote = addTransactionDraftNote,
                        initialCategoryId = addTransactionDraftCategoryId,
                        initialTransactionTypeId = addTransactionDraftTypeId,
                        autoStartVoice = addTransactionDraftAutoStartVoice,
                        onVoiceAutoStarted = onVoiceAutoStarted,
                        onBackClick = {
                            exitAddTransactionScreen(previousRoute)
                        },
                        onDeleteClick = {
                            val transactionToDelete = selectedTransaction
                            if (transactionToDelete == null) {
                                exitAddTransactionScreen(previousRoute)
                            } else {
                                onDeleteTransaction(transactionToDelete.id)
                            }
                        },
                        onCalculatorClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.ItemizedCalculator)
                        },
                        onAmountInputChange = onAddTransactionDraftAmountChange,
                        onNoteChange = onAddTransactionDraftNoteChange,
                        favorites = favorites,
                        onRemoveFavorite = { id ->
                            mainViewModel.removeFavorite(id)
                            Toast.makeText(
                                favoritesContext,
                                favoritesContext.getString(R.string.msg_favorite_removed),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onSaveExistingAsFavorite = { transaction ->
                            mainViewModel.saveAsFavorite(transaction)
                            Toast.makeText(
                                favoritesContext,
                                favoritesContext.getString(R.string.msg_favorite_added),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onSaveClick = { draftTransaction, recurringDraft ->
                            val isEdit = selectedTransaction != null
                            // Only a draft that came from the inbox carries a detection.
                            val detectionId = if (isEdit) null else smsInboxDraftDetectionId
                            // Linking that detection to this save needs the transaction's
                            // id at the moment it is written, and the repository would
                            // otherwise invent one out of the caller's reach — so it is
                            // settled here, with the same expression the repository uses.
                            val transactionToSave = when {
                                isEdit -> draftTransaction.copy(id = selectedTransaction.id)
                                detectionId != null && draftTransaction.id.isBlank() ->
                                    draftTransaction.copy(id = UUID.randomUUID().toString())
                                else -> draftTransaction
                            }
                            onSaveTransaction(
                                transactionToSave,
                                recurringDraft,
                                selectedRecurringRule
                            )
                            // Add was pressed, so the detection is now filed: the inbox
                            // drops its card and the notification goes with it. Backing out
                            // of this screen instead leaves the detection exactly as it was.
                            if (detectionId != null) {
                                onSmsDetectionSaved(detectionId, transactionToSave.id)
                            }
                        }
                    )
                }

                AppRoute.ItemizedCalculator -> {
                    val calculatorViewModel: ItemizedCalculatorViewModel = hiltViewModel()
                    ItemizedCalculatorScreen(
                        viewModel = calculatorViewModel,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = selectedDateFormatPattern,
                        timeFormat = selectedTimeFormat,
                        initialNote = addTransactionDraftNote ?: selectedTransaction?.note,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.AddTransaction)
                        },
                        onApplyToNoteClick = { finalAmount, finalNote ->
                            onAddTransactionDraftAmountChange(finalAmount)
                            onAddTransactionDraftNoteChange(finalNote)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.AddTransaction)
                        }
                    )
                }

                AppRoute.Goals -> {
                    GoalsScreen(
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        onBackClick = {
                            val backRoute = resolveBackNavigationRoute(AppRoute.Goals, profileOriginRoute, previousRoute) ?: AppRoute.Home
                            onBottomBarVisibilityChange(false)
                            onRouteChange(backRoute)
                        }
                    )
                }

                AppRoute.MembershipDetails -> {
                    val isAnonymousUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.isAnonymous ?: true
                    MembershipDetailsScreen(
                        userTier = userTier,
                        proExpiryTimestamp = userProfile.proExpiryTimestamp,
                        isAnonymous = isAnonymousUser,
                        isSubscription = userProfile.isSubscription,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }
            }
            if (showEmailUpdateSuccessDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showEmailUpdateSuccessDialog = false
                    },
                    title = { Text(stringResource(id = R.string.msg_email_updated_success)) },
                    text = { Text(stringResource(id = R.string.msg_email_updated_success_desc)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showEmailUpdateSuccessDialog = false
                            onDirectSignOut()
                        }) {
                            Text(stringResource(id = R.string.label_ok))
                        }
                    }
                )
            }
        }
    }
}
}
