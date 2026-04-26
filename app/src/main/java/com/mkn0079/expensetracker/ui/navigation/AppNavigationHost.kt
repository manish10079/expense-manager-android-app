package com.mkn0079.expensetracker.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.RecurringTransactionDraft
import com.mkn0079.expensetracker.models.RecurringFrequency
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.ui.screens.AboutScreen
import com.mkn0079.expensetracker.ui.screens.AddTransactionScreen
import com.mkn0079.expensetracker.ui.screens.AnalyticsScreen
import com.mkn0079.expensetracker.ui.screens.BudgetScreen
import com.mkn0079.expensetracker.ui.screens.CalendarScreen
import com.mkn0079.expensetracker.ui.screens.CategoryManagementScreen
import com.mkn0079.expensetracker.ui.screens.DataManagementScreen
import com.mkn0079.expensetracker.ui.screens.HomeScreen
import com.mkn0079.expensetracker.ui.screens.ItemizedCalculatorScreen
import com.mkn0079.expensetracker.ui.screens.NotificationSettingsScreen
import com.mkn0079.expensetracker.ui.screens.PreferencesScreen
import com.mkn0079.expensetracker.ui.screens.ProfileScreen
import com.mkn0079.expensetracker.ui.screens.SecurityPrivacyScreen
import com.mkn0079.expensetracker.ui.screens.SettingsScreen
import com.mkn0079.expensetracker.ui.screens.TransactionCardCustomizeScreen
import com.mkn0079.expensetracker.ui.screens.TransactionScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.mkn0079.expensetracker.ui.viewmodels.ItemizedCalculatorViewModel

@Composable
fun AppNavigationHost(
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
    selectedCurrencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
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
    onBlurInRecentsChange: (Boolean) -> Unit,
    onScreenshotProtectionChange: (Boolean) -> Unit,
    onAutoLockDurationChange: (Int) -> Unit,
    onAppLockToggleChange: (Boolean) -> Unit,
    onAutoBackupEnabledChange: (Boolean) -> Unit,
    onAutoBackupFrequencyChange: (Int) -> Unit,
    onPrepareForExternalActivity: () -> Unit
) {
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
        Box(modifier = Modifier.fillMaxSize()) {
            when (route) {
                AppRoute.Home -> {
                    HomeScreen(
                        userProfile = userProfile,
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        timeFormat = selectedTimeFormat,
                        categories = categories,
                        transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                        onTransactionClick = { transaction ->
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
                        }
                    )
                }

                AppRoute.Analytics -> {
                    AnalyticsScreen(
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
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
                    BudgetScreen(
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
                        onTransactionClick = { transaction ->
                            onSelectedTransactionChange(transaction)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.AddTransaction)
                        }
                    )
                }

                AppRoute.Transactions -> {
                    TransactionScreen(
                        currencyId = selectedCurrencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = selectedDateFormatPattern,
                        timeFormat = selectedTimeFormat,
                        transactions = transactions,
                        categories = categories,
                        transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Home)
                        },
                        onAddTransactionClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.AddTransaction)
                        },
                        onTransactionClick = { transaction ->
                            onSelectedTransactionChange(transaction)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.AddTransaction)
                        }
                    )
                }

                AppRoute.Settings -> {
                    SettingsScreen(
                        userProfile = userProfile,
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
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Home)
                        }
                    )
                }

                AppRoute.About -> {
                    AboutScreen(
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }

                AppRoute.NotificationSettings -> {
                    NotificationSettingsScreen(
                        isDailyReminderEnabled = isDailyReminderEnabled,
                        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                        onDailyReminderChange = onDailyReminderChange,
                        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                        onMissedEntryReminderChange = onMissedEntryReminderChange,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }

                AppRoute.Preferences -> {
                    PreferencesScreen(
                        onManageCategoryClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.CategoryManagement)
                        },
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }

                AppRoute.SecurityPrivacy -> {
                    SecurityPrivacyScreen(
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
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Settings)
                        }
                    )
                }

                AppRoute.DataManagement -> {
                    DataManagementScreen(
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
                        customCategories = categories.filter { !it.isSystem },
                        customPaymentTypes = paymentMethods.filter { !it.isSystem },
                        onCreateCustomCategory = onCreateCustomCategory,
                        onCreateCustomPaymentType = onCreateCustomPaymentType,
                        onDeleteCustomCategory = onDeleteCustomCategory,
                        onDeleteCustomPaymentType = onDeleteCustomPaymentType,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(AppRoute.Preferences)
                        }
                    )
                }

                AppRoute.TransactionCardCustomize -> {
                    TransactionCardCustomizeScreen(
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
                        userProfile = userProfile,
                        dateFormatPattern = selectedDateFormatPattern,
                        onSaveClick = { updatedProfile ->
                            onUserProfileChange(updatedProfile)
                            onBottomBarVisibilityChange(false)
                            onRouteChange(profileOriginRoute)
                        },
                        onPrepareForExternalActivity = onPrepareForExternalActivity,
                        onBackClick = {
                            onBottomBarVisibilityChange(false)
                            onRouteChange(profileOriginRoute)
                        }
                    )
                }

                AppRoute.AddTransaction -> {
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
                                exitAddTransactionScreen(previousRoute)
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
                            exitAddTransactionScreen(previousRoute)
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
            }
        }
    }
}
