package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.monetization.AdPlacement
import com.mknlabs.expensetracker.ui.components.AdaptiveContent
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.ui.components.ProfileCard
import com.mknlabs.expensetracker.ui.components.ProPassRedeemDialog
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.ui.viewmodels.SettingsViewModel

private const val DEFAULT_NOTIFICATIONS_ENABLED = true
private const val DEFAULT_BUDGET_LIMIT_ALERTS_ENABLED = true
private const val DEFAULT_MISSED_ENTRY_REMINDER_ENABLED = true

/**
 * Route composable for the Settings Screen.
 * Handles ViewModel injection, state observation, and top-level callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    userProfile: UserProfile = defaultUserProfile,
    userTier: UserTier = UserTier.FREE,
    isCloudSyncEnabled: Boolean = true,
    isDailyReminderEnabled: Boolean = DEFAULT_NOTIFICATIONS_ENABLED,
    isBudgetLimitAlertsEnabled: Boolean = DEFAULT_BUDGET_LIMIT_ALERTS_ENABLED,
    isMissedEntryReminderEnabled: Boolean = DEFAULT_MISSED_ENTRY_REMINDER_ENABLED,
    transactionCount: Int = 0,
    onDailyReminderChange: (Boolean) -> Unit = {},
    onBudgetLimitAlertsChange: (Boolean) -> Unit = {},
    onMissedEntryReminderChange: (Boolean) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCloudSyncDevicesClick: () -> Unit = {},
    onConnectedDevicesClick: () -> Unit = onCloudSyncDevicesClick,
    onSecurityPrivacyClick: () -> Unit = {},
    onMembershipClick: () -> Unit = {},
    onAdFreeAccessClick: () -> Unit = {},
    onRedeemProPassClick: () -> Unit = {},
    onManageCategoryClick: () -> Unit = {},
    onAppPreferencesClick: () -> Unit = {},
    onPreferencesClick: () -> Unit = onAppPreferencesClick,
    onNotificationsClick: () -> Unit = {},
    onTransactionCardCustomizeClick: () -> Unit = {},
    onDataManagementClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onLinkAccountClick: () -> Unit = {},
    onShowUpgradeSheet: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    isAdsEnabled: Boolean = false
) {
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()

    LaunchedEffect(
        transactionCount, isAdsEnabled, userTier, isCloudSyncEnabled, userProfile
    ) {
        settingsViewModel.updateInputs(
            transactionCount = transactionCount,
            isAdsEnabled = isAdsEnabled,
            userTier = userTier,
            isCloudSyncEnabled = isCloudSyncEnabled
        )
    }

    var showRedeemDialog by remember { mutableStateOf(false) }

    if (showRedeemDialog) {
        ProPassRedeemDialog(
            viewModel = monetizationViewModel,
            onDismiss = { showRedeemDialog = false }
        )
    }

    SettingsScreenContent(
        modifier = modifier,
        userProfile = userProfile,
        userTier = userTier,
        isAdsEnabled = isAdsEnabled,
        onProfileClick = onProfileClick,
        onCloudSyncDevicesClick = onConnectedDevicesClick,
        onSecurityPrivacyClick = onSecurityPrivacyClick,
        onAdFreeAccessClick = onAdFreeAccessClick,
        onRedeemProPassClick = { showRedeemDialog = true },
        onManageCategoryClick = onManageCategoryClick,
        onAppPreferencesClick = onPreferencesClick,
        onNotificationsClick = onNotificationsClick,
        onTransactionCardCustomizeClick = onTransactionCardCustomizeClick,
        onDataManagementClick = onDataManagementClick,
        onAboutClick = onAboutClick,
        onLogoutClick = onLogoutClick,
        onBackClick = onBackClick
    )
}

/**
 * Previewable Content composable for the Settings Screen.
 * Pure UI with no ViewModel dependency.
 */
@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    userProfile: UserProfile = defaultUserProfile,
    userTier: UserTier = UserTier.FREE,
    isAdsEnabled: Boolean = false,
    onProfileClick: () -> Unit = {},
    onCloudSyncDevicesClick: () -> Unit = {},
    onSecurityPrivacyClick: () -> Unit = {},
    onAdFreeAccessClick: () -> Unit = {},
    onRedeemProPassClick: () -> Unit = {},
    onManageCategoryClick: () -> Unit = {},
    onAppPreferencesClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onTransactionCardCustomizeClick: () -> Unit = {},
    onDataManagementClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val isProUser = userTier == UserTier.PREMIUM

    Box(
        modifier = modifier
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

            AppHeader(
                title = stringResource(R.string.title_settings),
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            AdaptiveContent(
                maxWidth = 640.dp,
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        ProfileCard(
                            name = userProfile.fullName,
                            email = userProfile.emailAddress,
                            gender = userProfile.gender,
                            photoUri = userProfile.photoUri,
                            userTier = userTier,
                            isAnonymous = userProfile.authProvider == "anonymous",
                            onClick = onProfileClick
                        )
                    }

                    if (isAdsEnabled) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                        }
                    }

                    // Section 1: ACCOUNT & SECURITY
                    item {
                        SettingsSectionContainer(
                            headerRes = R.string.header_account_and_security,
                            items = listOf(
                                SettingsRowData(
                                    titleRes = R.string.label_edit_profile,
                                    subtitleRes = R.string.label_edit_profile_subtitle,
                                    icon = Icons.Filled.Person,
                                    onClick = onProfileClick
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_cloud_sync_devices,
                                    subtitleRes = R.string.desc_cloud_sync_devices_subtitle,
                                    icon = Icons.Filled.Cloud,
                                    onClick = if (isProUser) onCloudSyncDevicesClick else { {} },
                                    isEnabled = isProUser
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_security_privacy,
                                    subtitleRes = R.string.label_security_privacy_subtitle,
                                    icon = Icons.Filled.Shield,
                                    onClick = onSecurityPrivacyClick
                                )
                            )
                        )
                    }

                    // Section 2: MEMBERSHIP
                    item {
                        SettingsSectionContainer(
                            headerRes = R.string.header_membership,
                            items = listOf(
                                SettingsRowData(
                                    titleRes = R.string.label_remove_all_ads,
                                    subtitleRes = if (isProUser) R.string.label_ad_free_active else R.string.label_remove_all_ads_subtitle,
                                    icon = Icons.Filled.Star,
                                    onClick = if (isProUser) { {} } else onAdFreeAccessClick,
                                    isEnabled = !isProUser
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_redeem_pro_pass,
                                    subtitleRes = R.string.label_redeem_pro_pass_subtitle,
                                    icon = Icons.Rounded.LocalOffer,
                                    onClick = onRedeemProPassClick
                                )
                            )
                        )
                    }

                    // Section 3: PREFERENCES
                    item {
                        SettingsSectionContainer(
                            headerRes = R.string.header_preferences,
                            items = listOf(
                                SettingsRowData(
                                    titleRes = R.string.title_manage_category,
                                    subtitleRes = R.string.label_manage_category_subtitle,
                                    icon = Icons.Filled.Category,
                                    onClick = onManageCategoryClick
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_app_preferences,
                                    subtitleRes = R.string.label_app_preferences_subtitle,
                                    icon = Icons.Filled.Tune,
                                    onClick = onAppPreferencesClick
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_notifications,
                                    subtitleRes = R.string.label_notifications_subtitle,
                                    icon = Icons.Filled.Notifications,
                                    onClick = onNotificationsClick
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_transaction_card,
                                    subtitleRes = R.string.label_transaction_card_subtitle,
                                    icon = Icons.Filled.CreditCard,
                                    onClick = onTransactionCardCustomizeClick
                                )
                            )
                        )
                    }

                    // Section 4: SYSTEM & DATA
                    item {
                        SettingsSectionContainer(
                            headerRes = R.string.header_system_and_data,
                            items = listOf(
                                SettingsRowData(
                                    titleRes = R.string.title_data_management,
                                    subtitleRes = R.string.label_data_management_subtitle,
                                    icon = Icons.Filled.Storage,
                                    onClick = onDataManagementClick
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_about_app,
                                    subtitleRes = R.string.label_about_app_subtitle,
                                    icon = Icons.Filled.Info,
                                    onClick = onAboutClick
                                )
                            )
                        )
                    }

                    // Isolated Action: Logout (No Card Container)
                    item {
                        SettingsIsolatedLogoutRow(onClick = onLogoutClick)
                    }
                }
            }
        }
    }
}

/**
 * Section container component grouping multiple settings rows into a single Card/Surface.
 */
@Composable
private fun SettingsSectionContainer(
    headerRes: Int,
    items: List<SettingsRowData>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(headerRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 6.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    SettingsRowItemView(data = item)

                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single Row Item inside a Section Card.
 */
@Composable
private fun SettingsRowItemView(
    data: SettingsRowData,
    modifier: Modifier = Modifier
) {
    val isEnabled = data.isEnabled
    val colorScheme = MaterialTheme.colorScheme

    val iconTint = if (isEnabled) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    val iconBg = if (isEnabled) colorScheme.primary.copy(alpha = 0.12f) else colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
    val titleColor = if (isEnabled) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.38f)
    val subtitleColor = if (isEnabled) colorScheme.onSurfaceVariant else colorScheme.onSurfaceVariant.copy(alpha = 0.38f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .then(
                if (isEnabled) Modifier.clickable(onClick = data.onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon Box (40dp with centered 24dp icon)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = iconBg,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = stringResource(data.titleRes),
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        // Title and Subtitle Text Block
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(data.titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = stringResource(data.subtitleRes),
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isEnabled) {
            // Trailing Chevron Icon
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Isolated Logout action row without a card container.
 */
@Composable
private fun SettingsIsolatedLogoutRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .defaultMinSize(minHeight = 72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon Box with Error Tint Container
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = stringResource(R.string.label_logout),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
        }

        // Title & Subtitle
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.label_logout),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = stringResource(R.string.desc_logout_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Trailing Chevron Icon with Error tint
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Data model representing a single row within a Settings Section.
 */
private data class SettingsRowData(
    val titleRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isEnabled: Boolean = true
)

@Preview(
    name = "Settings Screen Light Preview",
    showBackground = true,
    widthDp = 412,
    heightDp = 1000
)
@Composable
private fun SettingsScreenLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        SettingsScreenContent()
    }
}

@Preview(
    name = "Settings Screen Dark Preview",
    showBackground = true,
    widthDp = 412,
    heightDp = 1000
)
@Composable
private fun SettingsScreenDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        SettingsScreenContent()
    }
}


