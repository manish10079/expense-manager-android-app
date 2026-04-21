package com.mkn0079.expensetracker.domain.repository

import com.mkn0079.expensetracker.models.AppSettings
import com.mkn0079.expensetracker.models.CurrencyGroupingStyle
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    fun observeAppSettings(): Flow<AppSettings>

    suspend fun updateCurrency(currencyId: Int)

    suspend fun updateDateFormat(dateFormatPattern: String)

    suspend fun updateTimeFormat(timeFormat: String)

    suspend fun updateCurrencyGroupingStyle(groupingStyle: CurrencyGroupingStyle)

    suspend fun updateCurrencyDecimalPlaces(decimalPlaces: Int)
}
