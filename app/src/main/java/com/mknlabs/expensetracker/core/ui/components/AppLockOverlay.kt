package com.mknlabs.expensetracker.core.ui.components

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.bold.Fingerprint
import com.adamglin.phosphoricons.fill.Diamond
import com.adamglin.phosphoricons.fill.FlowerLotus
import com.adamglin.phosphoricons.fill.Heart
import com.adamglin.phosphoricons.fill.Leaf
import com.adamglin.phosphoricons.fill.PawPrint
import com.adamglin.phosphoricons.fill.RocketLaunch
import com.adamglin.phosphoricons.fill.Sparkle
import com.adamglin.phosphoricons.fill.Star
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.navigation.AppLockFlow
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.data.constants.AppLockSecurityQuestion
import com.mknlabs.expensetracker.data.constants.buildRecoveryQuestionOptions
import com.mknlabs.expensetracker.data.constants.defaultAppSettings
import com.mknlabs.expensetracker.data.local.AppLockPreferences
import com.mknlabs.expensetracker.feature.auth.ui.APP_LOCK_DELETE_KEY
import com.mknlabs.expensetracker.feature.auth.ui.APP_LOCK_FORGOT_KEY
import com.mknlabs.expensetracker.feature.auth.ui.AppLockScreen
import com.mknlabs.expensetracker.feature.auth.ui.AppLockScreenMode
import com.mknlabs.expensetracker.feature.auth.ui.buildAppLockKeypadLayout
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.PinVisualMode
import com.mknlabs.expensetracker.utils.BiometricAuthManager
import com.mknlabs.expensetracker.utils.BiometricAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val APP_LOCK_BIOMETRIC_AUTO_TRIGGER_DELAY_MS = 650L

/** The mock holds the fourth dot up for a beat before it judges the PIN. */
private const val APP_LOCK_LAST_DIGIT_SETTLE_MS = 180L

/**
 * How long premium's glyph holds before handing over to the dot — the 400ms the pre-rewrite
 * unlock surface used (`PinSlot` in AppLockScreen).
 */
private const val APP_LOCK_GLYPH_HOLD_MS = 400L

/**
 * The glyph's box, 24dp — the size the glyphs had before the rewrite.
 *
 * Bigger than the mock's 14dp dot on purpose: the dot is the resting state, the glyph is the
 * flourish that lands on it. See the dot's own note for why it must not be clamped.
 */
private val AppLockGlyphSize = 24.dp

private const val APP_LOCK_PIN_LENGTH = 4

// ══════════════════════════════════════════════════════════════════════════════════════
//  Colours — deliberately local to this file while the lock design is still moving.
//
//  These are the mock's literal values (modern_lock_screen_mockup.html), not theme roles,
//  so the design can be iterated here without touching the app-wide palette. Once the lock
//  is signed off they move to core/ui/theme/Color.kt; several already exist there under a
//  name (noted per value), so that migration is mostly a rename plus a theme lookup.
// ══════════════════════════════════════════════════════════════════════════════════════

/** Mock `brand-gradient-text` / `pin-dot`: the ramp's lighter end, both themes. */
private val AppLockBrandStart = Color(0xFF6A4DFF) // Color.kt: BrandGradientStartLight

/** Mock `brand-gradient-text`: the ramp's title end. Color.kt: BrandGradientCompanion*. */
private val AppLockBrandCompanion = Color(0xFF6750A4)

/** Mock `pin-dot`: the dot ends deeper than the title does. Color.kt: HeroRailEndLight. */
private val AppLockPinDotEnd = Color(0xFF5B45D6)

/** Mock `pin-dot-error`. */
private val AppLockPinDotError = Color(0xFFF43F5E)

/**
 * The aurora's glow colours: the mock's `rgba(106,77,255,.20)` and `rgba(106,77,255,.30)`.
 *
 * The mock's third glow was a dimmer violet (`rgba(103,80,164,.28)`) in the bottom-right.
 * Both corners now paint the stronger brand glow: the violet sank into black, and matching
 * the two corners is the one reading that survives both themes.
 */
private val AppLockAuroraBrandSoft = Color(0x336A4DFF)
private val AppLockAuroraBrandStrong = Color(0x4D6A4DFF)

/** Mock biometric ring: `border-indigo-500/50` + `ring-indigo-500/20`. */
private val AppLockBiometricRing = Color(0x806365F1)
private val AppLockBiometricHalo = Color(0x336365F1)

/**
 * The lock screen's theme-dependent values.
 *
 * Only the things the mock actually repaints between its two themes live here. Everything
 * else — the brand ramps, the biometric ring, the aurora — is deliberately identical in
 * both, because the mock keeps one identity across the switch.
 */
private class AppLockPalette(
    /** Mock `bg-black` / `bg-white` on the phone container. */
    val container: Color,
    /** Mock `text-slate-100` / `text-slate-800`. */
    val ink: Color,
    /** Mock `keypad-btn-*`: `background`. */
    val keyFill: Color,
    /** Mock `keypad-btn-*`: `border`. */
    val keyBorder: Color,
    /** Mock `keypad-btn-*:active`: `background`. */
    val keyPressFill: Color,
    /** Mock `keypad-btn-*:active`: `border-color`. */
    val keyPressBorder: Color,
)

private val AppLockPaletteDark = AppLockPalette(
    container = Color(0xFF000000),
    ink = Color(0xFFF1F5F9),
    keyFill = Color(0x0DFFFFFF), // rgba(255,255,255,.05)
    keyBorder = Color(0x0FFFFFFF), // rgba(255,255,255,.06)
    keyPressFill = Color(0x596A4DFF), // rgba(106,77,255,.35)
    keyPressBorder = Color(0x8C6A4DFF), // rgba(106,77,255,.55)
)

private val AppLockPaletteLight = AppLockPalette(
    container = Color(0xFFFFFFFF),
    ink = Color(0xFF1E293B),
    keyFill = Color(0x08000000), // rgba(0,0,0,.03)
    keyBorder = Color(0x0A000000), // rgba(0,0,0,.04)
    keyPressFill = Color(0x266A4DFF), // rgba(106,77,255,.15)
    keyPressBorder = Color(0x596A4DFF), // rgba(106,77,255,.35)
)

@Composable
private fun appLockPalette(): AppLockPalette =
    if (MaterialTheme.colorScheme.isDark) AppLockPaletteDark else AppLockPaletteLight

/** Mock `brand-gradient-text`: `linear-gradient(135deg, #6A4DFF, #6750A4)`. */
private fun appLockBrandGradient(): Brush =
    Brush.linearGradient(colors = listOf(AppLockBrandStart, AppLockBrandCompanion))

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
    // Optional override for the recovery question set (real question + decoys).
    // When null, AppLockOverlay derives it from the saved security question.
    recoveryQuestions: List<AppLockSecurityQuestion>? = null,
    onBackClick: (() -> Unit)? = onDismiss,
    onBiometricClick: (() -> Unit)? = null,
    autoTriggerBiometricOnShow: Boolean = false,
    onSetupComplete: ((String, String, String) -> Unit)? = null,
    validateUnlockPin: ((String) -> Boolean)? = null,
    onForgotPinRecovery: (() -> Unit)? = null,
    validateSecurityAnswer: ((String, String) -> Boolean)? = null,
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
    // Real saved question + random decoys, recomputed only when the saved question
    // changes so the presented set stays stable throughout a recovery session.
    val detectedRecoveryQuestions = remember(context, isInPreview) {
        if (isInPreview) {
            emptyList()
        } else {
            buildRecoveryQuestionOptions(AppLockPreferences.getSecurityQuestionId(context))
        }
    }
    val effectiveRecoveryQuestions = recoveryQuestions ?: detectedRecoveryQuestions
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
            if (flow == AppLockFlow.Setup) {
                // Setting the lock up is still the older card-based flow. The redesigned
                // keypad below is the UNLOCK surface, which is what the mock covers.
                AppLockScreen(
                    mode = AppLockScreenMode.Setup,
                    biometricEnabled = biometricEnabled,
                    scrambledPinKeypadEnabled = scrambledPinKeypadEnabled,
                    isBiometricAvailable = effectiveBiometricAvailable,
                    recoveryQuestions = effectiveRecoveryQuestions,
                    onBackClick = onBackClick,
                    onBiometricClick = null,
                    onSetupComplete = onSetupComplete ?: { _, _, _ -> },
                    onUnlockSuccess = onUnlockSuccess,
                    validateUnlockPin = validateUnlockPin ?: { pin ->
                        AppLockPreferences.validatePin(context, pin)
                    },
                    onForgotPinRecovery = onForgotPinRecovery ?: {},
                    validateSecurityAnswer = validateSecurityAnswer ?: { questionId, answer ->
                        AppLockPreferences.validateSecurityQuestionAnswer(context, questionId, answer)
                    },
                    getLockoutRemainingMillis = getLockoutRemainingMillis ?: {
                        AppLockPreferences.getLockoutRemainingMillis(context)
                    },
                    getFailedAttemptCount = getFailedAttemptCount ?: {
                        AppLockPreferences.getFailedAttemptCount(context)
                    },
                    pinVisualMode = pinVisualMode
                )
            } else {
                AppLockKeypadScreen(
                    biometricEnabled = biometricEnabled,
                    isBiometricAvailable = effectiveBiometricAvailable,
                    scrambledPinKeypadEnabled = scrambledPinKeypadEnabled,
                    recoveryQuestions = effectiveRecoveryQuestions,
                    pinVisualMode = pinVisualMode,
                    onBiometricClick = onBiometricClick,
                    onForgotPinRecovery = onForgotPinRecovery ?: {},
                    validateUnlockPin = validateUnlockPin ?: { pin ->
                        AppLockPreferences.validatePin(context, pin)
                    },
                    validateSecurityAnswer = validateSecurityAnswer ?: { questionId, answer ->
                        AppLockPreferences.validateSecurityQuestionAnswer(context, questionId, answer)
                    },
                    getLockoutRemainingMillis = getLockoutRemainingMillis ?: {
                        AppLockPreferences.getLockoutRemainingMillis(context)
                    },
                    onUnlockSuccess = onUnlockSuccess
                )
            }
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

/**
 * The unlock surface: the "modern lock screen" mock, replicated.
 *
 * Self-contained on purpose — it owns the PIN state, the lockout countdown, the recovery
 * questions and the keypad layout, because the mock is a whole screen rather than a
 * component that can be dropped into the older card flow. [AppLockScreen] still owns
 * setting the lock up; only the unlock path is drawn from here.
 *
 * Every button the mock advertises does something real: the keys type, FORGOT opens the
 * saved security question, the fingerprint runs the caller's biometric prompt and a valid
 * PIN unlocks. The mock's Dark / Light pill is deliberately not drawn — the app's theme is
 * an app-level setting owned by the settings screen, and a control on the lock that could
 * not change it would be a dead button.
 */
@Composable
private fun AppLockKeypadScreen(
    biometricEnabled: Boolean,
    isBiometricAvailable: Boolean,
    scrambledPinKeypadEnabled: Boolean,
    recoveryQuestions: List<AppLockSecurityQuestion>,
    pinVisualMode: PinVisualMode,
    onBiometricClick: (() -> Unit)?,
    onForgotPinRecovery: () -> Unit,
    validateUnlockPin: (String) -> Boolean,
    validateSecurityAnswer: (String, String) -> Boolean,
    getLockoutRemainingMillis: () -> Long,
    onUnlockSuccess: () -> Unit
) {
    val palette = appLockPalette()
    val scope = rememberCoroutineScope()

    // ── Strings. Every one is resolved unconditionally: a stringResource call inside a
    // conditional branch makes a group appear/disappear in the slot table, which makes
    // Compose dispose and recreate the content around it (see AppLockScreen for the
    // long-form of this pitfall).
    val appTitle = stringResource(R.string.label_app_name_display)
    val appSubtitle = stringResource(R.string.label_budget_and_spend)
    val touchIdLabel = stringResource(R.string.label_app_lock_biometric_touch_id)
    val forgotLabel = stringResource(R.string.label_forgot)
    val enterPinMsg = stringResource(R.string.msg_enter_pin_to_continue)
    val biometricPinMsg = stringResource(R.string.msg_biometric_or_pin_to_continue)
    val incorrectPinMsg = stringResource(R.string.msg_incorrect_pin_try_again)
    val noRecoveryQuestionMsg = stringResource(R.string.msg_recovery_question_is_not_confi)
    val recoveryEyebrow = stringResource(R.string.label_verify_security_question)
    val recoveryHeadline = stringResource(R.string.label_forgot_your_pin)
    val recoverySupportMsg = stringResource(R.string.msg_select_your_security_question)
    val selectQuestionMsg = stringResource(R.string.msg_select_a_security_question)
    val enterAnswerMsg = stringResource(R.string.msg_enter_an_answer_for_your_secur)
    val incorrectAnswerMsg = stringResource(R.string.msg_incorrect_answer_try_again)
    val answerLabel = stringResource(R.string.label_security_answer)
    val answerPlaceholder = stringResource(R.string.placeholder_enter_your_answer)
    val submitRecoveryLabel = stringResource(R.string.label_disable_app_lock)
    val quotes = stringArrayResource(R.array.app_lock_quotes)

    var enteredPin by rememberSaveable { mutableStateOf("") }
    var statusText by rememberSaveable { mutableStateOf<String?>(null) }
    var statusIsError by rememberSaveable { mutableStateOf(false) }
    var isRecovery by rememberSaveable { mutableStateOf(false) }
    var recoveryQuestionId by rememberSaveable { mutableStateOf<String?>(null) }
    var recoveryAnswer by rememberSaveable { mutableStateOf("") }
    // The keypad layout IS the animated state below: a scramble re-shuffles the list, and
    // keying the crossfade on the list is what lets the outgoing arrangement finish out.
    var keypadLayout by remember(scrambledPinKeypadEnabled) {
        mutableStateOf(buildAppLockKeypadLayout(scrambled = scrambledPinKeypadEnabled))
    }
    // The mock shows one quote at a time and swaps it on each reset, so the same line
    // cannot become wallpaper the user stops reading. Seeded once here rather than in a
    // composition-time write, which would be a side effect during composition.
    var quoteIndex by rememberSaveable {
        mutableIntStateOf(if (quotes.isEmpty()) 0 else quotes.indices.random())
    }
    var isPinError by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }

    // ── Brute-force lockout. Polled from AppLockPreferences every second so the UI stays
    // in sync even after a failed attempt arms a new window mid-flow.
    var lockoutSeconds by remember {
        mutableIntStateOf(((getLockoutRemainingMillis() + 999L) / 1000L).toInt().coerceAtLeast(0))
    }
    fun refreshLockout() {
        lockoutSeconds = ((getLockoutRemainingMillis() + 999L) / 1000L).toInt().coerceAtLeast(0)
    }
    LaunchedEffect(lockoutSeconds > 0) {
        if (lockoutSeconds > 0) {
            while (lockoutSeconds > 0) {
                delay(1000L)
                refreshLockout()
            }
        }
    }
    val isLockedOut = lockoutSeconds > 0
    // Resolved UNCONDITIONALLY: a stringResource inside a conditional would make a group
    // appear/disappear in the slot table (see AppLockScreen for the long-form of this).
    val lockedOutMessageText = stringResource(R.string.msg_pin_locked_out, lockoutSeconds)
    val lockedOutMessage = if (isLockedOut) lockedOutMessageText else null
    fun startFreshQuote() {
        if (quotes.isNotEmpty()) quoteIndex = quotes.indices.random()
    }
    fun refreshKeypadLayout() {
        keypadLayout = buildAppLockKeypadLayout(scrambled = scrambledPinKeypadEnabled)
    }

    LaunchedEffect(enteredPin, isRecovery) {
        if (isRecovery || enteredPin.length != APP_LOCK_PIN_LENGTH) return@LaunchedEffect
        // Hold the fourth dot for a beat so it is seen to land before the verdict.
        delay(APP_LOCK_LAST_DIGIT_SETTLE_MS)
        val candidate = enteredPin
        val isValid = withContext(Dispatchers.Default) { validateUnlockPin(candidate) }
        if (isValid) {
            // A correct PIN hands the app straight back: no "Unlocked!" beat in between.
            // enteredPin is left as it is because this effect is keyed on it, and clearing
            // it here would only restart the effect on the way out.
            onUnlockSuccess()
        } else {
            // Pick up a freshly-armed lockout immediately so the countdown shows without
            // waiting for the next 1s poll tick.
            refreshLockout()
            if (lockoutSeconds > 0) {
                enteredPin = ""
            } else {
                // The mock answers a wrong PIN with the dots themselves: they turn red and
                // shake, then clear. The shake runs in its own scope because clearing the
                // PIN below would otherwise cancel it.
                scope.launch {
                    isPinError = true
                    shakeOffset.animateAppLockShake()
                    isPinError = false
                    enteredPin = ""
                }
            }
            startFreshQuote()
        }
    }

    val submitRecovery: () -> Unit = {
        val questionId = recoveryQuestionId
        when {
            recoveryQuestions.isEmpty() -> {
                statusIsError = true
                statusText = noRecoveryQuestionMsg
            }

            questionId == null -> {
                statusIsError = true
                statusText = selectQuestionMsg
            }

            recoveryAnswer.isBlank() -> {
                statusIsError = true
                statusText = enterAnswerMsg
            }

            else -> scope.launch {
                // PBKDF2 verification is ~50-150ms: keep it off the main thread.
                val isValid = withContext(Dispatchers.Default) {
                    validateSecurityAnswer(questionId, recoveryAnswer)
                }
                if (isValid) {
                    onForgotPinRecovery()
                } else {
                    recoveryAnswer = ""
                    // A wrong answer also advances the persisted lockout counter — refresh
                    // so a freshly-armed window shows instead of the plain error.
                    refreshLockout()
                    statusIsError = true
                    statusText = if (lockoutSeconds > 0) null else incorrectAnswerMsg
                }
            }
        }
    }

    BackHandler(enabled = isRecovery) { isRecovery = false }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.container)
    ) {
        // The mock's aurora. Drawn rather than layered as three composables: each glow is
        // an ellipse, which no Brush can express directly (see drawAppLockAurora).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawAppLockAurora(size) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            if (isRecovery) {
                AppLockRecoveryContent(
                    palette = palette,
                    eyebrow = recoveryEyebrow,
                    headline = recoveryHeadline,
                    supportText = statusText ?: recoverySupportMsg,
                    supportIsError = statusText != null && statusIsError,
                    questions = recoveryQuestions,
                    selectedQuestionId = recoveryQuestionId,
                    onQuestionSelected = {
                        recoveryQuestionId = it
                        statusText = null
                    },
                    answer = recoveryAnswer,
                    onAnswerChange = {
                        recoveryAnswer = it
                        statusText = null
                    },
                    answerLabel = answerLabel,
                    answerPlaceholder = answerPlaceholder,
                    submitLabel = submitRecoveryLabel,
                    onSubmit = submitRecovery,
                    onBack = { isRecovery = false }
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // The branding pair is lifted out of the block's own centring. The mock
                    // stacks all four lines 4dp apart, so nudging only the title and the
                    // subtitle gives the air above them and leaves the phrase, the prompt,
                    // the dots and the keypad exactly where they were.
                    Column(
                        modifier = Modifier.offset(y = (-100).dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = appTitle,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                brush = appLockBrandGradient(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Lifted past the mock's `text-base` (16px) on request, so the pair
                        // reads as a title and its subtitle rather than two similar sizes.
                        Text(
                            text = appSubtitle,
                            color = palette.ink,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp,
                                letterSpacing = 0.3.sp
                            ),
                            modifier = Modifier.graphicsLayer { alpha = 0.6f }
                        )
                    }
                    if (quotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\u201C${quotes[quoteIndex.coerceIn(quotes.indices)]}\u201D",
                            color = palette.ink,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .offset(y = -90.dp)
                                .widthIn(max = 280.dp)
                                .graphicsLayer { alpha = 0.5f }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (biometricEnabled && isBiometricAvailable) {
                            biometricPinMsg
                        } else {
                            enterPinMsg
                        },
                        color = palette.ink,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer { alpha = 0.7f }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AppLockPinStatusRow(
                        filledCount = enteredPin.length,
                        pinVisualMode = pinVisualMode,
                        isError = isPinError,
                        shakeOffsetPx = shakeOffset.value,
                        statusText = lockedOutMessage ?: statusText,
                        errorDescription = incorrectPinMsg
                    )


                    Spacer(modifier = Modifier.height(32.dp))

                    AppLockKeypad(
                        layout = keypadLayout,
                        enabled = !isLockedOut && !isPinError,
                        onDigit = { digit ->
                            if (enteredPin.length < APP_LOCK_PIN_LENGTH) {
                                enteredPin += digit
                                statusText = null
                            }
                        },
                        onDelete = { enteredPin = enteredPin.dropLast(1) },
                        onForgot = {
                            if (isLockedOut) {
                                // While locked out the security question is rate-limited by
                                // the same lockout; the countdown already speaks.
                                statusText = null
                            } else if (recoveryQuestions.isEmpty()) {
                                statusIsError = true
                                statusText = noRecoveryQuestionMsg
                            } else {
                                enteredPin = ""
                                recoveryQuestionId = null
                                recoveryAnswer = ""
                                statusText = null
                                isRecovery = true
                            }
                        },
                        forgotLabel = forgotLabel,
                        palette = palette
                    )
                }

                if (biometricEnabled && isBiometricAvailable && onBiometricClick != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppLockBiometricButton(
                            label = touchIdLabel,
                            palette = palette,
                            onClick = onBiometricClick
                        )
                    }
                }
            }
        }
    }
}

/**
 * The mock's centre column: four dots, or a sentence where the dots would be.
 *
 * [filledCount] rather than a PIN string because the dots are the only thing that reads
 * it, and the mock draws no empty slots — an entered digit pops a dot in, and deleting
 * pops it back out. The sentence is always an error line: a correct PIN unlocks on the
 * spot rather than announcing itself, so there is no success state to style.
 */
@Composable
private fun AppLockPinStatusRow(
    filledCount: Int,
    pinVisualMode: PinVisualMode,
    isError: Boolean,
    shakeOffsetPx: Float,
    statusText: String?,
    errorDescription: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .graphicsLayer { translationX = shakeOffsetPx }
            .semantics {
                // The shake and the red dots are the whole error signal on screen, and
                // neither reaches a screen reader. Surface the same sentence it means.
                if (isError) {
                    contentDescription = errorDescription
                    liveRegion = LiveRegionMode.Assertive
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = statusText,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "app_lock_pin_status"
        ) { message ->
            if (message != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = message,
                        color = AppLockPinDotError,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(filledCount) {
                        AppLockPinDot(
                            isError = isError,
                            pinVisualMode = pinVisualMode
                        )
                    }
                }
            }
        }
    }
}

/**
 * One entered PIN digit: the mock's brand-painted dot, with premium's glyph landing on it.
 *
 * The dot is the mock's — 14dp, the `pin-dot` ramp, a 6dp brand shadow, the `dot-pop`
 * overshoot. Premium keeps the flourish the pre-rewrite surface had: a glyph from the same
 * pool eases in from 0.7 on a soft spring, holds 400ms, then hands over to the dot.
 */
@Composable
private fun AppLockPinDot(
    isError: Boolean,
    pinVisualMode: PinVisualMode
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
    var glyph by remember { mutableStateOf<ImageVector?>(null) }
    // The mock's popIn overshoots to 1.2 before landing on 1, which is what a low damping
    // ratio gives for free.
    val scale = remember { Animatable(0f) }
    // The glyph does not pop from nothing: it eases in from 0.7, as the pre-rewrite
    // surface's scaleIn did.
    val glyphScale = remember { Animatable(0.7f) }
    val dotBrush = remember(isError) {
        if (isError) {
            SolidColor(AppLockPinDotError)
        } else {
            Brush.linearGradient(colors = listOf(AppLockBrandStart, AppLockPinDotEnd))
        }
    }

    LaunchedEffect(isError, pinVisualMode) {
        if (!isError && pinVisualMode == PinVisualMode.PRO_ANIMATED) {
            // While the glyph is up it carries the entrance on its own, so the dot's pop is
            // parked at full size instead of fighting it up from zero.
            scale.snapTo(1f)
            glyph = iconPool.random()
            glyphScale.snapTo(0.7f)
            glyphScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            delay(APP_LOCK_GLYPH_HOLD_MS)
            glyph = null
            scale.snapTo(0f)
        } else {
            glyph = null
            scale.snapTo(0f)
        }
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Box(
        modifier = Modifier
            .size(14.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        contentAlignment = Alignment.Center
    ) {
        val currentGlyph = glyph
        if (currentGlyph != null) {
            // requiredSize, not size: the glyph is meant to be larger than the dot it lands
            // on, and a plain size() would be clamped to this 14dp slot — which is what made
            // the glyphs read as specks.
            Box(
                modifier = Modifier
                    .requiredSize(AppLockGlyphSize)
                    .graphicsLayer {
                        scaleX = glyphScale.value
                        scaleY = glyphScale.value
                    }
            ) {
                AppLockGradientIcon(
                    imageVector = currentGlyph,
                    brush = appLockBrandGradient(),
                    size = AppLockGlyphSize
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = AppLockBrandStart.copy(alpha = 0.5f),
                        spotColor = AppLockBrandStart.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(brush = dotBrush)
            )
        }
    }
}

/**
 * The mock's 3×4 keypad: one circle per key, flat until pressed.
 *
 * [layout] comes from [buildAppLockKeypadLayout] so premium's scrambled keypad keeps
 * working; the mock only draws the unscrambled arrangement.
 */
@Composable
private fun AppLockKeypad(
    layout: List<List<String>>,
    enabled: Boolean,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onForgot: () -> Unit,
    forgotLabel: String,
    palette: AppLockPalette
) {
    AnimatedContent(
        targetState = layout,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(140))
        },
        label = "app_lock_keypad_layout"
    ) { currentLayout ->
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            currentLayout.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    row.forEach { key ->
                        AppLockKey(
                            key = key,
                            enabled = enabled,
                            onClick = {
                                when (key) {
                                    APP_LOCK_DELETE_KEY -> onDelete()
                                    APP_LOCK_FORGOT_KEY -> onForgot()
                                    else -> onDigit(key)
                                }
                            },
                            forgotLabel = forgotLabel,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppLockKey(
    key: String,
    enabled: Boolean,
    onClick: () -> Unit,
    forgotLabel: String,
    palette: AppLockPalette,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    // The mock presses with `transform: scale(.92)` and swaps the fill/border for a brand
    // tint, both over 150ms.
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "app_lock_key_scale"
    )
    val fill = if (isPressed) palette.keyPressFill else palette.keyFill
    val border = if (isPressed) palette.keyPressBorder else palette.keyBorder

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(fill)
            .border(width = 1.dp, color = border, shape = CircleShape)
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
        val contentAlpha = if (enabled) 1f else 0.45f
        when (key) {
            APP_LOCK_DELETE_KEY -> Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = stringResource(R.string.desc_delete_pin_digit),
                tint = palette.ink.copy(alpha = contentAlpha),
                modifier = Modifier.size(22.dp)
            )

            APP_LOCK_FORGOT_KEY -> Text(
                text = forgotLabel,
                color = palette.ink,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                ),
                modifier = Modifier.graphicsLayer { alpha = 0.7f * contentAlpha }
            )

            else -> Text(
                text = key,
                color = palette.ink.copy(alpha = contentAlpha),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
            )
        }
    }
}

/** The mock's fingerprint target: a ringed circle with a brand-painted glyph under it. */
@Composable
private fun AppLockBiometricButton(
    label: String,
    palette: AppLockPalette,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "app_lock_biometric_scale"
    )

    Column(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(AppLockBiometricHalo),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(width = 2.dp, color = AppLockBiometricRing, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AppLockGradientIcon(
                    imageVector = PhosphorIcons.Bold.Fingerprint,
                    brush = appLockBrandGradient(),
                    size = 44.dp
                )
            }
        }
        Text(
            text = label,
            color = palette.ink,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.graphicsLayer { alpha = 0.6f }
        )
    }
}

/**
 * Recovery, in the mock's language.
 *
 * The mock does not cover this screen, so it is built from the same tokens and type scale
 * as the keypad rather than invented separately: the same title treatment, cards shaped
 * like the keys, and the same brand-painted CTA. Validation goes through the caller's
 * callbacks, so the decoys and the shared lockout behave exactly as before.
 */
@Composable
private fun AppLockRecoveryContent(
    palette: AppLockPalette,
    eyebrow: String,
    headline: String,
    supportText: String,
    supportIsError: Boolean,
    questions: List<AppLockSecurityQuestion>,
    selectedQuestionId: String?,
    onQuestionSelected: (String) -> Unit,
    answer: String,
    onAnswerChange: (String) -> Unit,
    answerLabel: String,
    answerPlaceholder: String,
    submitLabel: String,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = palette.ink,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = eyebrow,
                color = palette.ink,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = 0.6f }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = headline,
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = appLockBrandGradient(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = supportText,
                color = if (supportIsError) AppLockPinDotError else palette.ink,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = if (supportIsError) 1f else 0.7f }
            )
            Spacer(modifier = Modifier.height(20.dp))

            questions.forEach { question ->
                AppLockRecoveryQuestionCard(
                    prompt = stringResource(question.promptResId),
                    isSelected = question.id == selectedQuestionId,
                    onClick = { onQuestionSelected(question.id) },
                    palette = palette
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = answer,
                onValueChange = onAnswerChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                label = { Text(text = answerLabel) },
                placeholder = { Text(text = answerPlaceholder) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onSubmit()
                        keyboardController?.hide()
                    }
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(appLockBrandGradient())
                    .clickable(onClick = onSubmit),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = submitLabel,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun AppLockRecoveryQuestionCard(
    prompt: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    palette: AppLockPalette
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) AppLockAuroraBrandStrong else palette.keyFill)
            .border(
                width = 1.dp,
                color = if (isSelected) AppLockBrandStart.copy(alpha = 0.55f) else palette.keyBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Text(
            text = prompt,
            color = if (isSelected) AppLockBrandStart else palette.ink,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

/**
 * Paints [imageVector] with [brush] instead of a flat tint.
 *
 * `tint` only takes a solid colour, so the glyph is drawn white and the ramp is punched
 * through its alpha with SrcIn, which keeps the icon's own silhouette. The offscreen layer
 * fences the blend in so it cannot erase what was drawn around it.
 */
@Composable
private fun AppLockGradientIcon(
    imageVector: ImageVector,
    brush: Brush,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = Color.White,
        modifier = modifier
            .size(size)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                drawRect(brush = brush, blendMode = BlendMode.SrcIn)
            }
    )
}

/**
 * The mock's background: three brand glows over the theme's field.
 *
 * CSS draws them as ELLIPTICAL radial gradients (`70% 44% at 50% 46%`), which Compose's
 * [Brush.radialGradient] cannot express — it takes one radius. Each is therefore drawn as
 * a circle sized to the horizontal radius and scaled vertically around its own centre,
 * which is the same ellipse. The mock's film-grain layer is not drawn: it is a 7% texture
 * over a 160px tile, which would mean shipping and tiling a noise bitmap for something
 * that is invisible against the glows on a phone screen.
 */
private fun DrawScope.drawAppLockAurora(size: Size) {
    appLockGlows.forEach { glow ->
        val radiusPx = size.width * glow.radiusX
        if (radiusPx <= 0f) return@forEach
        val brush = Brush.radialGradient(
            colorStops = arrayOf(0f to glow.color, glow.fade to Color.Transparent),
            center = Offset.Zero,
            radius = radiusPx
        )
        withTransform(
            transformBlock = {
                translate(left = size.width * glow.centerX, top = size.height * glow.centerY)
                scale(scaleX = 1f, scaleY = (size.height * glow.radiusY) / radiusPx, pivot = Offset.Zero)
            }
        ) {
            drawCircle(brush = brush, radius = radiusPx, center = Offset.Zero)
        }
    }
}

private class AppLockGlow(
    val color: Color,
    /** Centre, as a fraction of the box. */
    val centerX: Float,
    val centerY: Float,
    /** Radii, as fractions of the box's width and height. */
    val radiusX: Float,
    val radiusY: Float,
    /** Where the colour has fully faded, as a fraction of the radius. */
    val fade: Float
)

private val appLockGlows = listOf(
    AppLockGlow(AppLockAuroraBrandSoft, 0.50f, 0.46f, 0.70f, 0.44f, 0.70f),
    AppLockGlow(AppLockAuroraBrandStrong, 0.12f, 0.06f, 0.66f, 0.44f, 0.62f),
    // The top-left glow, mirrored: same colour, same radii, same fade, centre reflected
    // through the middle of the box (0.12/0.06 -> 0.88/0.94).
    AppLockGlow(AppLockAuroraBrandStrong, 0.88f, 0.94f, 0.66f, 0.44f, 0.62f)
)

/**
 * The mock's `shake` keyframes: ±8px collapsing to nothing over 400ms.
 *
 * Runs in the caller's scope rather than inside the state effect that detected the wrong
 * PIN, because clearing the PIN is what cancels that effect.
 */
private suspend fun Animatable<Float, AnimationVector1D>.animateAppLockShake() {
    val offsets = listOf(-8f, 8f, -8f, 8f, 0f)
    snapTo(0f)
    offsets.forEach { target ->
        animateTo(targetValue = target, animationSpec = tween(durationMillis = 80))
    }
    snapTo(0f)
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
            validateSecurityAnswer = { _, _ -> false },
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
            validateSecurityAnswer = { _, _ -> false },
            getLockoutRemainingMillis = { 0L },
            getFailedAttemptCount = { 0 }
        )
    }
}
