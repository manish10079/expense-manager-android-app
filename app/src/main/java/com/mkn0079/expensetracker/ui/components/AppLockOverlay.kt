package com.mkn0079.expensetracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mkn0079.expensetracker.ui.navigation.AppLockFlow
import com.mkn0079.expensetracker.ui.screens.AppLockScreen
import com.mkn0079.expensetracker.ui.screens.AppLockScreenMode

@Composable
fun AppLockOverlay(
    appLockFlow: AppLockFlow?,
    isAppUnlocked: Boolean,
    biometricEnabled: Boolean, 
    scrambledPinKeypadEnabled: Boolean,
    isBiometricAvailable: Boolean,
    securityQuestionPrompt: String,
    onBackClick: () -> Unit,
    onBiometricClick: (() -> Unit)?,
    onSetupComplete: (String, String, String) -> Unit,
    onUnlockSuccess: () -> Unit,
    validateUnlockPin: (String) -> Boolean,
    onForgotPinRecovery: () -> Unit,
    validateSecurityAnswer: (String) -> Boolean
) {
    val targetState = if (appLockFlow != null && (appLockFlow == AppLockFlow.Setup || !isAppUnlocked)) appLockFlow else null
    
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith
                fadeOut(animationSpec = tween(500))
        },
        label = "app_lock_transition",
        modifier = if (targetState != null) Modifier.fillMaxSize() else Modifier
    ) { flow ->
        if (flow != null) {
            AppLockScreen(
                mode = if (flow == AppLockFlow.Setup) AppLockScreenMode.Setup else AppLockScreenMode.Unlock,
                biometricEnabled = biometricEnabled,
                scrambledPinKeypadEnabled = scrambledPinKeypadEnabled,
                isBiometricAvailable = isBiometricAvailable,
                securityQuestionPrompt = securityQuestionPrompt,
                onBackClick = if (flow == AppLockFlow.Setup) onBackClick else null,
                onBiometricClick = if (flow == AppLockFlow.Unlock) onBiometricClick else null,
                onSetupComplete = onSetupComplete,
                onUnlockSuccess = onUnlockSuccess,
                validateUnlockPin = validateUnlockPin,
                onForgotPinRecovery = onForgotPinRecovery,
                validateSecurityAnswer = validateSecurityAnswer
            )
        }
    }
}
