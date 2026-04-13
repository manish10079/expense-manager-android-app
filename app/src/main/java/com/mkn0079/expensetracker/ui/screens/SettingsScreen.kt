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

 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile = defaultUserProfile,
    isDailyReminderEnabled: Boolean = DEFAULT_NOTIFICATIONS_ENABLED,
    isBudgetLimitAlertsEnabled: Boolean = DEFAULT_BUDGET_LIMIT_ALERTS_ENABLED,
    isMissedEntryReminderEnabled: Boolean = DEFAULT_MISSED_ENTRY_REMINDER_ENABLED,
    transactionCount: Int = 0,
    onDailyReminderChange: (Boolean) -> Unit = {},
    onBudgetLimitAlertsChange: (Boolean) -> Unit = {},
    onMissedEntryReminderChange: (Boolean) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onPreferencesClick: () -> Unit = {},
    onSecurityPrivacyClick: () -> Unit = {},
    onTransactionCardCustomizeClick: () -> Unit = {},
    onLegacyImportFileSelected: (Uri) -> Unit = {},
    onDeleteAllTransactionsClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var isDeleteTransactionsDialogVisible by rememberSaveable { mutableStateOf(false) }
    val legacyImportFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let(onLegacyImportFileSelected)
        }
    )
    LaunchedEffect(
        transactionCount
    ) {
        settingsViewModel.updateInputs(
            currentCurrencyId = DEFAULT_CURRENCY_ID,
            currentDateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
            currentTimeFormat = DEFAULT_TIME_FORMAT,
            autoLockDurationMinutes = 0,
            transactionCount = transactionCount
        )
    }
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

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
                            isDailyReminderEnabled = isDailyReminderEnabled,
                            isBudgetLimitAlertsEnabled = isBudgetLimitAlertsEnabled,
                            isMissedEntryReminderEnabled = isMissedEntryReminderEnabled,
                            onItemClick = { actionId ->
                                when (actionId) {
                                    SettingsActionId.Profile -> onProfileClick()
                                    SettingsActionId.AppPreferences -> onPreferencesClick()
                                    SettingsActionId.SecurityPrivacy -> onSecurityPrivacyClick()
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
    isDailyReminderEnabled: Boolean,
    isBudgetLimitAlertsEnabled: Boolean,
    isMissedEntryReminderEnabled: Boolean,
    onItemClick: (SettingsActionId?) -> Unit,
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
                    SettingsToggleId.DailyReminder -> isDailyReminderEnabled
                    SettingsToggleId.BudgetLimitAlerts -> isBudgetLimitAlertsEnabled
                    SettingsToggleId.MissedEntryReminder -> isMissedEntryReminderEnabled
                    else -> null
                }
                val isEnabled = true

                SettingsRow(
                    item = item,
                    enabled = isEnabled,
                    toggleState = toggleState,
                    onClick = {
                        onItemClick(item.actionId)
                    },
                    onToggleChange = { isChecked ->
                        when (item.toggleId) {
                            SettingsToggleId.DailyReminder -> onDailyReminderChange(isChecked)
                            SettingsToggleId.BudgetLimitAlerts -> onBudgetLimitAlertsChange(isChecked)
                            SettingsToggleId.MissedEntryReminder -> onMissedEntryReminderChange(isChecked)
                            null -> Unit
                            else -> Unit
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
