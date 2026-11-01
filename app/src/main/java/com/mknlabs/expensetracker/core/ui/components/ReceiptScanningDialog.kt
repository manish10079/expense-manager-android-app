package com.mknlabs.expensetracker.core.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.FileImage
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lucide
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.cta
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onCta

private val BrandStart = Color(0xFF6A4DFF)
private val BrandEnd = Color(0xFF6750A4)
private val BrandGradient = Brush.linearGradient(listOf(BrandStart, BrandEnd))

private val LightCanvas = Color(0xFFF1F5F9)
private val LightCanvasBorder = Color(0xCCE2E8F0)
private val LightReceiptTop = Color(0xFFFFFFFF)
private val LightReceiptBottom = Color(0xFFF9FAFB)
private val LightReceiptBorder = Color(0xE6E2E8F0)
private val LightHeader = Color(0xFF94A3B8)
private val LightFileName = Color(0xFF334155)
private val LightFileSize = Color(0xFF94A3B8)
private val LightCancel = Color(0xFF94A3B8)
private val LightIconWell = Color(0x99E2E8F0)
private val LightIconWellBorder = Color(0x66CBD5E1)
private val LightIcon = Color(0x99647A8F)
private val LightSkeletonA = Color(0xB3CBD5E1)
private val LightSkeletonB = Color(0xCCE2E8F0)
private val LightSkeletonAmount = Color(0x266366F1)
private val LightDivider = Color(0xFFF1F5F9)

private val DarkCanvas = Color(0xFF09090B)
private val DarkCanvasBorder = Color(0xCC27272A)
private val DarkReceiptTop = Color(0xFF18181B)
private val DarkReceiptBottom = Color(0xFF09090B)
private val DarkReceiptBorder = Color(0xFF27272A)
private val DarkHeader = Color(0xFF71717A)
private val DarkFileName = Color(0xFFD4D4D8)
private val DarkFileSize = Color(0xFF71717A)
private val DarkCancel = Color(0xFF71717A)
private val DarkIconWell = Color(0xE618181B)
private val DarkIconWellBorder = Color(0xCC27272A)
private val DarkIcon = Color(0x80A1A1AA)
private val DarkSkeletonA = Color(0x993F3F46)
private val DarkSkeletonB = Color(0xFF27272A)
private val DarkSkeletonAmount = Color(0x336366F1)
private val DarkDivider = Color(0xCC27272A)

data class ScanningFileMeta(
    val displayName: String,
    val sizeLabel: String,
    val isImage: Boolean,
)

@Composable
fun ReceiptScanningDialog(
    uri: Uri?,
    onCancel: () -> Unit,
    statusMessageRes: Int? = null,
    onRetry: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val meta = remember(uri) { resolveScanningFileMeta(context, uri) }
    val dark = MaterialTheme.colorScheme.isDark

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (dark) DarkCanvas else LightCanvas,
            border = BorderStroke(
                1.dp,
                if (dark) DarkCanvasBorder else LightCanvasBorder,
            ),
            shadowElevation = if (dark) 24.dp else 16.dp,
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(BrandGradient),
                    )
                    Text(
                        text = stringResource(
                            if (meta.isImage) {
                                R.string.label_ai_scanning_image
                            } else {
                                R.string.label_ai_scanning_document
                            },
                        ),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (dark) DarkHeader else LightHeader,
                        ),
                    )
                }

                Box(
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (statusMessageRes != null) {
                        Text(
                            text = stringResource(statusMessageRes),
                            modifier = Modifier
                                .width(216.dp)
                                .padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = if (dark) DarkFileName else LightFileName,
                            ),
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        ScanningReceiptCard(isImage = meta.isImage, dark = dark)
                    }
                }

                Text(
                    text = meta.displayName,
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 0.3.sp,
                        color = if (dark) DarkFileName else LightFileName,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (meta.sizeLabel.isNotBlank()) {
                    Text(
                        text = meta.sizeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = if (dark) DarkFileSize else LightFileSize,
                        ),
                    )
                }
                val expenseColor = MaterialTheme.colorScheme.expense
                val ctaColor = MaterialTheme.colorScheme.cta
                if (statusMessageRes != null && onRetry != null) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .defaultMinSize(minHeight = 28.dp)
                            .height(28.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ctaColor,
                            contentColor = MaterialTheme.colorScheme.onCta,
                        ),
                        border = BorderStroke(1.dp, ctaColor),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 20.dp,
                            vertical = 2.dp,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.label_retry),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onCta,
                            ),
                        )
                    }
                }
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .defaultMinSize(minHeight = 28.dp)
                        .height(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = expenseColor,
                        contentColor = MaterialTheme.colorScheme.onCta,
                    ),
                    border = BorderStroke(1.dp, expenseColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp,
                        vertical = 2.dp,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.label_cancel),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onCta,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanningReceiptCard(
    isImage: Boolean,
    dark: Boolean,
) {
    val transition = rememberInfiniteTransition(label = "receipt-scan")
    val sweep = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scan-sweep",
    )
    val bracketScale = transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bracket-pulse",
    )
    val skeletonAlpha = transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton",
    )
    val cubePhase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cubes",
    )

    Box(
        modifier = Modifier
            .padding(14.dp)
            .size(width = 216.dp, height = 280.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(bracketScale.value),
        ) {
            val stroke = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Miter,
                miter = 4f,
            )
            val inset = 2.dp.toPx()
            val arm = 14.dp.toPx()
            val w = size.width
            val h = size.height
            val brand = BrandGradient
            fun cornerL(horizontalFrom: Offset, corner: Offset, verticalTo: Offset) {
                val path = Path().apply {
                    moveTo(horizontalFrom.x, horizontalFrom.y)
                    lineTo(corner.x, corner.y)
                    lineTo(verticalTo.x, verticalTo.y)
                }
                drawPath(path, brush = brand, style = stroke)
            }
            // True 90° L-brackets: both arms meet at the corner vertex.
            cornerL(
                Offset(inset + arm, inset),
                Offset(inset, inset),
                Offset(inset, inset + arm),
            )
            cornerL(
                Offset(w - inset - arm, inset),
                Offset(w - inset, inset),
                Offset(w - inset, inset + arm),
            )
            cornerL(
                Offset(inset + arm, h - inset),
                Offset(inset, h - inset),
                Offset(inset, h - inset - arm),
            )
            cornerL(
                Offset(w - inset - arm, h - inset),
                Offset(w - inset, h - inset),
                Offset(w - inset, h - inset - arm),
            )
        }

        Box(
            modifier = Modifier
                .width(192.dp)
                .height(256.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (dark) DarkReceiptTop else LightReceiptTop,
                            if (dark) DarkReceiptBottom else LightReceiptBottom,
                        ),
                    ),
                )
                .border(
                    1.dp,
                    if (dark) DarkReceiptBorder else LightReceiptBorder,
                    RoundedCornerShape(12.dp),
                )
                .padding(16.dp),
        ) {
            if (!isImage) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                0.88f to Color.Transparent,
                                1f to if (dark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f),
                            ),
                        ),
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (dark) DarkIconWell else LightIconWell)
                        .border(
                            1.dp,
                            if (dark) DarkIconWellBorder else LightIconWellBorder,
                            RoundedCornerShape(8.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isImage) Lucide.FileImage else Lucide.FileText,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(brush = AvatarRingBrush, blendMode = BlendMode.SrcIn)
                                }
                            },
                    )
                }

                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    val delays = (1..12).map { it * 0.1f }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        delays.chunked(6).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                row.forEach { delay ->
                                    val t = ((cubePhase.value - delay / 2.2f) % 1f + 1f) % 1f
                                    val fill = cubeFill(t)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .scale(0.85f + 0.15f * fill)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(BrandGradient.copy(alpha = 0.15f + 0.85f * fill)),
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .border(
                            width = 0.dp,
                            color = Color.Transparent,
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (dark) DarkDivider else LightDivider),
                    )
                    SkeletonBar(0.66f, 10.dp, if (dark) DarkSkeletonA else LightSkeletonA, skeletonAlpha.value)
                    SkeletonBar(0.5f, 8.dp, if (dark) DarkSkeletonB else LightSkeletonB, skeletonAlpha.value)
                    SkeletonBar(0.33f, 12.dp, if (dark) DarkSkeletonAmount else LightSkeletonAmount, skeletonAlpha.value)
                }
            }

            val density = LocalDensity.current
            val beamY = with(density) { (256.dp * sweep.value).toPx() }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val beamH = 48.dp.toPx()
                val top = beamY - beamH / 2f
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BrandStart.copy(alpha = if (dark) 0.25f else 0.20f),
                            Color.Transparent,
                        ),
                    ),
                    topLeft = Offset(0f, top),
                    size = Size(size.width, beamH),
                )
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, BrandStart, Color.Transparent),
                    ),
                    topLeft = Offset(0f, beamY - 0.75.dp.toPx()),
                    size = Size(size.width, 1.5.dp.toPx()),
                )
            }
        }
    }
}

@Composable
private fun SkeletonBar(fraction: Float, height: androidx.compose.ui.unit.Dp, color: Color, alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(fraction)
            .height(height)
            .clip(CircleShape)
            .background(color.copy(alpha = color.alpha * alpha / 0.75f)),
    )
}

private fun cubeFill(t: Float): Float {
    return when {
        t < 0.05f -> 0f
        t < 0.30f -> (t - 0.05f) / 0.25f
        t < 0.85f -> 1f
        t < 0.95f -> 1f - (t - 0.85f) / 0.10f
        else -> 0f
    }
}

private fun Brush.copy(alpha: Float): Brush {
    return Brush.linearGradient(listOf(BrandStart.copy(alpha = alpha), BrandEnd.copy(alpha = alpha)))
}

private fun resolveScanningFileMeta(context: Context, uri: Uri?): ScanningFileMeta {
    if (uri == null) {
        return ScanningFileMeta(
            displayName = context.getString(R.string.label_ai_scan_shared_source),
            sizeLabel = "",
            isImage = false,
        )
    }
    var name = uri.lastPathSegment?.substringAfterLast('/').orEmpty()
    var size = -1L
    runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) name = cursor.getString(nameIdx).orEmpty()
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
            }
        }
    }
    val mime = runCatching { context.contentResolver.getType(uri) }.getOrNull().orEmpty()
    val isImage = mime.startsWith("image/") ||
        name.endsWith(".jpg", true) ||
        name.endsWith(".jpeg", true) ||
        name.endsWith(".png", true) ||
        name.endsWith(".webp", true) ||
        name.endsWith(".heic", true)
    if (name.isBlank()) {
        name = context.getString(
            if (isImage) R.string.label_ai_scan_fallback_image else R.string.label_ai_scan_fallback_pdf,
        )
    }
    val sizeLabel = if (size >= 0) {
        formatFileSize(context, size)
    } else {
        ""
    }
    return ScanningFileMeta(name, sizeLabel, isImage)
}

private fun formatFileSize(context: Context, bytes: Long): String {
    if (bytes < 1024) {
        return context.getString(R.string.label_file_size_bytes, bytes)
    }
    val kb = bytes / 1024.0
    if (kb < 1024) {
        return context.getString(R.string.label_file_size_kb, kb)
    }
    val mb = kb / 1024.0
    return context.getString(R.string.label_file_size_mb, mb)
}
