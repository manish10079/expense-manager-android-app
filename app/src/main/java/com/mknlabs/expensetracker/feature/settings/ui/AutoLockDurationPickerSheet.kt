package com.mknlabs.expensetracker.feature.settings.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CameraSlash
import com.adamglin.phosphoricons.regular.EyeSlash
import com.adamglin.phosphoricons.regular.Fingerprint
import com.adamglin.phosphoricons.regular.GridFour
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.Password
import com.adamglin.phosphoricons.regular.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.data.constants.DEFAULT_APP_LOCK_TIMEOUT_MINUTES
import com.mknlabs.expensetracker.data.constants.DEFAULT_BIOMETRIC_LOCK_ENABLED
import com.mknlabs.expensetracker.data.constants.DEFAULT_BLUR_IN_RECENTS_ENABLED
import com.mknlabs.expensetracker.data.constants.DEFAULT_SCRAMBLED_PIN_KEYPAD_ENABLED
import com.mknlabs.expensetracker.data.constants.DEFAULT_SCREENSHOT_PROTECTION_ENABLED
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.AdPlacement
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.AppCardColors
import com.mknlabs.expensetracker.core.ui.components.dismissSheetThen
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.GatedAction
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.core.ui.components.SettingsGroup
import com.mknlabs.expensetracker.core.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.SettingsTilePalette
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.mknlabs.expensetracker.core.ui.components.input.InputFieldCard
import com.mknlabs.expensetracker.core.ui.components.input.InputType
import com.mknlabs.expensetracker.feature.auth.ui.AuthViewModel
import com.mknlabs.expensetracker.feature.auth.ui.UpdatePasswordState
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme


internal val presetAutoLockDurations = listOf(1) + (5..60 step 5).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AutoLockDurationPickerSheet(
    selectedDurationMinutes: Int,
    onDismiss: () -> Unit,
    onDurationSelected: (Int) -> Unit
) {
    var customMinutesInput by rememberSaveable(selectedDurationMinutes) {
        mutableStateOf(
            if (selectedDurationMinutes > 0 && selectedDurationMinutes !in presetAutoLockDurations) {
                selectedDurationMinutes.toString()
            } else {
                ""
            }
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // Apply the choice first, then let the sheet slide down before it leaves the
    // composition; calling onDismiss straight away would cut the exit animation.
    val applyDuration: (Int) -> Unit = { duration ->
        onDurationSelected(duration)
        dismissSheetThen(scope, sheetState, onDismiss)
    }

    val colorScheme = MaterialTheme.colorScheme
    // The gender picker's own fills: the sheet on the edit profile card colour, and the
    // rows on its #191D2C / #F8FAFC card, #28253D / #F5F3FF once chosen.
    val rowColors = AppCardColors(
        containerColor = colorScheme.genderRow,
        contentColor = colorScheme.onSurface,
        border = null
    )
    // Picked the way every other selection sheet picks: the same card fill as its neighbours,
    // with a 1.5dp accent edge standing in for the old tinted card.
    val selectedRowColors = AppCardColors(
        containerColor = colorScheme.genderRow,
        contentColor = colorScheme.onSurface,
        border = BorderStroke(1.5.dp, colorScheme.accentInk)
    )
    // ...and its leading boxes: a 42dp tile rounded 14dp, surfaceVariant at half strength
    // with onSurface ink, whether or not the row is the chosen one.
    val idleIconTile = SettingsTilePalette(
        fill = colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ink = colorScheme.onSurface
    )
    val iconTileShape = RoundedCornerShape(14.dp)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.profileSheet,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.title_auto_lock_duration),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = stringResource(R.string.label_auto_lock_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Immediate (0) and 1 Minute are FREE
                val freeDurations = listOf(0, 1)
                items(freeDurations) { duration ->
                    val title = if (duration == 0) stringResource(R.string.label_immediately) 
                               else stringResource(R.string.label_val_minutes_away, duration)
                    SettingsItemCard(
                        icon = PhosphorIcons.Regular.Timer,
                        iconTile = idleIconTile,
                        iconTileShape = iconTileShape,
                        iconTileSize = 42.dp,
                        iconTileGlyphSize = 20.dp,
                        title = title,
                        type = SettingsItemType.Value,
                        isChecked = selectedDurationMinutes == duration,
                        isSelected = selectedDurationMinutes == duration,
                        cardColors = if (selectedDurationMinutes == duration) selectedRowColors else rowColors,
                        onClick = {
                            applyDuration(duration)
                        }
                    )
                }

                // 5, 10, 15 Minutes are AD_SUPPORTED
                val adSupportedDurations = listOf(5, 10, 15)
                items(adSupportedDurations) { duration ->
                    GatedAction(
                        feature = com.mknlabs.expensetracker.monetization.Feature.AUTO_LOCK_SETTING,
                        optionId = duration.toString(),
                        displayName = stringResource(R.string.label_val_minutes_away, duration),
                        onAction = {
                            applyDuration(duration)
                        }
                    ) { status, onClick ->
                        val accessLevel = com.mknlabs.expensetracker.monetization.FeatureRegistry.getAccessLevel(com.mknlabs.expensetracker.monetization.Feature.AUTO_LOCK_SETTING, duration.toString())
                        SettingsItemCard(
                            icon = PhosphorIcons.Regular.Timer,
                            iconTile = idleIconTile,
                            iconTileShape = iconTileShape,
                            iconTileSize = 42.dp,
                            iconTileGlyphSize = 20.dp,
                            title = stringResource(R.string.label_val_minutes_away, duration),
                            type = SettingsItemType.Value,
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            isChecked = selectedDurationMinutes == duration,
                            isSelected = selectedDurationMinutes == duration,
                            cardColors = if (selectedDurationMinutes == duration) selectedRowColors else rowColors,
                            onClick = onClick
                        )
                    }
                }

                // 20+ Minutes are PREMIUM
                val premiumDurations = presetAutoLockDurations.filter { it >= 20 }
                items(premiumDurations) { duration ->
                    GatedAction(
                        feature = com.mknlabs.expensetracker.monetization.Feature.AUTO_LOCK_SETTING,
                        optionId = duration.toString(),
                        displayName = stringResource(R.string.label_val_minutes_away, duration),
                        onAction = {
                            applyDuration(duration)
                        }
                    ) { status, onClick ->
                        val accessLevel = com.mknlabs.expensetracker.monetization.FeatureRegistry.getAccessLevel(com.mknlabs.expensetracker.monetization.Feature.AUTO_LOCK_SETTING, duration.toString())
                        SettingsItemCard(
                            icon = PhosphorIcons.Regular.Timer,
                            iconTile = idleIconTile,
                            iconTileShape = iconTileShape,
                            iconTileSize = 42.dp,
                            iconTileGlyphSize = 20.dp,
                            title = stringResource(R.string.label_val_minutes_away, duration),
                            type = SettingsItemType.Value,
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            isChecked = selectedDurationMinutes == duration,
                            isSelected = selectedDurationMinutes == duration,
                            cardColors = if (selectedDurationMinutes == duration) selectedRowColors else rowColors,
                            onClick = onClick
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Duration is PREMIUM
            GatedAction(
                feature = com.mknlabs.expensetracker.monetization.Feature.AUTO_LOCK_SETTING,
                optionId = "custom",
                onAction = {}
            ) { status, onClick ->
                val isLocked = status !is AccessStatus.Granted
                OutlinedTextField(
                    value = customMinutesInput,
                    onValueChange = { input ->
                        if (isLocked) onClick() 
                        else if (input.all { char -> char.isDigit() }) customMinutesInput = input 
                    },
                    label = { Text(stringResource(R.string.label_custom_minutes)) },
                    placeholder = { Text(stringResource(R.string.placeholder_enter_minutes)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    // The focused edge and the typed minutes both take the app's accent,
                    // not the theme's default primary.
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.accentInk,
                        focusedBorderColor = MaterialTheme.colorScheme.accentInk,
                        focusedLabelColor = MaterialTheme.colorScheme.accentInk,
                        focusedTextColor = MaterialTheme.colorScheme.accentInk
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        if (isLocked) {
                            Icon(PhosphorIcons.Regular.Lock, null, tint = MaterialTheme.colorScheme.featureGateLock)
                        } else if (customMinutesInput.isNotEmpty()) {
                            AppTextButton(onClick = {
                                val mins = customMinutesInput.toIntOrNull() ?: 0
                                if (mins > 0) {
                                    applyDuration(mins)
                                }
                            }) {
                                Text(stringResource(R.string.btn_apply))
                            }
                        }
                    }
                )
            }
        }
    }
}

