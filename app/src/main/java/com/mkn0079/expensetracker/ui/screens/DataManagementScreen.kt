package com.mkn0079.expensetracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import com.mkn0079.expensetracker.ui.viewmodels.SettingsActionId
import com.mkn0079.expensetracker.ui.viewmodels.SettingsItemUi
import com.mkn0079.expensetracker.ui.viewmodels.SettingsSectionUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    transactionCount: Int,
    onLegacyImportFileSelected: (Uri) -> Unit,
    onDeleteAllTransactionsClick: () -> Unit,
    onPrepareForExternalActivity: () -> Unit,
    onBackClick: () -> Unit
) {
    var isDeleteTransactionsDialogVisible by rememberSaveable { mutableStateOf(false) }

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
            title = "OPERATIONS",
            items = listOf(
                SettingsItemUi(
                    title = "Import Legacy Data",
                    icon = Icons.Filled.Refresh,
                    trailing = "JSON",
                    actionId = SettingsActionId.LegacyImport
                ),
                SettingsItemUi(
                    title = "Delete All Transactions",
                    icon = Icons.Filled.Delete,
                    trailing = "Only transactions",
                    actionId = SettingsActionId.DeleteAllTransactions
                )
            )
        ),
        SettingsSectionUi(
            title = "STORAGE (COMING SOON)",
            items = listOf(
                SettingsItemUi(title = "Backup", icon = Icons.Filled.Sync, showChevron = false),
                SettingsItemUi(title = "Restore", icon = Icons.Filled.Refresh, showChevron = false),
                SettingsItemUi(title = "Export", icon = Icons.Filled.SettingsApplications, showChevron = false)
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

            DataManagementHeader(onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                sections.forEach { section ->
                    item(key = section.title) {
                        DataManagementSection(
                            section = section,
                            onItemClick = { actionId ->
                                when (actionId) {
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
}

@Composable
private fun DataManagementHeader(
    onBackClick: () -> Unit
) {
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
            text = "Data Management",
            color = PurplePrimary,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
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
