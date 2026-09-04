package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.isDark
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkStart
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkCenter
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkEnd
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightStart
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightCenter
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightEnd


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
    val isDark = colorScheme.isDark
    val shape = RoundedCornerShape(Dimens.CardRadius)

    // Theme-aware gradient background (same as StatsCard)
    val gradientBrush = if (isDark) {
        Brush.linearGradient(listOf(PremiumCardDarkStart, PremiumCardDarkCenter, PremiumCardDarkEnd))
    } else {
        Brush.linearGradient(listOf(PremiumCardLightStart, PremiumCardLightCenter, PremiumCardLightEnd))
    }

    // Purple border glow matching the hero card
    val primaryColor = colorScheme.primary
    val borderBrush = remember(primaryColor) {
        Brush.linearGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.06f)
            )
        )
    }

    // Use theme primary for brand consistency with StatsCard
    val iconBgColor = colorScheme.primary

    Box(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .background(brush = gradientBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
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
                            .clip(CircleShape)
                            .background(iconBgColor.copy(alpha = 0.15f))
                            .border(
                                width = 1.dp,
                                color = iconBgColor.copy(alpha = 0.25f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconBgColor,
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
                                .background(colorScheme.primary)
                                .border(1.5.dp, colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            LabelText(
                                text = badgeCount.toString(),
                                color = colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    LabelText(
                        text = title,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    AppText(
                        text = value,
                        style = MaterialTheme.typography.titleSmall,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
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
