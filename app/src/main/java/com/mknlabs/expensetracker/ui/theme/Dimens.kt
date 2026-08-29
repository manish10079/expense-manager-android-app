package com.mknlabs.expensetracker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.ui.adaptive.AppWindowSize
import com.mknlabs.expensetracker.ui.adaptive.LocalAppWindowInfo

/**
 * Google 2026 spacing tokens.
 *
 * Naming follows the Google Material 3 spacing scale:
 * - [spacingTiny]    = 4dp
 * - [spacingSmall]   = 8dp
 * - [spacingCompact] = 12dp
 * - [spacingDefault] = 16dp
 * - [spacingMedium]  = 20dp
 * - [spacingLarge]   = 24dp
 * - [spacingXL]      = 32dp
 *
 * Legacy aliases ([PaddingSmall], [PaddingMedium], etc.) are kept for
 * backward compatibility with existing call sites. Size-class-aware code
 * should prefer [currentSpacing] / [CompactSpacing] / [MediumSpacing] /
 * [ExpandedSpacing].
 */
object Dimens {

    // ── Google 2026 Spacing Scale ──────────────────────────────────────
    val spacingTiny = 4.dp
    val spacingSmall = 8.dp
    val spacingCompact = 12.dp
    val spacingDefault = 16.dp
    val spacingMedium = 20.dp
    val spacingLarge = 24.dp
    val spacingXL = 32.dp

    // ── Layout ─────────────────────────────────────────────────────────
    val CardRadius = 24.dp

    // ── Legacy aliases (kept for backward compatibility) ────────────────
    val PaddingTiny = spacingTiny
    val PaddingSmall = spacingSmall
    val PaddingCompact = spacingCompact
    val PaddingMedium = spacingDefault
    val ScreenPadding = spacingMedium
    val HeaderSpacing = 10.dp  // Migrated from legacy; prefer spacingCompact
    val PaddingLarge = spacingLarge
    val PaddingXL = spacingXL

}

/** Size-class-aware spacing values. */
data class AppSpacing(
    val screenPadding: Dp,
    val headerSpacing: Dp,
    val paddingTiny: Dp,
    val paddingSmall: Dp,
    val paddingCompact: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp,
    val paddingXL: Dp,
    val cardRadius: Dp,
    /** Min height for text-bearing stat/chart cards (flexes, never clips). */
    val statsCardHeight: Dp,
    /** Min height for analytics chart rows. */
    val chartRowHeight: Dp,
    /** Max width for bottom sheets / modals on wide screens. */
    val sheetMaxWidth: Dp,
    /** Max list height for bottom sheets (fraction of window height on wide). */
    val sheetMaxListHeight: Dp,
    /** Min height for empty-state containers. */
    val emptyStateHeight: Dp,
    /** Bottom content padding when a floating bottom bar is present. */
    val bottomBarContentPadding: Dp,
    /** Bottom content padding when no floating bar is present. */
    val bottomContentPadding: Dp,
)

/** Compact (phone portrait) values — identical to the historical hardcoded ones. */
val CompactSpacing = AppSpacing(
    screenPadding = 20.dp,
    headerSpacing = 10.dp,
    paddingTiny = 4.dp,
    paddingSmall = 8.dp,
    paddingCompact = 12.dp,
    paddingMedium = 16.dp,
    paddingLarge = 24.dp,
    paddingXL = 32.dp,
    cardRadius = 24.dp,
    statsCardHeight = 190.dp,
    chartRowHeight = 170.dp,
    sheetMaxWidth = 560.dp,
    sheetMaxListHeight = 440.dp,
    emptyStateHeight = 400.dp,
    bottomBarContentPadding = 126.dp,
    bottomContentPadding = 24.dp,
)

/** Medium (600–840dp) values — slightly more generous, still phone-like. */
val MediumSpacing = CompactSpacing.copy(
    screenPadding = 24.dp,
    paddingLarge = 28.dp,
    paddingXL = 40.dp,
    sheetMaxWidth = 600.dp,
    sheetMaxListHeight = 480.dp,
    bottomBarContentPadding = 132.dp,
)

/** Expanded and wider — roomier padding, larger cards, wider sheets. */
val ExpandedSpacing = CompactSpacing.copy(
    screenPadding = 32.dp,
    headerSpacing = 12.dp,
    paddingLarge = 32.dp,
    paddingXL = 48.dp,
    cardRadius = 28.dp,
    statsCardHeight = 200.dp,
    chartRowHeight = 180.dp,
    sheetMaxWidth = 720.dp,
    sheetMaxListHeight = 520.dp,
    emptyStateHeight = 480.dp,
    bottomBarContentPadding = 140.dp,
)

/** Picks the spacing set for the current window width size class. */
@Composable
fun currentSpacing(): AppSpacing = when (LocalAppWindowInfo.current.width) {
    AppWindowSize.Compact -> CompactSpacing
    AppWindowSize.Medium -> MediumSpacing
    AppWindowSize.Expanded,
    AppWindowSize.Large,
    AppWindowSize.ExtraLarge,
    -> ExpandedSpacing
}
