package com.mknlabs.expensetracker.data.constants

import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.AppThemeMode
import com.mknlabs.expensetracker.models.CurrencyGroupingStyle
import com.mknlabs.expensetracker.models.SortType
import com.mknlabs.expensetracker.utils.getDefaultOrder

const val DEFAULT_CURRENCY_ID = 1
val DEFAULT_CURRENCY_GROUPING_STYLE = CurrencyGroupingStyle.INDIAN
const val DEFAULT_CURRENCY_DECIMAL_PLACES = 0
const val DEFAULT_DATE_FORMAT_PATTERN = "dd/MM/yyyy"
const val DOB_DATE_FORMAT_PATTERN = "dd MMM yyyy"
const val DEFAULT_TIME_FORMAT = "12-hour"
const val DEFAULT_SORT_BY = "Date"
val DEFAULT_SORT_ORDER: SortType = getDefaultOrder(DEFAULT_SORT_BY)
const val DEFAULT_TRANSACTION_TYPE_ID = 2
const val DEFAULT_TRANSACTION_TYPE_FILTER_ID = 2
const val DEFAULT_PAYMENT_TYPE_ID = 1
const val DEFAULT_LANGUAGE_CODE = "en"
const val DEFAULT_NOTIFICATIONS_ENABLED = true
// All notification toggles default ON for every tier (free + pro) so users opt OUT,
// not in. The per-user saved choice still wins once a user touches a toggle.
const val DEFAULT_BUDGET_LIMIT_ALERTS_ENABLED = true
const val DEFAULT_MISSED_ENTRY_REMINDER_ENABLED = true
const val DEFAULT_GOAL_REMINDERS_ENABLED = true
// Notification spec categories (default ON for every tier so users opt OUT,
// not in — same philosophy as the other toggles above).
const val DEFAULT_LARGE_TRANSACTION_ALERTS_ENABLED = true
const val DEFAULT_WEEKLY_SUMMARY_ENABLED = true
const val DEFAULT_FINANCIAL_INSIGHTS_ENABLED = true
const val DEFAULT_SAVINGS_GOALS_ENABLED = true
const val DEFAULT_BILL_REMINDERS_ENABLED = true
const val DEFAULT_CLOUD_SECURITY_ENABLED = true
// "Large" expense threshold in minor units — spec default ₹5,000 (= 500000).
const val DEFAULT_LARGE_TRANSACTION_THRESHOLD_MINOR = 500000L
// Weekly summary delivery time as millis-of-day — spec default 8:00 PM
// (= 20 * 60 * 60 * 1000). Delivery day is always Sunday.
const val DEFAULT_WEEKLY_SUMMARY_TIME_MILLIS = 20L * 60L * 60L * 1000L
// Daily-reminder windows (hour-of-day, 0-23). Morning 8-13, evening 17-22
// match the pre-existing hardcoded behavior.
const val DEFAULT_REMINDER_MORNING_START_HOUR = 8
const val DEFAULT_REMINDER_MORNING_END_HOUR = 13
const val DEFAULT_REMINDER_EVENING_START_HOUR = 17
const val DEFAULT_REMINDER_EVENING_END_HOUR = 22
const val DEFAULT_APP_LOCK_ENABLED = false
const val DEFAULT_BIOMETRIC_LOCK_ENABLED = false
const val DEFAULT_SCRAMBLED_PIN_KEYPAD_ENABLED = false
const val DEFAULT_BLUR_IN_RECENTS_ENABLED = false
const val DEFAULT_SCREENSHOT_PROTECTION_ENABLED = false
const val DEFAULT_APP_LOCK_TIMEOUT_MINUTES = 0
const val DEFAULT_SHOW_ONBOARDING_SCREEN = true
const val DEFAULT_SHOW_SPLASH_SCREEN = true
const val DEFAULT_SMART_SMS_PROMPTED = false
const val DEFAULT_SMS_PERMISSION_CARD_DISMISSED = false
const val DEFAULT_SMS_MIUI_SETUP_ACKNOWLEDGED = false
const val DEFAULT_DEVICE_INTEGRITY_NOTICE_ACKNOWLEDGED = false
val DEFAULT_THEME_MODE = AppThemeMode.DARK
const val DEFAULT_TRANSACTION_CARD_SHOW_INCOME_EXPENSE_LABELS = false
const val DEFAULT_TRANSACTION_CARD_SHOW_TRANSACTION_DATE = true
const val DEFAULT_TRANSACTION_CARD_SHOW_PAYMENT_METHOD = false
const val DEFAULT_TRANSACTION_CARD_SHOW_TRANSACTION_TIME = true
const val DEFAULT_TRANSACTION_CARD_SHOW_CATEGORY_ICON = true
const val DEFAULT_TRANSACTION_CARD_SHOW_CATEGORY_LABEL = true
const val DEFAULT_TRANSACTION_CARD_SHOW_DATE_SEPARATORS = false
const val DEFAULT_TRANSACTION_CARD_SHOW_TRANSACTION_LIST_SUMMARIES = true
const val DEFAULT_INSTALL_DATE_MILLIS = 0L
const val DEFAULT_AUTO_BACKUP_ENABLED = true
const val DEFAULT_AUTO_BACKUP_FREQUENCY_DAYS = 7
const val DEFAULT_LAST_AUTO_BACKUP_TIME_MILLIS = 0L
const val DEFAULT_LAST_SYNC_TIME_MILLIS = 0L
const val DEFAULT_CLOUD_SYNC_ENABLED = true
val DEFAULT_PENDING_AUTH_EMAIL: String? = null
const val DEFAULT_SETUP_DISMISSED_UNTIL_MILLIS = 0L
val DEFAULT_USER_TIER = com.mknlabs.expensetracker.models.UserTier.FREE

val defaultAppSettings = AppSettings(
    currencyId = DEFAULT_CURRENCY_ID,
    currencyGroupingStyle = DEFAULT_CURRENCY_GROUPING_STYLE,
    currencyDecimalPlaces = DEFAULT_CURRENCY_DECIMAL_PLACES,
    dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat = DEFAULT_TIME_FORMAT,
    sortBy = DEFAULT_SORT_BY,
    sortOrder = DEFAULT_SORT_ORDER,
    defaultTransactionTypeId = DEFAULT_TRANSACTION_TYPE_ID,
    defaultTransactionTypeFilterId = DEFAULT_TRANSACTION_TYPE_FILTER_ID,
    defaultPaymentTypeId = DEFAULT_PAYMENT_TYPE_ID,
    languageCode = DEFAULT_LANGUAGE_CODE,
    notificationsEnabled = DEFAULT_NOTIFICATIONS_ENABLED,
    budgetLimitAlertsEnabled = DEFAULT_BUDGET_LIMIT_ALERTS_ENABLED,
    missedEntryReminderEnabled = DEFAULT_MISSED_ENTRY_REMINDER_ENABLED,
    appLockEnabled = DEFAULT_APP_LOCK_ENABLED,
    biometricLockEnabled = DEFAULT_BIOMETRIC_LOCK_ENABLED,
    scrambledPinKeypadEnabled = DEFAULT_SCRAMBLED_PIN_KEYPAD_ENABLED,
    blurInRecentsEnabled = DEFAULT_BLUR_IN_RECENTS_ENABLED,
    screenshotProtectionEnabled = DEFAULT_SCREENSHOT_PROTECTION_ENABLED,
    appLockTimeoutMinutes = DEFAULT_APP_LOCK_TIMEOUT_MINUTES,
    showOnboardingScreen = DEFAULT_SHOW_ONBOARDING_SCREEN,
    showSplashScreen = DEFAULT_SHOW_SPLASH_SCREEN,
    smartSmsPrompted = DEFAULT_SMART_SMS_PROMPTED,
    smsPermissionCardDismissed = DEFAULT_SMS_PERMISSION_CARD_DISMISSED,
    smsMiuiSetupAcknowledged = DEFAULT_SMS_MIUI_SETUP_ACKNOWLEDGED,
    themeMode = DEFAULT_THEME_MODE,
    transactionCardShowIncomeExpenseLabels = DEFAULT_TRANSACTION_CARD_SHOW_INCOME_EXPENSE_LABELS,
    transactionCardShowTransactionDate = DEFAULT_TRANSACTION_CARD_SHOW_TRANSACTION_DATE,
    transactionCardShowPaymentMethod = DEFAULT_TRANSACTION_CARD_SHOW_PAYMENT_METHOD,
    transactionCardShowTransactionTime = DEFAULT_TRANSACTION_CARD_SHOW_TRANSACTION_TIME,
    transactionCardShowCategoryIcon = DEFAULT_TRANSACTION_CARD_SHOW_CATEGORY_ICON,
    transactionCardShowCategoryLabel = DEFAULT_TRANSACTION_CARD_SHOW_CATEGORY_LABEL,
    transactionCardShowDateSeparators = DEFAULT_TRANSACTION_CARD_SHOW_DATE_SEPARATORS,
    transactionCardShowListSummaries = DEFAULT_TRANSACTION_CARD_SHOW_TRANSACTION_LIST_SUMMARIES,
    installDateMillis = DEFAULT_INSTALL_DATE_MILLIS,
    isAutoBackupEnabled = DEFAULT_AUTO_BACKUP_ENABLED,
    autoBackupFrequencyDays = DEFAULT_AUTO_BACKUP_FREQUENCY_DAYS,
    lastAutoBackupTimeMillis = DEFAULT_LAST_AUTO_BACKUP_TIME_MILLIS,
    lastSyncTimeMillis = DEFAULT_LAST_SYNC_TIME_MILLIS,
    isCloudSyncEnabled = DEFAULT_CLOUD_SYNC_ENABLED,
    pendingAuthEmail = DEFAULT_PENDING_AUTH_EMAIL,
    setupDismissedUntilMillis = DEFAULT_SETUP_DISMISSED_UNTIL_MILLIS,
    userTier = DEFAULT_USER_TIER,
    goalRemindersEnabled = DEFAULT_GOAL_REMINDERS_ENABLED,
    reminderMorningStartHour = DEFAULT_REMINDER_MORNING_START_HOUR,
    reminderMorningEndHour = DEFAULT_REMINDER_MORNING_END_HOUR,
    reminderEveningStartHour = DEFAULT_REMINDER_EVENING_START_HOUR,
    reminderEveningEndHour = DEFAULT_REMINDER_EVENING_END_HOUR,
    largeTransactionAlertsEnabled = DEFAULT_LARGE_TRANSACTION_ALERTS_ENABLED,
    weeklySummaryEnabled = DEFAULT_WEEKLY_SUMMARY_ENABLED,
    financialInsightsEnabled = DEFAULT_FINANCIAL_INSIGHTS_ENABLED,
    savingsGoalsEnabled = DEFAULT_SAVINGS_GOALS_ENABLED,
    billRemindersEnabled = DEFAULT_BILL_REMINDERS_ENABLED,
    cloudSecurityEnabled = DEFAULT_CLOUD_SECURITY_ENABLED,
    largeTransactionThresholdMinor = DEFAULT_LARGE_TRANSACTION_THRESHOLD_MINOR,
    weeklySummaryTimeMillis = DEFAULT_WEEKLY_SUMMARY_TIME_MILLIS,
    deviceIntegrityNoticeAcknowledged = DEFAULT_DEVICE_INTEGRITY_NOTICE_ACKNOWLEDGED
)
