package com.mknlabs.expensetracker.feature.settings.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.BellRinging
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.CloudCheck
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.Moon
import com.adamglin.phosphoricons.regular.PaperPlaneTilt
import com.adamglin.phosphoricons.regular.PiggyBank
import com.adamglin.phosphoricons.regular.Repeat
import com.adamglin.phosphoricons.regular.Ruler
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Sun
import com.adamglin.phosphoricons.regular.Tag
import com.adamglin.phosphoricons.regular.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.ReminderWindow
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.monetization.AccessLevel
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.notifications.NotificationPermissionPrefs
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.SettingsGroup
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.SettingsTilePalette
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.core.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePicker
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.formatTime
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits
import com.mknlabs.expensetracker.utils.validateAndCalculateTimestamp
import java.util.Calendar

import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    isExpenseRemindersEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isLargeTransactionAlertsEnabled: Boolean,
    isWeeklySummaryEnabled: Boolean,
    isGoalRemindersEnabled: Boolean,
    isBillRemindersEnabled: Boolean,
    isFinancialInsightsEnabled: Boolean,
    isCloudSecurityEnabled: Boolean,
    largeTransactionThresholdMinor: Long,
    weeklySummaryTimeMillis: Long,
    reminderMorningStartHour: Int,
    reminderMorningEndHour: Int,
    reminderEveningStartHour: Int,
    reminderEveningEndHour: Int,
    timeFormat: String,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    isPremium: Boolean,
    onExpenseRemindersChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onLargeTransactionAlertsChange: (Boolean) -> Unit,
    onWeeklySummaryChange: (Boolean) -> Unit,
    onGoalRemindersChange: (Boolean) -> Unit,
    onBillRemindersChange: (Boolean) -> Unit,
    onFinancialInsightsChange: (Boolean) -> Unit,
    onCloudSecurityChange: (Boolean) -> Unit,
    onLargeTransactionThresholdChange: (Long) -> Unit,
    onWeeklySummaryTimeChange: (Long) -> Unit,
    onReminderWindowChange: (ReminderWindow, Int, Int) -> Unit,
    onPremiumCardClick: () -> Unit,
    onTestNotification: () -> Unit,
    onBackClick: () -> Unit,
    isAdsEnabled: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Live permission state so the blocked-banner and the test button react to
    // grants/denials and to returning from the system notification settings.
    var notificationsPermissionGranted by remember {
        mutableStateOf(NotificationHelper.areNotificationsEnabled(context))
    }
    var hasRequestedPermission by remember {
        mutableStateOf(NotificationPermissionPrefs.hasRequested(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        NotificationPermissionPrefs.markRequested(context)
        hasRequestedPermission = true
        notificationsPermissionGranted = NotificationHelper.areNotificationsEnabled(context)
    }

    // Re-check when returning from the system notification settings.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsPermissionGranted = NotificationHelper.areNotificationsEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // First time: show the system permission dialog. After a denial the dialog
    // won't reappear, so jump straight to the app's notification settings.
    val requestOrOpenSettings: () -> Unit = {
        if (!hasRequestedPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NotificationHelper.openAppNotificationSettings(context)
        }
    }

    NotificationSettingsContent(
        isExpenseRemindersEnabled = isExpenseRemindersEnabled,
        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
        isLargeTransactionAlertsEnabled = isLargeTransactionAlertsEnabled,
        isWeeklySummaryEnabled = isWeeklySummaryEnabled,
        isGoalRemindersEnabled = isGoalRemindersEnabled,
        isBillRemindersEnabled = isBillRemindersEnabled,
        isFinancialInsightsEnabled = isFinancialInsightsEnabled,
        isCloudSecurityEnabled = isCloudSecurityEnabled,
        largeTransactionThresholdMinor = largeTransactionThresholdMinor,
        weeklySummaryTimeMillis = weeklySummaryTimeMillis,
        reminderMorningStartHour = reminderMorningStartHour,
        reminderMorningEndHour = reminderMorningEndHour,
        reminderEveningStartHour = reminderEveningStartHour,
        reminderEveningEndHour = reminderEveningEndHour,
        timeFormat = timeFormat,
        currencyId = currencyId,
        amountFormatPreferences = amountFormatPreferences,
        isPremium = isPremium,
        isNotificationsPermissionGranted = notificationsPermissionGranted,
        hasRequestedPermission = hasRequestedPermission,
        isAdsEnabled = isAdsEnabled,
        onExpenseRemindersChange = onExpenseRemindersChange,
        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
        onLargeTransactionAlertsChange = onLargeTransactionAlertsChange,
        onWeeklySummaryChange = onWeeklySummaryChange,
        onGoalRemindersChange = onGoalRemindersChange,
        onBillRemindersChange = onBillRemindersChange,
        onFinancialInsightsChange = onFinancialInsightsChange,
        onCloudSecurityChange = onCloudSecurityChange,
        onLargeTransactionThresholdChange = onLargeTransactionThresholdChange,
        onWeeklySummaryTimeChange = onWeeklySummaryTimeChange,
        onReminderWindowChange = onReminderWindowChange,
        onPremiumCardClick = onPremiumCardClick,
        onEnableNotificationsClick = requestOrOpenSettings,
        onTestNotification = {
            if (notificationsPermissionGranted) {
                onTestNotification()
            } else {
                requestOrOpenSettings()
            }
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationSettingsContent(
    isExpenseRemindersEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isLargeTransactionAlertsEnabled: Boolean,
    isWeeklySummaryEnabled: Boolean,
    isGoalRemindersEnabled: Boolean,
    isBillRemindersEnabled: Boolean,
    isFinancialInsightsEnabled: Boolean,
    isCloudSecurityEnabled: Boolean,
    largeTransactionThresholdMinor: Long,
    weeklySummaryTimeMillis: Long,
    reminderMorningStartHour: Int,
    reminderMorningEndHour: Int,
    reminderEveningStartHour: Int,
    reminderEveningEndHour: Int,
    timeFormat: String,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    isPremium: Boolean,
    isNotificationsPermissionGranted: Boolean,
    hasRequestedPermission: Boolean,
    isAdsEnabled: Boolean,
    onExpenseRemindersChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onLargeTransactionAlertsChange: (Boolean) -> Unit,
    onWeeklySummaryChange: (Boolean) -> Unit,
    onGoalRemindersChange: (Boolean) -> Unit,
    onBillRemindersChange: (Boolean) -> Unit,
    onFinancialInsightsChange: (Boolean) -> Unit,
    onCloudSecurityChange: (Boolean) -> Unit,
    onLargeTransactionThresholdChange: (Long) -> Unit,
    onWeeklySummaryTimeChange: (Long) -> Unit,
    onReminderWindowChange: (ReminderWindow, Int, Int) -> Unit,
    onPremiumCardClick: () -> Unit,
    onEnableNotificationsClick: () -> Unit,
    onTestNotification: () -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    // Which window the time-picker modal is editing (null = modal closed).
    var editingWindow by remember { mutableStateOf<ReminderWindow?>(null) }
    // Which category's â“˜ sheet is open (null = closed).
    var infoCategory by remember { mutableStateOf<NotificationCategoryInfo?>(null) }
    // Large-transaction threshold + weekly-summary time pickers.
    var showThresholdPicker by remember { mutableStateOf(false) }
    var showWeeklyTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val enter = rememberSectionEnterAlphas(2)
        AppHeader(
            title = stringResource(id = R.string.title_notification_settings),
            onBackClick = onBackClick,
            modifier = Modifier.padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = Dimens.HeaderSpacing).alpha(enter[0])
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.ScreenPadding)
                .alpha(enter[1]),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (!isNotificationsPermissionGranted) {
                NotificationPermissionBanner(
                    hasRequestedPermission = hasRequestedPermission,
                    onEnableClick = onEnableNotificationsClick
                )
            }

            // Free tier (spec categories 1-4): all functional.
            SettingsGroup(colors = settingsGroupCardColors()) {
                SettingsItemCard(
                    icon = PhosphorIcons.Regular.BellRinging,
                    title = stringResource(id = R.string.title_expense_reminders),
                    iconTile = SettingsTiles.Violet,
                    subtitle = stringResource(id = R.string.desc_expense_reminders),
                    type = SettingsItemType.Toggle,
                    standalone = false,
                    isChecked = isExpenseRemindersEnabled,
                    onCheckedChange = onExpenseRemindersChange,
                    onInfoClick = { infoCategory = NotificationCategoryInfo.EXPENSE_REMINDERS }
                )
                SettingsGroupDivider()
                SettingsItemCard(
                    icon = PhosphorIcons.Regular.Tag,
                    title = stringResource(id = R.string.title_budget_limit_alerts),
                    iconTile = SettingsTiles.Sky,
                    subtitle = stringResource(id = R.string.desc_budget_limit_alerts),
                    type = SettingsItemType.Toggle,
                    standalone = false,
                    isChecked = isBudgetLimitAlertsEnabled,
                    onCheckedChange = onBudgetLimitAlertsChange,
                    onInfoClick = { infoCategory = NotificationCategoryInfo.BUDGET_ALERTS }
                )
                SettingsGroupDivider()
                SettingsItemCard(
                    icon = PhosphorIcons.Regular.Wallet,
                    title = stringResource(id = R.string.title_large_transaction_alerts),
                    iconTile = SettingsTiles.Emerald,
                    subtitle = stringResource(id = R.string.desc_large_transaction_alerts),
                    type = SettingsItemType.Toggle,
                    standalone = false,
                    isChecked = isLargeTransactionAlertsEnabled,
                    onCheckedChange = onLargeTransactionAlertsChange,
                    onInfoClick = { infoCategory = NotificationCategoryInfo.LARGE_TRANSACTION }
                )
                // Threshold only appears when the category is enabled (spec).
                if (isLargeTransactionAlertsEnabled) {
                    SettingsGroupDivider()
                    SettingsItemCard(
                        icon = PhosphorIcons.Regular.Ruler,
                        title = stringResource(id = R.string.title_large_transaction_threshold),
                    iconTile = SettingsTiles.Teal,
                        subtitle = formatCurrencyValue(
                            largeTransactionThresholdMinor.toMajorUnits(),
                            currencyId,
                            amountFormatPreferences
                        ),
                        type = SettingsItemType.Navigation,
                        standalone = false,
                        onClick = { showThresholdPicker = true }
                    )
                }
                SettingsGroupDivider()
                SettingsItemCard(
                    icon = PhosphorIcons.Regular.ChartBar,
                    title = stringResource(id = R.string.title_weekly_summary),
                    iconTile = SettingsTiles.Fuchsia,
                    subtitle = stringResource(id = R.string.desc_weekly_summary),
                    type = SettingsItemType.Toggle,
                    standalone = false,
                    isChecked = isWeeklySummaryEnabled,
                    onCheckedChange = onWeeklySummaryChange,
                    onInfoClick = { infoCategory = NotificationCategoryInfo.WEEKLY_SUMMARY }
                )
                // Sunday delivery time only appears when the category is enabled (spec).
                if (isWeeklySummaryEnabled) {
                    SettingsGroupDivider()
                    SettingsItemCard(
                        icon = PhosphorIcons.Regular.Clock,
                        title = stringResource(id = R.string.title_weekly_summary_time),
                    iconTile = SettingsTiles.Pink,
                        subtitle = weeklyTimeLabel(weeklySummaryTimeMillis, timeFormat),
                        type = SettingsItemType.Navigation,
                        standalone = false,
                        onClick = { showWeeklyTimePicker = true }
                    )
                }
            }

            AdContainer(isAdsEnabled = isAdsEnabled) {
                NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
            }

            // Premium tier (spec categories 5-8): â­-locked for Free users.
            SettingsGroup(colors = settingsGroupCardColors()) {
                PremiumNotificationCard(
                    icon = PhosphorIcons.Regular.Sparkle,
                    titleRes = R.string.title_financial_insights,
                    iconTile = SettingsTiles.Amber,
                    descRes = R.string.desc_financial_insights,
                    isEnabled = isFinancialInsightsEnabled,
                    isPremium = isPremium,
                    onToggle = onFinancialInsightsChange,
                    onInfo = { infoCategory = NotificationCategoryInfo.FINANCIAL_INSIGHTS },
                    onPremiumClick = onPremiumCardClick
                )
                SettingsGroupDivider()
                PremiumNotificationCard(
                    icon = PhosphorIcons.Regular.PiggyBank,
                    titleRes = R.string.title_goal_reminders,
                    iconTile = SettingsTiles.Indigo,
                    descRes = R.string.desc_goal_reminders,
                    isEnabled = isGoalRemindersEnabled,
                    isPremium = isPremium,
                    onToggle = onGoalRemindersChange,
                    onInfo = { infoCategory = NotificationCategoryInfo.SAVINGS_GOALS },
                    onPremiumClick = onPremiumCardClick
                )
                SettingsGroupDivider()
                PremiumNotificationCard(
                    icon = PhosphorIcons.Regular.Repeat,
                    titleRes = R.string.title_recurring_alerts,
                    iconTile = SettingsTiles.Purple,
                    descRes = R.string.desc_recurring_alerts,
                    isEnabled = isBillRemindersEnabled,
                    isPremium = isPremium,
                    onToggle = onBillRemindersChange,
                    onInfo = { infoCategory = NotificationCategoryInfo.RECURRING_ALERTS },
                    onPremiumClick = onPremiumCardClick
                )
                SettingsGroupDivider()
                PremiumNotificationCard(
                    icon = PhosphorIcons.Regular.CloudCheck,
                    titleRes = R.string.title_cloud_security_alerts,
                    iconTile = SettingsTiles.Blue,
                    descRes = R.string.desc_cloud_security_alerts,
                    isEnabled = isCloudSecurityEnabled,
                    isPremium = isPremium,
                    onToggle = onCloudSecurityChange,
                    onInfo = { infoCategory = NotificationCategoryInfo.CLOUD_SECURITY },
                    onPremiumClick = onPremiumCardClick
                )
            }

            AdContainer(isAdsEnabled = isAdsEnabled) {
                NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
            }

            // Reminder time windows + test notification (plan Â§Reminders/Phase 2).
            SettingsGroup(colors = settingsGroupCardColors()) {
                SettingsItemCard(
                    icon = PhosphorIcons.Regular.Sun,
                    title = stringResource(id = R.string.title_morning_window),
                    iconTile = SettingsTiles.Cyan,
                    subtitle = reminderWindowLabel(
                        startHour = reminderMorningStartHour,
                        endHour = reminderMorningEndHour,
                        timeFormat = timeFormat
                    ),
                    type = SettingsItemType.Navigation,
                    standalone = false,
                    onClick = { editingWindow = ReminderWindow.MORNING }
                )
                SettingsGroupDivider()
                SettingsItemCard(
                    icon = PhosphorIcons.Regular.Moon,
                    title = stringResource(id = R.string.title_evening_window),
                    iconTile = SettingsTiles.Rose,
                    subtitle = reminderWindowLabel(
                        startHour = reminderEveningStartHour,
                        endHour = reminderEveningEndHour,
                        timeFormat = timeFormat
                    ),
                    type = SettingsItemType.Navigation,
                    standalone = false,
                    onClick = { editingWindow = ReminderWindow.EVENING }
                )
                SettingsGroupDivider()
                SettingsItemCard(
                    icon = PhosphorIcons.Regular.PaperPlaneTilt,
                    title = stringResource(id = R.string.title_test_notification),
                    iconTile = SettingsTiles.Violet,
                    subtitle = stringResource(id = R.string.desc_test_notification),
                    type = SettingsItemType.Button,
                    valueText = stringResource(id = R.string.btn_send_test_notification),
                    standalone = false,
                    onClick = onTestNotification
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    editingWindow?.let { window ->
        val startHour = when (window) {
            ReminderWindow.MORNING -> reminderMorningStartHour
            ReminderWindow.EVENING -> reminderEveningStartHour
        }
        val endHour = when (window) {
            ReminderWindow.MORNING -> reminderMorningEndHour
            ReminderWindow.EVENING -> reminderEveningEndHour
        }
        ReminderTimeWindowPickerModal(
            initialStartHour = startHour,
            initialEndHour = endHour,
            onDismissRequest = { editingWindow = null },
            onConfirm = { newStart, newEnd ->
                onReminderWindowChange(window, newStart, newEnd)
                editingWindow = null
            }
        )
    }

    infoCategory?.let { category ->
        ModalBottomSheet(
            onDismissRequest = { infoCategory = null },
            containerColor = MaterialTheme.colorScheme.profileSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = stringResource(id = category.titleRes),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = category.bodyRes),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
    }

    if (showThresholdPicker) {
        LargeTransactionThresholdModal(
            currentThresholdMinor = largeTransactionThresholdMinor,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            onDismissRequest = { showThresholdPicker = false },
            onConfirm = { newThresholdMinor ->
                onLargeTransactionThresholdChange(newThresholdMinor)
                showThresholdPicker = false
            }
        )
    }

    if (showWeeklyTimePicker) {
        WeeklySummaryTimeModal(
            initialTimeMillis = weeklySummaryTimeMillis,
            onDismissRequest = { showWeeklyTimePicker = false },
            onConfirm = { newTimeMillis ->
                onWeeklySummaryTimeChange(newTimeMillis)
                showWeeklyTimePicker = false
            }
        )
    }
}

/** Premium category card: toggle for Pro users, â­ lock + upgrade tap for Free. */

