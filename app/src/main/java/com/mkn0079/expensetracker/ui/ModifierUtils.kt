package com.mkn0079.expensetracker.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

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
