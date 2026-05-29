package com.mkn0079.expensetracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.FeatureRegistry
import com.mkn0079.expensetracker.models.SettingsItemType
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.components.*
import com.mkn0079.expensetracker.ui.models.SelectionItem
import java.time.LocalDate
import androidx.hilt.navigation.compose.hiltViewModel
import com.mkn0079.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mkn0079.expensetracker.ui.components.AdContainer
import com.mkn0079.expensetracker.ui.components.NativeAdCard
import com.mkn0079.expensetracker.monetization.AdPlacement
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val presetAutoBackupFrequencies = listOf(1, 7, 15, 30)

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
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val mainViewModel: com.mkn0079.expensetracker.ui.viewmodels.MainViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()
    val currentUser by mainViewModel.currentUser.collectAsStateWithLifecycle()
    val isAnonymous = currentUser?.isAnonymous ?: true

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
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))

            AppHeader(
                title = stringResource(id = R.string.title_data_management),
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
                item { SectionHeader(text = stringResource(id = R.string.label_automated_sync)) }
                item {
                    GatedAction(
                        feature = Feature.AUTO_BACKUP,
                        onAction = { onAutoBackupEnabledChange(!isAutoBackupEnabled) }
                    ) { status, onClick ->
                        val accessLevel = FeatureRegistry.getAccessLevel(Feature.AUTO_BACKUP)
                        SettingsItemCard(
                            title = stringResource(id = R.string.title_auto_backup),
                            subtitle = stringResource(id = R.string.desc_auto_backup_subtitle),
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
                            title = stringResource(id = R.string.title_backup_frequency),
                            subtitle = stringResource(id = R.string.desc_backup_frequency_subtitle),
                            icon = Icons.Rounded.Timelapse,
                            type = SettingsItemType.Value,
                            valueText = pluralStringResource(id = R.plurals.label_days_count_formatted, count = autoBackupFrequencyDays, autoBackupFrequencyDays),
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            isEnabled = isAutoBackupEnabled,
                            onClick = onClick
                        )
                    }
                }

                // Inline Native Ad before Data Transfer
                item {
                    AdContainer(isAdsEnabled = isAdsEnabled) {
                        NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                    }
                }

                // SECTION 2: DATA TRANSFER
                item { 
                    SectionHeader(
                        text = stringResource(id = R.string.label_data_transfer),
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
                            title = stringResource(id = R.string.label_export_data_json),
                            subtitle = stringResource(id = R.string.desc_export_data_subtitle),
                            icon = Icons.Rounded.FileDownload,
                            type = SettingsItemType.Button,
                            valueText = stringResource(id = R.string.label_export),
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            onClick = onClick
                        )
                    }
                }
                item {
                    SettingsItemCard(
                        title = stringResource(id = R.string.label_import_data_json),
                        subtitle = stringResource(id = R.string.desc_import_data_subtitle),
                        icon = Icons.Rounded.FileUpload,
                        type = SettingsItemType.Button,
                        valueText = stringResource(id = R.string.label_import),
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
                            title = stringResource(id = R.string.label_backup_database_db),
                            subtitle = stringResource(id = R.string.desc_backup_db_subtitle),
                            icon = Icons.Rounded.Storage,
                            type = SettingsItemType.Button,
                            valueText = stringResource(id = R.string.title_backup),
                            accessLevel = accessLevel,
                            isLocked = status !is AccessStatus.Granted,
                            onClick = onClick
                        )
                    }
                }
                item {
                    SettingsItemCard(
                        title = stringResource(id = R.string.label_restore_database_db),
                        subtitle = stringResource(id = R.string.desc_restore_db_subtitle),
                        icon = Icons.Rounded.SettingsBackupRestore,
                        type = SettingsItemType.Button,
                        valueText = stringResource(id = R.string.title_restore),
                        onClick = { isRestorePickerVisible = true }
                    )
                }
                if (isLegacyImportVisible) {
                    item {
                        SettingsItemCard(
                            title = stringResource(id = R.string.label_legacy_import),
                            subtitle = stringResource(id = R.string.desc_legacy_import_subtitle),
                            icon = Icons.Rounded.History,
                            type = SettingsItemType.Button,
                            valueText = stringResource(id = R.string.label_migrate),
                            onClick = {
                                onPrepareForExternalActivity()
                                legacyImportFilePicker.launch(arrayOf("application/json", "*/*"))
                            }
                        )
                    }
                }

                // SECTION 3: UTILITIES & STATS
                item { SectionHeader(text = stringResource(id = R.string.label_utilities_and_stats)) }
                item {
                    GatedAction(
                        feature = Feature.TRANSACTION_COUNT,
                        onAction = { /* No action for count info */ }
                    ) { status, onClick ->
                        val accessLevel = FeatureRegistry.getAccessLevel(Feature.TRANSACTION_COUNT)
                        SettingsItemCard(
                            title = stringResource(id = R.string.title_transaction_count),
                            subtitle = stringResource(id = R.string.desc_transaction_count_subtitle),
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
                        title = stringResource(id = if (isAnonymous) R.string.label_delete_all_data else R.string.label_delete_account_and_data),
                        subtitle = stringResource(id = R.string.desc_delete_all_data_subtitle),
                        icon = Icons.Rounded.DeleteForever,
                        type = SettingsItemType.Button,
                        valueText = stringResource(id = R.string.label_delete_all),
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
                    text = stringResource(id = if (isAnonymous) R.string.label_delete_all_data else R.string.label_delete_account_and_data),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.desc_delete_data_warning),
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
                        text = stringResource(id = R.string.label_delete_all),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteTransactionsDialogVisible = false }) {
                    Text(
                        text = stringResource(id = R.string.label_cancel_1),
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
                    text = stringResource(id = R.string.label_restore_database),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.label_this_will_overwrite_your_curre),
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
                        text = stringResource(id = R.string.title_restore),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) {
                    Text(
                        text = stringResource(id = R.string.label_cancel_1),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
    
    if (isFrequencyPickerVisible) {
        val frequencyItems = presetAutoBackupFrequencies.map { days ->
            SelectionItem(
                id = days,
                title = pluralStringResource(id = R.plurals.label_days_count_formatted, count = days, days),
                subtitle = pluralStringResource(id = R.plurals.desc_auto_backup_every_days, count = days, days),
                leadingIcon = Icons.Rounded.Update
            )
        }
        AppSelectionSheet(
            title = stringResource(id = R.string.title_backup_frequency),
            description = stringResource(id = R.string.desc_choose_backup_frequency),
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
