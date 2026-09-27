package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.cta
import com.mknlabs.expensetracker.core.ui.theme.onCta

internal const val MAX_TAB_BADGE_COUNT = 99

/**
 * Pro-only count on the selected tab. Free users never see a number; an empty
 * list shows nothing so a literal 0 is not drawn as a badge.
 */
fun tabBadgeCount(
    count: Int,
    isSelected: Boolean,
    isProUser: Boolean
): Int? = count.takeIf { isProUser && isSelected && it > 0 }

internal fun isTabBadgeOverflow(count: Int): Boolean = count > MAX_TAB_BADGE_COUNT

@Composable
fun TabCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    val visibleText = if (isTabBadgeOverflow(count)) {
        stringResource(R.string.label_tab_count_overflow, MAX_TAB_BADGE_COUNT)
    } else {
        stringResource(R.string.label_tab_count_short, count)
    }
    val badgeDescription = pluralStringResource(R.plurals.label_tab_item_count, count, count)

    Box(
        modifier = modifier
            .semantics { contentDescription = badgeDescription }
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.cta)
            .padding(horizontal = 4.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = visibleText,
            color = MaterialTheme.colorScheme.onCta,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center
        )
    }
}
