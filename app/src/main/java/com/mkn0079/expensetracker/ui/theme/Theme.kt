package com.mkn0079.expensetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun ColorScheme.isExpenseTrackerDarkPalette(): Boolean {
    return primary == PurplePrimary && background == BackgroundDark
}

val ColorScheme.systemBarColor: Color
    get() = background

val ColorScheme.useDarkSystemBarIcons: Boolean
    get() = systemBarColor.luminance() > 0.5f

val ColorScheme.income: Color
    get() = if (isExpenseTrackerDarkPalette()) IncomeGreen else tertiary

val ColorScheme.expense: Color get() = error

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
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
