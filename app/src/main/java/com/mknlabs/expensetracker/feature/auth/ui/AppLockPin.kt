package com.mknlabs.expensetracker.feature.auth.ui

import androidx.activity.compose.BackHandler
import com.mknlabs.expensetracker.utils.AppLocaleHelper
import com.mknlabs.expensetracker.utils.localizeWesternDigits
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.appAmbientGlow
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.AppLockSecurityQuestion
import com.mknlabs.expensetracker.data.constants.appLockSecurityQuestions
import com.mknlabs.expensetracker.core.ui.theme.accentInkGradient
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.surfaceGradient
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.vector.ImageVector
import com.mknlabs.expensetracker.models.PinVisualMode
import com.mknlabs.expensetracker.models.PinSlotState
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.fill.Diamond
import com.adamglin.phosphoricons.fill.FlowerLotus
import com.adamglin.phosphoricons.fill.Heart
import com.adamglin.phosphoricons.fill.Leaf
import com.adamglin.phosphoricons.fill.PawPrint
import com.adamglin.phosphoricons.fill.RocketLaunch
import com.adamglin.phosphoricons.fill.Sparkle
import com.adamglin.phosphoricons.fill.Star
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun PinEntryContent(
    enteredPin: String,
    keypadLayout: List<List<String>>,
    keypadLayoutVersion: Int,
    keypadShakeOffsetPx: Float,
    mode: AppLockScreenMode,
    keypadEnabled: Boolean = true,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onForgotClick: () -> Unit,
    pinVisualMode: PinVisualMode,
    compact: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            PinSlot(
                isFilled = index < enteredPin.length,
                pinVisualMode = pinVisualMode
            )
        }
    }

    Spacer(modifier = Modifier.height(if (compact) 20.dp else 34.dp))

    Column(
        modifier = Modifier.graphicsLayer {
            translationX = keypadShakeOffsetPx
        },
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
    ) {
        AnimatedContent(
            targetState = keypadLayoutVersion,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 220)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = 140))
            },
            label = "app_lock_keypad_layout"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)) {
                keypadLayout.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)) {
                        row.forEach { key ->
                            AppLockKey(
                                key = key,
                                enabled = keypadEnabled && (key != APP_LOCK_FORGOT_KEY || mode == AppLockScreenMode.Unlock),
                                compact = compact,
                                onClick = {
                                    when (key) {
                                        APP_LOCK_DELETE_KEY -> onDeleteClick()
                                        APP_LOCK_FORGOT_KEY -> onForgotClick()
                                        else -> onDigitClick(key)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AppLockKey(
    key: String,
    enabled: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false
) {
    val isForgot = key == APP_LOCK_FORGOT_KEY
    val isDelete = key == APP_LOCK_DELETE_KEY
    val shape = RoundedCornerShape(if (compact) 28.dp else 38.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val keySize = if (compact) 62.dp else 84.dp

    Box(
        modifier = Modifier
            .size(width = keySize, height = keySize)
            .clip(shape)
            .background(surfaceGradient())
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            isDelete -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.desc_delete_pin_digit),
                    tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                    modifier = Modifier.size(if (compact) 26.dp else 34.dp)
                )
            }

            isForgot -> {
                Text(
                    text = stringResource(R.string.label_forgot),
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            else -> {
                AppLocaleHelper.appliedLanguageTags
                Text(
                    text = localizeWesternDigits(key, AppLocaleHelper.getCurrentLanguageTag()),
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = if (compact) 22.sp else 28.sp
                    )
                )
            }
        }
    }
}
@Composable
internal fun PinSlot(
    isFilled: Boolean,
    pinVisualMode: PinVisualMode,
    modifier: Modifier = Modifier
) {
    val iconPool = remember {
        listOf(
            PhosphorIcons.Fill.PawPrint,
            PhosphorIcons.Fill.Leaf,
            PhosphorIcons.Fill.Heart,
            PhosphorIcons.Fill.Star,
            PhosphorIcons.Fill.Diamond,
            PhosphorIcons.Fill.Sparkle,
            PhosphorIcons.Fill.RocketLaunch,
            PhosphorIcons.Fill.FlowerLotus
        )
    }

    var currentState by remember { mutableStateOf<PinSlotState>(PinSlotState.Empty) }
    // The brand as an ink, not a fill: this is the ramp the theme reserves for painting a
    // glyph, and it is the one that stays legible in dark. One brush is built here and
    // reused by both the animated icons and the dot they settle into, so the sequence
    // never ends on a different colour than it animated through.
    val brandInk = accentInkGradient()

    LaunchedEffect(isFilled, pinVisualMode) {
        if (!isFilled) {
            currentState = PinSlotState.Empty
        } else {
            if (currentState is PinSlotState.Empty) {
                if (pinVisualMode == PinVisualMode.PRO_ANIMATED) {
                    val randomIcon = iconPool.random()
                    currentState = PinSlotState.AnimatedIcon(randomIcon, System.nanoTime())
                    delay(400) // target duration 350-500ms
                    // Check if still filled (not deleted during delay)
                    if (currentState is PinSlotState.AnimatedIcon) {
                        currentState = PinSlotState.Dot
                    }
                } else {
                    currentState = PinSlotState.Dot
                }
            }
        }
    }

    Box(
        modifier = modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentState,
            transitionSpec = {
                val springSpec = spring<Float>(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )

                if (initialState is PinSlotState.Empty && targetState is PinSlotState.AnimatedIcon) {
                    (fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.7f, animationSpec = springSpec))
                        .togetherWith(fadeOut(animationSpec = tween(100)))
                } else if (initialState is PinSlotState.AnimatedIcon && targetState is PinSlotState.Dot) {
                    fadeIn(animationSpec = tween(200))
                        .togetherWith(fadeOut(animationSpec = tween(200)))
                } else if (targetState is PinSlotState.Empty) {
                    fadeIn(animationSpec = tween(50))
                        .togetherWith(fadeOut(animationSpec = tween(100)) + scaleOut(targetScale = 0.8f, animationSpec = tween(100)))
                } else {
                    fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                }
            },
            label = "PinSlotAnimation"
        ) { state ->
            when (state) {
                is PinSlotState.Empty -> {
                    Box(modifier = Modifier.size(18.dp))
                }
                is PinSlotState.AnimatedIcon -> {
                    // `tint` on an Icon takes a solid colour only, so the glyph is drawn
                    // white and the ramp is punched through its alpha with SrcIn, which
                    // keeps each icon's own silhouette. The offscreen layer fences the
                    // blend in so it cannot erase what was drawn around the slot.
                    Icon(
                        imageVector = state.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                            .drawWithContent {
                                drawContent()
                                drawRect(brush = brandInk, blendMode = BlendMode.SrcIn)
                            }
                    )
                }
                is PinSlotState.Dot -> {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(brush = brandInk)
                            .shadow(
                                elevation = 14.dp,
                                shape = CircleShape,
                                ambientColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.35f),
                                spotColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.35f)
                            )
                    )
                }
            }
        }
    }
}

internal suspend fun Animatable<Float, AnimationVector1D>.animateAppLockKeypadError() {
    val shakeOffsets = listOf(-10f, 10f, -6f, 6f, -3f, 3f, 0f)
    snapTo(0f)
    shakeOffsets.forEach { target ->
        animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 36)
        )
    }
}
