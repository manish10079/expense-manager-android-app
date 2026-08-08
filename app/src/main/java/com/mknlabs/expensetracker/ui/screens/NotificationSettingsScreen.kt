package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PriceCheck
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.SettingsGroup
import com.mknlabs.expensetracker.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.ui.theme.Dimens

import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun NotificationSettingsScreen(
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    isAdsEnabled: Boolean = false
) {
    NotificationSettingsContent(
        isDailyReminderEnabled = isDailyReminderEnabled,
        isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
        isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
        isAdsEnabled = isAdsEnabled,
        onDailyReminderChange = onDailyReminderChange,
        onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
        onMissedEntryReminderChange = onMissedEntryReminderChange,
        onBackClick = onBackClick
    )
}

@Composable
private fun NotificationSettingsContent(
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    isAdsEnabled: Boolean,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

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
            }

            // Inline Native Ad after Group
            AdContainer(isAdsEnabled = isAdsEnabled) {
                NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
            }

            Spacer(modifier = Modifier.height(32.dp))
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
            isAdsEnabled = false,
            onDailyReminderChange = {},
            onBudgetLimitAlertsChange = {},
            onMissedEntryReminderChange = {},
            onBackClick = {}
        )
    }
}
