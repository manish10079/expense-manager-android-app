package com.mkn0079.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkn0079.expensetracker.models.SortType
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun OrderOption(
    title: String,
    subtitle: String,
    value: SortType,
    selectedOrder: SortType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isSelected = selectedOrder == value
    val shape = RoundedCornerShape(22.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                color = if (isSelected) {
                    colorScheme.primary.copy(alpha = 0.10f)
                } else {
                    colorScheme.surfaceVariant.copy(alpha = 0.38f)
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) {
                    colorScheme.primary.copy(alpha = 0.55f)
                } else {
                    colorScheme.onSurface.copy(alpha = 0.08f)
                },
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = subtitle,
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colorScheme.primary,
                unselectedColor = colorScheme.onSurfaceVariant
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewOrderOption() {
    ExpenseTrackerTheme(darkTheme = true) {
        OrderOption(
            title = "Highest First",
            subtitle = "Larger amounts take priority in the list.",
            value = SortType.HIGHEST,
            selectedOrder = SortType.HIGHEST,
            onClick = {}
        )
    }
}
