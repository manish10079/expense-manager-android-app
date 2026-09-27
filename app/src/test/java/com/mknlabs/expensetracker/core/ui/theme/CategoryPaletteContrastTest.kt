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

    /** The amber trio: Shopping (3), Gifts (12), Pets (17). */
    private val lightTileExceptions = setOf(3, 12, 17)

    @Test
    fun `every category clears the glyph floor on its own theme's card`() {
        assertPaletteClearsTheFloor(CategoryAccentLight, light.surface, "light")
        assertPaletteClearsTheFloor(CategoryAccentDark, dark.surface, "dark")
    }

    @Test
    fun `every payment method clears the glyph floor on its own theme's card`() {
        assertPaletteClearsTheFloor(PaymentAccentLight, light.surface, "light")
        assertPaletteClearsTheFloor(PaymentAccentDark, dark.surface, "dark")
    }

    @Test
    fun `the palette clears the floor on the dark field as well as the dark card`() {
        // A category glyph can also land on the field itself rather than on a card — the
        // SMS inbox and the goals list both draw onto the background.
        assertPaletteClearsTheFloor(CategoryAccentLight, light.surface, "light card")
        assertPaletteClearsTheFloor(CategoryAccentDark, dark.background, "dark background")
        assertPaletteClearsTheFloor(PaymentAccentDark, dark.background, "dark background")
    }

    @Test
    fun `the amber trio also misses the floor on the light field, by a hair`() {
        // Measured at 2.998:1 on #F7F8FA, against 3.19:1 on the white card. The same three
        // entries as the tile case, so light mode has exactly one weak colour and it is the
        // same colour everywhere it appears. Pinned rather than exempted: if a future pass
        // deepens the amber, this test and the tile exception should both fail together.
        val offenders = CategoryAccentLight
            .filterValues { contrastRatio(it, light.background) < GLYPH_MIN_CONTRAST }
            .keys

        assertEquals("the entries that miss the floor on the light field have changed", lightTileExceptions, offenders)

        val amber = Color(0xFFD97706)
        assertEquals(3.00f, contrastRatio(amber, light.background), 0.01f)
        assertEquals(3.19f, contrastRatio(amber, light.surface), 0.01f)
    }

    @Test
    fun `the palette's own light and dark split is load-bearing`() {
        // Why both hexes exist rather than one shared value. Food is the sharpest example:
        // the light purple is 6.87:1 on the white card but only 2.68:1 on the near-black
        // one, so a single value could not serve both themes. This is also the case the
        // picker's adapter exists to rescue — see the sweep below.
        val foodLight = Color(0xFF5B2EED)
        val foodDark = Color(0xFF7A52FF)

        assertTrue(contrastRatio(foodLight, light.surface) > GLYPH_MIN_CONTRAST)
        assertTrue("the light purple should not survive on the dark card", contrastRatio(foodLight, dark.surface) < GLYPH_MIN_CONTRAST)
        assertTrue(contrastRatio(foodDark, dark.surface) > GLYPH_MIN_CONTRAST)
    }

    @Test
    fun `a pick taken from either palette survives being drawn in the other theme`() {
        // The picker offers these swatches, so a swatch chosen in light mode has to stay
        // legible when the same category is drawn in dark mode, and the reverse. This is
        // what makes offering the palette as swatches safe rather than merely convenient:
        // every one of the 66 entries passes through the adapter in both directions.
        (CategoryAccentLight.values + PaymentAccentLight.values).forEach { pick ->
            val inDark = adaptForContrast(pick, dark.surface, GLYPH_MIN_CONTRAST)
            assertTrue(
                "light pick $pick is illegible on the dark card even after adaptation",
                contrastRatio(inDark, dark.surface) >= GLYPH_MIN_CONTRAST
            )
        }
        (CategoryAccentDark.values + PaymentAccentDark.values).forEach { pick ->
            val inLight = adaptForContrast(pick, light.surface, GLYPH_MIN_CONTRAST)
            assertTrue(
                "dark pick $pick is illegible on the light card even after adaptation",
                contrastRatio(inLight, light.surface) >= GLYPH_MIN_CONTRAST
            )
        }
    }

    @Test
    fun `the amber trio is the only light palette entry that does not clear the floor on its own tile`() {
        val offenders = CategoryAccentLight
            .filterValues { contrastRatio(it, tile(it, light.surface, CategorySoftAlphaLight)) < GLYPH_MIN_CONTRAST }
            .keys

        assertEquals(
            "the set of entries that miss the floor on their own wash has changed",
            lightTileExceptions,
            offenders
        )
    }

    @Test
    fun `no dark palette entry fails on its own tile`() {
        val offenders = (CategoryAccentDark + PaymentAccentDark)
            .filterValues { contrastRatio(it, tile(it, dark.surface, CategorySoftAlphaDark)) < GLYPH_MIN_CONTRAST }
            .keys

        assertTrue(
            "dark entries unexpectedly below the floor: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `a d97706 tile really is the worst case it is documented as`() {
        // Guards the exception itself: if a future palette change makes the amber legible on
        // its tile, this test should be deleted along with the note rather than left to
        // describe a problem that no longer exists.
        val amber = Color(0xFFD97706)
        val onTile = contrastRatio(amber, tile(amber, light.surface, CategorySoftAlphaLight))

        assertTrue("amber no longer misses the floor on its tile", onTile < GLYPH_MIN_CONTRAST)
        assertEquals(2.89f, onTile, 0.02f)
    }

    @Test
    fun `the palette renders verbatim even where the adapter would move it`() {
        // Resolved through the real resolver, the amber arrives unchanged, because the
        // adapter is wired to picker-authored colours only. The card is where that matters:
        // 3.19:1 is above the floor, so there is nothing to correct.
        val amber = Color(0xFFD97706)
        assertEquals(amber, light.categoryColor(3))
        assertTrue(contrastRatio(amber, light.surface) >= GLYPH_MIN_CONTRAST)

        // The tile is the one background it does not clear, and the adapter *can* fix it
        // there — which is the point of keeping it off the palette path in the first place.
        val onOwnTile = tile(amber, light.surface, CategorySoftAlphaLight)
        assertNotEquals(amber, adaptForContrast(amber, onOwnTile, GLYPH_MIN_CONTRAST))
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
