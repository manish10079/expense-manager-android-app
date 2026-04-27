package com.mkn0079.expensetracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val PurplePrimary = Color(0xFF7B61FF)
val PurpleAccent = Color(0xFFCDBDFF)
val PurpleGlow = Color(0xFFBFA6FF)

val BackgroundDark = Color(0xFF0A0A0A)
val SurfaceDark = Color(0xFF242423)
val CardDark = Color(0xFF353534)

val TextPrimaryDark = Color(0xFFEAEAEA)
val TextSecondaryDark = Color(0xFF9E9E9E)

val DividerDark = Color(0xFF2A2A2A)

val IncomeGreen = Color(0xFF81C784)
val ExpenseRed = Color(0xFFFF7D7D)

val PurplePrimaryLight = Color(0xFF6A4DFF)
val PurpleAccentLight = Color(0xFF8C6DFF)

val BackgroundLight = Color(0xFFF6F6F8)
val SurfaceLight = Color(0xFFFFFFFF)
val CardLight = Color(0xFFD2D2E3)

val TextPrimaryLight = Color(0xFF1A1A1A)
val TextSecondaryLight = Color(0xFF6E6E73)

val DividerLight = Color(0xFFE2E2E6)

val DarkGradientStart = Color(0xFF6C5AE1)
val DarkGradientEnd = Color(0xFF282626)

val LightGradientStart = Color(0xFFF8F5FF)
val LightGradientEnd = Color(0xFF9480EE)

val IconColor = Color(0xFFCDBDFF)

private val DarkOnPrimary = Color(0xFF24114C)
private val DarkPrimaryContainer = Color(0xFF2D243F)
private val DarkOnPrimaryContainer = Color(0xFFF0E9FF)
private val DarkSecondaryContainer = Color(0xFF3D3159)
private val DarkOnSecondaryContainer = Color(0xFFE2D8FF)
private val DarkTertiary = Color(0xFFFFB74D)
private val DarkOnTertiary = Color(0xFF24114C)
private val DarkTertiaryContainer = Color(0xFF533B2A)
private val DarkErrorContainer = Color(0xFF4B1E20)
private val DarkOnErrorContainer = Color(0xFFFFAAA0)

internal val ExpenseTrackerDarkColorScheme: ColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = PurpleAccent,
    onSecondary = DarkOnPrimary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    error = ExpenseRed,
    onError = DarkOnPrimary,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerDark,
    outlineVariant = DividerDark,
    scrim = BackgroundDark
)

internal val ExpenseTrackerLightColorScheme: ColorScheme = lightColorScheme()
