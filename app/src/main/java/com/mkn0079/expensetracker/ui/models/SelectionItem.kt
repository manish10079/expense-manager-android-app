package com.mkn0079.expensetracker.ui.models

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A generic model for items in a selection list.
 * @param id The unique identifier/value of the item.
 * @param title The primary text to display.
 * @param titleRes Optional primary text resource.
 * @param subtitle Optional secondary text.
 * @param subtitleRes Optional secondary text resource.
 * @param leadingText Optional text to show in the leading box (e.g., currency symbol).
 * @param leadingIcon Optional icon to show in the leading box.
 */
data class SelectionItem<T>(
    val id: T,
    val title: String = "",
    @StringRes val titleRes: Int = 0,
    val subtitle: String? = null,
    @StringRes val subtitleRes: Int = 0,
    val leadingText: String? = null,
    val leadingIcon: ImageVector? = null
)
