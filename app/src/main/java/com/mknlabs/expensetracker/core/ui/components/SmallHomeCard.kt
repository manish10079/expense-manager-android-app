package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import com.mknlabs.expensetracker.core.ui.theme.isDark

// Quick-card inks from the redesign mock: slate-400 labels in both themes, white and
// slate-900 amounts, and slate-500 / slate-400 chevrons.
private val QuickCardLabelInk = Color(0xFF94A3B8)
private val QuickCardAmountInkDark = Color(0xFFFFFFFF)
private val QuickCardAmountInkLight = Color(0xFF0F172A)
private val QuickCardCaretInkDark = Color(0xFF64748B)
private val QuickCardCaretInkLight = Color(0xFF94A3B8)

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
    onClick: () -> Unit = {},
    // Optional tile styling so a caller can match a screen's own mock; the defaults keep
    // the shared brand-tile look for every other card.
    iconTileColor: Color? = null,
    iconTileBorderColor: Color? = null,
    iconTileShape: Shape = CircleShape,
    iconTint: Color? = null,
    // Optional card chrome so a caller can match a screen's own mock.
    containerColor: Color? = null,
    borderColor: Color? = null,
    badgeColor: Color? = null,
    badgeContentColor: Color? = null,
    badgeRingColor: Color? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    AppCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(AppCardDefaults.CornerRadius),
        colors = AppCardDefaults.colors().let { defaults ->
            if (containerColor == null && borderColor == null) {
                defaults
            } else {
                AppCardColors(
                    containerColor = containerColor ?: defaults.containerColor,
                    contentColor = defaults.contentColor,
                    border = borderColor?.let { BorderStroke(1.dp, it) } ?: defaults.border
                )
            }
        },
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
                            .clip(iconTileShape)
                            .background(iconTileColor ?: colorScheme.accentSoft)
                            .then(
                                if (iconTileBorderColor != null) {
                                    Modifier.border(1.dp, iconTileBorderColor, iconTileShape)
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconRamp = brandGradient()
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint ?: Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                .drawWithCache {
                                    onDrawWithContent {
                                        drawContent()
                                        if (iconTint == null) {
                                            drawRect(brush = iconRamp, blendMode = BlendMode.SrcIn)
                                        }
                                    }
                                }
                        )
                    }

                    if (badgeCount != null && badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(badgeColor ?: colorScheme.accentInk)
                                .border(1.5.dp, badgeRingColor ?: colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            LabelText(
                                text = badgeCount.toString(),
                                color = badgeContentColor ?: if (colorScheme.isDark) {
                                    colorScheme.background
                                } else {
                                    colorScheme.onPrimary
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    LabelText(
                        text = title,
                        color = QuickCardLabelInk,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    AppText(
                        text = value,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (colorScheme.isDark) {
                            QuickCardAmountInkDark
                        } else {
                            QuickCardAmountInkLight
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = PhosphorIcons.Regular.CaretRight,
                contentDescription = null,
                tint = if (colorScheme.isDark) {
                    QuickCardCaretInkDark
                } else {
                    QuickCardCaretInkLight
                },
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
