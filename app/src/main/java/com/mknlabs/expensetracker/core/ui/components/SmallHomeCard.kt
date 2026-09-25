package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.PiggyBank
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
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.SmallCardDarkStart
import com.mknlabs.expensetracker.core.ui.theme.SmallCardDarkEnd
import com.mknlabs.expensetracker.core.ui.theme.SmallCardBorderDark
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.SmallCardIconBgDark
import com.mknlabs.expensetracker.core.ui.theme.SmallCardIconBgLight
import com.mknlabs.expensetracker.core.ui.theme.SmallCardIconDark
import com.mknlabs.expensetracker.core.ui.theme.SmallCardIconLight
import com.mknlabs.expensetracker.core.ui.theme.SmallCardLabelDark
import com.mknlabs.expensetracker.core.ui.theme.SmallCardLabelLight


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
    // Dark keeps the 18dp quick-action shape and the brand-tinted edge it was drawn
    // with; light is the standard card, 24dp with the palette's hairline. See AppCard.
    val shape = if (isDark) RoundedCornerShape(18.dp) else AppCardDefaults.shape()

    // The brand-tinted gradient is the dark surface matching indexmockup.html --qcard-bg.
    // Light mode does not raise a tinted surface at all, so the gradient is not painted
    // there and the card falls back to its white container.
    val gradientBrush = Brush.linearGradient(listOf(SmallCardDarkStart, SmallCardDarkEnd))

    // Icon background & tint matching indexmockup.html --qicon-bg & --qicon-c
    val iconBgColor = if (isDark) SmallCardIconBgDark else SmallCardIconBgLight
    val iconTintColor = if (isDark) SmallCardIconDark else SmallCardIconLight

    // Label color matching indexmockup.html --t-secondary (#A5A1B8 in dark mode)
    val labelColor = if (isDark) SmallCardLabelDark else SmallCardLabelLight

    AppCard(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        // The dark gradient paints its own edge, so the card contributes the outline it
        // has always had and nothing else; light takes the standard card colours.
        colors = if (isDark) {
            AppCardColors(
                containerColor = Color.Transparent,
                contentColor = colorScheme.onSurface,
                border = BorderStroke(1.dp, SmallCardBorderDark)
            )
        } else {
            AppCardDefaults.colors()
        },
        brush = darkOnlyGradient(gradientBrush),
        contentPadding = PaddingValues(12.dp)
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
                            .background(iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTintColor,
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
                        color = labelColor,
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
                imageVector = PhosphorIcons.Regular.CaretRight,
                contentDescription = null,
                tint = if (isDark) Color.White.copy(alpha = 0.3f) else colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
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
                icon = PhosphorIcons.Regular.CalendarBlank,
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
                icon = PhosphorIcons.Regular.CalendarBlank,
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
                    icon = PhosphorIcons.Regular.CalendarBlank,
                    modifier = Modifier.weight(1f)
                )
                SmallHomeCard(
                    title = "Savings Goals",
                    value = "₹12,000",
                    icon = PhosphorIcons.Regular.PiggyBank,
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
                    icon = PhosphorIcons.Regular.CalendarBlank,
                    modifier = Modifier.weight(1f)
                )
                SmallHomeCard(
                    title = "Savings Goals",
                    value = "₹12,000",
                    icon = PhosphorIcons.Regular.PiggyBank,
                    badgeCount = 3,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
