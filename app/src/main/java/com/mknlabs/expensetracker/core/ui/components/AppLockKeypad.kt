package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.AppLockPalette
import com.mknlabs.expensetracker.feature.auth.ui.APP_LOCK_DELETE_KEY
import com.mknlabs.expensetracker.feature.auth.ui.APP_LOCK_FORGOT_KEY

/**
 * The mock's 3×4 keypad: one circle per key, flat until pressed.
 *
 * [layout] comes from [buildAppLockKeypadLayout] so premium's scrambled keypad keeps
 * working; the mock only draws the unscrambled arrangement.
 */
@Composable
fun AppLockKeypad(
    layout: List<List<String>>,
    enabled: Boolean,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onForgot: () -> Unit,
    forgotLabel: String,
    palette: AppLockPalette
) {
    AnimatedContent(
        targetState = layout,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(140))
        },
        label = "app_lock_keypad_layout"
    ) { currentLayout ->
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            currentLayout.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    row.forEach { key ->
                        AppLockKey(
                            key = key,
                            enabled = enabled,
                            onClick = {
                                when (key) {
                                    APP_LOCK_DELETE_KEY -> onDelete()
                                    APP_LOCK_FORGOT_KEY -> onForgot()
                                    else -> onDigit(key)
                                }
                            },
                            forgotLabel = forgotLabel,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppLockKey(
    key: String,
    enabled: Boolean,
    onClick: () -> Unit,
    forgotLabel: String,
    palette: AppLockPalette,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    // The mock presses with `transform: scale(.92)` and swaps the fill/border for a brand
    // tint, both over 150ms.
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "app_lock_key_scale"
    )
    val fill = if (isPressed) palette.keyPressFill else palette.keyFill
    val border = if (isPressed) palette.keyPressBorder else palette.keyBorder

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(fill)
            .border(width = 1.dp, color = border, shape = CircleShape)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) 1f else 0.45f
        when (key) {
            APP_LOCK_DELETE_KEY -> Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = stringResource(R.string.desc_delete_pin_digit),
                tint = palette.ink.copy(alpha = contentAlpha),
                modifier = Modifier.size(22.dp)
            )

            APP_LOCK_FORGOT_KEY -> Text(
                text = forgotLabel,
                color = palette.ink,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                ),
                modifier = Modifier.graphicsLayer { alpha = 0.7f * contentAlpha }
            )

            else -> Text(
                text = key,
                color = palette.ink.copy(alpha = contentAlpha),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
            )
        }
    }
}
