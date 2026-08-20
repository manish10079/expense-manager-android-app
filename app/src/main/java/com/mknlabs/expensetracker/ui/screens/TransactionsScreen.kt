package com.mknlabs.expensetracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.SortType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.FilterBottomSheet
import com.mknlabs.expensetracker.ui.components.GatedAction
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.ui.components.SelectionHeader
import com.mknlabs.expensetracker.ui.components.TransactionCard
import com.mknlabs.expensetracker.ui.components.TransactionPeriodFilter
import com.mknlabs.expensetracker.ui.components.TransactionPeriodNavigator
import com.mknlabs.expensetracker.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.ui.horizontalSwipe
import com.mknlabs.expensetracker.ui.models.TransactionListItemUi
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseRed
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.IncomeGreen
import com.mknlabs.expensetracker.ui.theme.featureGateLock
import com.mknlabs.expensetracker.ui.viewmodels.TransactionsScreenUiState
import com.mknlabs.expensetracker.ui.viewmodels.TransactionsViewModel
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
    transactions: List<Transaction> = emptyList(),
    categories: List<CategoryType> = emptyList(),
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
        transactions,
        categories,
        currencyId,
        amountFormatPreferences,
        dateFormatPattern,
        timeFormat,
        transactionCardCustomizationSettings
    ) {
        transactionsViewModel.updateInputs(
            transactions = transactions,
            categories = categories,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat,
            customizationSettings = transactionCardCustomizationSettings
        )
    }
    val uiState by transactionsViewModel.uiState.collectAsStateWithLifecycle()

    TransactionScreenContent(
        uiState = uiState,
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
        selectAll = transactionsViewModel::selectAll,
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
        updateMinAmount = transactionsViewModel::updateMinAmount,
        updateMaxAmount = transactionsViewModel::updateMaxAmount,
        applyFilters = transactionsViewModel::applyFilters,
        resetFilters = transactionsViewModel::resetFilters,
        deleteSelectedTransactions = transactionsViewModel::deleteSelectedTransactions
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TransactionScreenContent(
    uiState: TransactionsScreenUiState,
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
    updateMinAmount: (String) -> Unit,
    updateMaxAmount: (String) -> Unit,
    applyFilters: () -> Unit,
    resetFilters: () -> Unit,
    deleteSelectedTransactions: () -> Unit
) {
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val searchFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()
    var searchBarBounds by remember { mutableStateOf<Rect?>(null) }
    
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
            .background(MaterialTheme.colorScheme.background)
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
                            IconButton(
                                onClick = { isSearchExpanded = true },
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = stringResource(R.string.desc_search_transactions),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(30.dp))

                            IconButton(
                                onClick = {
                                    closeSearchBar(
                                        focusManager = focusManager,
                                        onSearchQueryChange = updateSearchQuery,
                                        onSearchExpandedChange = { isSearchExpanded = it }
                                    )
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FilterAlt,
                                    contentDescription = stringResource(R.string.label_sort_filter),
                                    tint = MaterialTheme.colorScheme.primary
                                )
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
                    shape = RoundedCornerShape(Dimens.CardRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.PaddingMedium)
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
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }


            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))

            // Pinned summary rendered above the lazy list (out of the scrollable area)
            uiState.pinnedSummary?.let { summary ->
                TransactionSummaryCard(
                    income = summary.totalIncome,
                    expense = summary.totalExpense,
                    periodLabel = summary.periodLabel
                )
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
            }

            if (uiState.transactionItems.isEmpty()) {
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
                            emphasisColor = MaterialTheme.colorScheme.primary,
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
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                        items(
                            items = uiState.transactionItems,
                            key = { item ->
                                when (item) {
                                    is TransactionListItemUi.Header -> item.id
                                    is TransactionListItemUi.TransactionRow -> item.card.id
                                    is TransactionListItemUi.SummaryCard -> item.id
                                    is TransactionListItemUi.Ad -> item.id
                                }
                            },
                            contentType = { item ->
                                when (item) {
                                    is TransactionListItemUi.Header -> "header"
                                    is TransactionListItemUi.TransactionRow -> "transaction"
                                    is TransactionListItemUi.SummaryCard -> "summary"
                                    is TransactionListItemUi.Ad -> "ad"
                                }
                            }
                        ) { item ->
                            // Modifier.animateItem() keeps every item keyed stably
                            // (transaction id), so when one is removed the remaining
                            // cards animate upward to close the gap instead of
                            // jumping, and duplicated cards fade/place into position.
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
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
                                            transactionTypeId = card.transactionTypeId,
                                            paymentType = card.paymentType,
                                            categoryLabel = card.categoryLabel,
                                            showTypeLabel = uiState.customizationSettings.showIncomeExpenseLabels,
                                            showTransactionDate = uiState.customizationSettings.showTransactionDate,
                                            showPaymentMethod = uiState.customizationSettings.showPaymentMethod,
                                            showTransactionTime = uiState.customizationSettings.showTransactionTime,
                                            showCategoryIcon = uiState.customizationSettings.showCategoryIcon,
                                            showCategoryLabel = uiState.customizationSettings.showCategoryLabel,
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
                                        Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                                        AdContainer(isAdsEnabled = true) {
                                            NativeAdCard(placement = item.placement)
                                        }
                                        Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                                    }
                                }
                                }
                            }
                        }
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
                modifier = Modifier.padding(top = Dimens.PaddingSmall)
            )
        }

        // Snackbar overlay for swipe-to-delete Undo (floats above the period navigator).
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
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
                    minAmount = uiState.selectedMinAmount,
                    maxAmount = uiState.selectedMaxAmount,
                    onSortChange = updateSort,
                    onOrderChange = updateOrder,
                    onDateRangeChange = updateDateRange,
                    onCustomDateRangeChange = updateCustomDateRange,
                    onTransactionTypeToggle = toggleTransactionTypeFilter,
                    onCategoryToggle = toggleCategory,
                    onPaymentModeToggle = togglePaymentMode,
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
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.label_delete_transactions)) },
            text = { Text(stringResource(R.string.msg_delete_transactions_confirm, uiState.selectedTransactionIds.size)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteSelectedTransactions()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(stringResource(R.string.label_delete_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.label_cancel_confirm))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showRecurringDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showRecurringDuplicateDialog = false },
            title = { Text(stringResource(R.string.title_cannot_duplicate_recurring)) },
            text = { Text(stringResource(R.string.msg_cannot_duplicate_recurring)) },
            confirmButton = {
                TextButton(onClick = { showRecurringDuplicateDialog = false }) {
                    Text(stringResource(R.string.label_ok), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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

// Swipe-to-duplicate constants (dp are converted to px via LocalDensity).
private const val SWIPE_MAX_REVEAL_DP = 96f
private const val SWIPE_MIN_REVEAL_DP = 24f
// Finger distance (dp) that must be exceeded to trigger the duplicate. Kept
// below SWIPE_MAX_REVEAL_DP so the trigger always lands inside the card's
// 1:1 drag range regardless of screen density.
private const val SWIPE_TRIGGER_THRESHOLD_DP = 56f
// Release velocity (px/s) that triggers the duplicate even below the distance threshold.
private const val SWIPE_FLING_VELOCITY_PX_PER_SECOND = 600f
// Extra finger movement beyond the reveal point is resisted (rubber-band feel).
private const val SWIPE_OVERSHOOT_RESISTANCE = 0.25f
// Premium swipe exit & gap-collapse animation (DELETE only): on commit the card
// keeps flying toward the screen edge for SWIPE_EXIT_DURATION_MS while still
// occupying its layout slot (so a clearly visible gap is left behind), the gap
// is held for SWIPE_GAP_HOLD_MS, and only then is the real delete fired — the
// remaining cards collapse the gap via the list's Modifier.animateItem().
private const val SWIPE_EXIT_DURATION_MS = 180
private const val SWIPE_GAP_HOLD_MS = 120
// Extra distance beyond the card's own width so it fully clears the screen edge.
private const val SWIPE_EXIT_MARGIN_DP = 48f
// Subtle scale-down during the exit (1.0 -> ~0.98) so the card feels physically
// dismissed without looking like it shrinks dramatically.
private const val SWIPE_EXIT_SCALE = 0.98f

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
 * card does NOT vanish — it keeps flying in the swipe direction toward the
 * screen edge while fading out and shrinking imperceptibly ([SWIPE_EXIT_SCALE]),
 * still occupying its layout slot so a clearly visible temporary gap is left
 * behind. After a short hold ([SWIPE_GAP_HOLD_MS]) the real delete is fired;
 * the remaining cards then close the gap smoothly via the parent list's
 * [androidx.compose.foundation.lazy.animateItem] placement animation. Gesture
 * state (this composable) stays separate from data state (the ViewModel): the
 * item remains in the list until the delete lands, and the [isRemoving] guard
 * makes a double-delete impossible.
 */
@Composable
private fun SwipeableDuplicateCard(
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
                    alpha = if (isRemoving) 0f else revealAlpha(
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .graphicsLayer {
                    alpha = if (isRemoving) 0f else revealAlpha(
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
                        // it toward the screen edge, fading to 0 and shrinking to
                        // SWIPE_EXIT_SCALE. All values are driven off one
                        // Animatable inside graphicsLayer — no recomposition per
                        // frame, no per-frame object allocation.
                        val progress = dismissProgress.value
                        translationX = dismissStartX + (dismissTargetX - dismissStartX) * progress
                        alpha = 1f - progress
                        scaleX = 1f - (1f - SWIPE_EXIT_SCALE) * progress
                        scaleY = 1f - (1f - SWIPE_EXIT_SCALE) * progress
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
                            // Duplicate: fire the data operation immediately; the
                            // original card settles back naturally below and the
                            // new card fades/places in via the list's animateItem.
                            isDuplicateCommitting = true
                            onSwipe(transactionSwipeAction(isLeftSwipe = true))
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
                        // Below-threshold release (or a duplicate commit): spring
                        // the card back to centre. Skipped while a delete is flying
                        // out — the exit coroutine owns the card then.
                        if (!isRemoving) {
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
                        if (!isRemoving) {
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
private fun SwipeActionLabel(
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
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

/**
 * Card follows the finger 1:1 up to [maxRevealPx]; extra movement beyond it is
 * resisted so the card doesn't fly off-screen.
 */
private fun dampedTranslation(rawOffsetPx: Float, maxRevealPx: Float): Float {
    val clamped = rawOffsetPx.coerceIn(-maxRevealPx, maxRevealPx)
    return clamped + (rawOffsetPx - clamped) * SWIPE_OVERSHOOT_RESISTANCE
}

/**
 * Label opacity for the edge revealed as the card slides: [revealsWhenPositive]
 * is true for the left label (shown when the card slides right) and false for
 * the right label. Fades in after [minRevealPx] and is fully opaque at
 * [maxRevealPx], following the card's damped position.
 */
private fun revealAlpha(
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

@Composable
private fun TransactionDateHeader(
    dayLabel: String,
    dateLabel: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dayLabel,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = dateLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
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
            updateMinAmount = {},
            updateMaxAmount = {},
            applyFilters = {},
            resetFilters = {},
            deleteSelectedTransactions = {}
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
            updateMinAmount = {},
            updateMaxAmount = {},
            applyFilters = {},
            resetFilters = {},
            deleteSelectedTransactions = {}
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
            updateMinAmount = {},
            updateMaxAmount = {},
            applyFilters = {},
            resetFilters = {},
            deleteSelectedTransactions = {}
        )
    }
}

@Composable
private fun TransactionSummaryCard(
    income: String,
    expense: String,
    periodLabel: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.8.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (!periodLabel.isNullOrBlank()) {
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }

                // Income
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowDownward,
                        contentDescription = null,
                        tint = IncomeGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
//                       text = stringResource(R.string.label_income) + ": " + income,
                        text = "+" + income,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Expense
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = null,
                        tint = ExpenseRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
//                      text = stringResource(R.string.label_expense) + ": " + expense,
                        text = "-" + expense,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.8.dp
            )
        }
    }
}

@Composable
private fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    emphasisColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    textAlign: TextAlign? = null,
    softWrap: Boolean = true,
    charDelayMillis: Long = 25L,
    pauseAtFraction: Float = 0.4f,
    morphDurationMillis: Int = 500
) {
    var visibleText by remember(text) { mutableStateOf("") }
    var showCursor by remember(text) { mutableStateOf(true) }
    // 0 = typing phase 1 (base color), 1 = pause + color morph, 2 = typing phase 2 (emphasis color)
    var morphPhase by remember(text) { mutableStateOf(0) }

    // Interpolates the whole typed string from the base color to the brand color
    // while morphPhase == 1 (the pause), then stays at the emphasis color.
    // Keyed by text so the progress snaps back to grey instantly when a new
    // empty-state message starts typing (no brand -> grey reverse animation).
    val colorProgress by key(text) {
        animateFloatAsState(
            targetValue = if (morphPhase >= 1) 1f else 0f,
            animationSpec = tween(durationMillis = morphDurationMillis),
            label = "typewriterColorMorph"
        )
    }
    val renderedColor = lerp(color, emphasisColor, colorProgress)

    LaunchedEffect(text) {
        if (text.isEmpty()) return@LaunchedEffect
        visibleText = ""
        showCursor = true
        morphPhase = 0

        // Pause point: a fixed 40% of the message has been typed.
        val pauseIndex = (text.length * pauseAtFraction).toInt().coerceIn(1, text.length)

        // Phase 1 — type the first 40% in the base (grey/white) color
        for (i in 1..pauseIndex) {
            visibleText = text.substring(0, i)
            kotlinx.coroutines.delay(charDelayMillis)
        }

        // Phase 2 — pause: the whole typed string animates to the brand color
        // while the cursor keeps blinking.
        morphPhase = 1
        val pauseUntil = System.currentTimeMillis() + morphDurationMillis + 400L
        var blink = false
        while (System.currentTimeMillis() < pauseUntil) {
            blink = !blink
            showCursor = blink
            kotlinx.coroutines.delay(300L)
        }
        showCursor = true

        // Phase 3 — resume typing the rest, now in the brand color
        morphPhase = 2
        for (i in (pauseIndex + 1)..text.length) {
            visibleText = text.substring(0, i)
            kotlinx.coroutines.delay(charDelayMillis)
        }

        // Blink the cursor a few times then fade it out
        for (j in 1..6) {
            showCursor = !showCursor
            kotlinx.coroutines.delay(350)
        }
        showCursor = false
    }

    Text(
        text = buildAnnotatedString {
            append(visibleText)
            if (showCursor) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Light, color = renderedColor.copy(alpha = 0.5f))) {
                    append(" |")
                }
            }
        },
        modifier = modifier,
        style = style,
        color = renderedColor,
        textAlign = textAlign,
        softWrap = softWrap
    )
}



