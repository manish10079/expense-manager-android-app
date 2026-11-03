package com.mknlabs.expensetracker.feature.auth.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.isDark
import java.util.Locale
import kotlin.math.sin

private val Brand600 = Color(0xFF7C3AED)
private val Brand500 = Color(0xFF8B5CF6)
private val Brand400 = Color(0xFFA78BFA)
private val Indigo600 = Color(0xFF4F46E5)
private val Indigo400 = Color(0xFF818CF8)
private val DarkCanvas = Color(0xFF0B0A16)
private val LightCanvas = Color(0xFFF8FAFC) // slate-50
private val Purple200 = Color(0xFFE9D5FF)
private val Indigo200 = Color(0xFFC7D2FE)
private val Pink200 = Color(0xFFFBCFE8)
private val Purple900 = Color(0xFF581C87)
private val Indigo300 = Color(0xFFA5B4FC)
private val Purple400 = Color(0xFFC084FC)

@Composable
fun WelcomeBackPage(
    userName: String,
    userEmail: String,
    modifier: Modifier = Modifier,
    onContinue: (() -> Unit)? = null,
) {
    val dark = MaterialTheme.colorScheme.isDark
    val firstName = userName.trim().substringBefore(" ").ifBlank {
        userName.ifBlank { stringResource(R.string.label_welcome_back_fallback) }
    }.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    val infinite = rememberInfiniteTransition(label = "welcome_back")
    val glow by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(7000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow",
    )
    val glow2 by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow2",
    )
    val robotT by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "robot",
    )
    val hiT by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "hi",
    )
    val arrowT by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "arrow",
    )
    val shimmerT by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer",
    )
    val particleT by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "particle",
    )

    val robotY = -8f * robotT
    val robotRot = -6f + 12f * robotT
    val arrowX = when {
        arrowT < 0.25f -> 6f * (arrowT / 0.25f)
        arrowT < 0.5f -> 6f * (1f - (arrowT - 0.25f) / 0.25f)
        else -> 0f
    }

    // Matches hiStickerPop keyframes (2.6s)
    val hiScale = when {
        hiT < 0.18f -> 0.55f + (hiT / 0.18f) * 0.57f
        hiT < 0.32f -> 1.12f - ((hiT - 0.18f) / 0.14f) * 0.16f
        hiT < 0.48f -> 0.96f + ((hiT - 0.32f) / 0.16f) * 0.04f
        hiT < 0.72f -> 1f
        hiT < 0.88f -> 1f - ((hiT - 0.72f) / 0.16f) * 0.10f
        else -> 0.90f - ((hiT - 0.88f) / 0.12f) * 0.35f
    }
    val hiRot = when {
        hiT < 0.18f -> -12f + (hiT / 0.18f) * 18f
        hiT < 0.32f -> 6f - ((hiT - 0.18f) / 0.14f) * 8f
        hiT < 0.48f -> -2f + ((hiT - 0.32f) / 0.16f) * 2f
        hiT < 0.72f -> 0f
        hiT < 0.88f -> -((hiT - 0.72f) / 0.16f) * 4f
        else -> -4f - ((hiT - 0.88f) / 0.12f) * 8f
    }
    val hiY = when {
        hiT < 0.18f -> 8f - (hiT / 0.18f) * 12f
        hiT < 0.32f -> -4f + ((hiT - 0.18f) / 0.14f) * 4f
        hiT < 0.72f -> -2f
        hiT < 0.88f -> -2f + ((hiT - 0.72f) / 0.16f) * 6f
        else -> 4f + ((hiT - 0.88f) / 0.12f) * 4f
    }
    val hiAlpha = when {
        hiT < 0.18f -> (hiT / 0.18f).coerceIn(0f, 1f)
        hiT < 0.72f -> 1f
        hiT < 0.88f -> 1f - ((hiT - 0.72f) / 0.16f) * 0.6f
        else -> 0.4f * (1f - (hiT - 0.88f) / 0.12f)
    }

    val canvas = if (dark) DarkCanvas else LightCanvas
    val kicker = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val nameColor = if (dark) Color.White else Color(0xFF0F172A)
    val emailMuted = if (dark) Color(0xFF64748B) else Color(0xFF64748B)
    val emailFg = if (dark) Color(0xFF94A3B8) else Color(0xFF334155)
    val subColor = if (dark) Color(0xFF94A3B8) else Color(0xFF475569)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(canvas),
    ) {
        WelcomeOrbs(dark = dark, glow = glow, glow2 = glow2)
        WelcomeParticles(dark = dark, t = particleT)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .offset(y = (-28).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(contentAlignment = Alignment.TopCenter) {
                    HiSticker(
                        t = hiT,
                        scale = hiScale,
                        rotation = hiRot,
                        translationY = hiY,
                        alpha = hiAlpha,
                        sparkColor = if (dark) Color.White else Brand600,
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_welcome_robot),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 36.dp, bottom = 32.dp)
                            .size(88.dp)
                            .graphicsLayer {
                                translationY = robotY
                                rotationZ = robotRot
                            },
                    )
                    val robotSpark = if (dark) Brand400 else Brand600
                    HiSpark(
                        t = particleT,
                        delay = 0f,
                        color = robotSpark,
                        modifier = Modifier.align(Alignment.CenterEnd).offset(x = 10.dp, y = 8.dp),
                        size = 8.dp,
                    )
                    HiSpark(
                        t = particleT,
                        delay = 0.28f,
                        color = if (dark) Color.White else Indigo600,
                        modifier = Modifier.align(Alignment.CenterStart).offset(x = (-8).dp, y = 18.dp),
                        size = 6.dp,
                    )
                    HiSpark(
                        t = particleT,
                        delay = 0.5f,
                        color = robotSpark,
                        modifier = Modifier.align(Alignment.BottomCenter).offset(x = 18.dp, y = (-20).dp),
                        size = 5.dp,
                    )
                }

                Text(
                    text = stringResource(R.string.label_welcome_back_comma).uppercase(Locale.getDefault()),
                    color = kicker,
                    fontWeight = if (dark) FontWeight.Medium else FontWeight.SemiBold,
                    fontSize = 20.sp,
                    letterSpacing = 1.4.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = firstName,
                    color = nameColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 12.dp, end = 12.dp),
                )
                if (userEmail.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.label_signed_in_as, "").trim(),
                        color = MaterialTheme.colorScheme.accentInk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = userEmail,
                        color = emailFg,
                        fontWeight = if (dark) FontWeight.Normal else FontWeight.Medium,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                    )
                } else {
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    text = stringResource(R.string.msg_welcome_back_subtitle),
                    color = subColor,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 240.dp),
                )
            }

            if (onContinue != null) {
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .height(56.dp)
                        .graphicsLayer {
                            scaleX = if (pressed) 0.98f else 1f
                            scaleY = if (pressed) 0.98f else 1f
                        }
                        .shadow(
                            16.dp,
                            RoundedCornerShape(16.dp),
                            ambientColor = Brand600.copy(if (dark) 0.40f else 0.25f),
                            spotColor = Brand600.copy(if (dark) 0.40f else 0.25f),
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(brandGradient())
                        .clickable(interactionSource = interaction, indication = null, onClick = onContinue),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .graphicsLayer { translationX = (shimmerT * 2f - 1f) * 360f }
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(0.25f),
                                        Color.Transparent,
                                    ),
                                    start = Offset.Zero,
                                    end = Offset(280f, 0f),
                                ),
                            ),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.label_continue),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 0.4.sp,
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(18.dp)
                                .offset(x = arrowX.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HiSticker(
    t: Float,
    scale: Float,
    rotation: Float,
    translationY: Float,
    alpha: Float,
    sparkColor: Color,
) {
    val hi = stringResource(R.string.label_hi_sticker)
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
                this.translationY = translationY
                this.alpha = alpha
                transformOrigin = TransformOrigin(0.2f, 1f)
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Box(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(18.dp), ambientColor = Brand600.copy(0.45f), spotColor = Brand600.copy(0.45f))
                    .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 6.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Brand400, Brand600, Indigo400),
                            start = Offset.Zero,
                            end = Offset(220f, 180f),
                        ),
                    )
                    .padding(horizontal = 20.dp, vertical = 7.dp),
            ) {
                Row {
                    hi.forEachIndexed { i, c ->
                        val local = ((t - 0.08f * i).coerceIn(0f, 1f))
                        val letterAlpha = when {
                            local < 0.08f -> 0f
                            local < 0.22f -> ((local - 0.08f) / 0.14f).coerceIn(0f, 1f)
                            local < 0.78f -> 1f
                            else -> (1f - (local - 0.78f) / 0.22f).coerceIn(0f, 1f)
                        }
                        val letterY = when {
                            local < 0.22f -> 6f * (1f - local / 0.22f)
                            local < 0.78f -> 0f
                            else -> -4f * ((local - 0.78f) / 0.22f)
                        }
                        Text(
                            text = c.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            letterSpacing = 0.3.sp,
                            modifier = Modifier.graphicsLayer {
                                this.alpha = letterAlpha
                                this.translationY = letterY
                            },
                        )
                    }
                }
            }
            Canvas(
                Modifier
                    .padding(start = 10.dp)
                    .size(12.dp, 8.dp)
                    .offset(y = (-2).dp),
            ) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, Brand600)
            }
        }
        HiSpark(t = t, delay = 0.15f, color = sparkColor, modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-8).dp), size = 6.dp)
        HiSpark(t = t, delay = 0.35f, color = sparkColor, modifier = Modifier.align(Alignment.TopStart).offset(x = (-10).dp, y = 2.dp), size = 5.dp)
        HiSpark(t = t, delay = 0.50f, color = sparkColor, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-6).dp, y = 2.dp), size = 4.dp)
    }
}

@Composable
private fun HiSpark(
    t: Float,
    delay: Float,
    modifier: Modifier,
    size: androidx.compose.ui.unit.Dp,
    color: Color,
) {
    val p = ((t - delay).coerceIn(0f, 1f))
    val sparkAlpha = when {
        p < 0.20f -> 0f
        p < 0.40f -> (p - 0.20f) / 0.20f
        p < 0.70f -> 0.8f + 0.2f * (1f - (p - 0.40f) / 0.30f)
        else -> ((1f - p) / 0.30f).coerceIn(0f, 0.8f)
    }
    val sparkScale = when {
        p < 0.40f -> 0.4f + 0.6f * (p / 0.40f)
        p < 0.70f -> 1f - 0.15f * ((p - 0.40f) / 0.30f)
        else -> 0.85f - 0.55f * ((p - 0.70f) / 0.30f)
    }
    val sparkRot = when {
        p < 0.40f -> 20f * (p / 0.40f)
        p < 0.70f -> 20f - 30f * ((p - 0.40f) / 0.30f)
        else -> -10f + 50f * ((p - 0.70f) / 0.30f)
    }
    Canvas(
        modifier
            .size(size)
            .graphicsLayer {
                alpha = sparkAlpha
                scaleX = sparkScale
                scaleY = sparkScale
                rotationZ = sparkRot
            },
    ) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w / 2f, 0f)
            lineTo(w * 0.61f, h * 0.35f)
            lineTo(w * 0.98f, h * 0.35f)
            lineTo(w * 0.68f, h * 0.57f)
            lineTo(w * 0.79f, h * 0.91f)
            lineTo(w / 2f, h * 0.70f)
            lineTo(w * 0.21f, h * 0.91f)
            lineTo(w * 0.32f, h * 0.57f)
            lineTo(w * 0.02f, h * 0.35f)
            lineTo(w * 0.39f, h * 0.35f)
            close()
        }
        drawPath(path, color)
    }
}

@Composable
private fun BoxScope.WelcomeOrbs(dark: Boolean, glow: Float, glow2: Float) {
    val a = 0.5f + glow * 0.3f
    val b = 0.5f + glow2 * 0.3f
    val y1 = -glow * 20f
    val y2 = -glow2 * 20f
    BoxWithConstraints(Modifier.matchParentSize()) {
        GlowOrb(
            color = if (dark) Brand600.copy(0.30f) else Purple200.copy(0.80f),
            blur = if (dark) 70.dp else 60.dp,
            glowAlpha = a,
            glowScale = 1f + glow * 0.1f,
            modifier = Modifier.offset((-96).dp, (-96).dp + y1.dp).size(360.dp),
        )
        GlowOrb(
            color = if (dark) Indigo600.copy(0.25f) else Indigo200.copy(0.70f),
            blur = if (dark) 80.dp else 70.dp,
            glowAlpha = b,
            glowScale = 1f + glow2 * 0.1f,
            modifier = Modifier
                .offset(x = maxWidth - 400.dp + 96.dp, y = maxHeight * 0.5f + y2.dp)
                .size(400.dp),
        )
        GlowOrb(
            color = if (dark) Purple900.copy(0.30f) else Pink200.copy(0.60f),
            blur = if (dark) 60.dp else 50.dp,
            glowAlpha = a,
            glowScale = 1f + glow * 0.1f,
            modifier = Modifier
                .offset(x = (-80).dp, y = maxHeight - 320.dp + 80.dp + y1.dp)
                .size(320.dp),
        )
    }
}

@Composable
private fun GlowOrb(
    color: Color,
    blur: androidx.compose.ui.unit.Dp,
    glowAlpha: Float,
    glowScale: Float,
    modifier: Modifier,
) {
    Box(
        modifier
            .graphicsLayer {
                clip = true
                shape = CircleShape
                alpha = glowAlpha
                scaleX = glowScale
                scaleY = glowScale
            }
            .clip(CircleShape)
            .blur(blur)
            .background(
                Brush.radialGradient(
                    colors = listOf(color, color.copy(alpha = 0f)),
                ),
                CircleShape,
            ),
    )
}

@Composable
private fun BoxScope.WelcomeParticles(dark: Boolean, t: Float) {
    BoxWithConstraints(Modifier.matchParentSize()) {
        Particle(
            color = if (dark) Brand400 else Brand600,
            size = if (dark) 6.dp else 8.dp,
            x = 40.dp,
            bottom = 80.dp,
            t = t,
            delay = 0f,
            parentHeight = maxHeight,
        )
        Particle(
            color = if (dark) Indigo300 else Indigo400,
            size = 8.dp,
            x = maxWidth * 0.75f,
            bottom = 40.dp,
            t = t,
            delay = 2f / 6f,
            parentHeight = maxHeight,
        )
        Particle(
            color = Purple400,
            size = 4.dp,
            x = maxWidth * 0.5f,
            bottom = 128.dp,
            t = t,
            delay = 4f / 6f,
            parentHeight = maxHeight,
        )
    }
}

@Composable
private fun Particle(
    color: Color,
    size: androidx.compose.ui.unit.Dp,
    x: androidx.compose.ui.unit.Dp,
    bottom: androidx.compose.ui.unit.Dp,
    t: Float,
    delay: Float,
    parentHeight: androidx.compose.ui.unit.Dp,
) {
    val p = ((t + delay) % 1f)
    val alpha = if (p < 0.5f) (p / 0.5f) * 0.6f else ((1f - p) / 0.5f) * 0.6f
    Box(
        Modifier
            .offset(x = x, y = parentHeight - size - bottom - (p * 100).dp)
            .size(size)
            .graphicsLayer {
                rotationZ = p * 360f
                this.alpha = alpha
            }
            .clip(CircleShape)
            .background(color),
    )
}

@Preview(
    name = "Welcome Back - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420",
)
@Composable
private fun PreviewWelcomeBackPageDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        WelcomeBackPage(
            userName = "Manish",
            userEmail = "manish@example.com",
            onContinue = {},
        )
    }
}

@Preview(
    name = "Welcome Back - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420",
)
@Composable
private fun PreviewWelcomeBackPageLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        WelcomeBackPage(
            userName = "Manish",
            userEmail = "manish@example.com",
            onContinue = {},
        )
    }
}
