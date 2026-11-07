package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


import com.mknlabs.expensetracker.core.ui.theme.sheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.components.QuickActionCard
import com.mknlabs.expensetracker.core.ui.components.QuickActionOption
import com.mknlabs.expensetracker.models.CalculatorHistoryEntry
import com.adamglin.phosphoricons.Bold
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient

import com.mknlabs.expensetracker.utils.getDateTime

import androidx.compose.ui.window.Dialog

@Composable
internal fun CalculatorHistoryHeaderIcon(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (MaterialTheme.colorScheme.isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = stringResource(id = R.string.content_desc_calculator_history),
                tint = MaterialTheme.colorScheme.accentInk,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun CalculatorHistorySheet(
    entries: List<CalculatorHistoryEntry>,
    dateFormatPattern: String,
    timeFormat: String,
    onDismiss: () -> Unit,
    onTapExpression: (String) -> Unit,
    onTapResult: (String) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var selectedLongPressEntry by remember { mutableStateOf<CalculatorHistoryEntry?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheet,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f),
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.label_history),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                if (entries.isNotEmpty()) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(id = R.string.label_clear_history),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.msg_calculator_history_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries) { entry ->
                        CalculatorHistoryEntryRow(
                            entry = entry,
                            dateFormatPattern = dateFormatPattern,
                            timeFormat = timeFormat,
                            onTapExpression = onTapExpression,
                            onTapResult = onTapResult,
                            onLongPress = { selectedLongPressEntry = it }
                        )
                    }
                }
                Text(
                    text = stringResource(id = R.string.msg_history_long_press_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showDeleteConfirm) {
        ModernConfirmationDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = stringResource(id = R.string.label_clear_history),
            text = stringResource(id = R.string.msg_clear_history_confirm),
            confirmButtonText = stringResource(id = R.string.label_delete_confirm),
            onConfirm = {
                showDeleteConfirm = false
                onClearHistory()
            },
            headerIcon = Icons.Filled.Delete,
            dismissButtonText = stringResource(id = R.string.label_cancel_confirm),
            onDismiss = { showDeleteConfirm = false }
        )
    }

    selectedLongPressEntry?.let { entry ->
        Dialog(onDismissRequest = { selectedLongPressEntry = null }) {
            QuickActionCard(
                title = stringResource(R.string.title_quick_action),
                caption = stringResource(R.string.msg_history_quick_action_caption),
                options = listOf(
                    QuickActionOption("copy_expr", stringResource(R.string.label_copy_expression), Icons.Filled.ContentCopy),
                    QuickActionOption("copy_result", stringResource(R.string.label_copy_result), Icons.Filled.ContentCopy),
                    QuickActionOption("copy_both", stringResource(R.string.label_copy_expression_and_result), Icons.Filled.ContentCopy),
                    QuickActionOption("delete", stringResource(R.string.label_delete_entry), Icons.Filled.Delete)
                ),
                selectedId = null,
                onSelect = { id ->
                    when (id) {
                        "copy_expr" -> {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.label_copy_expression), entry.expression))
                            Toast.makeText(context, context.getString(R.string.toast_expression_copied), Toast.LENGTH_SHORT).show()
                            selectedLongPressEntry = null
                            onDismiss()
                        }
                        "copy_result" -> {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.label_copy_result), entry.result))
                            Toast.makeText(context, context.getString(R.string.toast_result_copied), Toast.LENGTH_SHORT).show()
                            selectedLongPressEntry = null
                            onDismiss()
                        }
                        "copy_both" -> {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText(
                                    context.getString(R.string.label_copy_expression_and_result),
                                    "${entry.expression} = ${entry.result}"
                                )
                            )
                            Toast.makeText(context, context.getString(R.string.toast_history_copied), Toast.LENGTH_SHORT).show()
                            selectedLongPressEntry = null
                            onDismiss()
                        }
                        "delete" -> {
                            onDeleteEntry(entry.timestampMillis)
                            Toast.makeText(context, context.getString(R.string.toast_entry_deleted), Toast.LENGTH_SHORT).show()
                            selectedLongPressEntry = null
                        }
                    }
                },
                headerIcon = Icons.Rounded.History
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CalculatorHistoryEntryRow(
    entry: CalculatorHistoryEntry,
    dateFormatPattern: String,
    timeFormat: String,
    onTapExpression: (String) -> Unit,
    onTapResult: (String) -> Unit,
    onLongPress: (CalculatorHistoryEntry) -> Unit
) {
    val shape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (MaterialTheme.colorScheme.isDark) standardCardGradient() else SolidColor(MaterialTheme.colorScheme.surfaceVariant))
            .combinedClickable(
                onClick = { onTapResult(entry.result) },
                onLongClick = { onLongPress(entry) }
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = entry.expression,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTapExpression(entry.expression) }
                    .padding(vertical = 2.dp, horizontal = 4.dp)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTapResult(entry.result) }
                    .padding(vertical = 2.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "=",
                    color = MaterialTheme.colorScheme.accentInk,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = entry.result,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = getDateTime(entry.timestampMillis, dateFormatPattern, timeFormat),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        IconButton(
            onClick = { onTapResult(entry.result) },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = stringResource(id = R.string.content_desc_copy_history),
                tint = MaterialTheme.colorScheme.accentInk,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
