package com.mkn0079.expensetracker.models

import androidx.compose.runtime.Immutable

@Immutable
data class AppSettings(
    val currencyId: Int,
    val dateFormatPattern: String,
    val timeFormat: String,
    val sortBy: String,
    val sortOrder: SortType,
    val defaultTransactionTypeId: Int,
    val defaultTransactionTypeFilterId: Int,
    val defaultPaymentTypeId: Int,
    val languageCode: String,
    val notificationsEnabled: Boolean,
    val budgetLimitAlertsEnabled: Boolean,
    val missedEntryReminderEnabled: Boolean,
    val appLockEnabled: Boolean,
    val biometricLockEnabled: Boolean,
    val blurInRecentsEnabled: Boolean,
    val screenshotProtectionEnabled: Boolean,
    val appLockTimeoutMinutes: Int,
    val showOnboardingScreen: Boolean,
    val showSplashScreen: Boolean,
    val darkThemeEnabled: Boolean,
    val transactionCardShowIncomeExpenseLabels: Boolean,
    val transactionCardShowTransactionDate: Boolean,
    val transactionCardShowPaymentMethod: Boolean,
    val transactionCardShowTransactionTime: Boolean,
    val transactionCardShowCategoryIcon: Boolean,
    val transactionCardShowDateSeparators: Boolean
)
