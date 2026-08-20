package com.mknlabs.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import android.app.Application
import androidx.lifecycle.ViewModel
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_SORT_BY
import com.mknlabs.expensetracker.data.constants.DEFAULT_SORT_ORDER
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.domain.mapper.buildTransactionListItems
import com.mknlabs.expensetracker.domain.mapper.toTransactionCardItemUi
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.SortType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.ui.components.FILTER_DATE_LAST_15_DAYS
import com.mknlabs.expensetracker.ui.components.FILTER_DATE_LAST_30_DAYS
import com.mknlabs.expensetracker.ui.components.FILTER_DATE_LAST_60_DAYS
import com.mknlabs.expensetracker.ui.components.FILTER_DATE_LAST_7_DAYS
import com.mknlabs.expensetracker.ui.components.TransactionPeriodFilter
import com.mknlabs.expensetracker.ui.models.PaginationState
import com.mknlabs.expensetracker.ui.models.TransactionListItemUi
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.getDefaultOrder
import com.mknlabs.expensetracker.utils.sortTransactions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.AdPlacement
import com.mknlabs.expensetracker.domain.usecase.ObserveAccessStatusUseCase

import com.mknlabs.expensetracker.utils.UiText

@Immutable
data class TransactionsScreenUiState(
    val searchQuery: String = "",
    val selectedSort: String = DEFAULT_SORT_BY,
    val selectedOrder: SortType = DEFAULT_SORT_ORDER,
    val selectedDateRange: String? = null,
    val selectedCustomStartDate: Long? = null,
    val selectedCustomEndDate: Long? = null,
    val selectedTransactionTypeIds: Set<Int> = setOf(1, 2),
    val selectedCategoryIds: Set<Int> = emptySet(),
    val selectedPaymentTypeIds: Set<Int> = emptySet(),
    val selectedMinAmount: String = "",
    val selectedMaxAmount: String = "",
    val selectedPeriodFilter: TransactionPeriodFilter = TransactionPeriodFilter.MONTHLY,
    val focusedPeriodTimestamp: Long = 0L,
    val canNavigateBackward: Boolean = false,
    val canNavigateForward: Boolean = false,
    val selectedPeriodLabel: UiText = UiText.dynamic(""),
    val availableCategories: List<CategoryType> = emptyList(),
    val paymentModes: List<PaymentType> = emptyList(),
    val transactionItems: List<TransactionListItemUi> = emptyList(),
    val pinnedSummary: TransactionListItemUi.SummaryCard? = null,
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    val isSelectionMode: Boolean = false,
    val selectedTransactionIds: Set<String> = emptySet(),
    val isDragging: Boolean = false,
    val pagination: PaginationState = PaginationState()
)

private const val KEY_CUSTOM_RANGE = "KEY_CUSTOM_RANGE"
private const val DEFAULT_MONTH_YEAR_PATTERN = "MMM, yyyy"
private const val DEFAULT_YEAR_PATTERN = "yyyy"
private const val FALLBACK_CATEGORY_NAME = "Other"
private const val FALLBACK_TODAY_LABEL = "Today"
private const val FALLBACK_YESTERDAY_LABEL = "Yesterday"
private const val FALLBACK_TOMORROW_LABEL = "Tomorrow"

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val application: Application,
    private val transactionRepository: TransactionRepository,
    private val observeAccessStatusUseCase: ObserveAccessStatusUseCase
) : ViewModel() {

    /**
     * Accumulated transactions for the current view. Each page load appends to this list.
     * Reset to empty when filters, sort, period, or search change.
     */
    private val currentTransactions = mutableListOf<Transaction>()

    private var currentCategories: List<CategoryType> = emptyList()

    private val _selectedTransactionIds = MutableStateFlow<Set<String>>(emptySet())
    private val _isSelectionMode = MutableStateFlow(false)

    private var currentCurrencyId: Int = DEFAULT_CURRENCY_ID
    private var currentAmountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences
    private var currentDateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN
    private var currentTimeFormat: String = DEFAULT_TIME_FORMAT
    private var currentCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings()

    private var searchQuery: String = ""
    private var selectedSort: String = DEFAULT_SORT_BY
    private var selectedOrder: SortType = DEFAULT_SORT_ORDER
    private var selectedDateRange: String? = null
    private var customStartDate: Long? = null
    private var customEndDate: Long? = null
    private var selectedTransactionTypeIds: Set<Int> = setOf(1, 2)
    private var selectedCategoryIds: Set<Int> = emptySet()
    private var selectedPaymentTypeIds: Set<Int> = emptySet()
    private var selectedMinAmount: String = ""
    private var selectedMaxAmount: String = ""

    private var appliedSortType: SortType = DEFAULT_SORT_ORDER
    private var appliedDateRange: String? = null
    private var appliedCustomStartDate: Long? = null
    private var appliedCustomEndDate: Long? = null
    private var appliedTransactionTypeIds: Set<Int> = setOf(1, 2)
    private var appliedCategoryIds: Set<Int> = emptySet()
    private var appliedPaymentTypeIds: Set<Int> = emptySet()
    private var appliedMinAmount: String = ""
    private var appliedMaxAmount: String = ""

    private var selectedPeriodFilter: TransactionPeriodFilter = TransactionPeriodFilter.MONTHLY
    private var focusedPeriodTimestamp: Long = System.currentTimeMillis()

    private var advancedSearchGranted: Boolean = false

    private val _baseUiState = MutableStateFlow(
        TransactionsScreenUiState(
            focusedPeriodTimestamp = focusedPeriodTimestamp,
            paymentModes = paymentTypeMap.values.toList()
        )
    )

    val uiState: StateFlow<TransactionsScreenUiState> = combine(
        _baseUiState,
        _selectedTransactionIds,
        _isSelectionMode
    ) { base, selectedIds, selectionMode ->
        base.copy(
            selectedTransactionIds = selectedIds,
            isSelectionMode = selectionMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _baseUiState.value.copy(
            selectedTransactionIds = _selectedTransactionIds.value,
            isSelectionMode = _isSelectionMode.value
        )
    )

    init {
        observeAdvancedSearchAccess()
        observeTransactionsChange()
        loadFirstPage()
    }

    private fun observeTransactionsChange() {
        viewModelScope.launch {
            transactionRepository.observeActiveTransactions().collect {
                // Whenever Room DB active transactions change (added, deleted, restored),
                // refresh the currently loaded view pages.
                reloadCurrentPages()
            }
        }
    }

    private fun observeAdvancedSearchAccess() {
        viewModelScope.launch {
            observeAccessStatusUseCase(Feature.ADVANCED_SEARCH_SCOPE).collect { status ->
                advancedSearchGranted = status is AccessStatus.Granted
                resetAndReload()
            }
        }
    }

    /**
     * Called from the composable when inputs change (categories, currency, etc.).
     * Only triggers a reload if the inputs actually changed.
     */
    fun updateInputs(
        categories: List<CategoryType>,
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences,
        dateFormatPattern: String,
        timeFormat: String,
        customizationSettings: TransactionCardCustomizationSettings
    ) {
        val inputsChanged = currentCategories != categories ||
            currentCurrencyId != currencyId ||
            currentDateFormatPattern != dateFormatPattern ||
            currentTimeFormat != timeFormat ||
            currentCustomizationSettings != customizationSettings

        currentCategories = categories
        currentCurrencyId = currencyId
        currentAmountFormatPreferences = amountFormatPreferences
        currentDateFormatPattern = dateFormatPattern
        currentTimeFormat = timeFormat
        currentCustomizationSettings = customizationSettings

        if (inputsChanged) {
            resetAndReload()
        }
    }

    // ─── Search & Filter Actions ──────────────────────────────────────

    fun updateSearchQuery(query: String) {
        searchQuery = query
        _baseUiState.update { it.copy(searchQuery = query) }
        resetAndReload()
    }

    fun updateSort(sort: String) {
        selectedSort = sort
        selectedOrder = getDefaultOrder(sort)
        resetAndReload()
    }

    fun updateOrder(order: SortType) {
        selectedOrder = order
        resetAndReload()
    }

    fun updateDateRange(dateRange: String?) {
        selectedDateRange = dateRange
        resetAndReload()
    }

    fun updateCustomDateRange(start: Long?, end: Long?) {
        customStartDate = start
        customEndDate = end ?: start
        selectedDateRange = KEY_CUSTOM_RANGE
        resetAndReload()
    }

    fun toggleTransactionTypeFilter(transactionTypeId: Int) {
        selectedTransactionTypeIds = selectedTransactionTypeIds.toggle(transactionTypeId)
        selectedCategoryIds = emptySet()
        resetAndReload()
    }

    fun toggleCategory(categoryId: Int) {
        selectedCategoryIds = selectedCategoryIds.toggle(categoryId)
        resetAndReload()
    }

    fun togglePaymentMode(paymentTypeId: Int) {
        selectedPaymentTypeIds = selectedPaymentTypeIds.toggle(paymentTypeId)
        resetAndReload()
    }

    fun updateMinAmount(amount: String) {
        selectedMinAmount = amount
        resetAndReload()
    }

    fun updateMaxAmount(amount: String) {
        selectedMaxAmount = amount
        resetAndReload()
    }

    fun applyFilters() {
        appliedSortType = selectedOrder
        appliedDateRange = selectedDateRange
        appliedCustomStartDate = customStartDate
        appliedCustomEndDate = customEndDate
        appliedTransactionTypeIds = selectedTransactionTypeIds
        appliedCategoryIds = selectedCategoryIds
        appliedPaymentTypeIds = selectedPaymentTypeIds
        appliedMinAmount = selectedMinAmount
        appliedMaxAmount = selectedMaxAmount
        resetAndReload()
    }

    fun resetFilters() {
        selectedSort = DEFAULT_SORT_BY
        selectedOrder = DEFAULT_SORT_ORDER
        selectedDateRange = null
        customStartDate = null
        customEndDate = null
        selectedTransactionTypeIds = setOf(1, 2)
        selectedCategoryIds = emptySet()
        selectedPaymentTypeIds = emptySet()
        selectedMinAmount = ""
        selectedMaxAmount = ""
        applyFilters()
    }

    // ─── Period Navigation ────────────────────────────────────────────

    fun updatePeriodFilter(filter: TransactionPeriodFilter) {
        selectedPeriodFilter = filter
        focusedPeriodTimestamp = System.currentTimeMillis()
        clearSelection()
        resetAndReload()
    }

    fun navigatePeriod(step: Int) {
        focusedPeriodTimestamp = shiftPeriod(
            timestamp = focusedPeriodTimestamp,
            filter = selectedPeriodFilter,
            step = step
        )
        clearSelection()
        resetAndReload()
    }

    fun jumpToPeriod(millis: Long) {
        focusedPeriodTimestamp = millis
        clearSelection()
        resetAndReload()
    }

    // ─── Selection ────────────────────────────────────────────────────

    fun toggleSelection(transactionId: String) {
        if (!_isSelectionMode.value) {
            _isSelectionMode.value = true
        }
        val currentIds = _selectedTransactionIds.value
        val newIds = if (currentIds.contains(transactionId)) {
            currentIds - transactionId
        } else {
            currentIds + transactionId
        }

        _selectedTransactionIds.value = newIds
        if (newIds.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun enterSelectionMode(initialId: String) {
        _isSelectionMode.value = true
        _selectedTransactionIds.value = setOf(initialId)
    }

    fun clearSelection() {
        _isSelectionMode.value = false
        _selectedTransactionIds.value = emptySet()
    }

    /**
     * Selects all currently loaded transactions (the visible batch).
     * Used by the top-bar select-all button.
     */
    fun selectAll() {
        val allIds = _baseUiState.value.transactionItems
            .filterIsInstance<TransactionListItemUi.TransactionRow>()
            .map { it.card.id }
            .toSet()

        val currentlySelected = _selectedTransactionIds.value

        if (currentlySelected.size >= allIds.size && allIds.isNotEmpty()) {
            clearSelection()
        } else {
            _selectedTransactionIds.value = allIds
            _isSelectionMode.value = allIds.isNotEmpty()
        }
    }

    private fun selectAllEveryLoadedItem() {
        val allIds = _baseUiState.value.transactionItems
            .filterIsInstance<TransactionListItemUi.TransactionRow>()
            .map { it.card.id }
            .toSet()

        if (allIds.isNotEmpty()) {
            _selectedTransactionIds.value = allIds
            _isSelectionMode.value = true
        }
    }

    /**
     * Loads ALL remaining pages from Room, then selects every transaction.
     * Called by the "Select all N in this view" link above the period navigator.
     */
    fun selectAllInQuery() {
        val current = _baseUiState.value.pagination
        if (current.isLoading) return

        // If everything is already loaded, select all loaded items without toggling off
        if (!current.hasMore) {
            selectAllEveryLoadedItem()
            return
        }

        viewModelScope.launch {
            _baseUiState.update { it.copy(pagination = it.pagination.copy(isLoading = true)) }

            val range = computePeriodRange()
            var pageNumber = current.currentPage + 1

            // Load remaining pages one by one
            while (true) {
                val page = withContext(Dispatchers.IO) {
                    if (range != null) {
                        transactionRepository.getActiveTransactionsPagedInRange(
                            startMillis = range.first,
                            endMillis = range.second,
                            pageSize = current.pageSize,
                            pageNumber = pageNumber
                        )
                    } else {
                        transactionRepository.getActiveTransactionsPaged(
                            pageSize = current.pageSize,
                            pageNumber = pageNumber
                        )
                    }
                }

                if (page.isEmpty()) break
                currentTransactions.addAll(page)
                pageNumber++

                if (page.size < current.pageSize) break
            }

            val updatedPagination = current.copy(
                currentPage = pageNumber - 1,
                hasMore = false,
                isLoading = false,
                loadedCount = currentTransactions.size
            )

            val newState = withContext(Dispatchers.Default) {
                calculateNewUiState(updatedPagination)
            }
            _baseUiState.value = newState

            // Ensure selection mode is ON and all transaction IDs in this view are selected
            selectAllEveryLoadedItem()
        }
    }

    fun deleteSelectedTransactions() {
        val idsToDelete = _selectedTransactionIds.value.toList()
        if (idsToDelete.isEmpty()) return

        viewModelScope.launch {
            transactionRepository.softDeleteTransactions(idsToDelete)
            // Remove deleted items from current page and rebuild
            currentTransactions.removeAll { it.id in idsToDelete }
            clearSelection()
            rebuildUiState()
        }
    }

    fun selectRange(fromId: String, toId: String) {
        val transactionsInList = _baseUiState.value.transactionItems
            .filterIsInstance<TransactionListItemUi.TransactionRow>()
            .map { it.card.id }

        val startIndex = transactionsInList.indexOf(fromId)
        val endIndex = transactionsInList.indexOf(toId)

        if (startIndex == -1 || endIndex == -1) return

        val rangeIds = if (startIndex <= endIndex) {
            transactionsInList.subList(startIndex, endIndex + 1)
        } else {
            transactionsInList.subList(endIndex, startIndex + 1)
        }

        _selectedTransactionIds.value = _selectedTransactionIds.value + rangeIds
        _isSelectionMode.value = true
    }

    // ─── Pagination ───────────────────────────────────────────────────

    /**
     * Resets pagination state and loads the first page from Room.
     * Called whenever filters, sort, period, or search change.
     */
    private fun resetAndReload() {
        currentTransactions.clear()
        _baseUiState.update {
            it.copy(pagination = PaginationState(isLoading = true))
        }
        loadFirstPage()
    }

    /**
     * Loads page 0 from Room and rebuilds the UI state.
     */
    private fun loadFirstPage() {
        viewModelScope.launch {
            val pageSize = PaginationState.PAGE_SIZE_DEFAULT
            val range = computePeriodRange()

            val totalCount = withContext(Dispatchers.IO) {
                if (range != null) {
                    transactionRepository.countActiveTransactionsInRange(range.first, range.second)
                } else {
                    transactionRepository.countActiveTransactions()
                }
            }

            val page = withContext(Dispatchers.IO) {
                if (range != null) {
                    transactionRepository.getActiveTransactionsPagedInRange(
                        startMillis = range.first,
                        endMillis = range.second,
                        pageSize = pageSize,
                        pageNumber = 0
                    )
                } else {
                    transactionRepository.getActiveTransactionsPaged(
                        pageSize = pageSize,
                        pageNumber = 0
                    )
                }
            }

            currentTransactions.clear()
            currentTransactions.addAll(page)

            val pagination = PaginationState(
                currentPage = 0,
                pageSize = pageSize,
                hasMore = page.size >= pageSize,
                isLoading = false,
                loadedCount = page.size,
                totalCount = totalCount
            )

            rebuildUiState(pagination)
        }
    }

    /**
     * Loads the next page from Room and appends to the current list.
     * Called by the UI when the user scrolls near the bottom.
     */
    fun loadNextPage() {
        val current = _baseUiState.value.pagination
        if (current.isLoading || !current.hasMore) return

        viewModelScope.launch {
            _baseUiState.update { it.copy(pagination = it.pagination.copy(isLoading = true)) }

            val nextPage = current.currentPage + 1
            val range = computePeriodRange()

            val page = withContext(Dispatchers.IO) {
                if (range != null) {
                    transactionRepository.getActiveTransactionsPagedInRange(
                        startMillis = range.first,
                        endMillis = range.second,
                        pageSize = current.pageSize,
                        pageNumber = nextPage
                    )
                } else {
                    transactionRepository.getActiveTransactionsPaged(
                        pageSize = current.pageSize,
                        pageNumber = nextPage
                    )
                }
            }

            currentTransactions.addAll(page)

            val updatedPagination = current.copy(
                currentPage = nextPage,
                hasMore = page.size >= current.pageSize,
                isLoading = false,
                loadedCount = currentTransactions.size
            )

            rebuildUiState(updatedPagination)
        }
    }

    /**
     * Re-queries all currently loaded pages from Room and updates the UI state.
     * Keeps the currently loaded page count intact so the scroll position is preserved.
     */
    private fun reloadCurrentPages() {
        val current = _baseUiState.value.pagination
        val pagesToFetch = (current.currentPage + 1).coerceAtLeast(1)
        val limit = pagesToFetch * current.pageSize

        viewModelScope.launch {
            val range = computePeriodRange()

            val totalCount = withContext(Dispatchers.IO) {
                if (range != null) {
                    transactionRepository.countActiveTransactionsInRange(range.first, range.second)
                } else {
                    transactionRepository.countActiveTransactions()
                }
            }

            val loadedItems = withContext(Dispatchers.IO) {
                if (range != null) {
                    transactionRepository.getActiveTransactionsPagedInRange(
                        startMillis = range.first,
                        endMillis = range.second,
                        pageSize = limit,
                        pageNumber = 0
                    )
                } else {
                    transactionRepository.getActiveTransactionsPaged(
                        pageSize = limit,
                        pageNumber = 0
                    )
                }
            }

            currentTransactions.clear()
            currentTransactions.addAll(loadedItems)

            val updatedPagination = current.copy(
                hasMore = loadedItems.size >= limit,
                isLoading = false,
                loadedCount = loadedItems.size,
                totalCount = totalCount
            )

            rebuildUiState(updatedPagination)
        }
    }

    // ─── Core State Builder ───────────────────────────────────────────

    private fun rebuildUiState(pagination: PaginationState = _baseUiState.value.pagination) {
        viewModelScope.launch {
            val newState = withContext(Dispatchers.Default) {
                calculateNewUiState(pagination)
            }
            _baseUiState.update { newState }
        }
    }

    private fun calculateNewUiState(pagination: PaginationState): TransactionsScreenUiState {
        val availableCategories = currentCategories
            .filter { selectedTransactionTypeIds.contains(it.transactionTypeId) }
            .sortedBy { it.name }

        val appliedMinAmountValue = appliedMinAmount.toDoubleOrNull()
        val appliedMaxAmountValue = appliedMaxAmount.toDoubleOrNull()
        val categoryNames = currentCategories.associate { it.id to it.name }
        val paymentTypeNames = paymentTypeMap.mapValues { it.value.name }
        val normalizedQuery = searchQuery.trim()

        // Apply in-memory filters to the currently loaded page
        val filteredTransactions = sortTransactions(
            currentTransactions.filter { transaction ->
                val paymentName = paymentTypeNames[transaction.paymentTypeId].orEmpty()
                val categoryName = categoryNames[transaction.categoryId].orEmpty()
                val matchesSearchQuery = normalizedQuery.isBlank() ||
                    transaction.note.contains(normalizedQuery, ignoreCase = true) ||
                    transaction.amount.toString().contains(normalizedQuery, ignoreCase = true) ||
                    (advancedSearchGranted && (paymentName.contains(normalizedQuery, ignoreCase = true) ||
                    categoryName.contains(normalizedQuery, ignoreCase = true)))

                matchesSearchQuery &&
                    matchesQuickDateFilter(
                        transactionTimestamp = transaction.createdAt,
                        selectedDateRange = appliedDateRange,
                        anchorTimestamp = System.currentTimeMillis(),
                        customStart = appliedCustomStartDate,
                        customEnd = appliedCustomEndDate
                    ) &&
                    matchesTransactionTypeFilter(
                        transactionTypeId = transaction.transactionTypeId,
                        selectedTransactionTypeIds = appliedTransactionTypeIds
                    ) &&
                    matchesCategoryFilter(
                        categoryId = transaction.categoryId,
                        selectedCategoryIds = appliedCategoryIds
                    ) &&
                    matchesPaymentModeFilter(
                        paymentTypeId = transaction.paymentTypeId,
                        selectedPaymentTypeIds = appliedPaymentTypeIds
                    ) &&
                    matchesAmountRangeFilter(
                        amount = transaction.amount,
                        minAmount = appliedMinAmountValue,
                        maxAmount = appliedMaxAmountValue
                    )
            },
            appliedSortType
        )

        val isFilterApplied = appliedDateRange != null ||
            appliedCategoryIds.isNotEmpty() ||
            appliedPaymentTypeIds.isNotEmpty() ||
            appliedMinAmount.isNotBlank() ||
            appliedMaxAmount.isNotBlank() ||
            appliedTransactionTypeIds.size < 2

        val isSearchActive = searchQuery.trim().isNotEmpty()
        val isFilteredOrSearchApplied = isFilterApplied || isSearchActive

        val formatVal = { value: Double ->
            com.mknlabs.expensetracker.utils.formatCurrencyValue(
                amount = value,
                currencyId = currentCurrencyId,
                amountFormatPreferences = currentAmountFormatPreferences
            )
        }

        val mappedCardItems = filteredTransactions.map { transaction ->
            transaction.toTransactionCardItemUi(
                currencyId = currentCurrencyId,
                amountFormatPreferences = currentAmountFormatPreferences,
                dateFormatPattern = currentDateFormatPattern,
                timeFormat = currentTimeFormat,
                paymentTypeName = paymentTypeNames[transaction.paymentTypeId].orEmpty(),
                categories = currentCategories,
                fallbackCategoryName = application.getString(R.string.label_other) ?: FALLBACK_CATEGORY_NAME
            )
        }

        val shouldGroupTransactions = currentCustomizationSettings.showDateSeparators &&
            selectedPeriodFilter != TransactionPeriodFilter.DAILY

        val showSummaries = currentCustomizationSettings.showTransactionListSummaries
        val transactionItems = mutableListOf<TransactionListItemUi>()
        var pinnedSummary: TransactionListItemUi.SummaryCard? = null

        if (isFilteredOrSearchApplied) {
            val totalIncome = filteredTransactions.filter { it.transactionTypeId == 1 }.sumOf { it.amount }
            val totalExpense = filteredTransactions.filter { it.transactionTypeId != 1 }.sumOf { it.amount }
            if (showSummaries) {
                pinnedSummary = TransactionListItemUi.SummaryCard(
                    id = "summary_filtered_search",
                    totalIncome = formatVal(totalIncome),
                    totalExpense = formatVal(totalExpense),
                    periodLabel = null
                )
            }
            transactionItems.addAll(
                buildTransactionListItems(
                    transactions = mappedCardItems,
                    groupByDate = shouldGroupTransactions,
                    sortType = appliedSortType,
                    todayLabel = application.getString(R.string.label_today) ?: FALLBACK_TODAY_LABEL,
                    yesterdayLabel = application.getString(R.string.label_yesterday) ?: FALLBACK_YESTERDAY_LABEL,
                    tomorrowLabel = application.getString(R.string.label_tomorrow) ?: FALLBACK_TOMORROW_LABEL
                )
            )
        } else {
            when (selectedPeriodFilter) {
                TransactionPeriodFilter.DAILY -> {
                    val totalIncome = filteredTransactions.filter { it.transactionTypeId == 1 }.sumOf { it.amount }
                    val totalExpense = filteredTransactions.filter { it.transactionTypeId != 1 }.sumOf { it.amount }
                    if (showSummaries) {
                        pinnedSummary = TransactionListItemUi.SummaryCard(
                            id = "summary_daily",
                            totalIncome = formatVal(totalIncome),
                            totalExpense = formatVal(totalExpense),
                            periodLabel = null
                        )
                    }
                    transactionItems.addAll(
                        buildTransactionListItems(
                            transactions = mappedCardItems,
                            groupByDate = false,
                            sortType = appliedSortType,
                            todayLabel = application.getString(R.string.label_today) ?: FALLBACK_TODAY_LABEL,
                            yesterdayLabel = application.getString(R.string.label_yesterday) ?: FALLBACK_YESTERDAY_LABEL,
                            tomorrowLabel = application.getString(R.string.label_tomorrow) ?: FALLBACK_TOMORROW_LABEL
                        )
                    )
                }
                TransactionPeriodFilter.MONTHLY -> {
                    val totalIncome = filteredTransactions.filter { it.transactionTypeId == 1 }.sumOf { it.amount }
                    val totalExpense = filteredTransactions.filter { it.transactionTypeId != 1 }.sumOf { it.amount }
                    if (showSummaries) {
                        pinnedSummary = TransactionListItemUi.SummaryCard(
                            id = "summary_monthly",
                            totalIncome = formatVal(totalIncome),
                            totalExpense = formatVal(totalExpense),
                            periodLabel = null
                        )
                    }
                    transactionItems.addAll(
                        buildTransactionListItems(
                            transactions = mappedCardItems,
                            groupByDate = shouldGroupTransactions,
                            sortType = appliedSortType,
                            todayLabel = application.getString(R.string.label_today) ?: FALLBACK_TODAY_LABEL,
                            yesterdayLabel = application.getString(R.string.label_yesterday) ?: FALLBACK_YESTERDAY_LABEL,
                            tomorrowLabel = application.getString(R.string.label_tomorrow) ?: FALLBACK_TOMORROW_LABEL
                        )
                    )
                }
                TransactionPeriodFilter.YEARLY -> {
                    val yearIncome = filteredTransactions.filter { it.transactionTypeId == 1 }.sumOf { it.amount }
                    val yearExpense = filteredTransactions.filter { it.transactionTypeId != 1 }.sumOf { it.amount }
                    if (showSummaries) {
                        pinnedSummary = TransactionListItemUi.SummaryCard(
                            id = "summary_yearly",
                            totalIncome = formatVal(yearIncome),
                            totalExpense = formatVal(yearExpense),
                            periodLabel = null
                        )
                    }
                    val cal = Calendar.getInstance()
                    val groupedByMonth = mappedCardItems.groupBy { item ->
                        cal.timeInMillis = item.transaction.createdAt
                        cal.get(Calendar.MONTH)
                    }

                    val sortedMonthKeys = if (appliedSortType == SortType.OLDEST) {
                        groupedByMonth.keys.sorted()
                    } else {
                        groupedByMonth.keys.sortedDescending()
                    }

                    sortedMonthKeys.forEach { monthKey ->
                        val monthTransactions = groupedByMonth[monthKey].orEmpty()
                        val monthIncome = monthTransactions.filter { it.transactionTypeId == 1 }.sumOf { it.transaction.amount }
                        val monthExpense = monthTransactions.filter { it.transactionTypeId != 1 }.sumOf { it.transaction.amount }

                        cal.set(Calendar.MONTH, monthKey)
                        val monthLabel = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)

                        if (showSummaries) {
                            transactionItems.add(
                                TransactionListItemUi.SummaryCard(
                                    id = "summary_yearly_month_$monthKey",
                                    totalIncome = formatVal(monthIncome),
                                    totalExpense = formatVal(monthExpense),
                                    periodLabel = monthLabel
                                )
                            )
                        }

                        transactionItems.addAll(
                            buildTransactionListItems(
                                transactions = monthTransactions,
                                groupByDate = shouldGroupTransactions,
                                sortType = appliedSortType,
                                todayLabel = application.getString(R.string.label_today) ?: FALLBACK_TODAY_LABEL,
                                yesterdayLabel = application.getString(R.string.label_yesterday) ?: FALLBACK_YESTERDAY_LABEL,
                                tomorrowLabel = application.getString(R.string.label_tomorrow) ?: FALLBACK_TOMORROW_LABEL
                            )
                        )
                    }
                }
                TransactionPeriodFilter.ALL -> {
                    transactionItems.addAll(
                        buildTransactionListItems(
                            transactions = mappedCardItems,
                            groupByDate = shouldGroupTransactions,
                            sortType = appliedSortType,
                            todayLabel = application.getString(R.string.label_today) ?: FALLBACK_TODAY_LABEL,
                            yesterdayLabel = application.getString(R.string.label_yesterday) ?: FALLBACK_YESTERDAY_LABEL,
                            tomorrowLabel = application.getString(R.string.label_tomorrow) ?: FALLBACK_TOMORROW_LABEL
                        )
                    )
                }
            }
        }

        // Ad injection: insert ads after every 5th transaction row.
        val itemsWithAds = ArrayList<TransactionListItemUi>(transactionItems.size + transactionItems.size / 5 + 1)
        var rowIndex = 0
        var adCount = 0
        transactionItems.forEach { item ->
            itemsWithAds.add(item)
            if (item is TransactionListItemUi.TransactionRow) {
                rowIndex++
                if (rowIndex % 5 == 0) {
                    adCount++
                    val placement = if (adCount % 2 == 1) {
                        AdPlacement.TRANSACTIONS_LIST
                    } else {
                        AdPlacement.TRANSACTIONS_LIST_2
                    }
                    itemsWithAds.add(TransactionListItemUi.Ad(id = "ad_$rowIndex", placement = placement))
                }
            }
        }

        // Period navigation check — use hasMore from pagination for "next" direction,
        // and check if there are any items for "previous".
        val canNavigateForward = pagination.hasMore ||
            currentTransactions.any {
                matchesSelectedPeriod(
                    transactionTimestamp = it.createdAt,
                    focusedTimestamp = shiftPeriod(focusedPeriodTimestamp, selectedPeriodFilter, 1),
                    filter = selectedPeriodFilter
                )
            }
        val canNavigateBackward = currentTransactions.any {
            matchesSelectedPeriod(
                transactionTimestamp = it.createdAt,
                focusedTimestamp = shiftPeriod(focusedPeriodTimestamp, selectedPeriodFilter, -1),
                filter = selectedPeriodFilter
            )
        }

        return _baseUiState.value.copy(
            searchQuery = searchQuery,
            selectedSort = selectedSort,
            selectedOrder = selectedOrder,
            selectedDateRange = selectedDateRange,
            selectedCustomStartDate = customStartDate,
            selectedCustomEndDate = customEndDate,
            selectedTransactionTypeIds = selectedTransactionTypeIds,
            selectedCategoryIds = selectedCategoryIds,
            selectedPaymentTypeIds = selectedPaymentTypeIds,
            selectedMinAmount = selectedMinAmount,
            selectedMaxAmount = selectedMaxAmount,
            selectedPeriodFilter = selectedPeriodFilter,
            focusedPeriodTimestamp = focusedPeriodTimestamp,
            canNavigateBackward = canNavigateBackward,
            canNavigateForward = canNavigateForward,
            selectedPeriodLabel = buildPeriodLabel(
                timestamp = focusedPeriodTimestamp,
                filter = selectedPeriodFilter,
                dateFormatPattern = currentDateFormatPattern
            ),
            availableCategories = availableCategories,
            paymentModes = paymentTypeMap.values.toList(),
            transactionItems = itemsWithAds,
            pinnedSummary = pinnedSummary,
            customizationSettings = currentCustomizationSettings,
            pagination = pagination
        )
    }

    /**
     * Computes the time range boundaries for the current period filter.
     * Returns null for ALL filter (no range restriction).
     */
    private fun computePeriodRange(): Pair<Long, Long>? {
        return when (selectedPeriodFilter) {
            TransactionPeriodFilter.ALL -> null
            TransactionPeriodFilter.DAILY -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = focusedPeriodTimestamp
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 1)
                Pair(start, cal.timeInMillis)
            }
            TransactionPeriodFilter.MONTHLY -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = focusedPeriodTimestamp
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                Pair(start, cal.timeInMillis)
            }
            TransactionPeriodFilter.YEARLY -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = focusedPeriodTimestamp
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = cal.timeInMillis
                cal.add(Calendar.YEAR, 1)
                Pair(start, cal.timeInMillis)
            }
        }
    }

    private fun buildPeriodLabel(
        timestamp: Long,
        filter: TransactionPeriodFilter,
        dateFormatPattern: String
    ): UiText {
        val date = Date(timestamp)

        return when (filter) {
            TransactionPeriodFilter.ALL -> UiText.res(R.string.label_all_records)
            TransactionPeriodFilter.DAILY -> UiText.dynamic(formatDate(timestamp, dateFormatPattern))
            TransactionPeriodFilter.MONTHLY -> UiText.dynamic(SimpleDateFormat(application.getString(R.string.date_pattern_month_year_comma) ?: DEFAULT_MONTH_YEAR_PATTERN, Locale.getDefault()).format(date))
            TransactionPeriodFilter.YEARLY -> UiText.dynamic(SimpleDateFormat(application.getString(R.string.date_pattern_year) ?: DEFAULT_YEAR_PATTERN, Locale.getDefault()).format(date))
        }
    }
}

private fun matchesQuickDateFilter(
    transactionTimestamp: Long,
    selectedDateRange: String?,
    anchorTimestamp: Long,
    customStart: Long? = null,
    customEnd: Long? = null
): Boolean {
    if (selectedDateRange == KEY_CUSTOM_RANGE && customStart != null && customEnd != null) {
        val startCal = Calendar.getInstance().apply {
            timeInMillis = customStart
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            timeInMillis = customEnd
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return transactionTimestamp in startCal.timeInMillis..endCal.timeInMillis
    }

    val rangeDays = when (selectedDateRange) {
        FILTER_DATE_LAST_7_DAYS -> 7
        FILTER_DATE_LAST_15_DAYS -> 15
        FILTER_DATE_LAST_30_DAYS -> 30
        FILTER_DATE_LAST_60_DAYS -> 60
        null -> return true
        else -> return true
    }

    val calendar = Calendar.getInstance().apply {
        timeInMillis = anchorTimestamp
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val endTimestamp = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, -(rangeDays - 1))
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startTimestamp = calendar.timeInMillis

    return transactionTimestamp in startTimestamp..endTimestamp
}

private fun matchesTransactionTypeFilter(
    transactionTypeId: Int,
    selectedTransactionTypeIds: Set<Int>
): Boolean {
    return selectedTransactionTypeIds.contains(transactionTypeId)
}

private fun matchesCategoryFilter(
    categoryId: Int,
    selectedCategoryIds: Set<Int>
): Boolean {
    return selectedCategoryIds.isEmpty() || selectedCategoryIds.contains(categoryId)
}

private fun matchesPaymentModeFilter(
    paymentTypeId: Int,
    selectedPaymentTypeIds: Set<Int>
): Boolean {
    return selectedPaymentTypeIds.isEmpty() || selectedPaymentTypeIds.contains(paymentTypeId)
}

private fun matchesAmountRangeFilter(
    amount: Double,
    minAmount: Double?,
    maxAmount: Double?
): Boolean {
    val matchesMin = minAmount == null || amount >= minAmount
    val matchesMax = maxAmount == null || amount <= maxAmount
    return matchesMin && matchesMax
}

private fun matchesSelectedPeriod(
    transactionTimestamp: Long,
    focusedTimestamp: Long,
    filter: TransactionPeriodFilter
): Boolean {
    if (filter == TransactionPeriodFilter.ALL) {
        return true
    }

    val transactionCalendar = Calendar.getInstance().apply { timeInMillis = transactionTimestamp }
    val focusedCalendar = Calendar.getInstance().apply { timeInMillis = focusedTimestamp }

    return when (filter) {
        TransactionPeriodFilter.ALL -> true
        TransactionPeriodFilter.DAILY -> {
            transactionCalendar.get(Calendar.YEAR) == focusedCalendar.get(Calendar.YEAR) &&
                transactionCalendar.get(Calendar.DAY_OF_YEAR) == focusedCalendar.get(Calendar.DAY_OF_YEAR)
        }

        TransactionPeriodFilter.MONTHLY -> {
            transactionCalendar.get(Calendar.YEAR) == focusedCalendar.get(Calendar.YEAR) &&
                transactionCalendar.get(Calendar.MONTH) == focusedCalendar.get(Calendar.MONTH)
        }

        TransactionPeriodFilter.YEARLY -> {
            transactionCalendar.get(Calendar.YEAR) == focusedCalendar.get(Calendar.YEAR)
        }
    }
}

private fun shiftPeriod(
    timestamp: Long,
    filter: TransactionPeriodFilter,
    step: Int
): Long {
    if (filter == TransactionPeriodFilter.ALL) {
        return timestamp
    }

    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        when (filter) {
            TransactionPeriodFilter.ALL -> Unit
            TransactionPeriodFilter.DAILY -> add(Calendar.DAY_OF_YEAR, step)
            TransactionPeriodFilter.MONTHLY -> add(Calendar.MONTH, step)
            TransactionPeriodFilter.YEARLY -> add(Calendar.YEAR, step)
        }
    }.timeInMillis
}

private fun Set<Int>.toggle(id: Int): Set<Int> {
    return if (contains(id)) this - id else this + id
}
