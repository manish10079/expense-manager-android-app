package com.mknlabs.expensetracker.ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.core.ui.components.AiParseReviewCard
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.utils.toMinorUnits
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal

@AndroidEntryPoint
class ShareCaptureActivity : AppCompatActivity() {

    private val extractViewModel: AiTextExtractViewModel by viewModels()
    private val saveViewModel: ShareCaptureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val share = intent.toIncomingShare()
        val uri = share?.documentUri
        if (uri != null) {
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        }
        setContent {
            ExpenseTrackerTheme {
                ShareCaptureDialog(
                    share = share,
                    extractViewModel = extractViewModel,
                    onSave = { item, fingerprint, done ->
                        saveViewModel.save(item, fingerprint, done)
                    },
                    onDismiss = { finish() },
                )
            }
        }
    }
}

@Composable
private fun ShareCaptureDialog(
    share: IncomingShare?,
    extractViewModel: AiTextExtractViewModel,
    onSave: (AiParsedReviewItem, String, () -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var errorRes by remember { mutableStateOf<Int?>(null) }
    val items = remember { mutableStateListOf<AiParsedReviewItem>() }
    var fingerprint by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<AiParsedReviewItem?>(null) }

    LaunchedEffect(share) {
        if (share == null) {
            errorRes = R.string.msg_ai_parse_failed
            loading = false
            return@LaunchedEffect
        }
        val uri = share.documentUri
        if (uri != null) {
            extractViewModel.parseFromDocument(uri) { outcome ->
                loading = false
                when (outcome) {
                    DocumentParseOutcome.PlayServicesUnavailable,
                    DocumentParseOutcome.Failed,
                    -> errorRes = R.string.msg_ai_parse_failed
                    DocumentParseOutcome.DailyLimit -> errorRes = R.string.msg_ai_daily_limit
                    DocumentParseOutcome.Duplicate -> errorRes = R.string.msg_ai_receipt_already_added
                    is DocumentParseOutcome.RawAdd -> errorRes = R.string.msg_ai_parse_failed
                    is DocumentParseOutcome.Review -> {
                        fingerprint = outcome.fingerprint
                        items.clear()
                        items.addAll(outcome.items)
                    }
                }
            }
        } else {
            val text = share.text.orEmpty()
            extractViewModel.parse(text) { outcome ->
                loading = false
                when (outcome) {
                    TextParseOutcome.Failed -> errorRes = R.string.msg_ai_parse_failed
                    TextParseOutcome.DailyLimit -> errorRes = R.string.msg_ai_daily_limit
                    TextParseOutcome.RawAdd -> errorRes = R.string.msg_ai_parse_failed
                    is TextParseOutcome.Review -> {
                        items.clear()
                        items.addAll(outcome.items)
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.label_sms_inbox_edit_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                when {
                    loading -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text(
                                text = stringResource(R.string.msg_ai_extracting_document),
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
                    errorRes != null -> {
                        Text(text = stringResource(errorRes!!))
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.label_cancel))
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(items, key = { it.id }) { item ->
                                AiParseReviewCard(
                                    item = item,
                                    onAdd = {
                                        onSave(item, fingerprint) {
                                            items.removeAll { it.id == item.id }
                                            if (items.isEmpty()) onDismiss()
                                        }
                                    },
                                    onEdit = { editing = item },
                                )
                            }
                        }
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.label_cancel))
                        }
                    }
                }
            }
        }
    }

    val editItem = editing
    if (editItem != null) {
        var amount by remember(editItem.id) {
            mutableStateOf(BigDecimal.valueOf(editItem.amount).stripTrailingZeros().toPlainString())
        }
        var note by remember(editItem.id) { mutableStateOf(editItem.note) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(stringResource(R.string.label_edit)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(stringResource(R.string.label_amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(stringResource(R.string.label_note)) },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = amount.toDoubleOrNull() ?: return@TextButton
                        val updated = editItem.copy(
                            amount = parsed,
                            amountMinor = parsed.toMinorUnits(),
                            note = note,
                        )
                        val index = items.indexOfFirst { it.id == editItem.id }
                        if (index >= 0) items[index] = updated
                        editing = null
                    },
                ) {
                    Text(stringResource(R.string.label_done))
                }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) {
                    Text(stringResource(R.string.label_cancel))
                }
            },
        )
    }
}
