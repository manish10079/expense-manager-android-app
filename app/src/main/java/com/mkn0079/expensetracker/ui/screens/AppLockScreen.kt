package com.mkn0079.expensetracker.ui.screens

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.data.constants.appLockSecurityQuestions
import com.mkn0079.expensetracker.ui.theme.brandGradient
import com.mkn0079.expensetracker.ui.theme.surfaceGradient
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.standardCardGradient
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.theme.Dimens
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

@Composable
fun AppLockScreen(
    mode: AppLockScreenMode,
    biometricEnabled: Boolean = false,
    scrambledPinKeypadEnabled: Boolean = false,
    isBiometricAvailable: Boolean = false,
    securityQuestionPrompt: String? = null,
    onBackClick: (() -> Unit)? = null,
    onBiometricClick: (() -> Unit)? = null,
    onSetupComplete: (String, String, String) -> Unit = { _, _, _ -> },
    onUnlockSuccess: () -> Unit = {},
    onForgotPinRecovery: () -> Unit = {},
    validateUnlockPin: (String) -> Boolean = { false },
    validateSecurityAnswer: (String) -> Boolean = { false }
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
    val triggerForgotRecovery: () -> Unit = {
        failedUnlockAttempts = 0
        enteredPin = ""
        if (mode == AppLockScreenMode.Unlock && securityQuestionPrompt != null) {
            isRecoveryMode = true
            recoveryAnswer = ""
            message = null
        } else if (mode == AppLockScreenMode.Unlock) {
            message = "Recovery question is not configured for this app lock."
        }
    }

    LaunchedEffect(mode, isRecoveryMode, scrambledPinKeypadEnabled) {
        refreshKeypadLayout()
        keypadShakeOffsetPx.snapTo(0f)
    }

    LaunchedEffect(enteredPin, mode, setupStage, isRecoveryMode) {
        if (!isPinEntryVisible || enteredPin.length != 4) return@LaunchedEffect

        when (mode) {
            AppLockScreenMode.Setup -> {
                if (setupStage == PinSetupStage.Create) {
                    firstPin = enteredPin
                    enteredPin = ""
                    setupStage = PinSetupStage.Confirm
                    message = "Enter the same PIN once more to confirm."
                } else if (enteredPin == firstPin) {
                    enteredPin = ""
                    setupStage = PinSetupStage.SecurityQuestion
                    message = "Choose one security question for PIN recovery."
                } else {
                    firstPin = ""
                    enteredPin = ""
                    setupStage = PinSetupStage.Create
                    message = "PINs did not match. Start again."
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
                        message = "Incorrect PIN. Try again."
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
                message = "Repeat the same 4 digits to finish setup."
            }
        }

        else -> onBackClick
    }

    val title = when {
        isRecoveryMode -> "Recover Access"
        mode == AppLockScreenMode.Unlock -> ""
        else -> "App Lock"
    }
    val eyebrow = when {
        isRecoveryMode -> "VERIFY SECURITY QUESTION"
        mode == AppLockScreenMode.Unlock -> if (biometricEnabled) {
            "BIOMETRIC SECURITY ACTIVE"
        } else {
            "PIN SECURITY ACTIVE"
        }

        setupStage == PinSetupStage.Create -> "CREATE A 4-DIGIT PIN"
        setupStage == PinSetupStage.Confirm -> "CONFIRM YOUR PIN"
        else -> ""
    }
    val headline = when {
        isRecoveryMode -> "Forgot Your PIN?"
        mode == AppLockScreenMode.Unlock -> "Welcome Back"
        setupStage == PinSetupStage.Create -> ""
        setupStage == PinSetupStage.Confirm -> "Confirm PIN"
        else -> "Security Question"
    }
    val supportText = message ?: when {
        isRecoveryMode -> securityQuestionPrompt?.let {
            "Answer your saved security question to disable app lock."
        } ?: "Recovery question is not configured for this app lock."

        mode == AppLockScreenMode.Unlock -> if (biometricEnabled && isBiometricAvailable) {
            "Use your biometric or enter your PIN to continue."
        } else {
            "Enter your PIN to continue."
        }

        setupStage == PinSetupStage.Create -> ""
        setupStage == PinSetupStage.Confirm -> "Repeat the same 4 digits to finish setup."
        else -> "Choose one of the five questions and enter an answer you will remember."
    }
    val primaryActionLabel = if (isRecoveryMode) "Disable App Lock" else "Save"
    val isDarkPalette = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val onPrimaryActionClick: () -> Unit = {
        if (isRecoveryMode) {
            if (securityQuestionPrompt == null) {
                message = "Recovery question is not configured for this app lock."
            } else if (recoveryAnswer.isBlank()) {
                message = "Enter your answer to disable app lock."
            } else if (validateSecurityAnswer(recoveryAnswer)) {
                onForgotPinRecovery()
            } else {
                recoveryAnswer = ""
                message = "Incorrect answer. Try again."
            }
        } else {
            if (securityAnswer.isBlank()) {
                message = "Enter an answer for your security question."
            } else {
                onSetupComplete(firstPin, selectedSecurityQuestionId, securityAnswer)
            }
        }
    }

    BackHandler(enabled = headerAction != null) {
        headerAction?.invoke()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                contentAlignment = if (isPinEntryVisible) Alignment.Center else Alignment.TopCenter
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
                        .padding(horizontal = 20.dp, vertical = 28.dp)
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
                            color = if (message == null) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.error
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
                                questionPrompt = securityQuestionPrompt,
                                answer = recoveryAnswer,
                                onAnswerChange = {
                                    recoveryAnswer = it
                                    message = null
                                }
                            )
                        }

                        mode == AppLockScreenMode.Setup && setupStage == PinSetupStage.SecurityQuestion -> {
                            SetupSecurityQuestionContent(
                                selectedQuestionId = selectedSecurityQuestionId,
                                answer = securityAnswer,
                                onQuestionSelected = {
                                    selectedSecurityQuestionId = it
                                    message = null
                                },
                                onAnswerChange = {
                                    securityAnswer = it
                                    message = null
                                }
                            )
                        }

                        else -> {
                            PinEntryContent(
                                enteredPin = enteredPin,
                                keypadLayout = keypadLayout,
                                keypadLayoutVersion = keypadLayoutVersion,
                                keypadShakeOffsetPx = if (scrambledPinKeypadEnabled) {
                                    keypadShakeOffsetPx.value
                                } else {
                                    0f
                                },
                                mode = mode,
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
                                }
                            )

                            if (mode == AppLockScreenMode.Unlock && biometricEnabled && isBiometricAvailable && onBiometricClick != null) {
                                Spacer(modifier = Modifier.height(34.dp))

                                BiometricActionButton(
                                    onClick = onBiometricClick
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))
                }
            }

            if (!isPinEntryVisible) {
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
    onForgotClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val filled = index < enteredPin.length
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                    .shadow(
                        elevation = if (filled) 14.dp else 0.dp,
                        shape = CircleShape,
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    )
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
    onAnswerChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        appLockSecurityQuestions.forEach { question ->
            SecurityQuestionCard(
                prompt = question.prompt,
                isSelected = question.id == selectedQuestionId,
                onClick = { onQuestionSelected(question.id) }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        AppLockAnswerField(
            value = answer,
            label = "Your Answer",
            placeholder = "Type your recovery answer",
            onValueChange = onAnswerChange
        )
    }
}

@Composable
private fun RecoveryQuestionContent(
    questionPrompt: String?,
    answer: String,
    onAnswerChange: (String) -> Unit
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
                text = questionPrompt ?: "No recovery question is saved for this app lock yet.",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        AppLockAnswerField(
            value = answer,
            label = "Security Answer",
            placeholder = "Enter your answer",
            onValueChange = onAnswerChange
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
    onValueChange: (String) -> Unit
) {
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
            contentDescription = "Use biometric",
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
                    contentDescription = "Delete PIN digit",
                    tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                    modifier = Modifier.size(34.dp)
                )
            }

            isForgot -> {
                Text(
                    text = "FORGOT",
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
