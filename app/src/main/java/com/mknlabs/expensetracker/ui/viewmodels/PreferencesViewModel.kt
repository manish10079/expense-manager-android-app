package com.mknlabs.expensetracker.ui.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.currencyMap
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.domain.usecase.GetAppPreferencesUseCase
import com.mknlabs.expensetracker.domain.usecase.UpdateCurrencyDecimalPlacesUseCase
import com.mknlabs.expensetracker.domain.usecase.UpdateCurrencyGroupingStyleUseCase
import com.mknlabs.expensetracker.domain.usecase.UpdateCurrencyUseCase
import com.mknlabs.expensetracker.domain.usecase.UpdateDateFormatUseCase
import com.mknlabs.expensetracker.domain.usecase.UpdateThemeModeUseCase
import com.mknlabs.expensetracker.domain.usecase.UpdateTimeFormatUseCase
import com.mknlabs.expensetracker.models.AppThemeMode
import com.mknlabs.expensetracker.models.Currency
import com.mknlabs.expensetracker.models.CurrencyGroupingStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.mknlabs.expensetracker.utils.formatNumberValue
import com.mknlabs.expensetracker.utils.getDateFormatPreviewLabel
import com.mknlabs.expensetracker.utils.getTimeFormatPreviewLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PreferencesSheetType {
    Currency,
    ThemeMode,
    DateFormat,
    TimeFormat,
    NumberFormat,
    DecimalPlaces
}

@Immutable
data class NumberFormatOptionUi(
    val groupingStyle: CurrencyGroupingStyle,
    val labelRes: Int,
    val preview: String
)

@Immutable
data class DecimalPlacesOptionUi(
    val value: Int,
    val preview: String
)

@Immutable
data class ThemeModeOptionUi(
    val themeMode: AppThemeMode,
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int
)

@Immutable
data class PreferencesScreenUiState(
    val selectedCurrencyId: Int = DEFAULT_CURRENCY_ID,
    val selectedThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val selectedDateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    val selectedTimeFormat: String = DEFAULT_TIME_FORMAT,
    val selectedGroupingStyle: CurrencyGroupingStyle = CurrencyGroupingStyle.INDIAN,
    val selectedDecimalPlaces: Int = 2,
    val currentCurrencyLabel: String = "",
    val currentCurrencyLabelRes: Int = R.string.label_select,
    val currentThemeModeLabelRes: Int = R.string.label_theme_system,
    val currentDateFormatLabel: String = getDateFormatPreviewLabel(DEFAULT_DATE_FORMAT_PATTERN),
    val currentTimeFormatLabelRes: Int = getTimeFormatPreviewLabel(DEFAULT_TIME_FORMAT),
    val currentGroupingLabelRes: Int = R.string.label_system_active,
    val currentDecimalPlacesLabel: String = "2",
    val currencySearchQuery: String = "",
    val filteredCurrencies: List<Currency> = emptyList(),
    val themeModeOptions: List<ThemeModeOptionUi> = buildThemeModeOptions(),
    val numberFormatOptions: List<NumberFormatOptionUi> = emptyList(),
    val decimalPlaceOptions: List<DecimalPlacesOptionUi> = emptyList(),
    val activeSheet: PreferencesSheetType? = null
)

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    repository: AppPreferencesRepository
) : ViewModel() {

    private val getAppPreferences = GetAppPreferencesUseCase(repository)
    private val updateCurrency = UpdateCurrencyUseCase(repository)
    private val updateDateFormat = UpdateDateFormatUseCase(repository)
    private val updateTimeFormat = UpdateTimeFormatUseCase(repository)
    private val updateThemeMode = UpdateThemeModeUseCase(repository)
    private val updateCurrencyGroupingStyle = UpdateCurrencyGroupingStyleUseCase(repository)
    private val updateCurrencyDecimalPlaces = UpdateCurrencyDecimalPlacesUseCase(repository)

    private val allCurrencies = currencyMap.values.sortedBy { it.countryName.lowercase() }

    private var selectedCurrencyId: Int = DEFAULT_CURRENCY_ID
    private var selectedThemeMode: AppThemeMode = AppThemeMode.SYSTEM
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
                selectedThemeMode = settings.themeMode
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

    fun selectThemeMode(themeMode: AppThemeMode) {
        viewModelScope.launch {
            updateThemeMode(themeMode)
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
            ?: ""

        _uiState.update {
            it.copy(
                selectedCurrencyId = selectedCurrencyId,
                selectedThemeMode = selectedThemeMode,
                selectedDateFormatPattern = selectedDateFormatPattern,
                selectedTimeFormat = selectedTimeFormat,
                selectedGroupingStyle = selectedGroupingStyle,
                selectedDecimalPlaces = selectedDecimalPlaces,
                currentCurrencyLabel = selectedCurrencyLabel,
                currentCurrencyLabelRes = if (selectedCurrencyLabel.isEmpty()) R.string.label_select else 0,
                currentThemeModeLabelRes = selectedThemeMode.toDisplayLabelRes(),
                currentDateFormatLabel = getDateFormatPreviewLabel(selectedDateFormatPattern),
                currentTimeFormatLabelRes = getTimeFormatPreviewLabel(selectedTimeFormat),
                currentGroupingLabelRes = selectedGroupingStyle.toDisplayLabelRes(),
                currentDecimalPlacesLabel = selectedDecimalPlaces.toString(),
                filteredCurrencies = filteredCurrencies,
                themeModeOptions = buildThemeModeOptions(),
                numberFormatOptions = buildNumberFormatOptions(selectedDecimalPlaces),
                decimalPlaceOptions = buildDecimalPlaceOptions(selectedGroupingStyle)
            )
        }
    }
}

private fun buildThemeModeOptions(): List<ThemeModeOptionUi> {
    return AppThemeMode.entries.map { themeMode ->
        ThemeModeOptionUi(
            themeMode = themeMode,
            labelRes = themeMode.toDisplayLabelRes(),
            descriptionRes = when (themeMode) {
                AppThemeMode.SYSTEM -> R.string.desc_theme_system
                AppThemeMode.LIGHT -> R.string.desc_theme_light
                AppThemeMode.DARK -> R.string.desc_theme_dark
            }
        )
    }
}

private fun AppThemeMode.toDisplayLabelRes(): Int {
    return when (this) {
        AppThemeMode.SYSTEM -> R.string.label_theme_system
        AppThemeMode.LIGHT -> R.string.label_theme_light
        AppThemeMode.DARK -> R.string.label_theme_dark
    }
}

private fun CurrencyGroupingStyle.toDisplayLabelRes(): Int {
    return when (this) {
        CurrencyGroupingStyle.INDIAN -> R.string.label_grouping_indian
        CurrencyGroupingStyle.INTERNATIONAL -> R.string.label_grouping_international
    }
}

private fun buildNumberFormatOptions(decimalPlaces: Int): List<NumberFormatOptionUi> {
    return CurrencyGroupingStyle.entries.map { style ->
        NumberFormatOptionUi(
            groupingStyle = style,
            labelRes = style.toDisplayLabelRes(),
            preview = formatNumberValue(
                amount = 1234567.89,
                amountFormatPreferences = com.mknlabs.expensetracker.models.AmountFormatPreferences(
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
                amountFormatPreferences = com.mknlabs.expensetracker.models.AmountFormatPreferences(
                    groupingStyle = groupingStyle,
                    decimalPlaces = decimalPlaces
                )
            )
        )
    }
}
