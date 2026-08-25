package com.mknlabs.expensetracker.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.models.VoiceConfidence
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.utils.toMajorUnits

/**
 * Voice input bottom sheet states.
 */
enum class VoiceSheetState {
    /** Microphone active, listening for speech. */
    LISTENING,
    /** Speech received, parser processing. */
    PROCESSING,
    /** Parse complete, showing preview for user confirmation. */
    RESULT,
    /** Error occurred — shows message with retry option. */
    ERROR
}

/**
 * Bottom sheet for voice transaction input.
 *
 * Flow: Listening → Processing → Result (or Error)
 *
 * Follows the same [ModalBottomSheet] pattern as
 * [TransactionNoteBottomSheet] in AddTransactionScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputSheet(
    sheetState: VoiceSheetState,
    transcript: String,
    parsedTransaction: ParsedVoiceTransaction?,
    errorMessage: String?,
    currencySymbol: String,
    onDismissRequest: () -> Unit,
    onConfirm: (ParsedVoiceTransaction) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetBodyState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetBodyState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.title_voice_add),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.desc_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Content based on state
            when (sheetState) {
                VoiceSheetState.LISTENING -> ListeningContent(transcript)
                VoiceSheetState.PROCESSING -> ProcessingContent()
                VoiceSheetState.RESULT -> ResultContent(
                    transaction = parsedTransaction,
                    currencySymbol = currencySymbol,
                    onConfirm = onConfirm,
                    onRetry = onRetry
                )
                VoiceSheetState.ERROR -> ErrorContent(
                    message = errorMessage ?: stringResource(R.string.msg_voice_error_empty_input),
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
private fun ListeningContent(transcript: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pulsing mic icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = stringResource(R.string.desc_voice_add),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = stringResource(R.string.msg_voice_listening),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        // Live transcript
        if (transcript.isNotBlank()) {
            Text(
                text = "\"$transcript\"",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            )
        } else {
            Text(
                text = stringResource(R.string.desc_voice_add),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProcessingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = stringResource(R.string.msg_voice_processing),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun ResultContent(
    transaction: ParsedVoiceTransaction?,
    currencySymbol: String,
    onConfirm: (ParsedVoiceTransaction) -> Unit,
    onRetry: () -> Unit
) {
    if (transaction == null) {
        ErrorContent(
            message = stringResource(R.string.msg_voice_error_empty_input),
            onRetry = onRetry
        )
        return
    }

    val confidenceColor = when (transaction.confidence) {
        VoiceConfidence.HIGH -> MaterialTheme.colorScheme.primary
        VoiceConfidence.MEDIUM -> MaterialTheme.colorScheme.tertiary
        VoiceConfidence.LOW -> MaterialTheme.colorScheme.error
    }
    val confidenceText = when (transaction.confidence) {
        VoiceConfidence.HIGH -> stringResource(R.string.label_voice_confidence_high)
        VoiceConfidence.MEDIUM -> stringResource(R.string.label_voice_confidence_medium)
        VoiceConfidence.LOW -> stringResource(R.string.label_voice_confidence_low)
    }
    val typeLabel = when (transaction.transactionTypeId) {
        1 -> stringResource(R.string.label_income)
        else -> stringResource(R.string.label_expense)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Parsed preview card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Amount + type row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text =                    "$currencySymbol${"%.2f".format(transaction.amountMinor.toMajorUnits())}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = confidenceColor
                    )
                )
            }

            // Category
            PreviewRow(
                label = stringResource(R.string.label_category_1),
                value = transaction.note.ifBlank { stringResource(R.string.label_other) }
            )

            // Note (if different from category)
            if (transaction.note.isNotBlank()) {
                PreviewRow(
                    label = stringResource(R.string.label_note),
                    value = transaction.note
                )
            }

            // Merchant (if detected)
            if (!transaction.merchant.isNullOrBlank()) {
                PreviewRow(
                    label = stringResource(R.string.label_merchant),
                    value = transaction.merchant
                )
            }

            // Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.label_voice_confidence),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = confidenceText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = confidenceColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.label_voice_retry))
            }

            Button(
                onClick = { onConfirm(transaction) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.label_voice_confirm),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MicOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(R.string.label_voice_retry))
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(name = "Voice Sheet - Listening", showBackground = true)
@Composable
private fun VoiceInputSheetListeningPreview() {
    ExpenseTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            ListeningContent(transcript = "Spent 50 dollars on food")
        }
    }
}

@Preview(name = "Voice Sheet - Result", showBackground = true)
@Composable
private fun VoiceInputSheetResultPreview() {
    ExpenseTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            ResultContent(
                transaction = ParsedVoiceTransaction(
                    amountMinor = 5000L,
                    transactionTypeId = 2,
                    categoryId = 4,
                    note = "Food",
                    merchant = null,
                    confidence = VoiceConfidence.HIGH
                ),
                currencySymbol = "$",
                onConfirm = {},
                onRetry = {}
            )
        }
    }
}
