package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.data.constants.defaultAppSettings
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.AppThemeMode
import com.mknlabs.expensetracker.models.CurrencyGroupingStyle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `selectThemeMode updates selected theme mode and label`() {
        val fakeRepository = FakeAppPreferencesRepository()
        val viewModel = PreferencesViewModel(fakeRepository)

        viewModel.viewModelScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect { }
        }

        viewModel.selectThemeMode(AppThemeMode.DARK)

        assertEquals(AppThemeMode.DARK, fakeRepository.settings.value.themeMode)
        assertEquals(AppThemeMode.DARK, viewModel.uiState.value.selectedThemeMode)
        assertEquals(com.mknlabs.expensetracker.R.string.label_theme_dark, viewModel.uiState.value.currentThemeModeLabelRes)
    }

    @Test
    fun `uiState exposes all in-app theme options`() {
        val viewModel = PreferencesViewModel(FakeAppPreferencesRepository())

        viewModel.viewModelScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect { }
        }

        assertEquals(
            listOf(AppThemeMode.SYSTEM, AppThemeMode.LIGHT, AppThemeMode.DARK),
            viewModel.uiState.value.themeModeOptions.map { it.themeMode }
        )
    }

    private class FakeAppPreferencesRepository : AppPreferencesRepository {
        val settings = MutableStateFlow(defaultAppSettings)

        override fun observeAppSettings(): Flow<AppSettings> = settings

        override suspend fun updateCurrency(currencyId: Int) {
            settings.value = settings.value.copy(currencyId = currencyId)
        }

        override suspend fun updateDateFormat(dateFormatPattern: String) {
            settings.value = settings.value.copy(dateFormatPattern = dateFormatPattern)
        }

        override suspend fun updateTimeFormat(timeFormat: String) {
            settings.value = settings.value.copy(timeFormat = timeFormat)
        }

        override suspend fun updateThemeMode(themeMode: AppThemeMode) {
            settings.value = settings.value.copy(themeMode = themeMode)
        }

        override suspend fun updateCurrencyGroupingStyle(groupingStyle: CurrencyGroupingStyle) {
            settings.value = settings.value.copy(currencyGroupingStyle = groupingStyle)
        }

        override suspend fun updateCurrencyDecimalPlaces(decimalPlaces: Int) {
            settings.value = settings.value.copy(currencyDecimalPlaces = decimalPlaces)
        }
    }
}
