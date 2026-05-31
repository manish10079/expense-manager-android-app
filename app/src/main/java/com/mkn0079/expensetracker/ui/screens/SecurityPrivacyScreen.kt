package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NoPhotography
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.data.constants.DEFAULT_APP_LOCK_TIMEOUT_MINUTES
import com.mkn0079.expensetracker.data.constants.DEFAULT_BIOMETRIC_LOCK_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_BLUR_IN_RECENTS_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_SCRAMBLED_PIN_KEYPAD_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_SCREENSHOT_PROTECTION_ENABLED
import com.mkn0079.expensetracker.models.SettingsItemType
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.AdPlacement
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.FeatureRegistry
import com.mkn0079.expensetracker.ui.components.AdContainer
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.components.GatedAction
import com.mkn0079.expensetracker.ui.components.NativeAdCard
import com.mkn0079.expensetracker.ui.components.SettingsItemCard
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.theme.featureGateLock
import com.mkn0079.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mkn0079.expensetracker.ui.viewmodels.formatAutoLockDurationLabel

private val presetAutoLockDurations = listOf(1) + (5..60 step 5).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityPrivacyScreen(
    isAppLockEnabled: Boolean = false,
    hasAppLockPin: Boolean = false,
    isBiometricEnabled: Boolean = DEFAULT_BIOMETRIC_LOCK_ENABLED,
    isScrambledPinKeypadEnabled: Boolean = DEFAULT_SCRAMBLED_PIN_KEYPAD_ENABLED,
    isBlurInRecentsEnabled: Boolean = DEFAULT_BLUR_IN_RECENTS_ENABLED,
    isScreenshotProtectionEnabled: Boolean = DEFAULT_SCREENSHOT_PROTECTION_ENABLED,
    autoLockDurationMinutes: Int = DEFAULT_APP_LOCK_TIMEOUT_MINUTES,
    onAppLockChange: (Boolean) -> Unit = {},
    onBiometricChange: (Boolean) -> Unit = {},
    onScrambledPinKeypadChange: (Boolean) -> Unit = {},
    onBlurInRecentsChange: (Boolean) -> Unit = {},
    onScreenshotProtectionChange: (Boolean) -> Unit = {},
    onAutoLockDurationChange: (Int) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()

    var isAutoLockDurationPickerVisible by rememberSaveable { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
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
                title = stringResource(R.string.title_security_privacy),
                onBackClick = onBackClick
            )
            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    SettingsItemCard(
                        title = stringResource(R.string.title_app_lock),
                        subtitle = stringResource(R.string.label_secure_app_pin),
                        icon = Icons.Rounded.Lock,
                        type = SettingsItemType.Toggle,
                        isChecked = isAppLockEnabled,
                        onCheckedChange = onAppLockChange
                    )
                }

                item {
                    SettingsItemCard(
                        title = stringResource(R.string.title_biometric),
                        subtitle = stringResource(R.string.label_use_biometric_subtitle),
                        icon = Icons.Rounded.Fingerprint,
                        type = SettingsItemType.Toggle,
                        isEnabled = isAppLockEnabled && hasAppLockPin,
                        isChecked = isBiometricEnabled,
                        onCheckedChange = onBiometricChange
                    )
                }

                item {
                    GatedAction(
                        feature = Feature.SCRAMBLED_PIN_KEYPAD,
                        onAction = { onScrambledPinKeypadChange(!isScrambledPinKeypadEnabled) }
                    ) { status, onClick ->
                        val accessLevel = FeatureRegistry.getAccessLevel(Feature.SCRAMBLED_PIN_KEYPAD)
                        SettingsItemCard(
                            title = stringResource(R.string.label_scrambled_keypad),
                            subtitle = stringResource(R.string.label_scrambled_keypad_subtitle),
                            icon = Icons.Rounded.GridView,
                            type = SettingsItemType.Toggle,
                            accessLevel = accessLevel,
                            isLocked = !isScrambledPinKeypadEnabled && status !is AccessStatus.Granted,
                            isEnabled = isAppLockEnabled && hasAppLockPin,
                            isChecked = isScrambledPinKeypadEnabled,
                            onCheckedChange = { onClick() },
                            onClick = onClick
                        )
                    }
                }

                item {
                    GatedAction(
                        feature = Feature.PRIVACY_PROTECTION,
                        onAction = { onBlurInRecentsChange(!isBlurInRecentsEnabled) }
                    ) { status, onClick ->
                        val accessLevel = FeatureRegistry.getAccessLevel(Feature.PRIVACY_PROTECTION)
                        SettingsItemCard(
                            title = stringResource(R.string.label_blur_in_recents),
                            subtitle = stringResource(R.string.label_blur_in_recents_subtitle),
                            icon = Icons.Rounded.BlurOn,
                            type = SettingsItemType.Toggle,
                            accessLevel = accessLevel,
                            isLocked = !isBlurInRecentsEnabled && status !is AccessStatus.Granted,
                            isChecked = isBlurInRecentsEnabled,
                            onCheckedChange = { onClick() },
                            onClick = onClick
                        )
                    }
                }

                item {
                    GatedAction(
                        feature = Feature.PRIVACY_PROTECTION,
                        onAction = { onScreenshotProtectionChange(!isScreenshotProtectionEnabled) }
                    ) { status, onClick ->
                        val accessLevel = FeatureRegistry.getAccessLevel(Feature.PRIVACY_PROTECTION)
                        SettingsItemCard(
                            title = stringResource(R.string.title_block_screenshots),
                            subtitle = stringResource(R.string.label_block_screenshots_subtitle),
                            icon = Icons.Rounded.NoPhotography,
                            type = SettingsItemType.Toggle,
                            accessLevel = accessLevel,
                            isLocked = !isScreenshotProtectionEnabled && status !is AccessStatus.Granted,
                            isChecked = isScreenshotProtectionEnabled,
                            onCheckedChange = { onClick() },
                            onClick = onClick
                        )
                    }
                }

                item {
                    SettingsItemCard(
                        title = stringResource(R.string.title_auto_lock_duration),
                        subtitle = stringResource(R.string.label_auto_lock_subtitle),
                        icon = Icons.Filled.AccessTime,
                        type = SettingsItemType.Value,
                        valueText = formatAutoLockDurationLabel(autoLockDurationMinutes),
                        isEnabled = isAppLockEnabled,
                        onClick = { isAutoLockDurationPickerVisible = true }
                    )
                }

                item {
                    // Inline Native Ad after Auto Lock Duration
                    AdContainer(isAdsEnabled = isAdsEnabled) {
                        NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                    }
                }
            }
        }
    }

    if (isAutoLockDurationPickerVisible) {
        AutoLockDurationPickerSheet(
            selectedDurationMinutes = autoLockDurationMinutes,
            onDismiss = { isAutoLockDurationPickerVisible = false },
            onDurationSelected = onAutoLockDurationChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoLockDurationPickerSheet(
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
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
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
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
                    val subtitle = if (duration == 0) stringResource(R.string.label_lock_immediately)
                                  else stringResource(R.string.title_require_the_pin_again_after_va, duration)
                    
                    SettingsItemCard(
                        icon = Icons.Filled.AccessTime,
                        title = title,
                        subtitle = subtitle,
                        type = SettingsItemType.Value,
                        isChecked = selectedDurationMinutes == duration,
                        onClick = {
                            onDurationSelected(duration)
                            onDismiss()
                        }
                    )
                }

                // 5, 10, 15 Minutes are AD_SUPPORTED
                val adSupportedDurations = listOf(5, 10, 15)
                items(adSupportedDurations) { duration ->
                    GatedAction(
                        feature = com.mkn0079.expensetracker.monetization.Feature.AUTO_LOCK_SETTING,
                        optionId = duration.toString(),
                        displayName = stringResource(R.string.label_val_minutes_away, duration),
                        onAction = {
                            onDurationSelected(duration)
                            onDismiss()
                        }
                    ) { status, onClick ->
                        val accessLevel = com.mkn0079.expensetracker.monetization.FeatureRegistry.getAccessLevel(com.mkn0079.expensetracker.monetization.Feature.AUTO_LOCK_SETTING, duration.toString())
                        SettingsItemCard(
                            icon = Icons.Filled.AccessTime,
                            title = stringResource(R.string.label_val_minutes_away, duration),
                            subtitle = stringResource(R.string.title_require_the_pin_again_after_va, duration),
                            type = SettingsItemType.Value,
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            isChecked = selectedDurationMinutes == duration,
                            onClick = onClick
                        )
                    }
                }

                // 20+ Minutes are PREMIUM
                val premiumDurations = presetAutoLockDurations.filter { it >= 20 }
                items(premiumDurations) { duration ->
                    GatedAction(
                        feature = com.mkn0079.expensetracker.monetization.Feature.AUTO_LOCK_SETTING,
                        optionId = duration.toString(),
                        displayName = stringResource(R.string.label_val_minutes_away, duration),
                        onAction = {
                            onDurationSelected(duration)
                            onDismiss()
                        }
                    ) { status, onClick ->
                        val accessLevel = com.mkn0079.expensetracker.monetization.FeatureRegistry.getAccessLevel(com.mkn0079.expensetracker.monetization.Feature.AUTO_LOCK_SETTING, duration.toString())
                        SettingsItemCard(
                            icon = Icons.Filled.AccessTime,
                            title = stringResource(R.string.label_val_minutes_away, duration),
                            subtitle = stringResource(R.string.title_require_the_pin_again_after_va, duration),
                            type = SettingsItemType.Value,
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            isChecked = selectedDurationMinutes == duration,
                            onClick = onClick
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Duration is PREMIUM
            GatedAction(
                feature = com.mkn0079.expensetracker.monetization.Feature.AUTO_LOCK_SETTING,
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        if (isLocked) {
                            Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.featureGateLock)
                        } else if (customMinutesInput.isNotEmpty()) {
                            TextButton(onClick = {
                                val mins = customMinutesInput.toIntOrNull() ?: 0
                                if (mins > 0) {
                                    onDurationSelected(mins)
                                    onDismiss()
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
