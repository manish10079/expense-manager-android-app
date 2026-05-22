package com.mkn0079.expensetracker.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.currencyMap
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.Currency
import com.mkn0079.expensetracker.models.CurrencyGroupingStyle
import com.mkn0079.expensetracker.models.CurrencyPosition
import com.mkn0079.expensetracker.ui.theme.expense
import com.mkn0079.expensetracker.ui.theme.income
import java.math.BigDecimal
import java.math.RoundingMode

fun formatAmount(
    amount: Double,
    transactionTypeId: Int = 0,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences
): String {
    val sign = if (getTransactionTypeName(transactionTypeId).equals("Income", true)) "+" else "-"
    return formatCurrencyValue(
        amount = amount,
        currencyId = currencyId,
        amountFormatPreferences = amountFormatPreferences,
        prefix = sign
    )
}

fun formatCurrencyValue(
    amount: Double,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    prefix: String = ""
): String {
    val currency = getCurrency(currencyId)
    val formattedAmount = formatNumberValue(kotlin.math.abs(amount), amountFormatPreferences)
    val safePrefix = prefix.trim().ifEmpty {
        if (amount < 0) "-" else ""
    }

    return when (currency.position) {
        CurrencyPosition.POSTFIX -> "$safePrefix$formattedAmount${currency.currencySymbol}"
        CurrencyPosition.PREFIX -> "$safePrefix${currency.currencySymbol}$formattedAmount"
    }
}

/**
 * Formats a currency value in a compact way (e.g., $1.2k, €5M) for limited UI space.
 */
fun formatCompactCurrencyValue(
    amount: Double,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    prefix: String = ""
): String {
    val currency = getCurrency(currencyId)
    val absAmount = kotlin.math.abs(amount)

    val (abbreviatedAmount, suffix) = when {
        absAmount >= 1_000_000_000 -> (absAmount / 1_000_000_000.0) to "B"
        absAmount >= 1_000_000 -> (absAmount / 1_000_000.0) to "M"
        absAmount >= 100_000 -> (absAmount / 1_000.0) to "k" // Use 'k' for 100k+ to save space
        else -> null to ""
    }

    val formattedAmount = if (abbreviatedAmount == null) {
        formatNumberValue(absAmount, amountFormatPreferences)
    } else {
        val df = java.text.DecimalFormat("#.#")
        df.roundingMode = java.math.RoundingMode.HALF_UP
        df.format(abbreviatedAmount) + suffix
    }

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

fun formatNumberValue(
    amount: Double,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences
): String {
    val normalizedDecimalPlaces = amountFormatPreferences.decimalPlaces.coerceIn(0, 4)
    val roundedAmount = BigDecimal.valueOf(amount)
        .setScale(normalizedDecimalPlaces, RoundingMode.HALF_UP)
        .toPlainString()

    val parts = roundedAmount.split('.')
    val integerPart = parts.first()
    val groupedIntegerPart = when (amountFormatPreferences.groupingStyle) {
        CurrencyGroupingStyle.INDIAN -> formatIndianGroupedInteger(integerPart)
        CurrencyGroupingStyle.INTERNATIONAL -> formatInternationalGroupedInteger(integerPart)
    }

    return if (normalizedDecimalPlaces == 0) {
        groupedIntegerPart
    } else {
        val fractionalPart = parts.getOrNull(1).orEmpty().padEnd(normalizedDecimalPlaces, '0')
        "$groupedIntegerPart.$fractionalPart"
    }
}

private fun formatInternationalGroupedInteger(integerPart: String): String {
    return integerPart.reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}

private fun formatIndianGroupedInteger(integerPart: String): String {
    if (integerPart.length <= 3) {
        return integerPart
    }

    val prefix = integerPart.dropLast(3)
    val lastThreeDigits = integerPart.takeLast(3)
    val groupedPrefix = prefix.reversed()
        .chunked(2)
        .joinToString(",")
        .reversed()

    return "$groupedPrefix,$lastThreeDigits"
}

@Composable
fun getAmountColor(transactionId: Int) =
    if (getTransactionTypeName(transactionId).equals("Income", ignoreCase = true)) {
        MaterialTheme.colorScheme.income
    } else {
        MaterialTheme.colorScheme.expense
    }
