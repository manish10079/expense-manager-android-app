package com.mknlabs.expensetracker.core.ui.components

import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.sheet

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
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
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme

/**
 * Voice input bottom sheet states.
 */
enum class VoiceSheetState {
    /** Downloading offline Whisper voice model. */
    DOWNLOADING_MODEL,
    /** Microphone active, listening and recording speech. */
    LISTENING,
    /** Speech received, transcribing & parsing. */
    PROCESSING,
    /** Parse complete, showing preview for user confirmation. */
    RESULT,
    /** Error occurred — shows message with retry option. */
    ERROR
}

/**
 * Bottom sheet for voice transaction input.
 *
 * Flow: Downloading (if needed) → Listening / Recording → Processing → Result (or Error)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputSheet(
    sheetState: VoiceSheetState,
    transcript: String,
    errorMessage: String?,
    onDismissRequest: () -> Unit,
    onRetry: () -> Unit,
    onDone: () -> Unit = onDismissRequest,
    audioAmplitude: Float = 0f,
    downloadProgress: Float = 0f,
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
                VoiceSheetState.DOWNLOADING_MODEL -> VoiceDownloadingModelContent(
                    progress = downloadProgress,
                    onCancel = onDismissRequest,
                )
                VoiceSheetState.LISTENING, VoiceSheetState.PROCESSING, VoiceSheetState.RESULT -> VoiceListeningContent(
                    transcript = transcript,
                    isProcessing = sheetState != VoiceSheetState.LISTENING,
                    audioAmplitude = audioAmplitude,
                    onCancel = onDismissRequest,
                    onDone = onDone,
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
private fun VoiceDownloadingModelContent(
    progress: Float,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.msg_voice_downloading_model, (progress * 100).toInt()),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Box(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .size(96.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                progress = { progress.coerceIn(0.05f, 1f) },
                modifier = Modifier.size(80.dp),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(stringResource(R.string.label_cancel), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun VoiceListeningContent(
    transcript: String,
    isProcessing: Boolean,
    audioAmplitude: Float = 0f,
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
    val ampBoost = (1f + audioAmplitude * 0.35f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(
                if (isProcessing) R.string.msg_voice_transcribing else R.string.label_listening_to_voice
            ),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
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
                    .graphicsLayer { scaleX = blob * ampBoost; scaleY = blob * ampBoost }
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
                        scaleX = ring1 * ampBoost
                        scaleY = ring1 * ampBoost
                        alpha = 0.25f + (1.35f - ring1) * 0.7f
                    }
                    .border(1.dp, Color(0xFF818CF8).copy(alpha = 0.4f), CircleShape)
                    .background(Color(0xFF6366F1).copy(alpha = 0.10f), CircleShape)
            )
            Box(
                Modifier
                    .size(128.dp)
                    .graphicsLayer {
                        scaleX = ring2 * ampBoost
                        scaleY = ring2 * ampBoost
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
                        val dynamicH = if (audioAmplitude > 0.05f) {
                            (h * (0.6f + audioAmplitude * 1.5f)).coerceIn(8f, 56f)
                        } else {
                            h
                        }
                        Box(
                            Modifier
                                .width(6.dp)
                                .height(dynamicH.dp)
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
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.label_cancel), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brandGradient())
                    .clickable(enabled = !isProcessing, onClick = onDone),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.label_done),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBrandGradient,
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

@Preview(name = "Voice Sheet - Listening", showBackground = true)
@Composable
private fun VoiceInputSheetListeningPreview() {
    ExpenseTrackerTheme {
        VoiceListeningContent(
            transcript = "Paid 250 for groceries",
            isProcessing = false,
            audioAmplitude = 0.4f,
            onCancel = {},
            onDone = {},
        )
    }
}
