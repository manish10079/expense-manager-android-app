package com.mknlabs.expensetracker.core.ui.components

import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.sheet

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.models.VoiceConfidence
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
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
    onDone: () -> Unit = onDismissRequest,
    modifier: Modifier = Modifier
) {
    val sheetBodyState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetBodyState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = if (MaterialTheme.colorScheme.isDark) {
            MaterialTheme.colorScheme.profileSheet
        } else {
            MaterialTheme.colorScheme.sheet
        },
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
            when (sheetState) {
                VoiceSheetState.LISTENING, VoiceSheetState.PROCESSING -> ListeningContent(
                    transcript = transcript,
                    isProcessing = sheetState == VoiceSheetState.PROCESSING,
                    onCancel = onDismissRequest,
                    onDone = onDone,
                )
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
private fun ListeningContent(
    transcript: String,
    isProcessing: Boolean,
    onCancel: () -> Unit,
    onDone: () -> Unit,
) {
    val dark = MaterialTheme.colorScheme.isDark
    val pulse = rememberInfiniteTransition(label = "voice-listen")
    val ring1 by pulse.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring1",
    )
    val ring2 by pulse.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring2",
    )
    val blob by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "blob",
    )
    val barDelays = intArrayOf(100, 300, 200, 500, 400)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(
                if (isProcessing) R.string.msg_voice_processing else R.string.label_listening_to_voice
            ),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
        )
        Text(
            text = if (transcript.isNotBlank()) {
                "\"$transcript\""
            } else {
                stringResource(R.string.hint_voice_listen_example)
            },
            modifier = Modifier.padding(top = 4.dp).height(20.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .padding(vertical = 24.dp)
                .size(144.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer { scaleX = blob; scaleY = blob }
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF818CF8).copy(alpha = if (dark) 0.35f else 0.22f),
                                Color.Transparent,
                            )
                        ),
                        CircleShape,
                    )
            )
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = ring1
                        scaleY = ring1
                        alpha = 0.25f + (1.35f - ring1) * 0.7f
                    }
                    .border(1.dp, Color(0xFF818CF8).copy(alpha = 0.4f), CircleShape)
                    .background(Color(0xFF6366F1).copy(alpha = 0.10f), CircleShape)
            )
            Box(
                Modifier
                    .size(128.dp)
                    .graphicsLayer {
                        scaleX = ring2
                        scaleY = ring2
                        alpha = 0.25f + (1.35f - ring2) * 0.7f
                    }
                    .border(1.dp, Color(0xFFC084FC).copy(alpha = 0.5f), CircleShape)
                    .background(Color(0xFFA855F7).copy(alpha = 0.10f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)))),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    barDelays.forEach { delay ->
                        val h by pulse.animateFloat(
                            initialValue = 12f,
                            targetValue = 48f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse,
                            ),
                            label = "bar-$delay",
                        )
                        Box(
                            Modifier
                                .width(6.dp)
                                .height(h.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color.White),
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .border(
                    width = 0.dp,
                    color = Color.Transparent,
                    shape = RoundedCornerShape(0.dp),
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.label_cancel), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = onDone,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
            ) {
                Text(stringResource(R.string.label_done), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
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
        VoiceConfidence.HIGH -> MaterialTheme.colorScheme.accentInk
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
            AppTextButton(
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
                    containerColor = MaterialTheme.colorScheme.accentInk
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
                containerColor = MaterialTheme.colorScheme.accentInk
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
            ListeningContent(
                transcript = "Spent 50 dollars on food",
                isProcessing = false,
                onCancel = {},
                onDone = {},
            )
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
