package com.mkn0079.expensetracker.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mkn0079.expensetracker.data.constants.currencyMap
import com.mkn0079.expensetracker.data.repository.ExpenseTrackerRepositoryProvider
import com.mkn0079.expensetracker.domain.usecase.GetAppPreferencesUseCase
import com.mkn0079.expensetracker.domain.usecase.UpdateCurrencyDecimalPlacesUseCase
import com.mkn0079.expensetracker.domain.usecase.UpdateCurrencyGroupingStyleUseCase
import com.mkn0079.expensetracker.domain.usecase.UpdateCurrencyUseCase
import com.mkn0079.expensetracker.domain.usecase.UpdateDateFormatUseCase
import com.mkn0079.expensetracker.domain.usecase.UpdateTimeFormatUseCase
import com.mkn0079.expensetracker.models.Currency
import com.mkn0079.expensetracker.models.CurrencyGroupingStyle
import com.mkn0079.expensetracker.utils.formatNumberValue
import com.mkn0079.expensetracker.utils.getDateFormatPreviewLabel
import com.mkn0079.expensetracker.utils.getTimeFormatPreviewLabel
import com.mkn0079.expensetracker.utils.toDisplayLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PreferencesSheetType {
    Currency,
    DateFormat,
    TimeFormat,
    NumberFormat,
    DecimalPlaces
}

@Immutable
data class NumberFormatOptionUi(
    val groupingStyle: CurrencyGroupingStyle,
    val label: String,
    val preview: String
)

@Immutable
data class DecimalPlacesOptionUi(
    val value: Int,
    val preview: String
)

@Immutable
data class PreferencesScreenUiState(
    val selectedCurrencyId: Int = DEFAULT_CURRENCY_ID,
    val selectedDateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    val selectedTimeFormat: String = DEFAULT_TIME_FORMAT,
    val selectedGroupingStyle: CurrencyGroupingStyle = CurrencyGroupingStyle.INDIAN,
    val selectedDecimalPlaces: Int = 2,
    val currentCurrencyLabel: String = "Select",
    val currentDateFormatLabel: String = getDateFormatPreviewLabel(DEFAULT_DATE_FORMAT_PATTERN),
    val currentTimeFormatLabel: String = getTimeFormatPreviewLabel(DEFAULT_TIME_FORMAT),
    val currentGroupingLabel: String = CurrencyGroupingStyle.INDIAN.toDisplayLabel(),
    val currentDecimalPlacesLabel: String = "2",
    val currencySearchQuery: String = "",
    val filteredCurrencies: List<Currency> = emptyList(),
    val numberFormatOptions: List<NumberFormatOptionUi> = emptyList(),
    val decimalPlaceOptions: List<DecimalPlacesOptionUi> = emptyList(),
    val activeSheet: PreferencesSheetType? = null
)

class PreferencesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseTrackerRepositoryProvider
        .appPreferencesRepository(application.applicationContext)
    private val getAppPreferences = GetAppPreferencesUseCase(repository)
    private val updateCurrency = UpdateCurrencyUseCase(repository)
    private val updateDateFormat = UpdateDateFormatUseCase(repository)
    private val updateTimeFormat = UpdateTimeFormatUseCase(repository)
    private val updateCurrencyGroupingStyle = UpdateCurrencyGroupingStyleUseCase(repository)
    private val updateCurrencyDecimalPlaces = UpdateCurrencyDecimalPlacesUseCase(repository)

    private val allCurrencies = currencyMap.values.sortedBy { it.countryName.lowercase() }

    private var selectedCurrencyId: Int = DEFAULT_CURRENCY_ID
    private var selectedDateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN
    private var selectedTimeFormat: String = DEFAULT_TIME_FORMAT
    private var selectedGroupingStyle: CurrencyGroupingStyle = CurrencyGroupingStyle.INDIAN
    private var selectedDecimalPlaces: Int = 2

    private val _uiState = MutableStateFlow(
        PreferencesScreenUiState(
            filteredCurrencies = allCurrencies,
            numberFormatOptions = buildNumberFormatOptions(2),
            decimalPlaceOptions = buildDecimalPlaceOptions(CurrencyGroupingStyle.INDIAN)
        )
    )
    val uiState: StateFlow<PreferencesScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAppPreferences().collect { settings ->
                selectedCurrencyId = settings.currencyId
                selectedDateFormatPattern = settings.dateFormatPattern
                selectedTimeFormat = settings.timeFormat
                selectedGroupingStyle = settings.currencyGroupingStyle
                selectedDecimalPlaces = settings.currencyDecimalPlaces.coerceIn(0, 4)
                rebuildUiState()
            }
        }
    }

    fun showSheet(sheetType: PreferencesSheetType) {
        _uiState.update { it.copy(activeSheet = sheetType) }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(activeSheet = null) }
    }

    fun updateCurrencySearchQuery(query: String) {
        _uiState.update { it.copy(currencySearchQuery = query) }
        rebuildUiState()
    }

    fun clearCurrencySearchQuery() {
        if (_uiState.value.currencySearchQuery.isEmpty()) {
            return
        }
        _uiState.update { it.copy(currencySearchQuery = "") }
        rebuildUiState()
    }

    fun selectCurrency(currencyId: Int) {
        viewModelScope.launch {
            updateCurrency(currencyId)
        }
        clearCurrencySearchQuery()
        dismissSheet()
    }

    fun selectDateFormat(dateFormatPattern: String) {
        viewModelScope.launch {
            updateDateFormat(dateFormatPattern)
        }
        dismissSheet()
    }

    fun selectTimeFormat(timeFormat: String) {
        viewModelScope.launch {
            updateTimeFormat(timeFormat)
        }
        dismissSheet()
    }

    fun selectGroupingStyle(groupingStyle: CurrencyGroupingStyle) {
        viewModelScope.launch {
            updateCurrencyGroupingStyle(groupingStyle)
        }
        dismissSheet()
    }

    fun selectDecimalPlaces(decimalPlaces: Int) {
        viewModelScope.launch {
            updateCurrencyDecimalPlaces(decimalPlaces)
        }
        dismissSheet()
    }

    private fun rebuildUiState() {
        val query = _uiState.value.currencySearchQuery.trim()
        val filteredCurrencies = if (query.isEmpty()) {
            allCurrencies
        } else {
            allCurrencies.filter { currency ->
                currency.countryName.contains(query, ignoreCase = true) ||
                    currency.currencyName.contains(query, ignoreCase = true) ||
                    currency.currencySymbol.contains(query, ignoreCase = true)
            }
        }
        val selectedCurrencyLabel = currencyMap[selectedCurrencyId]
            ?.let { "${it.currencySymbol} ${it.countryName}" }
            ?: "Select"

        _uiState.update {
            it.copy(
                selectedCurrencyId = selectedCurrencyId,
                selectedDateFormatPattern = selectedDateFormatPattern,
                selectedTimeFormat = selectedTimeFormat,
                selectedGroupingStyle = selectedGroupingStyle,
                selectedDecimalPlaces = selectedDecimalPlaces,
                currentCurrencyLabel = selectedCurrencyLabel,
                currentDateFormatLabel = getDateFormatPreviewLabel(selectedDateFormatPattern),
                currentTimeFormatLabel = getTimeFormatPreviewLabel(selectedTimeFormat),
                currentGroupingLabel = selectedGroupingStyle.toDisplayLabel(),
                currentDecimalPlacesLabel = selectedDecimalPlaces.toString(),
                filteredCurrencies = filteredCurrencies,
                numberFormatOptions = buildNumberFormatOptions(selectedDecimalPlaces),
                decimalPlaceOptions = buildDecimalPlaceOptions(selectedGroupingStyle)
            )
        }
    }
}

private fun buildNumberFormatOptions(decimalPlaces: Int): List<NumberFormatOptionUi> {
    return CurrencyGroupingStyle.entries.map { style ->
        NumberFormatOptionUi(
            groupingStyle = style,
            label = style.toDisplayLabel(),
            preview = formatNumberValue(
                amount = 1234567.89,
                amountFormatPreferences = com.mkn0079.expensetracker.models.AmountFormatPreferences(
                    groupingStyle = style,
                    decimalPlaces = decimalPlaces.coerceIn(0, 4)
                )
            )
        )
    }
}

private fun buildDecimalPlaceOptions(
    groupingStyle: CurrencyGroupingStyle
): List<DecimalPlacesOptionUi> {
    return (0..4).map { decimalPlaces ->
        DecimalPlacesOptionUi(
            value = decimalPlaces,
            preview = formatNumberValue(
                amount = 1234567.89,
                amountFormatPreferences = com.mkn0079.expensetracker.models.AmountFormatPreferences(
                    groupingStyle = groupingStyle,
                    decimalPlaces = decimalPlaces
                )
            )
        )
    }
}
