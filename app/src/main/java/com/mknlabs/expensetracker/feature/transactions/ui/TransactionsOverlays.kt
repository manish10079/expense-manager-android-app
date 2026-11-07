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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionsOverlays(
    uiState: TransactionsScreenUiState,
    sheetState: androidx.compose.material3.SheetState,
    scope: kotlinx.coroutines.CoroutineScope,
    showPeriodPickerState: androidx.compose.runtime.MutableState<Boolean>,
    showBottomSheetState: androidx.compose.runtime.MutableState<Boolean>,
    showDeleteConfirmationState: androidx.compose.runtime.MutableState<Boolean>,
    showRecurringDuplicateDialogState: androidx.compose.runtime.MutableState<Boolean>,
    jumpToPeriod: (Long) -> Unit,
    updateSort: (String) -> Unit,
    updateOrder: (SortType) -> Unit,
    updateDateRange: (String?) -> Unit,
    updateCustomDateRange: (Long?, Long?) -> Unit,
    toggleTransactionTypeFilter: (Int) -> Unit,
    toggleCategory: (Int) -> Unit,
    togglePaymentMode: (Int) -> Unit,
    toggleTag: (String) -> Unit,
    updateTagMatchMode: (com.mknlabs.expensetracker.models.TagMatchMode) -> Unit,
    updateMinAmount: (String) -> Unit,
    updateMaxAmount: (String) -> Unit,
    applyFilters: () -> Unit,
    resetFilters: () -> Unit,
    deleteSelectedTransactions: () -> Unit,
) {
    var showPeriodPicker by showPeriodPickerState
    var showBottomSheet by showBottomSheetState
    var showDeleteConfirmation by showDeleteConfirmationState
    var showRecurringDuplicateDialog by showRecurringDuplicateDialogState

    // Period date-jump picker
    if (showPeriodPicker) {
        val pickerMode = when (uiState.selectedPeriodFilter) {
            TransactionPeriodFilter.DAILY   -> WheelPickerMode.SINGLE_DATE
            TransactionPeriodFilter.MONTHLY -> WheelPickerMode.MONTH_YEAR
            TransactionPeriodFilter.YEARLY  -> WheelPickerMode.YEAR_ONLY
            TransactionPeriodFilter.ALL     -> WheelPickerMode.SINGLE_DATE
        }
        WheelDateTimePickerModal(
            mode = pickerMode,
            initialStartMillis = uiState.focusedPeriodTimestamp,
            onDismissRequest = { showPeriodPicker = false },
            onConfirm = { millis, _ ->
                jumpToPeriod(millis)
                showPeriodPicker = false
            }
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.76f),
            dragHandle = null,
            tonalElevation = 0.dp
        ) {
            var isSheetReady by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(10) // Small delay to let the sheet start animating
                isSheetReady = true
            }

            if (isSheetReady) {
                FilterBottomSheet(
                    selectedSort = uiState.selectedSort,
                    selectedOrder = uiState.selectedOrder,
                    selectedDateRange = uiState.selectedDateRange,
                    selectedCustomStartDate = uiState.selectedCustomStartDate,
                    selectedCustomEndDate = uiState.selectedCustomEndDate,
                    selectedTransactionTypeIds = uiState.selectedTransactionTypeIds,
                    availableCategories = uiState.availableCategories,
                    selectedCategoryIds = uiState.selectedCategoryIds,
                    paymentModes = uiState.paymentModes,
                    selectedPaymentTypeIds = uiState.selectedPaymentTypeIds,
                    availableTags = uiState.availableTags,
                    selectedTagIds = uiState.selectedTagIds,
                    selectedTagMatchMode = uiState.selectedTagMatchMode,
                    minAmount = uiState.selectedMinAmount,
                    maxAmount = uiState.selectedMaxAmount,
                    onSortChange = updateSort,
                    onOrderChange = updateOrder,
                    onDateRangeChange = updateDateRange,
                    onCustomDateRangeChange = updateCustomDateRange,
                    onTransactionTypeToggle = toggleTransactionTypeFilter,
                    onCategoryToggle = toggleCategory,
                    onPaymentModeToggle = togglePaymentMode,
                    onTagToggle = toggleTag,
                    onTagMatchModeChange = updateTagMatchMode,
                    onMinAmountChange = updateMinAmount,
                    onMaxAmountChange = updateMaxAmount,
                    onApply = {
                        applyFilters()
                        scope.launch {
                            sheetState.hide()
                            showBottomSheet = false
                        }
                    },
                    onReset = {
                        resetFilters()
                        scope.launch {
                            sheetState.hide()
                            showBottomSheet = false
                        }
                    },
                    onClose = {
                        scope.launch {
                            sheetState.hide()
                            showBottomSheet = false
                        }
                    }
                )
            } else {
                Spacer(modifier = Modifier.fillMaxWidth().height(400.dp))
            }
        }
    }

    if (showDeleteConfirmation) {
        ModernConfirmationDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = stringResource(R.string.label_delete_transactions),
            text = stringResource(R.string.msg_delete_transactions_confirm, uiState.selectedTransactionIds.size),
            confirmButtonText = stringResource(R.string.label_delete_confirm),
            onConfirm = {
                deleteSelectedTransactions()
                showDeleteConfirmation = false
            },
            headerIcon = Icons.Filled.Delete,
            dismissButtonText = stringResource(R.string.label_cancel_confirm),
            onDismiss = { showDeleteConfirmation = false }
        )
    }

    if (showRecurringDuplicateDialog) {
        ModernConfirmationDialog(
            onDismissRequest = { showRecurringDuplicateDialog = false },
            title = stringResource(R.string.title_cannot_duplicate_recurring),
            text = stringResource(R.string.msg_cannot_duplicate_recurring),
            confirmButtonText = stringResource(R.string.label_ok),
            onConfirm = { showRecurringDuplicateDialog = false },
            headerIcon = Icons.Filled.ContentCopy,
            showDismissButton = false
        )
    }

}
