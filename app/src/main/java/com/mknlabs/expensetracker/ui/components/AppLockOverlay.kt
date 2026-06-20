package com.mknlabs.expensetracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mknlabs.expensetracker.data.local.AppLockPreferences
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.ui.navigation.AppLockFlow
import com.mknlabs.expensetracker.ui.screens.AppLockScreen
import com.mknlabs.expensetracker.ui.screens.AppLockScreenMode
import com.mknlabs.expensetracker.utils.BiometricAuthManager
import com.mknlabs.expensetracker.data.constants.appLockSecurityQuestions
import kotlinx.coroutines.delay

import com.mknlabs.expensetracker.models.PinVisualMode

private const val APP_LOCK_BIOMETRIC_AUTO_TRIGGER_DELAY_MS = 650L

/**
 * A wrapper component that handles the App Lock screen logic.
 * It can be used as a standalone screen (Root Mode) or as a dialog overlay.
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
    securityQuestionPrompt: Int? = null,
    onBackClick: (() -> Unit)? = onDismiss,
    onBiometricClick: (() -> Unit)? = null,
    autoTriggerBiometricOnShow: Boolean = false,
    onSetupComplete: ((String, String, String) -> Unit)? = null,
    validateUnlockPin: ((String) -> Boolean)? = null,
    onForgotPinRecovery: (() -> Unit)? = null,
    validateSecurityAnswer: ((String) -> Boolean)? = null,
    pinVisualMode: PinVisualMode = PinVisualMode.NORMAL
) {
    val context = LocalContext.current
    val biometricAvailability = remember(context) { BiometricAuthManager.getAvailability(context) }
    
    // Compute defaults if not provided
    val effectiveBiometricAvailable = biometricAvailability.isAvailable
    val effectiveSecurityQuestionPromptResId = remember(context) {
        val questionId = AppLockPreferences.getSecurityQuestionId(context)
        appLockSecurityQuestions.firstOrNull { it.id == questionId }?.promptResId
    }
    var hasAutoTriggeredBiometric by remember(initialFlow) { mutableStateOf(false) }

    // If we are in "Overlay" mode (onDismiss is not null), we use a full-screen dialog.
    // If we are in "Root" mode (onDismiss is null), we render directly as a screen.
    val isOverlay = onDismiss != null

    LaunchedEffect(
        initialFlow,
        biometricEnabled,
        effectiveBiometricAvailable,
        autoTriggerBiometricOnShow,
        onBiometricClick
    ) {
        if (
            !hasAutoTriggeredBiometric &&
            autoTriggerBiometricOnShow &&
            initialFlow == AppLockFlow.Unlock &&
            biometricEnabled &&
            effectiveBiometricAvailable &&
            onBiometricClick != null
        ) {
            hasAutoTriggeredBiometric = true
            delay(APP_LOCK_BIOMETRIC_AUTO_TRIGGER_DELAY_MS)
            onBiometricClick()
        }
    }
    
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
                securityQuestionPrompt = effectiveSecurityQuestionPromptResId,
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
                },
                pinVisualMode = pinVisualMode
            )
        }
    }

    if (isOverlay) {
        Dialog(
            onDismissRequest = { onDismiss?.invoke() },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            content()
        }
    } else {
        content()
    }
}
