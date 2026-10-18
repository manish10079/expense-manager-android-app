package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

/**
 * The app's page: the dark background with the mockup's violet ambient glow at the top.
 *
 * The glow is the one the Manage Categories mockup introduced — a circle at 50% 0, violet
 * at 18%, gone by the 70% stop of a farthest-corner circle drawn over the first 384dp of
 * the screen. Light mode keeps the plain background; every screen paints through this so
 * the glow can never appear on just one of them.
 */
@Composable
fun Modifier.appScreenBackground(): Modifier {
    val colorScheme = MaterialTheme.colorScheme
    return this
        .background(colorScheme.background)
        .appAmbientGlow()
}

/**
 * The mockup's violet top glow alone, for screens that paint their own page fill (the
 * paywall's black scene, for instance). Light mode draws nothing.
 */
@Composable
fun Modifier.appAmbientGlow(): Modifier {
    val colorScheme = MaterialTheme.colorScheme
    return this.drawBehind {
        if (!colorScheme.isDark) return@drawBehind
        val glow = colorScheme.manageCategoryGlow
        val halfWidth = size.width / 2f
        val glowHeight = 384.dp.toPx()
        val radius = sqrt(halfWidth * halfWidth + glowHeight * glowHeight) * 0.7f
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    glow.copy(alpha = 0.18f),
                    glow.copy(alpha = 0f)
                ),
                center = Offset(halfWidth, 0f),
                radius = radius
            )
        )
    }
}
