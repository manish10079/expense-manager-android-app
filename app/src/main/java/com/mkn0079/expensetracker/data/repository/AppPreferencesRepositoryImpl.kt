package com.mkn0079.expensetracker.data.repository

import android.content.Context
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.domain.repository.AppPreferencesRepository
import com.mkn0079.expensetracker.models.AppSettings
import com.mkn0079.expensetracker.models.AppThemeMode
import com.mkn0079.expensetracker.models.CurrencyGroupingStyle
import kotlinx.coroutines.flow.Flow

class AppPreferencesRepositoryImpl(
    private val context: Context
) : AppPreferencesRepository {

    override fun observeAppSettings(): Flow<AppSettings> {
        return AppSettingsDataStore.getAppSettingsFlow(context)
    }

    override suspend fun updateCurrency(currencyId: Int) {
        AppSettingsDataStore.updateAppSettings(context) { settings ->
            settings.copy(currencyId = currencyId)
        }
    }

    override suspend fun updateDateFormat(dateFormatPattern: String) {
        AppSettingsDataStore.updateAppSettings(context) { settings ->
            settings.copy(dateFormatPattern = dateFormatPattern)
        }
    }

    override suspend fun updateTimeFormat(timeFormat: String) {
        AppSettingsDataStore.updateAppSettings(context) { settings ->
            settings.copy(timeFormat = timeFormat)
        }
    }

    override suspend fun updateThemeMode(themeMode: AppThemeMode) {
        AppSettingsDataStore.updateAppSettings(context) { settings ->
            settings.copy(themeMode = themeMode)
        }
    }

    override suspend fun updateCurrencyGroupingStyle(groupingStyle: CurrencyGroupingStyle) {
        AppSettingsDataStore.updateAppSettings(context) { settings ->
            settings.copy(currencyGroupingStyle = groupingStyle)
        }
    }

    override suspend fun updateCurrencyDecimalPlaces(decimalPlaces: Int) {
        AppSettingsDataStore.updateAppSettings(context) { settings ->
            settings.copy(currencyDecimalPlaces = decimalPlaces.coerceIn(0, 4))
        }
    }
}
