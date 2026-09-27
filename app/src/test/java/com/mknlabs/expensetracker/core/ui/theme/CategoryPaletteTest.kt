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
            1 to Color(0xFF5B2EED), 2 to Color(0xFF0288D1), 3 to Color(0xFFD97706),
            4 to Color(0xFF059669), 5 to Color(0xFFDC2626), 6 to Color(0xFF9333EA),
            7 to Color(0xFFEA580C), 8 to Color(0xFF059669), 9 to Color(0xFF2563EB),
            10 to Color(0xFF7C3AED), 11 to Color(0xFF0288D1), 12 to Color(0xFFD97706),
            13 to Color(0xFF9333EA), 14 to Color(0xFFEA580C), 15 to Color(0xFF2563EB),
            16 to Color(0xFFDC2626), 17 to Color(0xFFD97706), 18 to Color(0xFF9333EA),
            19 to Color(0xFF059669), 20 to Color(0xFF4B5563), 22 to Color(0xFF0288D1),
            23 to Color(0xFF4B5563),
            101 to Color(0xFF059669), 102 to Color(0xFF5B2EED), 103 to Color(0xFF7C3AED),
            104 to Color(0xFF2563EB), 105 to Color(0xFF4B5563)
        )
        val expectedDark = mapOf(
            1 to Color(0xFF7A52FF), 2 to Color(0xFF4FC3F7), 3 to Color(0xFFF5C542),
            4 to Color(0xFF3DDC97), 5 to Color(0xFFFF5C5C), 6 to Color(0xFFC77DFF),
            7 to Color(0xFFFF9F45), 8 to Color(0xFF3DDC97), 9 to Color(0xFF6BA6FF),
            10 to Color(0xFFA78BFA), 11 to Color(0xFF4FC3F7), 12 to Color(0xFFF5C542),
            13 to Color(0xFFC77DFF), 14 to Color(0xFFFF9F45), 15 to Color(0xFF6BA6FF),
            16 to Color(0xFFFF6B6B), 17 to Color(0xFFF5C542), 18 to Color(0xFFC77DFF),
            19 to Color(0xFF3DDC97), 20 to Color(0xFFA5A1B8), 22 to Color(0xFF4FC3F7),
            23 to Color(0xFFA5A1B8),
            101 to Color(0xFF3DDC97), 102 to Color(0xFF7A52FF), 103 to Color(0xFFA78BFA),
            104 to Color(0xFF6BA6FF), 105 to Color(0xFFA5A1B8)
        )

        assertEquals(expectedLight, CategoryAccentLight)
        assertEquals(expectedDark, CategoryAccentDark)
    }

    @Test
    fun `payment method colours match the design`() {
        assertEquals(
            mapOf(
                1 to Color(0xFF5B2EED), 2 to Color(0xFF059669), 3 to Color(0xFF2563EB),
                4 to Color(0xFF9333EA), 5 to Color(0xFF4B5563), 6 to Color(0xFF059669)
            ),
            PaymentAccentLight
        )
        assertEquals(
            mapOf(
                1 to Color(0xFF7A52FF), 2 to Color(0xFF3DDC97), 3 to Color(0xFF6BA6FF),
                4 to Color(0xFFC77DFF), 5 to Color(0xFFA5A1B8), 6 to Color(0xFF3DDC97)
            ),
            PaymentAccentDark
        )
    }

    @Test
    fun `the payment ids do not follow the order they were supplied in`() {
        // SalaryDeposit is 6 and OtherPayment is 5. The palette arrived with them the other
        // way round, so this is the specific mis-transcription the map is exposed to.
        assertEquals(Color(0xFF059669), PaymentAccentLight[6])
        assertEquals(Color(0xFF4B5563), PaymentAccentLight[5])
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
        assertTrue(CategorySoftAlphaLight < CategorySoftAlphaDark)
        val color = Color(0xFF5B2EED)
        assertEquals(CategorySoftAlphaLight, light.categorySoft(color).alpha, byteStep)
        assertEquals(CategorySoftAlphaDark, dark.categorySoft(color).alpha, byteStep)
        assertEquals(color.red, light.categorySoft(color).red, byteStep)
        assertEquals(color.copy(alpha = CategorySoftAlphaLight), light.categorySoft(color))
    }

    // ── The resolver ───────────────────────────────────────────────────────────

    @Test
    fun `a seeded category resolves to its own palette colour`() {
        assertEquals(Color(0xFF5B2EED), light.categoryColor(1))
        assertEquals(Color(0xFF7A52FF), dark.categoryColor(1))
        assertEquals(Color(0xFF059669), light.paymentColor(2))
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
        assertEquals(Color(0xFF5B2EED), light.categoryColor(1, "not a colour"))
        assertEquals(Color(0xFF5B2EED), light.categoryColor(1, ""))
        assertEquals(Color(0xFF5B2EED), light.categoryColor(1, null))
        assertEquals(Color(0xFF5B2EED), light.categoryColor(1, "#12345"))
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
        assertEquals(Color(0xFFD97706), light.categoryColor(3))
        assertEquals(Color(0xFFD97706), light.categoryColor(12))
        assertEquals(Color(0xFFD97706), light.categoryColor(17))
    }
}
