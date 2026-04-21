package com.mkn0079.expensetracker.models

import androidx.compose.runtime.Immutable

@Immutable
data class AmountFormatPreferences(
    val groupingStyle: CurrencyGroupingStyle,
    val decimalPlaces: Int
)
