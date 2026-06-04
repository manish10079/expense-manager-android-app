package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.data.constants.defaultAppSettings
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CurrencyGroupingStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class AmountFormatUtilsTest {

    @Test
    fun formatCurrencyValue_usesIndianGrouping() {
        val formatted = formatCurrencyValue(
            amount = 1234567.89,
            currencyId = 2,
            amountFormatPreferences = AmountFormatPreferences(
                groupingStyle = CurrencyGroupingStyle.INDIAN,
                decimalPlaces = 2
            )
        )

        assertEquals("$12,34,567.89", formatted)
    }

    @Test
    fun formatCurrencyValue_usesInternationalGrouping() {
        val formatted = formatCurrencyValue(
            amount = 1234567.89,
            currencyId = 2,
            amountFormatPreferences = AmountFormatPreferences(
                groupingStyle = CurrencyGroupingStyle.INTERNATIONAL,
                decimalPlaces = 2
            )
        )

        assertEquals("$1,234,567.89", formatted)
    }

    @Test
    fun formatCurrencyValue_respectsZeroDecimalPlaces() {
        val formatted = formatCurrencyValue(
            amount = 1234567.89,
            currencyId = 2,
            amountFormatPreferences = AmountFormatPreferences(
                groupingStyle = CurrencyGroupingStyle.INTERNATIONAL,
                decimalPlaces = 0
            )
        )

        assertEquals("$1,234,568", formatted)
    }

    @Test
    fun formatCurrencyValue_respectsPostfixCurrenciesAndSigns() {
        val formatted = formatCurrencyValue(
            amount = -1234.5,
            currencyId = 14,
            amountFormatPreferences = AmountFormatPreferences(
                groupingStyle = CurrencyGroupingStyle.INTERNATIONAL,
                decimalPlaces = 4
            )
        )

        assertEquals("-1,234.5000CHF", formatted)
    }

    @Test
    fun toAmountFormatPreferences_clampsDecimalPlaces() {
        val amountFormatPreferences = defaultAppSettings
            .copy(currencyDecimalPlaces = 9)
            .toAmountFormatPreferences()

        assertEquals(4, amountFormatPreferences.decimalPlaces)
        assertEquals(CurrencyGroupingStyle.INDIAN, amountFormatPreferences.groupingStyle)
    }
}
