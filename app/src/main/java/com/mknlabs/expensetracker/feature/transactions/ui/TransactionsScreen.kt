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
fun TransactionScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    categories: List<CategoryType> = emptyList(),
    paymentMethods: List<PaymentType> = emptyList(),
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    recurringRules: List<RecurringTransactionRule> = emptyList(),
    onBackClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    onDuplicateTransaction: (Transaction, (Transaction) -> Unit) -> Unit = { _, _ -> },
    onDeleteTransaction: (Transaction) -> Unit = {},
    onRestoreTransaction: (Transaction, RecurringTransactionRule?) -> Unit = { _, _ -> },
    isAdsEnabled: Boolean = false,
    isProUser: Boolean = false
) {
    val transactionsViewModel: TransactionsViewModel = hiltViewModel()

    LaunchedEffect(
        categories,
        paymentMethods,
        currencyId,
        amountFormatPreferences,
        dateFormatPattern,
        timeFormat,
        transactionCardCustomizationSettings
    ) {
        transactionsViewModel.updateInputs(
            categories = categories,
            paymentMethods = paymentMethods,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat,
            customizationSettings = transactionCardCustomizationSettings
        )
    }
    val uiState by transactionsViewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = transactionsViewModel.transactions.collectAsLazyPagingItems()
    // Tags for each row's info popup, keyed by transaction id. Collected here rather than
    // folded into the paged rows so a tag rename repaints the popup without reloading pages.
    val transactionTags by transactionsViewModel.transactionTags.collectAsStateWithLifecycle()
    val fundNames by transactionsViewModel.fundNames.collectAsStateWithLifecycle()

    // Paging 3 gives the screen the loaded pages; the feed builder turns them into
    // the grouped list (date headers, summaries, ads) the LazyColumn renders.
    val loadedTransactions = pagingItems.itemSnapshotList.items
    val paymentTypeNames = remember(paymentMethods) {
        paymentTypeMap.mapValues { it.value.name } + paymentMethods.associate { it.id to it.name }
    }
    val todayLabel = stringResource(R.string.label_today)
    val yesterdayLabel = stringResource(R.string.label_yesterday)
    val tomorrowLabel = stringResource(R.string.label_tomorrow)
    val fallbackCategoryName = stringResource(R.string.label_other)

    val transactionsFeed = remember(
        loadedTransactions,
        uiState.selectedPeriodFilter,
        uiState.appliedSortType,
        uiState.isFilterActive,
        uiState.summaryIncomeMinor,
        uiState.summaryExpenseMinor,
        uiState.customizationSettings,
        currencyId,
        amountFormatPreferences,
        dateFormatPattern,
        timeFormat,
        paymentTypeNames,
        categories,
        fallbackCategoryName,
        todayLabel,
        yesterdayLabel,
        tomorrowLabel,
        fundNames,
        transactionTags
    ) {
        buildTransactionsFeed(
            transactions = loadedTransactions,
            periodFilter = uiState.selectedPeriodFilter,
            sortType = uiState.appliedSortType,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat,
            paymentTypeNames = paymentTypeNames,
            categories = categories,
            customizationSettings = uiState.customizationSettings,
            isFilterActive = uiState.isFilterActive,
            pinnedIncomeMinor = uiState.summaryIncomeMinor,
            pinnedExpenseMinor = uiState.summaryExpenseMinor,
            fallbackCategoryName = fallbackCategoryName,
            todayLabel = todayLabel,
            yesterdayLabel = yesterdayLabel,
            tomorrowLabel = tomorrowLabel,
            fundNames = fundNames,
            transactionTags = transactionTags.mapValues { (_, tags) -> tags.map { it.name } }
        )
    }

    val loadedTransactionIds = remember(transactionsFeed) {
        transactionsFeed.items
            .filterIsInstance<TransactionListItemUi.TransactionRow>()
            .map { it.card.id }
            .toSet()
    }
    val pagingIndexById = remember(loadedTransactions) {
        loadedTransactions.mapIndexed { index, transaction -> transaction.id to index }.toMap()
    }

    TransactionScreenContent(
        uiState = uiState,
        transactionItems = transactionsFeed.items,
        pinnedSummary = transactionsFeed.pinnedSummary,
        pagingItems = pagingItems,
        pagingIndexById = pagingIndexById,
        transactionTags = transactionTags,
        isRefreshing = pagingItems.loadState.refresh is LoadState.Loading,
        isAppending = pagingItems.loadState.append is LoadState.Loading,
        isRefreshError = pagingItems.loadState.refresh is LoadState.Error,
        isAppendError = pagingItems.loadState.append is LoadState.Error,
        onRetry = { pagingItems.retry() },
        isSummaryLoading = uiState.isSummaryLoading,
        loadedCount = pagingItems.itemCount,
        isAdsEnabled = isAdsEnabled,
        isProUser = isProUser,
        onBackClick = onBackClick,
        onAddTransactionClick = onAddTransactionClick,
        onTransactionClick = onTransactionClick,
        recurringRules = recurringRules,
        onDuplicateTransaction = onDuplicateTransaction,
        onDeleteTransaction = onDeleteTransaction,
        onRestoreTransaction = onRestoreTransaction,
        clearSelection = transactionsViewModel::clearSelection,
        selectAll = { transactionsViewModel.selectAll(loadedTransactionIds) },
        toggleSelection = transactionsViewModel::toggleSelection,
        enterSelectionMode = transactionsViewModel::enterSelectionMode,
        updateSearchQuery = transactionsViewModel::updateSearchQuery,
        updatePeriodFilter = transactionsViewModel::updatePeriodFilter,
        navigatePeriod = transactionsViewModel::navigatePeriod,
        jumpToPeriod = transactionsViewModel::jumpToPeriod,
        updateSort = transactionsViewModel::updateSort,
        updateOrder = transactionsViewModel::updateOrder,
        updateDateRange = transactionsViewModel::updateDateRange,
        updateCustomDateRange = transactionsViewModel::updateCustomDateRange,
        toggleTransactionTypeFilter = transactionsViewModel::toggleTransactionTypeFilter,
        toggleCategory = transactionsViewModel::toggleCategory,
        togglePaymentMode = transactionsViewModel::togglePaymentMode,
        toggleTag = transactionsViewModel::toggleTag,
        updateTagMatchMode = transactionsViewModel::updateTagMatchMode,
        updateMinAmount = transactionsViewModel::updateMinAmount,
        updateMaxAmount = transactionsViewModel::updateMaxAmount,
        applyFilters = transactionsViewModel::applyFilters,
        resetFilters = transactionsViewModel::resetFilters,
        deleteSelectedTransactions = transactionsViewModel::deleteSelectedTransactions,
        selectAllInQuery = transactionsViewModel::selectAllInQuery
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TransactionScreenContent(
    uiState: TransactionsScreenUiState,
    transactionItems: List<TransactionListItemUi> = emptyList(),
    pinnedSummary: TransactionListItemUi.SummaryCard? = null,
    pagingItems: LazyPagingItems<Transaction>? = null,
    pagingIndexById: Map<String, Int> = emptyMap(),
    // Tags keyed by transaction id, for each row's info popup. Empty by default so the
    // previews and any other caller render without tags.
    transactionTags: Map<String, List<Tag>> = emptyMap(),
    isRefreshing: Boolean = false,
    isAppending: Boolean = false,
    isRefreshError: Boolean = false,
    isAppendError: Boolean = false,
    onRetry: () -> Unit = {},
    isSummaryLoading: Boolean = false,
    loadedCount: Int = 0,
    isAdsEnabled: Boolean,
    isProUser: Boolean = false,
    onBackClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    recurringRules: List<RecurringTransactionRule> = emptyList(),
    onDuplicateTransaction: (Transaction, (Transaction) -> Unit) -> Unit = { _, _ -> },
    onDeleteTransaction: (Transaction) -> Unit = {},
    onRestoreTransaction: (Transaction, RecurringTransactionRule?) -> Unit = { _, _ -> },
    clearSelection: () -> Unit,
    selectAll: () -> Unit,
    toggleSelection: (String) -> Unit,
    enterSelectionMode: (String) -> Unit,
    updateSearchQuery: (String) -> Unit,
    updatePeriodFilter: (TransactionPeriodFilter) -> Unit,
    navigatePeriod: (Int) -> Unit,
    jumpToPeriod: (Long) -> Unit,
    updateSort: (String) -> Unit,
    updateOrder: (SortType) -> Unit,
    updateDateRange: (String?) -> Unit,
    updateCustomDateRange: (Long?, Long?) -> Unit,
    toggleTransactionTypeFilter: (Int) -> Unit,
    toggleCategory: (Int) -> Unit,
    togglePaymentMode: (Int) -> Unit,
    toggleTag: (String) -> Unit,
    updateTagMatchMode: (TagMatchMode) -> Unit,
    updateMinAmount: (String) -> Unit,
    updateMaxAmount: (String) -> Unit,
    applyFilters: () -> Unit,
    resetFilters: () -> Unit,
    deleteSelectedTransactions: () -> Unit,
    selectAllInQuery: () -> Unit
) {
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val searchFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()
    rememberBindAddFabToScroll(lazyListState)
    val enter = rememberSectionEnterAlphas(4)
    var searchBarBounds by remember { mutableStateOf<Rect?>(null) }

    // Paging 3 loads the next page automatically once the list scrolls near the
    // end of the loaded data, so there is no scroll listener here anymore.

    val emptyTransactionMessages = stringArrayResource(R.array.empty_transaction_messages).toList()

    val emptyTransactionMessage = remember(
        uiState.selectedPeriodFilter,
        uiState.selectedPeriodLabel,
        uiState.searchQuery,
        uiState.selectedSort,
        uiState.selectedOrder,
        uiState.isSelectionMode,
        uiState.selectedTransactionIds
    ) {
        emptyTransactionMessages.random()
    }

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showRecurringDuplicateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessage = stringResource(R.string.msg_transaction_deleted)
    val duplicatedMessage = stringResource(R.string.msg_transaction_duplicated)
    val undoLabel = stringResource(R.string.label_undo)

    // Swiping left (right-to-left) on a transaction card replicates it, swiping
    // right (left-to-right) soft-deletes it and offers Undo via a snackbar.
    // Transactions that are part of a recurring series — the main recurring
    // transaction or one auto-created by it — cannot be duplicated and show an
    // explanatory dialog.
    val onSwipeToDuplicate: (Transaction) -> Unit = { transaction ->
        if (!uiState.isSelectionMode) {
            if (isRecurringTransaction(transaction, recurringRules)) {
                showRecurringDuplicateDialog = true
            } else {
                // Duplicate now, then offer Undo: the callback delivers the exact
                // copy that was persisted (with its fresh id) so Undo can
                // soft-delete precisely that transaction.
                onDuplicateTransaction(transaction) { created ->
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = duplicatedMessage,
                            actionLabel = undoLabel,
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onDeleteTransaction(created)
                        }
                    }
                }
            }
        }
    }

    val onSwipeToDelete: (Transaction) -> Unit = { transaction ->
        // NOTE: no isSelectionMode gate here on purpose. The swipe gesture is
        // already disabled while in multi-select mode (SwipeableDuplicateCard
        // receives enabled = !isSelectionMode), so any delete reaching this
        // callback was committed before selection mode could interfere — it must
        // always land. Re-checking the flag at fire time (which happens ~300 ms
        // after release, once the exit animation has played) would silently drop
        // the delete if the user entered selection mode meanwhile, leaving an
        // invisible card stuck in the list.
        //
        // Soft-delete; Undo re-upserts the transaction (and its recurring rule,
        // if it was a template) with isDeleted = false.
        val rule = recurringRules.firstOrNull { it.transactionId == transaction.id }
        onDeleteTransaction(transaction)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                onRestoreTransaction(transaction, rule)
            }
        }
    }

    BackHandler(enabled = uiState.isSelectionMode) {
        clearSelection()
    }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            searchFocusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(isSearchExpanded, searchBarBounds) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Final)
                    val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
                    if (up != null && isSearchExpanded) {
                        val tappedInsideSearchBar = searchBarBounds?.contains(up.position) == true
                        if (!tappedInsideSearchBar) {
                            closeSearchBar(
                                focusManager = focusManager,
                                onSearchQueryChange = updateSearchQuery,
                                onSearchExpandedChange = { isSearchExpanded = it }
                            )
                        }
                    }
                }
            }
            .padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = Dimens.HeaderSpacing, bottom = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = uiState.isSelectionMode,
                label = "HeaderTransition",
                modifier = Modifier.alpha(enter[0]),
                transitionSpec = {
                    (slideInVertically { -it } + fadeIn(tween(300)))
                        .togetherWith(slideOutVertically { -it } + fadeOut(tween(300)))
                }
            ) { isSelectionMode ->
                if (isSelectionMode) {
                    SelectionHeader(
                        selectedCount = uiState.selectedTransactionIds.size,
                        onCloseClick = { clearSelection() },
                        onSelectAllClick = { selectAll() },
                        onDeleteClick = { showDeleteConfirmation = true }
                    )
                } else {
                    AppHeader(
                        title = stringResource(R.string.title_transactions),
                        onBackClick = onBackClick,
                        actions = {
                            HeaderCircleActionButton(
                                onClick = { isSearchExpanded = true },
                                icon = Icons.Filled.Search,
                                contentDescription = stringResource(R.string.desc_search_transactions)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box {
                                HeaderCircleActionButton(
                                    onClick = {
                                        closeSearchBar(
                                            focusManager = focusManager,
                                            onSearchQueryChange = updateSearchQuery,
                                            onSearchExpandedChange = { isSearchExpanded = it }
                                        )
                                        showBottomSheet = true
                                    },
                                    icon = if (uiState.isFilterActive) Icons.Rounded.FilterAlt else Icons.Outlined.FilterAlt,
                                    contentDescription = stringResource(R.string.label_sort_filter)
                                )
                                // Tiny purple dot indicator when any filter/sort is active,
                                // anchored to the 40dp circle's corner.
                                if (uiState.isFilterActive) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(7.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.85f),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    )
                }
            }
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut()
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = updateSearchQuery,
                    placeholder = {
                        Text(
                            stringResource(R.string.label_search_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(
                        if (MaterialTheme.colorScheme.isDark) Dimens.CardRadius else 16.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()

                        .focusRequester(searchFocusRequester)
                        .onGloballyPositioned { coordinates ->
                            searchBarBounds = coordinates.boundsInRoot()
                        },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.desc_search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GatedAction(
                                feature = Feature.ADVANCED_SEARCH_SCOPE,
                                displayName = stringResource(R.string.label_search_by_category_wallet),
                                onAction = { /* Handled by GatedAction dialogs */ }
                            ) { status, onClick ->
                                if (status !is AccessStatus.Granted) {
                                    IconButton(onClick = onClick) {
                                         Icon(
                                             imageVector = Icons.Default.Lock,
                                             contentDescription = stringResource(R.string.desc_unlock_advanced_search),
                                             tint = MaterialTheme.colorScheme.featureGateLock,
                                             modifier = Modifier.size(16.dp)
                                         )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    closeSearchBar(
                                        focusManager = focusManager,
                                        onSearchQueryChange = updateSearchQuery,
                                        onSearchExpandedChange = { isSearchExpanded = it }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.desc_close_search),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.accentInk,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.accentInk,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }


            // Active filter pills — shown below search bar, above the list.
            val activeFilters = remember(
                uiState.selectedSort,
                uiState.selectedOrder,
                uiState.selectedDateRange,
                uiState.selectedTransactionTypeIds,
                uiState.selectedCategoryIds,
                uiState.selectedPaymentTypeIds,
                uiState.selectedMinAmount,
                uiState.selectedMaxAmount,
                uiState.searchQuery
            ) {
                buildList {
                    // Sort
                    if (uiState.selectedSort != DEFAULT_SORT_BY || uiState.selectedOrder != DEFAULT_SORT_ORDER) {
                        val orderLabel = if (uiState.selectedOrder == SortType.NEWEST) "↓" else "↑"
                        add(ActiveFilter(FilterPillType.SORT, "${uiState.selectedSort} $orderLabel") {
                            updateSort(DEFAULT_SORT_BY)
                        })
                    }
                    // Date range
                    uiState.selectedDateRange?.let { range ->
                        add(ActiveFilter(FilterPillType.DATE_RANGE, range) {
                            updateDateRange(null)
                        })
                    }
                    // Transaction type (show only if filtered to one type)
                    if (uiState.selectedTransactionTypeIds.size == 1) {
                        val isIncome = uiState.selectedTransactionTypeIds.contains(1)
                        val label = if (isIncome) "Income" else "Expense"
                        val pillType = if (isIncome) FilterPillType.INCOME else FilterPillType.EXPENSE
                        add(ActiveFilter(pillType, label) {
                            toggleTransactionTypeFilter(uiState.selectedTransactionTypeIds.first())
                        })
                    }
                    // Categories
                    uiState.selectedCategoryIds.forEach { catId ->
                        val catName = uiState.availableCategories.firstOrNull { it.id == catId }?.name ?: "# $catId"
                        add(ActiveFilter(FilterPillType.CATEGORY, catName) {
                            toggleCategory(catId)
                        })
                    }
                    // Payment modes
                    uiState.selectedPaymentTypeIds.forEach { payId ->
                        val payName = uiState.paymentModes.firstOrNull { it.id == payId }?.name ?: "# $payId"
                        add(ActiveFilter(FilterPillType.PAYMENT_MODE, payName) {
                            togglePaymentMode(payId)
                        })
                    }
                    // Amount range
                    val hasMin = uiState.selectedMinAmount.isNotBlank()
                    val hasMax = uiState.selectedMaxAmount.isNotBlank()
                    if (hasMin || hasMax) {
                        val label = when {
                            hasMin && hasMax -> "${uiState.selectedMinAmount}–${uiState.selectedMaxAmount}"
                            hasMin -> "> ${uiState.selectedMinAmount}"
                            else -> "< ${uiState.selectedMaxAmount}"
                        }
                        add(ActiveFilter(FilterPillType.AMOUNT_RANGE, label) {
                            updateMinAmount("")
                            updateMaxAmount("")
                        })
                    }
                    // Search
                    if (uiState.searchQuery.isNotBlank()) {
                        add(ActiveFilter(FilterPillType.SEARCH, "\"${uiState.searchQuery.trim()}\"") {
                            updateSearchQuery("")
                        })
                    }
                }
            }

            ActiveFilterBar(
                filters = activeFilters,
                onClearAll = { resetFilters() },
                modifier = Modifier.alpha(enter[1])
            )



            // Pinned summary rendered above the lazy list (out of the scrollable
            // area). While the totals are still being computed the same composable
            // renders placeholders, so the card keeps its height and the list below
            // never jumps when the numbers arrive.
            pinnedSummary?.let { summary ->
                Box(Modifier.alpha(enter[2])) {
                TransactionSummaryCard(
                    income = if (isSummaryLoading) SUMMARY_PLACEHOLDER else summary.totalIncome,
                    expense = if (isSummaryLoading) SUMMARY_PLACEHOLDER else summary.totalExpense,
                    periodLabel = summary.periodLabel
                )
                }
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
            }

            if (transactionItems.isEmpty() && isRefreshError) {
                // A failed query must not fall through to the "no transactions"
                // state, which would tell the user they have no data when the load
                // actually broke.
                TransactionListErrorState(
                    onRetry = onRetry,
                    modifier = Modifier.weight(1f)
                )
            } else if (transactionItems.isEmpty() && isRefreshing) {
                // First load (or a filter change) — Paging is still fetching page 1.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.accentInk,
                        strokeWidth = 2.dp
                    )
                }
            } else if (transactionItems.isEmpty()) {
                var isEmptyMessageVisible by remember { mutableStateOf(false) }

                LaunchedEffect(emptyTransactionMessage) {
                    isEmptyMessageVisible = true
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isEmptyMessageVisible,
                        enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                            scaleIn(
                                initialScale = 0.5f,
                                animationSpec = tween(durationMillis = 500)
                            ) +
                            slideInVertically(
                                animationSpec = tween(durationMillis = 500),
                                initialOffsetY = { height -> height / 3 }
                            ),
                        exit = fadeOut(animationSpec = tween(durationMillis = 500)) +
                            scaleOut(
                                targetScale = 0.5f,
                                animationSpec = tween(durationMillis = 500)
                            ) +
                            slideOutVertically(
                                animationSpec = tween(durationMillis = 500),
                                targetOffsetY = { height -> height / 3 }
                            )
                    ) {
                        TypewriterText(
                            text = emptyTransactionMessage,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            emphasisColor = MaterialTheme.colorScheme.accentInk,
                            textAlign = TextAlign.Center,
                            softWrap = true,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 28.sp
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(enter[3]),
                    verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                        items(
                            count = transactionItems.size,
                            key = { index -> transactionItems[index].stableKey },
                            contentType = { index -> transactionItems[index].contentType }
                        ) { index ->
                            val item = transactionItems[index]
                            // Touch the backing Paging item for the rows the user has
                            // scrolled to. That is how Paging learns the viewport moved
                            // and prefetches the next page.
                            if (item is TransactionListItemUi.TransactionRow) {
                                pagingItems?.let { items ->
                                    pagingIndexById[item.card.id]?.let { pagingIndex ->
                                        items[pagingIndex]
                                    }
                                }
                            }
                            // Modifier.animateItem() keeps every item keyed stably
                            // (transaction id), so when one is removed the remaining
                            // cards animate upward to close the gap instead of
                            // jumping, and duplicated cards fade/place into position.
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(
                                        fadeInSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
                                        fadeOutSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                        placementSpec = spring(
                                            stiffness = Spring.StiffnessMediumLow,
                                            visibilityThreshold = IntOffset(1, 1)
                                        )
                                    )
                            ) {
                            when (item) {
                                is TransactionListItemUi.SummaryCard -> {
                                    TransactionSummaryCard(
                                        income = item.totalIncome,
                                        expense = item.totalExpense,
                                        periodLabel = item.periodLabel
                                    )
                                }

                                is TransactionListItemUi.Header -> {
                                    TransactionDateHeader(
                                        dayLabel = item.dayLabel,
                                        dateLabel = item.dateLabel
                                    )
                                }

                                is TransactionListItemUi.TransactionRow -> {
                                    val card = item.card
                                    // Slides with the finger, reveals the "Duplicate"/
                                    // "Delete" actions peeking from behind the card, and
                                    // springs back on release.
                                    SwipeableDuplicateCard(
                                        enabled = !uiState.isSelectionMode,
                                        onSwipe = { action ->
                                            when (action) {
                                                TransactionSwipeAction.Duplicate ->
                                                    onSwipeToDuplicate(card.transaction)
                                                TransactionSwipeAction.Delete ->
                                                    onSwipeToDelete(card.transaction)
                                            }
                                        }
                                    ) {
                                        TransactionCard(
                                            note = card.note,
                                            transactionDate = card.transactionDate,
                                            transactionTime = card.transactionTime,
                                            amount = card.amount,
                                            icon = card.icon,
                                            categoryId = card.transaction.categoryId,
                                            categoryColorHex = card.categoryColorHex,
                                            transactionTypeId = card.transactionTypeId,
                                            paymentType = card.paymentType,
                                            categoryLabel = card.categoryLabel,
                                            showTypeLabel = uiState.customizationSettings.showIncomeExpenseLabels,
                                            showTransactionDate = uiState.customizationSettings.showTransactionDate,
                                            showPaymentMethod = uiState.customizationSettings.showPaymentMethod,
                                            showTransactionTime = uiState.customizationSettings.showTransactionTime,
                                            showCategoryIcon = uiState.customizationSettings.showCategoryIcon,
                                            showCategoryLabel = uiState.customizationSettings.showCategoryLabel,
                                            tags = card.tags,
                                            fundName = card.fundName,
                                            showNoteTooltip = isProUser,
                                            isProUser = isProUser,
                                            isRecurring = card.isRecurring,
                                            isSelected = uiState.selectedTransactionIds.contains(card.id),
                                            selectionMode = uiState.isSelectionMode,
                                            onClick = {
                                                if (uiState.isSelectionMode) {
                                                    toggleSelection(card.id)
                                                } else {
                                                    onTransactionClick(card.transaction)
                                                }
                                            },
                                            onLongClick = {
                                                if (!uiState.isSelectionMode) {
                                                    enterSelectionMode(card.id)
                                                }
                                            }
                                        )
                                    }
                                }

                                // Phase 2: the ad is its own dedicated list item (keyed "ad_N")
                                // injected by the ViewModel after every 5th transaction row, so it
                                // never recomposes with transaction cards and its AndroidView is
                                // recycled across scroll entries (ADS_UI_JANK_FIX_PLAN §5).
                                //
                                // Gated entirely on isAdsEnabled: for ad-free (Pro) users the
                                // item renders nothing — no spacers, no reserved height — so no
                                // phantom gap every 5th row.
                                //
                                // The ViewModel alternates the placement between
                                // TRANSACTIONS_LIST and TRANSACTIONS_LIST_2 so both AdMob units
                                // render (and are tracked separately in the console).
                                is TransactionListItemUi.Ad -> {
                                    if (isAdsEnabled) {
                                        AdContainer(isAdsEnabled = true) {
                                            NativeAdCard(placement = item.placement)
                                        }
                                    }
                                }
                                }
                            }
                        }

                        // Bottom slot: a retry affordance if the next page failed,
                        // otherwise a spinner while Paging appends.
                        if (isAppendError) {
                            item(key = "append_error") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimens.PaddingMedium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AppTextButton(onClick = onRetry) {
                                        Text(
                                            text = stringResource(R.string.label_retry),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.accentInk
                                        )
                                    }
                                }
                            }
                        } else if (isAppending) {
                            item(key = "loading_indicator") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimens.PaddingMedium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.accentInk,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                }
            }

            // "Select all N in this view" link — only when selection mode is active
            // and not all items are loaded yet
            val showSelectAllLink = uiState.isSelectionMode
                && loadedCount < uiState.totalTransactionCount
                && uiState.totalTransactionCount > 0

            AnimatedVisibility(
                visible = showSelectAllLink,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.hairline
                    )
                    AppTextButton(
                        onClick = { selectAllInQuery() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(
                                R.string.label_select_all_in_view,
                                uiState.totalTransactionCount
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.accentInk
                        )
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.hairline
                    )
                }
            }

            // Period navigator fixed at the bottom of the screen
            TransactionPeriodNavigator(
                selectedFilter = uiState.selectedPeriodFilter,
                periodLabel = uiState.selectedPeriodLabel.asString(),
                canNavigateBackward = uiState.canNavigateBackward,
                canNavigateForward = uiState.canNavigateForward,
                onFilterSelected = updatePeriodFilter,
                onPreviousClick = { navigatePeriod(-1) },
                onNextClick = { navigatePeriod(1) },
                onLabelClick = when (uiState.selectedPeriodFilter) {
                    TransactionPeriodFilter.ALL -> null
                    else -> ({ showPeriodPicker = true })
                },
                modifier = Modifier.alpha(enter[3]).padding(top = Dimens.PaddingSmall)
            )
        }

        // Snackbar overlay for swipe-to-delete Undo (floats above the period navigator).
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        BrandAddFabSlot(
            onClick = onAddTransactionClick,
            visible = !uiState.isSelectionMode && snackbarHostState.currentSnackbarData == null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 16.dp)
        )
    }

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

private fun closeSearchBar(
    focusManager: FocusManager,
    onSearchQueryChange: (String) -> Unit,
    onSearchExpandedChange: (Boolean) -> Unit
) {
    focusManager.clearFocus(force = true)
    onSearchQueryChange("")
    onSearchExpandedChange(false)
}



@Preview(
    name = "Transactions Screen - Empty State (Light)",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun TransactionsScreenEmptyStatePreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        TransactionScreenContent(
            uiState = TransactionsScreenUiState(
                selectedPeriodLabel = UiText.dynamic("July 2026"),
                canNavigateBackward = true,
                canNavigateForward = true
            ),
            isAdsEnabled = true,
            onBackClick = {},
            onAddTransactionClick = {},
            onTransactionClick = {},
            clearSelection = {},
            selectAll = {},
            toggleSelection = {},
            enterSelectionMode = {},
            updateSearchQuery = {},
            updatePeriodFilter = {},
            navigatePeriod = {},
            jumpToPeriod = {},
            updateSort = {},
            updateOrder = {},
            updateDateRange = {},
            updateCustomDateRange = { _, _ -> },
            toggleTransactionTypeFilter = {},
            toggleCategory = {},
            togglePaymentMode = {},
            toggleTag = {},
            updateTagMatchMode = {},
            updateMinAmount = {},
            updateMaxAmount = {},
            applyFilters = {},
            resetFilters = {},
            deleteSelectedTransactions = {},
            selectAllInQuery = {}
        )
    }
}

@Preview(
    name = "Transactions Screen - Empty State (Dark)",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun TransactionsScreenEmptyStatePreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        TransactionScreenContent(
            uiState = TransactionsScreenUiState(
                selectedPeriodLabel = UiText.dynamic("July 2026"),
                canNavigateBackward = true,
                canNavigateForward = true
            ),
            isAdsEnabled = true,
            onBackClick = {},
            onAddTransactionClick = {},
            onTransactionClick = {},
            clearSelection = {},
            selectAll = {},
            toggleSelection = {},
            enterSelectionMode = {},
            updateSearchQuery = {},
            updatePeriodFilter = {},
            navigatePeriod = {},
            jumpToPeriod = {},
            updateSort = {},
            updateOrder = {},
            updateDateRange = {},
            updateCustomDateRange = { _, _ -> },
            toggleTransactionTypeFilter = {},
            toggleCategory = {},
            togglePaymentMode = {},
            toggleTag = {},
            updateTagMatchMode = {},
            updateMinAmount = {},
            updateMaxAmount = {},
            applyFilters = {},
            resetFilters = {},
            deleteSelectedTransactions = {},
            selectAllInQuery = {}
        )
    }
}

// Multi-config adaptive previews (phone → desktop sizes × font scales) so the
// list layout, search/filter bar, and empty state are visually verifiable
// across configurations without a device (roadmap Milestone 5).
@Preview(name = "Transactions - Multi-Config", showBackground = true)
@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun TransactionsScreenMultiConfigPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        TransactionScreenContent(
            uiState = TransactionsScreenUiState(
                selectedPeriodLabel = UiText.dynamic("July 2026"),
                canNavigateBackward = true,
                canNavigateForward = true
            ),
            isAdsEnabled = true,
            onBackClick = {},
            onAddTransactionClick = {},
            onTransactionClick = {},
            clearSelection = {},
            selectAll = {},
            toggleSelection = {},
            enterSelectionMode = {},
            updateSearchQuery = {},
            updatePeriodFilter = {},
            navigatePeriod = {},
            jumpToPeriod = {},
            updateSort = {},
            updateOrder = {},
            updateDateRange = {},
            updateCustomDateRange = { _, _ -> },
            toggleTransactionTypeFilter = {},
            toggleCategory = {},
            togglePaymentMode = {},
            toggleTag = {},
            updateTagMatchMode = {},
            updateMinAmount = {},
            updateMaxAmount = {},
            applyFilters = {},
            resetFilters = {},
            deleteSelectedTransactions = {},
            selectAllInQuery = {}
        )
    }
}


/** Stable LazyColumn key for a feed item (unchanged from the list-entry ids). */
private val TransactionListItemUi.stableKey: String
    get() = when (this) {
        is TransactionListItemUi.Header -> id
        is TransactionListItemUi.TransactionRow -> card.id
        is TransactionListItemUi.SummaryCard -> id
        is TransactionListItemUi.Ad -> id
    }

/** Recyclable content type so Compose reuses the right item composables. */
private val TransactionListItemUi.contentType: String
    get() = when (this) {
        is TransactionListItemUi.Header -> "header"
        is TransactionListItemUi.TransactionRow -> "transaction"
        is TransactionListItemUi.SummaryCard -> "summary"
        is TransactionListItemUi.Ad -> "ad"
    }



