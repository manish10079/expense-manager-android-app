package com.mkn0079.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.lifecycle.ViewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.currencyMap
import com.mkn0079.expensetracker.models.Currency
import com.mkn0079.expensetracker.utils.getDateFormatPreviewLabel
import com.mkn0079.expensetracker.utils.getTimeFormatPreviewLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class SettingsItemUi(
    val title: String,
    val icon: ImageVector,
    val trailing: String? = null,
    val actionId: SettingsActionId? = null,
    val toggleId: SettingsToggleId? = null,
    val showChevron: Boolean = true
)

@Immutable
data class SettingsSectionUi(
    val title: String,
    val items: List<SettingsItemUi>
)

enum class SettingsToggleId {
    PinLock,
    Biometric,
    BlurInRecents,
    ScreenshotProtection,
    DailyReminder,
    BudgetLimitAlerts,
    MissedEntryReminder
}

enum class SettingsActionId {
    Profile,
    AppPreferences,
    SecurityPrivacy,
    TransactionCardCustomize,
    LegacyImport,
    DeleteAllTransactions
}

@Immutable
data class SettingsScreenUiState(
    val settingsSections: List<SettingsSectionUi> = emptyList(),
    val currencySearchQuery: String = "",
    val filteredCurrencies: List<Currency> = emptyList()
)

class SettingsViewModel : ViewModel() {

    private val allCurrencies = currencyMap.values.sortedBy { it.countryName.lowercase() }

    private var currentCurrencyId: Int = DEFAULT_CURRENCY_ID
    private var currentDateFormatPattern: String = ""
    private var currentTimeFormat: String = ""
    private var autoLockDurationMinutes: Int = 0
    private var transactionCount: Int = 0

    private val _uiState = MutableStateFlow(
        SettingsScreenUiState(filteredCurrencies = allCurrencies)
    )
    val uiState: StateFlow<SettingsScreenUiState> = _uiState.asStateFlow()

    fun updateInputs(
        currentCurrencyId: Int,
        currentDateFormatPattern: String,
        currentTimeFormat: String,
        autoLockDurationMinutes: Int,
        transactionCount: Int
    ) {
        this.currentCurrencyId = currentCurrencyId
        this.currentDateFormatPattern = currentDateFormatPattern
        this.currentTimeFormat = currentTimeFormat
        this.autoLockDurationMinutes = autoLockDurationMinutes
        this.transactionCount = transactionCount
        rebuildUiState()
    }

    fun updateCurrencySearchQuery(query: String) {
        _uiState.update { it.copy(currencySearchQuery = query) }
        rebuildUiState()
    }

    fun clearCurrencySearchQuery() {
        if (_uiState.value.currencySearchQuery.isEmpty()) {
            return
        }
        _uiState.update { it.copy(currencySearchQuery = "") }
        rebuildUiState()
    }

    private fun rebuildUiState() {
        val currentCurrency = currencyMap[currentCurrencyId] ?: currencyMap[DEFAULT_CURRENCY_ID]
        val selectedCurrencyLabel = currentCurrency
            ?.let { "${it.currencySymbol} ${it.countryName}" }
            ?: "Select"
        val query = _uiState.value.currencySearchQuery.trim()
        val filteredCurrencies = if (query.isEmpty()) {
            allCurrencies
        } else {
            allCurrencies.filter { currency ->
                currency.countryName.contains(query, ignoreCase = true) ||
                    currency.currencyName.contains(query, ignoreCase = true) ||
                    currency.currencySymbol.contains(query, ignoreCase = true)
            }
        }

        _uiState.update {
            it.copy(
                settingsSections = buildSettingsSections(
                    selectedCurrencyLabel = selectedCurrencyLabel,
                    selectedDateFormatLabel = getDateFormatPreviewLabel(currentDateFormatPattern),
                    selectedTimeFormatLabel = getTimeFormatPreviewLabel(currentTimeFormat),
                    transactionCountLabel = transactionCount.toString()
                ),
                filteredCurrencies = filteredCurrencies
            )
        }
    }
}

private fun buildSettingsSections(
    selectedCurrencyLabel: String,
    selectedDateFormatLabel: String,
    selectedTimeFormatLabel: String,
    transactionCountLabel: String
): List<SettingsSectionUi> {
    return listOf(
        SettingsSectionUi(
            title = "USER PROFILE",
            items = listOf(
                SettingsItemUi(
                    title = "Profile",
                    icon = Icons.Filled.Person,
                    trailing = "Edit",
                    actionId = SettingsActionId.Profile
                )
            )
        ),
        SettingsSectionUi(
            title = "PREFERENCE",
            items = listOf(
                SettingsItemUi(
                    title = "App Preferences",
                    icon = Icons.Filled.Tune,
                    actionId = SettingsActionId.AppPreferences
                )
            )
        ),
        SettingsSectionUi(
            title = "CUSTOMIZE",
            items = listOf(
                SettingsItemUi(
                    title = "Transaction Card",
                    icon = Icons.Filled.Tune,
                    actionId = SettingsActionId.TransactionCardCustomize
                )
            )
        ),
        SettingsSectionUi(
            title = "SECURITY & PRIVACY",
            items = listOf(
                SettingsItemUi(
                    title = "Security & Privacy",
                    icon = Icons.Filled.Security,
                    actionId = SettingsActionId.SecurityPrivacy
                )
            )
        ),
        SettingsSectionUi(
            title = "DATA MANAGEMENT",
            items = listOf(
                SettingsItemUi(
                    title = "Transaction Count",
                    icon = Icons.Filled.Info,
                    trailing = transactionCountLabel,
                    showChevron = false
                ),
                SettingsItemUi(
                    title = "Import Legacy Data",
                    icon = Icons.Filled.Refresh,
                    trailing = "JSON",
                    actionId = SettingsActionId.LegacyImport
                ),
                SettingsItemUi(
                    title = "Delete All Transactions",
                    icon = Icons.Filled.Delete,
                    trailing = "Only transactions",
                    actionId = SettingsActionId.DeleteAllTransactions
                ),
                SettingsItemUi(title = "Backup", icon = Icons.Filled.Sync),
                SettingsItemUi(title = "Restore", icon = Icons.Filled.Refresh),
                SettingsItemUi(title = "Export", icon = Icons.Filled.SettingsApplications)
            )
        ),
        SettingsSectionUi(
            title = "NOTIFICATIONS",
            items = listOf(
                SettingsItemUi(
                    title = "Daily Reminder",
                    icon = Icons.Filled.Notifications,
                    toggleId = SettingsToggleId.DailyReminder
                ),
                SettingsItemUi(
                    title = "Budget Limit Alerts",
                    icon = Icons.Filled.Notifications,
                    toggleId = SettingsToggleId.BudgetLimitAlerts
                ),
                SettingsItemUi(
                    title = "Missed Entry Reminder",
                    icon = Icons.Filled.Notifications,
                    toggleId = SettingsToggleId.MissedEntryReminder
                )
            )
        ),
        SettingsSectionUi(
            title = "ABOUT",
            items = listOf(
                SettingsItemUi(title = "About", icon = Icons.Filled.Info)
            )
        )
    )
}

fun formatAutoLockDurationLabel(minutes: Int): String {
    return if (minutes <= 0) {
        "Immediately"
    } else {
        "$minutes min"
    }
}
