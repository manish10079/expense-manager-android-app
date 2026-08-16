package com.mknlabs.expensetracker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.ui.adaptive.AppWindowSize
import com.mknlabs.expensetracker.ui.adaptive.LocalAppWindowInfo

/**
 * Legacy static spacing tokens. These remain as the Compact (phone portrait)
 * values so every existing call site compiles unchanged; size-class-aware code
 * should prefer [currentSpacing] / [CompactSpacing] / [MediumSpacing] /
 * [ExpandedSpacing].
 */
object Dimens {

    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val ScreenPadding = 20.dp
    val HeaderSpacing = 10.dp
    val PaddingLarge = 24.dp
    val PaddingXL = 32.dp

    val CardRadius = 24.dp

}

/** Size-class-aware spacing values. */
data class AppSpacing(
    val screenPadding: Dp,
    val headerSpacing: Dp,
    val paddingSmall: Dp,
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
    paddingSmall = 8.dp,
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
