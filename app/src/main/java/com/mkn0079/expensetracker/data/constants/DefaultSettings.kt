package com.mkn0079.expensetracker.data.constants

import com.mkn0079.expensetracker.models.AppSettings
import com.mkn0079.expensetracker.models.AppThemeMode
import com.mkn0079.expensetracker.models.CurrencyGroupingStyle
import com.mkn0079.expensetracker.models.SortType
import com.mkn0079.expensetracker.utils.getDefaultOrder

const val DEFAULT_CURRENCY_ID = 1
val DEFAULT_CURRENCY_GROUPING_STYLE = CurrencyGroupingStyle.INDIAN
const val DEFAULT_CURRENCY_DECIMAL_PLACES = 2
const val DEFAULT_DATE_FORMAT_PATTERN = "dd/MM/yyyy"
const val DEFAULT_TIME_FORMAT = "12-hour"
const val DEFAULT_SORT_BY = "Date"
val DEFAULT_SORT_ORDER: SortType = getDefaultOrder(DEFAULT_SORT_BY)
const val DEFAULT_TRANSACTION_TYPE_ID = 2
const val DEFAULT_TRANSACTION_TYPE_FILTER_ID = 2
const val DEFAULT_PAYMENT_TYPE_ID = 1
const val DEFAULT_LANGUAGE_CODE = "en"
const val DEFAULT_NOTIFICATIONS_ENABLED = true
const val DEFAULT_BUDGET_LIMIT_ALERTS_ENABLED = false
const val DEFAULT_MISSED_ENTRY_REMINDER_ENABLED = false
const val DEFAULT_APP_LOCK_ENABLED = false
const val DEFAULT_BIOMETRIC_LOCK_ENABLED = false
const val DEFAULT_SCRAMBLED_PIN_KEYPAD_ENABLED = false
const val DEFAULT_BLUR_IN_RECENTS_ENABLED = false
const val DEFAULT_SCREENSHOT_PROTECTION_ENABLED = false
const val DEFAULT_APP_LOCK_TIMEOUT_MINUTES = 0
const val DEFAULT_SHOW_ONBOARDING_SCREEN = true
const val DEFAULT_SHOW_SPLASH_SCREEN = true
val DEFAULT_THEME_MODE = AppThemeMode.SYSTEM
const val DEFAULT_TRANSACTION_CARD_SHOW_INCOME_EXPENSE_LABELS = false
const val DEFAULT_TRANSACTION_CARD_SHOW_TRANSACTION_DATE = true
const val DEFAULT_TRANSACTION_CARD_SHOW_PAYMENT_METHOD = false
const val DEFAULT_TRANSACTION_CARD_SHOW_TRANSACTION_TIME = true
const val DEFAULT_TRANSACTION_CARD_SHOW_CATEGORY_ICON = true
const val DEFAULT_TRANSACTION_CARD_SHOW_CATEGORY_LABEL = true
const val DEFAULT_TRANSACTION_CARD_SHOW_DATE_SEPARATORS = false
const val DEFAULT_INSTALL_DATE_MILLIS = 0L
const val DEFAULT_AUTO_BACKUP_ENABLED = false
const val DEFAULT_AUTO_BACKUP_FREQUENCY_DAYS = 7
const val DEFAULT_LAST_AUTO_BACKUP_TIME_MILLIS = 0L
const val DEFAULT_LAST_SYNC_TIME_MILLIS = 0L
val DEFAULT_USER_TIER = com.mkn0079.expensetracker.models.UserTier.FREE

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
    themeMode = DEFAULT_THEME_MODE,
    transactionCardShowIncomeExpenseLabels = DEFAULT_TRANSACTION_CARD_SHOW_INCOME_EXPENSE_LABELS,
    transactionCardShowTransactionDate = DEFAULT_TRANSACTION_CARD_SHOW_TRANSACTION_DATE,
    transactionCardShowPaymentMethod = DEFAULT_TRANSACTION_CARD_SHOW_PAYMENT_METHOD,
    transactionCardShowTransactionTime = DEFAULT_TRANSACTION_CARD_SHOW_TRANSACTION_TIME,
    transactionCardShowCategoryIcon = DEFAULT_TRANSACTION_CARD_SHOW_CATEGORY_ICON,
    transactionCardShowCategoryLabel = DEFAULT_TRANSACTION_CARD_SHOW_CATEGORY_LABEL,
    transactionCardShowDateSeparators = DEFAULT_TRANSACTION_CARD_SHOW_DATE_SEPARATORS,
    installDateMillis = DEFAULT_INSTALL_DATE_MILLIS,
    isAutoBackupEnabled = DEFAULT_AUTO_BACKUP_ENABLED,
    autoBackupFrequencyDays = DEFAULT_AUTO_BACKUP_FREQUENCY_DAYS,
    lastAutoBackupTimeMillis = DEFAULT_LAST_AUTO_BACKUP_TIME_MILLIS,
    lastSyncTimeMillis = DEFAULT_LAST_SYNC_TIME_MILLIS,
    userTier = DEFAULT_USER_TIER
)
