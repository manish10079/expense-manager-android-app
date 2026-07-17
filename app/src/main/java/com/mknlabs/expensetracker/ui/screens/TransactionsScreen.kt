package com.mknlabs.expensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.ui.components.GatedAction
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.viewmodels.TransactionsViewModel
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.FilterBottomSheet
import com.mknlabs.expensetracker.ui.components.TransactionPeriodFilter
import com.mknlabs.expensetracker.ui.components.TransactionPeriodNavigator
import com.mknlabs.expensetracker.ui.components.TransactionCard
import com.mknlabs.expensetracker.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.ui.components.SelectionHeader
import com.mknlabs.expensetracker.ui.models.TransactionListItemUi
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.ui.theme.featureGateLock
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.launch

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer

import com.mknlabs.expensetracker.ui.components.NativeAdCard

import com.mknlabs.expensetracker.monetization.AdPlacement

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
    onTransactionClick: (Transaction) -> Unit = {}
) {
    val transactionsViewModel: TransactionsViewModel = hiltViewModel()
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()

    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val searchFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()
    var searchBarBounds by remember { mutableStateOf<Rect?>(null) }
    
    val emptyTransactionMessages = stringArrayResource(R.array.empty_transaction_messages).toList()

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

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var isPeriodMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

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

    BackHandler(enabled = uiState.isSelectionMode) {
        transactionsViewModel.clearSelection()
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
                                onSearchQueryChange = transactionsViewModel::updateSearchQuery,
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
                        onCloseClick = { transactionsViewModel.clearSelection() },
                        onSelectAllClick = { transactionsViewModel.selectAll() },
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
                                        onSearchQueryChange = transactionsViewModel::updateSearchQuery,
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
                    onValueChange = transactionsViewModel::updateSearchQuery,
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
                                        onSearchQueryChange = transactionsViewModel::updateSearchQuery,
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

            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))

            TransactionPeriodNavigator(
                selectedFilter = uiState.selectedPeriodFilter,
                periodLabel = uiState.selectedPeriodLabel.asString(),
                isMenuExpanded = isPeriodMenuExpanded,
                canNavigateBackward = uiState.canNavigateBackward,
                canNavigateForward = uiState.canNavigateForward,
                onMenuExpandedChange = { isPeriodMenuExpanded = it },
                onFilterSelected = { filter ->
                    transactionsViewModel.updatePeriodFilter(filter)
                    isPeriodMenuExpanded = false
                },
                onPreviousClick = {
                    transactionsViewModel.navigatePeriod(-1)
                },
                onNextClick = {
                    transactionsViewModel.navigatePeriod(1)
                },
                // Only enable label tap for Daily/Monthly/Yearly, not All
                onLabelClick = when (uiState.selectedPeriodFilter) {
                    TransactionPeriodFilter.ALL -> null
                    else -> ({ showPeriodPicker = true })
                }
            )

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
                        transactionsViewModel.jumpToPeriod(millis)
                        showPeriodPicker = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

            if (uiState.transactionItems.isEmpty()) {
                var isEmptyMessageVisible by remember { mutableStateOf(false) }

                LaunchedEffect(emptyTransactionMessage) {
                    // Trigger exit animation before showing new message
                    isEmptyMessageVisible = false
                    kotlinx.coroutines.delay(500) // wait for exit animation to complete
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
                        Text(
                            text = emptyTransactionMessage,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    contentPadding = PaddingValues(bottom = 180.dp)
                ) {
                        itemsIndexed(
                            items = uiState.transactionItems,
                            key = { _, item ->
                                when (item) {
                                    is TransactionListItemUi.Header -> item.id
                                    is TransactionListItemUi.TransactionRow -> item.card.id
                                }
                            },
                            contentType = { _, item ->
                                when (item) {
                                    is TransactionListItemUi.Header -> "header"
                                    is TransactionListItemUi.TransactionRow -> "transaction"
                                }
                            }
                        ) { index, item ->
                            when (item) {
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
                                                transactionsViewModel.toggleSelection(card.id)
                                            } else {
                                                onTransactionClick(card.transaction)
                                            }
                                        },
                                        onLongClick = {
                                            if (!uiState.isSelectionMode) {
                                                transactionsViewModel.enterSelectionMode(card.id)
                                            }
                                        }
                                    )
                                }
                            }

                            // Inject Native Ad every 7th item
                            if (index > 0 && index % 7 == 0) {
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
                    onSortChange = { transactionsViewModel.updateSort(it) },
                    onOrderChange = { transactionsViewModel.updateOrder(it) },
                    onDateRangeChange = { transactionsViewModel.updateDateRange(it) },
                    onCustomDateRangeChange = { start, end ->
                            transactionsViewModel.updateCustomDateRange(start, end)
                        },
                    onTransactionTypeToggle = {
                        transactionsViewModel.toggleTransactionTypeFilter(it)
                    },
                    onCategoryToggle = { categoryId ->
                        transactionsViewModel.toggleCategory(categoryId)
                    },
                    onPaymentModeToggle = { paymentTypeId ->
                        transactionsViewModel.togglePaymentMode(paymentTypeId)
                    },
                    onMinAmountChange = { transactionsViewModel.updateMinAmount(it) },
                    onMaxAmountChange = { transactionsViewModel.updateMaxAmount(it) },
                    onApply = {
                        transactionsViewModel.applyFilters()
                        scope.launch {
                            sheetState.hide()
                            showBottomSheet = false
                        }
                    },
                    onReset = {
                        transactionsViewModel.resetFilters()
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
                        transactionsViewModel.deleteSelectedTransactions()
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
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = dateLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


