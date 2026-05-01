package com.mkn0079.expensetracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.FeatureRegistry
import com.mkn0079.expensetracker.models.SettingsItemType
import com.mkn0079.expensetracker.ui.components.*
import com.mkn0079.expensetracker.ui.models.SelectionItem
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
    var isRestorePickerVisible by rememberSaveable { mutableStateOf(false) }
    
    // Secret Unlock Logic: 7 clicks on Import JSON to show Legacy Import
    // Using remember (not saveable) so it resets on app kill/re-entry
    var importJsonClickCount by remember { mutableIntStateOf(0) }
    val isLegacyImportVisible = importJsonClickCount >= 7

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                // SECTION 1: AUTOMATED SYNC
                item { SectionHeader(text = "AUTOMATED SYNC") }
                item {
                    GatedAction(
                        feature = Feature.AUTO_BACKUP,
                        onAction = { onAutoBackupEnabledChange(!isAutoBackupEnabled) }
                    ) { status, onClick ->
                        val accessLevel = FeatureRegistry.getAccessLevel(Feature.AUTO_BACKUP)
                        SettingsItemCard(
                            title = "Auto Backup",
                            subtitle = "Automatic cloud/local sync",
                            icon = Icons.Rounded.CloudUpload,
                            type = SettingsItemType.Toggle,
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            isChecked = isAutoBackupEnabled,
                            onCheckedChange = { onClick() },
                            onClick = onClick
                        )
                    }
                }
                item {
                    GatedAction(
                        feature = Feature.AUTO_BACKUP,
                        onAction = { isFrequencyPickerVisible = true }
                    ) { status, onClick ->
                        val accessLevel = FeatureRegistry.getAccessLevel(Feature.AUTO_BACKUP)
                        SettingsItemCard(
                            title = "Backup Frequency",
                            subtitle = "How often to sync data",
                            icon = Icons.Rounded.Timelapse,
                            type = SettingsItemType.Value,
                            valueText = "$autoBackupFrequencyDays days",
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            isEnabled = isAutoBackupEnabled,
                            onClick = onClick
                        )
                    }
                }

                // SECTION 2: DATA TRANSFER
                item { 
                    SectionHeader(
                        text = "DATA TRANSFER",
                        modifier = Modifier.clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null // Silent clicks
                        ) {
                            importJsonClickCount++
                        }
                    ) 
                }
                item {
                    GatedAction(
                        feature = Feature.DATA_EXPORT,
                        onAction = {
                            onPrepareForExternalActivity()
                            jsonExportFileCreator.launch("expense_tracker_export_$todayLabel.json")
                        }
                    ) { status, onClick ->
                        val accessLevel = FeatureRegistry.getAccessLevel(Feature.DATA_EXPORT)
                        SettingsItemCard(
                            title = "Export Data (JSON)",
                            subtitle = "Download merge-safe JSON file",
                            icon = Icons.Rounded.FileDownload,
                            type = SettingsItemType.Button,
                            valueText = "Export",
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            onClick = onClick
                        )
                    }
                }
                item {
                    SettingsItemCard(
                        title = "Import Data (JSON)",
                        subtitle = "Restore or merge from JSON",
                        icon = Icons.Rounded.FileUpload,
                        type = SettingsItemType.Button,
                        valueText = "Import",
                        onClick = {
                            onPrepareForExternalActivity()
                            jsonImportFilePicker.launch(arrayOf("application/json", "*/*"))
                        }
                    )
                }
                item {
                    GatedAction(
                        feature = Feature.AUTO_BACKUP, // Using auto backup as proxy for premium DB tools
                        onAction = {
                            onPrepareForExternalActivity()
                            databaseBackupFileCreator.launch("expense_tracker_backup_$todayLabel.db")
                        }
                    ) { status, onClick ->
                        val accessLevel = FeatureRegistry.getAccessLevel(Feature.AUTO_BACKUP)
                        SettingsItemCard(
                            title = "Backup Database (.db)",
                            subtitle = "Direct SQLite file backup",
                            icon = Icons.Rounded.Storage,
                            type = SettingsItemType.Button,
                            valueText = "Backup",
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            onClick = onClick
                        )
                    }
                }
                item {
                    SettingsItemCard(
                        title = "Restore Database (.db)",
                        subtitle = "Full database overwrite",
                        icon = Icons.Rounded.SettingsBackupRestore,
                        type = SettingsItemType.Button,
                        valueText = "Restore",
                        onClick = { isRestorePickerVisible = true }
                    )
                }
                if (isLegacyImportVisible) {
                    item {
                        SettingsItemCard(
                            title = "Legacy Import",
                            subtitle = "Import from older app versions",
                            icon = Icons.Rounded.History,
                            type = SettingsItemType.Button,
                            valueText = "Migrate",
                            onClick = {
                                onPrepareForExternalActivity()
                                legacyImportFilePicker.launch(arrayOf("application/json", "*/*"))
                            }
                        )
                    }
                }

                // SECTION 3: UTILITIES & STATS
                item { SectionHeader(text = "UTILITIES & STATS") }
                item {
                    GatedAction(
                        feature = Feature.TRANSACTION_COUNT,
                        onAction = { /* No action for count info */ }
                    ) { status, onClick ->
                        val accessLevel = FeatureRegistry.getAccessLevel(Feature.TRANSACTION_COUNT)
                        SettingsItemCard(
                            title = "Transaction Count",
                            subtitle = "Total records in database",
                            icon = Icons.AutoMirrored.Rounded.Notes,
                            type = SettingsItemType.Value,
                            valueText = transactionCount.toString(),
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            onClick = onClick
                        )
                    }
                }
                item {
                    SettingsItemCard(
                        title = "Delete All Data",
                        subtitle = "Irreversible destructive action",
                        icon = Icons.Rounded.DeleteForever,
                        type = SettingsItemType.Button,
                        valueText = "Delete All",
                        isDanger = true,
                        onClick = { isDeleteTransactionsDialogVisible = true }
                    )
                }
            }
        }
    }

    if (isDeleteTransactionsDialogVisible) {
        AlertDialog(
            onDismissRequest = { isDeleteTransactionsDialogVisible = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Delete all transactions?",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This removes only transactions and their recurring rules. Categories, payment methods, settings, and profile data will stay.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteTransactionsDialogVisible = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    pendingRestoreUri?.let { selectedUri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Restore database?",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will overwrite your current database with the selected .db backup. It will not merge missing records. Existing database data will be replaced.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
    
    if (isFrequencyPickerVisible) {
        val frequencyItems = remember {
            presetAutoBackupFrequencies.map { days ->
                SelectionItem(
                    id = days,
                    title = "$days days",
                    subtitle = "Automatically backup every $days days",
                    leadingIcon = Icons.Rounded.Update
                )
            }
        }
        AppSelectionSheet(
            title = "Backup Frequency",
            description = "Choose how often the app should perform automated backups.",
            items = frequencyItems,
            selectedId = autoBackupFrequencyDays,
            onItemSelected = { days ->
                onAutoBackupFrequencyChange(days)
                isFrequencyPickerVisible = false
            },
            onDismiss = { isFrequencyPickerVisible = false }
        )
    }

    if (isRestorePickerVisible) {
        BackupRestoreSheet(
            onDismiss = { isRestorePickerVisible = false },
            onBackupSelected = { backup ->
                pendingRestoreUri = backup.uri
                isRestorePickerVisible = false
            },
            onManualSelectClick = {
                onPrepareForExternalActivity()
                databaseRestoreFilePicker.launch(arrayOf("*/*"))
            }
        )
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        ),
        modifier = modifier.padding(start = 6.dp, top = 8.dp, bottom = 4.dp)
    )
}
