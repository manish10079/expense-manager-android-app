package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.core.ui.theme.CategoryAccentDark
import com.mknlabs.expensetracker.core.ui.theme.CategoryAccentLight
import com.mknlabs.expensetracker.core.ui.theme.parseHexColorOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Covers the writer's half of the colour contract.
 *
 * The storage column, the cloud document and the backup file should only ever hold one shape
 * of colour, which is why normalisation happens at the boundary rather than at each of the
 * three. That makes this function the single place a shape can go wrong, and makes it worth
 * testing directly rather than only through a database.
 */
class ColorHexTest {

    @Test
    fun acceptsTheCanonicalForm_andLeavesItAlone() {
        assertEquals("#5B2EED", normalizeColorHexOrNull("#5B2EED"))
    }

    @Test
    fun upperCasesAndToleratesAMissingSigil() {
        assertEquals("#5B2EED", normalizeColorHexOrNull("5b2eed"))
        assertEquals("#5B2EED", normalizeColorHexOrNull("  #5b2eed "))
    }

    @Test
    fun dropsAlphaRatherThanStoringIt() {
        // A user picks a colour, not an opacity. Transparency is a drawing decision — the wash
        // behind a glyph, a disabled state — so an alpha arriving from elsewhere is reduced to
        // its RGB rather than allowed to leak into a column the renderer treats as
        // authoritative.
        assertEquals("#FF0000", normalizeColorHexOrNull("#80FF0000"))
        assertEquals("#FF0000", normalizeColorHexOrNull("#00FF0000"))
        assertEquals("#5B2EED", normalizeColorHexOrNull("#FF5B2EED"))
    }

    @Test
    fun returnsNullForAnythingThatIsNotAColour() {
        // Null is what the caller turns into "no override", which resolves from the palette.
        // Storing an unreadable value instead would leave the renderer defending against its
        // own database.
        assertNull(normalizeColorHexOrNull(null))
        assertNull(normalizeColorHexOrNull(""))
        assertNull(normalizeColorHexOrNull("   "))
        assertNull(normalizeColorHexOrNull("#"))
        assertNull(normalizeColorHexOrNull("#12345"))
        assertNull(normalizeColorHexOrNull("#1234567"))
        assertNull(normalizeColorHexOrNull("not a colour"))
        assertNull(normalizeColorHexOrNull("#GGGGGG"))
        assertNull(normalizeColorHexOrNull("0x5B2EED"))
    }

    @Test
    fun everyPaletteEntrySurvivesTheNormaliserUnchanged() {
        // The picker offers these swatches, so each one has to be storable exactly as drawn.
        // A normaliser that altered a palette value would mean the colour a user picked is not
        // quite the colour they get back.
        (CategoryAccentLight.values + CategoryAccentDark.values).forEach { color ->
            val canonical = color.toHex()
            assertEquals(canonical, normalizeColorHexOrNull(canonical))
        }
    }

    @Test
    fun everythingTheNormaliserStores_canBeReadBack() {
        // The writer and the reader disagree on purpose — the reader tolerates a missing sigil
        // and an alpha, the writer strips them — but they must never disagree about whether a
        // stored value is a colour at all. If they did, the picker would store a colour and the
        // screen would draw the fallback, with nothing in the database to suggest why.
        listOf("#5B2EED", "5B2EED", "#80FF0000", "  #5b2eed  ").forEach { input ->
            val stored = normalizeColorHexOrNull(input)
            assertNotNull("expected '$input' to be storable", stored)

            val parsed = parseHexColorOrNull(stored)
            assertNotNull("the parser could not read back '$stored', stored from '$input'", parsed)

            assertEquals("'$input' did not survive the round trip", stored, parsed!!.toHex())
        }
    }

    private fun androidx.compose.ui.graphics.Color.toHex(): String =
        "#" + listOf(red, green, blue).joinToString("") {
            ((it * 255f).toInt()).toString(16).padStart(2, '0').uppercase()
        }
}
