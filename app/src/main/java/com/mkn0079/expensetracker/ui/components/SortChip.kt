package com.mkn0079.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun SortChip(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(22.dp)
    val borderColor = if (selected) {
        colorScheme.primary.copy(alpha = 0.55f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.08f)
    }

    val backgroundBrush = if (selected) {
        Brush.linearGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.24f),
                colorScheme.secondary.copy(alpha = 0.16f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                colorScheme.surfaceVariant.copy(alpha = 0.60f),
                colorScheme.surface.copy(alpha = 0.92f)
            )
        )
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge
        )
        if (selected) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Active",
                color = colorScheme.primary,
                style = MaterialTheme.typography.labelSmall
            )
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SortChipPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        SortChip(
            modifier = Modifier.fillMaxWidth(),
            title = "Date",
            icon = Icons.Default.DateRange,
            selected = true,
            onClick = {}
        )
    }
}
