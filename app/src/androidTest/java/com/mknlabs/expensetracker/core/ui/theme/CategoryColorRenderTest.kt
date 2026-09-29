package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The one claim about the palette that a plain unit test cannot make: that it follows the
 * **app's** theme, not the system's.
 *
 * `CategoryPaletteTest` builds a `ColorScheme` directly and asks it what colour id 1 is, which
 * proves the maps and the roles agree but says nothing about which scheme a running screen is
 * handed. The palette resolves through `isDark`, which reads the active scheme's background — so
 * a screen wrapped in the app's own dark theme gets the dark map even on a device sitting in
 * light mode. That is the behaviour under test, and it is why a category colour cannot disagree
 * with the surface behind it when the user overrides the system setting.
 *
 * Composed through the real [ExpenseTrackerTheme] rather than by constructing a scheme, so the
 * wiring between the two is covered as well.
 *
 * `setContent` may be called only once per test, so a case that needs both themes composes them
 * as two sibling themes inside one composition rather than re-composing the rule. Nesting is
 * exactly what a theme is for, and it puts the light and dark reads in the same frame, which is
 * a stronger comparison than two separate compositions would be.
 *
 * [resolve] is `@Composable` because reading the scheme is: the point is to ask the running theme
 * what colour a screen would get, not to ask a `ColorScheme` object on the side.
 */
class CategoryColorRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Composes [resolve] under one theme and returns what the composition read. */
    private fun resolvedUnder(darkTheme: Boolean, resolve: @Composable () -> Color): Color {
        var captured: Color? = null
        composeTestRule.setContent {
            ExpenseTrackerTheme(darkTheme = darkTheme) {
                captured = resolve()
            }
        }
        composeTestRule.waitForIdle()
        return requireNotNull(captured) { "the theme resolved no colour" }
    }

    /** Composes [resolve] under both themes at once, returning light to dark. */
    private fun resolvedInBothThemes(resolve: @Composable () -> Color): Pair<Color, Color> {
        var light: Color? = null
        var dark: Color? = null
        composeTestRule.setContent {
            ExpenseTrackerTheme(darkTheme = false) { light = resolve() }
            ExpenseTrackerTheme(darkTheme = true) { dark = resolve() }
        }
        composeTestRule.waitForIdle()
        return requireNotNull(light) to requireNotNull(dark)
    }

    @Test
    fun aSeededCategoryDrawsItsPaletteColourInTheLightTheme() {
        val color = resolvedUnder(darkTheme = false) {
            MaterialTheme.colorScheme.categoryColor(categoryId = 1)
        }
        assertEquals(Color(0xFF5B2EED), color) // Food, light
    }

    @Test
    fun theSameCategoryDrawsItsDarkPaletteColourUnderTheAppToggle() {
        // Same id, same call, and the device's own setting is not consulted. Food's two hexes
        // are deliberately different values rather than one shared colour, which is what makes
        // this assertion worth making.
        val color = resolvedUnder(darkTheme = true) {
            MaterialTheme.colorScheme.categoryColor(categoryId = 1)
        }
        assertEquals(Color(0xFF7A52FF), color) // Food, dark
        assertNotEquals(Color(0xFF5B2EED), color)
    }

    @Test
    fun switchingTheAppThemeSwitchesTheColourWithIt() {
        val (light, dark) = resolvedInBothThemes {
            MaterialTheme.colorScheme.categoryColor(categoryId = 5)
        }

        assertEquals(Color(0xFFDC2626), light) // Health, light
        assertEquals(Color(0xFFFF5C5C), dark)  // Health, dark
    }

    @Test
    fun aCategoryWithNoColourOfItsOwnGetsThePalette_andAnUnknownOneGetsTheBrandInk() {
        // The ordinary case for every seeded category: a null override resolves from the
        // palette. That is what the per-launch reseed of those rows depends on, and what makes
        // leaving the column unset the correct state rather than a gap to be filled in.
        var seeded: Color? = null
        var unknown: Color? = null
        composeTestRule.setContent {
            ExpenseTrackerTheme(darkTheme = false) {
                seeded = MaterialTheme.colorScheme.categoryColor(categoryId = 3, colorHex = null)
                unknown = MaterialTheme.colorScheme.categoryColor(categoryId = 9999, colorHex = null)
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(Color(0xFFD97706), seeded) // Shopping, light
        assertEquals(ExpenseTrackerLightColorScheme.accentInk, unknown)
    }

    @Test
    fun aStoredColourIsMadeLegibleForTheOtherTheme() {
        // A pick made in light mode, read under the app's dark theme. It has to arrive at
        // something that clears the glyph floor, because that is the promise the adapter makes
        // and the reason offering the palette as swatches is safe rather than merely convenient.
        // The light case comes back verbatim: it already clears the floor there.
        val (onLight, onDark) = resolvedInBothThemes {
            MaterialTheme.colorScheme.categoryColor(categoryId = 106, colorHex = "#5B2EED")
        }

        assertEquals(Color(0xFF5B2EED), onLight)
        assertTrue(
            "the pick was not made legible for the dark surface",
            contrastRatio(onDark, ExpenseTrackerDarkColorScheme.surface) >= GLYPH_MIN_CONTRAST
        )
    }

    @Test
    fun aPickAlreadyLegibleInBothThemesIsReturnedUntouched() {
        // The adapter has to be a no-op when it has nothing to fix, or a user's colour would
        // drift the moment it was stored and the same hex would come back a different shade.
        val (onLight, onDark) = resolvedInBothThemes {
            MaterialTheme.colorScheme.categoryColor(categoryId = 1, colorHex = "#0288D1")
        }

        assertEquals(Color(0xFF0288D1), onLight)
        assertEquals(Color(0xFF0288D1), onDark)
    }
}
