package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Isolated so large screens can tint a category without preview loading CategoryPaletteKt. */
@Composable
fun categoryTint(categoryId: Int, colorHex: String? = null): Color =
    MaterialTheme.colorScheme.categoryColor(categoryId = categoryId, colorHex = colorHex)
