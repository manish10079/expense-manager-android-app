package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.AppThemeMode
import com.mknlabs.expensetracker.models.CurrencyGroupingStyle
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    fun observeAppSettings(): Flow<AppSettings>

    suspend fun updateCurrency(currencyId: Int)

    suspend fun updateDateFormat(dateFormatPattern: String)

    suspend fun updateTimeFormat(timeFormat: String)

    suspend fun updateThemeMode(themeMode: AppThemeMode)

    suspend fun updateCurrencyGroupingStyle(groupingStyle: CurrencyGroupingStyle)

    suspend fun updateCurrencyDecimalPlaces(decimalPlaces: Int)
}
