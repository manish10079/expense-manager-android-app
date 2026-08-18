package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Savings

@Composable
fun SmallHomeCard(
    title: String,
    value: String,
    icon: ImageVector,
    badgeCount: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Dimens.CardRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = shape
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = brandGradient(),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (badgeCount != null && badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badgeCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines  = 1
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview(name = "Today's Spending — Light", showBackground = true)
@Composable
private fun SmallHomeCardTodayLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SmallHomeCard(
                title = "Today's Spending",
                value = "₹2,450",
                icon = Icons.Filled.CalendarMonth,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Today's Spending — Dark", showBackground = true)
@Composable
private fun SmallHomeCardTodayDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SmallHomeCard(
                title = "Today's Spending",
                value = "₹2,450",
                icon = Icons.Filled.CalendarMonth,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Home Row — Light", showBackground = true)
@Composable
private fun SmallHomeCardRowLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SmallHomeCard(
                    title = "Today's Spending",
                    value = "₹2,450",
                    icon = Icons.Filled.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
                SmallHomeCard(
                    title = "Savings Goals",
                    value = "₹12,000",
                    icon = Icons.Filled.Savings,
                    badgeCount = 3,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(name = "Home Row — Dark", showBackground = true)
@Composable
private fun SmallHomeCardRowDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SmallHomeCard(
                    title = "Today's Spending",
                    value = "₹2,450",
                    icon = Icons.Filled.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
                SmallHomeCard(
                    title = "Savings Goals",
                    value = "₹12,000",
                    icon = Icons.Filled.Savings,
                    badgeCount = 3,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
