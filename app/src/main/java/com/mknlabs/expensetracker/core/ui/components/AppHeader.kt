package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowBendUpLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.appHeaderTitle


/**
 * A unified header component for all screens (except Home).
 * Supports back navigation and adaptive layout for large font scales.
 */
@Composable
fun AppHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
) {
    // Shared tier logic (see rememberFontScaleInfo / maxLinesForTier) — never
    // multiply sizes by the raw fontScale (non-linear on Android 14+).
    val titleMaxLines = maxLinesForTier(compact = 2, large = 3, huge = 3)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onClick = onBackClick)
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                // The page's own ink in light; in dark the dedicated header-title token
                // (the light field, used as ink on the near-black page).
                color = MaterialTheme.colorScheme.appHeaderTitle.copy(alpha = 0.75f),
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
        Spacer(modifier = Modifier.height(Dimens.HeaderContentGap))
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    // Visual size matches the title line so the glyph sits on the same
    // horizontal axis; the 48.dp box was dropping the title under the arrow.
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.ArrowBendUpLeft,
            contentDescription = stringResource(R.string.desc_back),
            tint = MaterialTheme.colorScheme.appHeaderTitle.copy(alpha = 0.75f),
            modifier = Modifier.size(24.dp)
        )
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
