package com.mkn0079.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.ui.models.BackupInfo
import com.mkn0079.expensetracker.ui.models.SelectionItem
import com.mkn0079.expensetracker.utils.BackupFileManager
import com.mkn0079.expensetracker.utils.formatDate
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreSheet(
    onDismiss: () -> Unit,
    onBackupSelected: (BackupInfo) -> Unit,
    onManualSelectClick: () -> Unit
) {
    val context = LocalContext.current
    val backups = remember { BackupFileManager.getAvailableBackups(context) }
    val datePattern = stringResource(id = R.string.date_pattern_full_short)
    val timeSeparator = stringResource(id = R.string.separator_bullet)

    val selectionItems = remember(backups, datePattern, timeSeparator) {
        backups.map { backup ->
            SelectionItem(
                id = backup,
                title = backup.fileName,
                subtitle = "${formatDate(backup.lastModifiedMillis, datePattern)} $timeSeparator ${BackupFileManager.formatFileSize(context, backup.sizeBytes)}",
                leadingIcon = if (backup.isAutoBackup) Icons.Rounded.History else Icons.Rounded.Storage
            )
        }
    }
    AppSelectionSheet(
        title = stringResource(R.string.title_restore_database),
        description = stringResource(R.string.label_select_a_backup_file),
        items = selectionItems,
        selectedId = null,
        onItemSelected = { onBackupSelected(it) },
        onDismiss = onDismiss,
        maxListHeight = 500.dp
    ) {
        // Footer: Manual Selection Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = {
                        onManualSelectClick()
                        onDismiss()
                    }),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FileOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    Text(
                        text = stringResource(R.string.label_select_file_manually),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}
