package com.mknlabs.expensetracker.feature.auth.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
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

/**
 * Underdamped spring: settles with overshoot so motion reads as mass + inertia, but at
 * medium stiffness so the app name lands quickly instead of drifting into place.
 */
private val SplashInertiaSpring = spring<Float>(
    dampingRatio = 0.62f,
    stiffness = Spring.StiffnessMedium,
)

@Composable
fun SplashOverlay(
    viewModel: SplashViewModel,
    onExitFinished: () -> Unit = {},
) {
    val currentTask by viewModel.currentTask.collectAsStateWithLifecycle()
    SplashOverlayContent(
        currentTask = currentTask,
        // The platform splash screen is kept on screen for exactly `InitTask.Start`
        // (MainActivity#setKeepOnScreenCondition), and this overlay is composed behind it
        // from the very first frame. Hold the entrance until the task moves on, or the copy
        // animates - and finishes - where nobody can see it.
        startEntrance = currentTask !is InitTask.Start,
        onExitFinished = onExitFinished,
    )
}

@Composable
private fun SplashOverlayContent(
    currentTask: InitTask,
    startEntrance: Boolean = true,
    onExitFinished: () -> Unit = {},
) {
    val titleEnter = remember { Animatable(0f) }
    val subtitleEnter = remember { Animatable(0f) }
    val taglineEnter = remember { Animatable(0f) }
    val ashT = remember { Animatable(0f) }
    val titleText = stringResource(id = R.string.label_app_name_display)
    val subtitleText = stringResource(id = R.string.label_budget_and_spend)
    val taglineText = stringResource(id = R.string.label_splash_tagline)
    var enterDone by remember { mutableStateOf(false) }
    LaunchedEffect(titleText, subtitleText, taglineText, startEntrance) {
        if (!startEntrance) return@LaunchedEffect
        val slide = tween<Float>(durationMillis = 460, easing = FastOutSlowInEasing)
        titleEnter.animateTo(1f, SplashInertiaSpring)
        delay(160)
        coroutineScope {
            launch { subtitleEnter.animateTo(1f, slide) }
            launch { taglineEnter.animateTo(1f, slide) }
        }
        // The copy has landed - no hold here, so the ash can burn straight away.
        enterDone = true
    }

    // The ash is what clears this overlay, and it must not dissolve before the app content
    // has composed. `isReady` is what puts MainScreen into the composition, and
    // SplashViewModel flips it while the platform splash still covers the window - before
    // Syncing, and so before the Complete this waits on - which is what makes the screen
    // behind this overlay a composed dashboard rather than an empty themed window.
    LaunchedEffect(currentTask, enterDone) {
        if (enterDone && currentTask is InitTask.Complete) {
            ashT.animateTo(
                1f,
                tween(durationMillis = 1_150, easing = LinearEasing),
            )
            onExitFinished()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val logoSize = if (isLandscape) SplashLogoSizeLandscape else SplashLogoSizePortrait
    val textOffsetY = if (isLandscape) {
        configuration.screenHeightDp.dp * 0.04f
    } else {
        configuration.screenHeightDp.dp * 0.08f
    }

    // `ashT` is deliberately read inside the `graphicsLayer`/draw blocks below instead of
    // here. A read in the composable body subscribes this whole overlay - the logo bitmap,
    // the copy, the footer - to the ash, recomposing all of it on each of the animation's
    // ~70 frames. Read inside those blocks the state is observed in the layer/draw phase, so
    // a frame only updates a layer alpha and redraws the specks.
    val nameInk = MaterialTheme.colorScheme.onBackground
    val subtitleInk = MaterialTheme.colorScheme.accentInk
    val taglineInk = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = (1f - ((ashT.value - 0.55f) / 0.45f)).coerceIn(0f, 1f)
            }
            .appScreenBackground()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = (1f - ashT.value / 0.28f).coerceIn(0f, 1f) }
        ) {
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
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                        compact = true,
                    )
                }
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
                )
                Spacer(modifier = Modifier.height(16.dp))
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

        // Always composed, and handed a lambda rather than a value: the ash then updates in
        // the draw phase alone instead of recomposing this function every frame. It draws
        // nothing until the ash actually starts.
        SplashAshField(
            progress = { ashT.value },
            nameInk = nameInk,
            subtitleInk = subtitleInk,
            taglineInk = taglineInk,
        )
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
    progress: () -> Float,
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
                width = 3.2f + rng.nextFloat() * 8.5f,
                height = 2.4f + rng.nextFloat() * 5.0f,
                spin = (rng.nextFloat() - 0.5f) * 420f,
                start = rng.nextFloat() * 0.18f,
                lift = 0.7f + rng.nextFloat() * 0.6f,
                tone = rng.nextInt(3),
            )
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val animated = progress()
        if (animated <= 0f) return@Canvas
        specks.forEach { speck ->
            val local = ((animated - speck.start) / (1f - speck.start)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach
            // Cull before doing any work: a speck that has already faded out, or that has
            // flown off the top of the frame, is skipped rather than issued as one of ~86
            // rotated draw calls for that frame.
            val alpha = (1f - local) * (1f - local) * 0.85f
            if (alpha <= 0.01f) return@forEach
            val drift = local * speck.lift
            val x = (speck.x0 + speck.vx * drift) * size.width
            val y = (speck.y0 + speck.vy * drift) * size.height
            if (y + speck.height < 0f) return@forEach
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
) {
    Image(
        painter = painterResource(id = R.drawable.splash_logo),
        contentDescription = null,
        modifier = Modifier.size(logoSize)
    )
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
    val slidePx = with(androidx.compose.ui.platform.LocalDensity.current) { 96.dp.toPx() }
    SplashFloatLine(
        text = titleText,
        progress = titleEnter,
        riseFromBelow = true,
        fromLeft = false,
        curvePx = if (compact) 18f else 36f,
        travelPx = slidePx,
        color = MaterialTheme.colorScheme.onBackground,
        style = titleStyle,
    )
    SplashFloatLine(
        text = subtitleText,
        progress = subtitleEnter,
        riseFromBelow = false,
        fromLeft = true,
        curvePx = 0f,
        travelPx = slidePx,
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
        travelPx = slidePx,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium.copy(
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Light
        )
    )
}

@Composable
private fun SplashFloatLine(
    text: String,
    progress: Float,
    riseFromBelow: Boolean,
    fromLeft: Boolean,
    curvePx: Float,
    travelPx: Float,
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
                val side = if (fromLeft) -1f else 1f
                translationX = if (riseFromBelow) {
                    side * curvePx * sin(settle.coerceAtLeast(0f) * PI.toFloat())
                } else {
                    settle * side * travelPx
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
