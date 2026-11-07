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

@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "Biometric Unlock Dark")
@Composable
internal fun AppLockBiometricDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        AppLockScreen(
            mode = AppLockScreenMode.Unlock,
            biometricEnabled = true,
            isBiometricAvailable = true,
            onBiometricClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "Biometric Unlock Light")
@Composable
internal fun AppLockBiometricLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        AppLockScreen(
            mode = AppLockScreenMode.Unlock,
            biometricEnabled = true,
            isBiometricAvailable = true,
            onBiometricClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Security Question Setup Dark")
@Composable
internal fun SecurityQuestionSetupDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface {
            SetupSecurityQuestionContent(
                selectedQuestionId = appLockSecurityQuestions.first().id,
                answer = "Fluffy",
                onQuestionSelected = {},
                onAnswerChange = {},
                onDone = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Security Question Setup Light")
@Composable
internal fun SecurityQuestionSetupLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface {
            SetupSecurityQuestionContent(
                selectedQuestionId = appLockSecurityQuestions.first().id,
                answer = "Fluffy",
                onQuestionSelected = {},
                onAnswerChange = {},
                onDone = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Security Question Recovery Dark")
@Composable
internal fun SecurityQuestionRecoveryDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface {
            RecoveryQuestionContent(
                questions = appLockSecurityQuestions.take(3),
                selectedQuestionId = appLockSecurityQuestions.first().id,
                onQuestionSelected = {},
                answer = "Fluffy",
                onAnswerChange = {},
                onDone = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Security Question Recovery Light")
@Composable
internal fun SecurityQuestionRecoveryLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface {
            RecoveryQuestionContent(
                questions = appLockSecurityQuestions.take(3),
                selectedQuestionId = appLockSecurityQuestions.first().id,
                onQuestionSelected = {},
                answer = "Fluffy",
                onAnswerChange = {},
                onDone = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Security Question Card Dark")
@Composable
internal fun SecurityQuestionCardDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecurityQuestionCard(
                    prompt = "What was the name of your first pet?",
                    isSelected = true,
                    onClick = {}
                )
                SecurityQuestionCard(
                    prompt = "What city were you born in?",
                    isSelected = false,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Security Question Card Light")
@Composable
internal fun SecurityQuestionCardLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecurityQuestionCard(
                    prompt = "What was the name of your first pet?",
                    isSelected = true,
                    onClick = {}
                )
                SecurityQuestionCard(
                    prompt = "What city were you born in?",
                    isSelected = false,
                    onClick = {}
                )
            }
        }
    }
}

