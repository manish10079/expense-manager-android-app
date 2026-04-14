package com.mkn0079.expensetracker.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.data.constants.appLockSecurityQuestions
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurpleGlow
import com.mkn0079.expensetracker.ui.theme.PurplePrimary

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

    val isPinEntryVisible = !isRecoveryMode && !(mode == AppLockScreenMode.Setup && setupStage == PinSetupStage.SecurityQuestion)
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
                if (validateUnlockPin(enteredPin)) {
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
        else -> "ADD PIN RECOVERY"
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF09090B),
                        BackgroundDark,
                        Color(0xFF121216)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .then(
                        if (headerAction != null) {
                            Modifier.clickable(onClick = headerAction)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (headerAction != null) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFC8B7FF),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (title.isNotBlank()) {
                Text(
                    text = title,
                    color = PurplePrimary,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 300.dp, height = 240.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PurpleGlow.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isPinEntryVisible) {
                            Modifier
                        } else {
                            Modifier.verticalScroll(rememberScrollState())
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = eyebrow,
                    color = Color(0xFFB9B1C9),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 4.sp,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (headline.isNotBlank()) {
                    Text(
                        text = headline,
                        color = Color(0xFFF3EEF8),
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
                        color = if (message == null) Color(0xFF8F879E) else Color(0xFFFFAAA0),
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
                            },
                            onRecoverClick = {
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
                            },
                            onContinueClick = {
                                if (securityAnswer.isBlank()) {
                                    message = "Enter an answer for your security question."
                                } else {
                                    onSetupComplete(firstPin, selectedSecurityQuestionId, securityAnswer)
                                }
                            }
                        )
                    }

                    else -> {
                        PinEntryContent(
                            enteredPin = enteredPin,
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

                if (!isPinEntryVisible) {
                    Spacer(modifier = Modifier.height(22.dp))
                }
            }
        }
    }
}

@Composable
private fun PinEntryContent(
    enteredPin: String,
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
                            PurpleAccent
                        } else {
                            Color(0xFF3A3940)
                        }
                    )
                    .shadow(
                        elevation = if (filled) 14.dp else 0.dp,
                        shape = CircleShape,
                        ambientColor = PurpleGlow.copy(alpha = 0.35f),
                        spotColor = PurpleGlow.copy(alpha = 0.35f)
                    )
            )
        }
    }

    Spacer(modifier = Modifier.height(34.dp))

    val keypadRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("forgot", "0", "delete")
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        keypadRows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEach { key ->
                    AppLockKey(
                        key = key,
                        enabled = key != "forgot" || mode == AppLockScreenMode.Unlock,
                        onClick = {
                            when (key) {
                                "delete" -> onDeleteClick()
                                "forgot" -> onForgotClick()
                                else -> onDigitClick(key)
                            }
                        }
                    )
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
    onContinueClick: () -> Unit
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

        Spacer(modifier = Modifier.height(6.dp))

        PrimaryActionButton(
            label = "Save App Lock",
            onClick = onContinueClick
        )
    }
}

@Composable
private fun RecoveryQuestionContent(
    questionPrompt: String?,
    answer: String,
    onAnswerChange: (String) -> Unit,
    onRecoverClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF19191D))
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text = questionPrompt ?: "No recovery question is saved for this app lock yet.",
                color = Color(0xFFF3EEF8),
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

        PrimaryActionButton(
            label = "Disable App Lock",
            onClick = onRecoverClick
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
                    PurplePrimary.copy(alpha = 0.22f)
                } else {
                    Color(0xFF19191D)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            text = prompt,
            color = if (isSelected) Color(0xFFF6F0FF) else Color(0xFFD2CADF),
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
            focusedContainerColor = Color(0xFF1D1D21),
            unfocusedContainerColor = Color(0xFF1D1D21),
            focusedBorderColor = PurpleAccent.copy(alpha = 0.75f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
            focusedTextColor = Color(0xFFF0EBF7),
            unfocusedTextColor = Color(0xFFF0EBF7),
            focusedLabelColor = PurpleAccent,
            unfocusedLabelColor = Color(0xFF968EA8),
            cursorColor = PurpleAccent
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
            .height(58.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PurpleAccent,
            contentColor = Color(0xFF24114C)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun BiometricActionButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(68.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1D1D21),
            contentColor = PurpleAccent
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Fingerprint,
            contentDescription = "Use biometric",
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun AppLockKey(
    key: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val isForgot = key == "forgot"
    val isDelete = key == "delete"
    val shape = RoundedCornerShape(38.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(width = 84.dp, height = 84.dp)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (enabled) Color(0xFF232326) else Color(0xFF1A1A1D),
                        if (enabled) Color(0xFF1A1A1D) else Color(0xFF141417)
                    )
                )
            )
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
                    tint = if (enabled) Color(0xFFEDE7F7) else Color(0xFF6B6772),
                    modifier = Modifier.size(24.dp)
                )
            }

            isForgot -> {
                Text(
                    text = "FORGOT",
                    color = if (enabled) Color(0xFFC2BACE) else Color(0xFF6B6772),
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
                    color = if (enabled) Color(0xFFF3EEF8) else Color(0xFF6B6772),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 28.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 780)
@Composable
private fun AppLockUnlockPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        AppLockScreen(
            mode = AppLockScreenMode.Unlock,
            securityQuestionPrompt = "What was the name of your first school?"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 780)
@Composable
private fun AppLockSetupPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        AppLockScreen(
            mode = AppLockScreenMode.Setup,
            onBackClick = {}
        )
    }
}
