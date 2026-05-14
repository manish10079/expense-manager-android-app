package com.mkn0079.expensetracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavyLoadingBar(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color,
    inactiveColor: Color,
    showInactiveTrack: Boolean = false,
    waveHeight: Dp = 5.5.dp,
    waveLength: Dp = 30.dp,
    strokeWidth: Dp = 4.dp,
    animationDurationMillis: Int = 900
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val infiniteTransition = rememberInfiniteTransition(label = "wavy_loading_bar")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavy_phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        val centerY = size.height / 2f
        val totalWidth = size.width
        val activeWidth = totalWidth * clampedProgress
        val waveAmplitude = waveHeight.toPx() / 2f
        val wavelengthPx = waveLength.toPx().coerceAtLeast(1f)
        val strokePx = strokeWidth.toPx()

        if (showInactiveTrack) {
            drawLine(
                color = inactiveColor,
                start = Offset(0f, centerY),
                end = Offset(totalWidth, centerY),
                strokeWidth = strokePx,
                cap = StrokeCap.Round
            )
        }

        if (activeWidth <= 0f) return@Canvas

        val path = androidx.compose.ui.graphics.Path()
        var x = 0f
        val step = 2f
        path.moveTo(0f, centerY)

        while (x <= activeWidth) {
            val angle = ((x / wavelengthPx) * (2f * PI).toFloat()) + phase
            val y = centerY + waveAmplitude * sin(angle)
            path.lineTo(x, y)
            x += step
        }

        drawPath(
            path = path,
            color = activeColor,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}
