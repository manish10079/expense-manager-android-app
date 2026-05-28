package com.mkn0079.expensetracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_BUDGET_LIMIT_ALERTS_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_MISSED_ENTRY_REMINDER_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_NOTIFICATIONS_ENABLED
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.models.avatarInitials
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.ui.components.ProfileCard
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.components.SettingsItemCard
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.models.SettingsItemType
import com.mkn0079.expensetracker.ui.viewmodels.SettingsActionId
import com.mkn0079.expensetracker.ui.viewmodels.SettingsViewModel
import com.mkn0079.expensetracker.ui.viewmodels.SettingsItemUi
import com.mkn0079.expensetracker.ui.viewmodels.SettingsSectionUi
import com.mkn0079.expensetracker.ui.viewmodels.SettingsToggleId
import androidx.compose.ui.res.stringResource

import androidx.hilt.navigation.compose.hiltViewModel
import com.mkn0079.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mkn0079.expensetracker.ui.components.AdContainer
import com.mkn0079.expensetracker.ui.components.NativeAdCard
import com.mkn0079.expensetracker.monetization.AdPlacement
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.AccessStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile = defaultUserProfile,
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
    onBackClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(
        transactionCount, isAdsEnabled
    ) {
        settingsViewModel.updateInputs(transactionCount = transactionCount, isAdsEnabled = isAdsEnabled)
    }
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreenContent(
        userProfile = userProfile,
        settingsSections = uiState.settingsSections,
        isAdsEnabled = isAdsEnabled,
        isDailyReminderEnabled = isDailyReminderEnabled,
        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
        onDailyReminderChange = onDailyReminderChange,
        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
        onMissedEntryReminderChange = onMissedEntryReminderChange,
        onProfileClick = onProfileClick,
        onPreferencesClick = onPreferencesClick,
        onSecurityPrivacyClick = onSecurityPrivacyClick,
        onTransactionCardCustomizeClick = onTransactionCardCustomizeClick,
        onDataManagementClick = onDataManagementClick,
        onAboutClick = onAboutClick,
        onNotificationsClick = onNotificationsClick,
        onManageCategoryClick = onManageCategoryClick,
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
    settingsSections: List<SettingsSectionUi>,
    isAdsEnabled: Boolean,
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit,
    onProfileClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onSecurityPrivacyClick: () -> Unit,
    onTransactionCardCustomizeClick: () -> Unit,
    onDataManagementClick: () -> Unit,
    onAboutClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onManageCategoryClick: () -> Unit,
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
                        initials = userProfile.avatarInitials(),
                        photoUri = userProfile.photoUri
                    )
                }

                settingsSections.forEach { section ->
                    item(key = section.titleRes) {
                        SettingsSection(
                            section = section,
                            isDailyReminderEnabled = isDailyReminderEnabled,
                            isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                            isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                            onItemClick = { actionId ->
                                when (actionId) {
                                    SettingsActionId.EditProfile -> onProfileClick()
                                    SettingsActionId.AppPreferences -> onPreferencesClick()
                                    SettingsActionId.SecurityPrivacy -> onSecurityPrivacyClick()
                                    SettingsActionId.TransactionCardCustomize -> onTransactionCardCustomizeClick()
                                    SettingsActionId.DataManagement -> onDataManagementClick()
                                    SettingsActionId.About -> onAboutClick()
                                    SettingsActionId.Notifications -> onNotificationsClick()
                                    SettingsActionId.ManageCategories -> onManageCategoryClick()
                                    SettingsActionId.AdFreeAccess -> onAdFreeAccessClick()
                                    else -> Unit
                                }
                            },
                            onDailyReminderChange = onDailyReminderChange,
                            onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                            onMissedEntryReminderChange = onMissedEntryReminderChange
                        )

                        // Inline Native Ad after the "Customize" section
                        if (section.titleRes == R.string.title_customize_caps) {
                            Spacer(modifier = Modifier.height(18.dp))
                            AdContainer(isAdsEnabled = isAdsEnabled) {
                                NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                            }
                        }
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
    onItemClick: (SettingsActionId?) -> Unit,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        section.items.forEach { item ->
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
                    onItemClick(item.actionId)
                }
            )
        }
    }
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
            settingsSections = listOf(
                SettingsSectionUi(
                    titleRes = R.string.title_account,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.label_edit_profile,
                            subtitleRes = R.string.label_edit_profile_subtitle,
                            icon = Icons.Filled.Person,
                            actionId = SettingsActionId.EditProfile
                        )
                    )
                ),
                SettingsSectionUi(
                    titleRes = R.string.title_preference,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.title_app_preferences,
                            subtitleRes = R.string.label_app_preferences_subtitle,
                            icon = Icons.Filled.Tune,
                            actionId = SettingsActionId.AppPreferences
                        )
                    )
                ),
                SettingsSectionUi(
                    titleRes = R.string.title_customize_caps,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.title_transaction_card,
                            subtitleRes = R.string.label_transaction_card_subtitle,
                            icon = Icons.Filled.SettingsApplications,
                            actionId = SettingsActionId.TransactionCardCustomize
                        ),
                        SettingsItemUi(
                            titleRes = R.string.title_manage_category,
                            subtitleRes = R.string.label_manage_category_subtitle,
                            icon = Icons.Filled.Category,
                            actionId = SettingsActionId.ManageCategories
                        )
                    )
                ),
                SettingsSectionUi(
                    titleRes = R.string.title_security_privacy_1,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.title_security_privacy,
                            subtitleRes = R.string.label_security_privacy_subtitle,
                            icon = Icons.Filled.Security,
                            actionId = SettingsActionId.SecurityPrivacy
                        )
                    )
                ),
                SettingsSectionUi(
                    titleRes = R.string.title_data_management_1,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.title_data_management,
                            subtitleRes = R.string.label_data_management_subtitle,
                            icon = Icons.Filled.Sync,
                            actionId = SettingsActionId.DataManagement
                        )
                    )
                ),
                SettingsSectionUi(
                    titleRes = R.string.title_notifications,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.title_notifications_1,
                            subtitleRes = R.string.label_notifications_subtitle,
                            icon = Icons.Filled.NotificationAdd,
                            actionId = SettingsActionId.Notifications
                        ),
                        SettingsItemUi(
                            titleRes = R.string.title_daily_reminder,
                            subtitleRes = R.string.desc_daily_reminder,
                            icon = Icons.Filled.CalendarMonth,
                            toggleId = SettingsToggleId.DailyReminder,
                            showChevron = false
                        ),
                        SettingsItemUi(
                            titleRes = R.string.dialog_budget_limit_alerts,
                            subtitleRes = R.string.desc_budget_limit_alerts,
                            icon = Icons.Filled.CurrencyRupee,
                            toggleId = SettingsToggleId.BudgetLimitAlerts,
                            showChevron = false
                        ),
                        SettingsItemUi(
                            titleRes = R.string.title_missed_entry_reminder,
                            subtitleRes = R.string.desc_missed_entry_reminder,
                            icon = Icons.Filled.Refresh,
                            toggleId = SettingsToggleId.MissedEntryReminder,
                            showChevron = false
                        )
                    )
                ),
                SettingsSectionUi(
                    titleRes = R.string.title_about_caps,
                    items = listOf(
                        SettingsItemUi(
                            titleRes = R.string.label_about,
                            subtitleRes = R.string.label_about_subtitle,
                            icon = Icons.Filled.Info,
                            actionId = SettingsActionId.About
                        )
                    )
                )
            ),
            isAdsEnabled = true,
            isDailyReminderEnabled = true,
            isBudgetLimitAlertsEnabled = true,
            isMissedEntryReminderEnabled = false,
            onDailyReminderChange = {},
            onBudgetLimitAlertsChange = {},
            onMissedEntryReminderChange = {},
            onProfileClick = {},
            onPreferencesClick = {},
            onSecurityPrivacyClick = {},
            onTransactionCardCustomizeClick = {},
            onDataManagementClick = {},
            onAboutClick = {},
            onNotificationsClick = {},
            onManageCategoryClick = {},
            onAdFreeAccessClick = {},
            onBackClick = {}
        )
    }
}
