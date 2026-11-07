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
internal fun ReminderTimeWindowPickerModal(
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
        containerColor = MaterialTheme.colorScheme.profileSheet,
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
                color = MaterialTheme.colorScheme.accentInk,
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
                color = MaterialTheme.colorScheme.accentInk,
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
                AppTextButton(onClick = onDismissRequest, modifier = Modifier.weight(1f)) {
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
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                            .background(brush = brandGradient()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.btn_confirm),
                            color = MaterialTheme.colorScheme.onCta,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }


    }
}
