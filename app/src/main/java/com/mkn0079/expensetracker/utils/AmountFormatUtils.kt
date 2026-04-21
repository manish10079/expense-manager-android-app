package com.mkn0079.expensetracker.utils

import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_DECIMAL_PLACES
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_GROUPING_STYLE
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.AppSettings
import com.mkn0079.expensetracker.models.CurrencyGroupingStyle

val defaultAmountFormatPreferences = AmountFormatPreferences(
    groupingStyle = DEFAULT_CURRENCY_GROUPING_STYLE,
    decimalPlaces = DEFAULT_CURRENCY_DECIMAL_PLACES
)

fun AppSettings.toAmountFormatPreferences(): AmountFormatPreferences {
    return AmountFormatPreferences(
        groupingStyle = currencyGroupingStyle,
        decimalPlaces = currencyDecimalPlaces.coerceIn(0, 4)
    )
}

fun CurrencyGroupingStyle.toDisplayLabel(): String {
    return when (this) {
        CurrencyGroupingStyle.INDIAN -> "Indian"
        CurrencyGroupingStyle.INTERNATIONAL -> "International"
    }
}
