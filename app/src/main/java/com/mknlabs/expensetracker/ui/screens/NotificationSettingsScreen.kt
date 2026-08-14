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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PriceCheck
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.ReminderWindow
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.notifications.NotificationPermissionPrefs
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.SettingsGroup
import com.mknlabs.expensetracker.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.ui.components.WheelDateTimePicker
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.utils.formatTime
import com.mknlabs.expensetracker.utils.validateAndCalculateTimestamp
import java.util.Calendar

import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun NotificationSettingsScreen(
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    isGoalRemindersEnabled: Boolean,
    reminderMorningStartHour: Int,
    reminderMorningEndHour: Int,
    reminderEveningStartHour: Int,
    reminderEveningEndHour: Int,
    timeFormat: String,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit,
    onGoalRemindersChange: (Boolean) -> Unit,
    onReminderWindowChange: (ReminderWindow, Int, Int) -> Unit,
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
        isDailyReminderEnabled = isDailyReminderEnabled,
        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
        isGoalRemindersEnabled = isGoalRemindersEnabled,
        reminderMorningStartHour = reminderMorningStartHour,
        reminderMorningEndHour = reminderMorningEndHour,
        reminderEveningStartHour = reminderEveningStartHour,
        reminderEveningEndHour = reminderEveningEndHour,
        timeFormat = timeFormat,
        isNotificationsPermissionGranted = notificationsPermissionGranted,
        hasRequestedPermission = hasRequestedPermission,
        isAdsEnabled = isAdsEnabled,
        onDailyReminderChange = onDailyReminderChange,
        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
        onMissedEntryReminderChange = onMissedEntryReminderChange,
        onGoalRemindersChange = onGoalRemindersChange,
        onReminderWindowChange = onReminderWindowChange,
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

@Composable
private fun NotificationSettingsContent(
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    isGoalRemindersEnabled: Boolean,
    reminderMorningStartHour: Int,
    reminderMorningEndHour: Int,
    reminderEveningStartHour: Int,
    reminderEveningEndHour: Int,
    timeFormat: String,
    isNotificationsPermissionGranted: Boolean,
    hasRequestedPermission: Boolean,
    isAdsEnabled: Boolean,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit,
    onGoalRemindersChange: (Boolean) -> Unit,
    onReminderWindowChange: (ReminderWindow, Int, Int) -> Unit,
    onEnableNotificationsClick: () -> Unit,
    onTestNotification: () -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    // Which window the time-picker modal is editing (null = modal closed).
    var editingWindow by remember { mutableStateOf<ReminderWindow?>(null) }

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

            SettingsGroup {
                SettingsItemCard(
                    icon = Icons.Rounded.NotificationsActive,
                    title = stringResource(id = R.string.title_daily_reminder),
                    subtitle = stringResource(id = R.string.desc_daily_reminder),
                    type = SettingsItemType.Toggle,
                    standalone = false,
                    isChecked = isDailyReminderEnabled,
                    onCheckedChange = onDailyReminderChange
                )
                SettingsGroupDivider()
                SettingsItemCard(
                    icon = Icons.Rounded.PriceCheck,
                    title = stringResource(id = R.string.title_budget_limit_alerts),
                    subtitle = stringResource(id = R.string.desc_budget_limit_alerts),
                    type = SettingsItemType.Toggle,
                    standalone = false,
                    isChecked = isBudgetLimitAlertsEnabled,
                    onCheckedChange = onBudgetLimitAlertsChange
                )
                SettingsGroupDivider()
                SettingsItemCard(
                    icon = Icons.Rounded.History,
                    title = stringResource(id = R.string.title_missed_entry_reminder),
                    subtitle = stringResource(id = R.string.desc_missed_entry_reminder),
                    type = SettingsItemType.Toggle,
                    standalone = false,
                    isChecked = isMissedEntryReminderEnabled,
                    onCheckedChange = onMissedEntryReminderChange
                )
                SettingsGroupDivider()
                SettingsItemCard(
                    icon = Icons.Rounded.Savings,
                    title = stringResource(id = R.string.title_goal_reminders),
                    subtitle = stringResource(id = R.string.desc_goal_reminders),
                    type = SettingsItemType.Toggle,
                    standalone = false,
                    isChecked = isGoalRemindersEnabled,
                    onCheckedChange = onGoalRemindersChange
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

            // Inline Native Ad after Group
            AdContainer(isAdsEnabled = isAdsEnabled) {
                NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
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

private fun hourToMillis(hour: Int): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
        set(Calendar.MINUTE, 0)
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
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.label_starts_at),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
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
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
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
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
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
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(54.dp),
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
            isDailyReminderEnabled = true,
            isBudgetLimitAlertsEnabled = false,
            isMissedEntryReminderEnabled = true,
            isGoalRemindersEnabled = true,
            reminderMorningStartHour = 8,
            reminderMorningEndHour = 13,
            reminderEveningStartHour = 17,
            reminderEveningEndHour = 22,
            timeFormat = "12-hour",
            isNotificationsPermissionGranted = true,
            hasRequestedPermission = true,
            isAdsEnabled = false,
            onDailyReminderChange = {},
            onBudgetLimitAlertsChange = {},
            onMissedEntryReminderChange = {},
            onGoalRemindersChange = {},
            onReminderWindowChange = { _, _, _ -> },
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
