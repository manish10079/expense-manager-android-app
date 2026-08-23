package com.mknlabs.expensetracker.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Surface
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.PurplePrimary

@Composable
fun ProfileAvatar(
    gender: String = "",
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    photoUri: String? = null,
    showBadge: Boolean = false,
    badgeIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.Check,
    @DrawableRes badgeIconRes: Int? = null,
    badgeContentDescription: String? = null,
    showGlow: Boolean = true,
    showBorder: Boolean = true,
    backgroundBrush: Brush? = null,
    backgroundColor: Color? = null,
    borderBrush: Brush? = null,
    userTier: UserTier = UserTier.FREE,
    isSyncing: Boolean = false,
    isAnonymous: Boolean = false
) {
    val isPremium = userTier == UserTier.PREMIUM && !isAnonymous

    val infiniteTransition = rememberInfiniteTransition(label = "SyncRingTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SyncRingRotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (showGlow) {
            Box(
                modifier = Modifier
                    .size(size)
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f),
                        spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Premium Sync Ring
        if (isPremium) {
            val ringColor = PurplePrimary
            if (isSyncing) {
                Canvas(
                    modifier = Modifier
                        .size(size)
                        .rotate(rotation)
                ) {
                    drawArc(
                        color = ringColor,
                        startAngle = 0f,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(size)
                        .border(width = 2.dp, color = ringColor, shape = CircleShape)
                )
            }
        }

        // Anonymous Dashed Border
        if (isAnonymous) {
            val dashColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            Canvas(
                modifier = Modifier.size(size)
            ) {
                drawCircle(
                    color = dashColor,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .size(if (isPremium || isAnonymous) size * 0.88f else size * 0.95f)
                .clip(CircleShape)
                .then(
                    backgroundBrush?.let { brush ->
                        Modifier.background(brush = brush, shape = CircleShape)
                    } ?: Modifier.background(
                        color = backgroundColor ?: MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                        shape = CircleShape
                    )
                )
                .then(
                    if (showBorder && !isPremium && !isAnonymous) {
                        if (borderBrush != null) {
                            Modifier.border(
                                width = 2.dp,
                                brush = borderBrush,
                                shape = CircleShape
                            )
                        } else {
                            val borderColor = MaterialTheme.colorScheme.outlineVariant
                            Modifier.border(
                                width = 2.dp,
                                color = borderColor,
                                shape = CircleShape
                            )
                        }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUri.isNullOrBlank()) {
                // Coil handles memory cache + disk cache + network automatically.
                // No flicker on scroll because the image is cached in memory.
                AsyncImage(
                    model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(photoUri)
                        .crossfade(true)
                        .transformations(CircleCropTransformation())
                        .build(),
                    contentDescription = stringResource(R.string.desc_profile_photo),
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    // Show gender avatar as placeholder while image loads (first time only)
                    placeholder = painterResource(id = genderToAvatarRes(gender)),
                    error = painterResource(id = genderToAvatarRes(gender)),
                    fallback = painterResource(id = genderToAvatarRes(gender))
                )
            } else {
                // No photo at all — show gender-based avatar
                androidx.compose.foundation.Image(
                    painter = painterResource(id = genderToAvatarRes(gender)),
                    contentDescription = stringResource(R.string.desc_profile_placeholder),
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.FillBounds
                )
            }
        }

        if (showBadge) {
            val badgeChipSize = if (badgeIconRes != null) size * 0.432f else size * 0.216f
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = size * 0.04f, y = size * 0.04f)
                    .size(badgeChipSize)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color.Black,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (badgeIconRes != null) {
                    Icon(
                        painter = painterResource(id = badgeIconRes),
                        contentDescription = badgeContentDescription,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(size * 0.27f)
                    )
                } else {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = badgeContentDescription,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(size * 0.135f)
                    )
                }
            }
        }
    }
}

/**
 * Returns the appropriate avatar drawable resource based on the user's selected gender.
 */
@DrawableRes
private fun genderToAvatarRes(gender: String): Int = when (gender) {
    "Male" -> R.drawable.ic_avatar_male
    "Female" -> R.drawable.ic_avatar_female
    else -> R.drawable.ic_avatar_default
}

@Preview(name = "Light Mode - Default", showBackground = true)
@Composable
fun ProfileAvatarLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                ProfileAvatar(
                    gender = "",
                    showBadge = true
                )
            }
        }
    }
}

@Preview(name = "Dark Mode - Male", showBackground = true)
@Composable
fun ProfileAvatarDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                ProfileAvatar(
                    gender = "Male",
                    showBadge = true
                )
            }
        }
    }
}
