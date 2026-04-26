package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.CalculatorLineItem
import com.mkn0079.expensetracker.utils.formatCurrencyValue
import javax.inject.Inject

/**
 * Converts a list of [CalculatorLineItem]s into a formatted breakdown note string.
 */
class BuildBreakdownNoteUseCase @Inject constructor() {
    operator fun invoke(
        items: List<CalculatorLineItem>,
        currencyId: Int,
        amountFormatPreferences: AmountFormatPreferences
    ): String {
        return items.joinToString(separator = "\n") { item ->
            "${item.description} - ${formatCurrencyValue(item.amount, currencyId, amountFormatPreferences)}"
        }
    }
}
