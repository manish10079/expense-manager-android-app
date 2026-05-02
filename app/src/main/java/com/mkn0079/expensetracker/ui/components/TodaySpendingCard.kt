package com.mkn0079.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.brandGradient
import com.mkn0079.expensetracker.ui.theme.standardCardGradient
import androidx.compose.ui.graphics.Color

@Composable
fun TodaySpendingCard(
    amount: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Dimens.CardRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = colorScheme.outlineVariant.copy(alpha =  0.65f),
                shape = shape
            )
            .padding(horizontal = Dimens.PaddingMedium, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = brandGradient(),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Today's spending",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.PaddingMedium))

                Column {
                    Text(
                        text = "Today's Spending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = amount,
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Open calendar",
                    tint = colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodaySpendingCardPreview() {
    ExpenseTrackerTheme {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            TodaySpendingCard(amount = "\u20B92,450")
        }
    }
}
