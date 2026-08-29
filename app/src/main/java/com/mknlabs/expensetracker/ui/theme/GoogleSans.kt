package com.mknlabs.expensetracker.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mknlabs.expensetracker.R

/**
 * Google Sans font family — used for display, headlines, titles, and labels.
 *
 * Provides three weights: Regular (400), Medium (500), Bold (700).
 */
val GoogleSansFont = FontFamily(
    Font(R.font.google_sans_regular, FontWeight.Normal),
    Font(R.font.google_sans_medium, FontWeight.Medium),
    Font(R.font.google_sans_bold, FontWeight.Bold)
)
