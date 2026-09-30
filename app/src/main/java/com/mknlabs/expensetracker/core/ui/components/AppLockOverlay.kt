package com.mknlabs.expensetracker.core.ui.components

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.data.constants.defaultAppSettings
import com.mknlabs.expensetracker.data.local.AppLockPreferences
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.core.ui.navigation.AppLockFlow
import com.mknlabs.expensetracker.feature.auth.ui.AppLockScreen
import com.mknlabs.expensetracker.feature.auth.ui.AppLockScreenMode
import com.mknlabs.expensetracker.utils.BiometricAuthManager
import com.mknlabs.expensetracker.utils.BiometricAvailability
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
    isBiometricAvailable: Boolean? = null,
    securityQuestionPrompt: Int? = null,
    onBackClick: (() -> Unit)? = onDismiss,
    onBiometricClick: (() -> Unit)? = null,
    autoTriggerBiometricOnShow: Boolean = false,
    onSetupComplete: ((String, String, String) -> Unit)? = null,
    validateUnlockPin: ((String) -> Boolean)? = null,
    onForgotPinRecovery: (() -> Unit)? = null,
    validateSecurityAnswer: ((String) -> Boolean)? = null,
    getLockoutRemainingMillis: (() -> Long)? = null,
    getFailedAttemptCount: (() -> Int)? = null,
    pinVisualMode: PinVisualMode = PinVisualMode.NORMAL
) {
    val context = LocalContext.current
    // Compose previews render in layoutlib, which has no Android system services and no
    // keystore: BiometricManager.from() throws "Unsupported Service: biometric" and
    // EncryptedSharedPreferences cannot be created. Skip both probes while inspecting so a
    // preview never crashes, and let the explicit overrides below drive what is rendered.
    val isInPreview = LocalInspectionMode.current
    val biometricAvailability = remember(context, isInPreview) {
        if (isInPreview) BiometricAvailability(isAvailable = false)
        else BiometricAuthManager.getAvailability(context)
    }
    
    // Compute defaults if not provided
    val effectiveBiometricAvailable = isBiometricAvailable ?: biometricAvailability.isAvailable
    val detectedSecurityQuestionPromptResId = remember(context, isInPreview) {
        if (isInPreview) {
            null
        } else {
            val questionId = AppLockPreferences.getSecurityQuestionId(context)
            appLockSecurityQuestions.firstOrNull { it.id == questionId }?.promptResId
        }
    }
    val effectiveSecurityQuestionPromptResId =
        securityQuestionPrompt ?: detectedSecurityQuestionPromptResId
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
                getLockoutRemainingMillis = getLockoutRemainingMillis ?: {
                    AppLockPreferences.getLockoutRemainingMillis(context)
                },
                getFailedAttemptCount = getFailedAttemptCount ?: {
                    AppLockPreferences.getFailedAttemptCount(context)
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

@Preview(showBackground = true, widthDp = 360, heightDp = 780, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "App Lock Overlay (Dark)")
@Composable
private fun AppLockOverlayDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        // Root mode (onDismiss = null) instead of the Dialog overlay path: layoutlib does
        // not reliably render Dialog windows, and the rendered screen is identical.
        AppLockOverlay(
            isReady = true,
            appSettings = defaultAppSettings,
            isAppUnlocked = false,
            onUnlockSuccess = {},
            onDismiss = null,
            biometricEnabled = true,
            isBiometricAvailable = true,
            onBiometricClick = {},
            validateUnlockPin = { false },
            validateSecurityAnswer = { false },
            getLockoutRemainingMillis = { 0L },
            getFailedAttemptCount = { 0 }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "App Lock Overlay (Light)")
@Composable
private fun AppLockOverlayLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        // Root mode (onDismiss = null) instead of the Dialog overlay path: layoutlib does
        // not reliably render Dialog windows, and the rendered screen is identical.
        AppLockOverlay(
            isReady = true,
            appSettings = defaultAppSettings,
            isAppUnlocked = false,
            onUnlockSuccess = {},
            onDismiss = null,
            biometricEnabled = true,
            isBiometricAvailable = true,
            onBiometricClick = {},
            validateUnlockPin = { false },
            validateSecurityAnswer = { false },
            getLockoutRemainingMillis = { 0L },
            getFailedAttemptCount = { 0 }
        )
    }
}
