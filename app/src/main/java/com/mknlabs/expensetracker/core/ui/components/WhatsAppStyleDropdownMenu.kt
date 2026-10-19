package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.mknlabs.expensetracker.core.ui.theme.flatCard

/**
 * WhatsApp 2.26.39.74 overflow: clip-reveal from the top, not scale.
 * The panel’s height grows/collapses; content does not shrink.
 */
@Composable
fun WhatsAppStyleDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset((-14).dp, 44.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val visibleState = remember { MutableTransitionState(false) }
    SideEffect { visibleState.targetState = expanded }

    if (visibleState.currentState || visibleState.targetState) {
        val density = LocalDensity.current
        val popupOffset = with(density) {
            IntOffset(offset.x.roundToPx(), offset.y.roundToPx())
        }
        Popup(
            alignment = Alignment.TopEnd,
            offset = popupOffset,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true, dismissOnBackPress = true)
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visibleState = visibleState,
                modifier = Modifier.graphicsLayer {
                    transformOrigin = TransformOrigin(1f, 0f)
                },
                enter = fadeIn(animationSpec = tween(40)) +
                    expandVertically(
                        animationSpec = tween(220, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.Top
                    ),
                exit = fadeOut(animationSpec = tween(400)) +
                    shrinkVertically(
                        animationSpec = tween(170, easing = FastOutSlowInEasing),
                        shrinkTowards = Alignment.Top
                    )
            ) {
                Surface(
                    modifier = modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.flatCard,
                    border = AppCardDefaults.colors().border,
                    shadowElevation = AppCardDefaults.Elevation,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.widthIn(min = 200.dp).width(IntrinsicSize.Max),
                        content = content
                    )
                }
            }
        }
    }
}
