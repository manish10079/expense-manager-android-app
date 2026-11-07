package com.mknlabs.expensetracker.feature.home.ui

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.track

import android.content.res.Configuration
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.CheckCircle
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.ChatCircle
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Gear
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.PiggyBank
import com.adamglin.phosphoricons.regular.X
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.avatarInitials
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.models.hasPhoneNumber
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.core.ui.components.*
import com.mknlabs.expensetracker.core.ui.HomeFirstRunPromptGate
import com.mknlabs.expensetracker.core.ui.components.LocalSplashGone
import com.mknlabs.expensetracker.core.ui.components.MissingPermissionsBottomSheet
import com.mknlabs.expensetracker.core.ui.components.RateAppBottomSheet
import com.mknlabs.expensetracker.core.ui.components.ReferOfferBottomSheet
import com.mknlabs.expensetracker.data.local.ReferPromptStore
import com.mknlabs.expensetracker.data.local.RateAppPromptStore
import com.mknlabs.expensetracker.data.local.SmsInboxStaleRemindStore
import com.mknlabs.expensetracker.utils.RateAppPrompt
import com.mknlabs.expensetracker.utils.SmsInboxStaleRemind
import kotlinx.coroutines.delay
import com.mknlabs.expensetracker.utils.LaunchSheetPick
import kotlinx.coroutines.launch
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.NavOnDark
import com.mknlabs.expensetracker.core.ui.theme.NavOnLight
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsSetupUiState
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsSetupViewModel
import com.mknlabs.expensetracker.utils.DeviceVendorUtils
import com.mknlabs.expensetracker.utils.findFragmentActivity
import com.mknlabs.expensetracker.utils.PermissionNudge
import com.mknlabs.expensetracker.data.local.PermissionNudgeState
import com.mknlabs.expensetracker.data.local.PermissionNudgeStore
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.AdPlacement


@Composable
internal fun HomeTopSection(
    userProfile: UserProfile,
    uiState: HomeScreenUiState,
    appSettings: com.mknlabs.expensetracker.models.AppSettings? = null,
    isAdsEnabled: Boolean = false,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSmsInboxClick: () -> Unit = {},
    onCalculatorClick: () -> Unit = {},
    onTodaySpendingClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onToggleBalanceVisibility: () -> Unit,
    smsSetupUiState: SmsSetupUiState = SmsSetupUiState(),
    showMissingPermissionsCard: Boolean = false,
    missingNotifications: Boolean = false,
    missingSms: Boolean = false,
    onMissingPermissionsOpenSettings: () -> Unit = {},
    onMissingPermissionsDismiss: () -> Unit = {},
    onSmsPermissionCardOpenSettings: () -> Unit = {},
    onSmsPermissionCardDismiss: () -> Unit = {},
    onMiuiSetupCardOpenAppSettings: () -> Unit = {},
    onMiuiSetupCardBatterySettings: () -> Unit = {},
    onMiuiSetupCardDismiss: () -> Unit = {},
    onPeriodChanged: (CashFlowPeriod) -> Unit = {},
    isLockOverlayActive: Boolean = false,
    isWide: Boolean = false,
    headerEnter: Float = 1f,
    cashFlowEnter: Float = 1f,
    miniCardsEnter: Float = 1f
) {
    Box(modifier = Modifier.alpha(headerEnter)) {
        HomeHeaderRow(
            userProfile = userProfile,
            uiState = uiState,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick,
            onSmsInboxClick = onSmsInboxClick,
        onCalculatorClick = onCalculatorClick,
            onCalendarViewClick = onTodaySpendingClick,
            isLockOverlayActive = isLockOverlayActive
        )
    }
    // Tighter than the 7.dp gap below the Cash Flow card: the header row above it is
    // text and icons with no container of its own, so the card reads better sitting
    // close to it than spaced away.
    Spacer(modifier = Modifier.height(4.dp))
    HomeStatsSection(
        userProfile = userProfile,
        appSettings = appSettings,
        uiState = uiState,
        isAdsEnabled = isAdsEnabled,
        onProfileClick = onProfileClick,
        onTodaySpendingClick = onTodaySpendingClick,
        onGoalsClick = onGoalsClick,
        onToggleBalanceVisibility = onToggleBalanceVisibility,
        smsSetupUiState = smsSetupUiState,
        showMissingPermissionsCard = showMissingPermissionsCard,
        missingNotifications = missingNotifications,
        missingSms = missingSms,
        onMissingPermissionsOpenSettings = onMissingPermissionsOpenSettings,
        onMissingPermissionsDismiss = onMissingPermissionsDismiss,
        onSmsPermissionCardOpenSettings = onSmsPermissionCardOpenSettings,
        onSmsPermissionCardDismiss = onSmsPermissionCardDismiss,
        onMiuiSetupCardOpenAppSettings = onMiuiSetupCardOpenAppSettings,
        onMiuiSetupCardBatterySettings = onMiuiSetupCardBatterySettings,
        onMiuiSetupCardDismiss = onMiuiSetupCardDismiss,
        onPeriodChanged = onPeriodChanged,
        isWide = isWide,
        cashFlowEnter = cashFlowEnter,
        miniCardsEnter = miniCardsEnter
    )
}

/**
 * Full-width greeting row. Compact (portrait): left-aligned greeting with
 * settings + avatar on the right. Two-pane (landscape/wide): the greeting
 * (Hi user 👋) with settings + profile avatar on the far RIGHT, and the
 * recent-activities list rendering below the row.
 */
