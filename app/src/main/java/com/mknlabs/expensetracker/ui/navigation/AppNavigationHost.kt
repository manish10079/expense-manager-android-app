package com.mknlabs.expensetracker.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mknlabs.expensetracker.ui.models.CategoryManagementTab
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.RecurringTransactionDraft
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.ui.screens.AboutScreen
import com.mknlabs.expensetracker.ui.screens.FeedbackRoute
import com.mknlabs.expensetracker.ui.screens.AddTransactionScreen
import com.mknlabs.expensetracker.ui.screens.AnalyticsScreen
import com.mknlabs.expensetracker.ui.screens.BudgetAndRecurringScreen
import com.mknlabs.expensetracker.ui.screens.AddCategoryScreen
import com.mknlabs.expensetracker.ui.screens.CalendarScreen
import com.mknlabs.expensetracker.ui.screens.CategoryManagementScreen
import com.mknlabs.expensetracker.ui.screens.DataManagementScreen
import com.mknlabs.expensetracker.ui.screens.GoalsScreen
import com.mknlabs.expensetracker.ui.screens.HomeScreen
import com.mknlabs.expensetracker.ui.screens.ItemizedCalculatorScreen
import com.mknlabs.expensetracker.ui.screens.NotificationSettingsScreen
import com.mknlabs.expensetracker.ui.screens.PreferencesScreen
import com.mknlabs.expensetracker.ui.screens.ProfileScreen
import com.mknlabs.expensetracker.ui.screens.ConnectedDevicesScreen
import com.mknlabs.expensetracker.ui.screens.SecurityPrivacyScreen
import com.mknlabs.expensetracker.ui.screens.SettingsScreen
import com.mknlabs.expensetracker.ui.screens.TransactionCardCustomizeScreen
import com.mknlabs.expensetracker.ui.screens.TransactionScreen
import com.mknlabs.expensetracker.ui.screens.MembershipDetailsScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.viewmodels.ItemizedCalculatorViewModel

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
    hasAppLockPin: Boolean,
    isBiometricEnabled: Boolean,
    isScrambledPinKeypadEnabled: Boolean,
    isBlurInRecentsEnabled: Boolean,
    isScreenshotProtectionEnabled: Boolean,
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    isGoalRemindersEnabled: Boolean,
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
    onDuplicateTransaction: (Transaction) -> Unit,
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
    onGoalRemindersChange: (Boolean) -> Unit,
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
    onShowUpgradeSheet: () -> Unit
) {
    var addingCategoryTargetTab by remember { mutableStateOf(CategoryManagementTab.Expense) }
    
    val exitAddTransactionScreen: (AppRoute) -> Unit = { destinationRoute ->
        onBottomBarVisibilityChange(false)
        onRouteChange(destinationRoute)
    }
    val selectedRecurringRule = selectedTransaction?.let { transaction ->
        recurringRules.firstOrNull { it.transactionId == transaction.id }
    }

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
                        onTodaySpendingClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Calendar)
                        },
                        onGoalsClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Goals)
                        }
                    )
                }

                AppRoute.Analytics -> {
                    AnalyticsScreen(
                        isAdsEnabled = isAdsEnabled,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = selectedDateFormatPattern,
                        transactions = transactions,
                        categories = categories,
                        paymentMethods = paymentMethods,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Home)
                        }
                    )
                }

                AppRoute.Budget -> {
                    BudgetAndRecurringScreen(
                        isAdsEnabled = isAdsEnabled,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        transactions = transactions,
                        availableCategories = categories,
                        recurringRules = recurringRules,
                        onDeleteRecurring = onDeleteRecurring,
                        onRecurringEnabledChange = onRecurringEnabledChange,
                        onUpdateRecurringRule = onUpdateRecurringRule,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Home)
                        }
                    )
                }

                AppRoute.Calendar -> {
                    CalendarScreen(
                        isAdsEnabled = isAdsEnabled,
                        transactions = transactions,
                        categories = categories,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = selectedDateFormatPattern,
                        timeFormat = selectedTimeFormat,
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
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = selectedDateFormatPattern,
                        timeFormat = selectedTimeFormat,
                        transactions = transactions,
                        categories = categories,
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
                        }
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
                        isDailyReminderEnabled = isDailyReminderEnabled,
                        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                        isGoalRemindersEnabled = isGoalRemindersEnabled,
                        onDailyReminderChange = onDailyReminderChange,
                        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                        onMissedEntryReminderChange = onMissedEntryReminderChange,
                        onGoalRemindersChange = onGoalRemindersChange,
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
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }

                AppRoute.CategoryManagement -> {
                    CategoryManagementScreen(
                        isAdsEnabled = isAdsEnabled,
                        customCategories = categories.filter { !it.isSystem },
                        customPaymentTypes = paymentMethods.filter { !it.isSystem },
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
                    val addCategoryViewModel: com.mknlabs.expensetracker.ui.viewmodels.AddCategoryViewModel = hiltViewModel()
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
                        dateFormatPattern = selectedDateFormatPattern,
                        onSaveClick = { updatedProfile ->
                            onUserProfileChange(updatedProfile)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(profileOriginRoute)
                        },
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(profileOriginRoute)
                        }
                    )
                }

                AppRoute.AddTransaction -> {
                    val mainViewModel: com.mknlabs.expensetracker.ui.viewmodels.MainViewModel = hiltViewModel()
                    
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        mainViewModel.uiEvent.collect { event ->
                            if (event is com.mknlabs.expensetracker.ui.viewmodels.MainUiEvent.TransactionOperationCompleted) {
                                exitAddTransactionScreen(previousRoute)
                            }
                        }
                    }
                    
                    AddTransactionScreen(
                        currencyId = selectedCurrencyId,
                        transactions = transactions,
                        availableCategories = categories,
                        availablePaymentMethods = paymentMethods,
                        existingTransaction = selectedTransaction,
                        existingRecurringRule = selectedRecurringRule,
                        initialAmountInput = addTransactionDraftAmount,
                        initialNote = addTransactionDraftNote,
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
                        onSaveClick = { draftTransaction, recurringDraft ->
                            val transactionToSave = if (selectedTransaction != null) {
                                draftTransaction.copy(id = selectedTransaction.id)
                            } else {
                                draftTransaction
                            }
                            onSaveTransaction(
                                transactionToSave,
                                recurringDraft,
                                selectedRecurringRule
                            )
                        }
                    )
                }

                AppRoute.ItemizedCalculator -> {
                    val calculatorViewModel: ItemizedCalculatorViewModel = hiltViewModel()
                    ItemizedCalculatorScreen(
                        viewModel = calculatorViewModel,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
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
        }
    }
}
}
