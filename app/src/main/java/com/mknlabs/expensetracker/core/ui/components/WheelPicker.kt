package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun <T> WheelPicker(
    modifier: Modifier = Modifier,
    items: List<T>,
    initialIndex: Int = 0,
    itemHeight: Dp = 48.dp,
    visibleCount: Int = 5,
    selectedTextColor: Color? = null,
    unselectedTextColor: Color? = null,
    onItemSelected: (T) -> Unit,
    label: (T) -> String
) {
    if (items.isEmpty()) return

    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    // Infinite scroll setup
    val repeatFactor = 1000
    val totalItems = items.size * repeatFactor
    val centerOffset = (repeatFactor / 2) * items.size

    // The center slot (3rd of 5) means items[0..visibleCount/2-1] are above, so start visibleCount/2 before.
    // Result: when list opens, desired item appears in slot index visibleCount/2 (the center slot, 0-indexed).
    val realInitialIndex = centerOffset + (initialIndex % items.size) - (visibleCount / 2)

    val listState = rememberLazyListState(realInitialIndex)
    val decaySpec = remember { exponentialDecay<Float>(frictionMultiplier = 0.42f) }
    val flingBehavior = remember(itemHeightPx, decaySpec) {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                if (kotlin.math.abs(initialVelocity) > 1f) {
                    var lastValue = 0f
                    AnimationState(initialValue = 0f, initialVelocity = initialVelocity).animateDecay(decaySpec) {
                        val delta = value - lastValue
                        lastValue = value
                        val consumed = scrollBy(delta)
                        val blocked = kotlin.math.abs(delta) > 0.5f &&
                            kotlin.math.abs(consumed) < kotlin.math.abs(delta) * 0.4f
                        // Don't let the coast crawl to a stop between two rows.
                        if (blocked || kotlin.math.abs(velocity) < 180f) {
                            cancelAnimation()
                        }
                    }
                }
                val offset = listState.firstVisibleItemScrollOffset.toFloat()
                val remaining = if (offset > itemHeightPx / 2f) itemHeightPx - offset else -offset
                if (kotlin.math.abs(remaining) > 0.5f) {
                    scrollBy(remaining)
                }
                return 0f
            }
        }
    }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = available

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
        }
    }

    val currentOnItemSelected by rememberUpdatedState(onItemSelected)
    var lastSelectedItem by remember { mutableStateOf<T?>(null) }

    fun centerItem(): T {
        val layoutInfo = listState.layoutInfo
        val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
        val closest = layoutInfo.visibleItemsInfo.minByOrNull { item ->
            kotlin.math.abs((item.offset + item.size / 2f) - viewportCenter)
        }
        val centerItemIndex = closest?.index ?: (listState.firstVisibleItemIndex + visibleCount / 2)
        val safeIndex = (centerItemIndex % items.size + items.size) % items.size
        return items[safeIndex]
    }

    // Programmatic jumps only — never interrupt a user swipe/fling.
    LaunchedEffect(initialIndex, items) {
        if (listState.isScrollInProgress) return@LaunchedEffect
        val clampedIndex = initialIndex.coerceIn(0, items.size - 1)
        val targetListIndex = centerOffset + clampedIndex - (visibleCount / 2)
        if (listState.firstVisibleItemIndex != targetListIndex || listState.firstVisibleItemScrollOffset != 0) {
            listState.scrollToItem(targetListIndex)
        }
    }

    // Commit the value after the wheel settles so parent state does not rebuild mid-fling.
    LaunchedEffect(listState, items) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { scrolling -> !scrolling }
            .map { centerItem() }
            .distinctUntilChanged()
            .collect {
                lastSelectedItem = it
                currentOnItemSelected(it)
            }
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleCount)
            .onSizeChanged { },
        contentAlignment = Alignment.Center
    ) {
        // Visual selection zone lines — framing the center slot
        // Center slot top = visibleCount/2 * itemHeight, bottom = (visibleCount/2 + 1) * itemHeight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.Center)
        ) {
                    val lineColor = selectedTextColor ?: MaterialTheme.colorScheme.onSurface
                    // Top line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .align(Alignment.TopCenter)
                            .background(lineColor.copy(alpha = 0.3f))
                    )
                    // Bottom line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .align(Alignment.BottomCenter)
                            .background(lineColor.copy(alpha = 0.3f))
                    )
        }

        // NO contentPadding — items scroll naturally and FVI+visibleCount/2 is always center
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            overscrollEffect = null,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            items(totalItems) { index ->
                val item = items[index % items.size]
                val info = listState.layoutInfo.visibleItemsInfo.find { it.index == index }

                val opacity: Float
                val scale: Float

                if (info != null) {
                    // True visual center of the viewport (no contentPadding, so this is exact)
                    val viewportCenter = (listState.layoutInfo.viewportStartOffset +
                            listState.layoutInfo.viewportEndOffset) / 2f
                    val itemCenter = info.offset + info.size / 2f
                    val distance = kotlin.math.abs(itemCenter - viewportCenter)
                    val normalized = (distance / (itemHeightPx * 2)).coerceIn(0f, 1.2f)
                    opacity = 1f - (normalized * 0.6f)
                    scale = 1f - (normalized * 0.2f)
                } else {
                    opacity = 0.4f
                    scale = 0.8f
                }

                // isSelected: the item whose center is closest to the viewport center
                val isSelected = opacity > 0.92f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    val finalSelectedColor = selectedTextColor ?: MaterialTheme.colorScheme.onSurface
                    val finalUnselectedColor = unselectedTextColor ?: MaterialTheme.colorScheme.onSurfaceVariant

                    Text(
                        text = label(item),
                        color = if (isSelected) finalSelectedColor else finalUnselectedColor.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        modifier = Modifier.graphicsLayer {
                            this.alpha = opacity
                            this.scaleX = scale
                            this.scaleY = scale
                        }
                    )
                }
            }
        }
    }
}
