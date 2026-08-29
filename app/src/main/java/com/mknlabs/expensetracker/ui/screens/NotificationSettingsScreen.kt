package com.mknlabs.expensetracker.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PriceCheck
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.ReminderWindow
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.monetization.AccessLevel
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.notifications.NotificationPermissionPrefs
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.SettingsGroup
import com.mknlabs.expensetracker.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.ui.components.WheelDateTimePicker
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.formatTime
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits
import com.mknlabs.expensetracker.utils.validateAndCalculateTimestamp
import java.util.Calendar

import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme

/** ⓘ sheet content for each notification category (spec: one parent per category). */
private enum class NotificationCategoryInfo(val titleRes: Int, val bodyRes: Int) {
    EXPENSE_REMINDERS(R.string.title_expense_reminders, R.string.info_expense_reminders),
    BUDGET_ALERTS(R.string.title_budget_limit_alerts, R.string.info_budget_alerts),
    LARGE_TRANSACTION(R.string.title_large_transaction_alerts, R.string.info_large_transaction),
    WEEKLY_SUMMARY(R.string.title_weekly_summary, R.string.info_weekly_summary),
    FINANCIAL_INSIGHTS(R.string.title_financial_insights, R.string.info_financial_insights),
    SAVINGS_GOALS(R.string.title_goal_reminders, R.string.info_savings_goals),
    RECURRING_ALERTS(R.string.title_recurring_alerts, R.string.info_recurring_alerts),
    CLOUD_SECURITY(R.string.title_cloud_security_alerts, R.string.info_cloud_security)
}

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
private fun NotificationSettingsContent(
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
    // Which category's ⓘ sheet is open (null = closed).
    var infoCategory by remember { mutableStateOf<NotificationCategoryInfo?>(null) }
    // Large-transaction threshold + weekly-summary time pickers.
    var showThresholdPicker by remember { mutableStateOf(false) }
    var showWeeklyTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppHeader(
            title = stringResource(id = R.string.title_notification_settings),
            onBackClick = onBackClick,
            modifier = Modifier.padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = Dimens.HeaderSpacing, bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (!isNotificationsPermissionGranted) {
                NotificationPermissionBanner(
                    hasRequestedPermission = hasRequestedPermission,
                    onEnableClick = onEnableNotificationsClick
                )
            }

            // Free tier (spec categories 1-4): all functional.
            SettingsGroup {
                SettingsItemCard(
                    icon = Icons.Rounded.NotificationsActive,
                    title = stringResource(id = R.string.title_expense_reminders),
                    subtitle = stringResource(id = R.string.desc_expense_reminders),
                    type = SettingsItemType.Toggle,
                    standalone = false,
                    isChecked = isExpenseRemindersEnabled,
                    onCheckedChange = onExpenseRemindersChange,
                    onInfoClick = { infoCategory = NotificationCategoryInfo.EXPENSE_REMINDERS }
                )
                SettingsGroupDivider()
                SettingsItemCard(
                    icon = Icons.Rounded.PriceCheck,
                    title = stringResource(id = R.string.title_budget_limit_alerts),
                    subtitle = stringResource(id = R.string.desc_budget_limit_alerts),
                    type = SettingsItemType.Toggle,
                    standalone = false,
                    isChecked = isBudgetLimitAlertsEnabled,
                    onCheckedChange = onBudgetLimitAlertsChange,
                    onInfoClick = { infoCategory = NotificationCategoryInfo.BUDGET_ALERTS }
                )
                SettingsGroupDivider()
                SettingsItemCard(
                    icon = Icons.Rounded.Payments,
                    title = stringResource(id = R.string.title_large_transaction_alerts),
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
                        icon = Icons.Rounded.Payments,
                        title = stringResource(id = R.string.title_large_transaction_threshold),
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
                    icon = Icons.Rounded.BarChart,
                    title = stringResource(id = R.string.title_weekly_summary),
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
                        icon = Icons.Rounded.BarChart,
                        title = stringResource(id = R.string.title_weekly_summary_time),
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

            // Premium tier (spec categories 5-8): ⭐-locked for Free users.
            SettingsGroup {
                PremiumNotificationCard(
                    icon = Icons.Rounded.AutoAwesome,
                    titleRes = R.string.title_financial_insights,
                    descRes = R.string.desc_financial_insights,
                    isEnabled = isFinancialInsightsEnabled,
                    isPremium = isPremium,
                    onToggle = onFinancialInsightsChange,
                    onInfo = { infoCategory = NotificationCategoryInfo.FINANCIAL_INSIGHTS },
                    onPremiumClick = onPremiumCardClick
                )
                SettingsGroupDivider()
                PremiumNotificationCard(
                    icon = Icons.Rounded.Savings,
                    titleRes = R.string.title_goal_reminders,
                    descRes = R.string.desc_goal_reminders,
                    isEnabled = isGoalRemindersEnabled,
                    isPremium = isPremium,
                    onToggle = onGoalRemindersChange,
                    onInfo = { infoCategory = NotificationCategoryInfo.SAVINGS_GOALS },
                    onPremiumClick = onPremiumCardClick
                )
                SettingsGroupDivider()
                PremiumNotificationCard(
                    icon = Icons.Rounded.EventRepeat,
                    titleRes = R.string.title_recurring_alerts,
                    descRes = R.string.desc_recurring_alerts,
                    isEnabled = isBillRemindersEnabled,
                    isPremium = isPremium,
                    onToggle = onBillRemindersChange,
                    onInfo = { infoCategory = NotificationCategoryInfo.RECURRING_ALERTS },
                    onPremiumClick = onPremiumCardClick
                )
                SettingsGroupDivider()
                PremiumNotificationCard(
                    icon = Icons.Rounded.CloudDone,
                    titleRes = R.string.title_cloud_security_alerts,
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

            // Reminder time windows + test notification (plan §Reminders/Phase 2).
            SettingsGroup {
                SettingsItemCard(
                    icon = Icons.Rounded.WbSunny,
                    title = stringResource(id = R.string.title_morning_window),
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
                    icon = Icons.Rounded.Bedtime,
                    title = stringResource(id = R.string.title_evening_window),
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
                    icon = Icons.Rounded.NotificationsActive,
                    title = stringResource(id = R.string.title_test_notification),
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
            containerColor = MaterialTheme.colorScheme.surface,
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

/** Premium category card: toggle for Pro users, ⭐ lock + upgrade tap for Free. */
@Composable
private fun PremiumNotificationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titleRes: Int,
    descRes: Int,
    isEnabled: Boolean,
    isPremium: Boolean,
    onToggle: (Boolean) -> Unit,
    onInfo: () -> Unit,
    onPremiumClick: () -> Unit
) {
    SettingsItemCard(
        icon = icon,
        title = stringResource(id = titleRes),
        subtitle = stringResource(id = descRes),
        type = SettingsItemType.Toggle,
        standalone = false,
        isLocked = !isPremium,
        accessLevel = AccessLevel.PREMIUM,
        isChecked = isEnabled,
        onCheckedChange = onToggle,
        onInfoClick = onInfo,
        onClick = onPremiumClick
    )
}

@Composable
private fun reminderWindowLabel(
    startHour: Int,
    endHour: Int,
    timeFormat: String
): String {
    val start = remember(startHour, timeFormat) { formatTime(hourToMillis(startHour), timeFormat) }
    val end = remember(endHour, timeFormat) { formatTime(hourToMillis(endHour), timeFormat) }
    return stringResource(id = R.string.notification_window_format, start, end)
}

@Composable
private fun weeklyTimeLabel(timeMillis: Long, timeFormat: String): String {
    val time = remember(timeMillis, timeFormat) { formatTime(timeOfDayToEpochMillis(timeMillis), timeFormat) }
    return stringResource(id = R.string.notification_weekly_time_format, time)
}

private fun hourToMillis(hour: Int): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/**
 * Local epoch timestamp for a millis-of-day time (e.g. 72_000_000 = 8 PM), so
 * it can be passed to epoch-based formatters/pickers. Keeps the weekly-summary
 * millis-of-day storage semantics intact while rendering in the local timezone.
 */
private fun timeOfDayToEpochMillis(timeMillis: Long): Long {
    val hour = (timeMillis / 3_600_000L).toInt().coerceIn(0, 23)
    val minute = ((timeMillis % 3_600_000L) / 60_000L).toInt().coerceIn(0, 59)
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/**
 * Bottom sheet for editing one reminder window's start + end times. Reuses the
 * same wheel picker and validation utilities as the app's other pickers, so the
 * AM/PM handling and locale behavior stay consistent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeWindowPickerModal(
    initialStartHour: Int,
    initialEndHour: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (startHour: Int, endHour: Int) -> Unit
) {
    val context = LocalContext.current
    val labelAm = stringResource(R.string.label_am)
    val labelPm = stringResource(R.string.label_pm)

    // Start wheel state (12-hour wheel values, converted on confirm).
    val startCal = remember(initialStartHour) { Calendar.getInstance().apply { timeInMillis = hourToMillis(initialStartHour) } }
    var startHour12 by remember(initialStartHour) {
        mutableIntStateOf(startCal.get(Calendar.HOUR_OF_DAY).let { if (it % 12 == 0) 12 else it % 12 })
    }
    var startMin by remember(initialStartHour) { mutableIntStateOf(0) }
    var startAmPm by remember(initialStartHour) {
        mutableStateOf(if (startCal.get(Calendar.HOUR_OF_DAY) < 12) labelAm else labelPm)
    }

    // End wheel state.
    val endCal = remember(initialEndHour) { Calendar.getInstance().apply { timeInMillis = hourToMillis(initialEndHour) } }
    var endHour12 by remember(initialEndHour) {
        mutableIntStateOf(endCal.get(Calendar.HOUR_OF_DAY).let { if (it % 12 == 0) 12 else it % 12 })
    }
    var endMin by remember(initialEndHour) { mutableIntStateOf(0) }
    var endAmPm by remember(initialEndHour) {
        mutableStateOf(if (endCal.get(Calendar.HOUR_OF_DAY) < 12) labelAm else labelPm)
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.title_select_reminder_window),
                color = MaterialTheme.colorScheme.onSurface,                    style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.label_starts_at),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
            WheelDateTimePicker(
                initialDateMillis = hourToMillis(initialStartHour),
                showDay = false,
                showMonth = false,
                showDate = false,
                showTime = true,
                onDateChanged = { _, _, _, h, min, ap ->
                    startHour12 = h
                    startMin = min
                    startAmPm = ap
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.label_ends_at),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
            WheelDateTimePicker(
                initialDateMillis = hourToMillis(initialEndHour),
                showDay = false,
                showMonth = false,
                showDate = false,
                showTime = true,
                onDateChanged = { _, _, _, h, min, ap ->
                    endHour12 = h
                    endMin = min
                    endAmPm = ap
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onDismissRequest, modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(id = R.string.btn_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        // Convert the 12-hour wheel values to 24-hour hours via the
                        // same validation utility the app's other pickers use.
                        val startRes = validateAndCalculateTimestamp(
                            day = 1, month = 0, year = 2026,
                            hour = startHour12, minute = startMin, amPm = startAmPm,
                            showDate = false, showTime = true
                        )
                        val endRes = validateAndCalculateTimestamp(
                            day = 1, month = 0, year = 2026,
                            hour = endHour12, minute = endMin, amPm = endAmPm,
                            showDate = false, showTime = true
                        )
                        val start = startRes.timestamp?.let { Calendar.getInstance().apply { timeInMillis = it }.get(Calendar.HOUR_OF_DAY) }
                        val end = endRes.timestamp?.let { Calendar.getInstance().apply { timeInMillis = it }.get(Calendar.HOUR_OF_DAY) }
                        if (start == null || end == null) {
                            errorMessage = context.getString(R.string.error_reminder_window_invalid)
                        } else if (start >= end) {
                            errorMessage = context.getString(R.string.error_reminder_window_invalid)
                        } else {
                            onConfirm(start, end)
                        }
                    },                    modifier = Modifier
                        .weight(1.5f)
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_confirm),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }


    }
}

/** Large-transaction threshold: ₹1k / ₹5k / ₹10k presets + custom amount (spec). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LargeTransactionThresholdModal(
    currentThresholdMinor: Long,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onDismissRequest: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val context = LocalContext.current
    val presetsMajor = listOf(1000L, 5000L, 10000L)
    // Minor units are fixed ×100 of major (see MoneyUtils).
    val presetsMinor = presetsMajor.map { it * 100L }

    var selectedPresetMinor by remember(currentThresholdMinor) {
        mutableStateOf(presetsMinor.firstOrNull { it == currentThresholdMinor })
    }
    var customText by remember { mutableStateOf("") }
    var isCustom by remember(currentThresholdMinor) {
        mutableStateOf(currentThresholdMinor !in presetsMinor)
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(id = R.string.title_large_transaction_threshold),
                color = MaterialTheme.colorScheme.onSurface,                    style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.desc_large_transaction_threshold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            presetsMajor.forEachIndexed { index, presetMajor ->
                val presetMinor = presetsMinor[index]
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCustom.not() && selectedPresetMinor == presetMinor) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    onClick = {
                        selectedPresetMinor = presetMinor
                        isCustom = false
                        errorMessage = null
                    }
                ) {
                    Text(
                        text = formatCurrencyValue(presetMajor.toDouble(), currencyId, amountFormatPreferences),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Custom row.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isCustom) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                onClick = {
                    isCustom = true
                    errorMessage = null
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.label_custom),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it.filter(Char::isDigit).take(9) },
                        singleLine = true,
                        enabled = isCustom,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = {
                            Text(stringResource(id = R.string.placeholder_custom_threshold))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isCustom) {
                        val major = customText.toDoubleOrNull()
                        if (major == null || major <= 0) {
                            errorMessage = context.getString(R.string.error_invalid_threshold)
                            return@Button
                        }
                        onConfirm(major.toMinorUnits())
                    } else {
                        selectedPresetMinor?.let { onConfirm(it) }
                            ?: run { errorMessage = context.getString(R.string.error_invalid_threshold) }
                    }
                },                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.btn_confirm),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }


    }
}

/** Single-time picker for the Sunday weekly summary delivery (spec: Sunday 8 PM default). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklySummaryTimeModal(
    initialTimeMillis: Long,
    onDismissRequest: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val context = LocalContext.current
    val labelAm = stringResource(R.string.label_am)
    val labelPm = stringResource(R.string.label_pm)

    // initialTimeMillis is millis-of-day (e.g. 72_000_000 = 8 PM), NOT an epoch
    // timestamp — derive the 12-hour wheel state from it directly so the picker
    // opens showing the actual stored time in any timezone.
    val initialHour24 = (initialTimeMillis / 3_600_000L).toInt().coerceIn(0, 23)
    val initialMinute = ((initialTimeMillis % 3_600_000L) / 60_000L).toInt().coerceIn(0, 59)
    var hour12 by remember(initialTimeMillis) {
        mutableIntStateOf(if (initialHour24 % 12 == 0) 12 else initialHour24 % 12)
    }
    var minute by remember(initialTimeMillis) { mutableIntStateOf(initialMinute) }
    var amPm by remember(initialTimeMillis) {
        mutableStateOf(if (initialHour24 < 12) labelAm else labelPm)
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.title_weekly_summary_time),
                color = MaterialTheme.colorScheme.onSurface,                    style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.desc_weekly_summary_time),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            WheelDateTimePicker(
                initialDateMillis = timeOfDayToEpochMillis(initialTimeMillis),
                showDay = false,
                showMonth = false,
                showDate = false,
                showTime = true,
                onDateChanged = { _, _, _, h, min, ap ->
                    hour12 = h
                    minute = min
                    amPm = ap
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onDismissRequest, modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(id = R.string.btn_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        val result = validateAndCalculateTimestamp(
                            day = 1, month = 0, year = 2026,
                            hour = hour12, minute = minute, amPm = amPm,
                            showDate = false, showTime = true
                        )
                        val hourOfDay = result.timestamp?.let {
                            Calendar.getInstance().apply { timeInMillis = it }.get(Calendar.HOUR_OF_DAY)
                        }
                        if (hourOfDay == null) {
                            errorMessage = context.getString(R.string.error_reminder_window_invalid)
                        } else {
                            onConfirm(hourOfDay * 3_600_000L + minute * 60_000L)
                        }
                    },                    modifier = Modifier
                        .weight(1.5f)
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_confirm),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }


    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationSettingsScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        NotificationSettingsContent(
            isExpenseRemindersEnabled = true,
            isBudgetLimitAlertsEnabled = true,
            isLargeTransactionAlertsEnabled = true,
            isWeeklySummaryEnabled = true,
            isGoalRemindersEnabled = true,
            isBillRemindersEnabled = true,
            isFinancialInsightsEnabled = true,
            isCloudSecurityEnabled = true,
            largeTransactionThresholdMinor = 500000L,
            weeklySummaryTimeMillis = 72000000L,
            reminderMorningStartHour = 8,
            reminderMorningEndHour = 13,
            reminderEveningStartHour = 17,
            reminderEveningEndHour = 22,
            timeFormat = "12-hour",
            currencyId = 1,
            amountFormatPreferences = com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences,
            isPremium = true,
            isNotificationsPermissionGranted = true,
            hasRequestedPermission = true,
            isAdsEnabled = false,
            onExpenseRemindersChange = {},
            onBudgetLimitAlertsChange = {},
            onLargeTransactionAlertsChange = {},
            onWeeklySummaryChange = {},
            onGoalRemindersChange = {},
            onBillRemindersChange = {},
            onFinancialInsightsChange = {},
            onCloudSecurityChange = {},
            onLargeTransactionThresholdChange = {},
            onWeeklySummaryTimeChange = {},
            onReminderWindowChange = { _, _, _ -> },
            onPremiumCardClick = {},
            onEnableNotificationsClick = {},
            onTestNotification = {},
            onBackClick = {}
        )
    }
}

/**
 * Shown when the app can't post notifications (denied permission on Android
 * 13+, or notifications disabled below). Leads the user to either the system
 * permission dialog (first time) or the app's notification settings.
 */
@Composable
private fun NotificationPermissionBanner(
    hasRequestedPermission: Boolean,
    onEnableClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.errorContainer.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.notification_permission_banner_title),
                    color = colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(id = R.string.notification_permission_banner_desc),
                    color = colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(onClick = onEnableClick) {
                Text(
                    text = stringResource(
                        id = if (hasRequestedPermission) {
                            R.string.btn_open_notification_settings
                        } else {
                            R.string.btn_allow_notifications
                        }
                    ),
                    color = colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
