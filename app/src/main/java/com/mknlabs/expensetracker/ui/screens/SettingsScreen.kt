package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationAdd
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.data.constants.DEFAULT_BUDGET_LIMIT_ALERTS_ENABLED
import com.mknlabs.expensetracker.data.constants.DEFAULT_MISSED_ENTRY_REMINDER_ENABLED
import com.mknlabs.expensetracker.data.constants.DEFAULT_NOTIFICATIONS_ENABLED
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.ui.components.ProfileCard
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.ui.viewmodels.SettingsActionId
import com.mknlabs.expensetracker.ui.viewmodels.SettingsViewModel
import com.mknlabs.expensetracker.ui.viewmodels.SettingsItemUi
import com.mknlabs.expensetracker.ui.viewmodels.SettingsSectionUi
import com.mknlabs.expensetracker.ui.viewmodels.SettingsToggleId
import androidx.compose.ui.res.stringResource

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import com.mknlabs.expensetracker.ui.components.SettingsGroup
import com.mknlabs.expensetracker.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.ui.components.ProPassRedeemDialog
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile = defaultUserProfile,
    userTier: com.mknlabs.expensetracker.models.UserTier = com.mknlabs.expensetracker.models.UserTier.FREE,
    isCloudSyncEnabled: Boolean = true,
    isDailyReminderEnabled: Boolean = DEFAULT_NOTIFICATIONS_ENABLED,
    isBudgetLimitAlertsEnabled: Boolean = DEFAULT_BUDGET_LIMIT_ALERTS_ENABLED,
    isMissedEntryReminderEnabled: Boolean = DEFAULT_MISSED_ENTRY_REMINDER_ENABLED,
    transactionCount: Int = 0,
    onDailyReminderChange: (Boolean) -> Unit = {},
    onBudgetLimitAlertsChange: (Boolean) -> Unit = {},
    onMissedEntryReminderChange: (Boolean) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onPreferencesClick: () -> Unit = {},
    onSecurityPrivacyClick: () -> Unit = {},
    onTransactionCardCustomizeClick: () -> Unit = {},
    onDataManagementClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onManageCategoryClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onLinkAccountClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onConnectedDevicesClick: () -> Unit = {},
    onShowUpgradeSheet: () -> Unit = {},
    onMembershipClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    val effectiveUserTier = userTier

    LaunchedEffect(
        transactionCount, isAdsEnabled, effectiveUserTier, isCloudSyncEnabled, userProfile
    ) {
        settingsViewModel.updateInputs(
            transactionCount = transactionCount, 
            isAdsEnabled = isAdsEnabled,
            userTier = effectiveUserTier,
            isCloudSyncEnabled = isCloudSyncEnabled
        )
    }
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    var showRedeemDialog by remember { mutableStateOf(false) }

    if (showRedeemDialog) {
        ProPassRedeemDialog(
            viewModel = monetizationViewModel,
            onDismiss = { showRedeemDialog = false }
        )
    }

    SettingsScreenContent(
        userProfile = userProfile,
        userTier = effectiveUserTier,
        settingsSections = uiState.settingsSections,
        isAdsEnabled = isAdsEnabled,
        isDailyReminderEnabled = isDailyReminderEnabled,
        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
        onDailyReminderChange = onDailyReminderChange,
        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
        onMissedEntryReminderChange = onMissedEntryReminderChange,
        onProfileClick = onProfileClick,
        onMembershipClick = onMembershipClick,
        onPreferencesClick = onPreferencesClick,
        onSecurityPrivacyClick = onSecurityPrivacyClick,
        onTransactionCardCustomizeClick = onTransactionCardCustomizeClick,
        onDataManagementClick = onDataManagementClick,
        onAboutClick = onAboutClick,
        onNotificationsClick = onNotificationsClick,
        onManageCategoryClick = onManageCategoryClick,
        onGoalsClick = onGoalsClick,
        onLinkAccountClick = onLinkAccountClick,
        onLogoutClick = onLogoutClick,
        onConnectedDevicesClick = onConnectedDevicesClick,
        onShowUpgradeSheet = onShowUpgradeSheet,
        onRedeemProPassClick = { showRedeemDialog = true },
        onAdFreeAccessClick = {
            val activity = context as? android.app.Activity
            if (activity != null) {
                monetizationViewModel.onWatchAdFreeClicked(activity)
            }
        },
        onBackClick = onBackClick
    )
}

@Composable
private fun SettingsScreenContent(
    userProfile: UserProfile,
    userTier: com.mknlabs.expensetracker.models.UserTier,
    settingsSections: List<SettingsSectionUi>,
    isAdsEnabled: Boolean,
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit,
    onProfileClick: () -> Unit,
    onMembershipClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onSecurityPrivacyClick: () -> Unit,
    onTransactionCardCustomizeClick: () -> Unit,
    onDataManagementClick: () -> Unit,
    onAboutClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onManageCategoryClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onLinkAccountClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onConnectedDevicesClick: () -> Unit,
    onShowUpgradeSheet: () -> Unit,
    onRedeemProPassClick: () -> Unit,
    onAdFreeAccessClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))

            AppHeader(title = stringResource(R.string.desc_settings), onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    ProfileCard(
                        name = userProfile.fullName,
                        email = userProfile.emailAddress,
                        gender = userProfile.gender,
                        photoUri = userProfile.photoUri,
                        userTier = userTier,
                        isAnonymous = userProfile.authProvider == "anonymous",
                    )
                }

                settingsSections.forEach { section ->
                    // Inline Native Ad before the "Workspace / Configuration" section
                    if (section.titleRes == R.string.title_preference && isAdsEnabled) {
                        item {
                            NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                        }
                    }

                    item(key = section.titleRes) {
                        SettingsSection(
                            section = section,
                            isDailyReminderEnabled = isDailyReminderEnabled,
                            isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                            isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                            onItemClick = { item ->
                                when (item.actionId) {
                                    SettingsActionId.EditProfile -> onProfileClick()
                                    SettingsActionId.MyMembership -> onMembershipClick()
                                    SettingsActionId.AppPreferences -> onPreferencesClick()
                                    SettingsActionId.SecurityPrivacy -> onSecurityPrivacyClick()
                                    SettingsActionId.TransactionCardCustomize -> onTransactionCardCustomizeClick()
                                    SettingsActionId.DataManagement -> onDataManagementClick()
                                    SettingsActionId.About -> onAboutClick()
                                    SettingsActionId.Notifications -> onNotificationsClick()
                                    SettingsActionId.ManageCategories -> onManageCategoryClick()
                                    SettingsActionId.Goals -> onGoalsClick()
                                    SettingsActionId.AdFreeAccess -> onAdFreeAccessClick()
                                    SettingsActionId.LinkAccount -> onLinkAccountClick()
                                    SettingsActionId.ConnectedDevices -> {
                                        if (item.isLocked) {
                                            onShowUpgradeSheet()
                                        } else {
                                            onConnectedDevicesClick()
                                        }
                                    }
                                    SettingsActionId.Logout -> onLogoutClick()
                                    SettingsActionId.RedeemProPass -> onRedeemProPassClick()
                                    else -> Unit
                                }
                            },
                            onDailyReminderChange = onDailyReminderChange,
                            onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                            onMissedEntryReminderChange = onMissedEntryReminderChange
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    section: SettingsSectionUi,
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    onItemClick: (SettingsItemUi) -> Unit,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val nonHighlightItems = section.items.filter { !it.isHighlight }
        val highlightItems = section.items.filter { it.isHighlight }

        // Render Highlight items first (Standalone)
        highlightItems.forEach { item ->
            SettingsItemContent(
                item = item,
                standalone = true,
                isDailyReminderEnabled = isDailyReminderEnabled,
                isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                onItemClick = onItemClick,
                onDailyReminderChange = onDailyReminderChange,
                onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                onMissedEntryReminderChange = onMissedEntryReminderChange
            )
        }

        // Render non-highlight items in a Grouped Card
        if (nonHighlightItems.isNotEmpty()) {
            SettingsGroup {
                nonHighlightItems.forEachIndexed { index, item ->
                    if (index > 0) {
                        SettingsGroupDivider()
                    }
                    SettingsItemContent(
                        item = item,
                        standalone = false,
                        isDailyReminderEnabled = isDailyReminderEnabled,
                        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                        onItemClick = onItemClick,
                        onDailyReminderChange = onDailyReminderChange,
                        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                        onMissedEntryReminderChange = onMissedEntryReminderChange
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItemContent(
    item: SettingsItemUi,
    standalone: Boolean,
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    onItemClick: (SettingsItemUi) -> Unit,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit
) {
    val toggleState = when (item.toggleId) {
        SettingsToggleId.DailyReminder -> isDailyReminderEnabled
        SettingsToggleId.BudgetLimitAlerts -> isBudgetLimitAlertsEnabled
        SettingsToggleId.MissedEntryReminder -> isMissedEntryReminderEnabled
        else -> null
    }
    val itemType = when {
        toggleState != null -> SettingsItemType.Toggle
        !item.trailing.isNullOrEmpty() && !item.showChevron -> SettingsItemType.Value
        item.showChevron -> SettingsItemType.Navigation
        else -> SettingsItemType.Value
    }

    SettingsItemCard(
        icon = item.icon,
        title = stringResource(item.titleRes),
        subtitle = item.subtitleRes?.let { stringResource(it) },
        type = itemType,
        valueText = item.trailing,
        isEnabled = true,
        isChecked = toggleState ?: false,
        isHighlight = item.isHighlight,
        isLocked = item.isLocked,
        standalone = standalone,
        onCheckedChange = { isChecked ->
            when (item.toggleId) {
                SettingsToggleId.DailyReminder -> onDailyReminderChange(isChecked)
                SettingsToggleId.BudgetLimitAlerts -> onBudgetLimitAlertsChange(isChecked)
                SettingsToggleId.MissedEntryReminder -> onMissedEntryReminderChange(isChecked)
                null -> Unit
                else -> Unit
            }
        },
        onClick = {
            onItemClick(item)
        }
    )
}

 

@Preview(
    name = "Settings Screen Preview",
    showBackground = true,
    widthDp = 412,
    heightDp = 1600
)
@Composable
private fun SettingsScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        SettingsScreenContent(
            userProfile = defaultUserProfile,
            userTier = com.mknlabs.expensetracker.models.UserTier.FREE,
            settingsSections = listOf(
                // 1. Account Section
                SettingsSectionUi(
                    titleRes = R.string.title_account,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.label_edit_profile,
                            subtitleRes = R.string.label_edit_profile_subtitle,
                            icon = Icons.Rounded.Person,
                            actionId = SettingsActionId.EditProfile
                        ),
                        SettingsItemUi(
                            titleRes = R.string.title_cloud_sync_devices,
                            subtitleRes = R.string.desc_sync_premium_subtitle,
                            icon = Icons.Rounded.CloudSync,
                            actionId = SettingsActionId.ConnectedDevices,
                            isLocked = true
                        )
                    )
                ),
                // 2. Monetization Section
                SettingsSectionUi(
                    titleRes = R.string.title_monetization_caps,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.label_remove_all_ads,
                            subtitleRes = R.string.msg_watch_ad_for_ad_free,
                            icon = Icons.Rounded.CreditCard,
                            actionId = SettingsActionId.AdFreeAccess
                        ),
                        SettingsItemUi(
                            titleRes = R.string.title_redeem_pro_pass,
                            subtitleRes = R.string.label_redeem_pro_pass_subtitle,
                            icon = Icons.Rounded.ConfirmationNumber,
                            actionId = SettingsActionId.RedeemProPass
                        )
                    )
                ),
                // 3. Security Section
                SettingsSectionUi(
                    titleRes = R.string.title_security_privacy_1,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.title_security_privacy,
                            subtitleRes = R.string.label_security_privacy_subtitle,
                            icon = Icons.Rounded.Security,
                            actionId = SettingsActionId.SecurityPrivacy
                        )
                    )
                ),
                // 4. Workspace / Configuration Section
                SettingsSectionUi(
                    titleRes = R.string.title_preference,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.title_manage_category,
                            subtitleRes = R.string.label_manage_category_subtitle,
                            icon = Icons.Rounded.Category,
                            actionId = SettingsActionId.ManageCategories
                        ),
                        SettingsItemUi(
                            titleRes = R.string.title_app_preferences,
                            subtitleRes = R.string.label_app_preferences_subtitle,
                            icon = Icons.Rounded.SettingsSuggest,
                            actionId = SettingsActionId.AppPreferences
                        ),
                        SettingsItemUi(
                            titleRes = R.string.title_notifications_1,
                            subtitleRes = R.string.label_notifications_subtitle,
                            icon = Icons.Rounded.NotificationAdd,
                            actionId = SettingsActionId.Notifications
                        ),
                        SettingsItemUi(
                            titleRes = R.string.title_transaction_card,
                            subtitleRes = R.string.label_transaction_card_subtitle,
                            icon = Icons.Rounded.Tune,
                            actionId = SettingsActionId.TransactionCardCustomize
                        )
                    )
                ),
                // 5. Data Section
                SettingsSectionUi(
                    titleRes = R.string.title_database,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.title_data_management,
                            subtitleRes = R.string.label_data_management_subtitle,
                            icon = Icons.Rounded.Dns,
                            actionId = SettingsActionId.DataManagement
                        )
                    )
                ),
                // 6. Session Section
                SettingsSectionUi(
                    titleRes = R.string.title_session,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.label_logout,
                            subtitleRes = R.string.desc_logout_subtitle,
                            icon = Icons.AutoMirrored.Rounded.Logout,
                            actionId = SettingsActionId.Logout,
                            showChevron = false
                        )
                    )
                ),
                // 7. About Section
                SettingsSectionUi(
                    titleRes = R.string.title_about_caps,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.title_about,
                            subtitleRes = R.string.label_about_subtitle,
                            icon = Icons.Rounded.Info,
                            actionId = SettingsActionId.About
                        )
                    )
                )
            ),
            isAdsEnabled = false,
            isDailyReminderEnabled = true,
            isBudgetLimitAlertsEnabled = true,
            isMissedEntryReminderEnabled = false,
            onDailyReminderChange = {},
            onBudgetLimitAlertsChange = {},
            onMissedEntryReminderChange = {},
            onProfileClick = {},
            onMembershipClick = {},
            onPreferencesClick = {},
            onSecurityPrivacyClick = {},
            onTransactionCardCustomizeClick = {},
            onDataManagementClick = {},
            onAboutClick = {},
            onNotificationsClick = {},
            onManageCategoryClick = {},
            onGoalsClick = {},
            onLinkAccountClick = {},
            onLogoutClick = {},
            onConnectedDevicesClick = {},
            onShowUpgradeSheet = {},
            onRedeemProPassClick = {},
            onAdFreeAccessClick = {},
            onBackClick = {}
        )
    }
}
