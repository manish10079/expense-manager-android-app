package com.mknlabs.expensetracker.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Surface
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.InputStream

@Composable
fun ProfileAvatar(
    gender: String = "",
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    photoUri: String? = null,
    showBadge: Boolean = false,
    badgeIcon: ImageVector = Icons.Filled.Check,
    showGlow: Boolean = true,
    showBorder: Boolean = true,
    backgroundBrush: Brush? = null,
    backgroundColor: Color? = null,
    borderBrush: Brush? = null,
    userTier: UserTier = UserTier.FREE,
    isSyncing: Boolean = false,
    isAnonymous: Boolean = false
) {
    val context = LocalContext.current
    val targetSizePx = with(LocalDensity.current) { size.roundToPx().coerceAtLeast(1) }
    val avatarBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = photoUri,
        key2 = targetSizePx
    ) {
        value = photoUri?.let { uriString ->
            loadAvatarBitmap(
                context = context,
                uriString = uriString,
                targetSizePx = targetSizePx
            )
        }
    }

    val isPremium = userTier == UserTier.PREMIUM
    
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
            val ringColor = MaterialTheme.colorScheme.primary
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

        Box(
            modifier = Modifier
                .size(if (isPremium) size * 0.88f else size * 0.95f)
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
                    if (showBorder && !isPremium) {
                        if (borderBrush != null) {
                            Modifier.border(
                                width = 2.dp,
                                brush = borderBrush,
                                shape = CircleShape
                            )
                        } else {
                            val borderColor = Color.White
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
            val resolvedAvatarBitmap = avatarBitmap
            if (resolvedAvatarBitmap != null) {
                Image(
                    bitmap = resolvedAvatarBitmap,
                    contentDescription = stringResource(R.string.desc_profile_photo),
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Show gender-based avatar drawable as placeholder
                Image(
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
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.24f)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(size * 0.12f)
                )
            }
        }
    }
}

private suspend fun loadAvatarBitmap(
    context: Context,
    uriString: String,
    targetSizePx: Int
): ImageBitmap? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(uriString)
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            openImageInputStream(context, uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val sampleSize = calculateInSampleSize(
                width = boundsOptions.outWidth,
                height = boundsOptions.outHeight,
                requestedSize = targetSizePx
            )

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            openImageInputStream(context, uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)?.asImageBitmap()
            }
        }.getOrNull()
    }
}

private fun calculateInSampleSize(
    width: Int,
    height: Int,
    requestedSize: Int
): Int {
    if (width <= 0 || height <= 0 || requestedSize <= 0) {
        return 1
    }

    var sampleSize = 1
    var currentWidth = width
    var currentHeight = height

    while (currentWidth / 2 >= requestedSize && currentHeight / 2 >= requestedSize) {
        currentWidth /= 2
        currentHeight /= 2
        sampleSize *= 2
    }

    return sampleSize
}

private fun openImageInputStream(context: Context, uri: Uri) = when (uri.scheme) {
    "file" -> uri.path?.let(::FileInputStream)
    "http", "https" -> {
        try {
            val connection = java.net.URL(uri.toString()).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
            connection.getInputStream()
        } catch (e: Exception) {
            null
        }
    }
    else -> context.contentResolver.openInputStream(uri)
}

/**
 * Returns the appropriate avatar drawable resource based on the user's selected gender.
 * - "Male" → ic_avatar_male
 * - "Female" → ic_avatar_female
 * - Anything else (empty, Non-binary, Prefer not to say) → ic_avatar_default
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
