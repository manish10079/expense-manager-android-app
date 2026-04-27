package com.mkn0079.expensetracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private fun ColorScheme.isExpenseTrackerDarkPalette(): Boolean {
    return primary == PurplePrimary && background == BackgroundDark
}

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
