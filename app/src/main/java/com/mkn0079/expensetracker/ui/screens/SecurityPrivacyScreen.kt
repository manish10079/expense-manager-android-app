package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_APP_LOCK_TIMEOUT_MINUTES
import com.mkn0079.expensetracker.data.constants.DEFAULT_BIOMETRIC_LOCK_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_BLUR_IN_RECENTS_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_SCREENSHOT_PROTECTION_ENABLED
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.viewmodels.SettingsViewModel
import com.mkn0079.expensetracker.ui.viewmodels.formatAutoLockDurationLabel
import com.mkn0079.expensetracker.ui.components.GatedAction
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.AccessStatus

private val presetAutoLockDurations = listOf(1) + (5..60 step 5).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityPrivacyScreen(
    isAppLockEnabled: Boolean = false,
    hasAppLockPin: Boolean = false,
    isBiometricEnabled: Boolean = DEFAULT_BIOMETRIC_LOCK_ENABLED,
    isBlurInRecentsEnabled: Boolean = DEFAULT_BLUR_IN_RECENTS_ENABLED,
    isScreenshotProtectionEnabled: Boolean = DEFAULT_SCREENSHOT_PROTECTION_ENABLED,
    autoLockDurationMinutes: Int = DEFAULT_APP_LOCK_TIMEOUT_MINUTES,
    onAppLockChange: (Boolean) -> Unit = {},
    onBiometricChange: (Boolean) -> Unit = {},
    onBlurInRecentsChange: (Boolean) -> Unit = {},
    onScreenshotProtectionChange: (Boolean) -> Unit = {},
    onAutoLockDurationChange: (Int) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var isAutoLockDurationPickerVisible by rememberSaveable { mutableStateOf(false) }
    val autoLockDurationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            AppHeader(
                title = "Security & Privacy",
                onBackClick = onBackClick
            )
            Spacer(modifier = Modifier.height(22.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SecurityToggleRow(
                    title = "App Lock",
                    icon = Icons.Filled.Lock,
                    isChecked = isAppLockEnabled,
                    enabled = true,
                    onCheckedChange = onAppLockChange
                )
                SecurityToggleRow(
                    title = "Biometric",
                    icon = Icons.Filled.Security,
                    isChecked = isBiometricEnabled,
                    enabled = isAppLockEnabled && hasAppLockPin,
                    onCheckedChange = onBiometricChange
                )
                GatedAction(
                    feature = Feature.PRIVACY_PROTECTION,
                    onAction = { onBlurInRecentsChange(!isBlurInRecentsEnabled) }
                ) { status, onClick ->
                    SecurityToggleRow(
                        title = "Blur In Recents",
                        icon = Icons.Filled.Security,
                        isChecked = isBlurInRecentsEnabled,
                        enabled = true,
                        isLocked = status !is AccessStatus.Granted,
                        onCheckedChange = { onClick() }
                    )
                }
                GatedAction(
                    feature = Feature.PRIVACY_PROTECTION,
                    onAction = { onScreenshotProtectionChange(!isScreenshotProtectionEnabled) }
                ) { status, onClick ->
                    SecurityToggleRow(
                        title = "Block Screenshots",
                        icon = Icons.Filled.PhotoCamera,
                        isChecked = isScreenshotProtectionEnabled,
                        enabled = true,
                        isLocked = status !is AccessStatus.Granted,
                        onCheckedChange = { onClick() }
                    )
                }
                SecurityItemRow(
                    title = "Auto Lock Duration",
                    icon = Icons.Filled.AccessTime,
                    trailing = formatAutoLockDurationLabel(autoLockDurationMinutes),
                    enabled = isAppLockEnabled,
                    onClick = { isAutoLockDurationPickerVisible = true }
                )
            }
        }
    }

    if (isAutoLockDurationPickerVisible) {
        AutoLockDurationPickerSheet(
            selectedDurationMinutes = autoLockDurationMinutes,
            sheetState = autoLockDurationSheetState,
            onDismiss = { isAutoLockDurationPickerVisible = false },
            onDurationSelected = onAutoLockDurationChange
        )
    }
}

@Composable
private fun SecurityToggleRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    enabled: Boolean,
    isLocked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = enabled) { onCheckedChange(!isChecked) }
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.weight(1f)
            )

            if (isLocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp).padding(end = 8.dp)
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                enabled = enabled && !isLocked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.secondary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                )
            )
        }
    }
}

@Composable
private fun SecurityItemRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: String?,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.weight(1f)
            )

            if (trailing != null) {
                Text(
                    text = trailing,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open \$title",
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoLockDurationPickerSheet(
    selectedDurationMinutes: Int,
    sheetState: SheetState,
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
    var customInputError by rememberSaveable { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Auto Lock Duration",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            item {
                Text(
                    text = "Pick a preset in 5-minute steps up to 60, or enter a custom value in minutes.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                DurationPickerRow(
                    label = "Immediately",
                    subtitle = "Lock as soon as the app moves to the background.",
                    isSelected = selectedDurationMinutes <= 0,
                    onClick = { onDurationSelected(0) }
                )
            }

            items(
                items = presetAutoLockDurations,
                key = { durationMinutes -> durationMinutes }
            ) { durationMinutes ->
                GatedAction(
                    feature = Feature.AUTO_LOCK_SETTING,
                    optionId = durationMinutes.toString(),
                    displayName = "$durationMinutes Minute Lock",
                    onAction = { onDurationSelected(durationMinutes) }
                ) { status, onClick ->
                    DurationPickerRow(
                        label = "$durationMinutes minutes",
                        subtitle = "Require the PIN again after $durationMinutes minutes away from the app.",
                        isSelected = selectedDurationMinutes == durationMinutes,
                        isLocked = status !is AccessStatus.Granted,
                        onClick = onClick
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Custom Duration",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    OutlinedTextField(
                        value = customMinutesInput,
                        onValueChange = { value ->
                            customMinutesInput = value.filter { it.isDigit() }
                            customInputError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = {
                            Text(
                                text = "Enter minutes",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        supportingText = {
                            val helperText = customInputError ?: if (
                                selectedDurationMinutes > 0 &&
                                selectedDurationMinutes !in presetAutoLockDurations
                            ) {
                                "Currently selected: $selectedDurationMinutes min"
                            } else {
                                "Use any positive number of minutes."
                            }
                            Text(
                                text = helperText,
                                color = if (customInputError == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha =  0.65f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    GatedAction(
                        feature = Feature.AUTO_LOCK_SETTING,
                        optionId = "custom",
                        displayName = "Custom Auto-Lock",
                        onAction = { 
                            val customMinutes = customMinutesInput.toIntOrNull()
                            if (customMinutes != null && customMinutes > 0) {
                                onDurationSelected(customMinutes)
                            }
                        }
                    ) { status, onClick ->
                        Button(
                            onClick = {
                                val customMinutes = customMinutesInput.toIntOrNull()
                                if (customMinutes == null || customMinutes <= 0) {
                                    customInputError = "Enter a valid duration in minutes."
                                } else {
                                    onClick() // Triggers gate or runs onAction
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status is AccessStatus.Granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (status !is AccessStatus.Granted) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp).padding(end = 8.dp)
                                    )
                                }
                                Text(
                                    text = "Apply Custom Duration",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DurationPickerRow(
    label: String,
    subtitle: String,
    isSelected: Boolean,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isLocked) Icons.Filled.Lock else Icons.Filled.AccessTime,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.secondary else if (isLocked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isSelected) {
            Text(
                text = "Selected",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
