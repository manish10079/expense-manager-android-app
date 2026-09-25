package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.adaptive.FontScaleTier
import com.mknlabs.expensetracker.core.ui.adaptive.LocalFontScaleInfo
import com.mknlabs.expensetracker.core.ui.navigation.AppRoute
import com.mknlabs.expensetracker.core.ui.navigation.BottomNavBarItem
import com.mknlabs.expensetracker.core.ui.navigation.bottomNavBarItems
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.NavEdgeLight
import com.mknlabs.expensetracker.core.ui.theme.NavOffDark
import com.mknlabs.expensetracker.core.ui.theme.NavOffLight
import com.mknlabs.expensetracker.core.ui.theme.NavOnDark
import com.mknlabs.expensetracker.core.ui.theme.NavOnLight
import com.mknlabs.expensetracker.core.ui.theme.fabGradient
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

/**
 * Gap reserved between the two destination groups for the docked Add FAB.
 *
 * Deliberately 8dp NARROWER than the FAB ([AddTransactionFabSize]), i.e. 4dp per
 * side, so Analytics and Budget sit closer to Add. The slot previously measured
 * exactly one FAB diameter, which left the two inner pills tangent to the FAB's
 * *bounding box* — but the FAB is a circle whose widest point rests on the
 * capsule's top edge, so the space beside its lower half was already empty. That
 * is the space reclaimed here, which is why the pills can move in without a
 * collision.
 *
 * Measured at the default font scale on a 411dp window, the FAB protrudes over
 * bare capsule background rather than over any pill content:
 * - the selected indicator (a 20dp-rounded rect) keeps ~5dp of clearance from the
 *   circle — its rounded top corner curves away from the circle's widest part;
 * - the icon keeps ~34dp, and the labels start below the circle's vertical extent
 *   entirely (the circle reaches 28dp down from the capsule's top edge, the labels
 *   begin at 48dp), so no glyph is affected at any string length;
 * - only the pills' raw tap *boxes* intrude (~2.8dp into the circle's bounding
 *   box) — the transparent, corner-clipped region the FAB is drawn over and owns
 *   for touch anyway.
 *
 * That margin is the budget, so it stays bounded below by the same geometry: at a
 * 40dp slot (16dp off the FAB) the indicator gap is down to ~1dp and the tap-box
 * intrusion triples to ~6.8dp, which reads as a collision. Narrowing further would
 * also steal room the labels need at raised font scales.
 */
private val AddFabSlotWidth = AddTransactionFabSize - 8.dp

/** Capsule width cap so the bar stays a capsule rather than stretching edge to
 *  edge if it is ever rendered on a wide window (the rail covers those today). */
private val CapsuleMaxWidth = 600.dp

/**
 * Fraction of the available width the capsule occupies — a 10% inset from the
 * edge-to-edge maximum. Expressed as a fraction rather than a fixed dp trim so
 * the reduction holds proportionally at every window width instead of only on
 * the screen size it was tuned against.
 */
private const val CapsuleWidthFraction = 0.90f

/** Minimum pill height — a touch target, intentionally font-scale independent. */
private val NavItemMinHeight = 56.dp

/**
 * Diameter of the circular reveal handle. Held at the 48dp minimum touch target
 * because it is the only way back to the bar once it has hidden itself, and kept
 * below [AddTransactionFabSize] so it reads as a compact affordance rather than
 * competing with the Add button that occupied the same centre line.
 */
private val RevealHandleSize = 48.dp

/**
 * How long the bar stays open after any show — an arrival, a nav-bar tap or a
 * reveal from the handle. A single window covers every trigger, so the countdown
 * always restarts at a full 5s from the most recent interaction.
 */
private const val IDLE_HIDE_MILLIS = 5_000L

/** Slide/fade duration for the bar and its handle. */
private const val BarSlideMillis = 320

/**
 * Minimum capsule height per font-scale tier.
 *
 * The capsule is a text-bearing container, so it uses `heightIn(min = …)` and
 * lets the label grow it — it is never given a fixed height and never shrinks
 * to fit. Branching on the coarse [FontScaleTier] (not the raw scale) keeps the
 * layout correct under Android 14+ non-linear font scaling.
 */
private fun capsuleMinHeight(tier: FontScaleTier): Dp = when (tier) {
    FontScaleTier.Default -> 72.dp
    FontScaleTier.Large -> 78.dp
    FontScaleTier.Huge -> 88.dp
}

/**
 * Floating capsule bottom navigation bar with the Add-transaction FAB docked
 * into its centre — one composable owning both, so the destinations and the FAB
 * can never drift apart or disagree about the bar's height.
 *
 * Auto-hide lifecycle — one window for every trigger:
 * - Arriving at a destination shows the bar for [IDLE_HIDE_MILLIS]; it then
 *   slides down out of view, leaving the reveal handle in its place.
 * - Tapping that handle slides the bar back up and restarts the window.
 * - Tapping any destination, or the docked FAB, restarts it too — including the
 *   destination already selected, which changes no route and would otherwise
 *   leave the previous countdown running.
 *
 * This composable owns only the show/hide state and its timers; the visuals live
 * in [AppBottomBarContent] so they stay previewable without a ViewModel.
 *
 * @param currentRoute Drives the selected pill and the arrival peek; `null`
 *   selects nothing.
 * @param onItemClick Fired with the tapped destination route.
 * @param onAddClick Fired by the docked FAB (navigates to Add Transaction).
 * @param modifier Applied to the outer container — callers align it to
 *   `Alignment.BottomCenter`. Horizontal/bottom insets are handled internally.
 */
@Composable
fun AppBottomBar(
    currentRoute: AppRoute?,
    onItemClick: (AppRoute) -> Unit,
    onAddClick: () -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    var barVisible by remember { mutableStateOf(true) }

    // Every event that should (re)start the hide countdown bumps this counter: an
    // arrival, a reveal from the handle, or a tap on any nav-bar control. Keying
    // the timer on a counter instead of the route is what makes re-tapping the
    // already-selected destination restart the window — the route does not change,
    // so a route-keyed effect would leave the earlier countdown running.
    var showRequests by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentRoute) {
        showRequests++
    }

    LaunchedEffect(showRequests) {
        barVisible = true
        delay(IDLE_HIDE_MILLIS)
        barVisible = false
    }

    AppBottomBarContent(
        currentRoute = currentRoute,
        onItemClick = { route ->
            showRequests++
            onItemClick(route)
        },
        onAddClick = {
            showRequests++
            onAddClick()
        },
        barVisible = barVisible,
        onRevealClick = { showRequests++ },
        hazeState = hazeState,
        modifier = modifier
    )
}

/**
 * Pure visuals for [AppBottomBar]: the capsule (or its reveal handle) plus the
 * docked FAB. Takes visibility as a parameter rather than owning it, so both
 * states can be previewed directly.
 *
 * Layout:
 * - The four destinations split 2 / 2 around a centre gap, matching the grouping
 *   [AppNavigationRail] uses, so the Add action lands in the middle of the shell
 *   on every form factor.
 * - Destination pills are `weight(1f)` each: they spread perfectly evenly and
 *   can never overflow the capsule on a narrow window.
 * - The capsule spans [CapsuleWidthFraction] of the available width (a 10% inset
 *   from edge-to-edge) and is centred by the parent.
 * - Destination labels wrap instead of truncating as the system font scale
 *   rises ([maxLinesForTier]) and their containers flex with them, so nothing
 *   is clipped at Large/Huge font scales.
 * - The FAB is top-aligned to the capsule and pushed up by half its diameter PLUS
 *   its glow inset — the wrapper the button is centred in is taller than the button,
 *   and the wrapper is what gets aligned — so its midpoint sits exactly on the
 *   capsule's top edge (half inside, half protruding) at any capsule height.
 *
 * Adaptivity: the bar is theme-aware and reflows with the system font scale
 * (see [capsuleMinHeight]), wraps instead of clipping, caps its width for wide
 * windows, and honours the navigation-bar inset. Screens wider than Compact
 * portrait get [AppNavigationRail] instead, which carries the same centred Add
 * action.
 */
@Composable
private fun AppBottomBarContent(
    currentRoute: AppRoute?,
    onItemClick: (AppRoute) -> Unit,
    onAddClick: () -> Unit,
    barVisible: Boolean,
    onRevealClick: () -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val capsuleShape = RoundedCornerShape(32.dp)

    // Frosted glass. A true backdrop blur is not available here: the bar is a SHARED
    // sibling of the scrolling content, not its parent, so there is no composable for
    // a RenderEffect to sample. What is available is translucency — the surface tone
    // drawn at 80% over whatever is behind it — so live content reads through the
    // capsule instead of being hidden behind an opaque slab, and the hairline border
    // gives the pane the edge a glass surface needs to stay legible against both the
    // app background and a bright card scrolled under it.
    //
    // Light is the spec's white bar, edged with the nav's own hairline instead of the
    // card outline: the capsule floats over live content rather than sitting in the
    // card grid, so its edge is drawn a touch warmer and lighter than the grid's. Dark
    // keeps both the elevated tone and the outline wash it has always used.
    val isDark = MaterialTheme.colorScheme.isDark
    val containerColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.80f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.80f)
    }
    val capsuleBorderColor = if (isDark) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
    } else {
        NavEdgeLight
    }

    val capsuleMinHeight = capsuleMinHeight(LocalFontScaleInfo.current.tier)

    // Split rather than hardcoding indices, so the centre gap stays in the middle
    // if a destination is ever added or removed.
    val half = bottomNavBarItems.size / 2
    val leadingItems = bottomNavBarItems.take(half)
    val trailingItems = bottomNavBarItems.drop(half)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Composed BEFORE the bar so the bar — later in the Box — paints on top
        // of it. Both are briefly on screen mid-transition, and the handle would
        // otherwise draw over the rising capsule.
        AnimatedVisibility(
            visible = !barVisible,
            // Delayed so the sequence reads as the bar leaving, then the handle
            // arriving — never both mid-flight at once.
            enter = slideInVertically(
                animationSpec = tween(
                    BarSlideMillis,
                    delayMillis = BarSlideMillis / 2,
                    easing = FastOutSlowInEasing
                ),
                initialOffsetY = { it }
            ) + fadeIn(tween(BarSlideMillis, delayMillis = BarSlideMillis / 2)),
            exit = slideOutVertically(
                animationSpec = tween(BarSlideMillis, easing = FastOutLinearInEasing),
                targetOffsetY = { it }
            ) + fadeOut(tween(BarSlideMillis)),
            label = "bottom_bar_handle_reveal"
        ) {
            BottomBarRevealHandle(onClick = onRevealClick)
        }

        AnimatedVisibility(
            visible = barVisible,
            enter = slideInVertically(
                animationSpec = tween(BarSlideMillis, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ) + fadeIn(tween(BarSlideMillis)),
            exit = slideOutVertically(
                animationSpec = tween(BarSlideMillis, easing = FastOutLinearInEasing),
                targetOffsetY = { it }
            ) + fadeOut(tween(BarSlideMillis)),
            label = "bottom_bar_reveal"
        ) {
            // Every inset lives INSIDE the sliding element so its bottom edge is
            // the screen edge. Sliding down by its own height then clears the
            // screen completely — with the insets outside, the FAB's protruding
            // half would still peek above the bottom of the display.
            //
            // The top pad reserves the FAB's protruding half plus its glow inset,
            // because the offset below moves the whole composed FAB — circle plus
            // the glow box it is centred in — and it is that box which has to fit
            // inside the element for the slide to clear the display.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        top = AddTransactionFabSize / 2 + AddTransactionFabGlowInset,
                        start = Dimens.spacingCompact,
                        end = Dimens.spacingCompact,
                        bottom = Dimens.spacingCompact
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Wraps the capsule exactly. The FAB is a SIBLING of the clipped
                // capsule (not a child — the capsule's clip would slice off its
                // protruding half) and anchors to this wrapper's top edge, which
                // the top padding above has already aligned to the Column's top.
                Box(contentAlignment = Alignment.TopCenter) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(CapsuleWidthFraction)
                            .widthIn(max = CapsuleMaxWidth)
                            .heightIn(min = capsuleMinHeight)
                            .shadow(
                                elevation = 12.dp,
                                shape = capsuleShape,
                                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
                            )
                            .clip(capsuleShape)
                            .then(
                                if (hazeState != null) {
                                    Modifier.hazeEffect(
                                        state = hazeState,
                                        style = HazeStyle(
                                            // The frost's own fill, and what the app
                                            // actually samples: white from the scheme in
                                            // light, so the light bar cannot drift off the
                                            // spec, and the same charcoal it has always
                                            // been in dark.
                                            backgroundColor = if (isDark) Color(0xD90E0D13)
                                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                            blurRadius = 24.dp,
                                            noiseFactor = 0.03f,
                                            tints = emptyList()
                                        )
                                    )
                                } else {
                                    Modifier.background(containerColor)
                                }
                            )
                            // Drawn after the background so the hairline sits on top
                            // of it rather than being painted over by it.
                            .border(1.dp, capsuleBorderColor, capsuleShape)
                            // Tight inner padding: the destination labels need this
                            // room at Huge font scale more than the capsule needs the
                            // breathing space.
                            .padding(horizontal = Dimens.spacingTiny),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        leadingItems.forEach { item ->
                            FloatingCapsuleNavItem(
                                item = item,
                                selected = currentRoute == item.route,
                                onClick = { onItemClick(item.route) }
                            )
                        }

                        // Centre gap the FAB docks into.
                        Spacer(modifier = Modifier.width(AddFabSlotWidth))

                        trailingItems.forEach { item ->
                            FloatingCapsuleNavItem(
                                item = item,
                                selected = currentRoute == item.route,
                                onClick = { onItemClick(item.route) }
                            )
                        }
                    }

                    // Docked Add FAB — half inside the capsule, half above it. It
                    // rides the bar's show/hide animation rather than tracking the
                    // list scroll, so the centre gap is never left as a bare hole.
                    //
                    // The offset covers the glow inset as well as the radius: what
                    // is aligned to the capsule is the glow box, and the circle sits
                    // one inset inside that box.
                    AddTransactionFab(
                        onClick = onAddClick,
                        modifier = Modifier.offset(
                            y = -(AddTransactionFabSize / 2 + AddTransactionFabGlowInset)
                        )
                    )
                }
            }
        }

    }
}

/**
 * Circular reveal handle that slides up in the bar's place once it has hidden
 * itself. Centred on the horizontal axis the docked FAB occupied and floated one
 * bottom gap above the edge, so the bar reads as collapsing into a single
 * affordance rather than jumping to a different spot.
 *
 * Deliberately NOT the capsule's `surfaceColorAtElevation` treatment: in both
 * light and dark mode that tone sits almost on top of the screen background, so
 * the handle read as a smudge. A `surface` component can afford to be quiet —
 * this one is the only route back to the destination bar, so it carries a filled
 * gradient with a primary/secondary-tinted shadow.
 *
 * The fill is [fabGradient], the same one the docked FAB uses, and the chevron
 * takes the same [onBrandGradient] ink — so the affordance the bar collapses into
 * is visibly the same button as the one it collapsed from, rather than a second
 * brand surface a shade off it. Both ends of the gradient are derived from
 * [MaterialTheme.colorScheme], so it separates from the background in either theme.
 */
@Composable
private fun BottomBarRevealHandle(onClick: () -> Unit) {
    val handleShape = CircleShape

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = Dimens.spacingCompact),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(RevealHandleSize)
                .shadow(
                    elevation = 22.dp,
                    shape = handleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                    spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)
                )
                .clip(handleShape)
                .background(brush = fabGradient())
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = stringResource(R.string.desc_show_navigation_bar),
                tint = MaterialTheme.colorScheme.onBrandGradient,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun RowScope.FloatingCapsuleNavItem(
    item: BottomNavBarItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val indicatorShape = RoundedCornerShape(20.dp)
    val isDark = MaterialTheme.colorScheme.isDark

    val selectedContent = if (isDark) NavOnDark else NavOnLight
    val unselectedContent = if (isDark) NavOffDark else NavOffLight

    val iconTint by animateColorAsState(
        targetValue = if (selected) selectedContent else unselectedContent,
        label = "bottom_bar_icon_tint"
    )

    val labelColor by animateColorAsState(
        targetValue = if (selected) selectedContent else unselectedContent,
        label = "bottom_bar_label_tint"
    )

    Box(
        modifier = Modifier
            // Equal weights spread the destinations evenly across the capsule and
            // guarantee they never overflow a narrow window.
            .weight(1f)
            .heightIn(min = NavItemMinHeight)
            .clip(indicatorShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = NavItemMinHeight)
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.icon,
                contentDescription = stringResource(item.titleRes),
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = stringResource(item.titleRes),
                color = labelColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                // User-facing copy: wrap to a second line as the font scale rises
                // rather than truncate. The pill and the capsule both flex via
                // heightIn(min = …), so the extra line is never clipped.
                maxLines = maxLinesForTier(compact = 1, large = 2, huge = 2),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(name = "Bottom bar - Revealed", showBackground = true)
@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun AppBottomBarRevealedPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppBottomBarContent(
                currentRoute = AppRoute.Budget,
                onItemClick = {},
                onAddClick = {},
                barVisible = true,
                onRevealClick = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Preview(name = "Bottom bar - Hidden (handle only)", showBackground = true)
@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun AppBottomBarHiddenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppBottomBarContent(
                currentRoute = AppRoute.Budget,
                onItemClick = {},
                onAddClick = {},
                barVisible = false,
                onRevealClick = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
