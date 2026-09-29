package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.HeroRailEndLight
import com.mknlabs.expensetracker.core.ui.theme.HeroRailStartLight
import com.mknlabs.expensetracker.core.ui.theme.fabGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import kotlinx.coroutines.delay

/**
 * The measurements the brand "+" FAB is drawn with, exposed so a host that has to align
 * to the button's geometry (the dock in [AppBottomBar], the goals screen's FAB slot) can
 * do it without restating the numbers and drifting from them.
 */
object BrandAddFabDefaults {

    /** Diameter of the button itself. */
    val Size = 56.dp

    /** Glyph size inside that diameter. */
    val IconSize = 24.dp

    /** Height of the drop shadow, tuned to sit under a 56dp circle. */
    val ShadowElevation = 16.dp

    /**
     * How far the ambient halo reaches beyond the button on every side.
     *
     * Worth knowing for placement: the composed FAB is [Size] + 2 × this tall, with the
     * circle centred inside that box, so a host that aligns *this* box to a surface has
     * put the circle one inset away from the edge it meant to straddle. Whoever anchors
     * the FAB has to add the inset back — see the dock offset in [AppBottomBar].
     */
    val GlowInset = 14.dp

    /** Halo strength where it meets the button. */
    const val GlowStartAlpha = 0.35f

    /** Halo strength at its rim, before it fades out entirely. */
    const val GlowEndAlpha = 0.12f

    /** Shadow tint where the light is strongest. */
    const val ShadowAmbientAlpha = 0.40f

    /** Shadow tint at the point of contact. */
    const val ShadowSpotAlpha = 0.45f
}

/**
 * The one "+" FAB in the app.
 *
 * Every add button is this composable: the one docked in the bottom navigation bar, the
 * standalone one over the transactions list, the add-goal button, the manage-categories
 * button and the navigation rail's. They used to be four separate bodies that had already
 * drifted — two different ramps in dark mode, three sizes, two glyph sizes — so the point
 * of this component is that the ramp, the halo, the shadow and the shape exist once.
 *
 * The fill is [fabGradient], which is deliberately the same ramp in both themes, so the
 * button does not recolour on the light/dark switch. [accentStart] and [accentEnd] carry
 * that same pair for the halo and the shadow: an overridden [brush] should pass its own
 * two ends there, or the glow will light a different purple than the button it sits under.
 *
 * Everything a caller might legitimately need to vary is a parameter, so a screen changes
 * the button by passing a value rather than by copying the body.
 *
 * @param onClick Performs the add action.
 * @param modifier Placement, owned by the caller — [AppBottomBar] offsets it onto the
 *   capsule's edge, the goals screen compensates for [BrandAddFabDefaults.GlowInset].
 * @param icon Glyph in the button; the plain "+" unless a screen asks for its own.
 * @param contentDescription Accessibility label for that glyph. Null keeps the
 *   add-transaction string, so a screen reusing this button for a different action must
 *   pass its own or talkback will name the wrong action.
 * @param size Diameter. Only differ from [BrandAddFabDefaults.Size] where the host is
 *   genuinely narrower, as the rail is.
 * @param shape Silhouette. Passed to the shadow, the halo and the fill together, so the
 *   three cannot disagree — the bug that comes from shaping the fill but not the button.
 * @param iconSize Glyph size, scaled by each caller rather than derived, because the
 *   ratio is a design choice, not a constant.
 * @param brush Fill. Null takes [fabGradient].
 * @param contentColor Ink on the fill. Null takes [onBrandGradient].
 * @param accentStart Leading end of the halo and shadow tints.
 * @param accentEnd Trailing end of the halo and shadow tints.
 * @param glow Draw the ambient halo. False leaves just the shadow, for a host that
 *   already paints behind the button.
 * @param glowOffset Shifts the halo off-centre. A downward offset pools the light toward
 *   the bottom of the button, which reads as illuminating a frosted surface below it.
 * @param shadowElevation Height of the drop shadow.
 */
@Composable
fun BrandAddFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Add,
    contentDescription: String? = null,
    size: Dp = BrandAddFabDefaults.Size,
    shape: Shape = CircleShape,
    iconSize: Dp = BrandAddFabDefaults.IconSize,
    brush: Brush? = null,
    contentColor: Color? = null,
    accentStart: Color = HeroRailStartLight,
    accentEnd: Color = HeroRailEndLight,
    glow: Boolean = true,
    glowOffset: Dp = 0.dp,
    shadowElevation: Dp = BrandAddFabDefaults.ShadowElevation
) {
    val fill = brush ?: fabGradient()
    val ink = contentColor ?: MaterialTheme.colorScheme.onBrandGradient

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (glow) {
            Box(
                modifier = Modifier
                    .size(size + BrandAddFabDefaults.GlowInset * 2)
                    .offset(y = glowOffset)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentStart.copy(alpha = BrandAddFabDefaults.GlowStartAlpha),
                                accentEnd.copy(alpha = BrandAddFabDefaults.GlowEndAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = shape
                    )
            )
        }

        FloatingActionButton(
            onClick = onClick,
            shape = shape,
            // The fill is painted by the Box inside, so the container itself must stay
            // unpainted — otherwise a flat purple slab would sit behind the ramp and
            // flatten it back out.
            containerColor = Color.Transparent,
            contentColor = ink,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            ),
            modifier = Modifier
                .size(size)
                // Brand-tinted shadow, reading the same two ends as the fill so the
                // shadow cannot be a different purple than the button casting it.
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = accentStart.copy(alpha = BrandAddFabDefaults.ShadowAmbientAlpha),
                    spotColor = accentEnd.copy(alpha = BrandAddFabDefaults.ShadowSpotAlpha)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = fill, shape = shape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                        ?: stringResource(R.string.desc_add_transaction),
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

/**
 * Shared visibility controller for the standalone add FAB rendered by [MainScaffold] —
 * docked into the centre of the floating bottom navigation bar.
 *
 * Each tab screen binds its primary list's scroll direction to this state via
 * [rememberBindAddFabToScroll], so the FAB auto-hides while the user scrolls down
 * (revealing more list items) and reappears on scroll up. The default value keeps
 * previews compiling without a provider.
 */
val LocalAddFabVisibility = staticCompositionLocalOf<MutableState<Boolean>> {
    mutableStateOf(true)
}

/**
 * Renders [BrandAddFab] reading its visibility straight from [LocalAddFabVisibility].
 * Kept as its own composable so the visibility read is scoped here instead of recomposing
 * the entire [MainScaffold] on every scroll-direction flip.
 */
@Composable
fun BrandAddFabSlot(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    val scrollVisibility = LocalAddFabVisibility.current.value
    AnimatedVisibility(
        visible = visible && scrollVisibility,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)) +
            scaleIn(
                initialScale = 0f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            ),
        exit = fadeOut(animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing)) +
            scaleOut(
                targetScale = 0f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing)
            ),
        label = "brand_add_fab_visibility"
    ) {
        // Pools the halo downward, the way the docked FAB lights the frosted capsule it
        // straddles; the standalone FAB sits over the same bar, so it reads as the same
        // button.
        BrandAddFab(onClick = onClick, glowOffset = 8.dp)
    }
}

/**
 * Returns true while [listState] is actively scrolling (any direction), and false when
 * the scroll settles — the scroll-activity signal that drives the FAB's auto-hide
 * behavior.
 */
@Composable
fun rememberFabHiddenOnScroll(listState: LazyListState): Boolean {
    var hidden by remember { mutableStateOf(false) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (isScrolling) {
                    hidden = true
                } else {
                    // Small delay so the FAB doesn't flicker on micro-janks
                    delay(200L)
                    hidden = false
                }
            }
    }
    return hidden
}

/**
 * Binds [listState]'s scroll activity to the shared [LocalAddFabVisibility] state. Add
 * one call in each tab screen whose content scrolls vertically so the standalone FAB
 * hides while scrolling and reappears when scroll stops.
 */
@Composable
fun rememberBindAddFabToScroll(listState: LazyListState) {
    val visibility = LocalAddFabVisibility.current
    val hidden = rememberFabHiddenOnScroll(listState)
    LaunchedEffect(hidden) {
        visibility.value = !hidden
    }
}

@Preview(showBackground = true)
@Composable
private fun BrandAddFabPreview() {
    ExpenseTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BrandAddFab(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -(BrandAddFabDefaults.Size / 2 + BrandAddFabDefaults.GlowInset)),
                glowOffset = 8.dp
            )
        }
    }
}
