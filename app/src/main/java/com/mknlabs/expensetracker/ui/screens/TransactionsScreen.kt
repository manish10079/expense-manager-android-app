package com.mknlabs.expensetracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.mknlabs.expensetracker.models.SortType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.AdPlacement
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
import com.mknlabs.expensetracker.ui.models.TransactionListItemUi
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseRed
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.IncomeGreen
import com.mknlabs.expensetracker.ui.theme.featureGateLock
import com.mknlabs.expensetracker.ui.viewmodels.TransactionsScreenUiState
import com.mknlabs.expensetracker.ui.viewmodels.TransactionsViewModel
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
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
    onBackClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    isAdsEnabled: Boolean = false
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
        onBackClick = onBackClick,
        onAddTransactionClick = onAddTransactionClick,
        onTransactionClick = onTransactionClick,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionScreenContent(
    uiState: TransactionsScreenUiState,
    isAdsEnabled: Boolean,
    onBackClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
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
                            GatedAction(
                                feature = Feature.SEARCH_TRANSACTIONS,
                                onAction = { isSearchExpanded = true }
                            ) { status, onClick ->
                                IconButton(
                                    onClick = onClick,
                                    modifier = Modifier
                                        .size(26.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                                ) {
                                      Icon(
                                          imageVector = if (status is AccessStatus.Granted) Icons.Filled.Search else Icons.Filled.Lock,
                                          contentDescription = stringResource(R.string.desc_search_transactions),
                                          tint = if (status is AccessStatus.Granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.featureGateLock,
                                          modifier = Modifier.size(if (status is AccessStatus.Granted) 18.dp else 14.dp)
                                      )
                                }
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

                                // Phase 2: the ad is its own dedicated list item (keyed "ad_N")
                                // injected by the ViewModel after every 5th transaction row, so it
                                // never recomposes with transaction cards and its AndroidView is
                                // recycled across scroll entries (ADS_UI_JANK_FIX_PLAN §5).
                                is TransactionListItemUi.Ad -> {
                                    Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                                    AdContainer(isAdsEnabled = isAdsEnabled) {
                                        NativeAdCard(placement = AdPlacement.TRANSACTIONS_LIST)
                                    }
                                    Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
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



