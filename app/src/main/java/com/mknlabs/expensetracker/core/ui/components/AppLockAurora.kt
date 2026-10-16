package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.mknlabs.expensetracker.core.ui.theme.AppLockAuroraSoft
import com.mknlabs.expensetracker.core.ui.theme.AppLockAuroraStrong

/**
 * The lock surface's background glows, painted behind whatever the caller puts on top.
 *
 * Shared by the unlock overlay and the setup flow so both sit on the same aurora; the
 * screen's own container colour still comes from [com.mknlabs.expensetracker.core.ui.theme.appLockPalette].
 */
fun Modifier.appLockAurora(): Modifier = drawBehind { drawAppLockAurora(size) }

/**
 * The mock's background: three brand glows over the theme's field.
 *
 * CSS draws them as ELLIPTICAL radial gradients (`70% 44% at 50% 46%`), which Compose's
 * [Brush.radialGradient] cannot express — it takes one radius. Each is therefore drawn as
 * a circle sized to the horizontal radius and scaled vertically around its own centre,
 * which is the same ellipse. The mock's film-grain layer is not drawn: it is a 7% texture
 * over a 160px tile, which would mean shipping and tiling a noise bitmap for something
 * that is invisible against the glows on a phone screen.
 */
private fun DrawScope.drawAppLockAurora(size: Size) {
    appLockGlows.forEach { glow ->
        val radiusPx = size.width * glow.radiusX
        if (radiusPx <= 0f) return@forEach
        val brush = Brush.radialGradient(
            colorStops = arrayOf(0f to glow.color, glow.fade to Color.Transparent),
            center = Offset.Zero,
            radius = radiusPx
        )
        withTransform(
            transformBlock = {
                translate(left = size.width * glow.centerX, top = size.height * glow.centerY)
                scale(scaleX = 1f, scaleY = (size.height * glow.radiusY) / radiusPx, pivot = Offset.Zero)
            }
        ) {
            drawCircle(brush = brush, radius = radiusPx, center = Offset.Zero)
        }
    }
}

private class AppLockGlow(
    val color: Color,
    /** Centre, as a fraction of the box. */
    val centerX: Float,
    val centerY: Float,
    /** Radii, as fractions of the box's width and height. */
    val radiusX: Float,
    val radiusY: Float,
    /** Where the colour has fully faded, as a fraction of the radius. */
    val fade: Float
)

private val appLockGlows = listOf(
    AppLockGlow(AppLockAuroraSoft, 0.50f, 0.46f, 0.70f, 0.44f, 0.70f),
    AppLockGlow(AppLockAuroraStrong, 0.12f, 0.06f, 0.66f, 0.44f, 0.62f),
    // The top-left glow, mirrored: same colour, same radii, same fade, centre reflected
    // through the middle of the box (0.12/0.06 -> 0.88/0.94).
    AppLockGlow(AppLockAuroraStrong, 0.88f, 0.94f, 0.66f, 0.44f, 0.62f)
)
