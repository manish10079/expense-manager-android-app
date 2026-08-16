package com.mknlabs.expensetracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ui.navigation.AppRoute
import com.mknlabs.expensetracker.ui.navigation.BottomNavBarItem
import com.mknlabs.expensetracker.ui.navigation.bottomNavBarItems
import com.mknlabs.expensetracker.ui.theme.brandGradient

/** Width of the branded rail, mirroring the 80dp spec in the roadmap. */
val AppNavigationRailWidth = 80.dp

/**
 * Branded navigation rail for Medium+ width windows. Mirrors the [AppBottomBar]
 * visual language (gradient selection, same icons/labels) laid out vertically,
 * with the Add action centered between the first and last two destinations.
 */
@Composable
fun AppNavigationRail(
    currentRoute: AppRoute?,
    onItemClick: (AppRoute) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topItems = bottomNavBarItems.take(2)
    val bottomItems = bottomNavBarItems.drop(2)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(AppNavigationRailWidth)
            .navigationBarsPadding()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        topItems.forEach { item ->
            RailNavItem(
                item = item,
                selected = currentRoute == item.route,
                onClick = { onItemClick(item.route) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Centered Add action (spacers balance the two item groups).
        Spacer(modifier = Modifier.weight(1f))
        RailAddButton(onClick = onAddClick)
        Spacer(modifier = Modifier.weight(1f))

        bottomItems.forEach { item ->
            RailNavItem(
                item = item,
                selected = currentRoute == item.route,
                onClick = { onItemClick(item.route) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RailNavItem(
    item: BottomNavBarItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "rail_icon_tint"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "rail_label_tint"
    )
    val pillHeight by animateDpAsState(
        targetValue = if (selected) 52.dp else 48.dp,
        label = "rail_pill_height"
    )
    val gradientBrush = brandGradient()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()                .width(52.dp)
                .height(pillHeight)
                .clip(RoundedCornerShape(18.dp))
                .then(
                    if (selected) Modifier.background(gradientBrush)
                    else Modifier.background(Color.Transparent)
                ),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.icon,
                contentDescription = stringResource(item.titleRes),
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = stringResource(item.titleRes).uppercase(),
            color = labelColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 8.sp,
                letterSpacing = 0.6.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun RailAddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(
                elevation = 22.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)
            )
            .clip(CircleShape)
            .background(brush = brandGradient())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.desc_add_transaction),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}
