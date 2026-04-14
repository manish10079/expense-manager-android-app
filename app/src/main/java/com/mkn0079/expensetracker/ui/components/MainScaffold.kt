package com.mkn0079.expensetracker.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.RecurringTransactionDraft
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.ui.navigation.bottomTabRoutes
import com.mkn0079.expensetracker.ui.navigation.resolveBackNavigationRoute
import com.mkn0079.expensetracker.ui.navigation.screenTransition
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
import com.mkn0079.expensetracker.ui.viewmodels.AnalyticsViewModel
import com.mkn0079.expensetracker.ui.viewmodels.BudgetViewModel
import com.mkn0079.expensetracker.ui.viewmodels.CalendarViewModel
import com.mkn0079.expensetracker.ui.viewmodels.SettingsViewModel
import com.mkn0079.expensetracker.ui.viewmodels.TransactionsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScaffold(
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

        AnimatedContent(
            targetState = currentRoute,
            transitionSpec = {
                val fromRoute = initialState
                val toRoute = targetState
                screenTransition(fromRoute ?: "", toRoute)
            },
            label = "main_navigation",
            modifier = Modifier.fillMaxSize()
        ) { route ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (route) {
                    "home" -> {
                        HomeScreen(
                            userProfile = userProfile,
                            currencyId = selectedCurrencyId,
                            timeFormat = selectedTimeFormat,
                            transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                            onTransactionClick = { transaction ->
                                onSelectedTransactionChange(transaction)
                                onBottomBarVisibilityChange(false)
                                onRouteChange("add_transaction")
                            },
                            onViewAllClick = {
                                onBottomBarVisibilityChange(false)
                                onRouteChange("transactions")
                            },
                            onProfileClick = {
                                onProfileOriginRouteChange("home")
                                onBottomBarVisibilityChange(false)
                                onRouteChange("profile")
                            },
                            onSettingsClick = {
                                onBottomBarVisibilityChange(false)
                                onRouteChange("settings")
                            }
                        )
                    }
                    "analytics" -> {
                        AnalyticsScreen(
                            currencyId = selectedCurrencyId,
                            transactions = transactions,
                            categories = categories
                        )
                    }
                    "budget" -> {
                        BudgetScreen(
                            currencyId = selectedCurrencyId,
                            transactions = transactions,
                            availableCategories = categories,
                            recurringRules = recurringRules,
                            onDeleteRecurring = onDeleteRecurring,
                            onRecurringEnabledChange = onRecurringEnabledChange
                        )
                    }
                    "calendar" -> {
                        CalendarScreen(
                            transactions = transactions,
                            currencyId = selectedCurrencyId,
                            dateFormatPattern = selectedDateFormatPattern,
                            timeFormat = selectedTimeFormat,
                            transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                            onTransactionClick = { transaction ->
                                onSelectedTransactionChange(transaction)
                                onBottomBarVisibilityChange(false)
                                onRouteChange("add_transaction")
                            }
                        )
                    }
                    "transactions" -> {
                        TransactionScreen(
                            currencyId = selectedCurrencyId,
                            dateFormatPattern = selectedDateFormatPattern,
                            timeFormat = selectedTimeFormat,
                            transactions = transactions,
                            transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                            onAddTransactionClick = {
                                onBottomBarVisibilityChange(false)
                                onRouteChange("add_transaction")
                            },
                            onTransactionClick = { transaction ->
                                onSelectedTransactionChange(transaction)
                                onBottomBarVisibilityChange(false)
                                onRouteChange("add_transaction")
                            }
                        )
                    }
                    "settings" -> {
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
                            onTransactionCardCustomizeClick = {
                                onBottomBarVisibilityChange(false)
                                onRouteChange("transaction_card_customize")
                            },
                            onBackClick = {
                                onBottomBarVisibilityChange(false)
                                onRouteChange("home")
                            }
                        )
                    }
                    "about" -> {
                        AboutScreen(
                            onBackClick = {
                                onBottomBarVisibilityChange(false)
                                onRouteChange("settings")
                            }
                        )
                    }
                    "notification_settings" -> {
                        NotificationSettingsScreen(
                            isDailyReminderEnabled = isDailyReminderEnabled,
                            isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                            isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                            onDailyReminderChange = onDailyReminderChange,
                            onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                            onMissedEntryReminderChange = onMissedEntryReminderChange,
                            onBackClick = {
                                onBottomBarVisibilityChange(false)
                                onRouteChange("settings")
                            }
                        )
                    }
                    "preferences" -> {
                        PreferencesScreen(
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
                                onBottomBarVisibilityChange(false)
                                onRouteChange("settings")
                            }
                        )
                    }
                    "security_privacy" -> {
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
                                onRouteChange("settings")
                            }
                        )
                    }
                    "data_management" -> {
                        DataManagementScreen(
                            transactionCount = transactionCount,
                            onLegacyImportFileSelected = onLegacyImportFileSelected,
                            onDeleteAllTransactionsClick = onDeleteAllTransactionsClick,
                            onPrepareForExternalActivity = onPrepareForExternalActivity,
                            onBackClick = {
                                onBottomBarVisibilityChange(false)
                                onRouteChange("settings")
                            }
                        )
                    }
                    "category_management" -> {
                        CategoryManagementScreen(
                            customCategories = categories.filter { !it.isSystem },
                            customPaymentTypes = paymentMethods.filter { !it.isSystem },
                            onCreateCustomCategory = onCreateCustomCategory,
                            onCreateCustomPaymentType = onCreateCustomPaymentType,
                            onDeleteCustomCategory = onDeleteCustomCategory,
                            onDeleteCustomPaymentType = onDeleteCustomPaymentType,
                            onBackClick = {
                                onBottomBarVisibilityChange(false)
                                onRouteChange("preferences")
                            }
                        )
                    }
                    "transaction_card_customize" -> {
                        TransactionCardCustomizeScreen(
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
                    }
                    "profile" -> {
                        ProfileScreen(
                            userProfile = userProfile,
                            dateFormatPattern = selectedDateFormatPattern,
                            onSaveClick = onUserProfileChange,
                            onPrepareForExternalActivity = onPrepareForExternalActivity,
                            onBackClick = {
                                onBottomBarVisibilityChange(false)
                                onRouteChange(profileOriginRoute)
                            }
                        )
                    }
                    "add_transaction" -> {
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
                                onRouteChange("itemized_calculator")
                            },
                            onAmountInputChange = { amount -> onAddTransactionDraftAmountChange(amount) },
                            onNoteChange = { note -> onAddTransactionDraftNoteChange(note) },
                            onSaveClick = { draftTransaction, recurringDraft ->
                                val transactionToSave = if (selectedTransaction != null) {
                                    draftTransaction.copy(id = selectedTransaction.id)
                                } else {
                                    draftTransaction
                                }
                                onSaveTransaction(transactionToSave, recurringDraft, selectedRecurringRule)
                                exitAddTransactionScreen(previousRoute)
                            }
                        )
                    }
                    "itemized_calculator" -> {
                        ItemizedCalculatorScreen(
                            onBackClick = {
                                onBottomBarVisibilityChange(false)
                                onRouteChange("add_transaction")
                            },
                            onApplyToNoteClick = { finalAmount, finalNote ->
                                onAddTransactionDraftAmountChange(finalAmount)
                                onAddTransactionDraftNoteChange(finalNote)
                                onBottomBarVisibilityChange(false)
                                onRouteChange("add_transaction")
                            }
                        )
                    }
                }
            }
        }

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
                    onRouteChange("add_transaction")
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
    selectedDateFormatPattern: String,
    selectedTimeFormat: String,
    transactionCount: Int,
    autoLockDurationMinutes: Int,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings
) {
    val transactionsViewModel: TransactionsViewModel = viewModel()
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val budgetViewModel: BudgetViewModel = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    LaunchedEffect(
        transactions,
        categories,
        recurringRules,
        selectedCurrencyId,
        selectedDateFormatPattern,
        selectedTimeFormat,
        transactionCount,
        autoLockDurationMinutes,
        transactionCardCustomizationSettings
    ) {
        analyticsViewModel.updateInputs(transactions, categories, selectedCurrencyId)
        budgetViewModel.updateInputs(transactions, categories, selectedCurrencyId, recurringRules)
        calendarViewModel.updateInputs(
            transactions,
            selectedCurrencyId,
            selectedDateFormatPattern,
            selectedTimeFormat,
            transactionCardCustomizationSettings
        )
        transactionsViewModel.updateInputs(
            transactions,
            selectedCurrencyId,
            selectedDateFormatPattern,
            selectedTimeFormat,
            transactionCardCustomizationSettings
        )
        settingsViewModel.updateInputs(
            selectedCurrencyId,
            selectedDateFormatPattern,
            selectedTimeFormat,
            autoLockDurationMinutes,
            transactionCount
        )
    }
}
