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
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        ExpenseTrackerDarkColorScheme
    } else {
        ExpenseTrackerLightColorScheme
    }

    ApplySystemBarStyle(colorScheme = colorScheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
private fun ApplySystemBarStyle(colorScheme: ColorScheme) {
    val view = LocalView.current

    if (view.isInEditMode) return

    SideEffect {
        val window = (view.context as Activity).window
        val systemBarColor = colorScheme.systemBarColor.toArgb()
        val insetsController = WindowCompat.getInsetsController(window, view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }

        window.statusBarColor = systemBarColor
        window.navigationBarColor = systemBarColor
        insetsController.isAppearanceLightStatusBars = colorScheme.useDarkSystemBarIcons

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            insetsController.isAppearanceLightNavigationBars = colorScheme.useDarkSystemBarIcons
        }
    }
}
