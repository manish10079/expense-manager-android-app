package com.mknlabs.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CardMembership
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationAdd
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Link
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import com.mknlabs.expensetracker.domain.repository.ConfigurationRepository
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.monetization.AdsCoordinator
import com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
    PrivacyOptions,
    TransactionCardCustomize,
    DataManagement,
    About,
    Notifications,
    ManageCategories,
    Goals,
    AdFreeAccess,
    LinkAccount,
    ConnectedDevices,
    RedeemProPass,
    LinkedIn,
    Logout,
    MyMembership
}

@Immutable
data class SettingsScreenUiState(
    val settingsSections: List<SettingsSectionUi> = emptyList()
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val monetizationRepository: MonetizationRepository,
    private val configurationRepository: ConfigurationRepository,
    private val syncRepository: com.mknlabs.expensetracker.domain.repository.SyncRepository,
    private val adsCoordinator: AdsCoordinator
) : ViewModel() {

    private var transactionCount: Int = 0
    private var isAdsEnabled: Boolean = true
    private var isAnonymous: Boolean = true
    private var isProPassEnabled: Boolean = true
    private var userTier: com.mknlabs.expensetracker.models.UserTier = com.mknlabs.expensetracker.models.UserTier.FREE
    private var isCloudSyncEnabled: Boolean = true
    private var adFreeRemainingTime: String? = null
    private var isPrivacyOptionsRequired: Boolean = false

    private val _uiState = MutableStateFlow(SettingsScreenUiState())
    val uiState: StateFlow<SettingsScreenUiState> = _uiState.asStateFlow()

    init {
        authRepository.currentUser
            .onEach { user ->
                isAnonymous = user?.isAnonymous ?: true
                rebuildUiState()
            }
            .launchIn(viewModelScope)

        configurationRepository.isProPassEnabled
            .onEach { enabled ->
                isProPassEnabled = enabled
                rebuildUiState()
            }
            .launchIn(viewModelScope)

        // GDPR / US state privacy: surface the "Privacy Options" entry point only when
        // the UMP SDK reports that the privacy options form is REQUIRED.
        adsCoordinator.privacyOptionsRequirementStatus
            .onEach { status ->
                isPrivacyOptionsRequired = status == PrivacyOptionsRequirementStatus.REQUIRED
                rebuildUiState()
            }
            .launchIn(viewModelScope)

        // Ticker to update the ad-free timer every second
        monetizationRepository.globalAdAccessExpiry
            .flatMapLatest { expiry ->
                flow {
                    while (true) {
                        val remaining = expiry - System.currentTimeMillis()
                        if (remaining > 0) {
                            val minutes = (remaining / 1000) / 60
                            val seconds = (remaining / 1000) % 60
                            emit(String.format("%02d:%02d", minutes, seconds))
                            delay(1000)
                        } else {
                            emit(null)
                            break
                        }
                    }
                }
            }
            .onEach { time ->
                adFreeRemainingTime = time
                rebuildUiState()
            }
            .launchIn(viewModelScope)
    }

    fun updateInputs(
        transactionCount: Int, 
        isAdsEnabled: Boolean,
        userTier: com.mknlabs.expensetracker.models.UserTier,
        isCloudSyncEnabled: Boolean
    ) {
        this.transactionCount = transactionCount
        this.isAdsEnabled = isAdsEnabled
        this.userTier = userTier
        this.isCloudSyncEnabled = isCloudSyncEnabled
        
        // Force refresh the live anonymous state from Firebase SDK
        this.isAnonymous = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.isAnonymous ?: true
        
        rebuildUiState()
    }

    private fun rebuildUiState() {
        _uiState.update {
            it.copy(
                settingsSections = buildSettingsSections(
                    transactionCountLabel = transactionCount.toString(),
                    isAdsEnabled = isAdsEnabled,
                    isAnonymous = isAnonymous,
                    isProPassEnabled = isProPassEnabled,
                    userTier = userTier,
                    isCloudSyncEnabled = isCloudSyncEnabled,
                    adFreeRemainingTime = adFreeRemainingTime,
                    privacyOptionsRequired = isPrivacyOptionsRequired
                )
            )
        }
    }
}

private fun buildSettingsSections(
    transactionCountLabel: String,
    isAdsEnabled: Boolean,
    isAnonymous: Boolean,
    isProPassEnabled: Boolean,
    userTier: com.mknlabs.expensetracker.models.UserTier,
    isCloudSyncEnabled: Boolean,
    adFreeRemainingTime: String?,
    privacyOptionsRequired: Boolean
): List<SettingsSectionUi> {
    val settingsSections = mutableListOf<SettingsSectionUi>()

    // 1. Account Section (Profile, Link Account, Cloud Sync, Logout)
    val accountItems = mutableListOf<SettingsItemUi>()
    if (isAnonymous) {
        accountItems.add(
            SettingsItemUi(
                titleRes = com.mknlabs.expensetracker.R.string.title_protect_your_data,
                subtitleRes = com.mknlabs.expensetracker.R.string.msg_link_account_desc,
                icon = Icons.Rounded.Security,
                actionId = SettingsActionId.LinkAccount,
                isHighlight = true
            )
        )
    }
    accountItems.add(
        SettingsItemUi(
            titleRes = com.mknlabs.expensetracker.R.string.label_edit_profile,
            subtitleRes = com.mknlabs.expensetracker.R.string.label_edit_profile_subtitle,
            icon = Icons.Rounded.Person,
            actionId = SettingsActionId.EditProfile
        )
    )
    accountItems.add(
        SettingsItemUi(
            titleRes = com.mknlabs.expensetracker.R.string.title_membership,
            subtitleRes = com.mknlabs.expensetracker.R.string.desc_membership_settings,
            icon = Icons.Rounded.CardMembership,
            actionId = SettingsActionId.MyMembership
        )
    )
    if (!isAnonymous) {
        accountItems.add(
            SettingsItemUi(
                titleRes = com.mknlabs.expensetracker.R.string.title_cloud_sync_devices,
                subtitleRes = if (userTier == com.mknlabs.expensetracker.models.UserTier.PREMIUM) {
                    if (isCloudSyncEnabled) com.mknlabs.expensetracker.R.string.desc_sync_active_subtitle
                    else com.mknlabs.expensetracker.R.string.desc_sync_disabled_subtitle
                } else com.mknlabs.expensetracker.R.string.desc_sync_premium_subtitle,
                icon = Icons.Rounded.CloudSync,
                actionId = SettingsActionId.ConnectedDevices,
                isLocked = userTier != com.mknlabs.expensetracker.models.UserTier.PREMIUM
            )
        )
        accountItems.add(
            SettingsItemUi(
                titleRes = com.mknlabs.expensetracker.R.string.label_logout,
                subtitleRes = com.mknlabs.expensetracker.R.string.desc_logout_subtitle,
                icon = Icons.AutoMirrored.Rounded.Logout,
                actionId = SettingsActionId.Logout,
                showChevron = false
            )
        )
    }
    settingsSections.add(
        SettingsSectionUi(
            titleRes = com.mknlabs.expensetracker.R.string.title_account,
            items = accountItems
        )
    )

    // 2. Security Section
    val securityItems = mutableListOf<SettingsItemUi>()
    securityItems.add(
        SettingsItemUi(
            titleRes = com.mknlabs.expensetracker.R.string.title_security_privacy,
            subtitleRes = com.mknlabs.expensetracker.R.string.label_security_privacy_subtitle,
            icon = Icons.Rounded.Security,
            actionId = SettingsActionId.SecurityPrivacy
        )
    )
    // GDPR / US state privacy compliance: only show the "Privacy Options" entry point
    // when the UMP SDK requires it (so it's hidden for users/regions that don't need it).
    if (privacyOptionsRequired) {
        securityItems.add(
            SettingsItemUi(
                titleRes = com.mknlabs.expensetracker.R.string.title_privacy_options,
                subtitleRes = com.mknlabs.expensetracker.R.string.label_privacy_options_subtitle,
                icon = Icons.Rounded.PrivacyTip,
                actionId = SettingsActionId.PrivacyOptions
            )
        )
    }
    settingsSections.add(
        SettingsSectionUi(
            titleRes = com.mknlabs.expensetracker.R.string.title_security_privacy_1,
            items = securityItems
        )
    )

    // 3. Workspace / Configuration Section
    settingsSections.add(
        SettingsSectionUi(
            titleRes = com.mknlabs.expensetracker.R.string.title_preference,
            items = listOf(
                SettingsItemUi(
                    titleRes = com.mknlabs.expensetracker.R.string.title_manage_category,
                    subtitleRes = com.mknlabs.expensetracker.R.string.label_manage_category_subtitle,
                    icon = Icons.Rounded.Category,
                    actionId = SettingsActionId.ManageCategories
                ),
                SettingsItemUi(
                    titleRes = com.mknlabs.expensetracker.R.string.title_app_preferences,
                    subtitleRes = com.mknlabs.expensetracker.R.string.label_app_preferences_subtitle,
                    icon = Icons.Rounded.SettingsSuggest,
                    actionId = SettingsActionId.AppPreferences
                ),
                SettingsItemUi(
                    titleRes = com.mknlabs.expensetracker.R.string.title_notifications_1,
                    subtitleRes = com.mknlabs.expensetracker.R.string.label_notifications_subtitle,
                    icon = Icons.Rounded.NotificationAdd,
                    actionId = SettingsActionId.Notifications
                ),
                SettingsItemUi(
                    titleRes = com.mknlabs.expensetracker.R.string.title_transaction_card,
                    subtitleRes = com.mknlabs.expensetracker.R.string.label_transaction_card_subtitle,
                    icon = Icons.Rounded.Tune,
                    actionId = SettingsActionId.TransactionCardCustomize
                )
            )
        )
    )

    // 4. Data Section
    settingsSections.add(
        SettingsSectionUi(
            titleRes = com.mknlabs.expensetracker.R.string.title_database,
            items = listOf(
                SettingsItemUi(
                    titleRes = com.mknlabs.expensetracker.R.string.title_data_management,
                    subtitleRes = com.mknlabs.expensetracker.R.string.label_data_management_subtitle,
                    icon = Icons.Rounded.Dns,
                    actionId = SettingsActionId.DataManagement
                )
            )
        )
    )

    // 5. Monetization Section (Only for Free users)
    if (userTier != com.mknlabs.expensetracker.models.UserTier.PREMIUM) {
        val monetizationItems = mutableListOf<SettingsItemUi>()
        val adPassActive = !isAdsEnabled && adFreeRemainingTime != null
        
        monetizationItems.add(
            SettingsItemUi(
                titleRes = if (adPassActive) com.mknlabs.expensetracker.R.string.label_ad_free_active
                else com.mknlabs.expensetracker.R.string.label_remove_all_ads,
                subtitleRes = if (adPassActive) com.mknlabs.expensetracker.R.string.msg_ad_free_duration_remaining
                else com.mknlabs.expensetracker.R.string.msg_watch_ad_for_ad_free,
                icon = Icons.Rounded.CreditCard,
                actionId = if (adPassActive) null else SettingsActionId.AdFreeAccess,
                trailing = if (adPassActive) adFreeRemainingTime else null,
                showChevron = !adPassActive
            )
        )

        if (isProPassEnabled) {
            monetizationItems.add(
                SettingsItemUi(
                    titleRes = com.mknlabs.expensetracker.R.string.title_redeem_pro_pass,
                    subtitleRes = com.mknlabs.expensetracker.R.string.label_redeem_pro_pass_subtitle,
                    icon = Icons.Rounded.ConfirmationNumber,
                    actionId = SettingsActionId.RedeemProPass
                )
            )
        }

        settingsSections.add(
            SettingsSectionUi(
                titleRes = com.mknlabs.expensetracker.R.string.title_monetization_caps,
                items = monetizationItems
            )
        )
    }

    // 6. About Section
    settingsSections.add(
        SettingsSectionUi(
            titleRes = com.mknlabs.expensetracker.R.string.title_about_caps,
            items = listOf(
                SettingsItemUi(
                    titleRes = com.mknlabs.expensetracker.R.string.title_about,
                    subtitleRes = com.mknlabs.expensetracker.R.string.label_about_subtitle,
                    icon = Icons.Rounded.Info,
                    actionId = SettingsActionId.About
                ),
                SettingsItemUi(
                    titleRes = com.mknlabs.expensetracker.R.string.title_connect_on_linkedin,
                    subtitleRes = com.mknlabs.expensetracker.R.string.label_connect_on_linkedin_subtitle,
                    icon = Icons.Rounded.Link,
                    actionId = SettingsActionId.LinkedIn
                )
            )
        )
    )

    return settingsSections
}

fun formatAutoLockDurationLabel(minutes: Int): String {
    return if (minutes <= 0) {
        "Immediately"
    } else {
        "$minutes min"
    }
}
