package com.mkn0079.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationAdd
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.Tune
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@Immutable
data class SettingsItemUi(
    val titleRes: Int,
    val subtitleRes: Int? = null,
    val icon: ImageVector,
    val trailing: String? = null,
    val actionId: SettingsActionId? = null,
    val toggleId: SettingsToggleId? = null,
    val showChevron: Boolean = true,
    val isHighlight: Boolean = false,
    val isLocked: Boolean = false
)

@Immutable
data class SettingsSectionUi(
    val titleRes: Int,
    val items: List<SettingsItemUi>
)

enum class SettingsToggleId {
    Biometric,
    DailyReminder,
    BudgetLimitAlerts,
    MissedEntryReminder
}

enum class SettingsActionId {
    AppPreferences,
    EditProfile,
    SecurityPrivacy,
    TransactionCardCustomize,
    DataManagement,
    About,
    Notifications,
    ManageCategories,
    AdFreeAccess,
    LinkAccount,
    ConnectedDevices,
    Logout
}

@Immutable
data class SettingsScreenUiState(
    val settingsSections: List<SettingsSectionUi> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private var transactionCount: Int = 0
    private var isAdsEnabled: Boolean = true
    private var isAnonymous: Boolean = true
    private var userTier: com.mkn0079.expensetracker.models.UserTier = com.mkn0079.expensetracker.models.UserTier.FREE

    private val _uiState = MutableStateFlow(SettingsScreenUiState())
    val uiState: StateFlow<SettingsScreenUiState> = _uiState.asStateFlow()

    init {
        authRepository.currentUser
            .onEach { user ->
                isAnonymous = user?.isAnonymous ?: true
                rebuildUiState()
            }
            .launchIn(viewModelScope)
    }

    fun updateInputs(
        transactionCount: Int, 
        isAdsEnabled: Boolean,
        userTier: com.mkn0079.expensetracker.models.UserTier
    ) {
        this.transactionCount = transactionCount
        this.isAdsEnabled = isAdsEnabled
        this.userTier = userTier
        rebuildUiState()
    }

    private fun rebuildUiState() {
        _uiState.update {
            it.copy(
                settingsSections = buildSettingsSections(
                    transactionCountLabel = transactionCount.toString(),
                    isAdsEnabled = isAdsEnabled,
                    isAnonymous = isAnonymous,
                    userTier = userTier
                )
            )
        }
    }
}

private fun buildSettingsSections(
    transactionCountLabel: String,
    isAdsEnabled: Boolean,
    isAnonymous: Boolean,
    userTier: com.mkn0079.expensetracker.models.UserTier
): List<SettingsSectionUi> {
    val accountItems = mutableListOf<SettingsItemUi>()
    
    // Add Link Account Highlight if Guest
    if (isAnonymous) {
        accountItems.add(
            SettingsItemUi(
                titleRes = com.mkn0079.expensetracker.R.string.title_protect_your_data,
                subtitleRes = com.mkn0079.expensetracker.R.string.msg_link_account_desc,
                icon = Icons.Rounded.Security,
                actionId = SettingsActionId.LinkAccount,
                isHighlight = true
            )
        )
    }

    accountItems.add(
        SettingsItemUi(
            titleRes = com.mkn0079.expensetracker.R.string.label_edit_profile,
            subtitleRes = com.mkn0079.expensetracker.R.string.label_edit_profile_subtitle,
            icon = Icons.Rounded.Person,
            actionId = SettingsActionId.EditProfile
        )
    )

    // Add Cloud Sync & Devices (Locked for Free, Active for Premium) - Only for Authenticated
    if (!isAnonymous) {
        accountItems.add(
            SettingsItemUi(
                titleRes = com.mkn0079.expensetracker.R.string.title_cloud_sync_devices,
                subtitleRes = if (userTier == com.mkn0079.expensetracker.models.UserTier.PREMIUM) 
                    com.mkn0079.expensetracker.R.string.desc_sync_active_subtitle 
                else com.mkn0079.expensetracker.R.string.desc_sync_premium_subtitle,
                icon = Icons.Rounded.CloudSync,
                actionId = SettingsActionId.ConnectedDevices,
                isLocked = userTier != com.mkn0079.expensetracker.models.UserTier.PREMIUM
            )
        )
    }

    // Add Logout if NOT Anonymous
    if (!isAnonymous) {
        accountItems.add(
            SettingsItemUi(
                titleRes = com.mkn0079.expensetracker.R.string.label_logout,
                subtitleRes = com.mkn0079.expensetracker.R.string.desc_logout_subtitle,
                icon = Icons.AutoMirrored.Rounded.Logout,
                actionId = SettingsActionId.Logout,
                showChevron = false
            )
        )
    }

    return listOf(
        SettingsSectionUi(
            titleRes = com.mkn0079.expensetracker.R.string.title_account,
            items = accountItems
        ),
        SettingsSectionUi(
            titleRes = com.mkn0079.expensetracker.R.string.title_monetization_caps,
            items = listOf(
                SettingsItemUi(
                    titleRes = com.mkn0079.expensetracker.R.string.label_remove_all_ads,
                    subtitleRes = com.mkn0079.expensetracker.R.string.msg_watch_ad_for_ad_free,
                    icon = Icons.Rounded.CreditCard,
                    actionId = SettingsActionId.AdFreeAccess,
                    trailing = if (!isAdsEnabled) "ACTIVE" else "WATCH NOW",
                    showChevron = false
                )
            )
        ),
        SettingsSectionUi(
            titleRes = com.mkn0079.expensetracker.R.string.title_preference,
            items = listOf(
                SettingsItemUi(
                    titleRes = com.mkn0079.expensetracker.R.string.title_app_preferences,
                    subtitleRes = com.mkn0079.expensetracker.R.string.label_app_preferences_subtitle,
                    icon = Icons.Rounded.SettingsSuggest,
                    actionId = SettingsActionId.AppPreferences
                ),
                SettingsItemUi(
                    titleRes = com.mkn0079.expensetracker.R.string.title_transaction_card,
                    subtitleRes = com.mkn0079.expensetracker.R.string.label_transaction_card_subtitle,
                    icon = Icons.Rounded.Tune,
                    actionId = SettingsActionId.TransactionCardCustomize
                ),
                SettingsItemUi(
                    titleRes = com.mkn0079.expensetracker.R.string.title_manage_category,
                    subtitleRes = com.mkn0079.expensetracker.R.string.label_manage_category_subtitle,
                    icon = Icons.Rounded.Category,
                    actionId = SettingsActionId.ManageCategories
                )
            )
        ),
        SettingsSectionUi(
            titleRes = com.mkn0079.expensetracker.R.string.title_security_privacy_1,
            items = listOf(
                SettingsItemUi(
                    titleRes = com.mkn0079.expensetracker.R.string.title_security_privacy,
                    subtitleRes = com.mkn0079.expensetracker.R.string.label_security_privacy_subtitle,
                    icon = Icons.Rounded.Security,
                    actionId = SettingsActionId.SecurityPrivacy
                )
            )
        ),
        SettingsSectionUi(
            titleRes = com.mkn0079.expensetracker.R.string.title_database,
            items = listOf(
                SettingsItemUi(
                    titleRes = com.mkn0079.expensetracker.R.string.title_data_management,
                    subtitleRes = com.mkn0079.expensetracker.R.string.label_data_management_subtitle,
                    icon = Icons.Rounded.Dns,
                    actionId = SettingsActionId.DataManagement,
                    showChevron = true
                )
            )
        ),
        SettingsSectionUi(
            titleRes = com.mkn0079.expensetracker.R.string.title_notifications,
            items = listOf(
                SettingsItemUi(
                    titleRes = com.mkn0079.expensetracker.R.string.title_notifications_1,
                    subtitleRes = com.mkn0079.expensetracker.R.string.label_notifications_subtitle,
                    icon = Icons.Rounded.NotificationAdd,
                    actionId = SettingsActionId.Notifications
                )
            )
        ),
        SettingsSectionUi(
            titleRes = com.mkn0079.expensetracker.R.string.title_about_caps,
            items = listOf(
                SettingsItemUi(
                    titleRes = com.mkn0079.expensetracker.R.string.title_about,
                    subtitleRes = com.mkn0079.expensetracker.R.string.label_about_subtitle,
                    icon = Icons.Rounded.Info,
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
