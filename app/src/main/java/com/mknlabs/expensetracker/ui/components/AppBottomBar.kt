package com.mknlabs.expensetracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.ui.navigation.AppRoute
import com.mknlabs.expensetracker.ui.navigation.BottomNavBarItem
import com.mknlabs.expensetracker.ui.navigation.bottomNavBarItems
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme

/**
 * Floating capsule bottom navigation bar matching the Telegram/Xiaomi design.
 *
 * - 72.dp height capsule with RoundedCornerShape(32.dp)
 * - 12.dp horizontal margins from parent edges
 * - Pill-shaped active indicator behind the selected tab
 * - 24.dp icons with LabelSmall labels underneath
 */
@Composable
fun AppBottomBar(
    currentRoute: AppRoute?,
    onItemClick: (AppRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val capsuleShape = RoundedCornerShape(32.dp)
    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 72.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = capsuleShape,
                    ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                .clip(capsuleShape)
                .background(containerColor)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavBarItems.forEach { item ->
                FloatingCapsuleNavItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = { onItemClick(item.route) }
                )
            }
        }
    }
}

@Composable
private fun FloatingCapsuleNavItem(
    item: BottomNavBarItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val indicatorWidth = 72.dp
    val indicatorMinHeight = 56.dp
    val indicatorShape = RoundedCornerShape(20.dp)

    val animatedIndicatorOffset by animateDpAsState(
        targetValue = if (selected) 0.dp else indicatorWidth,
        animationSpec = spring(),
        label = "indicator_offset"
    )

    val iconTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "bottom_bar_icon_tint"
    )

    val labelColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "bottom_bar_label_tint"
    )

    Box(
        modifier = Modifier
            .width(indicatorWidth)
            .heightIn(min = indicatorMinHeight)
            .clip(indicatorShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Active pill indicator
        if (selected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(indicatorShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(indicatorWidth)
                .heightIn(min = indicatorMinHeight)
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.icon,
                contentDescription = stringResource(item.titleRes),
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = stringResource(item.titleRes),
                color = labelColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppBottomBarPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 24.dp)
        ) {
            AppBottomBar(
                currentRoute = AppRoute.Budget,
                onItemClick = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
