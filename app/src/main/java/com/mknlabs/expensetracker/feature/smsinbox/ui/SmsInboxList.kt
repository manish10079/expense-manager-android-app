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
internal fun InboxList(
    uiState: SmsInboxUiState,
    availableCategories: List<CategoryType>,
    onLoadMore: () -> Unit,
    onDetectionOpened: (SmsInboxItemUi) -> Unit,
    onDetectionsVisible: (List<String>) -> Unit,
    onToggleSelection: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSwipeDelete: (String) -> Unit,
    onArrivalHandled: (String) -> Unit
) {
    val listState = rememberLazyListState()

    // A new detection only plays its entrance while the list is at the beginning: a card
    // appearing above a list the user has scrolled down would animate out of sight, and
    // the inbox must never jump under them.
    val isAtListStart by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex <= 1 &&
                listState.firstVisibleItemScrollOffset <= LIST_START_OFFSET_PX
        }
    }

    // Bring a notification's row into view. It may not be in the loaded page yet, in
    // which case this simply does nothing â€” the user still sees the inbox rather than
    // an empty screen. The index is looked up in the render list, since a date separator
    // means a row's position in the list is no longer its position among the detections.
    LaunchedEffect(uiState.focusedId, uiState.listItems) {
        val focusedId = uiState.focusedId ?: return@LaunchedEffect
        val index = uiState.listItems.indexOfFirst { it.id == focusedId }
        if (index >= 0) listState.animateScrollToItem(index)
    }

    // Seeing is reading. Reported from what the list is actually showing rather than from
    // what was fetched, so a page loaded off-screen does not mark itself read, and the
    // bell count empties exactly as fast as the user scrolls through the cards.
    LaunchedEffect(listState, uiState.listItems) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .mapNotNull { visible -> uiState.listItems.getOrNull(visible.index) }
        }
            .distinctUntilChanged()
            .collect { rows ->
                onDetectionsVisible(
                    rows.filterIsInstance<SmsInboxListItemUi.Detection>().map { it.item.id }
                )
            }
    }

    // Load the next page a little before the user reaches the bottom, so scrolling
    // never stalls on a spinner.
    LaunchedEffect(listState, uiState.canLoadMore) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisible ->
                if (uiState.canLoadMore && lastVisible >= uiState.listItems.size - PREFETCH_DISTANCE) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        // The bottom inset keeps the last row clear of the floating Clear all button.
        contentPadding = PaddingValues(
            start = Dimens.ScreenPadding,
            end = Dimens.ScreenPadding,
            top = 12.dp,
            bottom = 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = uiState.listItems, key = { it.id }) { row ->
            when (row) {
                is SmsInboxListItemUi.DateHeader -> TransactionDateHeader(
                    dayLabel = row.dayLabel.asString(),
                    dateLabel = row.dateLabel
                )

                is SmsInboxListItemUi.Detection -> {
                    val item = row.item
                    // Swiping is off while picking rows for a bulk action: a horizontal
                    // drag there would delete something the user only meant to select.
                    SwipeToDeleteCard(
                        enabled = !uiState.isSelectionMode,
                        onDelete = { onSwipeDelete(item.id) },
                        modifier = Modifier.animateItem()
                    ) {
                        DetectionCard(
                            item = item,
                            category = availableCategories.firstOrNull { it.id == item.categoryId },
                            isFocused = item.id == uiState.focusedId,
                            // Arriving and visible here: play it. Arriving but off-screen:
                            // just consume it, so scrolling back up later cannot re-run it.
                            isArriving = item.id in uiState.arrivalIds && isAtListStart,
                            isArrivalPending = item.id in uiState.arrivalIds,
                            onArrivalHandled = { onArrivalHandled(item.id) },
                            onClick = {
                                if (uiState.isSelectionMode) {
                                    onToggleSelection(item.id)
                                } else {
                                    onDetectionOpened(item)
                                }
                            },
                            onLongClick = { onToggleSelection(item.id) },
                            onDelete = { onDelete(item.id) }
                        )
                    }
                }
            }
        }

        if (uiState.canLoadMore || uiState.isWorking) {
            item(key = "loading") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.accentInk
                    )
                }
            }
        }
    }
}

/**
 * Swipe a detection either way to delete it.
 *
 * Both directions are accepted on purpose: the inbox has only one swipe action, so
 * refusing one side would just leave a dead gesture. The delete is reversible through the
 * snackbar's Undo, which is the safety net here instead of a confirmation dialog â€” a
 * mis-swipe costs a tap, not a row.
 *
 * On commit the card does not simply disappear: it keeps travelling in the swipe
 * direction until it has cleared the screen edge, so the gesture is seen finishing what
 * it started. The removal then closes the gap via the list's placement animation.
 */
@Composable
internal fun DetectionCard(
    item: SmsInboxItemUi,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    category: CategoryType? = null,
    /** True for the row a notification tap asked for, briefly after arrival. */
    isFocused: Boolean = false,
    /** True when this card must play the "a new message just arrived" entrance. */
    isArriving: Boolean = false,
    /** True while this row still counts as an unrevealed arrival. */
    isArrivalPending: Boolean = false,
    onArrivalHandled: () -> Unit = {}
) {
    val currentOnArrivalHandled by rememberUpdatedState(onArrivalHandled)

    LaunchedEffect(isArrivalPending) {
        if (isArrivalPending) {
            currentOnArrivalHandled()
        }
    }

    val playsArrival = remember { isArriving }
    val arrival = remember { Animatable(if (playsArrival) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (playsArrival) {
            arrival.animateTo(1f, tween(ARRIVAL_ANIMATION_MS, easing = FastOutSlowInEasing))
        }
    }

    val scheme = MaterialTheme.colorScheme
    val categoryColor = scheme.categoryColor(
        categoryId = item.categoryId ?: 0,
        colorHex = category?.colorHex
    )
    val categoryName = category?.name
        ?: item.categoryLabel.asString().substringAfter(": ").trim()
    val signedAmount = if (item.isIncome) "+${item.amountText}" else "-${item.amountText}"
    val amountDescription = stringResource(
        id = if (item.isIncome) {
            R.string.label_sms_inbox_income_amount
        } else {
            R.string.label_sms_inbox_expense_amount
        },
        item.amountText
    )
    val smsPrefix = stringResource(id = R.string.label_sms_inbox_sms)
    val smsText = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface.copy(alpha = 0.78f)
            )
        ) {
            append(smsPrefix)
            append(" ")
        }
        append('"')
        append(item.messagePreview)
        append('"')
    }

    val baseColors = settingsGroupCardColors()
    val cardColors = when {
        item.isSelected -> baseColors.copy(
            border = BorderStroke(1.dp, scheme.accentInk)
        )
        isFocused -> baseColors.copy(
            containerColor = scheme.primaryContainer
        )
        else -> baseColors
    }

    AppCard(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val entrance = arrival.value
                alpha = ARRIVAL_START_ALPHA + (1f - ARRIVAL_START_ALPHA) * entrance
                val scale = ARRIVAL_START_SCALE + (1f - ARRIVAL_START_SCALE) * entrance
                scaleX = scale
                scaleY = scale
                translationY = -ARRIVAL_SLIDE_DP.dp.toPx() * (1f - entrance)
            },
        shape = RoundedCornerShape(16.dp),
        colors = cardColors
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(scheme.categorySoft(categoryColor))
                            .border(
                                BorderStroke(1.dp, categoryColor.copy(alpha = 0.28f)),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category?.icon ?: Icons.Filled.QuestionMark,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(id = R.string.label_sms_inbox_suggested),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = categoryColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(scheme.categorySoft(categoryColor))
                                    .border(
                                        BorderStroke(1.dp, categoryColor.copy(alpha = 0.45f)),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = signedAmount,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isIncome) scheme.income else scheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.semantics { contentDescription = amountDescription }
                    )
                    Text(
                        text = item.timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = smsText,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun ClearAllFooter(onClearAll: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val cardColors = settingsGroupCardColors()
    val shape = RoundedCornerShape(12.dp)
    Surface(
        color = scheme.background,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.4f)))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(cardColors.containerColor)
                    .then(
                        cardColors.border?.let { Modifier.border(it, shape) } ?: Modifier
                    )
                    .clickable(onClick = onClearAll)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.action_sms_inbox_clear_all_pending),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
            }
        }
    }
}

@Composable
internal fun EmptyInbox(filter: SmsInboxFilter) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(
                id = when (filter) {
                    SmsInboxFilter.ALL -> R.string.label_sms_inbox_empty_title
                    else -> R.string.label_sms_inbox_empty_filtered_title
                }
            ),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.label_sms_inbox_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

