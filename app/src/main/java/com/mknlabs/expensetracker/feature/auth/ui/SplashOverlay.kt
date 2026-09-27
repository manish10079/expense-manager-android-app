package com.mknlabs.expensetracker.feature.auth.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.PurplePrimary
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import kotlinx.coroutines.delay

private val SplashLogoSize = 132.dp
private val UpperAlphabet = ('A'..'Z').toList()
private val LowerAlphabet = ('a'..'z').toList()

@Composable
fun SplashOverlay(viewModel: SplashViewModel) {
    val currentTask by viewModel.currentTask.collectAsStateWithLifecycle()
    SplashOverlayContent(currentTask = currentTask)
}

@Composable
private fun SplashOverlayContent(currentTask: InitTask) {
    val loadingProgress = remember { Animatable(0f) }
    val logoEnter = remember { Animatable(0f) }
    val subtitleEnter = remember { Animatable(0f) }
    val taglineEnter = remember { Animatable(0f) }
    val titleText = stringResource(id = R.string.label_app_name_display)
    val subtitleText = stringResource(id = R.string.label_budget_and_spend)
    val taglineText = stringResource(id = R.string.label_splash_tagline)
    var titleShown by remember { mutableStateOf("") }
    var logoTopInRoot by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentTask) {
        loadingProgress.animateTo(
            targetValue = currentTask.progress / 100f,
            animationSpec = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(titleText, subtitleText, taglineText) {
        val slideSpec = tween<Float>(durationMillis = 380, easing = FastOutSlowInEasing)
        logoEnter.animateTo(
            1f,
            tween(durationMillis = 720, easing = FastOutSlowInEasing)
        )
        countdownReveal(titleText) { titleShown = it }
        subtitleEnter.animateTo(1f, slideSpec)
        taglineEnter.animateTo(1f, slideSpec)
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

    val p = logoEnter.value
    val grow = 0.22f + 0.78f * p
    val landedPulse = 1f + (pulseScale - 1f) * p

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val brandPurple = PurplePrimary
            Box(
                modifier = Modifier
                    .size(SplashLogoSize * 2.2f)
                    .onGloballyPositioned { coords ->
                        logoTopInRoot = coords.positionInRoot().y
                    }
                    .graphicsLayer {
                        translationY = (1f - p) * -logoTopInRoot
                        scaleX = grow * landedPulse
                        scaleY = grow * landedPulse
                    }
                    .drawBehind {
                        val radius = this.size.minDimension / 2.2f
                        drawCircle(
                            brush = Brush.radialGradient(
                                0.0f to Color.Transparent,
                                0.45f to brandPurple.copy(alpha = glowAlpha * 0.4f * p),
                                0.6f to brandPurple.copy(alpha = glowAlpha * p),
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
                    modifier = Modifier.size(SplashLogoSize)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            SplashCountdownLine(
                fullText = titleText,
                shown = titleShown,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
            )

            SplashSlideLine(
                text = subtitleText,
                progress = subtitleEnter.value,
                fromLeft = true,
                color = MaterialTheme.colorScheme.accentInk,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            SplashSlideLine(
                text = taglineText,
                progress = taglineEnter.value,
                fromLeft = false,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Light
                )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 64.dp)
                .width(200.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { loadingProgress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.accentInk,
                trackColor = MaterialTheme.colorScheme.accentSoft
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = currentTask,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "TaskAnimation"
            ) { task ->
                if (task !is InitTask.Complete) {
                    Text(
                        text = stringResource(id = task.labelResId).lowercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        Text(
            text = stringResource(id = R.string.label_splash_footer),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.accentInk.copy(alpha = 1f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SplashCountdownLine(
    fullText: String,
    shown: String,
    color: Color,
    style: TextStyle
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = fullText,
            style = style,
            color = Color.Transparent,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = shown,
            style = style,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SplashSlideLine(
    text: String,
    progress: Float,
    fromLeft: Boolean,
    color: Color,
    style: TextStyle
) {
    val p = progress.coerceIn(0f, 1f)
    Text(
        text = text,
        style = style,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val dist = size.width
                translationX = (1f - p) * if (fromLeft) -dist else dist
                alpha = p
            }
    )
}

private suspend fun countdownReveal(
    text: String,
    onFrame: (String) -> Unit
) {
    val out = CharArray(text.length) { i -> if (text[i].isWhitespace()) text[i] else ' ' }
    onFrame(String(out))
    for (i in text.indices) {
        val target = text[i]
        if (target.isWhitespace()) continue
        val pool = when {
            target.isUpperCase() -> UpperAlphabet
            target.isLowerCase() -> LowerAlphabet
            else -> listOf(target)
        }
        if (pool.size > 1) {
            repeat(3) {
                out[i] = pool.random()
                onFrame(String(out))
                delay(16)
            }
        }
        out[i] = target
        onFrame(String(out))
        delay(18)
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashOverlayPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        SplashOverlayContent(currentTask = InitTask.Start)
    }
}
