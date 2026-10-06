package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every colour in the palette, measured rather than eyeballed.
 *
 * A category glyph is a non-text graphic, so the floor is 3:1 (WCAG 1.4.11), and it has to
 * clear it twice over: once against the card it sits on, and once against the tile the app
 * actually draws behind it — which is the same hue at [CategorySoftAlphaLight] /
 * [CategorySoftAlphaDark], so the glyph has less room than the bare card check suggests.
 *
 * ## The documented exception
 *
 * Three light entries do not clear the floor on their own tile: Shopping, Gifts and Pets,
 * all `#D97706`, at about 2.86:1. That is a decision, not an oversight — the palette was
 * supplied with those hexes and the instruction was to keep them verbatim — so it is pinned
 * here rather than exempted. A fourth amber joining the set, or the set emptying, is a
 * change to the design and this test is where it gets noticed.
 *
 * The tile case is a wash behind a glyph that is also accompanied by an icon and a label,
 * so identity never rests on this ratio alone. Recorded so that nobody has to rediscover
 * the arithmetic.
 */
class CategoryPaletteContrastTest {

    private val light = ExpenseTrackerLightColorScheme
    private val dark = ExpenseTrackerDarkColorScheme

    @Test
    fun `light and dark seeded palettes share the lucide hexes`() {
        assertEquals(CategoryAccentLight, CategoryAccentDark)
        assertEquals(PaymentAccentLight, PaymentAccentDark)
    }

    @Test
    fun `a pick taken from either palette survives being drawn in the other theme`() {
        (CategoryAccentLight.values + PaymentAccentLight.values).distinct().forEach { pick ->
            val inDark = adaptForContrast(pick, dark.surface, GLYPH_MIN_CONTRAST)
            assertTrue(
                "pick $pick is illegible on the dark card even after adaptation",
                contrastRatio(inDark, dark.surface) >= GLYPH_MIN_CONTRAST
            )
            val inLight = adaptForContrast(pick, light.surface, GLYPH_MIN_CONTRAST)
            assertTrue(
                "pick $pick is illegible on the light card even after adaptation",
                contrastRatio(inLight, light.surface) >= GLYPH_MIN_CONTRAST
            )
        }
    }

    @Test
    fun `the palette renders seeded hexes verbatim`() {
        assertEquals(Color(0xFFD97706), light.categoryColor(14))
        assertEquals(Color(0xFFFB923C), light.categoryColor(1))
        assertEquals(Color(0xFF9D4EDD), light.paymentColor(1))
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun assertPaletteClearsTheFloor(palette: Map<Int, Color>, background: Color, label: String) {
        val offenders = palette
            .filterValues { contrastRatio(it, background) < GLYPH_MIN_CONTRAST }
            .keys
        assertTrue("$label entries below $GLYPH_MIN_CONTRAST:1 on $background: $offenders", offenders.isEmpty())
    }

    /** The tile as drawn: the colour at [alpha] composited over the card. */
    private fun tile(color: Color, card: Color, alpha: Float): Color = lerp(card, color, alpha)
}
