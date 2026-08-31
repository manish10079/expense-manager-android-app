package com.mknlabs.expensetracker.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mknlabs.expensetracker.R

/**
 * Bundled Roboto font family — used for body text in APP mode.
 *
 * Provides three weights: Regular (400), Medium (500), Bold (700).
 * SemiBold (600) is synthesized by Android from Medium + Bold.
 *
 * This ensures body text always renders as Roboto regardless of
 * the device's OEM system font (Samsung Sans, MiSans, etc.).
 */
val BundledRoboto = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_bold, FontWeight.Bold)
)
