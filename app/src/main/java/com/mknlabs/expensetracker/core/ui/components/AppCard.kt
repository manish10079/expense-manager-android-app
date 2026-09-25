package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.core.ui.theme.CardShadowAmbientLight
import com.mknlabs.expensetracker.core.ui.theme.CardShadowSpotLight
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.isDark

/**
 * The app's card surface, and the one place the light theme's card spec lives.
 *
 * Light mode, as specified: a white card on the soft grey field, held apart by a
 * hairline outline, lifted by a shadow that is felt rather than seen. Cards are the
 * app's only raised surface, so every one of them reads from here and they stay
 * identical to each other by construction rather than by review.
 *
 * Dark mode is deliberately left on the chrome the app already had — same container,
 * a 65%-alpha outline variant, no lift — because this pass is light-only. A card that
 * styled both themes would have quietly restyled the dark one, which is out of scope.
 * A component whose dark card has always cast a lift of its own — the premium halo, the
 * raised field — keeps it by passing that elevation in; [AppCardDefaults.Elevation]
 * itself stays zero in dark, so nothing that passes nothing is lifted.
 *
 * The chrome is built from modifiers rather than delegated to a Material `Surface`, so
 * that the fill, the outline and the ripple stack in a known order: fill first, outline
 * over it, ripple over both. A card that took its click handling through the caller's
 * `modifier` would draw the ripple underneath its own fill and look dead to the touch.
 *
 * On the shadow: the spec asks for both a 2–3dp elevation and a 16–24dp blur, and
 * Compose derives the blur from the elevation, so the two cannot both hold. The look
 * is what the spec is after ("extremely soft", "do not use heavy Material shadows"),
 * so the elevation follows the blur and the lift is kept invisible by the 5–6% alpha
 * instead, which lands the vertical offset in the specified 4–6dp band. To go back to a
 * literal 2–3dp elevation, lower [AppCardDefaults.Elevation] and raise the alphas above
 * it — no call site has to change.
 *
 * @param brush fill for a card that is a brand gradient rather than a flat colour. The
 *   app's hero surfaces are gradients against the dark field and plain cards against the
 *   light one, so a call site passes its gradient through
 *   [com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient] and lets light fall back
 *   to [AppCardColors.containerColor]. When non-null this replaces that colour as the
 *   fill; the border, shape and shadow are unaffected.
 * @param contentPadding inside the card's own shape. Zero by default: the cards being
 *   migrated onto this already pad their own content, and a default would have doubled
 *   the padding on all of them. Pass [AppCardDefaults.ContentPadding] for a card that
 *   carries none of its own.
 * @param content the card's content, in a [BoxScope] rather than the [ColumnScope] a
 *   Material card hands you — a card is as often a row with something pinned to a corner
 *   as it is a stack. Wrap it in a [Column] to stack.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = AppCardDefaults.shape(),
    colors: AppCardColors = AppCardDefaults.colors(),
    elevation: Dp = AppCardDefaults.Elevation,
    brush: Brush? = null,
    contentPadding: PaddingValues = AppCardDefaults.ContentPadding,
    content: @Composable BoxScope.() -> Unit,
) {
    AppCardSurface(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        brush = brush,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * The same card where the whole surface is the tap target.
 *
 * Separate overload rather than an optional lambda, so that the touch target, the ripple
 * and the minimum interactive size are the card's own business and not something every
 * call site has to remember. [onLongClick] is the same treatment for the lists that
 * enter selection mode on a long press.
 */
@Composable
fun AppCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    shape: Shape = AppCardDefaults.shape(),
    colors: AppCardColors = AppCardDefaults.colors(),
    elevation: Dp = AppCardDefaults.Elevation,
    brush: Brush? = null,
    contentPadding: PaddingValues = AppCardDefaults.ContentPadding,
    content: @Composable BoxScope.() -> Unit,
) {
    AppCardSurface(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        brush = brush,
        contentPadding = contentPadding,
        onClick = onClick,
        onLongClick = onLongClick,
        enabled = enabled,
        content = content,
    )
}

@Composable
private fun AppCardSurface(
    modifier: Modifier,
    shape: Shape,
    colors: AppCardColors,
    elevation: Dp,
    brush: Brush?,
    contentPadding: PaddingValues,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.isDark

    val chrome = modifier
        .then(
            // Zero elevation is what keeps a dark card un-lifted: that is the default
            // there. A dark component that has always cast a shadow of its own passes
            // its elevation in and keeps it, drawn with the plain default shadow — the
            // very call the Material Surface it replaced made — while light lifts with
            // the redesign's two soft tokens.
            when {
                elevation <= 0.dp -> Modifier
                // clip = false so the shadow is cast outside the shape while the card
                // itself is still clipped to it below.
                isDark -> Modifier.shadow(
                    elevation = elevation,
                    shape = shape,
                    clip = false,
                )
                else -> Modifier.shadow(
                    elevation = elevation,
                    shape = shape,
                    clip = false,
                    ambientColor = CardShadowAmbientLight,
                    spotColor = CardShadowSpotLight,
                )
            }
        )
        .clip(shape)
        .then(
            if (brush == null) {
                Modifier.background(colors.containerColor)
            } else {
                Modifier.background(brush)
            }
        )
        // border() has no nullable overload, and a card may deliberately carry no
        // outline at all — a hero painting its own edge, or a transparent row.
        .then(
            if (colors.border == null) {
                Modifier
            } else {
                Modifier.border(border = colors.border, shape = shape)
            }
        )
        .then(
            when {
                onClick == null -> Modifier
                onLongClick == null -> Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(enabled = enabled, onClick = onClick)
                else -> Modifier
                    .minimumInteractiveComponentSize()
                    .combinedClickable(
                        enabled = enabled,
                        onClick = onClick,
                        onLongClick = onLongClick,
                    )
            }
        )

    CompositionLocalProvider(LocalContentColor provides colors.contentColor) {
        Box(modifier = chrome.padding(contentPadding)) { content() }
    }
}

/** Container, ink and outline of an [AppCard]; see [AppCardDefaults.colors]. */
data class AppCardColors(
    val containerColor: Color,
    val contentColor: Color,
    val border: BorderStroke?,
)

object AppCardDefaults {

    /** No padding of the card's own; every migrated card pads its content itself. */
    val ContentPadding = PaddingValues(0.dp)

    /**
     * Shadow elevation. Light mode only — dark cards are not lifted — and the value is
     * the shadow's blur, not a Material elevation step. See [AppCard] for why.
     */
    val Elevation: Dp
        @Composable get() = if (MaterialTheme.colorScheme.isDark) 0.dp else 12.dp

    /**
     * [shape]'s radius as a number, for the few places that draw the card's own
     * geometry rather than its fill — a status ring, a halo. They have to agree with
     * the card corner for corner, so they read the radius from here instead of
     * repeating it, which is what keeps a 24dp light card from being outlined at the
     * old 20dp.
     */
    val CornerRadius: Dp
        @Composable get() = if (MaterialTheme.colorScheme.isDark) Dimens.CardRadius else 24.dp

    /**
     * 24dp in light, the spec's card radius. Dark keeps [Dimens.CardRadius], the 20dp
     * the existing cards were drawn with.
     */
    @Composable
    fun shape(): Shape = RoundedCornerShape(CornerRadius)

    /**
     * The standard card in light, and the container and outline the component already had
     * in dark. Most cards predate the redesign with a dark fill of their own — a tonal
     * surface, a gradient, a tint, no fill at all — and this pass must not restyle any of
     * them, so the dark values are spelled out at the call site and light takes the card
     * spec. Keeping the branch here rather than open-coding `isDark` in each component is
     * what makes "light-only" checkable by reading one function.
     */
    @Composable
    fun colors(darkContainer: Color, darkBorder: BorderStroke? = null): AppCardColors =
        if (MaterialTheme.colorScheme.isDark) {
            AppCardColors(
                containerColor = darkContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = darkBorder
            )
        } else {
            colors()
        }

    /**
     * The card that was a flat tinted surface — half-strength variant container and no
     * outline — which is what the setup and permission prompts have always been. Light
     * takes the standard card, because a tinted flat surface is the shape of thing the
     * light redesign is replacing with white cards.
     */
    @Composable
    fun tintedColors(): AppCardColors =
        colors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

    /**
     * White on the grey field with a hairline outline in light; the existing
     * surface-plus-soft-outline pairing in dark.
     */
    @Composable
    fun colors(): AppCardColors {
        val scheme = MaterialTheme.colorScheme
        return if (scheme.isDark) {
            AppCardColors(
                containerColor = scheme.surface,
                contentColor = scheme.onSurface,
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.65f)),
            )
        } else {
            AppCardColors(
                containerColor = scheme.surface,
                contentColor = scheme.onSurface,
                border = BorderStroke(1.dp, scheme.outline),
            )
        }
    }
}
