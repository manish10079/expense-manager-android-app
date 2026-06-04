package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun DateChip(
    title: String,
    selected: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val isSelected = title == selected
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(
                brush = if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            colorScheme.primary.copy(alpha = 0.20f),
                            colorScheme.secondary.copy(alpha = 0.14f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.surfaceVariant.copy(alpha = 0.52f),
                            colorScheme.surface.copy(alpha = 0.92f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) {
                    colorScheme.primary.copy(alpha = 0.55f)
                } else {
                    colorScheme.onSurface.copy(alpha = 0.65f)
                },
                shape = shape
            )
            .clickable { onClick(title) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge
        )

        icon?.let {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDateChip() {
    ExpenseTrackerTheme(darkTheme = true) {
        DateChip("Today", "Today", {})
    }
}
