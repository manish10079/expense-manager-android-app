package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.PiggyBank
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import com.mknlabs.expensetracker.core.ui.theme.cta
import com.mknlabs.expensetracker.core.ui.theme.onCta

/**
 * One quick-action card on the home row.
 *
 * A neutral card like every other surface in both themes. It used to raise a violet-tinted
 * gradient of its own (indexmockup.html's `--qcard-bg`) in dark, which made two of the
 * smallest objects on the first screen the only brand-filled surfaces on it — the exact
 * over-purple case the redesign removes. The card now reads the shared card ladder, and
 * the brand lives where the spec puts it on a neutral surface: `--accentSoft` behind the
 * glyph and `--accent` on it.
 */
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

    AppCard(
        onClick = onClick,
        modifier = modifier,
        shape = AppCardDefaults.shape(),
        colors = AppCardDefaults.colors(),
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
                            .background(colorScheme.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colorScheme.accentInk,
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
                                .background(colorScheme.cta)
                                .border(1.5.dp, colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            LabelText(
                                text = badgeCount.toString(),
                                color = colorScheme.onCta
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    LabelText(
                        text = title,
                        color = colorScheme.onSurfaceVariant,
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
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
