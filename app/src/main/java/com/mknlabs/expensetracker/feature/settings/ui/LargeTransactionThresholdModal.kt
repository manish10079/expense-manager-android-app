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
internal fun LargeTransactionThresholdModal(
    currentThresholdMinor: Long,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onDismissRequest: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val context = LocalContext.current
    val presetsMajor = listOf(1000L, 5000L, 10000L)
    // Minor units are fixed Ã—100 of major (see MoneyUtils).
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
        containerColor = MaterialTheme.colorScheme.profileSheet,
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
                // Every preset keeps the same card fill; the chosen one wears a 1.5dp accent
                // edge, the way every other picker on these screens marks a single choice.
                val isChosen = isCustom.not() && selectedPresetMinor == presetMinor
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isChosen,
                            role = Role.RadioButton,
                            onClick = {
                                selectedPresetMinor = presetMinor
                                isCustom = false
                                errorMessage = null
                            }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.genderRow,
                    border = if (isChosen) {
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.accentInk)
                    } else {
                        null
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
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isCustom,
                        role = Role.RadioButton,
                        onClick = {
                            isCustom = true
                            errorMessage = null
                        }
                    ),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.genderRow,
                border = if (isCustom) {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.accentInk)
                } else {
                    null
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
                        modifier = Modifier.weight(1f),
                        shape = AppOutlinedFieldDefaults.shape,
                        colors = AppOutlinedFieldDefaults.colors()
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
                },
                modifier = Modifier
                    .fillMaxWidth()
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
