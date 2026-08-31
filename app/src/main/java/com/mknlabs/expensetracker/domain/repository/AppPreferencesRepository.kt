package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.AppThemeMode
import com.mknlabs.expensetracker.models.CurrencyGroupingStyle
import com.mknlabs.expensetracker.models.FontMode
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    fun observeAppSettings(): Flow<AppSettings>

    suspend fun updateCurrency(currencyId: Int)

    suspend fun updateDateFormat(dateFormatPattern: String)

    suspend fun updateTimeFormat(timeFormat: String)

    suspend fun updateThemeMode(themeMode: AppThemeMode)

    suspend fun updateCurrencyGroupingStyle(groupingStyle: CurrencyGroupingStyle)

    suspend fun updateCurrencyDecimalPlaces(decimalPlaces: Int)

    suspend fun updateFontMode(fontMode: FontMode)

    suspend fun setActiveCustomFont(fileName: String?)

    suspend fun addImportedFont(fileName: String)

    suspend fun removeImportedFont(fileName: String)

    /**
     * Atomically add a font to the library AND set it as active.
     * Single DataStore write to avoid stale intermediate states.
     */
    suspend fun addAndActivateFont(fileName: String)

    suspend fun updateMonthStartDay(day: Int)
}
