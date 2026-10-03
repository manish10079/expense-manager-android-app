package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.core.ui.theme.tabSwitcherSelectedFill
import com.mknlabs.expensetracker.core.ui.theme.tabSwitcherSelectedInk
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.models.TabItem
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient

/** Wash strength for a tab's own [TabItem.selectedColor]; the brand pill uses the same 20%. */
private const val SelectedTintAlpha = 0.20f

@Composable
fun <T> AnimatedTabSwitcher(
    items: List<TabItem<T>>,
    selectedItemId: T?,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    /**
     * Optional per-call override for the tab's own vertical padding, so a caller that
     * needs a shorter bar (the Add Transaction screen) can tighten it without changing
     * the height every other screen relies on. Null keeps the tier default below.
     */
    verticalPaddingOverride: Dp? = null
) {
    if (items.isEmpty()) return

    val density = LocalDensity.current
    var containerWidthPx by remember { mutableStateOf(0) }
    
    val containerRadius = if (compact) 12.dp else Dimens.CardRadius
    val containerPadding = if (compact) 2.dp else 4.dp
    val pillRadius = if (compact) 10.dp else 20.dp
    val innerRadius = if (compact) 10.dp else 18.dp
    // ~5% slimmer than the previous 12dp variant, applied on every screen.
    val verticalPadding = verticalPaddingOverride ?: if (compact) 6.dp else 11.dp
    val fontSize = if (compact) 10.sp else 15.sp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .onSizeChanged { containerWidthPx = it.width }
            .clip(RoundedCornerShape(containerRadius))
            .background(standardCardGradient())
            .padding(containerPadding)
    ) {
        val tabWidth = with(density) { (containerWidthPx.toDp() - (containerPadding * 2)) / items.size }
        val selectedIndex = items.indexOfFirst { it.id == selectedItemId }.coerceAtLeast(0)

        // A tab may carry its own semantic colour (the Add/Edit screen's income/expense pair).
        // When it does, the pill takes a wash of it and its label takes the colour itself;
        // without one every other screen keeps the brand tokens it has always used.
        val selectedItem = items.firstOrNull { it.id == selectedItemId }
        val selectedWash by animateColorAsState(
            targetValue = selectedItem?.selectedColor?.copy(alpha = SelectedTintAlpha)
                ?: MaterialTheme.colorScheme.tabSwitcherSelectedFill,
            label = "tab_indicator_wash"
        )
        val selectedInk = selectedItem?.selectedColor
            ?: MaterialTheme.colorScheme.tabSwitcherSelectedInk

        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "tab_indicator_offset"
        )

        // Sliding indicator (Pill)
        if (containerWidthPx > 0) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(pillRadius))
                    .background(selectedWash)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items.forEach { item ->
                val selected = item.id == selectedItemId
                
                val animatedColor by animateColorAsState(
                    targetValue = when {
                        selected -> selectedInk
                        item.isLocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    label = "tab_text_color_${item.id}"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(innerRadius))
                        .clickable {
                            if (item.isLocked) {
                                item.onLockedClick()
                            } else {
                                onItemSelected(item.id)
                            }
                        }
                        .padding(vertical = verticalPadding),
                    contentAlignment = Alignment.Center
                ) {
                    // Wrap-content so the badge pins to the label, not the tab's far
                    // edge. The outer Box keeps that cluster in the horizontal centre.
                    Box {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.label,
                                color = animatedColor,
                                fontSize = fontSize,
                                fontWeight = FontWeight.Bold,
                                maxLines = maxLinesForTier(compact = 1, large = 2, huge = 2),
                                textAlign = TextAlign.Center
                            )
                            if (item.isLocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = stringResource(R.string.content_desc_locked_formatted, item.label),
                                    tint = MaterialTheme.colorScheme.featureGateLock,
                                    modifier = Modifier.size(if (compact) 10.dp else 12.dp)
                                )
                            }
                        }
                        val badgeCount = item.badgeCount
                        if (badgeCount != null && badgeCount > 0) {
                            TabCountBadge(
                                count = badgeCount,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 12.dp, y = (-4).dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
