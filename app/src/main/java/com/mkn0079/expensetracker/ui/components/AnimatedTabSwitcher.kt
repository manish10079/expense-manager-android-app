package com.mkn0079.expensetracker.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.ui.models.TabItem
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.theme.featureGateLock
import com.mkn0079.expensetracker.ui.theme.standardCardGradient

@Composable
fun <T> AnimatedTabSwitcher(
    items: List<TabItem<T>>,
    selectedItemId: T?,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    if (items.isEmpty()) return

    val density = LocalDensity.current
    var containerWidthPx by remember { mutableStateOf(0) }
    
    val containerRadius = if (compact) 12.dp else Dimens.CardRadius
    val containerPadding = if (compact) 2.dp else 4.dp
    val pillRadius = if (compact) 10.dp else 20.dp
    val innerRadius = if (compact) 10.dp else 18.dp
    val verticalPadding = if (compact) 6.dp else 12.dp
    val fontSize = if (compact) 12.sp else 15.sp

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
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
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
                        selected -> MaterialTheme.colorScheme.onPrimary
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.label,
                            color = animatedColor,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold
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
                }
            }
        }
    }
}
