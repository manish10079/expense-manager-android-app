package com.mkn0079.expensetracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.ui.navigation.AppRoute
import com.mkn0079.expensetracker.ui.navigation.BottomNavBarItem
import com.mkn0079.expensetracker.ui.navigation.bottomNavBarItems
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import androidx.compose.ui.res.stringResource
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.ui.theme.brandGradient

private val BottomBarContainerHeight = 72.dp
private val BottomBarCenterActionSize = 40.dp
private val BottomBarCenterSpacerWidth = 44.dp
private val BottomBarBottomPadding = 0.dp

@Composable
fun AppBottomBar(
    currentRoute: AppRoute?,
    onItemClick: (AppRoute) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shellShape = RoundedCornerShape(34.dp)
    val leftItems = bottomNavBarItems.take(2)
    val rightItems = bottomNavBarItems.drop(2)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = BottomBarBottomPadding),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BottomBarContainerHeight)
                    .shadow(
                        elevation = 32.dp,
                        shape = shellShape,
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    )
                    .clip(shellShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        shape = shellShape
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItemGroup(
                    items = leftItems,
                    currentRoute = currentRoute,
                    onItemClick = onItemClick,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(BottomBarCenterSpacerWidth))

                NavItemGroup(
                    items = rightItems,
                    currentRoute = currentRoute,
                    onItemClick = onItemClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(BottomBarCenterActionSize)
                    .shadow(
                        elevation = 22.dp,
                        shape = CircleShape,
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                        spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)
                    )
                    .clip(CircleShape)
                    .background(brush = brandGradient())
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.desc_add_transaction),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun NavItemGroup(
    items: List<BottomNavBarItem>,
    currentRoute: AppRoute?,
    onItemClick: (AppRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            VaultNavItem(
                item = item,
                selected = currentRoute == item.route,
                onClick = { onItemClick(item.route) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VaultNavItem(
    item: BottomNavBarItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "bottom_bar_icon_tint"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
        label = "bottom_bar_label_tint"
    )
    val iconContainerSize by animateDpAsState(
        targetValue = if (selected) 40.dp else 34.dp,
        label = "bottom_bar_icon_size"
    )

    val gradientBrush = brandGradient()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(iconContainerSize)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = iconTint,
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            if (selected) {
                                drawRect(gradientBrush, blendMode = BlendMode.SrcAtop)
                            }
                        }
                    }
            )
        }

        Text(
            text = if (selected) item.title.uppercase() else " ",
            color = if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0f) else labelColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 8.sp,
                letterSpacing = 0.7.sp,
                brush = if (selected) gradientBrush else null
            )
        )
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
                onAddClick = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
