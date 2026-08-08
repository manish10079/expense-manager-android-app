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
    val isDragging: Boolean = false
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

    private var currentTransactions: List<Transaction> = emptyList()
    private var currentCategories: List<CategoryType> = emptyList()

    private val _selectedTransactionIds = MutableStateFlow<Set<String>>(emptySet())
    private val _isSelectionMode = MutableStateFlow(false)

    private var currentCurrencyId: Int = DEFAULT_CURRENCY_ID
    private var currentAmountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences
    private var currentDateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN
    private var currentTimeFormat: String = DEFAULT_TIME_FORMAT
    private var currentCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings()
    private var latestTransactionTimestamp: Long = System.currentTimeMillis()

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
    private var focusedPeriodTimestamp: Long = latestTransactionTimestamp

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
        rebuildUiState()
    }

    private fun observeAdvancedSearchAccess() {
        viewModelScope.launch {
            observeAccessStatusUseCase(Feature.ADVANCED_SEARCH_SCOPE).collect { status ->
                advancedSearchGranted = status is AccessStatus.Granted
                rebuildUiState()
            }
        }
    }

    fun updateInputs(
        transactions: List<Transaction>,
        categories: List<CategoryType>,
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences,
        dateFormatPattern: String,
        timeFormat: String,
        customizationSettings: TransactionCardCustomizationSettings
    ) {
        currentTransactions = transactions
        currentCategories = categories
        currentCurrencyId = currencyId
        currentAmountFormatPreferences = amountFormatPreferences
        currentDateFormatPattern = dateFormatPattern
        currentTimeFormat = timeFormat
        currentCustomizationSettings = customizationSettings
        latestTransactionTimestamp = transactions.maxOfOrNull { it.createdAt } ?: System.currentTimeMillis()
        if (focusedPeriodTimestamp == 0L) {
            focusedPeriodTimestamp = System.currentTimeMillis()
        }
        rebuildUiState()
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        // Update the state immediately for the text field to prevent cursor jumping
        _baseUiState.update { it.copy(searchQuery = query) }
        rebuildUiState()
    }

    fun updateSort(sort: String) {
        selectedSort = sort
        selectedOrder = getDefaultOrder(sort)
        rebuildUiState()
    }

    fun updateOrder(order: SortType) {
        selectedOrder = order
        rebuildUiState()
    }

    fun updateDateRange(dateRange: String?) {
        selectedDateRange = dateRange
        rebuildUiState()
    }

    fun updateCustomDateRange(start: Long?, end: Long?) {
        customStartDate = start
        customEndDate = end ?: start
        selectedDateRange = KEY_CUSTOM_RANGE
        rebuildUiState()
    }

    fun toggleTransactionTypeFilter(transactionTypeId: Int) {
        selectedTransactionTypeIds = selectedTransactionTypeIds.toggle(transactionTypeId)
        selectedCategoryIds = emptySet()
        rebuildUiState()
    }

    fun toggleCategory(categoryId: Int) {
        selectedCategoryIds = selectedCategoryIds.toggle(categoryId)
        rebuildUiState()
    }

    fun togglePaymentMode(paymentTypeId: Int) {
        selectedPaymentTypeIds = selectedPaymentTypeIds.toggle(paymentTypeId)
        rebuildUiState()
    }

    fun updateMinAmount(amount: String) {
        selectedMinAmount = amount
        rebuildUiState()
    }

    fun updateMaxAmount(amount: String) {
        selectedMaxAmount = amount
        rebuildUiState()
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
        rebuildUiState()
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

    fun updatePeriodFilter(filter: TransactionPeriodFilter) {
        selectedPeriodFilter = filter
        focusedPeriodTimestamp = System.currentTimeMillis()
        clearSelection()
        rebuildUiState()
    }

    fun navigatePeriod(step: Int) {
        focusedPeriodTimestamp = shiftPeriod(
            timestamp = focusedPeriodTimestamp,
            filter = selectedPeriodFilter,
            step = step
        )
        clearSelection()
        rebuildUiState()
    }

    fun jumpToPeriod(millis: Long) {
        focusedPeriodTimestamp = millis
        clearSelection()
        rebuildUiState()
    }

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

    fun selectAll() {
        val allIds = _baseUiState.value.transactionItems
            .filterIsInstance<TransactionListItemUi.TransactionRow>()
            .map { it.card.id }
            .toSet()
        
        val currentlySelected = _selectedTransactionIds.value
        
        // If everything is already selected, clear it. Otherwise, select all.
        if (currentlySelected.size >= allIds.size && allIds.isNotEmpty()) {
            clearSelection()
        } else {
            _selectedTransactionIds.value = allIds
            _isSelectionMode.value = allIds.isNotEmpty()
        }
    }

    fun deleteSelectedTransactions() {
        val idsToDelete = _selectedTransactionIds.value.toList()
        if (idsToDelete.isEmpty()) return

        viewModelScope.launch {
            transactionRepository.softDeleteTransactions(idsToDelete)
            clearSelection()
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

    private fun rebuildUiState() {
        viewModelScope.launch {
            val newState = withContext(Dispatchers.Default) {
                calculateNewUiState()
            }
            _baseUiState.update { newState }
        }
    }

    private fun calculateNewUiState(): TransactionsScreenUiState {
        val availableCategories = currentCategories
            .filter { selectedTransactionTypeIds.contains(it.transactionTypeId) }
            .sortedBy { it.name }

        val appliedMinAmountValue = appliedMinAmount.toDoubleOrNull()
        val appliedMaxAmountValue = appliedMaxAmount.toDoubleOrNull()
        val categoryNames = currentCategories.associate { it.id to it.name }
        val paymentTypeNames = paymentTypeMap.mapValues { it.value.name }
        val normalizedQuery = searchQuery.trim()

        val filteredTransactions = sortTransactions(
            currentTransactions.filter { transaction ->
                val paymentName = paymentTypeNames[transaction.paymentTypeId].orEmpty()
                val categoryName = categoryNames[transaction.categoryId].orEmpty()
                val matchesSearchQuery = normalizedQuery.isBlank() ||
                    transaction.note.contains(normalizedQuery, ignoreCase = true) ||
                    transaction.amount.toString().contains(normalizedQuery, ignoreCase = true) ||
                    (advancedSearchGranted && (paymentName.contains(normalizedQuery, ignoreCase = true) ||
                    categoryName.contains(normalizedQuery, ignoreCase = true)))

                matchesSelectedPeriod(
                    transactionTimestamp = transaction.createdAt,
                    focusedTimestamp = focusedPeriodTimestamp,
                    filter = selectedPeriodFilter
                ) &&
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
                    /* FUTURE EXTENSION: 
                       If you want to add a yearly/all summary in All View in the future, 
                       calculate and add a SummaryCard here:
                       
                       val totalIncome = filteredTransactions.filter { it.transactionTypeId == 1 }.sumOf { it.amount }
                       val totalExpense = filteredTransactions.filter { it.transactionTypeId != 1 }.sumOf { it.amount }
                       transactionItems.add(
                           TransactionListItemUi.SummaryCard(
                               id = "summary_all",
                               totalIncome = formatVal(totalIncome),
                               totalExpense = formatVal(totalExpense),
                               periodLabel = "All Time"
                           )
                       )
                    */
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

        // Phase 2 (ADS_UI_JANK_FIX_PLAN §5): inject the list ad as its own dedicated item after
        // every 5th transaction row. Each ad gets a stable key ("ad_5", "ad_10", ...) so its
        // composition is independent of transaction-card recompositions and Compose recycles the
        // ad's AndroidView across scroll entries instead of re-inflating it. Runs on
        // Dispatchers.Default, so no list scanning happens on the main thread.
        val itemsWithAds = ArrayList<TransactionListItemUi>(transactionItems.size + transactionItems.size / 5 + 1)
        var rowIndex = 0
        transactionItems.forEach { item ->
            itemsWithAds.add(item)
            if (item is TransactionListItemUi.TransactionRow) {
                rowIndex++
                if (rowIndex % 5 == 0) {
                    itemsWithAds.add(TransactionListItemUi.Ad(id = "ad_$rowIndex"))
                }
            }
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
            canNavigateBackward = canNavigateToPeriod(
                transactions = currentTransactions,
                focusedTimestamp = focusedPeriodTimestamp,
                filter = selectedPeriodFilter,
                direction = -1
            ),
            canNavigateForward = canNavigateToPeriod(
                transactions = currentTransactions,
                focusedTimestamp = focusedPeriodTimestamp,
                filter = selectedPeriodFilter,
                direction = 1
            ),
            selectedPeriodLabel = buildPeriodLabel(
                timestamp = focusedPeriodTimestamp,
                filter = selectedPeriodFilter,
                dateFormatPattern = currentDateFormatPattern
            ),
            availableCategories = availableCategories,
            paymentModes = paymentTypeMap.values.toList(),
            transactionItems = itemsWithAds,
            pinnedSummary = pinnedSummary,
            customizationSettings = currentCustomizationSettings
        )
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

private fun canNavigateToPeriod(
    transactions: List<Transaction>,
    focusedTimestamp: Long,
    filter: TransactionPeriodFilter,
    direction: Int
): Boolean {
    if (filter == TransactionPeriodFilter.ALL) {
        return false
    }

    val candidateTimestamp = shiftPeriod(
        timestamp = focusedTimestamp,
        filter = filter,
        step = direction
    )

    return transactions.any { transaction ->
        matchesSelectedPeriod(
            transactionTimestamp = transaction.createdAt,
            focusedTimestamp = candidateTimestamp,
            filter = filter
        )
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
