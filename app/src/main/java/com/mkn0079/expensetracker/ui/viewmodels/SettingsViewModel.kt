package com.mkn0079.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Storage
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class SettingsItemUi(
    val title: String,
    val subtitle: String? = null,
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
    AppPreferences,
    SecurityPrivacy,
    TransactionCardCustomize,
    DatabaseBackup,
    DatabaseRestore,
    JsonExport,
    JsonImport,
    LegacyImport,
    DeleteAllTransactions,
    DataManagement,
    About,
    Notifications,
    ManageCategories
}

@Immutable
data class SettingsScreenUiState(
    val settingsSections: List<SettingsSectionUi> = emptyList()
)

class SettingsViewModel : ViewModel() {

    private var transactionCount: Int = 0

    private val _uiState = MutableStateFlow(SettingsScreenUiState())
    val uiState: StateFlow<SettingsScreenUiState> = _uiState.asStateFlow()

    fun updateInputs(transactionCount: Int) {
        this.transactionCount = transactionCount
        rebuildUiState()
    }

    private fun rebuildUiState() {
        _uiState.update {
            it.copy(
                settingsSections = buildSettingsSections(
                    transactionCountLabel = transactionCount.toString()
                )
            )
        }
    }
}

private fun buildSettingsSections(
    transactionCountLabel: String
): List<SettingsSectionUi> {
    return listOf(
        SettingsSectionUi(
            title = "PREFERENCE",
            items = listOf(
                SettingsItemUi(
                    title = "App Preferences",
                    subtitle = "Customize app settings",
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
                    subtitle = "Adjust transaction display",
                    icon = Icons.Rounded.CreditCard,
                    actionId = SettingsActionId.TransactionCardCustomize
                ),
                SettingsItemUi(
                    title = "Manage Category",
                    subtitle = "Add or edit categories",
                    icon = Icons.Filled.Apps,
                    actionId = SettingsActionId.ManageCategories
                )
            )
        ),
        SettingsSectionUi(
            title = "SECURITY & PRIVACY",
            items = listOf(
                SettingsItemUi(
                    title = "Security & Privacy",
                    subtitle = "Protect your data and access",
                    icon = Icons.Filled.Security,
                    actionId = SettingsActionId.SecurityPrivacy
                )
            )
        ),
        SettingsSectionUi(
            title = "DATA MANAGEMENT",
            items = listOf(
                SettingsItemUi(
                    title = "Data Management",
                    subtitle = "Backup and manage data",
                    icon = Icons.Rounded.Dns,
                    actionId = SettingsActionId.DataManagement
                )
            )
        ),
        SettingsSectionUi(
            title = "NOTIFICATIONS",
            items = listOf(
                SettingsItemUi(
                    title = "Notifications",
                    subtitle = "Control alerts and reminders",
                    icon = Icons.Rounded.AccountBalanceWallet,
                    actionId = SettingsActionId.Notifications
                )
            )
        ),
        SettingsSectionUi(
            title = "ABOUT",
            items = listOf(
                SettingsItemUi(
                    title = "About",
                    subtitle = "App info and details",
                    icon = Icons.Filled.Info,
                    actionId = SettingsActionId.About
                )
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
