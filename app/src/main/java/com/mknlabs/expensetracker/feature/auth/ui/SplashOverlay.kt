package com.mknlabs.expensetracker.feature.auth.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.PurplePrimary
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashLogoSizePortrait = 132.dp
private val SplashLogoSizeLandscape = 72.dp

/** Underdamped spring: settles with overshoot so motion reads as mass + inertia. */
private val SplashInertiaSpring = spring<Float>(
    dampingRatio = 0.58f,
    stiffness = Spring.StiffnessLow,
)

@Composable
fun SplashOverlay(
    viewModel: SplashViewModel,
    onExitFinished: () -> Unit = {},
) {
    val currentTask by viewModel.currentTask.collectAsStateWithLifecycle()
    SplashOverlayContent(
        currentTask = currentTask,
        onExitFinished = onExitFinished,
    )
}

@Composable
private fun SplashOverlayContent(
    currentTask: InitTask,
    onExitFinished: () -> Unit = {},
) {
    val loadingProgress = remember { Animatable(0f) }
    val titleEnter = remember { Animatable(0f) }
    val subtitleEnter = remember { Animatable(0f) }
    val taglineEnter = remember { Animatable(0f) }
    val ashT = remember { Animatable(0f) }
    val titleText = stringResource(id = R.string.label_app_name_display)
    val subtitleText = stringResource(id = R.string.label_budget_and_spend)
    val taglineText = stringResource(id = R.string.label_splash_tagline)
    var displayedTask by remember { mutableStateOf<InitTask>(InitTask.Start) }

    val progressTween = tween<Float>(durationMillis = 600, easing = FastOutSlowInEasing)

    LaunchedEffect(titleText, subtitleText, taglineText) {
        // TEMP: replay enter + progress + ash for 1 minute.
        val deadline = System.currentTimeMillis() + 60_000L
        val ashSpec = tween<Float>(durationMillis = 1_150, easing = LinearEasing)
        while (System.currentTimeMillis() < deadline) {
            ashT.snapTo(0f)
            titleEnter.snapTo(0f)
            subtitleEnter.snapTo(0f)
            taglineEnter.snapTo(0f)
            loadingProgress.snapTo(0f)
            displayedTask = InitTask.Start
            coroutineScope {
                launch {
                    displayedTask = InitTask.Start
                    loadingProgress.animateTo(0.4f, progressTween)
                    displayedTask = InitTask.Syncing
                    loadingProgress.animateTo(0.8f, progressTween)
                    displayedTask = InitTask.Securing
                    loadingProgress.animateTo(1f, progressTween)
                    displayedTask = InitTask.Complete
                }
                launch {
                    titleEnter.animateTo(1f, SplashInertiaSpring)
                    delay(40)
                    coroutineScope {
                        launch { subtitleEnter.animateTo(1f, SplashInertiaSpring) }
                        launch { taglineEnter.animateTo(1f, SplashInertiaSpring) }
                    }
                }
            }
            delay(5)
            ashT.animateTo(1f, ashSpec)
        }
        onExitFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val logoSize = if (isLandscape) SplashLogoSizeLandscape else SplashLogoSizePortrait
    val glowMul = if (isLandscape) 1.45f else 2.2f
    val textOffsetY = if (isLandscape) {
        0.dp
    } else {
        -(configuration.screenHeightDp.dp * 0.10f)
    }

    val ash = ashT.value
    val contentAlpha = (1f - ash / 0.28f).coerceIn(0f, 1f)
    val nameInk = MaterialTheme.colorScheme.onBackground
    val subtitleInk = MaterialTheme.colorScheme.accentInk
    val taglineInk = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = contentAlpha }) {
        if (isLandscape) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SplashLogoMark(
                    logoSize = logoSize,
                    glowMul = glowMul,
                    pulseScale = pulseScale,
                    glowAlpha = glowAlpha,
                )
                Spacer(modifier = Modifier.height(2.dp))
                SplashCopyBlock(
                    titleText = titleText,
                    subtitleText = subtitleText,
                    taglineText = taglineText,
                    titleEnter = titleEnter.value,
                    subtitleEnter = subtitleEnter.value,
                    taglineEnter = taglineEnter.value,
                    compact = true,
                )
                Spacer(modifier = Modifier.height(12.dp))
                SplashProgressBlock(
                    progress = { loadingProgress.value },
                    displayedTask = displayedTask,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SplashLogoMark(
                    logoSize = logoSize,
                    glowMul = glowMul,
                    pulseScale = pulseScale,
                    glowAlpha = glowAlpha,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier.offset(y = textOffsetY),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SplashCopyBlock(
                        titleText = titleText,
                        subtitleText = subtitleText,
                        taglineText = taglineText,
                        titleEnter = titleEnter.value,
                        subtitleEnter = subtitleEnter.value,
                        taglineEnter = taglineEnter.value,
                        compact = false,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 64.dp)
                    .width(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SplashProgressBlock(
                    progress = { loadingProgress.value },
                    displayedTask = displayedTask,
                )
            }
        }

        Text(
            text = stringResource(id = R.string.label_splash_footer),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (isLandscape) 8.dp else 24.dp),
            textAlign = TextAlign.Center
        )
        }

        if (ash > 0f) {
            SplashAshField(
                progress = ash,
                nameInk = nameInk,
                subtitleInk = subtitleInk,
                taglineInk = taglineInk,
            )
        }
    }
}


private data class AshSpeck(
    val x0: Float,
    val y0: Float,
    val vx: Float,
    val vy: Float,
    val width: Float,
    val height: Float,
    val spin: Float,
    val start: Float,
    val lift: Float,
    val tone: Int,
)

@Composable
private fun SplashAshField(
    progress: Float,
    nameInk: Color,
    subtitleInk: Color,
    taglineInk: Color,
) {
    val specks = remember {
        val rng = Random(42)
        List(86) {
            AshSpeck(
                x0 = 0.38f + rng.nextFloat() * 0.24f,
                y0 = 0.28f + rng.nextFloat() * 0.28f,
                vx = (rng.nextFloat() - 0.5f) * 0.55f,
                vy = -0.12f - rng.nextFloat() * 0.55f,
                width = 1.6f + rng.nextFloat() * 5.5f,
                height = 1.2f + rng.nextFloat() * 3.2f,
                spin = (rng.nextFloat() - 0.5f) * 420f,
                start = rng.nextFloat() * 0.18f,
                lift = 0.7f + rng.nextFloat() * 0.6f,
                tone = rng.nextInt(3),
            )
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        specks.forEach { speck ->
            val local = ((progress - speck.start) / (1f - speck.start)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach
            val drift = local * speck.lift
            val x = (speck.x0 + speck.vx * drift) * size.width
            val y = (speck.y0 + speck.vy * drift) * size.height
            val alpha = (1f - local) * (1f - local) * 0.85f
            val degrees = speck.spin * local
            val ink = when (speck.tone) {
                0 -> nameInk
                1 -> subtitleInk
                else -> taglineInk
            }
            rotate(degrees = degrees, pivot = Offset(x, y)) {
                drawRoundRect(
                    color = ink.copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = Size(speck.width, speck.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(0.6f, 0.6f),
                )
            }
        }
    }
}

@Composable
private fun SplashLogoMark(
    logoSize: Dp,
    glowMul: Float,
    pulseScale: Float,
    glowAlpha: Float,
) {
    val brandPurple = PurplePrimary
    Box(
        modifier = Modifier
            .size(logoSize * glowMul)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
            .drawBehind {
                val radius = this.size.minDimension / 2.2f
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to Color.Transparent,
                        0.45f to brandPurple.copy(alpha = glowAlpha * 0.4f),
                        0.6f to brandPurple.copy(alpha = glowAlpha),
                        1.0f to Color.Transparent,
                        center = center,
                        radius = radius
                    ),
                    radius = radius
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = null,
            modifier = Modifier.size(logoSize)
        )
    }
}

@Composable
private fun SplashCopyBlock(
    titleText: String,
    subtitleText: String,
    taglineText: String,
    titleEnter: Float,
    subtitleEnter: Float,
    taglineEnter: Float,
    compact: Boolean,
) {
    val titleStyle = if (compact) {
        MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )
    } else {
        MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )
    }
    SplashFloatLine(
        text = titleText,
        progress = titleEnter,
        riseFromBelow = true,
        fromLeft = false,
        curvePx = if (compact) 18f else 36f,
        color = MaterialTheme.colorScheme.onBackground,
        style = titleStyle,
    )
    SplashFloatLine(
        text = subtitleText,
        progress = subtitleEnter,
        riseFromBelow = false,
        fromLeft = true,
        curvePx = 0f,
        color = MaterialTheme.colorScheme.accentInk,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    )
    Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))
    SplashFloatLine(
        text = taglineText,
        progress = taglineEnter,
        riseFromBelow = false,
        fromLeft = false,
        curvePx = 0f,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium.copy(
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Light
        )
    )
}

@Composable
private fun SplashProgressBlock(
    progress: () -> Float,
    displayedTask: InitTask,
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier
            .width(200.dp)
            .height(3.dp)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.accentInk,
        trackColor = MaterialTheme.colorScheme.accentSoft
    )
    Spacer(modifier = Modifier.height(12.dp))
    AnimatedContent(
        targetState = displayedTask,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "TaskAnimation"
    ) { task ->
        Text(
            text = stringResource(id = task.labelResId).lowercase(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SplashFloatLine(
    text: String,
    progress: Float,
    riseFromBelow: Boolean,
    fromLeft: Boolean,
    curvePx: Float,
    color: Color,
    style: TextStyle,
) {
    Text(
        text = text,
        style = style,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val settle = 1f - progress
                val dist = size.width
                val side = if (fromLeft) -1f else 1f
                translationX = if (riseFromBelow) {
                    side * curvePx * sin(settle.coerceAtLeast(0f) * PI.toFloat())
                } else {
                    settle * side * dist
                }
                translationY = if (riseFromBelow) settle * 96f else 0f
                alpha = progress.coerceIn(0f, 1f)
            }
    )
}

@Preview(showBackground = true)
@Composable
private fun SplashOverlayPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        SplashOverlayContent(currentTask = InitTask.Start)
    }
}
