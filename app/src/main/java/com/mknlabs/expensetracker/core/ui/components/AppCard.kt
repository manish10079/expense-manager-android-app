package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
 *
 * On the shadow: the spec asks for both a 2–3dp elevation and a 16–24dp blur, and
 * Compose derives the blur from the elevation, so the two cannot both hold. The look
 * is what the spec is after ("extremely soft", "do not use heavy Material shadows"),
 * so the elevation follows the blur and the lift is kept invisible by the 5–6% alpha
 * instead. That lands the vertical offset in the specified 4–6dp band. To go back to
 * a literal 2–3dp elevation, lower [AppCardDefaults.Elevation] and raise the alphas
 * above it — no call site has to change.
 *
 * @param contentPadding inside the card's own shape. Zero by default: the cards being
 *   migrated onto this already pad their own content, and a default would have doubled
 *   the padding on all of them. Pass [AppCardDefaults.ContentPadding] for a card that
 *   has none of its own.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = AppCardDefaults.shape(),
    colors: AppCardColors = AppCardDefaults.colors(),
    elevation: Dp = AppCardDefaults.Elevation,
    contentPadding: PaddingValues = AppCardDefaults.ContentPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCardSurface(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * The same card where the whole surface is the tap target.
 *
 * Separate overload rather than an optional lambda so the touch target, the ripple
 * and the role semantics come from [Surface] itself — a card that is clickable in
 * only part of its area is a card that half the users will miss.
 */
@Composable
fun AppCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = AppCardDefaults.shape(),
    colors: AppCardColors = AppCardDefaults.colors(),
    elevation: Dp = AppCardDefaults.Elevation,
    contentPadding: PaddingValues = AppCardDefaults.ContentPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCardSurface(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        onClick = onClick,
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
    contentPadding: PaddingValues,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val lifted = if (MaterialTheme.colorScheme.isDark) {
        modifier
    } else {
        // clip = false so the shadow is drawn outside the shape while the Surface
        // still clips its own content to it.
        modifier.shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = CardShadowAmbientLight,
            spotColor = CardShadowSpotLight,
        )
    }

    val body: @Composable () -> Unit = {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }

    if (onClick == null) {
        Surface(
            modifier = lifted,
            shape = shape,
            color = colors.containerColor,
            contentColor = colors.contentColor,
            border = colors.border,
            content = body,
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = lifted,
            enabled = enabled,
            shape = shape,
            color = colors.containerColor,
            contentColor = colors.contentColor,
            border = colors.border,
            content = body,
        )
    }
}

/** Container, ink and outline of an [AppCard]; see [AppCardDefaults.colors]. */
data class AppCardColors(
    val containerColor: Color,
    val contentColor: Color,
    val border: BorderStroke?,
)

object AppCardDefaults {

    /**
     * Default inside padding for a card that carries none of its own. The spacing
     * scale's base step, matching the 16dp the existing cards pad themselves with.
     */
    val ContentPadding = PaddingValues(Dimens.spacingDefault)

    /**
     * Shadow elevation. Light mode only — dark cards are not lifted — and the value
     * is the shadow's blur, not a Material elevation step. See [AppCard] for why.
     */
    val Elevation: Dp
        @Composable get() = if (MaterialTheme.colorScheme.isDark) 0.dp else 12.dp

    /**
     * 24dp in light, the spec's card radius. Dark keeps [Dimens.CardRadius], the 20dp
     * the existing cards were drawn with.
     */
    @Composable
    fun shape(): Shape = RoundedCornerShape(if (MaterialTheme.colorScheme.isDark) Dimens.CardRadius else 24.dp)

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
