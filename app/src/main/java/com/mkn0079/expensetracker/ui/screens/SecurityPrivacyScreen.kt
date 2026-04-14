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
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import com.mkn0079.expensetracker.ui.viewmodels.SettingsViewModel
import com.mkn0079.expensetracker.ui.viewmodels.formatAutoLockDurationLabel

private val presetAutoLockDurations = (5..60 step 5).toList()

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundDark, Color(0xFF0B0B0C), BackgroundDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.03f))
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFE4DBF6),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Security & Privacy",
                    color = PurplePrimary,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF18181A))
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
                SecurityToggleRow(
                    title = "Blur In Recents",
                    icon = Icons.Filled.Security,
                    isChecked = isBlurInRecentsEnabled,
                    enabled = true,
                    onCheckedChange = onBlurInRecentsChange
                )
                SecurityToggleRow(
                    title = "Block Screenshots",
                    icon = Icons.Filled.PhotoCamera,
                    isChecked = isScreenshotProtectionEnabled,
                    enabled = true,
                    onCheckedChange = onScreenshotProtectionChange
                )
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
            onDurationSelected = { minutes ->
                onAutoLockDurationChange(minutes)
                isAutoLockDurationPickerVisible = false
            }
        )
    }
}

@Composable
private fun SecurityToggleRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    enabled: Boolean,
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
                    .background(if (enabled) Color(0xFF232326) else Color(0xFF1D1D20)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (enabled) PurpleAccent else Color(0xFF6F687C),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                color = if (enabled) Color(0xFFF0EBF7) else Color(0xFF7A7386),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF24114C),
                    checkedTrackColor = PurpleAccent,
                    uncheckedThumbColor = Color(0xFFDDD6EC),
                    uncheckedTrackColor = Color(0xFF3B3548),
                    uncheckedBorderColor = Color(0xFF3B3548)
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
                    .background(if (enabled) Color(0xFF232326) else Color(0xFF1D1D20)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (enabled) PurpleAccent else Color(0xFF6F687C),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                color = if (enabled) Color(0xFFF0EBF7) else Color(0xFF7A7386),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.weight(1f)
            )

            if (trailing != null) {
                Text(
                    text = trailing,
                    color = if (enabled) Color(0xFF898297) else Color(0xFF676272),
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
                tint = if (enabled) Color(0xFF6F687C) else Color(0xFF4F4A59),
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
        containerColor = Color(0xFF141416),
        scrimColor = Color.Black.copy(alpha = 0.62f)
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
                    color = Color(0xFFF0EBF7),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            item {
                Text(
                    text = "Pick a preset in 5-minute steps up to 60, or enter a custom value in minutes.",
                    color = Color(0xFF968EA8),
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
                DurationPickerRow(
                    label = "\$durationMinutes minutes",
                    subtitle = "Require the PIN again after \$durationMinutes minutes away from the app.",
                    isSelected = selectedDurationMinutes == durationMinutes,
                    onClick = { onDurationSelected(durationMinutes) }
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1A1A1E))
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Custom Duration",
                        color = Color(0xFFF0EBF7),
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
                                color = Color(0xFF7E778D)
                            )
                        },
                        supportingText = {
                            val helperText = customInputError ?: if (
                                selectedDurationMinutes > 0 &&
                                selectedDurationMinutes !in presetAutoLockDurations
                            ) {
                                "Currently selected: \$selectedDurationMinutes min"
                            } else {
                                "Use any positive number of minutes."
                            }
                            Text(
                                text = helperText,
                                color = if (customInputError == null) Color(0xFF968EA8) else Color(0xFFFFAAA0)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1D1D21),
                            unfocusedContainerColor = Color(0xFF1D1D21),
                            focusedBorderColor = PurpleAccent.copy(alpha = 0.7f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                            focusedTextColor = Color(0xFFF0EBF7),
                            unfocusedTextColor = Color(0xFFF0EBF7),
                            cursorColor = PurpleAccent
                        )
                    )

                    Button(
                        onClick = {
                            val customMinutes = customMinutesInput.toIntOrNull()
                            if (customMinutes == null || customMinutes <= 0) {
                                customInputError = "Enter a valid duration in minutes."
                            } else {
                                onDurationSelected(customMinutes)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurpleAccent,
                            contentColor = Color(0xFF24114C)
                        )
                    ) {
                        Text(
                            text = "Apply Custom Duration",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) {
                    PurplePrimary.copy(alpha = 0.18f)
                } else {
                    Color(0xFF1A1A1E)
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
                        PurpleAccent.copy(alpha = 0.18f)
                    } else {
                        Color(0xFF232326)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccessTime,
                contentDescription = label,
                tint = if (isSelected) PurpleAccent else Color(0xFFF0EBF7),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = Color(0xFF9B93AE),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isSelected) {
            Text(
                text = "Selected",
                color = PurpleAccent,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
