package com.mknlabs.expensetracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.components.*
import com.mknlabs.expensetracker.ui.models.SelectionItem
import java.time.LocalDate
import androidx.hilt.navigation.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val presetAutoBackupFrequencies = listOf(1, 7, 15, 30, 0) // 0 for Custom

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
    val mainViewModel: com.mknlabs.expensetracker.ui.viewmodels.MainViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()
    val currentUser by mainViewModel.currentUser.collectAsStateWithLifecycle()
    val isAnonymous = currentUser?.isAnonymous ?: true

    var isDeleteTransactionsDialogVisible by rememberSaveable { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    val todayLabel = remember { LocalDate.now().toString() }
    
    var isFrequencyPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isRestorePickerVisible by rememberSaveable { mutableStateOf(false) }
    var isCustomFrequencyDialogVisible by rememberSaveable { mutableStateOf(false) }
    var customFrequencyInput by rememberSaveable { mutableStateOf("") }
    var showPremiumSheet by remember { mutableStateOf(false) }
    
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // SECTION 1: CLOUD & AUTOMATION
                item {
                    SettingsGroup {
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
                                onClick = onClick,
                                standalone = false
                            )
                        }
                        SettingsGroupDivider()
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
                                onClick = onClick,
                                standalone = false
                            )
                        }
                    }
                }

                // Inline Native Ad before Data Transfer
                item {
                    AdContainer(
                        isAdsEnabled = isAdsEnabled,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                    }
                }

                // SECTION 2: FILE TRANSFER
                item {
                    SettingsGroup {
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
                                onClick = onClick,
                                standalone = false
                            )
                        }
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(id = R.string.label_import_data_json),
                            subtitle = stringResource(id = R.string.desc_import_data_subtitle),
                            icon = Icons.Rounded.FileUpload,
                            type = SettingsItemType.Button,
                            valueText = stringResource(id = R.string.label_import),
                            onClick = {
                                onPrepareForExternalActivity()
                                jsonImportFilePicker.launch(arrayOf("application/json", "*/*"))
                            },
                            standalone = false
                        )
                        SettingsGroupDivider()
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
                                onClick = onClick,
                                standalone = false
                            )
                        }
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(id = R.string.label_restore_database_db),
                            subtitle = stringResource(id = R.string.desc_restore_db_subtitle),
                            icon = Icons.Rounded.SettingsBackupRestore,
                            type = SettingsItemType.Button,
                            valueText = stringResource(id = R.string.title_restore),
                            onClick = { isRestorePickerVisible = true },
                            standalone = false
                        )
                        if (isLegacyImportVisible) {
                            SettingsGroupDivider()
                            SettingsItemCard(
                                title = stringResource(id = R.string.label_legacy_import),
                                subtitle = stringResource(id = R.string.desc_legacy_import_subtitle),
                                icon = Icons.Rounded.History,
                                type = SettingsItemType.Button,
                                valueText = stringResource(id = R.string.label_migrate),
                                onClick = {
                                    onPrepareForExternalActivity()
                                    legacyImportFilePicker.launch(arrayOf("application/json", "*/*"))
                                },
                                standalone = false
                            )
                        }
                    }
                }

                // SECTION 3: STORAGE & MAINTENANCE
                item {
                    SettingsGroup {
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
                                onClick = onClick,
                                standalone = false
                            )
                        }
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(id = if (isAnonymous) R.string.label_delete_all_data else R.string.label_delete_account_and_data),
                            subtitle = stringResource(id = R.string.desc_delete_all_data_subtitle),
                            icon = Icons.Rounded.DeleteForever,
                            type = SettingsItemType.Button,
                            valueText = stringResource(id = R.string.label_delete_all),
                            isDanger = true,
                            onClick = { isDeleteTransactionsDialogVisible = true },
                            standalone = false
                        )
                    }
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
            val optionId = if (days == 0) "custom" else days.toString()
            val status = monetizationViewModel.getAccessStatus(Feature.AUTO_BACKUP, optionId).collectAsState(AccessStatus.Granted).value
            val accessLevel = FeatureRegistry.getAccessLevel(Feature.AUTO_BACKUP, optionId)
            
            SelectionItem(
                id = days,
                title = if (days == 0) stringResource(R.string.label_custom_frequency) else pluralStringResource(id = R.plurals.label_days_count_formatted, count = days, days),
                subtitle = if (days == 0) stringResource(R.string.label_pick_a_preset_or_enter_a_custo) else pluralStringResource(id = R.plurals.desc_auto_backup_every_days, count = days, days),
                leadingIcon = Icons.Rounded.Update,
                isLocked = status !is AccessStatus.Granted,
                accessLevel = accessLevel
            )
        }
        AppSelectionSheet(
            title = stringResource(id = R.string.title_backup_frequency),
            description = stringResource(id = R.string.desc_choose_backup_frequency),
            items = frequencyItems,
            selectedId = autoBackupFrequencyDays,
            onItemSelected = { days ->
                val optionId = if (days == 0) "custom" else days.toString()
                val status = monetizationViewModel.getAccessStatus(Feature.AUTO_BACKUP, optionId).value
                if (status is AccessStatus.Granted) {
                    if (days == 0) {
                        isCustomFrequencyDialogVisible = true
                    } else {
                        onAutoBackupFrequencyChange(days)
                        isFrequencyPickerVisible = false
                    }
                } else {
                    showPremiumSheet = true
                }
            },
            onDismiss = { isFrequencyPickerVisible = false }
        )
    }

    if (showPremiumSheet) {
        PremiumGateSheet(
            onDismiss = { showPremiumSheet = false },
            onUpgradeClick = {
                monetizationViewModel.onPurchaseSimulated()
                showPremiumSheet = false
            }
        )
    }

    if (isCustomFrequencyDialogVisible) {
        AlertDialog(
            onDismissRequest = { isCustomFrequencyDialogVisible = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.title_custom_backup_frequency),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.label_pick_a_preset_or_enter_a_custo),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = customFrequencyInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) customFrequencyInput = it },
                        label = { Text(stringResource(R.string.label_enter_days)) },
                        placeholder = { Text(stringResource(R.string.placeholder_days)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val days = customFrequencyInput.toIntOrNull() ?: 7
                        if (days in 1..365) {
                            onAutoBackupFrequencyChange(days)
                            isCustomFrequencyDialogVisible = false
                            isFrequencyPickerVisible = false
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.label_apply),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isCustomFrequencyDialogVisible = false }) {
                    Text(
                        text = stringResource(R.string.label_cancel_1),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
