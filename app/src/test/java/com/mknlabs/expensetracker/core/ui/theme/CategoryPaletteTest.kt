package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Freezes the category and payment palette against the design pass it came from, and
 * against the ids the app actually ships.
 *
 * The two directions this test cares about are different failures. The **id** assertions
 * catch a palette that has drifted out of step with `categoryTypeData.kt` — a colour that
 * can never be reached because nothing carries its id, or a category that can never be
 * coloured because the palette has never heard of it. The **hex** assertions catch a value
 * being edited while nobody was looking, which is the whole reason a palette is centralised
 * in the first place.
 *
 * The colour analogue of `CategoryIconCatalogTest`, which pins that every id the app seeds
 * has an icon that can actually be drawn.
 */
class CategoryPaletteTest {

    private val light = ExpenseTrackerLightColorScheme
    private val dark = ExpenseTrackerDarkColorScheme

    // ── The ids ────────────────────────────────────────────────────────────────

    @Test
    fun `every seeded category has a colour in both themes`() {
        val missingLight = categoryMap.keys - CategoryAccentLight.keys
        val missingDark = categoryMap.keys - CategoryAccentDark.keys
        assertTrue("no colour in light for $missingLight", missingLight.isEmpty())
        assertTrue("no colour in dark for $missingDark", missingDark.isEmpty())
    }

    @Test
    fun `every seeded payment method has a colour in both themes`() {
        val missingLight = paymentTypeMap.keys - PaymentAccentLight.keys
        val missingDark = paymentTypeMap.keys - PaymentAccentDark.keys
        assertTrue("no colour in light for $missingLight", missingLight.isEmpty())
        assertTrue("no colour in dark for $missingDark", missingDark.isEmpty())
    }

    @Test
    fun `the palette holds no ids the app does not ship`() {
        // Equality, not a subset check: a colour for an id nothing carries is dead weight
        // that a future reader would reasonably assume is live.
        assertEquals(categoryMap.keys, CategoryAccentLight.keys)
        assertEquals(categoryMap.keys, CategoryAccentDark.keys)
        assertEquals(paymentTypeMap.keys, PaymentAccentLight.keys)
        assertEquals(paymentTypeMap.keys, PaymentAccentDark.keys)
    }

    @Test
    fun `both themes cover the same ids`() {
        // A one-sided addition is the failure this catches: a category that is coloured in
        // daylight and grey at night, which is invisible in whichever theme the author was
        // looking at.
        assertEquals(CategoryAccentLight.keys, CategoryAccentDark.keys)
        assertEquals(PaymentAccentLight.keys, PaymentAccentDark.keys)
    }

    @Test
    fun `the seeded ranges are the documented ones`() {
        // Expenses 1-20 and 22-23 (21 was never allocated), incomes 101-105, payments 1-6.
        // Spelled out so a future renumbering has to argue with this line.
        assertEquals((1..20).toList() + listOf(22, 23) + (101..105).toList(), CategoryAccentLight.keys.sorted())
        assertEquals((1..6).toList(), PaymentAccentLight.keys.sorted())
    }

    // ── The hexes ──────────────────────────────────────────────────────────────

    @Test
    fun `expense and income colours match the design`() {
        val expectedLight = mapOf(
            1 to Color(0xFFFB923C), 2 to Color(0xFF38BDF8), 3 to Color(0xFFFBBF24),
            4 to Color(0xFFFACC15), 5 to Color(0xFFFB7185), 6 to Color(0xFFC084FC),
            7 to Color(0xFF60A5FA), 8 to Color(0xFF34D399), 9 to Color(0xFF2DD4BF),
            10 to Color(0xFFA78BFA), 11 to Color(0xFF4ADE80), 12 to Color(0xFFF472B6),
            13 to Color(0xFFE07A5F), 14 to Color(0xFFD97706), 15 to Color(0xFFA3E635),
            16 to Color(0xFFE11D48), 17 to Color(0xFFD4A373), 18 to Color(0xFFE879F9),
            19 to Color(0xFF2B9348), 20 to Color(0xFF00B4D8), 22 to Color(0xFF6366F1),
            23 to Color(0xFFCBD5E1),
            101 to Color(0xFF10B981), 102 to Color(0xFF06B6D4), 103 to Color(0xFF14B8A6),
            104 to Color(0xFF8B5CF6), 105 to Color(0xFFCBD5E1)
        )
        val expectedDark = expectedLight

        assertEquals(expectedLight, CategoryAccentLight)
        assertEquals(expectedDark, CategoryAccentDark)
    }

    @Test
    fun `payment method colours match the design`() {
        assertEquals(
            mapOf(
                1 to Color(0xFF9D4EDD), 2 to Color(0xFF52B788), 3 to Color(0xFF3A86FF),
                4 to Color(0xFF7209B7), 5 to Color(0xFFCBD5E1), 6 to Color(0xFF10B981)
            ),
            PaymentAccentLight
        )
        assertEquals(PaymentAccentLight, PaymentAccentDark)
    }

    @Test
    fun `the payment ids do not follow the order they were supplied in`() {
        // SalaryDeposit is 6 and OtherPayment is 5. The palette arrived with them the other
        // way round, so this is the specific mis-transcription the map is exposed to.
        assertEquals(Color(0xFF10B981), PaymentAccentLight[6])
        assertEquals(Color(0xFFCBD5E1), PaymentAccentLight[5])
        assertNotEquals(PaymentAccentLight[5], PaymentAccentLight[6])
    }

    // ── The roles ──────────────────────────────────────────────────────────────

    @Test
    fun `the roles pick the map that belongs to the active theme`() {
        assertEquals(CategoryAccentLight, light.categoryAccent)
        assertEquals(CategoryAccentDark, dark.categoryAccent)
        assertEquals(PaymentAccentLight, light.paymentAccent)
        assertEquals(PaymentAccentDark, dark.paymentAccent)
    }

    @Test
    fun `the fallback is the brand ink in both themes`() {
        assertEquals(light.accentInk, light.categoryAccentFallback)
        assertEquals(dark.accentInk, dark.categoryAccentFallback)
    }

    @Test
    fun `the wash is lighter in light than in dark`() {
        // The asymmetry accentSoft already uses: 14% of a pastel over near-black is a wash,
        // while 14% of a deep tone over white is a stain.
        //
        // Tolerance is one 8-bit step (1/255), not a float epsilon: `copy` on a colour that
        // came from a packed literal quantises each component to a byte, so 0.10f reads back
        // as 26/255. Asserting to four decimal places would be asserting against the
        // storage format rather than against the design.
        val byteStep = 1f / 255f
        assertEquals(CategorySoftAlphaLight, CategorySoftAlphaDark, 0f)
        val color = Color(0xFFFB923C)
        assertEquals(CategorySoftAlphaLight, light.categorySoft(color).alpha, byteStep)
        assertEquals(CategorySoftAlphaDark, dark.categorySoft(color).alpha, byteStep)
        assertEquals(color.red, light.categorySoft(color).red, byteStep)
        assertEquals(color.copy(alpha = CategorySoftAlphaLight), light.categorySoft(color))
    }

    // ── The resolver ───────────────────────────────────────────────────────────

    @Test
    fun `a seeded category resolves to its own palette colour`() {
        assertEquals(Color(0xFFFB923C), light.categoryColor(1))
        assertEquals(Color(0xFFFB923C), dark.categoryColor(1))
        assertEquals(Color(0xFF52B788), light.paymentColor(2))
    }

    @Test
    fun `an unknown id resolves to the brand ink`() {
        // 106 is the first id a user-created category can take, so this is the ordinary
        // case for anything the palette has never seen rather than an edge one.
        assertEquals(light.accentInk, light.categoryColor(106))
        assertEquals(dark.accentInk, dark.categoryColor(9999))
        assertEquals(light.accentInk, light.paymentColor(7))
    }

    @Test
    fun `an unparseable stored colour falls through to the palette`() {
        // Rather than drawing nothing, or drawing an arbitrary colour derived from a
        // malformed string.
        assertEquals(Color(0xFFFB923C), light.categoryColor(1, "not a colour"))
        assertEquals(Color(0xFFFB923C), light.categoryColor(1, ""))
        assertEquals(Color(0xFFFB923C), light.categoryColor(1, null))
        assertEquals(Color(0xFFFB923C), light.categoryColor(1, "#12345"))
    }

    @Test
    fun `a row with no id yet takes the pick, or the brand ink when there is none`() {
        // The icon picker previews the colour the row is about to be given, before the row exists
        // to be looked up — so there is no palette step to fall back to, and "nothing chosen yet"
        // has to be the brand ink rather than whichever colour happened to sit at some id.
        assertEquals(light.accentInk, light.identityColor())
        assertEquals(dark.accentInk, dark.identityColor(null))
        assertEquals(Color(0xFFFF0000), light.identityColor("#FF0000"))
    }

    @Test
    fun `the id-less resolver adapts a pick the same way the row resolver does`() {
        // Both routes have to agree, or the swatch the user taps and the tile it previews would
        // show two different colours for one choice.
        assertEquals(
            light.categoryColor(1, "#AAAAAA"),
            light.identityColor("#AAAAAA")
        )
    }

    @Test
    fun `a stored pick wins over the palette`() {
        // Pure red already clears 3:1 on the white card, so it is returned untouched.
        assertEquals(Color(0xFFFF0000), light.categoryColor(1, "#FF0000"))
        assertEquals(Color(0xFFFF0000), dark.paymentColor(1, "#FF0000"))
    }

    @Test
    fun `a pick that cannot be read in this theme is adapted rather than drawn as-is`() {
        // A pale pick on the white card: 2.32:1 as given, so the resolver has to move it.
        val pale = Color(0xFFAAAAAA)
        assertTrue(contrastRatio(pale, light.surface) < GLYPH_MIN_CONTRAST)

        val resolved = light.categoryColor(1, "#AAAAAA")
        assertNotEquals(pale, resolved)
        assertTrue(
            "adapted colour still below the glyph floor",
            contrastRatio(resolved, light.surface) >= GLYPH_MIN_CONTRAST
        )
    }

    @Test
    fun `a palette colour is never adapted, even when it is the tightest one`() {
        // Shopping is #D97706 at 3.19:1 on the white card — the closest any entry comes to
        // the floor. It must arrive verbatim, because the design signed off on that value;
        // running the palette through the adapter is what the file comment forbids.
        assertEquals(Color(0xFFFBBF24), light.categoryColor(3))
        assertEquals(Color(0xFFF472B6), light.categoryColor(12))
        assertEquals(Color(0xFFD4A373), light.categoryColor(17))
    }
}
