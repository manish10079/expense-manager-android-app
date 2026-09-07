package com.mknlabs.expensetracker.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.utils.toTitleCase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
    val isPremium = userTier == UserTier.PREMIUM && !isAnonymous
    val cardShape = RoundedCornerShape(20.dp)

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

    val brandColors = listOf(
        colorScheme.primary,
        androidx.compose.ui.graphics.Color.Black,
        androidx.compose.ui.graphics.Color.Black,
        colorScheme.primary
    )

    val currentAlpha = glowAlpha.value
    val animatedBorderModifier = if (isPremium && currentAlpha > 0f) {
        Modifier.drawWithContent {
            drawContent()

            if (currentAlpha > 0f) {
                val angle = borderProgress.value * 360f
                val strokePx = (2.dp + 1.2.dp * (1f - (borderProgress.value / 2f).coerceIn(0f, 1f))).toPx()
                val cornerRadiusPx = 20.dp.toPx()

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

    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = cardShape,
        color = colorScheme.surfaceContainerLow,
        tonalElevation = if (isPremium) 3.dp else 1.dp,
        shadowElevation = if (isPremium) 2.dp else 0.dp,
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
                backgroundColor = colorScheme.primary.copy(alpha = 0.1f),
                userTier = userTier,
                isSyncing = isSyncing,
                isAnonymous = isAnonymous,
                showBadge = isPremium,
                badgeIconRes = com.mknlabs.expensetracker.R.drawable.ic_crown,
                badgeContentDescription = stringResource(com.mknlabs.expensetracker.R.string.label_pro)
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

                    if (!isAnonymous) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPremium) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = if (isPremium) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Text(
                                text = if (isPremium) stringResource(com.mknlabs.expensetracker.R.string.label_pro) else stringResource(com.mknlabs.expensetracker.R.string.label_free_tier),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(top = 5.dp))
                val subtext = if (isAnonymous) {
                    stringResource(com.mknlabs.expensetracker.R.string.label_tap_to_sync)
                } else {
                    email
                }

                if (subtext.isNotEmpty()) {
                    Text(
                        text = subtext,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isAnonymous) MaterialTheme.colorScheme.primary else colorScheme.onSurfaceVariant,
                        maxLines = 1,
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
