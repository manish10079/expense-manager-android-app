package com.mknlabs.expensetracker.feature.transactions.ui

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
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.getDefaultOrder
import com.mknlabs.expensetracker.utils.toMinorUnits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import com.mknlabs.expensetracker.domain.repository.TransactionQuery
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.domain.repository.TransactionTotals
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.domain.usecase.ObserveAccessStatusUseCase

import com.mknlabs.expensetracker.utils.UiText

@Immutable
data class TransactionsScreenUiState(
    val searchQuery: String = "",
    val selectedSort: String = DEFAULT_SORT_BY,
    val selectedOrder: SortType = DEFAULT_SORT_ORDER,
    /** Sort actually applied to the query; the list groups/orders days by this. */
    val appliedSortType: SortType = DEFAULT_SORT_ORDER,
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
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    val isSelectionMode: Boolean = false,
    val selectedTransactionIds: Set<String> = emptySet(),
    val isDragging: Boolean = false,
    val isFilterActive: Boolean = false,
    /** Rows matching the current query, loaded or not. Drives "select all N in this view". */
    val totalTransactionCount: Int = 0,
    /** Income total for the whole query — not just the pages loaded in memory. */
    val summaryIncomeMinor: Long = 0,
    /** Expense total for the whole query — not just the pages loaded in memory. */
    val summaryExpenseMinor: Long = 0,
    /** True while the totals for the current query are still being computed. */
    val isSummaryLoading: Boolean = true
)

private const val KEY_CUSTOM_RANGE = "KEY_CUSTOM_RANGE"

/**
 * Free-text filters (search, min/max amount) wait this long after the last
 * keystroke before rebuilding the query, so typing does not fire a paging reload
 * plus a totals aggregate per character.
 */
private const val FILTER_DEBOUNCE_MS = 250L
private const val DEFAULT_MONTH_YEAR_PATTERN = "MMM, yyyy"
private const val DEFAULT_YEAR_PATTERN = "yyyy"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val application: Application,
    private val transactionRepository: TransactionRepository,
    private val observeAccessStatusUseCase: ObserveAccessStatusUseCase
) : ViewModel() {

    private var currentCategories: List<CategoryType> = emptyList()
    private var currentPaymentMethods: List<PaymentType> = emptyList()

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

    /** Overridable so tests can exercise the filter path without virtual-time delays. */
    internal var filterDebounceMillis: Long = FILTER_DEBOUNCE_MS

    private var filterDebounceJob: Job? = null

    // ─── Paging 3 ─────────────────────────────────────────────────────
    // The screen consumes `transactions` with collectAsLazyPagingItems(); pages load
    // as the list scrolls and Room invalidates the stream whenever the table
    // changes, so nothing here swaps pages by hand.
    private val pagedQuery = MutableStateFlow(buildQuery())

    val transactions: Flow<PagingData<Transaction>> = pagedQuery
        .flatMapLatest { query -> transactionRepository.getTransactionsPaging(query) }
        .cachedIn(viewModelScope)

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
        publishUiState()
        pagedQuery.value = buildQuery()
        observeSummaryTotals()
        refreshNavigationFlags()
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
        paymentMethods: List<PaymentType>,
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences,
        dateFormatPattern: String,
        timeFormat: String,
        customizationSettings: TransactionCardCustomizationSettings
    ) {
        val inputsChanged = currentCategories != categories ||
            currentPaymentMethods != paymentMethods ||
            currentCurrencyId != currencyId ||
            currentDateFormatPattern != dateFormatPattern ||
            currentTimeFormat != timeFormat ||
            currentCustomizationSettings != customizationSettings

        currentCategories = categories
        currentPaymentMethods = paymentMethods
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
        // The text field and search chip update immediately; only the query rebuild
        // is debounced, so typing stays responsive.
        publishUiState()
        applyFiltersDebounced()
    }

    fun updateSort(sort: String) {
        selectedSort = sort
        selectedOrder = getDefaultOrder(sort)
        applyFilters()
    }

    fun updateOrder(order: SortType) {
        selectedOrder = order
        applyFilters()
    }

    fun updateDateRange(dateRange: String?) {
        selectedDateRange = dateRange
        applyFilters()
    }

    fun updateCustomDateRange(start: Long?, end: Long?) {
        customStartDate = start
        customEndDate = end ?: start
        selectedDateRange = KEY_CUSTOM_RANGE
        applyFilters()
    }

    fun toggleTransactionTypeFilter(transactionTypeId: Int) {
        selectedTransactionTypeIds = selectedTransactionTypeIds.toggle(transactionTypeId)
        selectedCategoryIds = emptySet()
        applyFilters()
    }

    fun toggleCategory(categoryId: Int) {
        selectedCategoryIds = selectedCategoryIds.toggle(categoryId)
        applyFilters()
    }

    fun togglePaymentMode(paymentTypeId: Int) {
        selectedPaymentTypeIds = selectedPaymentTypeIds.toggle(paymentTypeId)
        applyFilters()
    }

    fun updateMinAmount(amount: String) {
        selectedMinAmount = amount
        publishUiState()
        applyFiltersDebounced()
    }

    fun updateMaxAmount(amount: String) {
        selectedMaxAmount = amount
        publishUiState()
        applyFiltersDebounced()
    }

    /** Commits the current filter state once the user stops typing. */
    private fun applyFiltersDebounced() {
        filterDebounceJob?.cancel()
        filterDebounceJob = viewModelScope.launch {
            delay(filterDebounceMillis)
            applyFilters()
        }
    }

    /**
     * Commits the draft filter selection to the active query. Every filter action
     * funnels through here, so what the chips show and what SQL filters are always
     * the same thing.
     */
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
        // "Clear all" on the filter chips also clears the search pill, so reset the
        // search text too.
        searchQuery = ""
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
     * Selects (or, when everything is already selected, clears) the transactions
     * the screen currently has loaded. The screen owns the loaded pages, so it
     * passes their ids in.
     */
    fun selectAll(loadedIds: Set<String>) {
        if (loadedIds.isEmpty()) return
        val currentlySelected = _selectedTransactionIds.value
        if (currentlySelected.isNotEmpty() && currentlySelected.containsAll(loadedIds)) {
            clearSelection()
        } else {
            _selectedTransactionIds.value = loadedIds
            _isSelectionMode.value = true
        }
    }

    /**
     * Selects every transaction matching the current query, including rows that
     * have not been paged in yet. Backed by a dedicated id-only SQL query, so it
     * does not have to walk every page through Paging.
     */
    fun selectAllInQuery() {
        val query = pagedQuery.value
        viewModelScope.launch {
            val ids = withContext(Dispatchers.IO) {
                transactionRepository.getTransactionIds(query)
            }.toSet()

            if (ids.isNotEmpty()) {
                _selectedTransactionIds.value = ids
                _isSelectionMode.value = true
            }
        }
    }

    fun deleteSelectedTransactions() {
        val idsToDelete = _selectedTransactionIds.value.toList()
        if (idsToDelete.isEmpty()) return

        viewModelScope.launch {
            transactionRepository.softDeleteTransactions(idsToDelete)
            clearSelection()
            // Both the list and the totals refresh through Room invalidation.
        }
    }

    // ─── Query & State ────────────────────────────────────────────────

    /** Publishes filter/summary state and swaps the Paging source to the new query. */
    private fun resetAndReload() {
        publishUiState()
        pagedQuery.value = buildQuery()
        refreshNavigationFlags()
    }

    private fun publishUiState() {
        _baseUiState.update {
            it.copy(
                searchQuery = searchQuery,
                selectedSort = selectedSort,
                selectedOrder = selectedOrder,
                appliedSortType = appliedSortType,
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
                selectedPeriodLabel = buildPeriodLabel(
                    timestamp = focusedPeriodTimestamp,
                    filter = selectedPeriodFilter,
                    dateFormatPattern = currentDateFormatPattern
                ),
                availableCategories = currentCategories
                    .filter { selectedTransactionTypeIds.contains(it.transactionTypeId) }
                    .sortedBy { it.name },
                paymentModes = paymentTypeMap.values.toList(),
                customizationSettings = currentCustomizationSettings,
                isFilterActive = computeIsFilterActive()
            )
        }
    }

    private fun computeIsFilterActive(): Boolean {
        return appliedDateRange != null ||
            appliedCategoryIds.isNotEmpty() ||
            appliedPaymentTypeIds.isNotEmpty() ||
            appliedMinAmount.isNotBlank() ||
            appliedMaxAmount.isNotBlank() ||
            appliedTransactionTypeIds.size < 2 ||
            searchQuery.trim().isNotEmpty()
    }

    /**
     * Keeps the summary card and "select all N in this view" honest.
     *
     * The totals come from a single aggregate over the same SQL the list uses, so
     * they cover the whole filtered set rather than the pages Paging has loaded.
     *
     * `flatMapLatest` restarts the aggregate whenever the query changes, so
     * `onStart` gives a real loading signal per query change — while a plain table
     * write re-emits on the same flow and updates the numbers in place, with no
     * loading flash.
     */
    private fun observeSummaryTotals() {
        viewModelScope.launch {
            pagedQuery
                .flatMapLatest { query ->
                    transactionRepository.observeTransactionTotals(query)
                        .map<TransactionTotals, TransactionTotals?> { it }
                        .onStart { emit(null) }
                }
                .collect { totals ->
                    if (totals == null) {
                        _baseUiState.update { it.copy(isSummaryLoading = true) }
                    } else {
                        _baseUiState.update {
                            it.copy(
                                isSummaryLoading = false,
                                summaryIncomeMinor = totals.incomeMinor,
                                summaryExpenseMinor = totals.expenseMinor,
                                totalTransactionCount = totals.totalCount
                            )
                        }
                        // A write can also add or remove the only row of an adjacent
                        // period, so re-derive the arrows alongside the totals.
                        refreshNavigationFlags()
                    }
                }
        }
    }

    /** Whether the periods either side of the focused one contain any transactions. */
    private fun refreshNavigationFlags() {
        viewModelScope.launch {
            val canNavigateBackward = withContext(Dispatchers.IO) { hasTransactionsInPeriod(-1) }
            val canNavigateForward = withContext(Dispatchers.IO) { hasTransactionsInPeriod(1) }

            _baseUiState.update {
                it.copy(
                    canNavigateBackward = canNavigateBackward,
                    canNavigateForward = canNavigateForward
                )
            }
        }
    }

    private suspend fun hasTransactionsInPeriod(step: Int): Boolean {
        val range = periodRange(
            timestamp = shiftPeriod(focusedPeriodTimestamp, selectedPeriodFilter, step),
            filter = selectedPeriodFilter
        ) ?: return false
        return transactionRepository.hasTransactionsInRange(range.first, range.second)
    }

    /**
     * Builds the SQL-backed query from the committed filters. Search is matched in
     * SQL against the note and amount; when advanced search is granted, names of
     * matching categories/payment methods are resolved to ids and OR-ed in.
     */
    private fun buildQuery(): TransactionQuery {
        val window = currentQueryWindow()
        val searchText = searchQuery.trim()
        val advancedSearch = searchText.isNotEmpty() && advancedSearchGranted

        return TransactionQuery(
            startMillis = window?.first ?: TransactionQuery.NO_START,
            endMillis = window?.second ?: TransactionQuery.NO_END,
            search = searchText.takeIf { it.isNotEmpty() },
            searchCategoryIds = if (advancedSearch) {
                currentCategories
                    .filter { it.name.contains(searchText, ignoreCase = true) }
                    .map { it.id }
            } else {
                emptyList()
            },
            searchPaymentTypeIds = if (advancedSearch) {
                (paymentTypeMap.values.toList() + currentPaymentMethods)
                    .filter { it.name.contains(searchText, ignoreCase = true) }
                    .map { it.id }
                    .distinct()
            } else {
                emptyList()
            },
            transactionTypeIds = appliedTransactionTypeIds.toList().sorted(),
            categoryIds = appliedCategoryIds.toList().sorted(),
            paymentTypeIds = appliedPaymentTypeIds.toList().sorted(),
            minAmountMinor = appliedMinAmount.toDoubleOrNull()?.toMinorUnits(),
            maxAmountMinor = appliedMaxAmount.toDoubleOrNull()?.toMinorUnits(),
            sort = appliedSortType
        )
    }

    /**
     * Effective `occurred_at` window: the period filter intersected with the quick
     * date-range filter. Null means unbounded.
     */
    private fun currentQueryWindow(): Pair<Long, Long>? {
        val period = periodRange(focusedPeriodTimestamp, selectedPeriodFilter)
        val quick = quickDateRangeWindow()
        return when {
            period == null -> quick
            quick == null -> period
            else -> Pair(maxOf(period.first, quick.first), minOf(period.second, quick.second))
        }
    }

    private fun quickDateRangeWindow(): Pair<Long, Long>? {
        if (appliedDateRange == KEY_CUSTOM_RANGE) {
            val start = appliedCustomStartDate ?: return null
            val end = appliedCustomEndDate ?: start
            val startCalendar = Calendar.getInstance().apply {
                timeInMillis = start
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCalendar = Calendar.getInstance().apply {
                timeInMillis = end
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            // +1 makes the inclusive end day half-open, matching the query bounds.
            return Pair(startCalendar.timeInMillis, endCalendar.timeInMillis + 1)
        }

        val rangeDays = when (appliedDateRange) {
            FILTER_DATE_LAST_7_DAYS -> 7
            FILTER_DATE_LAST_15_DAYS -> 15
            FILTER_DATE_LAST_30_DAYS -> 30
            FILTER_DATE_LAST_60_DAYS -> 60
            else -> return null
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val end = calendar.timeInMillis + 1
        calendar.add(Calendar.DAY_OF_YEAR, -(rangeDays - 1))
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return Pair(calendar.timeInMillis, end)
    }

    /**
     * Half-open boundaries for [filter] anchored on [timestamp].
     * Returns null for ALL (no range restriction).
     */
    private fun periodRange(
        timestamp: Long,
        filter: TransactionPeriodFilter
    ): Pair<Long, Long>? {
        return when (filter) {
            TransactionPeriodFilter.ALL -> null
            TransactionPeriodFilter.DAILY -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = timestamp
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
                    timeInMillis = timestamp
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
                    timeInMillis = timestamp
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
