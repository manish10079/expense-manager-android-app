package com.mknlabs.expensetracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.mknlabs.expensetracker.ui.adaptive.FontScaleTier
import com.mknlabs.expensetracker.ui.adaptive.LocalFontScaleInfo

/**
 * Tier-aware line limits for user-facing copy (titles, labels, notes, names):
 * the number of allowed lines grows as the system font scale rises so text
 * wraps instead of being truncated. Data fields (amounts, dates, balances)
 * should stay 1-line ellipsis regardless of tier.
 *
 * Defaults follow the AppHeader idiom (2 → 3 → 4 lines).
 *
 * @param compact lines at Default scale
 * @param large lines at Large scale (defaults to compact + 1)
 * @param huge lines at Huge scale (defaults to large + 1)
 */
@Composable
fun maxLinesForTier(
    compact: Int,
    large: Int = compact + 1,
    huge: Int = large + 1
): Int = when (LocalFontScaleInfo.current.tier) {
    FontScaleTier.Default -> compact
    FontScaleTier.Large -> large
    FontScaleTier.Huge -> huge
}

/**
 * 1-line data text (amounts, dates, balances) — never grows with the tier.
 * Use for pure data; use [maxLinesForTier] for user-facing copy.
 */
@Composable
fun dataMaxLines(): Int = 1

/**
 * The ellipsis overflow to use with [maxLinesForTier]; data text keeps ellipsis,
 * user-facing copy can pass [TextOverflow.Ellipsis] too (maxLines still caps it).
 */
val userCopyOverflow: TextOverflow = TextOverflow.Ellipsis
