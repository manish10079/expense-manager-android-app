package com.mknlabs.expensetracker.feature.analytics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The donut's colour, read back off the pixels instead of off the model.
 *
 * The ViewModel test proves which colour a row is *told* to draw, and the theme tests prove what the
 * resolver answers. Neither proves the chart actually paints it: the chart maps its own rows to
 * arcs, and a slice drawn from the wrong field — or from a position in a list, which is what it used
 * to be — would satisfy both of those and still come out wrong on screen.
 *
 * So this composes the real donut through the real theme and looks for the exact colour in the
 * rendered bitmap. Exact rather than approximate: every one of these is a flat fill, so a matching
 * pixel is a matching slice, and an arc drawn in another tone simply would not contain it.
 *
 * Each case is chosen so an accidental match cannot pass it:
 *
 * - the seeded case uses Health's dark `#FF5C5C`, a colour no other token in the dark scheme uses —
 *   neither the expense coral, the income mint nor either brand ink;
 * - the stored case uses a hex only a user could have picked, against an id the palette has never
 *   heard of, so finding it on the slice proves the row's override rather than agreeing with a
 *   palette entry that happened to be the same;
 * - the payment case uses id 5, where the two palettes genuinely differ: payment 5 is `#4B5563`,
 *   while category 5 is Health's `#DC2626`. Both palettes are keyed from 1, so this is the case
 *   that catches the donut resolving a payment method against the category table;
 * - and the last case asserts a colour is *absent*, because a test that only ever proves presence
 *   would pass just as happily if the chart painted every slice in every colour.
 */
class AnalyticsDonutColorRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aSeededCategorysSliceIsPaintedInItsPaletteColour() {
        val bitmap = renderCategoryDonut(
            darkTheme = true,
            breakdown = listOf(row(id = 5, label = "Health", fraction = 0.7f))
        )

        assertTrue(
            "no pixel of the donut carried Health's dark palette colour",
            bitmap.containsExactly(Color(0xFFFB7185))
        )
    }

    @Test
    fun aStoredColourWinsOverThePaletteForThatId() {
        val bitmap = renderCategoryDonut(
            darkTheme = false,
            breakdown = listOf(row(id = 106, label = "Coffee", fraction = 0.7f, colorHex = "#9333EA"))
        )

        assertTrue(
            "no pixel of the donut carried the stored colour",
            bitmap.containsExactly(Color(0xFF9333EA))
        )
    }

    @Test
    fun aStoredColourDoesNotAlsoLeaveTheSliceInSomeOtherTone() {
        // The slice is one colour, not two. If the chart painted the row's colour for part of the
        // arc and something else for the rest, the presence test above would still pass; this is
        // the half that says the palette entry never made it onto the same slice.
        val bitmap = renderCategoryDonut(
            darkTheme = false,
            breakdown = listOf(row(id = 106, label = "Coffee", fraction = 0.7f, colorHex = "#9333EA"))
        )

        assertFalse(
            "the donut drew a colour the row does not carry",
            bitmap.containsExactly(Color(0xFF5B2EED))
        )
    }

    @Test
    fun aPaymentSliceUsesThePaymentPaletteAndNotTheCategoryOne() {
        val bitmap = renderPaymentDonut(
            darkTheme = false,
            breakdown = listOf(
                PaymentTypeBreakdownUi(
                    id = 5,
                    label = "Other",
                    amountDisplay = "-100.00",
                    fraction = 0.7f,
                    percentLabel = 70,
                    icon = Icons.Filled.Payments
                )
            )
        )

        assertTrue(
            "no pixel of the donut carried the payment palette colour",
            bitmap.containsExactly(Color(0xFFCBD5E1))
        )
        assertFalse(
            "the donut resolved a payment method against the category palette",
            bitmap.containsExactly(Color(0xFFFB7185))
        )
    }

    private fun renderCategoryDonut(
        darkTheme: Boolean,
        breakdown: List<CategoryBreakdownUi>
    ): ImageBitmap = render(darkTheme) {
        SpendingDonutChart(breakdown = breakdown)
    }

    private fun renderPaymentDonut(
        darkTheme: Boolean,
        breakdown: List<PaymentTypeBreakdownUi>
    ): ImageBitmap = render(darkTheme) {
        PaymentDonutChart(breakdown = breakdown)
    }

    /**
     * Composes [chart] on the theme's own surface and captures it.
     *
     * The surface matters: the donut draws a track behind its slices and the arcs are drawn over
     * it, so the capture has to be of the background the chart expects rather than a transparent
     * one, or the anti-aliased edges would blend against black and the flat fills would come back
     * mixed.
     */
    private fun render(darkTheme: Boolean, chart: @androidx.compose.runtime.Composable () -> Unit): ImageBitmap {
        composeTestRule.setContent {
            ExpenseTrackerTheme(darkTheme = darkTheme) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    chart()
                }
            }
        }
        composeTestRule.waitForIdle()
        return composeTestRule.onRoot().captureToImage()
    }

    private fun row(
        id: Int,
        label: String,
        fraction: Float,
        colorHex: String? = null
    ) = CategoryBreakdownUi(
        id = id,
        label = label,
        amountDisplay = "-700.00",
        fraction = fraction,
        percentLabel = (fraction * 100).toInt(),
        colorHex = colorHex
    )
}

/** True when any pixel of [this] is exactly [color]. */
private fun ImageBitmap.containsExactly(color: Color): Boolean {
    val target = color.toArgb()
    val pixels = toPixelMap()
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (pixels[x, y].toArgb() == target) return true
        }
    }
    return false
}
