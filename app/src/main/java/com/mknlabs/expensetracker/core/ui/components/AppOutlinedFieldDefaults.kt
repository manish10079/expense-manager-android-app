package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.core.ui.theme.accentInk

/**
 * Shared outlined-field chrome from the itemized calculator "new item" inputs:
 * 22dp corners, transparent fill, accent ink when focused, outlineVariant when not.
 */
object AppOutlinedFieldDefaults {
    val shape: Shape = RoundedCornerShape(22.dp)

    @Composable
    fun colors() = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedBorderColor = MaterialTheme.colorScheme.accentInk,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
        cursorColor = MaterialTheme.colorScheme.accentInk,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedLabelColor = MaterialTheme.colorScheme.accentInk,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
