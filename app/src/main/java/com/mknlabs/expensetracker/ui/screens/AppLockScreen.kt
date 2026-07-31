package com.mknlabs.expensetracker.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.appLockSecurityQuestions
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.surfaceGradient
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.vector.ImageVector
import com.mknlabs.expensetracker.models.PinVisualMode
import com.mknlabs.expensetracker.models.PinSlotState
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Spa
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppLockScreenMode {
    Setup,
    Unlock
}

private enum class PinSetupStage {
    Create,
    Confirm,
    SecurityQuestion
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppLockScreen(
    mode: AppLockScreenMode,
    biometricEnabled: Boolean = false,
    scrambledPinKeypadEnabled: Boolean = false,
    isBiometricAvailable: Boolean = false,
    securityQuestionPrompt: Int? = null,
    onBackClick: (() -> Unit)? = null,
    onBiometricClick: (() -> Unit)? = null,
    onSetupComplete: (String, String, String) -> Unit = { _, _, _ -> },
    onUnlockSuccess: () -> Unit = {},
    onForgotPinRecovery: () -> Unit = {},
    validateUnlockPin: (String) -> Boolean = { false },
    validateSecurityAnswer: (String) -> Boolean = { false },
    pinVisualMode: PinVisualMode = PinVisualMode.NORMAL
) {
    var enteredPin by rememberSaveable(mode) { mutableStateOf("") }
    var firstPin by rememberSaveable(mode) { mutableStateOf("") }
    var setupStage by rememberSaveable(mode) { mutableStateOf(PinSetupStage.Create) }
    var message by rememberSaveable(mode) { mutableStateOf<String?>(null) }
    var failedUnlockAttempts by rememberSaveable(mode) { mutableStateOf(0) }
    var selectedSecurityQuestionId by rememberSaveable(mode) {
        mutableStateOf(appLockSecurityQuestions.first().id)
    }
    var securityAnswer by rememberSaveable(mode) { mutableStateOf("") }
    var isRecoveryMode by rememberSaveable(mode) { mutableStateOf(false) }
    var recoveryAnswer by rememberSaveable(mode) { mutableStateOf("") }
    var keypadLayout by remember(mode, scrambledPinKeypadEnabled) {
        mutableStateOf(
            buildAppLockKeypadLayout(
                scrambled = mode == AppLockScreenMode.Unlock && scrambledPinKeypadEnabled
            )
        )
    }
    var keypadLayoutVersion by remember(mode, scrambledPinKeypadEnabled) {
        mutableIntStateOf(0)
    }
    val keypadShakeOffsetPx = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val isPinEntryVisible = !isRecoveryMode && !(mode == AppLockScreenMode.Setup && setupStage == PinSetupStage.SecurityQuestion)
    fun refreshKeypadLayout() {
        keypadLayout = buildAppLockKeypadLayout(
            scrambled = mode == AppLockScreenMode.Unlock &&
                scrambledPinKeypadEnabled &&
                !isRecoveryMode
        )
        keypadLayoutVersion += 1
    }
    val recoveryQuestionNotFoundMsg = stringResource(R.string.msg_recovery_question_is_not_confi)
    val triggerForgotRecovery: () -> Unit = {
        failedUnlockAttempts = 0
        enteredPin = ""
        if (mode == AppLockScreenMode.Unlock && securityQuestionPrompt != null) {
            isRecoveryMode = true
            recoveryAnswer = ""
            message = null
        } else if (mode == AppLockScreenMode.Unlock) {
            message = recoveryQuestionNotFoundMsg
        }
    }

    LaunchedEffect(mode, isRecoveryMode, scrambledPinKeypadEnabled) {
        refreshKeypadLayout()
        keypadShakeOffsetPx.snapTo(0f)
    }

    val confirmPinMsg = stringResource(R.string.msg_enter_the_same_pin_once_more_t)
    val chooseQuestionMsg = stringResource(R.string.msg_choose_one_security_question_f)
    val pinMismatchMsg = stringResource(R.string.msg_pins_did_not_match_start_again)
    val incorrectPinMsg = stringResource(R.string.msg_incorrect_pin_try_again)

    LaunchedEffect(enteredPin, mode, setupStage, isRecoveryMode) {
        if (!isPinEntryVisible || enteredPin.length != 4) return@LaunchedEffect


        when (mode) {
            AppLockScreenMode.Setup -> {
                if (setupStage == PinSetupStage.Create) {
                    firstPin = enteredPin
                    enteredPin = ""
                    setupStage = PinSetupStage.Confirm
                    message = confirmPinMsg
                } else if (enteredPin == firstPin) {
                    enteredPin = ""
                    setupStage = PinSetupStage.SecurityQuestion
                    message = chooseQuestionMsg
                } else {
                    firstPin = ""
                    enteredPin = ""
                    setupStage = PinSetupStage.Create
                    message = pinMismatchMsg
                }
            }

            AppLockScreenMode.Unlock -> {
                val isValid = withContext(Dispatchers.Default) {
                    validateUnlockPin(enteredPin)
                }
                if (isValid) {
                    failedUnlockAttempts = 0
                    onUnlockSuccess()
                } else {
                    val nextFailedAttemptCount = failedUnlockAttempts + 1
                    failedUnlockAttempts = nextFailedAttemptCount
                    if (nextFailedAttemptCount >= 3) {
                        triggerForgotRecovery()
                    } else {
                        enteredPin = ""
                        message = incorrectPinMsg
                        if (scrambledPinKeypadEnabled) {
                            refreshKeypadLayout()
                            coroutineScope.launch {
                                keypadShakeOffsetPx.animateAppLockKeypadError()
                            }
                        }
                    }
                }
            }
        }
    }

    val repeatDigitsMsg = stringResource(R.string.msg_repeat_the_same_4_digits_to_fi)
    val headerAction: (() -> Unit)? = when {
        isRecoveryMode -> {
            {
                isRecoveryMode = false
                recoveryAnswer = ""
                message = null
            }
        }

        mode == AppLockScreenMode.Setup && setupStage == PinSetupStage.SecurityQuestion -> {
            {
                setupStage = PinSetupStage.Confirm
                securityAnswer = ""
                message = repeatDigitsMsg
            }
        }

        else -> onBackClick
    }

    val title = when {
        isRecoveryMode -> stringResource(R.string.title_recover_access)
        mode == AppLockScreenMode.Unlock -> ""
        else -> stringResource(R.string.title_app_lock)
    }
    val eyebrow = when {
        isRecoveryMode -> stringResource(R.string.label_verify_security_question)
        mode == AppLockScreenMode.Unlock -> if (biometricEnabled) {
            stringResource(R.string.label_biometric_security_active)
        } else {
            stringResource(R.string.label_pin_security_active)
        }

        setupStage == PinSetupStage.Create -> stringResource(R.string.label_create_4digit_pin)
        setupStage == PinSetupStage.Confirm -> stringResource(R.string.label_confirm_your_pin)
        else -> ""
    }
    val headline = when {
        isRecoveryMode -> stringResource(R.string.label_forgot_your_pin)
        mode == AppLockScreenMode.Unlock -> stringResource(R.string.label_welcome_back)
        setupStage == PinSetupStage.Create -> ""
        setupStage == PinSetupStage.Confirm -> stringResource(R.string.label_confirm_pin)
        else -> stringResource(R.string.label_security_question)
    }
    val supportText = message ?: when {
        isRecoveryMode -> securityQuestionPrompt?.let {
            stringResource(R.string.msg_answer_saved_security_question)
        } ?: stringResource(R.string.msg_recovery_question_is_not_confi)

        mode == AppLockScreenMode.Unlock -> if (biometricEnabled && isBiometricAvailable) {
            stringResource(R.string.msg_biometric_or_pin_to_continue)
        } else {
            stringResource(R.string.msg_enter_pin_to_continue)
        }

        setupStage == PinSetupStage.Create -> ""
        setupStage == PinSetupStage.Confirm -> repeatDigitsMsg
        else -> stringResource(R.string.msg_choose_question_remember)
    }
    val primaryActionLabel = if (isRecoveryMode) stringResource(R.string.label_disable_app_lock) else stringResource(R.string.label_save)
    val isDarkPalette = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val isKeyboardOpen = WindowInsets.isImeVisible
    val recoveryAnswerPlaceholder = stringResource(R.string.placeholder_type_recovery_answer)
    val enterAnswerMsg = stringResource(R.string.msg_enter_an_answer_for_your_secur)
    val incorrectAnswerMsg = stringResource(R.string.msg_incorrect_answer_try_again)
    val enterAnswerToDisableMsg = stringResource(R.string.msg_enter_your_answer_to_disable_a)
    val onPrimaryActionClick: () -> Unit = {
        if (isRecoveryMode) {
            if (securityQuestionPrompt == null) {
                message = recoveryQuestionNotFoundMsg
            } else if (recoveryAnswer.isBlank()) {
                message = enterAnswerToDisableMsg
            } else if (validateSecurityAnswer(recoveryAnswer)) {
                onForgotPinRecovery()
            } else {
                recoveryAnswer = ""
                message = incorrectAnswerMsg
            }
        } else {
            if (securityAnswer.isBlank()) {
                message = enterAnswerMsg
            } else {
                onSetupComplete(firstPin, selectedSecurityQuestionId, securityAnswer)
            }
        }
    }

    BackHandler(enabled = mode == AppLockScreenMode.Unlock || headerAction != null) {
        headerAction?.invoke()
    }

    AppLockScreenContent(
        mode = mode,
        headerAction = headerAction,
        title = title,
        eyebrow = eyebrow,
        headline = headline,
        supportText = supportText,
        supportTextIsError = message != null,
        isPinEntryVisible = isPinEntryVisible,
        isSetupSecurityQuestion = mode == AppLockScreenMode.Setup && setupStage == PinSetupStage.SecurityQuestion,
        isRecoveryMode = isRecoveryMode,
        isKeyboardOpen = isKeyboardOpen,
        isDarkPalette = isDarkPalette,
        primaryActionLabel = primaryActionLabel,
        onPrimaryActionClick = onPrimaryActionClick,
        enteredPin = enteredPin,
        keypadLayout = keypadLayout,
        keypadLayoutVersion = keypadLayoutVersion,
        keypadShakeOffsetPx = if (scrambledPinKeypadEnabled) keypadShakeOffsetPx.value else 0f,
        pinVisualMode = pinVisualMode,
        onDigitClick = { digit ->
            if (enteredPin.length < 4) {
                enteredPin += digit
            }
        },
        onDeleteClick = {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
            }
        },
        onForgotClick = {
            triggerForgotRecovery()
        },
        biometricEnabled = biometricEnabled,
        isBiometricAvailable = isBiometricAvailable,
        onBiometricClick = onBiometricClick,
        selectedSecurityQuestionId = selectedSecurityQuestionId,
        securityAnswer = securityAnswer,
        onQuestionSelected = {
            selectedSecurityQuestionId = it
            message = null
        },
        onSecurityAnswerChange = {
            securityAnswer = it
            message = null
        },
        securityQuestionPromptText = securityQuestionPrompt?.let { stringResource(it) },
        recoveryAnswer = recoveryAnswer,
        onRecoveryAnswerChange = {
            recoveryAnswer = it
            message = null
        }
    )
}

@Composable
private fun AppLockScreenContent(
    mode: AppLockScreenMode,
    headerAction: (() -> Unit)?,
    title: String,
    eyebrow: String,
    headline: String,
    supportText: String,
    supportTextIsError: Boolean,
    isPinEntryVisible: Boolean,
    isSetupSecurityQuestion: Boolean,
    isRecoveryMode: Boolean,
    isKeyboardOpen: Boolean,
    isDarkPalette: Boolean,
    primaryActionLabel: String,
    onPrimaryActionClick: () -> Unit,
    enteredPin: String,
    keypadLayout: List<List<String>>,
    keypadLayoutVersion: Int,
    keypadShakeOffsetPx: Float,
    pinVisualMode: PinVisualMode,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onForgotClick: () -> Unit,
    biometricEnabled: Boolean,
    isBiometricAvailable: Boolean,
    onBiometricClick: (() -> Unit)?,
    selectedSecurityQuestionId: String,
    securityAnswer: String,
    onQuestionSelected: (String) -> Unit,
    onSecurityAnswerChange: (String) -> Unit,
    securityQuestionPromptText: String?,
    recoveryAnswer: String,
    onRecoveryAnswerChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Consume clicks to prevent interaction with layers below
            )
    ) {
        // Base background to ensure opacity
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )

        // Premium theme-aware card gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(standardCardGradient())
        )

        // Subtle radial glow for depth (Hidden in recovery/disable mode)
        if (!isRecoveryMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (isDarkPalette) 0.12f else 0.06f
                                ),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            if (headerAction != null) {
                if (isRecoveryMode) {
                    Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))
                }

                AppHeader(
                    title = title,
                    onBackClick = headerAction,
                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                )
            } else {
                Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                ,
                contentAlignment = if (isPinEntryVisible) {
                    Alignment.Center
                } else if (isKeyboardOpen) {
                    Alignment.BottomCenter
                } else {
                    Alignment.TopCenter
                }
            ) {
                if (!isRecoveryMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 300.dp, height = 240.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = if (isDarkPalette) 0.14f else 0.08f
                                        ),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(34.dp))
                        .background(
                            MaterialTheme.colorScheme.surface.copy(
                                alpha = if (isDarkPalette) 0.42f else 0.72f
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = if (isKeyboardOpen) 16.dp else 28.dp)
                        .then(
                            if (isPinEntryVisible) {
                                Modifier
                            } else {
                                Modifier.verticalScroll(rememberScrollState())
                            }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (eyebrow.isNotBlank()) {
                        Text(
                            text = eyebrow,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 4.sp,
                                fontSize = 12.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (headline.isNotBlank()) {
                        Text(
                            text = headline,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 34.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (supportText.isNotBlank()) {
                        Text(
                            text = supportText,
                            color = if (supportTextIsError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    when {
                        isRecoveryMode -> {
                            RecoveryQuestionContent(
                                questionPrompt = securityQuestionPromptText,
                                answer = recoveryAnswer,
                                onAnswerChange = onRecoveryAnswerChange,
                                onDone = onPrimaryActionClick
                            )
                        }

                        isSetupSecurityQuestion -> {
                            SetupSecurityQuestionContent(
                                selectedQuestionId = selectedSecurityQuestionId,
                                answer = securityAnswer,
                                onQuestionSelected = onQuestionSelected,
                                onAnswerChange = onSecurityAnswerChange,
                                onDone = onPrimaryActionClick
                            )
                        }

                        else -> {
                            PinEntryContent(
                                enteredPin = enteredPin,
                                keypadLayout = keypadLayout,
                                keypadLayoutVersion = keypadLayoutVersion,
                                keypadShakeOffsetPx = keypadShakeOffsetPx,
                                mode = mode,
                                onDigitClick = onDigitClick,
                                onDeleteClick = onDeleteClick,
                                onForgotClick = onForgotClick,
                                pinVisualMode = pinVisualMode
                            )

                            if (mode == AppLockScreenMode.Unlock && biometricEnabled && isBiometricAvailable && onBiometricClick != null) {
                                Spacer(modifier = Modifier.height(34.dp))

                                BiometricActionButton(
                                    onClick = onBiometricClick
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isKeyboardOpen) 0.dp else 22.dp))
                }
            }

            if (!isPinEntryVisible && !isKeyboardOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    PrimaryActionButton(
                        label = primaryActionLabel,
                        onClick = onPrimaryActionClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PinEntryContent(
    enteredPin: String,
    keypadLayout: List<List<String>>,
    keypadLayoutVersion: Int,
    keypadShakeOffsetPx: Float,
    mode: AppLockScreenMode,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onForgotClick: () -> Unit,
    pinVisualMode: PinVisualMode
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

    Spacer(modifier = Modifier.height(34.dp))

    Column(
        modifier = Modifier.graphicsLayer {
            translationX = keypadShakeOffsetPx
        },
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AnimatedContent(
            targetState = keypadLayoutVersion,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 220)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = 140))
            },
            label = "app_lock_keypad_layout"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                keypadLayout.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        row.forEach { key ->
                            AppLockKey(
                                key = key,
                                enabled = key != APP_LOCK_FORGOT_KEY || mode == AppLockScreenMode.Unlock,
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
private fun SetupSecurityQuestionContent(
    selectedQuestionId: String,
    answer: String,
    onQuestionSelected: (String) -> Unit,
    onAnswerChange: (String) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        appLockSecurityQuestions.forEach { question ->
            SecurityQuestionCard(
                prompt = stringResource(question.promptResId),
                isSelected = question.id == selectedQuestionId,
                onClick = { onQuestionSelected(question.id) }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        AppLockAnswerField(
            value = answer,
            label = stringResource(R.string.label_your_answer),
            placeholder = stringResource(R.string.placeholder_type_recovery_answer),
            onValueChange = onAnswerChange,
            onDone = onDone
        )
    }
}

@Composable
private fun RecoveryQuestionContent(
    questionPrompt: String?,
    answer: String,
    onAnswerChange: (String) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text = questionPrompt ?: stringResource(R.string.msg_no_recovery_question_saved),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        AppLockAnswerField(
            value = answer,
            label = stringResource(R.string.label_security_answer),
            placeholder = stringResource(R.string.placeholder_enter_your_answer),
            onValueChange = onAnswerChange,
            onDone = onDone
        )
    }
}

@Composable
private fun SecurityQuestionCard(
    prompt: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            text = prompt,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun AppLockAnswerField(
    value: String,
    label: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onDone: (() -> Unit)? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(22.dp),
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
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun PrimaryActionButton(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(999.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.26f)
            ),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = brandGradient(),
                    shape = RoundedCornerShape(999.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            )
        }
    }
}

@Composable
private fun BiometricActionButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(90.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Fingerprint,
            contentDescription = stringResource(R.string.desc_use_biometric),
            modifier = Modifier.size(100.dp)
        )
    }
}

@Composable
private fun AppLockKey(
    key: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val isForgot = key == APP_LOCK_FORGOT_KEY
    val isDelete = key == APP_LOCK_DELETE_KEY
    val shape = RoundedCornerShape(38.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(width = 84.dp, height = 84.dp)
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
                    modifier = Modifier.size(34.dp)
                )
            }

            isForgot -> {
                Text(
                    text = stringResource(R.string.label_forgot),
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        fontSize = 12.sp
                    )
                )
            }

            else -> {
                Text(
                    text = key,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 28.sp
                    )
                )
            }
        }
    }
}

//@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "Unlock Dark")
//@Composable
//private fun AppLockUnlockDarkPreview() {
//    ExpenseTrackerTheme(darkTheme = true) {
//        AppLockScreen(
//            mode = AppLockScreenMode.Unlock,
//            securityQuestionPrompt = "What was the name of your first school?"
//        )
//    }
//}

//@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "Unlock Light")
//@Composable
//private fun AppLockUnlockLightPreview() {
//    ExpenseTrackerTheme(darkTheme = false) {
//        AppLockScreen(
//            mode = AppLockScreenMode.Unlock,
//            securityQuestionPrompt = "What was the name of your first school?"
//        )
//    }
//}

@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "Biometric Unlock Dark")
@Composable
private fun AppLockBiometricDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        AppLockScreen(
            mode = AppLockScreenMode.Unlock,
            biometricEnabled = true,
            isBiometricAvailable = true,
            onBiometricClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "Biometric Unlock Dark")
@Composable
private fun AppLockBiometricLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        AppLockScreen(
            mode = AppLockScreenMode.Unlock,
            biometricEnabled = true,
            isBiometricAvailable = true,
            onBiometricClick = {}
        )
    }
}

//@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "Setup Dark")
//@Composable
//private fun AppLockSetupDarkPreview() {
//    ExpenseTrackerTheme(darkTheme = true) {
//        AppLockScreen(
//            mode = AppLockScreenMode.Setup,
//            onBackClick = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "Setup Light")
//@Composable
//private fun AppLockSetupLightPreview() {
//    ExpenseTrackerTheme(darkTheme = false) {
//        AppLockScreen(
//            mode = AppLockScreenMode.Setup,
//            onBackClick = {}
//        )
//    }
//}

private suspend fun Animatable<Float, AnimationVector1D>.animateAppLockKeypadError() {
    val shakeOffsets = listOf(-10f, 10f, -6f, 6f, -3f, 3f, 0f)
    snapTo(0f)
    shakeOffsets.forEach { target ->
        animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 36)
        )
    }
}

@Composable
private fun PinSlot(
    isFilled: Boolean,
    pinVisualMode: PinVisualMode,
    modifier: Modifier = Modifier
) {
    val iconPool = remember {
        listOf(
            Icons.Filled.Pets,
            Icons.Filled.Eco,
            Icons.Filled.Favorite,
            Icons.Filled.Star,
            Icons.Filled.Diamond,
            Icons.Filled.AutoAwesome,
            Icons.Filled.RocketLaunch,
            Icons.Filled.Spa
        )
    }

    var currentState by remember { mutableStateOf<PinSlotState>(PinSlotState.Empty) }

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
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
                is PinSlotState.AnimatedIcon -> {
                    Icon(
                        imageVector = state.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                is PinSlotState.Dot -> {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                            .shadow(
                                elevation = 14.dp,
                                shape = CircleShape,
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                    )
                }
            }
        }
    }
}
