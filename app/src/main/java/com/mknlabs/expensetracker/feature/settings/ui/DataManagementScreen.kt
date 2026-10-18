package com.mknlabs.expensetracker.feature.settings.ui

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowCounterClockwise
import com.adamglin.phosphoricons.regular.ClockCountdown
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.CloudArrowUp
import com.adamglin.phosphoricons.regular.Database
import com.adamglin.phosphoricons.regular.FileArrowDown
import com.adamglin.phosphoricons.regular.FileArrowUp
import com.adamglin.phosphoricons.regular.Note
import com.adamglin.phosphoricons.regular.Trash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.genderRowSelected
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.components.*
import com.mknlabs.expensetracker.core.ui.navigation.LocalUpgradeToPro
import com.mknlabs.expensetracker.core.ui.models.SelectionItem
import java.time.LocalDate
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme

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
    onBackClick: () -> Unit,
    isAdsEnabled: Boolean = false
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val mainViewModel: com.mknlabs.expensetracker.core.ui.MainViewModel = hiltViewModel()
    val currentUser by mainViewModel.currentUser.collectAsStateWithLifecycle()
    val isAnonymous = currentUser?.isAnonymous ?: true
    val isSignedOut = currentUser == null
    val isSignedInAccount = currentUser != null && currentUser?.isAnonymous != true
    val isGoogleAccount = currentUser?.providerData?.any { it.providerId == "google.com" } == true
    val isEmailVerified = currentUser?.isEmailVerified == true || isGoogleAccount

    // Gate auto backup behind email verification
    val wrappedAutoBackupChange = { enabled: Boolean ->
        if (enabled && !isEmailVerified) {
            // Show verification dialog
            currentUser?.sendEmailVerification()
            android.widget.Toast.makeText(context, context.getString(R.string.toast_verification_email_sent), android.widget.Toast.LENGTH_SHORT).show()
        } else {
            onAutoBackupEnabledChange(enabled)
        }
    }

    DataManagementContent(
        transactionCount = transactionCount,
        isAutoBackupEnabled = isAutoBackupEnabled,
        autoBackupFrequencyDays = autoBackupFrequencyDays,
        isAdsEnabled = isAdsEnabled,
        isAnonymous = isAnonymous,
        isSignedOut = isSignedOut,
        isSignedInAccount = isSignedInAccount,
        onAutoBackupEnabledChange = wrappedAutoBackupChange,
        onAutoBackupFrequencyChange = onAutoBackupFrequencyChange,
        onDatabaseBackupFileSelected = onDatabaseBackupFileSelected,
        onDatabaseRestoreFileSelected = onDatabaseRestoreFileSelected,
        onJsonExportFileSelected = onJsonExportFileSelected,
        onJsonImportFileSelected = onJsonImportFileSelected,
        onLegacyImportFileSelected = onLegacyImportFileSelected,
        onDeleteAllTransactionsClick = onDeleteAllTransactionsClick,
        onPrepareForExternalActivity = onPrepareForExternalActivity,
        onBackClick = onBackClick,
        getAccessStatus = { feature, optionId -> monetizationViewModel.getAccessStatus(feature, optionId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataManagementContent(
    transactionCount: Int,
    isAutoBackupEnabled: Boolean,
    autoBackupFrequencyDays: Int,
    isAdsEnabled: Boolean,
    isAnonymous: Boolean,
    isSignedOut: Boolean = false,
    isSignedInAccount: Boolean = false,
    onAutoBackupEnabledChange: (Boolean) -> Unit,
    onAutoBackupFrequencyChange: (Int) -> Unit,
    onDatabaseBackupFileSelected: (Uri) -> Unit,
    onDatabaseRestoreFileSelected: (Uri) -> Unit,
    onJsonExportFileSelected: (Uri) -> Unit,
    onJsonImportFileSelected: (Uri) -> Unit,
    onLegacyImportFileSelected: (Uri) -> Unit,
    onDeleteAllTransactionsClick: () -> Unit,
    onPrepareForExternalActivity: () -> Unit,
    onBackClick: () -> Unit,
    getAccessStatus: (Feature, String?) -> kotlinx.coroutines.flow.StateFlow<AccessStatus>
) {
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
            .appScreenBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))

            val enter = rememberSectionEnterAlphas(2)
            AppHeader(
                title = stringResource(id = R.string.title_data_management),
                onBackClick = onBackClick,
                modifier = Modifier.alpha(enter[0])
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .alpha(enter[1]),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // SECTION 1: CLOUD & AUTOMATION
                item {
                    SettingsGroup(colors = settingsGroupCardColors()) {
                        GatedAction(
                            feature = Feature.AUTO_BACKUP,
                            onAction = { onAutoBackupEnabledChange(!isAutoBackupEnabled) }
                        ) { status, onClick ->
                            val accessLevel = FeatureRegistry.getAccessLevel(Feature.AUTO_BACKUP)
                            SettingsItemCard(
                                title = stringResource(id = R.string.title_auto_backup),
                                subtitle = stringResource(id = R.string.desc_auto_backup_subtitle),
                                icon = PhosphorIcons.Regular.CloudArrowUp,
                                iconTile = SettingsTiles.Violet,
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
                                icon = PhosphorIcons.Regular.ClockCountdown,
                                iconTile = SettingsTiles.Sky,
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
                    SettingsGroup(colors = settingsGroupCardColors()) {
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
                                icon = PhosphorIcons.Regular.FileArrowDown,
                                iconTile = SettingsTiles.Emerald,
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
                            icon = PhosphorIcons.Regular.FileArrowUp,
                            iconTile = SettingsTiles.Amber,
                            type = SettingsItemType.Button,
                            valueText = stringResource(id = R.string.label_import),
                            onClick = {
                                importJsonClickCount++
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
                                icon = PhosphorIcons.Regular.Database,
                                iconTile = SettingsTiles.Indigo,
                                type = SettingsItemType.Button,
                                valueText = stringResource(id = R.string.title_backup),
                                accessLevel = accessLevel,
                                isLocked = status !is AccessStatus.Granted,
                                onClick = onClick,
                                standalone = false
                            )
                        }
                        SettingsGroupDivider()
                        GatedAction(
                            feature = Feature.DATABASE_RESTORE,
                            onAction = { isRestorePickerVisible = true }
                        ) { status, onClick ->
                            val accessLevel = FeatureRegistry.getAccessLevel(Feature.DATABASE_RESTORE)
                            SettingsItemCard(
                                title = stringResource(id = R.string.label_restore_database_db),
                                subtitle = stringResource(id = R.string.desc_restore_db_subtitle),
                                icon = PhosphorIcons.Regular.ArrowCounterClockwise,
                                iconTile = SettingsTiles.Teal,
                                type = SettingsItemType.Button,
                                valueText = stringResource(id = R.string.title_restore),
                                accessLevel = accessLevel,
                                isLocked = status !is AccessStatus.Granted,
                                onClick = onClick,
                                standalone = false
                            )
                        }
                        if (isLegacyImportVisible) {
                            SettingsGroupDivider()
                            SettingsItemCard(
                                title = stringResource(id = R.string.label_legacy_import),
                                subtitle = stringResource(id = R.string.desc_legacy_import_subtitle),
                                icon = PhosphorIcons.Regular.ClockCounterClockwise,
                                iconTile = SettingsTiles.Fuchsia,
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
                    SettingsGroup(colors = settingsGroupCardColors()) {
                        GatedAction(
                            feature = Feature.TRANSACTION_COUNT,
                            onAction = { /* No action for count info */ }
                        ) { status, onClick ->
                            val accessLevel = FeatureRegistry.getAccessLevel(Feature.TRANSACTION_COUNT)
                            SettingsItemCard(
                                title = stringResource(id = R.string.title_transaction_count),
                                subtitle = stringResource(id = R.string.desc_transaction_count_subtitle),
                                icon = PhosphorIcons.Regular.Note,
                                iconTile = SettingsTiles.Pink,
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
                            title = stringResource(id = if (isSignedInAccount) R.string.label_delete_account_and_data else R.string.label_delete_all_data),
                            subtitle = stringResource(id = R.string.desc_delete_all_data_subtitle),
                            icon = PhosphorIcons.Regular.Trash,
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
        ModernConfirmationDialog(
            onDismissRequest = { isDeleteTransactionsDialogVisible = false },
            title = stringResource(
                id = if (isSignedInAccount) R.string.label_delete_account_and_data else R.string.label_delete_all_data
            ),
            text = stringResource(
                id = when {
                    isSignedInAccount -> R.string.desc_delete_account_and_data_warning
                    isSignedOut -> R.string.desc_delete_data_signed_out_warning
                    else -> R.string.desc_delete_data_guest_warning
                }
            ),
            confirmButtonText = stringResource(id = R.string.label_delete_all),
            onConfirm = {
                isDeleteTransactionsDialogVisible = false
                onDeleteAllTransactionsClick()
            },
            headerIcon = PhosphorIcons.Regular.Trash,
            dismissButtonText = stringResource(id = R.string.label_cancel_1),
            onDismiss = { isDeleteTransactionsDialogVisible = false }
        )
    }

    pendingRestoreUri?.let { selectedUri ->
        ModernConfirmationDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = stringResource(id = R.string.label_restore_database),
            text = stringResource(id = R.string.label_this_will_overwrite_your_curre),
            confirmButtonText = stringResource(id = R.string.title_restore),
            onConfirm = {
                pendingRestoreUri = null
                onDatabaseRestoreFileSelected(selectedUri)
            },
            headerIcon = PhosphorIcons.Regular.ArrowCounterClockwise,
            dismissButtonText = stringResource(id = R.string.label_cancel_1),
            onDismiss = { pendingRestoreUri = null }
        )
    }
    
    if (isFrequencyPickerVisible) {
        val frequencyItems = presetAutoBackupFrequencies.map { days ->
            val optionId = if (days == 0) "custom" else days.toString()
            val status = getAccessStatus(Feature.AUTO_BACKUP, optionId).value
            val accessLevel = FeatureRegistry.getAccessLevel(Feature.AUTO_BACKUP, optionId)
            
            SelectionItem(
                id = days,
                title = if (days == 0) stringResource(R.string.label_custom_frequency) else pluralStringResource(id = R.plurals.label_days_count_formatted, count = days, days),
                subtitle = if (days == 0) stringResource(R.string.label_pick_a_preset_or_enter_a_custo) else pluralStringResource(id = R.plurals.desc_auto_backup_every_days, count = days, days),
                leadingIcon = PhosphorIcons.Regular.ClockCountdown,
                isLocked = status !is AccessStatus.Granted,
                accessLevel = accessLevel
            )
        }
        AppSelectionSheet(
            title = stringResource(id = R.string.title_backup_frequency),
            description = stringResource(id = R.string.desc_choose_backup_frequency),
            items = frequencyItems,
            selectedId = autoBackupFrequencyDays,
            containerColor = MaterialTheme.colorScheme.profileSheet,
            rowColors = SelectionRowColors(
                container = MaterialTheme.colorScheme.genderRow,
                selectedContainer = MaterialTheme.colorScheme.genderRowSelected
            ),
            onItemSelected = { days ->
                val optionId = if (days == 0) "custom" else days.toString()
                val status = getAccessStatus(Feature.AUTO_BACKUP, optionId).value
                if (status is AccessStatus.Granted) {
                    if (days == 0) {
                        isFrequencyPickerVisible = false
                        isCustomFrequencyDialogVisible = true
                    } else {
                        onAutoBackupFrequencyChange(days)
                        isFrequencyPickerVisible = false
                    }
                } else {
                    isFrequencyPickerVisible = false
                    showPremiumSheet = true
                }
            },
            onDismiss = { isFrequencyPickerVisible = false }
        )
    }

    val upgradeToPro = LocalUpgradeToPro.current
    if (showPremiumSheet) {
        PremiumGateSheet(
            onDismiss = { showPremiumSheet = false },
            onUpgradeClick = {
                showPremiumSheet = false
                upgradeToPro()
            }
        )
    }

    if (isCustomFrequencyDialogVisible) {
        val days = customFrequencyInput.toIntOrNull()
        val canApply = days != null && days in 1..365
        Dialog(onDismissRequest = { isCustomFrequencyDialogVisible = false }) {
            NamedCreateCard(
                title = stringResource(R.string.title_custom_backup_frequency),
                caption = stringResource(R.string.label_pick_a_preset_or_enter_a_custo),
                value = customFrequencyInput,
                onValueChange = { if (it.all { char -> char.isDigit() }) customFrequencyInput = it },
                placeholder = stringResource(R.string.placeholder_days),
                confirmLabel = stringResource(R.string.label_apply),
                onConfirm = {
                    val applied = customFrequencyInput.toIntOrNull()
                    if (applied != null && applied in 1..365) {
                        onAutoBackupFrequencyChange(applied)
                        isCustomFrequencyDialogVisible = false
                        isFrequencyPickerVisible = false
                    }
                },
                headerIcon = PhosphorIcons.Regular.ClockCountdown,
                confirmEnabled = canApply
            )
        }
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
        color = MaterialTheme.colorScheme.accentInk,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        ),
        modifier = modifier.padding(start = 6.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun DataManagementScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        val flow = remember { kotlinx.coroutines.flow.MutableStateFlow(AccessStatus.Granted) }
        DataManagementContent(
            transactionCount = 120,
            isAutoBackupEnabled = true,
            autoBackupFrequencyDays = 7,
            isAdsEnabled = true,
            isAnonymous = false,
            onAutoBackupEnabledChange = {},
            onAutoBackupFrequencyChange = {},
            onDatabaseBackupFileSelected = {},
            onDatabaseRestoreFileSelected = {},
            onJsonExportFileSelected = {},
            onJsonImportFileSelected = {},
            onLegacyImportFileSelected = {},
            onDeleteAllTransactionsClick = {},
            onPrepareForExternalActivity = {},
            onBackClick = {},
            getAccessStatus = { _, _ -> flow }
        )
    }
}
