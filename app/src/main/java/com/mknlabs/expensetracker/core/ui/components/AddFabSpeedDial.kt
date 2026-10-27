package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.MicrophoneStage
import com.composables.icons.lucide.FileImage
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquareText
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val SpeedDialRadius = 78.dp
private val SpeedDialButtonSize = 52.dp

/** Upper-arc angles, left to right: file, mic, write. */
private val SpeedDialAngles = floatArrayOf(150f, 90f, 30f)

@Composable
fun AddFabSpeedDial(
    expanded: Boolean,
    onFileClick: () -> Unit,
    onMicClick: () -> Unit,
    onWriteClick: () -> Unit,
    onManualClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    fabSize: Dp = BrandAddFabDefaults.Size,
    fab: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val radiusPx = with(density) { SpeedDialRadius.toPx() }
    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "add_fab_speed_dial"
    )
    val pulse = rememberInfiniteTransition(label = "add_fab_speed_dial_pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "add_fab_speed_dial_pulse_scale"
    )
    val actions = listOf(
        SpeedDialSpec(
            icon = Lucide.FileImage,
            description = stringResource(R.string.desc_ai_fab_file),
            onClick = onFileClick
        ),
        SpeedDialSpec(
            icon = PhosphorIcons.Bold.MicrophoneStage,
            description = stringResource(R.string.desc_ai_fab_mic),
            onClick = onMicClick
        ),
        SpeedDialSpec(
            icon = Lucide.MessageSquareText,
            description = stringResource(R.string.desc_ai_fab_write),
            onClick = onWriteClick
        )
    )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        actions.forEachIndexed { index, action ->
            val radians = Math.toRadians(SpeedDialAngles[index].toDouble())
            val x = (cos(radians) * radiusPx * progress).roundToInt()
            val y = (-sin(radians) * radiusPx * progress).roundToInt()
            SpeedDialSatellite(
                icon = action.icon,
                contentDescription = action.description,
                visible = progress,
                pulseScale = pulseScale,
                onClick = action.onClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(x, y) }
            )
        }
        fab()
    }
}

private data class SpeedDialSpec(
    val icon: ImageVector,
    val description: String,
    val onClick: () -> Unit
)

@Composable
private fun SpeedDialSatellite(
    icon: ImageVector,
    contentDescription: String,
    visible: Float,
    pulseScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .graphicsLayer {
                val appear = 0.4f + 0.6f * visible
                scaleX = appear
                scaleY = appear
                alpha = visible
            }
            .size(SpeedDialButtonSize)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = colorScheme.accentInk.copy(alpha = 0.28f),
                spotColor = colorScheme.accentInk.copy(alpha = 0.28f)
            )
            .clip(CircleShape)
            .background(brandGradient())
            .clickable(enabled = visible > 0.85f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colorScheme.onCta,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    val pulse = 1f + (pulseScale - 1f) * visible
                    scaleX = pulse
                    scaleY = pulse
                }
        )
    }
}
