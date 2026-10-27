package com.mknlabs.expensetracker.feature.settings.ui

import android.content.Intent

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Cloud
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Database
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Tags
import com.composables.icons.lucide.Ticket
import com.composables.icons.lucide.UserPlus
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.User














import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.core.ui.components.adPassDurationLabel
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.monetization.AdPlacement
import com.mknlabs.expensetracker.core.ui.components.AdaptiveContent
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.theme.settingsSubtitleInk
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.core.ui.components.ProfileCard
import com.mknlabs.expensetracker.core.ui.components.ProPassRedeemDialog
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.SettingsTilePalette
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.disabled

import com.mknlabs.expensetracker.monetization.MonetizationViewModel

// Leading-tile palette, shared with the sibling settings screens through Color.kt.
private val TileViolet  = SettingsTiles.Violet
private val TileSky     = SettingsTiles.Sky
private val TileEmerald = SettingsTiles.Emerald
private val TileFuchsia = SettingsTiles.Fuchsia
private val TileAmber   = SettingsTiles.Amber
private val TileIndigo  = SettingsTiles.Indigo
private val TilePurple  = SettingsTiles.Purple
private val TilePink    = SettingsTiles.Pink
private val TileBlue    = SettingsTiles.Blue
private val TileTeal    = SettingsTiles.Teal
private val TileCyan    = SettingsTiles.Cyan
private val TileRose    = SettingsTiles.Rose

// Row chevron ink, the mockup's `--chev-ink` (mock-settings.html): neutral-500 on the dark
// page, slate-400 on the light one. Local for now, like the tiles.
private val SettingsChevronInkDark = Color(0xFF737373)
private val SettingsChevronInkLight = Color(0xFF94A3B8)

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
    onManageTagsClick: () -> Unit = {},
    onAppPreferencesClick: () -> Unit = {},
    onPreferencesClick: () -> Unit = onAppPreferencesClick,
    onNotificationsClick: () -> Unit = {},
    onTransactionCardCustomizeClick: () -> Unit = {},
    onDataManagementClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onReferAFriendClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onLinkAccountClick: () -> Unit = {},
    onShowUpgradeSheet: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    isAdsEnabled: Boolean = false
) {
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()

    val adFreeRemainingTime by settingsViewModel.adFreeRemainingTime.collectAsStateWithLifecycle()
    // Comes from the store entitlement, not from the Pro tier: the store is the authority on a
    // subscription, so its answer alone can grey the row. A running pass refuses a code too,
    // but the server is the one that says so, and the redeem dialog reports it.
    val hasActiveStoreSubscription by settingsViewModel.hasActiveStoreSubscription.collectAsStateWithLifecycle()
    // The ad-free row advertises the rewarded-ad pass, whose length is Remote Config-driven
    // (`ad_pass_duration_minutes`). Resolved here and passed down so the previewable Content
    // composable keeps taking everything through parameters (no ViewModel, no resources lookup).
    val adPassMinutes by monetizationViewModel.adPassDurationMinutes.collectAsStateWithLifecycle()
    val adFreeDurationLabel = adPassDurationLabel(adPassMinutes)

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
        adFreeRemainingTime = adFreeRemainingTime,
        adFreeDurationLabel = adFreeDurationLabel,
        hasActiveStoreSubscription = hasActiveStoreSubscription,
        onProfileClick = onProfileClick,
        onCloudSyncDevicesClick = onConnectedDevicesClick,
        onSecurityPrivacyClick = onSecurityPrivacyClick,
        onMembershipClick = onMembershipClick,
        onAdFreeAccessClick = onAdFreeAccessClick,
        onRedeemProPassClick = {
            val anonymous = userProfile.authProvider.isBlank() || userProfile.authProvider == "anonymous"
            if (anonymous) onLinkAccountClick() else showRedeemDialog = true
        },
        onManageCategoryClick = onManageCategoryClick,
        onManageTagsClick = onManageTagsClick,
        onAppPreferencesClick = onPreferencesClick,
        onNotificationsClick = onNotificationsClick,
        onTransactionCardCustomizeClick = onTransactionCardCustomizeClick,
        onDataManagementClick = onDataManagementClick,
        onAboutClick = onAboutClick,
        onReferAFriendClick = onReferAFriendClick,
        onLogoutClick = onLogoutClick,
        onLinkAccountClick = onLinkAccountClick,
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
    adFreeRemainingTime: String? = null,
    adFreeDurationLabel: String = "",
    hasActiveStoreSubscription: Boolean = false,
    onProfileClick: () -> Unit = {},
    onCloudSyncDevicesClick: () -> Unit = {},
    onSecurityPrivacyClick: () -> Unit = {},
    onMembershipClick: () -> Unit = {},
    onAdFreeAccessClick: () -> Unit = {},
    onRedeemProPassClick: () -> Unit = {},
    onManageCategoryClick: () -> Unit = {},
    onManageTagsClick: () -> Unit = {},
    onAppPreferencesClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onTransactionCardCustomizeClick: () -> Unit = {},
    onDataManagementClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onReferAFriendClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onLinkAccountClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val isProUser = userTier == UserTier.PREMIUM
    val isAdPassActive = !isProUser && adFreeRemainingTime != null
    val isAnonymous = userProfile.authProvider.isBlank() || userProfile.authProvider == "anonymous"
    val enter = rememberSectionEnterAlphas(4)
    val context = LocalContext.current
    val shareAppName = stringResource(R.string.label_app_name_display)
    val shareAppUrl = stringResource(R.string.url_rate_app)
    val shareAppBodies = listOf(
        stringResource(R.string.msg_share_app_chat, shareAppUrl),
        stringResource(R.string.msg_share_app_features, shareAppUrl),
        stringResource(R.string.msg_share_app_punchy, shareAppUrl)
    )
    val referFriendBody = stringResource(R.string.msg_refer_a_friend, shareAppName, shareAppUrl)
    val shareAppChooser = stringResource(R.string.label_share_app)
    val referFriendChooser = stringResource(R.string.title_refer_a_friend)

    Box(
        modifier = modifier
            .fillMaxSize()
            .appScreenBackground()
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
                onBackClick = onBackClick,
                modifier = Modifier.alpha(enter[0])
            )

            AdaptiveContent(
                maxWidth = 640.dp,
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Box(
                            Modifier
                                .padding(top = Dimens.spacingCompact)
                                .alpha(enter[1])
                        ) {
                        ProfileCard(
                            name = userProfile.fullName,
                            email = userProfile.emailAddress,
                            gender = userProfile.gender,
                            photoUri = userProfile.photoUri,
                            userTier = userTier,
                            isAnonymous = isAnonymous,
                            onClick = if (isAnonymous) onLinkAccountClick else onProfileClick
                        )
                        }
                    }

                    if (isAdsEnabled) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                        }
                    }

                    // Section 1: ACCOUNT & SECURITY
                    item {
                        val accountSecurityItems = mutableListOf<SettingsRowData>()

                        // Local profile is always editable. Cloud device list needs an account.
                        accountSecurityItems.add(
                            SettingsRowData(
                                titleRes = R.string.label_edit_profile,
                                subtitleRes = R.string.label_edit_profile_subtitle,
                                icon = PhosphorIcons.Regular.User,
                                tile = TileViolet,
                                onClick = onProfileClick
                            )
                        )
                        if (!isAnonymous) {
                            accountSecurityItems.add(
                                SettingsRowData(
                                    titleRes = R.string.title_cloud_sync_devices,
                                    subtitleRes = R.string.desc_cloud_sync_devices_subtitle,
                                    icon = Lucide.Cloud,
                                    tile = TileSky,
                                    onClick = if (isProUser) onCloudSyncDevicesClick else { {} },
                                    isEnabled = isProUser
                                )
                            )
                        }

                        accountSecurityItems.add(
                            SettingsRowData(
                                titleRes = R.string.title_security_privacy,
                                subtitleRes = R.string.label_security_privacy_subtitle,
                                icon = Lucide.Shield,
                                tile = TileEmerald,
                                onClick = onSecurityPrivacyClick
                            )
                        )

                        SettingsSectionContainer(
                            headerRes = R.string.header_account_and_security,
                            items = accountSecurityItems,
                            modifier = Modifier.alpha(enter[2])
                        )
                    }

                    // Section 2: MEMBERSHIP
                    item {
                        SettingsSectionContainer(
                            modifier = Modifier.alpha(enter[3]),
                            headerRes = R.string.header_membership,
                            items = listOf(
                                SettingsRowData(
                                    titleRes = R.string.title_membership,
                                    subtitleRes = if (isProUser) R.string.label_pro_active else R.string.label_free_tier,
                                    icon = Lucide.Sparkles,
                                    tile = TileFuchsia,
                                    onClick = onMembershipClick
                                ),
                                SettingsRowData(
                                    titleRes = if (isAdPassActive) R.string.label_ad_free_active else R.string.label_remove_all_ads,
                                    subtitleRes = if (isProUser) {
                                        R.string.label_ad_free_active
                                    } else if (isAdPassActive) {
                                        R.string.msg_ad_free_duration_remaining
                                    } else {
                                        R.string.label_remove_all_ads_subtitle
                                    },
                                    // This row plays a rewarded ad for a temporary pass, so the
                                    // Remote Config duration is stated instead of the
                                    // "one-time purchase" copy it inherited.
                                    subtitleText = if (!isProUser && !isAdPassActive &&
                                        adFreeDurationLabel.isNotEmpty()
                                    ) {
                                        stringResource(
                                            R.string.desc_ad_free_pass_duration,
                                            adFreeDurationLabel
                                        )
                                    } else {
                                        null
                                    },
                                    icon = Lucide.Star,
                                    tile = TileAmber,
                                    onClick = if (isProUser || isAdPassActive) { {} } else onAdFreeAccessClick,
                                    isEnabled = !isProUser && !isAdPassActive,
                                    trailing = if (isAdPassActive) adFreeRemainingTime else null
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_redeem_pro_pass,
                                    // The copy explains the greying rather than leaving the
                                    // "enter promo code" invitation on a row that no longer
                                    // responds, which would read as a bug.
                                    subtitleRes = when {
                                        hasActiveStoreSubscription -> R.string.label_redeem_pro_pass_subscribed_subtitle
                                        isAnonymous -> R.string.error_redeem_not_signed_in
                                        else -> R.string.label_redeem_pro_pass_subtitle
                                    },
                                    icon = Lucide.Ticket,
                                    tile = TileIndigo,
                                    onClick = if (hasActiveStoreSubscription) {
                                        {}
                                    } else if (isAnonymous) {
                                        onLinkAccountClick
                                    } else {
                                        onRedeemProPassClick
                                    },
                                    isEnabled = !hasActiveStoreSubscription
                                )
                            )
                        )
                    }

                    // Section 3: PREFERENCES
                    item {
                        SettingsSectionContainer(
                            modifier = Modifier.alpha(enter[3]),
                            headerRes = R.string.header_preferences,
                            items = listOf(
                                SettingsRowData(
                                    titleRes = R.string.title_manage_category,
                                    subtitleRes = R.string.label_manage_category_subtitle,
                                    icon = Lucide.LayoutGrid,
                                    tile = TilePurple,
                                    onClick = onManageCategoryClick
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_manage_tags,
                                    subtitleRes = R.string.label_manage_tags_subtitle,
                                    icon = Lucide.Tags,
                                    tile = TilePink,
                                    onClick = onManageTagsClick
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_app_preferences,
                                    subtitleRes = R.string.label_app_preferences_subtitle,
                                    icon = Lucide.SlidersHorizontal,
                                    tile = TileViolet,
                                    onClick = onAppPreferencesClick
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_notifications,
                                    subtitleRes = R.string.label_notifications_subtitle,
                                    icon = Lucide.Bell,
                                    tile = TileBlue,
                                    onClick = onNotificationsClick
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_transaction_card,
                                    subtitleRes = R.string.label_transaction_card_subtitle,
                                    icon = Lucide.CreditCard,
                                    tile = TileTeal,
                                    onClick = onTransactionCardCustomizeClick
                                )
                            )
                        )
                    }

                    // Section 4: SYSTEM & DATA
                    item {
                        SettingsSectionContainer(
                            modifier = Modifier.alpha(enter[3]),
                            headerRes = R.string.header_system_and_data,
                            items = listOf(
                                SettingsRowData(
                                    titleRes = R.string.title_data_management,
                                    subtitleRes = R.string.label_data_management_subtitle,
                                    icon = Lucide.Database,
                                    tile = TileIndigo,
                                    onClick = onDataManagementClick
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_refer_a_friend,
                                    subtitleRes = R.string.label_refer_a_friend_subtitle,
                                    icon = Lucide.UserPlus,
                                    tile = TileViolet,
                                    onClick = onReferAFriendClick
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_share_app,
                                    subtitleRes = R.string.label_share_app_subtitle,
                                    icon = Lucide.Share2,
                                    tile = TileCyan,
                                    onClick = {
                                        val send = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareAppBodies.random())
                                        }
                                        context.startActivity(Intent.createChooser(send, shareAppChooser))
                                    }
                                ),
                                SettingsRowData(
                                    titleRes = R.string.title_about_app,
                                    subtitleRes = R.string.label_about_app_subtitle,
                                    icon = Lucide.Info,
                                    tile = TilePurple,
                                    onClick = onAboutClick
                                )
                            )
                        )
                    }

                    // Isolated Action: Logout (No Card Container) - shown only for signed in users
                    if (!isAnonymous) {
                        item {
                            Box(Modifier.alpha(enter[3])) {
                            SettingsIsolatedLogoutRow(onClick = onLogoutClick)
                            }
                        }
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
    val colorScheme = MaterialTheme.colorScheme
    // The settings screen's own card fill, shared with the sibling screens that join it.
    val sectionCardColors = settingsGroupCardColors()
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(headerRes),
            // One step up from the label scale's 12sp, so the section name reads as a
            // heading above its card; the rest of labelMedium (family, weight, tracking)
            // stays as it was.
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
            // Same third-ink weight as light in both themes so the label stays an aside
            // above the card rather than competing with the brand-gradient icon pucks.
            color = colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 6.dp)
        )

        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            // Both modes take the mockup's own card fill, so the group cards match the
            // hero above them rather than sitting on a tonal container of their own.
            colors = sectionCardColors,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    SettingsRowItemView(data = item)

                    if (index < items.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(colorScheme.background)
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

    // The tile is the mockup's own pair per row; a disabled row keeps the neutral wash it
    // has always had, since the mockup has no disabled state to copy.
    val tileFill = if (isEnabled) data.tile.fill else colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
    val tileInk = if (isEnabled) data.tile.ink else colorScheme.disabled
    val titleColor = if (isEnabled) colorScheme.onSurface else colorScheme.disabled
    val subtitleColor = if (isEnabled) {
        colorScheme.settingsSubtitleInk
    } else {
        colorScheme.disabled
    }

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
        // Leading Icon Tile (40dp, 12dp corners, centered 24dp icon)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = tileFill,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = stringResource(data.titleRes),
                tint = tileInk,
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
                text = data.subtitleText ?: stringResource(data.subtitleRes),
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Trailing countdown (e.g. ad-free pass timer)
        if (data.trailing != null) {
            Text(
                text = data.trailing,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.accentInk,
                maxLines = 1
            )
        }

        if (isEnabled) {
            Spacer(modifier = Modifier.width(4.dp))

            // Trailing Chevron Icon, on the mockup's chevron ink
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = if (colorScheme.isDark) SettingsChevronInkDark else SettingsChevronInkLight,
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
        // Leading Icon Tile, the mockup's sign-out rose
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = TileRose.fill,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = stringResource(R.string.label_logout),
                tint = TileRose.ink,
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
    /** Pre-formatted subtitle (e.g. a duration interpolated from Remote Config); wins over
     *  [subtitleRes] when present. */
    val subtitleText: String? = null,
    val icon: ImageVector,
    /** Leading tile fill and ink, the mockup's hue pair for this row. */
    val tile: SettingsTilePalette,
    val onClick: () -> Unit,
    val isEnabled: Boolean = true,
    val trailing: String? = null
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

private val previewSignedInProfile = defaultUserProfile.copy(
    fullName = "Johnathan Doe",
    emailAddress = "john.doe@example.com",
    gender = "Male",
    authProvider = "google"
)

@Preview(
    name = "Settings Screen Signed In Light",
    showBackground = true,
    widthDp = 412,
    heightDp = 1000
)
@Composable
private fun SettingsScreenSignedInLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        SettingsScreenContent(
            userProfile = previewSignedInProfile,
            userTier = UserTier.PREMIUM
        )
    }
}

@Preview(
    name = "Settings Screen Signed In Dark",
    showBackground = true,
    widthDp = 412,
    heightDp = 1000
)
@Composable
private fun SettingsScreenSignedInDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        SettingsScreenContent(
            userProfile = previewSignedInProfile,
            userTier = UserTier.PREMIUM
        )
    }
}


