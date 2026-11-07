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
internal fun SetupSecurityQuestionContent(
    selectedQuestionId: String,
    answer: String,
    onQuestionSelected: (String) -> Unit,
    onAnswerChange: (String) -> Unit,
    onDone: () -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
    compact: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
    ) {
        if (compact) {
            // Landscape/short heights: stack the questions in two columns and
            // let each prompt's font auto-shrink so every card stays readable.
            appLockSecurityQuestions.chunked(2).forEach { rowQuestions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowQuestions.forEach { question ->
                        SecurityQuestionCard(
                            modifier = Modifier.weight(1f),
                            prompt = stringResource(question.promptResId),
                            isSelected = question.id == selectedQuestionId,
                            onClick = { onQuestionSelected(question.id) },
                            compact = true
                        )
                    }
                    // Keep the columns even when the last row has one question.
                    if (rowQuestions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            appLockSecurityQuestions.forEach { question ->
                SecurityQuestionCard(
                    prompt = stringResource(question.promptResId),
                    isSelected = question.id == selectedQuestionId,
                    onClick = { onQuestionSelected(question.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(if (compact) 2.dp else 6.dp))

        AppLockAnswerField(
            value = answer,
            label = stringResource(R.string.label_your_answer),
            placeholder = stringResource(R.string.placeholder_type_recovery_answer),
            onValueChange = onAnswerChange,
            onDone = onDone,
            onFocusChanged = onFocusChanged
        )
    }
}

@Composable
internal fun RecoveryQuestionContent(
    questions: List<AppLockSecurityQuestion>,
    selectedQuestionId: String?,
    onQuestionSelected: (String) -> Unit,
    answer: String,
    onAnswerChange: (String) -> Unit,
    onDone: () -> Unit,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Text(
                    text = stringResource(R.string.msg_no_recovery_question_saved),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        } else {
            // Present the real question shuffled among randomly chosen decoys. The
            // user must pick the one they saved; the selection is verified locally
            // together with the answer.
            questions.forEach { question ->
                SecurityQuestionCard(
                    prompt = stringResource(question.promptResId),
                    isSelected = question.id == selectedQuestionId,
                    onClick = { onQuestionSelected(question.id) }
                )
            }
        }

        AppLockAnswerField(
            value = answer,
            label = stringResource(R.string.label_security_answer),
            placeholder = stringResource(R.string.placeholder_enter_your_answer),
            onValueChange = onAnswerChange,
            onDone = onDone,
            onFocusChanged = onFocusChanged
        )
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
internal fun SecurityQuestionCard(
    prompt: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (compact) 18.dp else 24.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.accentInk.copy(alpha = 0.22f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (compact) 12.dp else 18.dp,
                vertical = if (compact) 10.dp else 16.dp
            )
    ) {
        // Resolved in composable context (the ColorProducer lambda below may
        // not read CompositionLocals directly).
        val promptColor = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        if (compact) {
            // Auto-shrink the prompt (down to 9sp) so it fits the narrower
            // column on up to two lines instead of clipping.
            BasicText(
                text = prompt,
                color = { promptColor },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 9.sp,
                    maxFontSize = 16.sp
                )
            )
        } else {
            Text(
                text = prompt,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
internal fun AppLockAnswerField(
    value: String,
    label: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onDone: (() -> Unit)? = null,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { onFocusChanged(it.isFocused) },
        singleLine = true,
        shape = AppOutlinedFieldDefaults.shape,
        label = {
            Text(text = label)
        },
        placeholder = {
            Text(text = placeholder)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onDone?.invoke()
                keyboardController?.hide()
            }
        ),
        colors = AppOutlinedFieldDefaults.colors()
    )
}

