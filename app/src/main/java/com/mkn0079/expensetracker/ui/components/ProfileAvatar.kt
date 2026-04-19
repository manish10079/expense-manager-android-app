package com.mkn0079.expensetracker.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.vector.ImageVector
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurpleGlow
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream

@Composable
fun ProfileAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    textSize: TextUnit,
    photoUri: String? = null,
    showBadge: Boolean = false,
    badgeIcon: ImageVector = Icons.Filled.Check,
    showGlow: Boolean = true,
    showBorder: Boolean = true,
    backgroundBrush: Brush? = null,
    borderBrush: Brush? = null
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
                        ambientColor = PurplePrimary.copy(alpha = 0.26f),
                        spotColor = PurpleGlow.copy(alpha = 0.22f)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PurpleGlow.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Box(
            modifier = Modifier
                .size(size * 0.95f)
                .clip(CircleShape)
                .then(
                    backgroundBrush?.let { brush ->
                        Modifier.background(brush = brush, shape = CircleShape)
                    } ?: Modifier.background(Color.Transparent)
                )
                .then(
                    if (showBorder) {
                        if (borderBrush != null) {
                            Modifier.border(
                                width = 2.dp,
                                brush = borderBrush,
                                shape = CircleShape
                            )
                        } else {
                            Modifier.border(
                                width = 2.dp,
                                color = PurpleAccent.copy(alpha = 0.88f),
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
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = textSize,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.Center)
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
                            colors = listOf(PurplePrimary, PurpleAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = Color(0xFF24114C),
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
    else -> context.contentResolver.openInputStream(uri)
}
