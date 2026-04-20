package com.mkn0079.expensetracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.viewmodels.SettingsActionId
import com.mkn0079.expensetracker.ui.viewmodels.SettingsItemUi
import com.mkn0079.expensetracker.ui.viewmodels.SettingsSectionUi
import java.time.LocalDate

private val presetAutoBackupFrequencies = listOf(7, 15, 30)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    transactionCount: Int,
    isAutoBackupEnabled: Boolean,
    autoBackupFrequencyDays: Int,
    onAutoBackupEnabledChange: (Boolean) -> Unit,
    onAutoBackupFrequencyChange: (Int) -> Unit,
    onDatabaseBackupFileSelected: (Uri) -> Unit,
    onDatabaseRestoreFileSelected: (Uri) -> Unit,
    onJsonExportFileSelected: (Uri) -> Unit,
    onJsonImportFileSelected: (Uri) -> Unit,
    onLegacyImportFileSelected: (Uri) -> Unit,
    onDeleteAllTransactionsClick: () -> Unit,
    onPrepareForExternalActivity: () -> Unit,
    onBackClick: () -> Unit
) {
    var isDeleteTransactionsDialogVisible by rememberSaveable { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    val todayLabel = remember { LocalDate.now().toString() }
    
    var isFrequencyPickerVisible by rememberSaveable { mutableStateOf(false) }
    val frequencySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val databaseBackupFileCreator = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { onDatabaseBackupFileSelected(it) }
    }

    val databaseRestoreFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        pendingRestoreUri = uri
    }

    val jsonExportFileCreator = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { onJsonExportFileSelected(it) }
    }

    val jsonImportFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onJsonImportFileSelected(it) }
    }

    val legacyImportFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onLegacyImportFileSelected(it) }
    }

    val sections = listOf(
        SettingsSectionUi(
            title = "DASHBOARD",
            items = listOf(
                SettingsItemUi(
                    title = "Transaction Count",
                    icon = Icons.Filled.Info,
                    trailing = transactionCount.toString(),
                    showChevron = false
                )
            )
        ),
        SettingsSectionUi(
            title = "DATABASE",
            items = listOf(
                SettingsItemUi(
                    title = "Backup",
                    icon = Icons.Filled.Sync,
                    trailing = ".db",
                    actionId = SettingsActionId.DatabaseBackup
                ),
                SettingsItemUi(
                    title = "Restore",
                    icon = Icons.Filled.Refresh,
                    trailing = ".db",
                    actionId = SettingsActionId.DatabaseRestore
                )
            )
        ),
        SettingsSectionUi(
            title = "JSON TRANSFER",
            items = listOf(
                SettingsItemUi(
                    title = "Export JSON",
                    icon = Icons.Filled.SettingsApplications,
                    trailing = "Merge-safe",
                    actionId = SettingsActionId.JsonExport
                ),
                SettingsItemUi(
                    title = "Import JSON",
                    icon = Icons.Filled.Refresh,
                    trailing = "Add missing",
                    actionId = SettingsActionId.JsonImport
                ),
                SettingsItemUi(
                    title = "Import Legacy Data",
                    icon = Icons.Filled.Refresh,
                    trailing = "JSON",
                    actionId = SettingsActionId.LegacyImport
                )
            )
        ),
        SettingsSectionUi(
            title = "MAINTENANCE",
            items = listOf(
                SettingsItemUi(
                    title = "Delete All Transactions",
                    icon = Icons.Filled.Delete,
                    trailing = "Only transactions",
                    actionId = SettingsActionId.DeleteAllTransactions
                )
            )
        )
    )

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

            AppHeader(
                title = "Data Management",
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item(key = "auto_backup_section") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFF18181A))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DataManagementToggleRow(
                            title = "Auto Backup",
                            icon = Icons.Filled.Sync,
                            isChecked = isAutoBackupEnabled,
                            onCheckedChange = onAutoBackupEnabledChange
                        )
                        DataManagementItemRow(
                            title = "Backup Frequency",
                            icon = Icons.Filled.AccessTime,
                            trailing = "${autoBackupFrequencyDays} days",
                            enabled = isAutoBackupEnabled,
                            onClick = { isFrequencyPickerVisible = true }
                        )
                    }
                }
                
                sections.forEach { section ->
                    item(key = section.title) {
                        DataManagementSection(
                            section = section,
                            onItemClick = { actionId ->
                                when (actionId) {
                                    SettingsActionId.DatabaseBackup -> {
                                        onPrepareForExternalActivity()
                                        databaseBackupFileCreator.launch(
                                            "expense_tracker_backup_$todayLabel.db"
                                        )
                                    }
                                    SettingsActionId.DatabaseRestore -> {
                                        onPrepareForExternalActivity()
                                        databaseRestoreFilePicker.launch(
                                            arrayOf(
                                                "application/octet-stream",
                                                "application/x-sqlite3",
                                                "application/vnd.sqlite3",
                                                "*/*"
                                            )
                                        )
                                    }
                                    SettingsActionId.JsonExport -> {
                                        onPrepareForExternalActivity()
                                        jsonExportFileCreator.launch(
                                            "expense_tracker_export_$todayLabel.json"
                                        )
                                    }
                                    SettingsActionId.JsonImport -> {
                                        onPrepareForExternalActivity()
                                        jsonImportFilePicker.launch(
                                            arrayOf(
                                                "application/json",
                                                "text/json",
                                                "text/plain",
                                                "application/octet-stream"
                                            )
                                        )
                                    }
                                    SettingsActionId.LegacyImport -> {
                                        onPrepareForExternalActivity()
                                        legacyImportFilePicker.launch(
                                            arrayOf(
                                                "application/json",
                                                "text/json",
                                                "text/plain",
                                                "application/octet-stream"
                                            )
                                        )
                                    }
                                    SettingsActionId.DeleteAllTransactions -> isDeleteTransactionsDialogVisible = true
                                    else -> Unit
                                }
                            }
                        )
                    }
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

    pendingRestoreUri?.let { selectedUri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            containerColor = Color(0xFF18181A),
            title = {
                Text(
                    text = "Restore database?",
                    color = Color(0xFFF2EDF9),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will overwrite your current database with the selected .db backup. It will not merge missing records. Existing database data will be replaced.",
                    color = Color(0xFFB7AEC8),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingRestoreUri = null
                        onDatabaseRestoreFileSelected(selectedUri)
                    }
                ) {
                    Text(
                        text = "Restore",
                        color = Color(0xFFFFAAA0),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) {
                    Text(
                        text = "Cancel",
                        color = PurpleAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
    
    if (isFrequencyPickerVisible) {
        AutoBackupFrequencyPickerSheet(
            selectedFrequencyDays = autoBackupFrequencyDays,
            sheetState = frequencySheetState,
            onDismiss = { isFrequencyPickerVisible = false },
            onFrequencySelected = { days ->
                onAutoBackupFrequencyChange(days)
                isFrequencyPickerVisible = false
            }
        )
    }
}


@Composable
private fun DataManagementSection(
    section: SettingsSectionUi,
    onItemClick: (SettingsActionId?) -> Unit
) {
    Column {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF18181A))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            section.items.forEach { item ->
                DataManagementRow(
                    item = item,
                    onClick = { onItemClick(item.actionId) }
                )
            }
        }
    }
}

@Composable
private fun DataManagementToggleRow(
    title: String,
    icon: ImageVector,
    isChecked: Boolean,
    enabled: Boolean = true,
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
private fun DataManagementItemRow(
    title: String,
    icon: ImageVector,
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
                contentDescription = null,
                tint = if (enabled) Color(0xFF6F687C) else Color(0xFF4F4A59),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DataManagementRow(
    item: SettingsItemUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF222224)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = PurpleAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = item.title,
            color = Color(0xFFEBE6F5),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.weight(1f)
        )

        if (item.trailing != null) {
            Text(
                text = item.trailing,
                color = Color(0xFF8D8699),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
            if (item.showChevron) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        if (item.showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF534F5C),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoBackupFrequencyPickerSheet(
    selectedFrequencyDays: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onFrequencySelected: (Int) -> Unit
) {
    var customDaysInput by rememberSaveable(selectedFrequencyDays) {
        mutableStateOf(
            if (selectedFrequencyDays > 0 && selectedFrequencyDays !in presetAutoBackupFrequencies) {
                selectedFrequencyDays.toString()
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
                    text = "Backup Frequency",
                    color = Color(0xFFF0EBF7),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            item {
                Text(
                    text = "Pick a preset or enter a custom value between 1 and 30 days.",
                    color = Color(0xFF968EA8),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(
                items = presetAutoBackupFrequencies,
                key = { days -> days }
            ) { days ->
                DurationPickerRow(
                    label = "$days days",
                    subtitle = "Automated backup every $days days.",
                    isSelected = selectedFrequencyDays == days,
                    onClick = { onFrequencySelected(days) }
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
                        text = "Custom Frequency",
                        color = Color(0xFFF0EBF7),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    OutlinedTextField(
                        value = customDaysInput,
                        onValueChange = { value ->
                            customDaysInput = value.filter { it.isDigit() }
                            customInputError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = {
                            Text(
                                text = "Enter days",
                                color = Color(0xFF7E778D)
                            )
                        },
                        supportingText = {
                            val helperText = customInputError ?: if (
                                selectedFrequencyDays > 0 &&
                                selectedFrequencyDays !in presetAutoBackupFrequencies
                            ) {
                                "Currently selected: $selectedFrequencyDays days"
                            } else {
                                "Use value between 1 to 30 days."
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
                            val customDays = customDaysInput.toIntOrNull()
                            if (customDays == null || customDays < 1 || customDays > 30) {
                                customInputError = "Enter a valid frequency (1 - 30 days)."
                            } else {
                                onFrequencySelected(customDays)
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
                            text = "Apply Custom Frequency",
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
