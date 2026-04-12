package com.mkn0079.expensetracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_APP_LOCK_TIMEOUT_MINUTES
import com.mkn0079.expensetracker.data.constants.DEFAULT_BIOMETRIC_LOCK_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_BUDGET_LIMIT_ALERTS_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_BLUR_IN_RECENTS_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.data.constants.DEFAULT_MISSED_ENTRY_REMINDER_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_NOTIFICATIONS_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_SCREENSHOT_PROTECTION_ENABLED
import com.mkn0079.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mkn0079.expensetracker.data.constants.currencyMap
import com.mkn0079.expensetracker.models.Currency
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.models.avatarInitials
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.ui.components.ProfileAvatar
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import com.mkn0079.expensetracker.ui.viewmodels.SettingsActionId
import com.mkn0079.expensetracker.ui.viewmodels.SettingsItemUi
import com.mkn0079.expensetracker.ui.viewmodels.SettingsSectionUi
import com.mkn0079.expensetracker.ui.viewmodels.SettingsToggleId
import com.mkn0079.expensetracker.ui.viewmodels.SettingsViewModel
import com.mkn0079.expensetracker.utils.DateFormatOption
import com.mkn0079.expensetracker.utils.TimeFormatOption
import com.mkn0079.expensetracker.utils.getDateFormatPreviewLabel
import com.mkn0079.expensetracker.utils.getTimeFormatPreviewLabel
import com.mkn0079.expensetracker.utils.supportedDateFormats
import com.mkn0079.expensetracker.utils.supportedTimeFormats

private val presetAutoLockDurations = (5..60 step 5).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile = defaultUserProfile,
    currentCurrencyId: Int = DEFAULT_CURRENCY_ID,
    currentDateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    currentTimeFormat: String = DEFAULT_TIME_FORMAT,
    isAppLockEnabled: Boolean = false,
    hasAppLockPin: Boolean = false,
    isBiometricEnabled: Boolean = DEFAULT_BIOMETRIC_LOCK_ENABLED,
    isBlurInRecentsEnabled: Boolean = DEFAULT_BLUR_IN_RECENTS_ENABLED,
    isScreenshotProtectionEnabled: Boolean = DEFAULT_SCREENSHOT_PROTECTION_ENABLED,
    isDailyReminderEnabled: Boolean = DEFAULT_NOTIFICATIONS_ENABLED,
    isBudgetLimitAlertsEnabled: Boolean = DEFAULT_BUDGET_LIMIT_ALERTS_ENABLED,
    isMissedEntryReminderEnabled: Boolean = DEFAULT_MISSED_ENTRY_REMINDER_ENABLED,
    autoLockDurationMinutes: Int = DEFAULT_APP_LOCK_TIMEOUT_MINUTES,
    transactionCount: Int = 0,
    onCurrencyChange: (Int) -> Unit = {},
    onDateFormatChange: (String) -> Unit = {},
    onTimeFormatChange: (String) -> Unit = {},
    onDailyReminderChange: (Boolean) -> Unit = {},
    onBudgetLimitAlertsChange: (Boolean) -> Unit = {},
    onMissedEntryReminderChange: (Boolean) -> Unit = {},
    onBiometricChange: (Boolean) -> Unit = {},
    onBlurInRecentsChange: (Boolean) -> Unit = {},
    onScreenshotProtectionChange: (Boolean) -> Unit = {},
    onAutoLockDurationChange: (Int) -> Unit = {},
    onAppLockChange: (Boolean) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onManageCategoryClick: () -> Unit = {},
    onTransactionCardCustomizeClick: () -> Unit = {},
    onLegacyImportFileSelected: (Uri) -> Unit = {},
    onDeleteAllTransactionsClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var isCurrencyPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isDateFormatPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isTimeFormatPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isAutoLockDurationPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isDeleteTransactionsDialogVisible by rememberSaveable { mutableStateOf(false) }
    val legacyImportFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let(onLegacyImportFileSelected)
        }
    )
    LaunchedEffect(
        currentCurrencyId,
        currentDateFormatPattern,
        currentTimeFormat,
        autoLockDurationMinutes,
        transactionCount
    ) {
        settingsViewModel.updateInputs(
            currentCurrencyId = currentCurrencyId,
            currentDateFormatPattern = currentDateFormatPattern,
            currentTimeFormat = currentTimeFormat,
            autoLockDurationMinutes = autoLockDurationMinutes,
            transactionCount = transactionCount
        )
    }
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val currencyPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val timeFormatPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val autoLockDurationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        Color(0xFF0B0B0C),
                        BackgroundDark
                    )
                )
            )
    ) {
        TopGlow()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            SettingsHeader(onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(18.dp))

            ProfileHero(
                userProfile = userProfile
            )

            Spacer(modifier = Modifier.height(22.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                uiState.settingsSections.forEach { section ->
                    item(key = section.title) {
                        SettingsSection(
                            section = section,
                            isAppLockEnabled = isAppLockEnabled,
                            hasAppLockPin = hasAppLockPin,
                            isBiometricEnabled = isBiometricEnabled,
                            isBlurInRecentsEnabled = isBlurInRecentsEnabled,
                            isScreenshotProtectionEnabled = isScreenshotProtectionEnabled,
                            isDailyReminderEnabled = isDailyReminderEnabled,
                            isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                            isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                            onItemClick = { actionId ->
                                when (actionId) {
                                    SettingsActionId.Profile -> onProfileClick()
                                    SettingsActionId.CurrencyPicker -> isCurrencyPickerVisible = true
                                    SettingsActionId.DateFormatPicker -> isDateFormatPickerVisible = true
                                    SettingsActionId.TimeFormatPicker -> isTimeFormatPickerVisible = true
                                    SettingsActionId.ManageCategory -> onManageCategoryClick()
                                    SettingsActionId.AutoLockDuration -> isAutoLockDurationPickerVisible = true
                                    SettingsActionId.TransactionCardCustomize -> onTransactionCardCustomizeClick()
                                    SettingsActionId.LegacyImport -> legacyImportFilePicker.launch(
                                        arrayOf(
                                            "application/json",
                                            "text/json",
                                            "text/plain",
                                            "application/octet-stream"
                                        )
                                    )
                                    SettingsActionId.DeleteAllTransactions -> isDeleteTransactionsDialogVisible = true
                                    null -> Unit
                                }
                            },
                            onAppLockChange = onAppLockChange,
                            onBiometricChange = onBiometricChange,
                            onBlurInRecentsChange = onBlurInRecentsChange,
                            onScreenshotProtectionChange = onScreenshotProtectionChange,
                            onAutoLockDurationClick = { isAutoLockDurationPickerVisible = true },
                            onDailyReminderChange = onDailyReminderChange,
                            onBudgetLimitAlertsChange = onBudgetLimitAlertsChange,
                            onMissedEntryReminderChange = onMissedEntryReminderChange
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (isCurrencyPickerVisible) {
        CurrencyPickerSheet(
            searchQuery = uiState.currencySearchQuery,
            filteredCurrencies = uiState.filteredCurrencies,
            selectedCurrencyId = currentCurrencyId,
            sheetState = currencyPickerSheetState,
            onDismiss = {
                settingsViewModel.clearCurrencySearchQuery()
                isCurrencyPickerVisible = false
            },
            onSearchQueryChange = settingsViewModel::updateCurrencySearchQuery,
            onCurrencySelected = { currencyId ->
                onCurrencyChange(currencyId)
                settingsViewModel.clearCurrencySearchQuery()
                isCurrencyPickerVisible = false
            }
        )
    }

    if (isDateFormatPickerVisible) {
        DateFormatPickerSheet(
            selectedPattern = currentDateFormatPattern,
            sheetState = dateFormatPickerSheetState,
            onDismiss = { isDateFormatPickerVisible = false },
            onFormatSelected = { pattern ->
                onDateFormatChange(pattern)
                isDateFormatPickerVisible = false
            }
        )
    }

    if (isTimeFormatPickerVisible) {
        TimeFormatPickerSheet(
            selectedTimeFormat = currentTimeFormat,
            sheetState = timeFormatPickerSheetState,
            onDismiss = { isTimeFormatPickerVisible = false },
            onFormatSelected = { timeFormat ->
                onTimeFormatChange(timeFormat)
                isTimeFormatPickerVisible = false
            }
        )
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

    if (isDeleteTransactionsDialogVisible) {
        AlertDialog(
            onDismissRequest = { isDeleteTransactionsDialogVisible = false },
            containerColor = Color(0xFF18181A),
            title = {
                Text(
                    text = "Delete all transactions?",
                    color = Color(0xFFF2EDF9),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This removes only transactions and their recurring rules. Categories, payment methods, settings, and profile data will stay.",
                    color = Color(0xFFB7AEC8),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleteTransactionsDialogVisible = false
                        onDeleteAllTransactionsClick()
                    }
                ) {
                    Text(
                        text = "Delete All",
                        color = Color(0xFFFFAAA0),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteTransactionsDialogVisible = false }) {
                    Text(
                        text = "Cancel",
                        color = PurpleAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun BoxScope.TopGlow() {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 64.dp)
            .size(width = 240.dp, height = 180.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PurplePrimary.copy(alpha = 0.12f),
                        PurpleAccent.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}

@Composable
private fun SettingsHeader(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            onClick = onBackClick
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Settings",
            color = PurplePrimary,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        )
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.03f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFFE4DBF6),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ProfileHero(
    userProfile: UserProfile
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileAvatar(
            initials = userProfile.avatarInitials(),
            size = 124.dp,
            textSize = 28.sp,
            photoUri = userProfile.photoUri
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = userProfile.fullName,
            color = Color(0xFFF2EDF9),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            PurplePrimary.copy(alpha = 0.95f),
                            PurpleAccent.copy(alpha = 0.90f)
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = "PREMIUM MEMBER",
                color = Color(0xFF271157),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.1.sp
                )
            )
        }
    }
}

@Composable
private fun SettingsSection(
    section: SettingsSectionUi,
    isAppLockEnabled: Boolean,
    hasAppLockPin: Boolean,
    isBiometricEnabled: Boolean,
    isBlurInRecentsEnabled: Boolean,
    isScreenshotProtectionEnabled: Boolean,
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    onItemClick: (SettingsActionId?) -> Unit,
    onAppLockChange: (Boolean) -> Unit,
    onBiometricChange: (Boolean) -> Unit,
    onBlurInRecentsChange: (Boolean) -> Unit,
    onScreenshotProtectionChange: (Boolean) -> Unit,
    onAutoLockDurationClick: () -> Unit,
    onDailyReminderChange: (Boolean) -> Unit,
    onBudgetLimitAlertsChange: (Boolean) -> Unit,
    onMissedEntryReminderChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = section.title,
            color = Color(0xFF6F687C),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.3.sp,
                fontSize = 10.sp
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF18181A))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            section.items.forEach { item ->
                val toggleState = when (item.toggleId) {
                    SettingsToggleId.PinLock -> isAppLockEnabled
                    SettingsToggleId.Biometric -> isBiometricEnabled
                    SettingsToggleId.BlurInRecents -> isBlurInRecentsEnabled
                    SettingsToggleId.ScreenshotProtection -> isScreenshotProtectionEnabled
                    SettingsToggleId.DailyReminder -> isDailyReminderEnabled
                    SettingsToggleId.BudgetLimitAlerts -> isBudgetLimitAlertsEnabled
                    SettingsToggleId.MissedEntryReminder -> isMissedEntryReminderEnabled
                    null -> null
                }
                val isEnabled = when {
                    item.toggleId == SettingsToggleId.Biometric -> isAppLockEnabled && hasAppLockPin
                    item.actionId == SettingsActionId.AutoLockDuration -> isAppLockEnabled
                    else -> true
                }

                SettingsRow(
                    item = item,
                    enabled = isEnabled,
                    toggleState = toggleState,
                    onClick = {
                        if (item.actionId == SettingsActionId.AutoLockDuration) {
                            onAutoLockDurationClick()
                        } else {
                            onItemClick(item.actionId)
                        }
                    },
                    onToggleChange = { isChecked ->
                        when (item.toggleId) {
                            SettingsToggleId.PinLock -> onAppLockChange(isChecked)
                            SettingsToggleId.Biometric -> onBiometricChange(isChecked)
                            SettingsToggleId.BlurInRecents -> onBlurInRecentsChange(isChecked)
                            SettingsToggleId.ScreenshotProtection -> onScreenshotProtectionChange(isChecked)
                            SettingsToggleId.DailyReminder -> onDailyReminderChange(isChecked)
                            SettingsToggleId.BudgetLimitAlerts -> onBudgetLimitAlertsChange(isChecked)
                            SettingsToggleId.MissedEntryReminder -> onMissedEntryReminderChange(isChecked)
                            null -> Unit
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    item: SettingsItemUi,
    enabled: Boolean = true,
    toggleState: Boolean? = null,
    onClick: () -> Unit = {},
    onToggleChange: (Boolean) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = enabled) {
                if (toggleState != null) {
                    onToggleChange(!toggleState)
                } else {
                    onClick()
                }
            }
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (enabled) Color(0xFF232326) else Color(0xFF1D1D20)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (enabled) PurpleAccent else Color(0xFF6F687C),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = item.title,
                color = if (enabled) Color(0xFFF0EBF7) else Color(0xFF7A7386),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                modifier = Modifier.weight(1f)
            )

            item.trailing?.let {
                Text(
                    text = it,
                    color = if (enabled) Color(0xFF898297) else Color(0xFF676272),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))
            }

            if (toggleState != null) {
                Switch(
                    checked = toggleState,
                    onCheckedChange = onToggleChange,
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF24114C),
                        checkedTrackColor = PurpleAccent,
                        uncheckedThumbColor = Color(0xFFDDD6EC),
                        uncheckedTrackColor = Color(0xFF3B3548),
                        uncheckedBorderColor = Color(0xFF3B3548)
                    )
                )
            } else if (item.showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open ${item.title}",
                    tint = if (enabled) Color(0xFF6F687C) else Color(0xFF4F4A59),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    searchQuery: String,
    filteredCurrencies: List<Currency>,
    selectedCurrencyId: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCurrencySelected: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141416),
        scrimColor = Color.Black.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Currency",
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Search by country and pick the currency you want to use across the app.",
                color = Color(0xFF968EA8),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search currency",
                        tint = Color(0xFF9B93AE)
                    )
                },
                placeholder = {
                    Text(
                        text = "Search country",
                        color = Color(0xFF7E778D)
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

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filteredCurrencies,
                    key = { currency -> currency.id }
                ) { currency ->
                    CurrencyPickerRow(
                        currency = currency,
                        isSelected = currency.id == selectedCurrencyId,
                        onClick = { onCurrencySelected(currency.id) }
                    )
                }

                if (filteredCurrencies.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF1A1A1E))
                                .padding(horizontal = 18.dp, vertical = 20.dp)
                        ) {
                            Text(
                                text = "No countries matched your search.",
                                color = Color(0xFF9B93AE),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun CurrencyPickerRow(
    currency: Currency,
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
            Text(
                text = currency.currencySymbol,
                color = if (isSelected) PurpleAccent else Color(0xFFF0EBF7),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.countryName,
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = currency.currencyName,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFormatPickerSheet(
    selectedPattern: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onFormatSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141416),
        scrimColor = Color.Black.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Date Format",
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose the date style you want to see across the app.",
                color = Color(0xFF968EA8),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = supportedDateFormats,
                    key = { option -> option.pattern }
                ) { option ->
                    DateFormatPickerRow(
                        option = option,
                        isSelected = option.pattern == selectedPattern,
                        onClick = { onFormatSelected(option.pattern) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun DateFormatPickerRow(
    option: DateFormatOption,
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
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = option.pattern,
                tint = if (isSelected) PurpleAccent else Color(0xFFF0EBF7),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.previewLabel,
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = option.pattern,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeFormatPickerSheet(
    selectedTimeFormat: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onFormatSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141416),
        scrimColor = Color.Black.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Time Format",
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose whether time is shown in 12-hour or 24-hour style.",
                color = Color(0xFF968EA8),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = supportedTimeFormats,
                    key = { option -> option.id }
                ) { option ->
                    TimeFormatPickerRow(
                        option = option,
                        isSelected = option.id == selectedTimeFormat,
                        onClick = { onFormatSelected(option.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun TimeFormatPickerRow(
    option: TimeFormatOption,
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
                imageVector = Icons.Filled.Tune,
                contentDescription = option.label,
                tint = if (isSelected) PurpleAccent else Color(0xFFF0EBF7),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.label,
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = option.previewLabel,
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
                    label = "$durationMinutes minutes",
                    subtitle = "Require the PIN again after $durationMinutes minutes away from the app.",
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
                                "Currently selected: $selectedDurationMinutes min"
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

@Preview(
    name = "Settings Screen",
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFF0A0A0A,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun SettingsScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        SettingsScreen()
    }
}
