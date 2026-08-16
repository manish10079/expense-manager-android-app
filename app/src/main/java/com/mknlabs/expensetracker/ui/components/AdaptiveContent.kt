package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centers its content horizontally, capping the width at [maxWidth]. Applied to
 * secondary screens (Settings, Transactions, About, …) so their lists don't
 * stretch edge-to-edge on tablets/desktops. On Compact phones the cap is larger
 * than the screen, so nothing changes.
 */
@Composable
fun AdaptiveContent(
    maxWidth: Dp = 640.dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth)
        ) {
            content()
        }
    }
}
