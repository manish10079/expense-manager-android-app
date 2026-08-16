package com.mknlabs.expensetracker.ui.adaptive

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

/**
 * Width size classes — Google's five classes:
 * Compact (<600dp), Medium (600–840dp), Expanded (840–1200dp),
 * Large (1200–1600dp), ExtraLarge (≥1600dp).
 */
enum class AppWindowSize { Compact, Medium, Expanded, Large, ExtraLarge }

/** Height size classes: Compact (<480dp), Medium (480–900dp), Expanded (≥900dp). */
enum class AppWindowHeight { Compact, Medium, Expanded }

/**
 * Stable snapshot of the current window's size classes. Screens branch on
 * [width]/[height] directly — never on device-type heuristics.
 */
data class AppWindowInfo(
    val width: AppWindowSize,
    val height: AppWindowHeight,
) {
    /** True when there is room for two-pane / side-by-side layouts. */
    val isWide: Boolean get() = width != AppWindowSize.Compact

    /**
     * True for the classic phone portrait footprint: compact WIDTH with a
     * non-compact height. (Compact height (<480dp) means phone landscape or a
     * very short window — never portrait, where heights are Medium/Expanded.)
     */
    val isCompactPortrait: Boolean
        get() = width == AppWindowSize.Compact && height != AppWindowHeight.Compact

    /** True when vertical space is tight (e.g. phone landscape). */
    val isHeightCompact: Boolean get() = height == AppWindowHeight.Compact

    /** True on the widest desktop-class windows. */
    val isVeryWide: Boolean
        get() = width == AppWindowSize.Large || width == AppWindowSize.ExtraLarge
}

/**
 * Reads the window size classes via [currentWindowAdaptiveInfo] with
 * `supportLargeAndXLargeWidth = true` so Large/ExtraLarge classes are reported
 * (needed for ChromeOS/desktop). Recomputes only when the size class changes.
 *
 * Uses [WindowSizeClass.isWidthAtLeastBreakpoint] / [isHeightAtLeastBreakpoint]
 * (the non-deprecated API) to map to the five width + three height classes.
 */
@Composable
fun rememberAppWindowInfo(): AppWindowInfo {
    val windowSizeClass = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass
    val width = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND) ->
            AppWindowSize.ExtraLarge
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND) ->
            AppWindowSize.Large
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
            AppWindowSize.Expanded
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
            AppWindowSize.Medium
        else -> AppWindowSize.Compact
    }
    val height = when {
        windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND) ->
            AppWindowHeight.Expanded
        windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) ->
            AppWindowHeight.Medium
        else -> AppWindowHeight.Compact
    }
    return AppWindowInfo(width = width, height = height)
}

/**
 * Provided once at the app root (above [com.mknlabs.expensetracker.MainScreen])
 * so every screen reads the same stable value and recomposes only on actual
 * size-class changes.
 */
val LocalAppWindowInfo = staticCompositionLocalOf {
    AppWindowInfo(width = AppWindowSize.Compact, height = AppWindowHeight.Compact)
}

/**
 * Size-class-aware horizontal padding: compact screens keep phone padding;
 * wider windows get progressively more breathing room so lists never stretch
 * edge-to-edge on tablets/desktops.
 */
@Composable
fun Modifier.windowWidthPadding(extraPadding: Dp = 0.dp): Modifier {
    val info = LocalAppWindowInfo.current
    val base = when (info.width) {
        AppWindowSize.Compact -> 20.dp
        AppWindowSize.Medium -> 24.dp
        AppWindowSize.Expanded -> 32.dp
        AppWindowSize.Large -> 48.dp
        AppWindowSize.ExtraLarge -> 64.dp
    }
    return padding(horizontal = base + extraPadding)
}

/**
 * Convenience: horizontal padding values for a given window info, useful when
 * the padding must be applied to a LazyColumn's contentPadding.
 */
@Composable
fun windowHorizontalPadding(info: AppWindowInfo): PaddingValues {
    val base = when (info.width) {
        AppWindowSize.Compact -> 20.dp
        AppWindowSize.Medium -> 24.dp
        AppWindowSize.Expanded -> 32.dp
        AppWindowSize.Large -> 48.dp
        AppWindowSize.ExtraLarge -> 64.dp
    }
    return PaddingValues(start = base, end = base)
}

/** Reusable two-pane math: the width fraction a pane should occupy on wide windows. */
fun AppWindowInfo.paneWeight(availableWidth: Dp): Float = when {
    availableWidth < 900.dp -> 1f
    width == AppWindowSize.ExtraLarge -> 0.5f
    else -> 0.62f
}
