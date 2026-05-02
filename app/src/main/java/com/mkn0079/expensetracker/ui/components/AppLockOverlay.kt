package com.mkn0079.expensetracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.mkn0079.expensetracker.data.local.AppLockPreferences
import com.mkn0079.expensetracker.models.AppSettings
import com.mkn0079.expensetracker.ui.navigation.AppLockFlow
import com.mkn0079.expensetracker.ui.screens.AppLockScreen
import com.mkn0079.expensetracker.ui.screens.AppLockScreenMode
import com.mkn0079.expensetracker.utils.BiometricAuthManager
import com.mkn0079.expensetracker.data.constants.getAppLockSecurityQuestionPrompt // Corrected import path

/**
 * A wrapper component that handles the App Lock screen logic.
 * It can be used as a standalone screen (Root Mode) or as a Popup overlay.
 */
@Composable
fun AppLockOverlay(
    isReady: Boolean,
    appSettings: AppSettings,
    initialFlow: AppLockFlow = AppLockFlow.Unlock,
    isAppUnlocked: Boolean = false,
    onUnlockSuccess: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    // Optional callbacks for internal logic override
    biometricEnabled: Boolean = appSettings.biometricLockEnabled,
    scrambledPinKeypadEnabled: Boolean = appSettings.scrambledPinKeypadEnabled,
    isBiometricAvailable: Boolean = false,
    securityQuestionPrompt: String = "",
    onBackClick: (() -> Unit)? = onDismiss,
    onBiometricClick: (() -> Unit)? = null,
    onSetupComplete: ((String, String, String) -> Unit)? = null,
    validateUnlockPin: ((String) -> Boolean)? = null,
    onForgotPinRecovery: (() -> Unit)? = null,
    validateSecurityAnswer: ((String) -> Boolean)? = null
) {
    val context = LocalContext.current
    val biometricAvailability = remember(context) { BiometricAuthManager.getAvailability(context) }
    
    // Compute defaults if not provided
    val effectiveBiometricAvailable = biometricAvailability.isAvailable
    val effectiveSecurityQuestionPrompt = remember(context) {
        val questionId = AppLockPreferences.getSecurityQuestionId(context)
        getAppLockSecurityQuestionPrompt(questionId).orEmpty()
    }

    // If we are in "Overlay" mode (onDismiss is not null), we use a Popup.
    // If we are in "Root" mode (onDismiss is null), we render directly as a screen.
    val isOverlay = onDismiss != null
    
    val content = @Composable {
        AnimatedContent(
            targetState = initialFlow,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith
                    fadeOut(animationSpec = tween(500))
            },
            label = "app_lock_transition",
            modifier = Modifier.fillMaxSize()
        ) { flow ->
            AppLockScreen(
                mode = if (flow == AppLockFlow.Setup) AppLockScreenMode.Setup else AppLockScreenMode.Unlock,
                biometricEnabled = biometricEnabled,
                scrambledPinKeypadEnabled = scrambledPinKeypadEnabled,
                isBiometricAvailable = effectiveBiometricAvailable,
                securityQuestionPrompt = effectiveSecurityQuestionPrompt,
                onBackClick = if (flow == AppLockFlow.Setup) onBackClick else null,
                onBiometricClick = if (flow == AppLockFlow.Unlock) onBiometricClick else null,
                onSetupComplete = onSetupComplete ?: { _, _, _ -> },
                onUnlockSuccess = onUnlockSuccess,
                validateUnlockPin = validateUnlockPin ?: { pin -> 
                    AppLockPreferences.validatePin(context, pin) 
                },
                onForgotPinRecovery = onForgotPinRecovery ?: {},
                validateSecurityAnswer = validateSecurityAnswer ?: { answer ->
                    AppLockPreferences.validateSecurityAnswer(context, answer)
                }
            )
        }
    }

    if (isOverlay) {
        Popup(
            onDismissRequest = { onDismiss?.invoke() },
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                excludeFromSystemGesture = false
            )
        ) {
            content()
        }
    } else {
        content()
    }
}
