package com.mknlabs.expensetracker.feature.smsinbox.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import androidx.compose.material3.FilterChipDefaults
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.chip
import com.mknlabs.expensetracker.core.ui.theme.chipInkOff
import com.mknlabs.expensetracker.core.ui.theme.chipOutline
import com.mknlabs.expensetracker.core.ui.theme.chipSelected
import com.mknlabs.expensetracker.core.ui.theme.chipSelectedInk
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.components.PeriodChipDefaults
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxFilter
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.sms.SmsConfidence
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.TransactionDateHeader
import com.mknlabs.expensetracker.core.ui.horizontalSwipe
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.categorySoft
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.BadgeOnColor
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.transparent
import com.mknlabs.expensetracker.utils.UiText
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


internal const val PREFETCH_DISTANCE = 5

/** How long a newly detected card takes to settle into the list. */
internal const val ARRIVAL_ANIMATION_MS = 420

/** How far above its place a new card starts before sliding down into it. */
internal const val ARRIVAL_SLIDE_DP = 28f

/** A new card is never fully invisible â€” it arrives, it does not blink in. */
internal const val ARRIVAL_START_ALPHA = 0.2f

/** Slightly smaller at the start, so the card grows as it lands. */
internal const val ARRIVAL_START_SCALE = 0.94f

/** Scroll offset still counted as "the list is at its beginning". */
internal const val LIST_START_OFFSET_PX = 24

/** How long a notification-arrival highlight stays on its row. */
internal const val FOCUS_HIGHLIGHT_MS = 2_500L

/** How far a swiped card travels before the movement is damped. */
internal const val SWIPE_MAX_REVEAL_DP = 96f

/** How long a committed card takes to leave the screen. */
internal const val SWIPE_EXIT_DURATION_MS = 200

/** Extra travel beyond the card's own width so it clears the screen edge completely. */
internal const val SWIPE_EXIT_MARGIN_DP = 48f

/** Drag distance at which the delete is armed and the haptic fires. */
internal const val SWIPE_TRIGGER_THRESHOLD_DP = 56f

/** The hint only starts to appear once the card has moved this far. */
internal const val SWIPE_MIN_REVEAL_DP = 24f

/** A quick flick counts as a swipe even when the distance threshold was not reached. */
internal const val SWIPE_FLING_VELOCITY_PX_PER_SECOND = 600f

/** Movement past [SWIPE_MAX_REVEAL_DP] is resisted, so a card never flies off-screen. */
internal const val SWIPE_OVERSHOOT_RESISTANCE = 0.15f
