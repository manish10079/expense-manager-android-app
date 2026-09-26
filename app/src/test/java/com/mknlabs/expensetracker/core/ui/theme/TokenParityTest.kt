package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Freezes the colour system against `mock-design-system.html`.
 *
 * The mock is the source of truth, so every value below is quoted from it and nothing here
 * may be changed to match the app — the test exists to make the opposite direction fail.
 * A screenshot can only say a screen "looks right"; this says the token table *is* the
 * mock's, token for token, in both themes. If a value is deliberately different from the
 * mock the test should carry the reason beside it (as the container roles do), not
 * silently drop the assertion.
 */
class TokenParityTest {

    private val dark: ColorScheme = ExpenseTrackerDarkColorScheme
    private val light: ColorScheme = ExpenseTrackerLightColorScheme

    // ── Material's own slots, retargeted to the spec ───────────────────────────

    @Test
    fun `dark scheme surfaces and brand match the spec`() {
        assertEquals(Color(0xFF0A0A0A), dark.background)      // --bg
        assertEquals(Color(0xFF141418), dark.surface)         // --s1
        assertEquals(Color(0xFF26262E), dark.surfaceVariant)  // --menu / --track
        assertEquals(Color(0xFF2A2A31), dark.outline)         // --line
        assertEquals(Color(0xFFF2F2F5), dark.onBackground)    // --tp
        assertEquals(Color(0xFFA8A8B3), dark.onSurfaceVariant) // --ts
    }

    @Test
    fun `light scheme surfaces and brand match the spec`() {
        assertEquals(Color(0xFFF7F8FA), light.background)     // --bg
        assertEquals(Color(0xFFFFFFFF), light.surface)        // --card
        assertEquals(Color(0xFFF1F2F4), light.surfaceVariant) // --track
        assertEquals(Color(0xFFE8EBEF), light.outline)        // --line
        assertEquals(Color(0xFF1F2937), light.onBackground)   // --tp
        assertEquals(Color(0xFF5B6270), light.onSurfaceVariant) // --ts
    }

    @Test
    fun `primary is the CTA fill in both themes`() {
        assertEquals(Color(0xFF5838FA), dark.primary)   // --cta, dark
        assertEquals(Color(0xFF6A4DFF), light.primary)  // --cta, light
        assertEquals(Color(0xFFFFFFFF), dark.onPrimary) // white carries both fills
        assertEquals(Color(0xFFFFFFFF), light.onPrimary)
    }

    @Test
    fun `secondary is the accent ink in both themes`() {
        assertEquals(Color(0xFF9E84FF), dark.secondary) // --accent, dark
        assertEquals(Color(0xFF6A4DFF), light.secondary) // --accent, light
    }

    @Test
    fun `containers are tints derived from spec tokens`() {
        assertEquals(Color(0x249E84FF), dark.primaryContainer) // --accentSoft dark
        assertEquals(Color(0x1A6A4DFF), light.primaryContainer) // --accentSoft light
        assertEquals(Color(0xFF2C283F), dark.secondaryContainer) // --chipSel dark
        assertEquals(Color(0xFFEDEAFF), light.secondaryContainer) // --chipSel light
        assertEquals(Color(0xFF9E84FF), dark.onSecondaryContainer) // --chipInk dark
        assertEquals(Color(0xFF5B45D6), light.onSecondaryContainer) // --chipInk light
    }

    // ── Amount inks ────────────────────────────────────────────────────────────

    @Test
    fun `semantic amount inks match the spec in both themes`() {
        assertEquals(Color(0xFF3DDC97), dark.tertiary)        // --income dark
        assertEquals(Color(0xFF15803D), light.tertiary)       // --income light
        assertEquals(Color(0xFFFF6B6B), dark.error)           // --expense dark
        assertEquals(Color(0xFFDC2626), light.error)          // --expense light
        assertEquals(Color(0xFF5EEAD4), dark.savings)         // --savings dark
        assertEquals(Color(0xFF0F766E), light.savings)        // --savings light
        assertEquals(Color(0xFFFBBF24), dark.invest)          // --invest dark
        assertEquals(Color(0xFFB45309), light.invest)         // --invest light
        assertEquals(Color(0xFF60A5FA), dark.transfer)        // --transfer dark
        assertEquals(Color(0xFF1D4ED8), light.transfer)       // --transfer light
        assertEquals(Color(0xFFFB7185), dark.debt)            // --debt dark
        assertEquals(Color(0xFFBE123C), light.debt)           // --debt light
    }

    @Test
    fun `income and expense roles read the semantic slots`() {
        assertEquals(dark.tertiary, dark.income)
        assertEquals(light.tertiary, light.income)
        assertEquals(dark.error, dark.expense)
        assertEquals(light.error, light.expense)
    }

    // ── Text weights, outlines, disabled, track ────────────────────────────────

    @Test
    fun `text weights, outlines and disabled match the spec`() {
        assertEquals(Color(0xFFF2F2F5), TextPrimaryDark)     // --tp
        assertEquals(Color(0xFFA8A8B3), TextSecondaryDark)   // --ts
        assertEquals(Color(0xFF8A8C95), TextTertiaryDark)    // --tt
        assertEquals(Color(0xFF1F2937), TextPrimaryLight)    // --tp
        assertEquals(Color(0xFF5B6270), TextSecondaryLight)  // --ts
        assertEquals(Color(0xFF6B7280), TextTertiaryLight)   // --tt

        assertEquals(Color(0xFF6E6E7A), dark.disabled)      // --dis
        assertEquals(Color(0xFFA8B0BB), light.disabled)

        assertEquals(Color(0xFF26262E), dark.track)         // --track
        assertEquals(Color(0xFFF1F2F4), light.track)
        assertEquals(Color(0xFF1E1E23), dark.sheet)         // --s3
        assertEquals(Color(0xFFFAFAFC), light.sheet)
        assertEquals(Color(0xFF26262E), dark.menu)          // --menu dark
        assertEquals(Color(0xFFFFFFFF), light.menu)         // --menu light
    }

    // ── Chips ──────────────────────────────────────────────────────────────────

    @Test
    fun `chip tokens match the spec in both themes`() {
        assertEquals(Color(0xFF242428), dark.chip)          // --chip
        assertEquals(Color(0xFFF5F5F5), light.chip)
        assertEquals(Color(0xFF2D2D31), dark.chipOutline)   // --chipLine
        assertEquals(Color(0x14000000), light.chipOutline)
        assertEquals(Color(0xFF2C283F), dark.chipSelected)  // --chipSel
        assertEquals(Color(0xFFEDEAFF), light.chipSelected)
        assertEquals(Color(0xFF9E84FF), dark.chipSelectedInk) // --chipInk
        assertEquals(Color(0xFF5B45D6), light.chipSelectedInk)
        assertEquals(Color(0xFFA8A8B3), dark.chipInkOff)    // --chipInkOff
        assertEquals(Color(0xFF5B6270), light.chipInkOff)
    }

    // ── Brand roles ────────────────────────────────────────────────────────────

    @Test
    fun `brand ink, fill and soft wash match the spec`() {
        assertEquals(Color(0xFF9E84FF), dark.accentInk)   // --accent dark
        assertEquals(Color(0xFF6A4DFF), light.accentInk)  // --accent light
        assertEquals(Color(0xFF5838FA), dark.cta)         // --cta dark
        assertEquals(Color(0xFF6A4DFF), light.cta)        // --cta light
        assertEquals(Color(0xFFFFFFFF), dark.onCta)
        assertEquals(Color(0xFFFFFFFF), light.onCta)
        assertEquals(Color(0x249E84FF), dark.accentSoft)  // --accentSoft dark
        assertEquals(Color(0x1A6A4DFF), light.accentSoft) // --accentSoft light

        assertEquals(Color(0x389E84FF), dark.glow)        // --glow dark
        assertEquals(Color(0x1A6A4DFF), light.glow)       // --glow light
    }

    // ── Ramps ──────────────────────────────────────────────────────────────────

    @Test
    fun `chart ramps match the spec in draw order`() {
        assertEquals(
            listOf(
                Color(0xFF9E84FF), Color(0xFF5EEAD4), Color(0xFFFBBF24),
                Color(0xFFFB7185), Color(0xFFBAE6FD),
            ),
            dark.chartSeries
        )
        assertEquals(
            listOf(
                Color(0xFF6A4DFF), Color(0xFF0F766E), Color(0xFFB45309),
                Color(0xFF1E3A8A), Color(0xFFDB2777),
            ),
            light.chartSeries
        )
    }

    @Test
    fun `budget trios match the spec`() {
        assertEquals(Color(0xFF34D399), dark.budgetOnTrack)
        assertEquals(Color(0xFFFBBF24), dark.budgetNearLimit)
        assertEquals(Color(0xFFF87171), dark.budgetOver)
        assertEquals(Color(0xFF15803D), light.budgetOnTrack)
        assertEquals(Color(0xFFB45309), light.budgetNearLimit)
        assertEquals(Color(0xFF7F1D1D), light.budgetOver)
    }

    // ── The membership exception ───────────────────────────────────────────────

    @Test
    fun `premium card uses the CTA ramp from the master table`() {
        assertEquals(Color(0xFF5838FA), PremiumGradientStart) // master table Premium
        assertEquals(Color(0xFF3713EC), PremiumGradientEnd)
        assertEquals(Color(0xFFFFD700), PremiumGold)
        assertEquals(Color(0xFFB388FF), PremiumBorder)
    }
}
