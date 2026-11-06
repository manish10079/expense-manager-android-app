package com.mknlabs.expensetracker.feature.transactions.ui

import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_SORT_BY
import com.mknlabs.expensetracker.data.constants.DEFAULT_SORT_ORDER
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.SortType
import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.models.TagMatchMode
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.core.ui.components.ActiveFilter
import com.mknlabs.expensetracker.core.ui.components.ActiveFilterBar
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.BrandAddFabSlot
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.FilterBottomSheet
import com.mknlabs.expensetracker.core.ui.components.FilterPillType
import com.mknlabs.expensetracker.core.ui.components.GatedAction
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.core.ui.components.SelectionHeader
import com.mknlabs.expensetracker.core.ui.components.TransactionCard
import com.mknlabs.expensetracker.core.ui.components.TransactionPeriodFilter
import com.mknlabs.expensetracker.core.ui.components.TransactionPeriodNavigator
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.core.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.core.ui.components.rememberBindAddFabToScroll
import com.mknlabs.expensetracker.core.ui.horizontalSwipe
import com.mknlabs.expensetracker.core.ui.models.TransactionListItemUi
import com.mknlabs.expensetracker.core.ui.models.buildTransactionsFeed
import com.mknlabs.expensetracker.core.ui.theme.CardLight
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.hairline
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.isDark

import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.utils.TransactionSwipeAction
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.isRecurringTransaction
import com.mknlabs.expensetracker.utils.transactionSwipeAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Swipe-to-duplicate constants (dp are converted to px via LocalDensity).
internal const val SWIPE_MAX_REVEAL_DP = 96f
internal const val SWIPE_MIN_REVEAL_DP = 24f
// Finger distance (dp) that must be exceeded to trigger the duplicate. Kept
// below SWIPE_MAX_REVEAL_DP so the trigger always lands inside the card's
// 1:1 drag range regardless of screen density.
internal const val SWIPE_TRIGGER_THRESHOLD_DP = 56f
// Release velocity (px/s) that triggers the duplicate even below the distance threshold.
internal const val SWIPE_FLING_VELOCITY_PX_PER_SECOND = 600f
// Extra finger movement beyond the reveal point is resisted (rubber-band feel).
internal const val SWIPE_OVERSHOOT_RESISTANCE = 0.25f
// Premium swipe exit & gap-collapse animation (DELETE only): on commit the card
// keeps flying toward the screen edge for SWIPE_EXIT_DURATION_MS while still
// occupying its layout slot (so a clearly visible gap is left behind), the gap
// is held for SWIPE_GAP_HOLD_MS, and only then is the real delete fired — the
// remaining cards collapse the gap via the list's Modifier.animateItem().
internal const val SWIPE_EXIT_DURATION_MS = 180
internal const val SWIPE_GAP_HOLD_MS = 120
// Extra distance beyond the card's own width so it fully clears the screen edge.
internal const val SWIPE_EXIT_MARGIN_DP = 48f
// Subtle scale-down during the exit (1.0 -> ~0.98) so the card feels physically
// dismissed without looking like it shrinks dramatically.
internal const val SWIPE_EXIT_SCALE = 0.98f

/**
 * Wraps a transaction card so it slides horizontally with the finger, reveals
 * the "Duplicate" / "Delete" actions peeking from behind either edge, and
 * springs back to centre on release. A swipe past [SWIPE_TRIGGER_THRESHOLD_DP]
 * — or a fast fling past [SWIPE_FLING_VELOCITY_PX_PER_SECOND] — resolves via
 * [transactionSwipeAction] and fires [onSwipe]: a right-to-left swipe yields
 * [TransactionSwipeAction.Duplicate], a left-to-right one yields
 * [TransactionSwipeAction.Delete].
 *
 * Premium swipe exit & gap-collapse (DELETE only): when the delete commits the
 * card does NOT vanish — it keeps flying in the swipe direction until it has
 * cleared the screen edge, shrinking imperceptibly ([SWIPE_EXIT_SCALE]) and
 * deliberately NOT fading, so the card is seen leaving rather than dissolving
 * short of the edge. It still occupies its layout slot so a clearly visible
 * temporary gap is left behind. After a short hold ([SWIPE_GAP_HOLD_MS]) the real
 * delete is fired;
 * the remaining cards then close the gap smoothly via the parent list's
 * [androidx.compose.foundation.lazy.animateItem] placement animation. Gesture
 * state (this composable) stays separate from data state (the ViewModel): the
 * item remains in the list until the delete lands, and the [isRemoving] guard
 * makes a double-delete impossible.
 */
@Composable
internal fun SwipeableDuplicateCard(
    enabled: Boolean,
    onSwipe: (TransactionSwipeAction) -> Unit,
    cardContent: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val maxRevealPx = with(density) { SWIPE_MAX_REVEAL_DP.dp.toPx() }
    val minRevealPx = with(density) { SWIPE_MIN_REVEAL_DP.dp.toPx() }
    val triggerThresholdPx = with(density) { SWIPE_TRIGGER_THRESHOLD_DP.dp.toPx() }
    val exitMarginPx = with(density) { SWIPE_EXIT_MARGIN_DP.dp.toPx() }
    val swipeOffset = remember { Animatable(0f) }

    // Premium exit state — kept here (gesture/UI state), NOT in the data layer.
    // While dismissProgress runs 0 -> 1 the card keeps its layout slot and its
    // translation continues toward the screen edge, alpha fades out and scale
    // shrinks imperceptibly. The flags below guard against double-delete /
    // double-duplicate from repeated callbacks, recomposition or rapid gestures.
    val dismissProgress = remember { Animatable(0f) }
    var dismissStartX by remember { mutableStateOf(0f) }
    var dismissTargetX by remember { mutableStateOf(0f) }
    var isRemoving by remember { mutableStateOf(false) }
    var isDuplicateCommitting by remember { mutableStateOf(false) }
    // True only once the real delete has been fired — lets the DisposableEffect
    // safety net below re-fire it if the card is disposed mid-exit without
    // ever double-deleting.
    var deleteFired by remember { mutableStateOf(false) }
    var cardWidthPx by remember { mutableStateOf(0f) }

    // Swipes are ignored while a removal/duplicate commit is in flight. Keying
    // the gesture modifier on this flag keeps its callbacks fresh whenever it
    // flips (avoids stale closures across recompositions).
    val gesturesEnabled = enabled && !isRemoving && !isDuplicateCommitting

    // Keep the safety net below on the latest callback (avoids capturing a stale
    // first-composition closure that could carry outdated transaction data).
    val currentOnSwipe by rememberUpdatedState(onSwipe)

    DisposableEffect(Unit) {
        onDispose {
            // Safety net: the card can leave composition mid-exit (scrolled out
            // of view, filters changed, screen changed). A committed delete must
            // still land exactly once.
            if (isRemoving && !deleteFired) {
                currentOnSwipe(transactionSwipeAction(isLeftSwipe = false))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { cardWidthPx = it.width.toFloat() }
    ) {
        // Action labels behind the card — the one on the side the card slides
        // away from peeks out as the card moves (alpha follows the card's
        // actual position, so no recomposition per drag frame). Swiping right
        // reveals Delete; swiping left reveals Duplicate. Hidden entirely once
        // a delete starts flying out (they must not linger in the gap).
        SwipeActionLabel(
            icon = Icons.Filled.Delete,
            text = stringResource(R.string.label_delete_confirm),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .graphicsLayer {
                    alpha = if (isRemoving || isDuplicateCommitting) 0f else revealAlpha(
                        offsetPx = swipeOffset.value,
                        revealsWhenPositive = true,
                        maxRevealPx = maxRevealPx,
                        minRevealPx = minRevealPx
                    )
                }
        )
        SwipeActionLabel(
            icon = Icons.Filled.ContentCopy,
            text = stringResource(R.string.label_duplicate),
            tint = MaterialTheme.colorScheme.accentInk,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .graphicsLayer {
                    alpha = if (isRemoving || isDuplicateCommitting) 0f else revealAlpha(
                        offsetPx = swipeOffset.value,
                        revealsWhenPositive = false,
                        maxRevealPx = maxRevealPx,
                        minRevealPx = minRevealPx
                    )
                }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    if (isRemoving) {
                        // Card is flying out: continue from where the finger left
                        // it, all the way past the screen edge, shrinking
                        // imperceptibly to SWIPE_EXIT_SCALE. All values are driven
                        // off one Animatable inside graphicsLayer — no
                        // recomposition per frame, no per-frame allocation.
                        //
                        // Deliberately NO fade: the card has to be seen leaving the
                        // screen, not dissolving short of the edge.
                        val progress = dismissProgress.value
                        translationX = dismissStartX + (dismissTargetX - dismissStartX) * progress
                        alpha = 1f
                        scaleX = 1f - (1f - SWIPE_EXIT_SCALE) * progress
                        scaleY = 1f - (1f - SWIPE_EXIT_SCALE) * progress
                    } else if (isDuplicateCommitting) {
                        // Duplicate is on its way out: same undamped travel, so the
                        // card clears the edge instead of stopping at the reveal point.
                        translationX = swipeOffset.value
                        alpha = 1f
                        scaleX = 1f
                        scaleY = 1f
                    } else {
                        translationX = dampedTranslation(swipeOffset.value, maxRevealPx)
                        alpha = 1f
                        scaleX = 1f
                        scaleY = 1f
                    }
                }
                .horizontalSwipe(
                    key = gesturesEnabled,
                    threshold = triggerThresholdPx,
                    flingVelocityThreshold = SWIPE_FLING_VELOCITY_PX_PER_SECOND,
                    onDragOffset = { rawOffset ->
                        // Don't slide the card while in multi-select mode or after
                        // an action has been committed.
                        if (gesturesEnabled) {
                            scope.launch { swipeOffset.snapTo(rawOffset) }
                        }
                    },
                    onThresholdCrossed = {
                        // Subtle confirmation the moment the commit threshold is
                        // crossed — horizontalSwipe fires it once per gesture, so
                        // no repeated haptics while dragging.
                        if (gesturesEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    onSwipeLeft = {
                        if (gesturesEnabled) {
                            // Duplicate: the card leaves the screen exactly as it does
                            // on a delete — it has to be seen going out — and then
                            // comes straight back, because a duplicate ADDS a
                            // transaction: the swiped one is still in the list and
                            // would otherwise have to snap back from the edge. The
                            // copy places itself beside it via the list's animateItem.
                            isDuplicateCommitting = true
                            val exitTarget = -(cardWidthPx + exitMarginPx)
                            scope.launch {
                                swipeOffset.animateTo(
                                    targetValue = exitTarget,
                                    animationSpec = tween(
                                        durationMillis = SWIPE_EXIT_DURATION_MS,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                                onSwipe(transactionSwipeAction(isLeftSwipe = true))
                                swipeOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                                isDuplicateCommitting = false
                            }
                        }
                    },
                    onSwipeRight = {
                        if (gesturesEnabled) {
                            // Delete: animate the exit FIRST while the card still
                            // occupies its layout space (visible temporary gap) and
                            // only fire the real delete after the card is gone and
                            // the gap has been held.
                            isRemoving = true
                            deleteFired = false
                            dismissStartX = swipeOffset.value
                            dismissTargetX = cardWidthPx + exitMarginPx
                            scope.launch {
                                dismissProgress.snapTo(0f)
                                dismissProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = SWIPE_EXIT_DURATION_MS,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                                delay(SWIPE_GAP_HOLD_MS.toLong())
                                deleteFired = true
                                onSwipe(transactionSwipeAction(isLeftSwipe = false))
                            }
                        }
                    },
                    onDragEnd = {
                        // Below-threshold release: spring the card back to centre.
                        // Skipped while a card is flying out (delete or duplicate) —
                        // the exit coroutine owns it then.
                        if (!isRemoving && !isDuplicateCommitting) {
                            scope.launch {
                                swipeOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                                isDuplicateCommitting = false
                            }
                        }
                    },
                    onDragCancel = {
                        if (!isRemoving && !isDuplicateCommitting) {
                            scope.launch {
                                swipeOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                                isDuplicateCommitting = false
                            }
                        }
                    }
                )
        ) {
            cardContent()
        }
    }
}

@Composable
internal fun SwipeActionLabel(
    icon: ImageVector,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = tint,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

/**
 * Card follows the finger 1:1 up to [maxRevealPx]; extra movement beyond it is
 * resisted so the card doesn't fly off-screen.
 */
internal fun dampedTranslation(rawOffsetPx: Float, maxRevealPx: Float): Float {
    val clamped = rawOffsetPx.coerceIn(-maxRevealPx, maxRevealPx)
    return clamped + (rawOffsetPx - clamped) * SWIPE_OVERSHOOT_RESISTANCE
}

/**
 * Label opacity for the edge revealed as the card slides: [revealsWhenPositive]
 * is true for the left label (shown when the card slides right) and false for
 * the right label. Fades in after [minRevealPx] and is fully opaque at
 * [maxRevealPx], following the card's damped position.
 */
internal fun revealAlpha(
    offsetPx: Float,
    revealsWhenPositive: Boolean,
    maxRevealPx: Float,
    minRevealPx: Float
): Float {
    val rawReveal = if (revealsWhenPositive) offsetPx else -offsetPx
    if (rawReveal <= 0f) return 0f
    val clamped = rawReveal.coerceIn(0f, maxRevealPx)
    val reveal = clamped + (rawReveal - clamped) * SWIPE_OVERSHOOT_RESISTANCE
    if (reveal <= minRevealPx) return 0f
    return ((reveal - minRevealPx) / (maxRevealPx - minRevealPx)).coerceIn(0f, 1f)
}
