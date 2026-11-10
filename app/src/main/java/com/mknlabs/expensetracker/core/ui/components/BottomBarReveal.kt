package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mknlabs.expensetracker.models.BottomBarRevealMotion

private const val RevealMillis = 320
private const val ExpandMillis = 420

private val bounceOffset = spring<IntOffset>(
    dampingRatio = 0.58f,
    stiffness = Spring.StiffnessMediumLow,
)
private val bounceSize = spring<IntSize>(
    dampingRatio = 0.58f,
    stiffness = Spring.StiffnessMediumLow,
)
private val bounceFloat = spring<Float>(
    dampingRatio = 0.58f,
    stiffness = Spring.StiffnessMediumLow,
)

fun bottomBarEnter(motion: BottomBarRevealMotion): EnterTransition = when (motion) {
    BottomBarRevealMotion.SlideUp ->
        slideInVertically(
            animationSpec = bounceOffset,
            initialOffsetY = { it },
        ) + fadeIn(tween(RevealMillis))
    BottomBarRevealMotion.ExpandFromCenter ->
        expandHorizontally(
            animationSpec = bounceSize,
            expandFrom = Alignment.CenterHorizontally,
            clip = false,
        ) + fadeIn(tween(280)) + scaleIn(
            animationSpec = bounceFloat,
            initialScale = 0.18f,
        )
}

fun bottomBarExit(motion: BottomBarRevealMotion): ExitTransition = when (motion) {
    BottomBarRevealMotion.SlideUp ->
        slideOutVertically(
            animationSpec = tween(RevealMillis, easing = FastOutLinearInEasing),
            targetOffsetY = { it },
        ) + fadeOut(tween(RevealMillis))
    BottomBarRevealMotion.ExpandFromCenter ->
        shrinkHorizontally(
            animationSpec = tween(ExpandMillis, easing = FastOutSlowInEasing),
            shrinkTowards = Alignment.CenterHorizontally,
            clip = true,
        ) + fadeOut(tween(ExpandMillis, easing = FastOutSlowInEasing)) + scaleOut(
            animationSpec = tween(ExpandMillis, easing = FastOutSlowInEasing),
            targetScale = 0.18f,
        )
}

fun bottomBarHandleEnter(motion: BottomBarRevealMotion): EnterTransition = when (motion) {
    BottomBarRevealMotion.SlideUp ->
        slideInVertically(
            animationSpec = tween(
                RevealMillis,
                delayMillis = RevealMillis / 2,
                easing = FastOutSlowInEasing,
            ),
            initialOffsetY = { it },
        ) + fadeIn(tween(RevealMillis, delayMillis = RevealMillis / 2))
    BottomBarRevealMotion.ExpandFromCenter ->
        fadeIn(tween(ExpandMillis, delayMillis = ExpandMillis / 3, easing = FastOutSlowInEasing)) +
            scaleIn(
                animationSpec = tween(
                    ExpandMillis,
                    delayMillis = ExpandMillis / 3,
                    easing = FastOutSlowInEasing,
                ),
                initialScale = 0.4f,
            )
}

fun bottomBarHandleExit(motion: BottomBarRevealMotion): ExitTransition = when (motion) {
    BottomBarRevealMotion.SlideUp ->
        slideOutVertically(
            animationSpec = tween(RevealMillis, easing = FastOutLinearInEasing),
            targetOffsetY = { it },
        ) + fadeOut(tween(RevealMillis))
    BottomBarRevealMotion.ExpandFromCenter ->
        fadeOut(tween(ExpandMillis, easing = FastOutSlowInEasing)) + scaleOut(
            animationSpec = tween(ExpandMillis, easing = FastOutSlowInEasing),
            targetScale = 0.4f,
        )
}
