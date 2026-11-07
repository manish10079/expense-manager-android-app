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

/**
 * Route: owns the ViewModel, hands the display context over once, surfaces one-shot
 * events as snackbars and forwards every user intent.
 *
 * Pure wiring â€” no layout decisions live here (GEMINI Â§2).
 */
@Composable
fun SmsInboxRoute(
    categories: List<CategoryType>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    /** The user's own date format, which the day separators follow. */
    dateFormatPattern: String,
    /** The user's own time format, which each row's own clock follows. */
    timeFormat: String,
    onBackClick: () -> Unit,
    /**
     * A live detection was tapped: open the Add Transaction screen prefilled with it. The
     * detection is only filed â€” and only leaves this list â€” when the user presses Add there.
     */
    onReviewInAddTransaction: (SmsInboxItemUi) -> Unit = {},
    /** An already-filed detection was tapped: open the transaction it created. */
    onOpenFiledTransaction: (String) -> Unit = {},
    /**
     * Set when the screen was opened from a detected-SMS notification: the row the
     * card was about, and whether its Edit action asked for the correction dialog.
     */
    focusDetectionId: String? = null,
    focusOpenEditor: Boolean = false,
    /** Releases the request once the ViewModel has taken it, so it fires only once. */
    onFocusConsumed: () -> Unit = {},
    viewModel: SmsInboxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // The ViewModel formats amounts and resolves category names, so it needs the same
    // currency + formatting context the rest of the app renders with.
    LaunchedEffect(categories, currencyId, amountFormatPreferences, dateFormatPattern, timeFormat) {
        viewModel.updateDisplayContext(
            categories = categories,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat
        )
    }

    // Opening from a notification brings one row into view. Keyed on the id (and the
    // edit flag) so a second tap on another notification re-focuses, while a plain
    // recomposition does not.
    LaunchedEffect(focusDetectionId, focusOpenEditor) {
        if (focusDetectionId != null) {
            viewModel.onFocusRequested(focusDetectionId, focusOpenEditor)
            // Handed over: without this, returning to the inbox later would re-focus the
            // row an old notification pointed at.
            onFocusConsumed()
        }
    }

    // The highlight is an arrival cue, not a state: it releases itself once the list
    // has had time to scroll, so the row never lingers looking selected.
    LaunchedEffect(uiState.focusedId) {
        if (uiState.focusedId != null) {
            delay(FOCUS_HIGHLIGHT_MS)
            viewModel.onFocusHighlightFinished()
        }
    }

    // Snackbar text is resolved here, where a Context exists â€” the ViewModel only ever
    // hands over a resource id. `UiText.asString()` is @Composable, so it cannot be
    // used from inside the collecting coroutine; the Context is the way to render a
    // resource id off the composition thread.
    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            when (event) {
                is SmsInboxEvent.Message -> snackbarHostState.showSnackbar(context.resolve(event.text))

                // The only message with an action: a swipe deleted something, and the
                // offer to bring it back lives exactly as long as this snackbar.
                is SmsInboxEvent.Deleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.msg_sms_inbox_swiped_away, event.count),
                        actionLabel = context.getString(R.string.label_undo),
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoDelete()
                }

                // Leaving this screen is the ViewModel's whole job here: the composable
                // only knows where to go, never what a tap on a detection means.
                is SmsInboxEvent.ReviewInAddTransaction -> onReviewInAddTransaction(event.item)

                is SmsInboxEvent.OpenFiledTransaction -> onOpenFiledTransaction(event.transactionId)
            }
        }
    }

    SmsInboxContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        availableCategories = categories,
        onFilterSelected = viewModel::onFilterSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onLoadMore = viewModel::onLoadMore,
        onDetectionOpened = viewModel::onDetectionOpened,
        onDetectionsVisible = viewModel::onDetectionsVisible,
        onToggleSelection = viewModel::onToggleSelection,
        onSelectAll = viewModel::onSelectAll,
        onClearSelection = viewModel::onClearSelection,
        onAdd = viewModel::onAddDetection,
        onEditBeforeAdd = viewModel::onOpenEditor,
        onRequestDelete = viewModel::onRequestDelete,
        onRequestDeleteSelected = viewModel::onRequestDeleteSelected,
        onSwipeDelete = viewModel::onSwipeDelete,
        onArrivalHandled = viewModel::onArrivalHandled,
        onClearAll = viewModel::onClearAllVisible,
        onCancelDelete = viewModel::onCancelDelete,
        onConfirmDelete = viewModel::onConfirmDelete,
        onEditorAmountChanged = viewModel::onEditorAmountChanged,
        onEditorNoteChanged = viewModel::onEditorNoteChanged,
        onEditorCategoryChanged = viewModel::onEditorCategoryChanged,
        onCancelEditor = viewModel::onCancelEditor,
        onConfirmEditor = viewModel::onConfirmEditor,
        onBackClick = onBackClick
    )
}

/**
 * Content: a pure function of [uiState] and callbacks â€” no ViewModel, no state
 * collection â€” so it renders in previews and in tests without Hilt.
 */
@Composable
internal fun SmsInboxContent(
    uiState: SmsInboxUiState,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    availableCategories: List<CategoryType> = emptyList(),
    onFilterSelected: (SmsInboxFilter) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onDetectionOpened: (SmsInboxItemUi) -> Unit = {},
    onDetectionsVisible: (List<String>) -> Unit = {},
    onToggleSelection: (String) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onAdd: (String) -> Unit = {},
    onEditBeforeAdd: (SmsInboxItemUi) -> Unit = {},
    onRequestDelete: (String) -> Unit = {},
    onRequestDeleteSelected: () -> Unit = {},
    onSwipeDelete: (String) -> Unit = {},
    onArrivalHandled: (String) -> Unit = {},
    onClearAll: () -> Unit = {},
    onCancelDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onEditorAmountChanged: (String) -> Unit = {},
    onEditorNoteChanged: (String) -> Unit = {},
    onEditorCategoryChanged: (Int) -> Unit = {},
    onCancelEditor: () -> Unit = {},
    onConfirmEditor: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.appScreenBackground(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.isSelectionMode) {
                val allSelected = uiState.detections.let { it.isNotEmpty() && it.all { row -> row.id in uiState.selectedIds } }
                BulkActionBar(
                    selectedCount = uiState.selectedIds.size,
                    allSelected = allSelected,
                    onClose = onClearSelection,
                    onToggleSelectAll = { if (allSelected) onClearSelection() else onSelectAll() },
                    onDelete = onRequestDeleteSelected
                )
            } else if (uiState.hasItems) {
                ClearAllFooter(onClearAll = onClearAll)
            }
        },
        // "Clear all" is only offered when there is something to clear, and it steps
        // aside in selection mode â€” that bar already carries its own Delete. It wipes in
        // from the start edge and wipes back out the same way.
    ) { padding ->
        val enter = rememberSectionEnterAlphas(4)
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // AppHeader carries no inset of its own â€” every screen pads it â€” so without this the
            // back button sat on the window edge, at x0 where the rest of the app puts it at 20dp.
            AppHeader(
                title = stringResource(id = R.string.label_sms_inbox_title),
                onBackClick = onBackClick,
                modifier = Modifier
                    .padding(horizontal = Dimens.ScreenPadding)
                    .alpha(enter[0])
            )

            Box(Modifier.alpha(enter[1])) {
            SearchField(
                query = uiState.searchQuery,
                onQueryChanged = onSearchQueryChanged
            )
            }

            // A Column, not a Box: this section holds two things, and a Box stacks its children at
            // a single origin. The hint was drawn on top of the filter chips rather than under
            // them â€” its sentence covered every chip past the first, and only the selected "All"
            // stayed in the accessibility tree because the rest were obscured by it.
            Column(Modifier.alpha(enter[2])) {
            FilterRow(
                selected = uiState.filter,
                onFilterSelected = onFilterSelected
            )

            // The cards carry no decision buttons of their own any more, so the one thing a
            // user has to know is what a tap does. It sits under the filters, where it is
            // read once, instead of being repeated on every card.
            InboxHint()
            }

            Box(Modifier.alpha(enter[3])) {
            when {
                uiState.isEmpty -> EmptyInbox(filter = uiState.filter)
                else -> InboxList(
                    uiState = uiState,
                    availableCategories = availableCategories,
                    onLoadMore = onLoadMore,
                    onDetectionOpened = onDetectionOpened,
                    onDetectionsVisible = onDetectionsVisible,
                    onToggleSelection = onToggleSelection,
                    onDelete = onRequestDelete,
                    onSwipeDelete = onSwipeDelete,
                    onArrivalHandled = onArrivalHandled
                )
            }
            }
        }
    }

    if (uiState.pendingDeleteIds.isNotEmpty()) {
        ModernConfirmationDialog(
            onDismissRequest = onCancelDelete,
            title = stringResource(id = R.string.label_sms_inbox_delete_title),
            subtitle = stringResource(id = R.string.label_sms_inbox_delete_subtitle),
            text = stringResource(id = R.string.label_sms_inbox_delete_body),
            confirmButtonText = stringResource(id = R.string.action_delete),
            onConfirm = onConfirmDelete,
            headerIcon = Icons.Rounded.Delete,
            headerIconContentDescription = stringResource(id = R.string.desc_delete)
        )
    }

    uiState.editor?.let { editor ->
        EditDetectionDialog(
            editor = editor,
            categories = availableCategories,
            onAmountChanged = onEditorAmountChanged,
            onNoteChanged = onEditorNoteChanged,
            onCategoryChanged = onEditorCategoryChanged,
            onDismiss = onCancelEditor,
            onConfirm = onConfirmEditor
        )
    }
}
