package com.mkn0079.expensetracker.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

/**
 * Reusable modifier for handling horizontal swipe gestures across the UI layer.
 * 
 * @param key Optional key to reset the gesture detection state (e.g., when content changes).
 * @param threshold The drag distance in pixels to trigger a swipe action (default: 80dp converted to px elsewhere or just raw px here).
 * @param onSwipeLeft Callback triggered when a left swipe (negative horizontal drag) exceeds the threshold.
 * @param onSwipeRight Callback triggered when a right swipe (positive horizontal drag) exceeds the threshold.
 */
fun Modifier.horizontalSwipe(
    key: Any? = null,
    threshold: Float = 80f,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {}
): Modifier = this.pointerInput(key) {
    var totalDrag = 0f
    detectHorizontalDragGestures(
        onHorizontalDrag = { _, dragAmount ->
            totalDrag += dragAmount
        },
        onDragEnd = {
            when {
                totalDrag > threshold -> onSwipeRight()
                totalDrag < -threshold -> onSwipeLeft()
            }
            totalDrag = 0f
        },
        onDragCancel = { totalDrag = 0f }
    )
}

/**
 * Applies a shimmering effect to a component. Ideal for loading states and placeholders.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ),
        label = "shimmer_offset"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFB8B5B5).copy(alpha = 0.2f),
                Color(0xFF8F8B8B).copy(alpha = 0.4f),
                Color(0xFFB8B5B5).copy(alpha = 0.2f),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}
