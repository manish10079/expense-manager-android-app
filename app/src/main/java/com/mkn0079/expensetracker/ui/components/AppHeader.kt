package com.mkn0079.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme


/**
 * A unified header component for all screens (except Home).
 * Supports back navigation and adaptive layout for large font scales.
 */
@Composable
fun AppHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Pulls the header up slightly to reduce the gap under the status bar / parent padding. */
    contentTopOffset: Dp = 2.dp,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
) {
    val fontScale = LocalDensity.current.fontScale
    val titleMaxLines = if (fontScale > 1.3f) 3 else 2
    val effectiveTopOffset = if (fontScale > 1.5f) 0.dp else contentTopOffset

    Row(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = -effectiveTopOffset),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(onClick = onBackClick)

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.weight(1f)
        )

        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp), // Outer padding for accessibility
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp) // The visible circle
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .clickable(onClick = onClick), // Ripple now limited to 40dp
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(name = "AppHeader - Light", showBackground = true)
@Composable
private fun AppHeaderPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp)
        ) {
            AppHeader(
                title = "Settings",
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "AppHeader - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun AppHeaderPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp)
        ) {
            AppHeader(
                title = "Settings",
                onBackClick = {}
            )
        }
    }
}

@Preview(
    name = "AppHeader - Large font",
    showBackground = true,
    fontScale = 2f
)
@Composable
private fun AppHeaderPreviewLargeFont() {
    ExpenseTrackerTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp)
        ) {
            AppHeader(
                title = "Budget & recurring transactions",
                onBackClick = {}
            )
        }
    }
}
