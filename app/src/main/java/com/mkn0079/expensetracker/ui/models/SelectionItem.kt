package com.mkn0079.expensetracker.ui.models

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A generic model for items in a selection list.
 * @param id The unique identifier/value of the item.
 * @param title The primary text to display.
 * @param subtitle Optional secondary text.
 * @param leadingText Optional text to show in the leading box (e.g., currency symbol).
 * @param leadingIcon Optional icon to show in the leading box.
 */
data class SelectionItem<T>(
    val id: T,
    val title: String,
    val subtitle: String? = null,
    val leadingText: String? = null,
    val leadingIcon: ImageVector? = null
)
