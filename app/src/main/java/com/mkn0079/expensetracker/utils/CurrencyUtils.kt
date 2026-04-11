package com.mkn0079.expensetracker.utils

import androidx.compose.ui.graphics.Color
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.currencyMap
import com.mkn0079.expensetracker.models.Currency
import com.mkn0079.expensetracker.models.CurrencyPosition
import com.mkn0079.expensetracker.ui.theme.NegativeRed
import com.mkn0079.expensetracker.ui.theme.PositiveGreen
import java.text.NumberFormat
import java.util.Locale

fun formatAmount(
    amount: Double,
    transactionTypeId: Int = 0,
    currencyId: Int = DEFAULT_CURRENCY_ID
): String {
    val sign = if (getTransactionTypeName(transactionTypeId).equals("Income", true)) "+" else "-"
    return formatCurrencyValue(
        amount = amount,
        currencyId = currencyId,
        prefix = sign
    )
}

fun formatCurrencyValue(
    amount: Double,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    prefix: String = ""
): String {
    val currency = getCurrency(currencyId)
    val formattedAmount = formatCurrencyNumber(kotlin.math.abs(amount))
    val safePrefix = prefix.trim().ifEmpty {
        if (amount < 0) "-" else ""
    }

    return when (currency.position) {
        CurrencyPosition.POSTFIX -> "$safePrefix$formattedAmount${currency.currencySymbol}"
        CurrencyPosition.PREFIX -> "$safePrefix${currency.currencySymbol}$formattedAmount"
    }
}

fun getCurrency(currencyId: Int = DEFAULT_CURRENCY_ID): Currency {
    return currencyMap[currencyId] ?: currencyMap.getValue(DEFAULT_CURRENCY_ID)
}

private fun formatCurrencyNumber(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        maximumFractionDigits = 2
    }
    return formatter.format(amount)
}

fun getAmountColor(transactionId:Int): Color
{
    if(getTransactionTypeName(transactionId).equals("Income", ignoreCase = true))
        return PositiveGreen
    return NegativeRed
}
