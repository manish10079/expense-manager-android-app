package com.mknlabs.expensetracker.core.ui.components

import android.content.res.Configuration
import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.CloudOff
import com.composables.icons.lucide.LogIn
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Zap
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.SettingsHeroAvatarIndigo
import com.mknlabs.expensetracker.core.ui.theme.SettingsHeroAvatarPurple
import com.mknlabs.expensetracker.core.ui.theme.SettingsHeroAvatarRose
import com.mknlabs.expensetracker.core.ui.theme.SettingsHeroProBadgeEnd
import com.mknlabs.expensetracker.core.ui.theme.SettingsHeroProBadgeStart
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.invest
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.settingsHeroGlassEnd
import com.mknlabs.expensetracker.core.ui.theme.settingsHeroGlassStart
import com.mknlabs.expensetracker.core.ui.theme.settingsHeroHairline
import com.mknlabs.expensetracker.core.ui.theme.settingsHeroSweepCoral
import com.mknlabs.expensetracker.core.ui.theme.settingsHeroSweepSky
import com.mknlabs.expensetracker.core.ui.theme.settingsHeroSweepViolet
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.utils.toTitleCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val HeroCorner = 20.dp
private val AvatarOuter = 16.dp
private val AvatarInnerSignedIn = 14.dp
private val AvatarInnerSignedOut = 12.dp

@Composable
@Suppress("UNUSED_PARAMETER")
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
    val cardShape = RoundedCornerShape(HeroCorner)
    val glass = Brush.linearGradient(
        colors = listOf(colorScheme.settingsHeroGlassStart, colorScheme.settingsHeroGlassEnd),
        start = Offset.Zero,
        end = Offset(1000f, 1000f)
    )
    val sweepColors = listOf(
        Color.Transparent,
        Color.Transparent,
        colorScheme.settingsHeroSweepSky.copy(alpha = 0f),
        colorScheme.settingsHeroSweepSky,
        colorScheme.settingsHeroSweepViolet,
        colorScheme.settingsHeroSweepCoral,
        Color.Transparent
    )
    val sweepStops = floatArrayOf(0f, 0.50f, 0.583f, 0.722f, 0.861f, 0.944f, 1f)

    val borderProgress = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(1f) }

    LaunchedEffect(isPremium) {
        if (!isPremium) {
            borderProgress.snapTo(0f)
            glowAlpha.snapTo(0f)
            return@LaunchedEffect
        }
        borderProgress.snapTo(0f)
        glowAlpha.snapTo(1f)
        launch {
            borderProgress.animateTo(
                targetValue = 6f,
                animationSpec = tween(durationMillis = 14_400, easing = LinearEasing)
            )
        }
        launch {
            delay(12_960)
            glowAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1_440, easing = FastOutLinearInEasing)
            )
        }
    }

    val animatedBorderModifier = if (!isPremium) {
        Modifier
    } else {
        Modifier.drawWithContent {
        drawContent()
        val currentAlpha = glowAlpha.value
        if (currentAlpha > 0f) {
            val angle = (borderProgress.value % 1f) * 360f
            val strokePx = 2.dp.toPx()
            val cornerRadiusPx = (HeroCorner + 2.dp).toPx()
            val shader = SweepGradient(
                size.width / 2f,
                size.height / 2f,
                sweepColors.map { it.toArgb() }.toIntArray(),
                sweepStops
            )
            val matrix = Matrix()
            matrix.postRotate(angle, size.width / 2f, size.height / 2f)
            shader.setLocalMatrix(matrix)
            drawRoundRect(
                brush = ShaderBrush(shader),
                topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
                size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                style = Stroke(width = strokePx),
                alpha = currentAlpha
            )
        }
        }
    }

    AppCard(
        shape = cardShape,
        colors = AppCardColors(
            containerColor = Color.Transparent,
            contentColor = colorScheme.onSurface,
            border = BorderStroke(1.dp, colorScheme.settingsHeroHairline)
        ),
        elevation = if (isDark) 20.dp else 16.dp,
        brush = glass,
        modifier = modifier
            .fillMaxWidth()
            .then(animatedBorderModifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (onClick != null) {
                            Modifier.clickable(onClick = onClick)
                        } else {
                            Modifier
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsHeroAvatar(
                    gender = gender,
                    photoUri = photoUri,
                    isAnonymous = isAnonymous
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = name.toTitleCase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isAnonymous) FontWeight.SemiBold else FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isPremium) {
                            SettingsHeroProBadge()
                        } else if (isAnonymous) {
                            SettingsHeroOfflineChip()
                        }
                    }
                    if (isAnonymous) {
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Lucide.CircleAlert,
                                contentDescription = null,
                                tint = colorScheme.invest,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stringResource(R.string.label_backup_sync_is_off),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colorScheme.invest,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else if (email.isNotEmpty()) {
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            if (isAnonymous) {
                SettingsHeroSignInButton(onClick = onClick ?: {})
            }
        }
    }
}

@Composable
private fun SettingsHeroAvatar(
    gender: String,
    photoUri: String?,
    isAnonymous: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val ring = Brush.linearGradient(
        colors = listOf(
            SettingsHeroAvatarRose,
            SettingsHeroAvatarPurple,
            SettingsHeroAvatarIndigo
        )
    )
    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isAnonymous) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRoundRect(
                            color = colorScheme.outline,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            ),
                            cornerRadius = CornerRadius(AvatarOuter.toPx(), AvatarOuter.toPx())
                        )
                    }
                    .padding(2.dp)
            ) {
                SettingsHeroAvatarImage(
                    gender = gender,
                    photoUri = photoUri,
                    grayscale = true,
                    shape = RoundedCornerShape(AvatarInnerSignedOut)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(16.dp)
                    .border(2.dp, colorScheme.background, CircleShape)
                    .clip(CircleShape)
                    .background(colorScheme.invest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.CloudOff,
                    contentDescription = null,
                    tint = colorScheme.background,
                    modifier = Modifier.size(10.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(AvatarOuter),
                        ambientColor = SettingsHeroAvatarRose.copy(alpha = 0.25f),
                        spotColor = SettingsHeroAvatarPurple.copy(alpha = 0.20f)
                    )
                    .background(ring, RoundedCornerShape(AvatarOuter))
                    .padding(2.dp)
            ) {
                SettingsHeroAvatarImage(
                    gender = gender,
                    photoUri = photoUri,
                    grayscale = false,
                    shape = RoundedCornerShape(AvatarInnerSignedIn)
                )
            }
        }
    }
}

@Composable
private fun SettingsHeroAvatarImage(
    gender: String,
    photoUri: String?,
    grayscale: Boolean,
    shape: RoundedCornerShape
) {
    val grayFilter = if (grayscale) {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    } else {
        null
    }
    val imageMod = Modifier
        .fillMaxSize()
        .clip(shape)
        .then(if (grayscale) Modifier.alpha(0.3f) else Modifier)
    if (!photoUri.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUri)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.desc_profile_photo),
            modifier = imageMod,
            contentScale = ContentScale.Crop,
            colorFilter = grayFilter,
            placeholder = painterResource(id = genderToAvatarRes(gender)),
            error = painterResource(id = genderToAvatarRes(gender)),
            fallback = painterResource(id = genderToAvatarRes(gender))
        )
    } else {
        androidx.compose.foundation.Image(
            painter = painterResource(id = genderToAvatarRes(gender)),
            contentDescription = stringResource(R.string.desc_profile_placeholder),
            modifier = imageMod,
            contentScale = ContentScale.Crop,
            colorFilter = grayFilter
        )
    }
}

@Composable
private fun SettingsHeroProBadge() {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = SettingsHeroProBadgeStart.copy(alpha = 0.25f),
                spotColor = SettingsHeroProBadgeStart.copy(alpha = 0.25f)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(SettingsHeroProBadgeStart, SettingsHeroProBadgeEnd)
                ),
                CircleShape
            )
            .padding(horizontal = 7.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = Lucide.Zap,
            contentDescription = null,
            tint = scheme.onBrandGradient,
            modifier = Modifier.size(10.dp)
        )
        Text(
            text = stringResource(R.string.label_pro),
            color = scheme.onBrandGradient,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun SettingsHeroOfflineChip() {
    val scheme = MaterialTheme.colorScheme
    Text(
        text = stringResource(R.string.label_offline),
        modifier = Modifier
            .border(1.dp, scheme.invest.copy(alpha = 0.20f), CircleShape)
            .background(scheme.invest.copy(alpha = if (scheme.isDark) 0.10f else 0.12f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        color = scheme.invest,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        maxLines = 1
    )
}

@Composable
private fun SettingsHeroSignInButton(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = scheme.primary.copy(alpha = 0.25f),
                spotColor = scheme.primary.copy(alpha = 0.25f)
            )
            .background(brandGradient())
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Lucide.LogIn,
            contentDescription = null,
            tint = scheme.onBrandGradient,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = stringResource(R.string.label_turn_on),
            color = scheme.onBrandGradient,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@DrawableRes
private fun genderToAvatarRes(gender: String): Int = when (gender) {
    "Male" -> R.drawable.ic_avatar_male
    "Female" -> R.drawable.ic_avatar_female
    else -> R.drawable.ic_avatar_default
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileCardPreview() {
    ExpenseTrackerTheme {
        Surface {
            ProfileCard(
                name = "Manish",
                email = "syntax.sarcasm@gmail.com",
                gender = "Male",
                userTier = UserTier.PREMIUM,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Logged in Free Light", showBackground = true)
@Preview(name = "Logged in Free Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileCardLoggedInFreePreview() {
    ExpenseTrackerTheme {
        Surface {
            ProfileCard(
                name = "Manish",
                email = "syntax.sarcasm@gmail.com",
                gender = "Male",
                userTier = UserTier.FREE,
                isAnonymous = false,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Guest Light", showBackground = true)
@Preview(name = "Guest Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileCardGuestPreview() {
    ExpenseTrackerTheme {
        Surface {
            ProfileCard(
                name = "Manish",
                email = "",
                gender = "Male",
                isAnonymous = true,
                onClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
