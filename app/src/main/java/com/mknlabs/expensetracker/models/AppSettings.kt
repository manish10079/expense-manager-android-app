package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable

@Immutable
data class AppSettings(
    val currencyId: Int,
    val currencyGroupingStyle: CurrencyGroupingStyle,
    val currencyDecimalPlaces: Int,
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
    val scrambledPinKeypadEnabled: Boolean,
    val blurInRecentsEnabled: Boolean,
    val screenshotProtectionEnabled: Boolean,
    val appLockTimeoutMinutes: Int,
    val showOnboardingScreen: Boolean,
    val showSplashScreen: Boolean,
    val smartSmsPrompted: Boolean,
    val smsPermissionCardDismissed: Boolean = false,
    val smsMiuiSetupAcknowledged: Boolean = false,
    val themeMode: AppThemeMode,
    val transactionCardShowIncomeExpenseLabels: Boolean,
    val transactionCardShowTransactionDate: Boolean,
    val transactionCardShowPaymentMethod: Boolean,
    val transactionCardShowTransactionTime: Boolean,
    val transactionCardShowCategoryIcon: Boolean,
    val transactionCardShowCategoryLabel: Boolean,
    val transactionCardShowDateSeparators: Boolean,
    val transactionCardShowListSummaries: Boolean,
    val installDateMillis: Long,
    val isAutoBackupEnabled: Boolean,
    val autoBackupFrequencyDays: Int,
    val lastAutoBackupTimeMillis: Long,
    val lastSyncTimeMillis: Long,
    val isCloudSyncEnabled: Boolean,
    val pendingAuthEmail: String?,
    val setupDismissedUntilMillis: Long,
    val userTier: UserTier,
    val goalRemindersEnabled: Boolean = true,
    val reminderMorningStartHour: Int = 8,
    val reminderMorningEndHour: Int = 13,
    val reminderEveningStartHour: Int = 17,
    val reminderEveningEndHour: Int = 22,
    // Notification spec categories (default ON for every tier so users opt OUT):
    // 3. Large Transaction Alerts (Free)
    val largeTransactionAlertsEnabled: Boolean = true,
    // 4. Weekly Summary (Free)
    val weeklySummaryEnabled: Boolean = true,
    // 5. Financial Insights (Premium)
    val financialInsightsEnabled: Boolean = true,
    // 6. Savings Goals (Premium)
    val savingsGoalsEnabled: Boolean = true,
    // 7. Bill & Subscription Reminders (Premium)
    val billRemindersEnabled: Boolean = true,
    // 8. Cloud & Security Alerts (Premium)
    val cloudSecurityEnabled: Boolean = true,
    // "Large" expense threshold in minor units (spec default ₹5,000 = 500000).
    val largeTransactionThresholdMinor: Long = 500000L,
    // Weekly summary delivery time as millis-of-day (spec default 8:00 PM =
    // 20h * 60m * 60s * 1000ms). Always Sunday, per the spec.
    val weeklySummaryTimeMillis: Long = 72000000L,
    val deviceIntegrityNoticeAcknowledged: Boolean = false,
    val muteRecurringDialogDismissed: Boolean = false
)
