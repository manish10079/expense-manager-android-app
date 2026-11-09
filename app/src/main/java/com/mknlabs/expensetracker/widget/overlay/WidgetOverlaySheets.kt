package com.mknlabs.expensetracker.widget.overlay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Camera
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Images
import com.adamglin.phosphoricons.regular.Scan
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.cta
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onCta
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.mknlabs.expensetracker.core.ui.components.VoiceListeningContent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.dp

@Composable
internal fun OverlayTextContent(onClose: () -> Unit) {
    val inspection = LocalInspectionMode.current
    val focusRequester = remember { FocusRequester() }
    var text by remember { mutableStateOf(if (inspection) "Coffee 120" else "") }
    LaunchedEffect(inspection) {
        if (!inspection) {
            kotlinx.coroutines.delay(150)
            runCatching { focusRequester.requestFocus() }
        }
    }
    val scheme = MaterialTheme.colorScheme
    val cardColor = if (scheme.isDark) scheme.genderRow else Color(0xFFF1F5F9)
    val canSend = text.isNotBlank()
    Box(modifier = Modifier.fillMaxWidth()) {
        OverlaySheetOrbs(modifier = Modifier.matchParentSize(), dark = scheme.isDark)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brandGradient()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    PhosphorIcons.Regular.PencilSimple,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Type",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (scheme.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                )
                Text(
                    text = "Type your expense/income as simple text",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardColor)
                .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .then(if (inspection) Modifier else Modifier.focusRequester(focusRequester)),
                singleLine = false,
                maxLines = 3,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
                cursorBrush = SolidColor(scheme.cta),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { if (canSend) onClose() }
                ),
                decorationBox = { inner ->
                    if (text.isEmpty()) {
                        Text(
                            "Add a note or amount",
                            color = scheme.onSurfaceVariant,
                            fontSize = 15.sp,
                            maxLines = 2
                        )
                    }
                    inner()
                }
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brandGradient(alpha = if (canSend) 1f else 0.45f))
                    .clickable(enabled = canSend, onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Lucide.ArrowRight,
                    contentDescription = "Send",
                    tint = scheme.onCta,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun OverlayVoiceSheet(
    transcript: String,
    isProcessing: Boolean,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    val dark = MaterialTheme.colorScheme.isDark
    Box(modifier = Modifier.fillMaxWidth()) {
        OverlaySheetOrbs(modifier = Modifier.matchParentSize(), dark = dark)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 16.dp)
        ) {
            VoiceListeningContent(
                transcript = transcript,
                isProcessing = isProcessing,
                onCancel = onCancel,
                onDone = onDone
            )
        }
    }
}

@Composable
internal fun OverlayScanSheet(
    onCamera: () -> Unit,
    onPick: () -> Unit,
    onClose: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.isDark
    val sheetFill = Color(0xF20B1220)
    val cardFill = if (dark) sheetFill else Color.White.copy(alpha = 0.70f)
    val cardBorder = if (dark) Color(0xCC334155) else Color.White.copy(alpha = 0.80f)
    val cancelFill = if (dark) sheetFill else Color(0x99E2E8F0)
    val cancelBorder = if (dark) Color(0x99334155) else Color(0x80CBD5E1)
    Box(modifier = Modifier.fillMaxWidth()) {
        OverlaySheetOrbs(modifier = Modifier.matchParentSize(), dark = dark)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        val iconPulse = rememberInfiniteTransition(label = "scan-icon-pulse")
        val ring1 by iconPulse.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "scan-icon-ring1"
        )
        val ring2 by iconPulse.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, delayMillis = 450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "scan-icon-ring2"
        )
        val glow by iconPulse.animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scan-icon-glow"
        )
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        scaleX = ring1
                        scaleY = ring1
                        alpha = (1.4f - ring1).coerceIn(0f, 0.5f)
                    }
                    .border(1.5.dp, scheme.accentInk.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        scaleX = ring2
                        scaleY = ring2
                        alpha = (1.4f - ring2).coerceIn(0f, 0.4f)
                    }
                    .border(1.5.dp, scheme.accentInk.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer { scaleX = glow; scaleY = glow }
                    .clip(RoundedCornerShape(16.dp))
                    .background(brandGradient()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    PhosphorIcons.Regular.Scan,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Scan Document",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = scheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Upload a receipt or screenshot to automatically parse details",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OverlayScanActionCard(
                modifier = Modifier.weight(1f),
                icon = PhosphorIcons.Regular.Camera,
                title = "Capture Image",
                subtitle = "Use device camera",
                cardFill = cardFill,
                cardBorder = cardBorder,
                iconBrush = brandGradient(),
                onClick = onCamera
            )
            OverlayScanActionCard(
                modifier = Modifier.weight(1f),
                icon = PhosphorIcons.Regular.Images,
                title = "Pick Photo",
                subtitle = "From device gallery",
                cardFill = cardFill,
                cardBorder = cardBorder,
                iconBrush = Brush.linearGradient(
                    listOf(Color(0xFFA855F7), Color(0xFFEC4899), Color(0xFFF43F5E))
                ),
                onClick = onPick
            )
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, cancelBorder, RoundedCornerShape(16.dp))
                .background(cancelFill)
                .clickable(onClick = onClose)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cancel",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = if (dark) Color(0xFFCBD5E1) else Color(0xFF334155)
            )
        }
        Spacer(Modifier.height(8.dp))
        }
    }
}

private data class OverlayOrbKeyframe(
    val at: Float,
    val tx: Float,
    val ty: Float,
    val scale: Float,
    val alpha: Float
)

private val OverlayOrbBorderRoute = listOf(
    OverlayOrbKeyframe(0.00f, -0.42f, -0.42f, 1.10f, 0.85f),
    OverlayOrbKeyframe(0.25f, 0.42f, -0.42f, 0.90f, 0.90f),
    OverlayOrbKeyframe(0.50f, 0.42f, 0.42f, 1.15f, 0.70f),
    OverlayOrbKeyframe(0.75f, -0.42f, 0.42f, 0.95f, 0.85f),
    OverlayOrbKeyframe(1.00f, -0.42f, -0.42f, 1.10f, 0.85f)
)

private val OverlayOrbDiagRoute = listOf(
    OverlayOrbKeyframe(0.00f, -0.44f, -0.44f, 0.70f, 0.20f),
    OverlayOrbKeyframe(0.20f, -0.44f, -0.44f, 1.20f, 0.90f),
    OverlayOrbKeyframe(0.50f, 0.44f, 0.44f, 1.30f, 0.95f),
    OverlayOrbKeyframe(0.80f, 0.44f, 0.44f, 0.70f, 0.30f),
    OverlayOrbKeyframe(1.00f, -0.44f, -0.44f, 0.70f, 0.20f)
)

private fun sampleOverlayOrb(t: Float, keys: List<OverlayOrbKeyframe>): OverlayOrbKeyframe {
    val u = ((t % 1f) + 1f) % 1f
    val index = keys.indexOfLast { it.at <= u }.coerceAtLeast(0)
    val start = keys[index]
    val end = keys.getOrElse(index + 1) { keys.last() }
    val span = (end.at - start.at).takeIf { it > 0f } ?: 1f
    val local = FastOutSlowInEasing.transform(((u - start.at) / span).coerceIn(0f, 1f))
    return OverlayOrbKeyframe(
        at = u,
        tx = lerp(start.tx, end.tx, local),
        ty = lerp(start.ty, end.ty, local),
        scale = lerp(start.scale, end.scale, local),
        alpha = lerp(start.alpha, end.alpha, local)
    )
}

@Composable
private fun OverlaySheetOrbs(modifier: Modifier, dark: Boolean) {
    val density = LocalDensity.current
    val pulse = rememberInfiniteTransition(label = "scan-orbs")
    val t by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10_000, easing = LinearEasing), RepeatMode.Restart),
        label = "scan-orb-t"
    )
    val t2 by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(10_000, delayMillis = 3_000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "scan-orb-t2"
    )
    val border = sampleOverlayOrb(t, OverlayOrbBorderRoute)
    val diag = sampleOverlayOrb(t2, OverlayOrbDiagRoute)
    val borderSizePx = with(density) { 224.dp.toPx() }
    val diagSizePx = with(density) { 208.dp.toPx() }
    fun cornerX(tx: Float, span: Float, orb: Float, parent: Float): Float {
        val n = ((tx + span) / (span * 2f)).coerceIn(0f, 1f)
        return n * (parent - orb)
    }
    fun cornerY(ty: Float, span: Float, orb: Float, parent: Float): Float {
        val n = ((ty + span) / (span * 2f)).coerceIn(0f, 1f)
        return n * (parent - orb)
    }
    BoxWithConstraints(
        modifier = modifier.graphicsLayer { alpha = if (dark) 0.60f else 0.80f }
    ) {
        val parentW = constraints.maxWidth.toFloat()
        val parentH = constraints.maxHeight.toFloat()
        Box(
            modifier = Modifier
                .size(224.dp)
                .graphicsLayer {
                    translationX = cornerX(border.tx, 0.42f, borderSizePx, parentW)
                    translationY = cornerY(border.ty, 0.42f, borderSizePx, parentH)
                    scaleX = border.scale
                    scaleY = border.scale
                    alpha = border.alpha
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xCC8B5CF6),
                            Color(0x666366F1),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(208.dp)
                .graphicsLayer {
                    translationX = cornerX(diag.tx, 0.44f, diagSizePx, parentW)
                    translationY = cornerY(diag.ty, 0.44f, diagSizePx, parentH)
                    scaleX = diag.scale
                    scaleY = diag.scale
                    alpha = diag.alpha
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xCCEC4899),
                            Color(0x66F43F5E),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun OverlayScanActionCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    cardFill: Color,
    cardBorder: Color,
    iconBrush: Brush,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .background(cardFill)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBrush),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = scheme.onSurface)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, fontSize = 10.sp, color = scheme.onSurfaceVariant)
    }
}


