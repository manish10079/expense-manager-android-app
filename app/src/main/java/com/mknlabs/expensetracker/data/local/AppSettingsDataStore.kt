package com.mknlabs.expensetracker.data.local

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
import com.mknlabs.expensetracker.data.constants.defaultAppSettings
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.AppThemeMode
import com.mknlabs.expensetracker.models.CurrencyGroupingStyle
import com.mknlabs.expensetracker.models.SortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

object AppSettingsDataStore {
    const val DATA_STORE_NAME = "app_settings"

    private object Keys {
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
        val smartSmsPrompted = booleanPreferencesKey("smart_sms_prompted")
        val themeMode = stringPreferencesKey("theme_mode")
        val transactionCardShowIncomeExpenseLabels = booleanPreferencesKey("transaction_card_show_income_expense_labels")
        val transactionCardShowTransactionDate = booleanPreferencesKey("transaction_card_show_transaction_date")
        val transactionCardShowPaymentMethod = booleanPreferencesKey("transaction_card_show_payment_method")
        val transactionCardShowTransactionTime = booleanPreferencesKey("transaction_card_show_transaction_time")
        val transactionCardShowCategoryIcon = booleanPreferencesKey("transaction_card_show_category_icon")
        val transactionCardShowCategoryLabel = booleanPreferencesKey("transaction_card_show_category_label")
        val transactionCardShowDateSeparators = booleanPreferencesKey("transaction_card_show_date_separators")
        val transactionCardShowListSummaries = booleanPreferencesKey("transaction_card_show_list_summaries")
        val installDateMillis = longPreferencesKey("install_date_millis")
        val isAutoBackupEnabled = booleanPreferencesKey("is_auto_backup_enabled")
        val autoBackupFrequencyDays = intPreferencesKey("auto_backup_frequency_days")
        val lastAutoBackupTimeMillis = longPreferencesKey("last_auto_backup_time_millis")
        val lastSyncTimeMillis = longPreferencesKey("last_sync_time_millis")
        val isCloudSyncEnabled = booleanPreferencesKey("is_cloud_sync_enabled")
        val pendingAuthEmail = stringPreferencesKey("pending_auth_email")
        val setupDismissedUntilMillis = longPreferencesKey("setup_dismissed_until_millis")
        val userTier = stringPreferencesKey("user_tier")
    }

    fun getAppSettingsFlow(context: Context): Flow<AppSettings> {
        return context.applicationContext.appSettingsDataStore.data.map { it.toAppSettings() }
    }

    suspend fun initialize(context: Context) {
        context.applicationContext.appSettingsDataStore.edit { preferences ->
            if (preferences[Keys.installDateMillis] == null) {
                preferences[Keys.installDateMillis] = System.currentTimeMillis()
            }
        }
    }

    suspend fun updateAppSettings(
        context: Context,
        transform: (AppSettings) -> AppSettings
    ) {
        // Read current profile status to enforce premium features from the single source of truth
        val profile = UserProfileDataStore.getUserProfileFlow(context).first()
        val now = System.currentTimeMillis()
        val isPremium = profile.accountTier == "PREMIUM" && (profile.proExpiryTimestamp == 0L || profile.proExpiryTimestamp > now)

        context.applicationContext.appSettingsDataStore.edit { preferences ->
            val currentSettings = preferences.toAppSettings()
            var updatedSettings = transform(currentSettings)

            val wasPremium = currentSettings.userTier == com.mknlabs.expensetracker.models.UserTier.PREMIUM
            val isNowPremium = isPremium || updatedSettings.userTier == com.mknlabs.expensetracker.models.UserTier.PREMIUM
            if (isNowPremium && !wasPremium) {
                updatedSettings = updatedSettings.copy(
                    transactionCardShowListSummaries = true,
                    isCloudSyncEnabled = true
                )
            }

            // Centralized Enforcer: If user is downgraded to FREE, clean up all Premium-only settings!
            if (!isPremium) {
                val needResetTimeout = updatedSettings.appLockTimeoutMinutes !in listOf(0, 1, 5, 10, 15)
                val newTimeout = if (needResetTimeout) 1 else updatedSettings.appLockTimeoutMinutes

                val needResetBackupFreq = updatedSettings.autoBackupFrequencyDays !in listOf(7, 15, 30)
                val newBackupFreq = if (needResetBackupFreq) 7 else updatedSettings.autoBackupFrequencyDays

                updatedSettings = updatedSettings.copy(
                    transactionCardShowPaymentMethod = false,
                    blurInRecentsEnabled = false,
                    screenshotProtectionEnabled = false,
                    scrambledPinKeypadEnabled = false,
                    appLockTimeoutMinutes = newTimeout,
                    autoBackupFrequencyDays = newBackupFreq,
                    isCloudSyncEnabled = false,
                    transactionCardShowListSummaries = false
                )

                if (needResetTimeout) {
                    com.mknlabs.expensetracker.data.local.AppLockPreferences.setAutoLockDurationMinutes(context, newTimeout)
                }
            }

            preferences.writeAppSettings(updatedSettings)
        }
    }

    suspend fun updateUserTier(context: Context, tier: com.mknlabs.expensetracker.models.UserTier) {
        updateAppSettings(context) { it.copy(userTier = tier) }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        return AppSettings(
            currencyId = this[Keys.currencyId] ?: defaultAppSettings.currencyId,
            currencyGroupingStyle = this[Keys.currencyGroupingStyle]?.let { CurrencyGroupingStyle.valueOf(it) }
                ?: defaultAppSettings.currencyGroupingStyle,
            currencyDecimalPlaces = this[Keys.currencyDecimalPlaces] ?: defaultAppSettings.currencyDecimalPlaces,
            dateFormatPattern = this[Keys.dateFormatPattern] ?: defaultAppSettings.dateFormatPattern,
            timeFormat = this[Keys.timeFormat] ?: defaultAppSettings.timeFormat,
            sortBy = this[Keys.sortBy] ?: defaultAppSettings.sortBy,
            sortOrder = this[Keys.sortOrder]?.let { SortType.valueOf(it) } ?: defaultAppSettings.sortOrder,
            defaultTransactionTypeId = this[Keys.defaultTransactionTypeId] ?: defaultAppSettings.defaultTransactionTypeId,
            defaultTransactionTypeFilterId = this[Keys.defaultTransactionTypeFilterId] ?: defaultAppSettings.defaultTransactionTypeFilterId,
            defaultPaymentTypeId = this[Keys.defaultPaymentTypeId] ?: defaultAppSettings.defaultPaymentTypeId,
            languageCode = this[Keys.languageCode] ?: defaultAppSettings.languageCode,
            notificationsEnabled = this[Keys.notificationsEnabled] ?: defaultAppSettings.notificationsEnabled,
            budgetLimitAlertsEnabled = this[Keys.budgetLimitAlertsEnabled] ?: defaultAppSettings.budgetLimitAlertsEnabled,
            missedEntryReminderEnabled = this[Keys.missedEntryReminderEnabled] ?: defaultAppSettings.missedEntryReminderEnabled,
            appLockEnabled = this[Keys.appLockEnabled] ?: defaultAppSettings.appLockEnabled,
            biometricLockEnabled = this[Keys.biometricLockEnabled] ?: defaultAppSettings.biometricLockEnabled,
            scrambledPinKeypadEnabled = this[Keys.scrambledPinKeypadEnabled] ?: defaultAppSettings.scrambledPinKeypadEnabled,
            blurInRecentsEnabled = this[Keys.blurInRecentsEnabled] ?: defaultAppSettings.blurInRecentsEnabled,
            screenshotProtectionEnabled = this[Keys.screenshotProtectionEnabled] ?: defaultAppSettings.screenshotProtectionEnabled,
            appLockTimeoutMinutes = this[Keys.appLockTimeoutMinutes] ?: defaultAppSettings.appLockTimeoutMinutes,
            showOnboardingScreen = this[Keys.showOnboardingScreen] ?: defaultAppSettings.showOnboardingScreen,
            showSplashScreen = this[Keys.showSplashScreen] ?: defaultAppSettings.showSplashScreen,
            smartSmsPrompted = this[Keys.smartSmsPrompted] ?: defaultAppSettings.smartSmsPrompted,
            themeMode = this[Keys.themeMode]?.let { AppThemeMode.valueOf(it) } ?: defaultAppSettings.themeMode,
            transactionCardShowIncomeExpenseLabels = this[Keys.transactionCardShowIncomeExpenseLabels] ?: defaultAppSettings.transactionCardShowIncomeExpenseLabels,
            transactionCardShowTransactionDate = this[Keys.transactionCardShowTransactionDate] ?: defaultAppSettings.transactionCardShowTransactionDate,
            transactionCardShowPaymentMethod = this[Keys.transactionCardShowPaymentMethod] ?: defaultAppSettings.transactionCardShowPaymentMethod,
            transactionCardShowTransactionTime = this[Keys.transactionCardShowTransactionTime] ?: defaultAppSettings.transactionCardShowTransactionTime,
            transactionCardShowCategoryIcon = this[Keys.transactionCardShowCategoryIcon] ?: defaultAppSettings.transactionCardShowCategoryIcon,
            transactionCardShowCategoryLabel = this[Keys.transactionCardShowCategoryLabel] ?: defaultAppSettings.transactionCardShowCategoryLabel,
            transactionCardShowDateSeparators = this[Keys.transactionCardShowDateSeparators] ?: defaultAppSettings.transactionCardShowDateSeparators,
            transactionCardShowListSummaries = this[Keys.transactionCardShowListSummaries] ?: defaultAppSettings.transactionCardShowListSummaries,
            installDateMillis = this[Keys.installDateMillis] ?: defaultAppSettings.installDateMillis,
            isAutoBackupEnabled = this[Keys.isAutoBackupEnabled] ?: defaultAppSettings.isAutoBackupEnabled,
            autoBackupFrequencyDays = this[Keys.autoBackupFrequencyDays] ?: defaultAppSettings.autoBackupFrequencyDays,
            lastAutoBackupTimeMillis = this[Keys.lastAutoBackupTimeMillis] ?: defaultAppSettings.lastAutoBackupTimeMillis,
            lastSyncTimeMillis = this[Keys.lastSyncTimeMillis] ?: 0L,
            isCloudSyncEnabled = this[Keys.isCloudSyncEnabled] ?: defaultAppSettings.isCloudSyncEnabled,
            pendingAuthEmail = this[Keys.pendingAuthEmail],
            setupDismissedUntilMillis = this[Keys.setupDismissedUntilMillis] ?: 0L,
            userTier = this[Keys.userTier]?.let(::userTierOrDefault) ?: defaultAppSettings.userTier
        )
    }

    private fun MutablePreferences.writeAppSettings(settings: AppSettings) {
        this[Keys.currencyId] = settings.currencyId
        this[Keys.currencyGroupingStyle] = settings.currencyGroupingStyle.name
        this[Keys.currencyDecimalPlaces] = settings.currencyDecimalPlaces
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
        this[Keys.smartSmsPrompted] = settings.smartSmsPrompted
        this[Keys.themeMode] = settings.themeMode.name
        this[Keys.transactionCardShowIncomeExpenseLabels] = settings.transactionCardShowIncomeExpenseLabels
        this[Keys.transactionCardShowTransactionDate] = settings.transactionCardShowTransactionDate
        this[Keys.transactionCardShowPaymentMethod] = settings.transactionCardShowPaymentMethod
        this[Keys.transactionCardShowTransactionTime] = settings.transactionCardShowTransactionTime
        this[Keys.transactionCardShowCategoryIcon] = settings.transactionCardShowCategoryIcon
        this[Keys.transactionCardShowCategoryLabel] = settings.transactionCardShowCategoryLabel
        this[Keys.transactionCardShowDateSeparators] = settings.transactionCardShowDateSeparators
        this[Keys.transactionCardShowListSummaries] = settings.transactionCardShowListSummaries
        this[Keys.installDateMillis] = settings.installDateMillis
        this[Keys.isAutoBackupEnabled] = settings.isAutoBackupEnabled
        this[Keys.autoBackupFrequencyDays] = settings.autoBackupFrequencyDays
        this[Keys.lastAutoBackupTimeMillis] = settings.lastAutoBackupTimeMillis
        this[Keys.lastSyncTimeMillis] = settings.lastSyncTimeMillis
        this[Keys.isCloudSyncEnabled] = settings.isCloudSyncEnabled
        if (settings.pendingAuthEmail != null) {
            this[Keys.pendingAuthEmail] = settings.pendingAuthEmail
        } else {
            remove(Keys.pendingAuthEmail)
        }
        this[Keys.setupDismissedUntilMillis] = settings.setupDismissedUntilMillis
        this[Keys.userTier] = settings.userTier.name
    }

    private fun userTierOrDefault(value: String): com.mknlabs.expensetracker.models.UserTier {
        return com.mknlabs.expensetracker.models.UserTier.entries.firstOrNull { it.name == value }
            ?: defaultAppSettings.userTier
    }

    suspend fun clearAll(context: Context) {
        context.applicationContext.appSettingsDataStore.edit { it.clear() }
    }
}
