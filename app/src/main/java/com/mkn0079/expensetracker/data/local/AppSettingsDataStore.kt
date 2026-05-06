package com.mkn0079.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mkn0079.expensetracker.data.constants.defaultAppSettings
import com.mkn0079.expensetracker.models.AppSettings
import com.mkn0079.expensetracker.models.AppThemeMode
import com.mkn0079.expensetracker.models.CurrencyGroupingStyle
import com.mkn0079.expensetracker.models.SortType
import com.mkn0079.expensetracker.utils.ThemePreferenceSync
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val APP_SETTINGS_DATASTORE_NAME = "app_settings"

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = APP_SETTINGS_DATASTORE_NAME
)

object AppSettingsDataStore {

    const val DATA_STORE_NAME = APP_SETTINGS_DATASTORE_NAME

    private object Keys {
        val initialized = booleanPreferencesKey("settings_initialized")
        val currencyId = intPreferencesKey("currency_id")
        val currencyGroupingStyle = stringPreferencesKey("currency_grouping_style")
        val currencyDecimalPlaces = intPreferencesKey("currency_decimal_places")
        val dateFormatPattern = stringPreferencesKey("date_format_pattern")
        val timeFormat = stringPreferencesKey("time_format")
        val sortBy = stringPreferencesKey("sort_by")
        val sortOrder = stringPreferencesKey("sort_order")
        val defaultTransactionTypeId = intPreferencesKey("default_transaction_type_id")
        val defaultTransactionTypeFilterId = intPreferencesKey("default_transaction_type_filter_id")
        val defaultPaymentTypeId = intPreferencesKey("default_payment_type_id")
        val languageCode = stringPreferencesKey("language_code")
        val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val budgetLimitAlertsEnabled = booleanPreferencesKey("budget_limit_alerts_enabled")
        val missedEntryReminderEnabled = booleanPreferencesKey("missed_entry_reminder_enabled")
        val appLockEnabled = booleanPreferencesKey("app_lock_enabled")
        val biometricLockEnabled = booleanPreferencesKey("biometric_lock_enabled")
        val scrambledPinKeypadEnabled = booleanPreferencesKey("scrambled_pin_keypad_enabled")
        val blurInRecentsEnabled = booleanPreferencesKey("blur_in_recents_enabled")
        val screenshotProtectionEnabled = booleanPreferencesKey("screenshot_protection_enabled")
        val appLockTimeoutMinutes = intPreferencesKey("app_lock_timeout_minutes")
        val showOnboardingScreen = booleanPreferencesKey("show_onboarding_screen")
        val showSplashScreen = booleanPreferencesKey("show_splash_screen")
        val themeMode = stringPreferencesKey("theme_mode")
        val transactionCardShowIncomeExpenseLabels =
            booleanPreferencesKey("transaction_card_show_income_expense_labels")
        val transactionCardShowTransactionDate =
            booleanPreferencesKey("transaction_card_show_transaction_date")
        val transactionCardShowPaymentMethod =
            booleanPreferencesKey("transaction_card_show_payment_method")
        val transactionCardShowTransactionTime =
            booleanPreferencesKey("transaction_card_show_transaction_time")
        val transactionCardShowCategoryIcon =
            booleanPreferencesKey("transaction_card_show_category_icon")
        val transactionCardShowCategoryLabel =
            booleanPreferencesKey("transaction_card_show_category_label")
        val transactionCardShowDateSeparators =
            booleanPreferencesKey("transaction_card_show_date_separators")
        val installDateMillis = longPreferencesKey("install_date_millis")
        val isAutoBackupEnabled = booleanPreferencesKey("is_auto_backup_enabled")
        val autoBackupFrequencyDays = intPreferencesKey("auto_backup_frequency_days")
        val lastAutoBackupTimeMillis = longPreferencesKey("last_auto_backup_time_millis")
        val userTier = stringPreferencesKey("user_tier")
    }

    fun getAppSettingsFlow(context: Context): Flow<AppSettings> {
        return context.applicationContext.appSettingsDataStore.data
            .map { preferences -> preferences.toAppSettings() }
    }

    suspend fun initialize(context: Context) {
        context.applicationContext.appSettingsDataStore.edit { preferences ->
            if (preferences[Keys.initialized] == true) {
                return@edit
            }

            val appLockState = AppLockPreferences.getCachedState()
            val initialSettings = defaultAppSettings.copy(
                appLockEnabled = appLockState.isAppLockEnabled,
                biometricLockEnabled = appLockState.isBiometricEnabled,
                appLockTimeoutMinutes = appLockState.autoLockDurationMinutes
            )

            preferences.writeAppSettings(initialSettings)
            if (preferences[Keys.installDateMillis] == null || preferences[Keys.installDateMillis] == 0L) {
                preferences[Keys.installDateMillis] = System.currentTimeMillis()
            }
            preferences[Keys.initialized] = true
        }
    }

    suspend fun updateAppSettings(
        context: Context,
        transform: (AppSettings) -> AppSettings
    ) {
        context.applicationContext.appSettingsDataStore.edit { preferences ->
            val updatedSettings = transform(preferences.toAppSettings())
            
            // Task 3: Sync DataStore + SharedPreferences
            ThemePreferenceSync.setTheme(context, updatedSettings.themeMode.name)
            
            preferences.writeAppSettings(updatedSettings)
            preferences[Keys.initialized] = true
        }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        return AppSettings(
            currencyId = this[Keys.currencyId] ?: defaultAppSettings.currencyId,
            currencyGroupingStyle = this[Keys.currencyGroupingStyle]
                ?.let(::currencyGroupingStyleOrDefault)
                ?: defaultAppSettings.currencyGroupingStyle,
            currencyDecimalPlaces = (this[Keys.currencyDecimalPlaces]
                ?: defaultAppSettings.currencyDecimalPlaces)
                .coerceIn(0, 4),
            dateFormatPattern = this[Keys.dateFormatPattern] ?: defaultAppSettings.dateFormatPattern,
            timeFormat = this[Keys.timeFormat] ?: defaultAppSettings.timeFormat,
            sortBy = this[Keys.sortBy] ?: defaultAppSettings.sortBy,
            sortOrder = this[Keys.sortOrder]
                ?.let(::sortTypeOrDefault)
                ?: defaultAppSettings.sortOrder,
            defaultTransactionTypeId = this[Keys.defaultTransactionTypeId]
                ?: defaultAppSettings.defaultTransactionTypeId,
            defaultTransactionTypeFilterId = this[Keys.defaultTransactionTypeFilterId]
                ?: defaultAppSettings.defaultTransactionTypeFilterId,
            defaultPaymentTypeId = this[Keys.defaultPaymentTypeId]
                ?: defaultAppSettings.defaultPaymentTypeId,
            languageCode = this[Keys.languageCode] ?: defaultAppSettings.languageCode,
            notificationsEnabled = this[Keys.notificationsEnabled]
                ?: defaultAppSettings.notificationsEnabled,
            budgetLimitAlertsEnabled = this[Keys.budgetLimitAlertsEnabled]
                ?: defaultAppSettings.budgetLimitAlertsEnabled,
            missedEntryReminderEnabled = this[Keys.missedEntryReminderEnabled]
                ?: defaultAppSettings.missedEntryReminderEnabled,
            appLockEnabled = this[Keys.appLockEnabled] ?: defaultAppSettings.appLockEnabled,
            biometricLockEnabled = this[Keys.biometricLockEnabled]
                ?: defaultAppSettings.biometricLockEnabled,
            scrambledPinKeypadEnabled = this[Keys.scrambledPinKeypadEnabled]
                ?: defaultAppSettings.scrambledPinKeypadEnabled,
            blurInRecentsEnabled = this[Keys.blurInRecentsEnabled]
                ?: defaultAppSettings.blurInRecentsEnabled,
            screenshotProtectionEnabled = this[Keys.screenshotProtectionEnabled]
                ?: defaultAppSettings.screenshotProtectionEnabled,
            appLockTimeoutMinutes = this[Keys.appLockTimeoutMinutes]
                ?: defaultAppSettings.appLockTimeoutMinutes,
            showOnboardingScreen = this[Keys.showOnboardingScreen]
                ?: defaultAppSettings.showOnboardingScreen,
            showSplashScreen = this[Keys.showSplashScreen]
                ?: defaultAppSettings.showSplashScreen,
            themeMode = this[Keys.themeMode]
                ?.let(::appThemeModeOrDefault)
                ?: defaultAppSettings.themeMode,
            transactionCardShowIncomeExpenseLabels = this[Keys.transactionCardShowIncomeExpenseLabels]
                ?: defaultAppSettings.transactionCardShowIncomeExpenseLabels,
            transactionCardShowTransactionDate = this[Keys.transactionCardShowTransactionDate]
                ?: defaultAppSettings.transactionCardShowTransactionDate,
            transactionCardShowPaymentMethod = this[Keys.transactionCardShowPaymentMethod]
                ?: defaultAppSettings.transactionCardShowPaymentMethod,
            transactionCardShowTransactionTime = this[Keys.transactionCardShowTransactionTime]
                ?: defaultAppSettings.transactionCardShowTransactionTime,
            transactionCardShowCategoryIcon = this[Keys.transactionCardShowCategoryIcon]
                ?: defaultAppSettings.transactionCardShowCategoryIcon,
            transactionCardShowCategoryLabel = this[Keys.transactionCardShowCategoryLabel]
                ?: defaultAppSettings.transactionCardShowCategoryLabel,
            transactionCardShowDateSeparators = this[Keys.transactionCardShowDateSeparators]
                ?: defaultAppSettings.transactionCardShowDateSeparators,
            installDateMillis = this[Keys.installDateMillis] ?: defaultAppSettings.installDateMillis,
            isAutoBackupEnabled = this[Keys.isAutoBackupEnabled] ?: defaultAppSettings.isAutoBackupEnabled,
            autoBackupFrequencyDays = this[Keys.autoBackupFrequencyDays] ?: defaultAppSettings.autoBackupFrequencyDays,
            lastAutoBackupTimeMillis = this[Keys.lastAutoBackupTimeMillis] ?: defaultAppSettings.lastAutoBackupTimeMillis,
            userTier = this[Keys.userTier]?.let(::userTierOrDefault) ?: defaultAppSettings.userTier
        )
    }

    private fun MutablePreferences.writeAppSettings(settings: AppSettings) {
        this[Keys.currencyId] = settings.currencyId
        this[Keys.currencyGroupingStyle] = settings.currencyGroupingStyle.name
        this[Keys.currencyDecimalPlaces] = settings.currencyDecimalPlaces.coerceIn(0, 4)
        this[Keys.dateFormatPattern] = settings.dateFormatPattern
        this[Keys.timeFormat] = settings.timeFormat
        this[Keys.sortBy] = settings.sortBy
        this[Keys.sortOrder] = settings.sortOrder.name
        this[Keys.defaultTransactionTypeId] = settings.defaultTransactionTypeId
        this[Keys.defaultTransactionTypeFilterId] = settings.defaultTransactionTypeFilterId
        this[Keys.defaultPaymentTypeId] = settings.defaultPaymentTypeId
        this[Keys.languageCode] = settings.languageCode
        this[Keys.notificationsEnabled] = settings.notificationsEnabled
        this[Keys.budgetLimitAlertsEnabled] = settings.budgetLimitAlertsEnabled
        this[Keys.missedEntryReminderEnabled] = settings.missedEntryReminderEnabled
        this[Keys.appLockEnabled] = settings.appLockEnabled
        this[Keys.biometricLockEnabled] = settings.biometricLockEnabled
        this[Keys.scrambledPinKeypadEnabled] = settings.scrambledPinKeypadEnabled
        this[Keys.blurInRecentsEnabled] = settings.blurInRecentsEnabled
        this[Keys.screenshotProtectionEnabled] = settings.screenshotProtectionEnabled
        this[Keys.appLockTimeoutMinutes] = settings.appLockTimeoutMinutes
        this[Keys.showOnboardingScreen] = settings.showOnboardingScreen
        this[Keys.showSplashScreen] = settings.showSplashScreen
        this[Keys.themeMode] = settings.themeMode.name
        this[Keys.transactionCardShowIncomeExpenseLabels] =
            settings.transactionCardShowIncomeExpenseLabels
        this[Keys.transactionCardShowTransactionDate] =
            settings.transactionCardShowTransactionDate
        this[Keys.transactionCardShowPaymentMethod] =
            settings.transactionCardShowPaymentMethod
        this[Keys.transactionCardShowTransactionTime] =
            settings.transactionCardShowTransactionTime
        this[Keys.transactionCardShowCategoryIcon] =
            settings.transactionCardShowCategoryIcon
        this[Keys.transactionCardShowCategoryLabel] =
            settings.transactionCardShowCategoryLabel
        this[Keys.transactionCardShowDateSeparators] =
            settings.transactionCardShowDateSeparators
        this[Keys.installDateMillis] = settings.installDateMillis
        this[Keys.isAutoBackupEnabled] = settings.isAutoBackupEnabled
        this[Keys.autoBackupFrequencyDays] = settings.autoBackupFrequencyDays
        this[Keys.lastAutoBackupTimeMillis] = settings.lastAutoBackupTimeMillis
        this[Keys.userTier] = settings.userTier.name
    }

    private fun sortTypeOrDefault(value: String): SortType {
        return SortType.entries.firstOrNull { it.name == value } ?: defaultAppSettings.sortOrder
    }

    private fun currencyGroupingStyleOrDefault(value: String): CurrencyGroupingStyle {
        return CurrencyGroupingStyle.entries.firstOrNull { it.name == value }
            ?: defaultAppSettings.currencyGroupingStyle
    }

    private fun appThemeModeOrDefault(value: String): AppThemeMode {
        return AppThemeMode.entries.firstOrNull { it.name == value }
            ?: defaultAppSettings.themeMode
    }

    private fun userTierOrDefault(value: String): com.mkn0079.expensetracker.models.UserTier {
        return com.mkn0079.expensetracker.models.UserTier.entries.firstOrNull { it.name == value }
            ?: defaultAppSettings.userTier
    }
}
