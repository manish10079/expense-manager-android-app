package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp

/**
 * Paints [imageVector] with [brush] instead of a flat tint.
 *
 * `tint` only takes a solid colour, so the glyph is drawn white and the ramp is punched
 * through its alpha with SrcIn, which keeps the icon's own silhouette. The offscreen layer
 * fences the blend in so it cannot erase what was drawn around it.
 */
@Composable
fun AppLockGradientIcon(
    imageVector: ImageVector,
    brush: Brush,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = Color.White,
        modifier = modifier
            .size(size)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                drawRect(brush = brush, blendMode = BlendMode.SrcIn)
            }
    )
}
