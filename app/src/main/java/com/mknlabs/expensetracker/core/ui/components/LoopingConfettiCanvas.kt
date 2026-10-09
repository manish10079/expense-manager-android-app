package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

private data class BurstParticle(
    val startXRatio: Float,
    val startYRatio: Float,
    val burstVxDp: Float,
    val burstVyDp: Float,
    val sizeDp: Float,
    val color: Color,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

private data class FallParticle(
    val startXRatio: Float,
    val startYRatio: Float,
    val fallSpeedDp: Float,
    val swaySpeed: Float,
    val sizeDp: Float,
    val color: Color,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun LoopingConfettiCanvas(
    modifier: Modifier = Modifier
) {
    val palette = remember {
        listOf(
            Color(0xFFF59E0B), // Amber
            Color(0xFF34D399), // Emerald
            Color(0xFF9333EA), // Purple
            Color(0xFF2DD4BF), // Teal
            Color(0xFFEC4899), // Pink
            Color(0xFFFBBF24), // Gold
            Color(0xFF6366F1)  // Indigo
        )
    }

    val burstParticles = remember {
        List(55) { index ->
            val angleRad = (Math.PI * (-145.0 + (index % 18) * 6.0)) / 180.0
            val burstSpeed = 240f + (index % 8) * 45f
            BurstParticle(
                startXRatio = 0.5f + (if (index % 2 == 0) 1f else -1f) * ((index % 6) * 0.03f),
                startYRatio = 0.55f,
                burstVxDp = (Math.cos(angleRad) * burstSpeed).toFloat(),
                burstVyDp = (Math.sin(angleRad) * burstSpeed).toFloat(),
                sizeDp = 5f + (index % 6) * 2f,
                color = palette[index % palette.size],
                rotationSpeed = (if (index % 2 == 0) 2.5f else -2.5f) * (1f + (index % 4) * 0.5f),
                isCircle = index % 3 == 0
            )
        }
    }

    val fallParticles = remember {
        List(35) { index ->
            FallParticle(
                startXRatio = ((index * 0.08f + (index % 5) * 0.12f)) % 1.0f,
                startYRatio = -((index % 12) * 0.09f + 0.05f),
                fallSpeedDp = 75f + (index % 5) * 25f,
                swaySpeed = 1.8f + (index % 4) * 0.4f,
                sizeDp = 4f + (index % 5) * 2f,
                color = palette[(index + 3) % palette.size],
                rotationSpeed = (if (index % 2 == 0) 1.5f else -1.5f) * (1f + (index % 3) * 0.4f),
                isCircle = index % 3 == 0
            )
        }
    }

    var timeSeconds by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var startFrameNanos = 0L
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (startFrameNanos == 0L) startFrameNanos = frameTimeNanos
                timeSeconds = (frameTimeNanos - startFrameNanos) / 1_000_000_000f
            }
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        if (w <= 0f || h <= 0f) return@Canvas

        val burstDuration = 1.8f
        val gravityPx = 400.dp.toPx()

        if (timeSeconds < burstDuration) {
            val t = timeSeconds
            val fadeAlpha = ((1.0f - (t / burstDuration)) * 0.9f).coerceIn(0f, 0.9f)

            burstParticles.forEachIndexed { _, p ->
                val vx = p.burstVxDp.dp.toPx()
                val vy = p.burstVyDp.dp.toPx()
                val startX = p.startXRatio * w
                val startY = p.startYRatio * h

                val currentX = startX + vx * t
                val currentY = startY + vy * t + 0.5f * gravityPx * t * t
                val currentRotation = t * p.rotationSpeed * 120f

                rotate(currentRotation, pivot = Offset(currentX, currentY)) {
                    if (p.isCircle) {
                        drawCircle(
                            color = p.color,
                            radius = p.sizeDp.dp.toPx() / 2f,
                            center = Offset(currentX, currentY),
                            alpha = fadeAlpha
                        )
                    } else {
                        val pSize = p.sizeDp.dp.toPx()
                        drawRect(
                            color = p.color,
                            topLeft = Offset(currentX - pSize / 2f, currentY - pSize / 2f),
                            size = Size(pSize, pSize * 1.6f),
                            alpha = fadeAlpha
                        )
                    }
                }
            }
        } else {
            val loopTime = timeSeconds - burstDuration

            fallParticles.forEachIndexed { i, p ->
                val fallSpeedPx = p.fallSpeedDp.dp.toPx()
                val screenHeightWithPadding = h + 50.dp.toPx()
                val rawY = (p.startYRatio * h + loopTime * fallSpeedPx) % screenHeightWithPadding
                val currentY = rawY - 25.dp.toPx()

                val sway = Math.sin((loopTime * p.swaySpeed + i).toDouble()).toFloat() * 14.dp.toPx()
                val currentX = (p.startXRatio * w + sway) % w
                val currentRotation = loopTime * p.rotationSpeed * 90f

                rotate(currentRotation, pivot = Offset(currentX, currentY)) {
                    if (p.isCircle) {
                        drawCircle(
                            color = p.color,
                            radius = p.sizeDp.dp.toPx() / 2f,
                            center = Offset(currentX, currentY),
                            alpha = 0.8f
                        )
                    } else {
                        val pSize = p.sizeDp.dp.toPx()
                        drawRect(
                            color = p.color,
                            topLeft = Offset(currentX - pSize / 2f, currentY - pSize / 2f),
                            size = Size(pSize, pSize * 1.6f),
                            alpha = 0.8f
                        )
                    }
                }
            }
        }
    }
}
