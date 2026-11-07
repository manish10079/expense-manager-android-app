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


@Composable
internal fun SwipeToDeleteCard(
    enabled: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val maxRevealPx = with(density) { SWIPE_MAX_REVEAL_DP.dp.toPx() }
    val minRevealPx = with(density) { SWIPE_MIN_REVEAL_DP.dp.toPx() }
    val thresholdPx = with(density) { SWIPE_TRIGGER_THRESHOLD_DP.dp.toPx() }
    val exitMarginPx = with(density) { SWIPE_EXIT_MARGIN_DP.dp.toPx() }
    val offset = remember { Animatable(0f) }

    // While the card is flying out it is no longer following a finger, and the delete has
    // to land exactly once â€” even if the list drops the card mid-flight (it can, since
    // the row is removed as soon as the write is queued).
    var isExiting by remember { mutableStateOf(false) }
    var exitFired by remember { mutableStateOf(false) }
    var cardWidthPx by remember { mutableStateOf(0f) }
    val currentOnDelete by rememberUpdatedState(onDelete)

    DisposableEffect(Unit) {
        onDispose {
            // Safety net for the card leaving composition before its exit finished.
            if (isExiting && !exitFired) currentOnDelete()
        }
    }

    val gesturesEnabled = enabled && !isExiting

    /**
     * Sends the card off the screen in [direction] (+1 right, -1 left) before the delete
     * is reported, so the row is never removed while the card is still visible.
     */
    val flyOut: (Float) -> Unit = { direction ->
        if (!isExiting) {
            isExiting = true
            scope.launch {
                offset.animateTo(
                    targetValue = direction * (cardWidthPx + exitMarginPx),
                    animationSpec = tween(
                        durationMillis = SWIPE_EXIT_DURATION_MS,
                        easing = FastOutSlowInEasing
                    )
                )
                exitFired = true
                currentOnDelete()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { cardWidthPx = it.width.toFloat() }
    ) {
        // One hint per side. Their opacity is read from the drag inside graphicsLayer, so
        // following the finger never recomposes the card.
        SwipeDeleteHint(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    alpha = if (isExiting) 0f else revealAlpha(offset.value, true, maxRevealPx, minRevealPx)
                }
        )
        SwipeDeleteHint(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .graphicsLayer {
                    alpha = if (isExiting) 0f else revealAlpha(offset.value, false, maxRevealPx, minRevealPx)
                }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                // A row is flat, so the hint behind it needs something opaque to hide
                // behind. The card used to be that; without this the delete label would
                // show through the row as it slides.
                .background(MaterialTheme.colorScheme.background)
                .graphicsLayer {
                    // Undamped while flying out: the card must clear the screen edge,
                    // not stop at the reveal point the drag is limited to.
                    translationX = if (isExiting) {
                        offset.value
                    } else {
                        dampedTranslation(offset.value, maxRevealPx)
                    }
                }
                .horizontalSwipe(
                    key = gesturesEnabled,
                    threshold = thresholdPx,
                    flingVelocityThreshold = SWIPE_FLING_VELOCITY_PX_PER_SECOND,
                    onDragOffset = { raw ->
                        if (gesturesEnabled) scope.launch { offset.snapTo(raw) }
                    },
                    onThresholdCrossed = {
                        // Fires once per gesture, at the point the row would be deleted.
                        if (gesturesEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    // Right-to-left sends the card off the left edge, left-to-right off
                    // the right edge; the delete is reported once it is gone.
                    onSwipeLeft = { if (gesturesEnabled) flyOut(-1f) },
                    onSwipeRight = { if (gesturesEnabled) flyOut(1f) },
                    onDragEnd = {
                        if (!isExiting) scope.launch { offset.animateTo(0f) }
                    },
                    onDragCancel = {
                        if (!isExiting) scope.launch { offset.animateTo(0f) }
                    }
                )
        ) {
            content()
        }
    }
}

/** The delete affordance peeking out from behind a swiped-away card. */
@Composable
internal fun SwipeDeleteHint(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.action_delete),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * One detection, laid out as the mock txn card: icon + title + suggested chip,
 * amount and time on the trailing edge, SMS snippet underneath.
 */
internal fun dampedTranslation(rawOffsetPx: Float, maxRevealPx: Float): Float {
    val clamped = rawOffsetPx.coerceIn(-maxRevealPx, maxRevealPx)
    return clamped + (rawOffsetPx - clamped) * SWIPE_OVERSHOOT_RESISTANCE
}

/**
 * Opacity of the hint revealed by the card's movement: [revealsWhenPositive] is true for
 * the start-side hint (shown while the card slides right) and false for the end side.
 */
internal fun revealAlpha(
    offsetPx: Float,
    revealsWhenPositive: Boolean,
    maxRevealPx: Float,
    minRevealPx: Float
): Float {
    val revealed = if (revealsWhenPositive) offsetPx else -offsetPx
    if (revealed <= minRevealPx) return 0f
    val clamped = revealed.coerceIn(0f, maxRevealPx)
    val damped = clamped + (revealed - clamped) * SWIPE_OVERSHOOT_RESISTANCE
    return ((damped - minRevealPx) / (maxRevealPx - minRevealPx)).coerceIn(0f, 1f)
}
