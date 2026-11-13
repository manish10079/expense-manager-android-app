package com.mknlabs.expensetracker.core.ui.theme

import android.app.Activity
import android.graphics.Bitmap
import android.view.Window
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

/**
 * Holds state for the circular ripple theme reveal transition (Telegram-style).
 */
class ThemeTransitionState internal constructor() {
    var snapshot: ImageBitmap? by mutableStateOf(null)
        private set
    var origin: Offset by mutableStateOf(Offset.Zero)
        private set
    var isTransitioning: Boolean by mutableStateOf(false)
        private set

    fun startTransition(bitmap: Bitmap?, originOffset: Offset) {
        if (bitmap == null) return
        snapshot = bitmap.asImageBitmap()
        origin = originOffset
        isTransitioning = true
    }

    fun endTransition() {
        isTransitioning = false
        snapshot = null
    }
}

val LocalThemeTransitionState = staticCompositionLocalOf { ThemeTransitionState() }

/**
 * Synchronously captures the window's decor view into a software Bitmap.
 */
fun captureWindowBitmap(window: Window?): Bitmap? {
    if (window == null) return null
    val decorView = window.decorView
    val width = decorView.width
    val height = decorView.height
    if (width <= 0 || height <= 0) return null

    return try {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        decorView.draw(canvas)
        bitmap
    } catch (_: Throwable) {
        null
    }
}

/**
 * Full-screen circular reveal overlay that smoothly animates an expanding hole
 * from the tapped button coordinate, revealing the newly rendered theme underneath.
 */
@Composable
fun ThemeCircularRevealOverlay(
    transitionState: ThemeTransitionState,
    modifier: Modifier = Modifier
) {
    val snapshot = transitionState.snapshot
    if (!transitionState.isTransitioning || snapshot == null) return

    val progress = remember(snapshot) { Animatable(0f) }

    LaunchedEffect(snapshot) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        )
        transitionState.endTransition()
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(snapshot) {
                // Intercept touches during the brief 400ms transition
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val origin = transitionState.origin

        // Distance from the origin point to the furthest corner of the display
        val maxRadius = hypot(
            maxOf(origin.x, w - origin.x),
            maxOf(origin.y, h - origin.y)
        )

        val currentRadius = maxRadius * progress.value

        // Cut out the expanding circle hole using EvenOdd fill rule:
        // Area outside the circle has winding number 1 (odd) -> drawn with old snapshot
        // Area inside the circle has winding number 2 (even) -> clipped out, showing the live new theme
        val clipPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, w, h))
            addOval(Rect(center = origin, radius = currentRadius))
        }

        clipPath(clipPath) {
            drawImage(
                image = snapshot,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(snapshot.width, snapshot.height),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(w.toInt(), h.toInt())
            )
        }

        // Delicate glowing wave ripple rim along the perimeter of the expanding circle
        if (progress.value in 0.01f..0.98f) {
            val rippleAlpha = (1f - progress.value) * 0.5f
            drawCircle(
                color = Color.White.copy(alpha = rippleAlpha),
                radius = currentRadius,
                center = origin,
                style = Stroke(width = 2.5.dp.toPx())
            )
        }
    }
}
