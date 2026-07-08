package com.mknlabs.expensetracker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.MaterialTheme

/**
 * Standardized gradients for the application to ensure brand consistency.
 */

@Composable
fun brandGradient(alpha: Float = 1f): Brush {
    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
    val secondaryColor = MaterialTheme.colorScheme.secondary.copy(alpha = alpha)
    return remember(primaryColor, secondaryColor) {
        Brush.linearGradient(
            colors = listOf(primaryColor, secondaryColor)
        )
    }
}

@Composable
fun standardCardGradient(): Brush {
    val color1 = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val color2 = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    return remember(color1, color2) {
        Brush.verticalGradient(
            colors = listOf(color1, color2)
        )
    }
}

@Composable
fun surfaceGradient(): Brush {
    val color1 = MaterialTheme.colorScheme.surface
    val color2 = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    return remember(color1, color2) {
        Brush.verticalGradient(
            colors = listOf(color1, color2)
        )
    }
}

@Composable
fun subtlePrimaryGradient(): Brush {
    val color1 = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    val color2 = MaterialTheme.colorScheme.surface
    return remember(color1, color2) {
        Brush.horizontalGradient(
            colors = listOf(color1, color2)
        )
    }
}
