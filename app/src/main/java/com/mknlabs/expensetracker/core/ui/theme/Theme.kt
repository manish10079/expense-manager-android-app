package com.mknlabs.expensetracker.core.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Detected from the field rather than from `primary`, which the spec retarget moved off
// the legacy #7B61FF and onto the CTA fill. Keying on the background keeps this correct
// through any future brand change: only the two app fields are ever passed here.
private fun ColorScheme.isExpenseTrackerDarkPalette(): Boolean {
    return background == BackgroundDark
}

val ColorScheme.systemBarColor: Color
    get() = background

val ColorScheme.useDarkSystemBarIcons: Boolean
    get() = systemBarColor.luminance() > 0.5f

// The spec's income ink. Both themes' `tertiary` now hold exactly that value (#3DDC97
// dark, #15803D light), so the role is simply the tertiary rather than a special case.
val ColorScheme.income: Color
    get() = tertiary

/**
 * Third text weight, below [ColorScheme.onSurfaceVariant]: timestamps, counts and
 * hints that annotate a row rather than being part of it.
 *
 * A token of its own because Material has only two ink slots, and the screens that
 * wanted a third were reaching for a hardcoded grey instead — which is how an app
 * ends up with six slightly different greys.
 */
val ColorScheme.textTertiary: Color
    get() = if (isExpenseTrackerDarkPalette()) TextTertiaryDark else TextTertiaryLight

val ColorScheme.expense: Color get() = error

/**
 * The hairline that rules one row off from the next.
 *
 * A token of its own because the app drew this separator several ways — half strength,
 * two-fifths strength, and once as the divider token outright — and the light redesign
 * needs them all to be the one specified #E8EBEF line. Light therefore takes the
 * divider colour at full strength; dark keeps the two-fifths wash these separators have
 * always been drawn with, which is what stops this from quietly re-ruling every dark
 * screen that reads it.
 */
val ColorScheme.hairline: Color
    get() = if (isDark) outlineVariant.copy(alpha = 0.4f) else outline

@Composable
private fun ApplySystemBarStyle(darkTheme: Boolean) {
    val view = LocalView.current

    if (view.isInEditMode) return

    SideEffect {
        val window = (view.context as Activity).window
        
        // Task 5: Fix System Bars (Android 15+ Compatible)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
    }
}

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    typography: Typography = Typography,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        ExpenseTrackerDarkColorScheme
    } else {
        ExpenseTrackerLightColorScheme
    }

    ApplySystemBarStyle(darkTheme = darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}
