package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the two pure functions a stored colour passes through on its way to the screen.
 *
 * Both are ordinary JVM functions with no Compose runtime, so this runs as a plain unit
 * test — which matters, because these decide whether a colour a user picked is legible and
 * the alternative would be judging that by eye on a device, in two themes, for every pick.
 */
class HexColorTest {

    // ── parseHexColorOrNull ────────────────────────────────────────────────────

    @Test
    fun `parses a six digit hex with its sigil`() {
        assertEquals(Color(0xFF5B2EED), parseHexColorOrNull("#5B2EED"))
    }

    @Test
    fun `parses a six digit hex without its sigil`() {
        // Deliberately lenient: a value that arrived without the `#` should still render
        // as its colour rather than silently falling back to the palette.
        assertEquals(Color(0xFF5B2EED), parseHexColorOrNull("5B2EED"))
    }

    @Test
    fun `is case insensitive`() {
        assertEquals(Color(0xFF5B2EED), parseHexColorOrNull("#5b2eed"))
        assertEquals(parseHexColorOrNull("#5B2EED"), parseHexColorOrNull("#5b2eed"))
    }

    @Test
    fun `trims surrounding whitespace`() {
        assertEquals(Color(0xFF5B2EED), parseHexColorOrNull("  #5B2EED\t"))
    }

    @Test
    fun `honours an eight digit alpha rather than stripping it`() {
        // This is a parser, so it reports what the string says. Normalising to opaque is
        // the writer's job — the picker stores six digits — so that what is stored and what
        // is parsed cannot disagree in the middle.
        val parsed = parseHexColorOrNull("#805B2EED")
        assertEquals(Color(0xFF5B2EED).red, parsed!!.red, 0.0001f)
        assertEquals(0x80 / 255f, parsed.alpha, 0.0001f)
    }

    @Test
    fun `rejects anything that is not a colour`() {
        assertNull(parseHexColorOrNull(null))
        assertNull(parseHexColorOrNull(""))
        assertNull(parseHexColorOrNull("   "))
        assertNull(parseHexColorOrNull("#"))
        assertNull(parseHexColorOrNull("#12345"))     // five digits
        assertNull(parseHexColorOrNull("#1234567"))   // seven digits
        assertNull(parseHexColorOrNull("#123456789")) // nine digits
        assertNull(parseHexColorOrNull("#GGGGGG"))    // not hex
        assertNull(parseHexColorOrNull("0x5B2EED"))   // eight characters, but `x` is not hex
        assertNull(parseHexColorOrNull("not a colour"))
        assertNull(parseHexColorOrNull("#FF0000;drop"))
    }

    @Test
    fun `round trips every canonical value the picker can write`() {
        // The parse has to survive the exact form the writer produces, for every entry in
        // both palettes — otherwise a pick could be stored and then not recognised.
        val palette = CategoryAccentLight.values + PaymentAccentLight.values
        palette.forEach { color ->
            val canonical = "#" + listOf(color.red, color.green, color.blue).joinToString("") {
                ((it * 255f).toInt()).toString(16).padStart(2, '0').uppercase()
            }
            assertEquals(CANONICAL_HEX_LENGTH, canonical.length)
            assertEquals(color, parseHexColorOrNull(canonical))
        }
    }

    // ── contrastRatio ──────────────────────────────────────────────────────────

    @Test
    fun `the ratio spans one to twenty one`() {
        assertEquals(1.0f, contrastRatio(Color.White, Color.White), 0.001f)
        assertEquals(21.0f, contrastRatio(Color.White, Color.Black), 0.001f)
    }

    @Test
    fun `the ratio is symmetric`() {
        assertEquals(
            contrastRatio(Color(0xFF5B2EED), Color.White),
            contrastRatio(Color.White, Color(0xFF5B2EED)),
            0.0001f
        )
    }

    @Test
    fun `the ratios quoted in the palette notes are the ones this returns`() {
        // The design commentary cites these two figures; if the maths and the comment ever
        // disagree, the comment is what a reader believes.
        assertEquals(3.19f, contrastRatio(Color(0xFFD97706), Color.White), 0.01f)
        assertEquals(4.83f, contrastRatio(Color(0xFFDC2626), Color.White), 0.01f)
    }

    // ── adaptForContrast ───────────────────────────────────────────────────────

    @Test
    fun `a colour that already clears the floor is returned untouched`() {
        // A mid-tone that genuinely clears 3:1 on both of the app's cards: 3.86:1 on the
        // white one, 4.76:1 on the near-black one. Note this is the *only* kind of colour
        // that suits both — see `the palette's own light and dark split is load-bearing`
        // in the contrast test for why a deep tone cannot.
        val midTone = Color(0xFF0288D1)
        assertEquals(midTone, adaptForContrast(midTone, Color.White, GLYPH_MIN_CONTRAST))
        assertEquals(midTone, adaptForContrast(midTone, Color(0xFF141418), GLYPH_MIN_CONTRAST))
    }

    @Test
    fun `a colour that does not clear the floor is moved until it does`() {
        val pale = Color(0xFFAAAAAA)
        assertTrue(contrastRatio(pale, Color.White) < GLYPH_MIN_CONTRAST)

        val adapted = adaptForContrast(pale, Color.White, GLYPH_MIN_CONTRAST)
        assertNotEquals(pale, adapted)
        assertTrue(contrastRatio(adapted, Color.White) >= GLYPH_MIN_CONTRAST)
    }

    @Test
    fun `the walk goes toward the theme's ink, not to the extremes`() {
        // The whole point of stepping rather than jumping: the pick keeps as much of its own
        // colour as legibility allows, so a pale red does not become pure black.
        // #EEAA99 rather than #EE9999 so green leads blue by a clear margin and the hue
        // assertions below are about the walk rather than about a tie.
        val paleRed = Color(0xFFEEAA99)
        assertTrue(contrastRatio(paleRed, Color.White) < GLYPH_MIN_CONTRAST)
        val adapted = adaptForContrast(paleRed, Color.White, GLYPH_MIN_CONTRAST)

        assertNotEquals(Color.Black, adapted)
        assertTrue(contrastRatio(adapted, Color.White) >= GLYPH_MIN_CONTRAST)
        // Still recognisably the same hue: red leads green leads blue, as they did before.
        assertTrue("red should still lead green", adapted.red > adapted.green)
        assertTrue("green should still lead blue", adapted.green > adapted.blue)
    }

    @Test
    fun `the ink direction follows the background`() {
        // White ink over the dark card, black ink over the light one, derived from the
        // background rather than passed in.
        val midGrey = Color(0xFF808080)
        val onDark = adaptForContrast(midGrey, Color(0xFF141418), 12f)
        val onLight = adaptForContrast(midGrey, Color.White, 12f)

        assertTrue("should lighten over the dark card", onDark.red > midGrey.red)
        assertTrue("should darken over the light card", onLight.red < midGrey.red)
    }

    @Test
    fun `contrast never falls as the demand rises`() {
        // The monotonicity the stepped search relies on to know that the first step clearing
        // the ratio is also the smallest change that can.
        val start = Color(0xFFAAAAAA)
        val background = Color.White
        var previous = 0f

        listOf(1.2f, 1.5f, 2f, 3f, 4f, 6f, 9f, 15f).forEach { demand ->
            val achieved = contrastRatio(adaptForContrast(start, background, demand), background)
            assertTrue("contrast fell while the demand rose to $demand", achieved >= previous)
            previous = achieved
        }
    }

    @Test
    fun `an unreachable demand terminates at the ink rather than reverting`() {
        // Nothing can clear 21:1 against a near-black card, so the walk cannot succeed.
        // It must still return the most legible colour it can reach, not the illegible
        // original it started from.
        val adapted = adaptForContrast(Color(0xFF101014), Color(0xFF141418), 21f)
        assertNotEquals(Color(0xFF101014), adapted)
        assertEquals(Color.White, adapted)
    }
}
