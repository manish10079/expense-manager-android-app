package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R

/**
 * Highest count rendered in full. Anything above it shows as an overflow badge instead, so a
 * three digit number can never widen a tab pill past the label sitting next to it.
 */
internal const val MAX_TAB_BADGE_COUNT = 99

/**
 * The count a tab should display, or `null` when the badge must not exist at all.
 *
 * Three rules decide this and none of them may drift apart:
 * - a count is a Pro perk, so a free user never sees one — not even a placeholder,
 * - only the selected tab shows its count, because the other tab's number would describe
 *   content the user cannot see from here,
 * - an empty list shows nothing: a literal "0" badge is noise, not information.
 *
 * Pure on purpose — the "who sees what" decision is the part worth testing, and the Compose
 * side only has to render whatever this returns.
 */
internal fun tabBadgeCount(
    count: Int,
    isSelected: Boolean,
    isProUser: Boolean
): Int? = count.takeIf { isProUser && isSelected && it > 0 }

/**
 * True when [count] must render as an overflow badge rather than the exact value.
 */
internal fun isTabBadgeOverflow(count: Int): Boolean = count > MAX_TAB_BADGE_COUNT

/**
 * Small count pill for a tab label.
 *
 * Coloured from the theme rather than hardcoded, because it is always drawn on top of the
 * brand-gradient tab indicator and has to stay readable in light and dark mode. Sized entirely
 * from typography with a minimum width instead of a fixed height, so it follows the system font
 * scale without clipping and keeps one and three digit counts the same shape.
 */
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

    // Screen readers get the real number, never the capped label.
    val badgeDescription = pluralStringResource(R.plurals.label_tab_item_count, count, count)

    Text(
        text = visibleText,
        color = MaterialTheme.colorScheme.onPrimary,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = modifier
            .defaultMinSize(minWidth = 22.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .semantics { contentDescription = badgeDescription }
    )
}
