package com.mknlabs.expensetracker.core.ui.components

import android.content.res.Configuration
import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.core.ui.theme.CardLight
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.utils.toTitleCase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * The tier chip ("PRO" / "FREE") is drawn at 90% of the label style and padding it borrows,
 * so it stays a chip beside the name without matching the name size.
 */
private const val TIER_BADGE_SCALE = 1.15f

@Composable
fun ProfileCard(
    name: String,
    email: String,
    gender: String = "",
    photoUri: String? = null,
    userTier: UserTier = UserTier.FREE,
    isAnonymous: Boolean = false,
    isSyncing: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.isDark
    val isPremium = userTier == UserTier.PREMIUM && !isAnonymous
    val cardShape = AppCardDefaults.shape()
    // The sweep below draws a round rect of its own, so it has to trace the same radius
    // the card does: a 24dp light card outlined at the old 20dp would show the ring
    // stepping in at every corner.
    val cardCornerRadius = AppCardDefaults.CornerRadius

    // Animates 2 full rotations (720 deg) and blends into the background upon visiting the settings screen
    val borderProgress = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(isPremium) {
        if (isPremium) {
            borderProgress.snapTo(0f)
            glowAlpha.snapTo(0f)

            coroutineScope {
                launch {
                    glowAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                    )
                    glowAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 4500, easing = FastOutLinearInEasing)
                    )
                }
                borderProgress.animateTo(
                    targetValue = 2f, // 2 full 360-degree rotations (720 deg)
                    animationSpec = tween(durationMillis = 5000, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    // The sweep is meant to fade into whatever sits behind the card, so its two dark stops
    // take the theme's background. They used to be a hardcoded black, which read as
    // "invisible" on the dark palette but drew a black ring around the card in light mode.
    val brandColors = listOf(
        colorScheme.accentInk,
        colorScheme.background,
        colorScheme.background,
        colorScheme.accentInk
    )

    val currentAlpha = glowAlpha.value
    val animatedBorderModifier = if (isPremium && currentAlpha > 0f) {
        Modifier.drawWithContent {
            drawContent()

            if (currentAlpha > 0f) {
                val angle = borderProgress.value * 360f
                val strokePx = (2.dp + 1.2.dp * (1f - (borderProgress.value / 2f).coerceIn(0f, 1f))).toPx()
                val cornerRadiusPx = cardCornerRadius.toPx()

                val shader = SweepGradient(
                    size.width / 2f,
                    size.height / 2f,
                    brandColors.map { it.toArgb() }.toIntArray(),
                    null
                )
                val matrix = Matrix()
                matrix.postRotate(angle, size.width / 2f, size.height / 2f)
                shader.setLocalMatrix(matrix)

                drawRoundRect(
                    brush = ShaderBrush(shader),
                    size = size,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = strokePx),
                    alpha = currentAlpha
                )
            }
        }
    } else {
        Modifier
    }

    AppCard(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = cardShape,
        // Dark keeps the tonal container the card has always had — the tonal elevation
        // it used to pass was inert, since the container was named explicitly — and light
        // takes the white card, outline and soft lift. The premium tier's own lift
        // travels with it rather than being flattened away.
        colors = AppCardDefaults.colors(colorScheme.surfaceContainerLow),
        elevation = if (isDark) (if (isPremium) 2.dp else 0.dp) else AppCardDefaults.Elevation,
        modifier = modifier
            .fillMaxWidth()
            .then(animatedBorderModifier)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                gender = gender,
                photoUri = photoUri,
                size = 64.dp,
                showGlow = isPremium,
                showBorder = true,
                backgroundBrush = if (isDark) brandGradient(alpha = 0.12f) else null,
                backgroundColor = if (isDark) null else colorScheme.accentInk.copy(alpha = 0.1f),
                userTier = userTier,
                isSyncing = isSyncing,
                isAnonymous = isAnonymous
                // No crown badge: the tier chip beside the name already says "PRO", and two
                // markers for the same fact on one card is one too many.
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = name.toTitleCase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Shown even without an account: the tier is the very thing that changes
                    // when a Pro user signs out, and hiding the chip hid the change with it.
                    //
                    // Glyph and padding both shrink, so the chip stays a true 90% copy of the
                    // label style instead of a smaller word in a full-size box.
                    val tierBadgeLabelStyle = MaterialTheme.typography.labelSmall
                    val tierBadgeTextStyle = tierBadgeLabelStyle.copy(
                        fontSize = tierBadgeLabelStyle.fontSize * TIER_BADGE_SCALE,
                        lineHeight = tierBadgeLabelStyle.lineHeight * TIER_BADGE_SCALE,
                        brush = if (isDark) brandGradient() else null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isPremium -> MaterialTheme.colorScheme.primaryContainer
                            // A neutral chip in light; the brand tint stays the premium
                            // tier's, where it means something.
                            isDark -> MaterialTheme.colorScheme.surfaceContainerHigh
                            else -> CardLight
                        },
                        contentColor = if (isPremium) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            // "Pro" / "Free": the short pairing the chip has always used, so a
                            // guest reads as a tier rather than as an error. Other screens keep
                            // the longer "Free Tier" wording.
                            text = if (isPremium) stringResource(com.mknlabs.expensetracker.R.string.label_pro) else stringResource(com.mknlabs.expensetracker.R.string.label_free),
                            style = tierBadgeTextStyle,
                            color = if (isDark) {
                                Color.Transparent
                            } else if (isPremium) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(
                                horizontal = 8.dp * TIER_BADGE_SCALE,
                                vertical = 2.dp * TIER_BADGE_SCALE
                            )
                        )
                    }
                }
                Spacer(Modifier.padding(top = 5.dp))
                if (isAnonymous) {
                    val inviteRamp = brandGradient()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                .drawWithCache {
                                    onDrawWithContent {
                                        drawContent()
                                        drawRect(brush = inviteRamp, blendMode = BlendMode.SrcIn)
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(com.mknlabs.expensetracker.R.string.label_backup_sync_is_off),
                            style = MaterialTheme.typography.bodyMedium.copy(brush = inviteRamp),
                            color = Color.Unspecified,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(com.mknlabs.expensetracker.R.string.label_turn_on),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                brush = inviteRamp
                            ),
                            color = Color.Unspecified,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = stringResource(com.mknlabs.expensetracker.R.string.label_sign_in_keep_data_safe),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (email.isNotEmpty()) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileCardPreview() {
    ExpenseTrackerTheme {
        Surface {
            ProfileCard(
                name = "Johnathan Doe",
                email = "john.doe@example.com",
                gender = "Male",
                userTier = UserTier.PREMIUM,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
