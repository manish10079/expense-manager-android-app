package com.mkn0079.expensetracker.data.constants

import com.mkn0079.expensetracker.models.Currency
import com.mkn0079.expensetracker.models.CurrencyPosition

val currencyMap = mapOf(

    // 🇮🇳 India (keep at 1)
    1 to Currency(1, "India", "Rupee", "₹", CurrencyPosition.PREFIX),

    // 🌍 Major Global Currencies
    2 to Currency(2, "USA", "Dollar", "$", CurrencyPosition.PREFIX),
    3 to Currency(3, "UK", "Pound Sterling", "£", CurrencyPosition.PREFIX),
    4 to Currency(4, "Europe", "Euro", "€", CurrencyPosition.POSTFIX),
    5 to Currency(5, "Japan", "Yen", "¥", CurrencyPosition.PREFIX),
    6 to Currency(6, "China", "Yuan", "¥", CurrencyPosition.PREFIX),
    7 to Currency(7, "South Korea", "Won", "₩", CurrencyPosition.PREFIX),

    // 🌏 Asia
    8 to Currency(8, "UAE", "Dirham", "د.إ", CurrencyPosition.PREFIX),
    9 to Currency(9, "Saudi Arabia", "Riyal", "﷼", CurrencyPosition.PREFIX),
    10 to Currency(10, "Singapore", "Singapore Dollar", "S$", CurrencyPosition.PREFIX),
    11 to Currency(11, "Thailand", "Baht", "฿", CurrencyPosition.PREFIX),
    12 to Currency(12, "Indonesia", "Rupiah", "Rp", CurrencyPosition.PREFIX),
    13 to Currency(13, "Malaysia", "Ringgit", "RM", CurrencyPosition.PREFIX),

    // 🌍 Europe
    14 to Currency(14, "Switzerland", "Swiss Franc", "CHF", CurrencyPosition.POSTFIX),
    15 to Currency(15, "Russia", "Ruble", "₽", CurrencyPosition.POSTFIX),
    16 to Currency(16, "Turkey", "Lira", "₺", CurrencyPosition.PREFIX),

    // 🌎 Americas
    17 to Currency(17, "Canada", "Canadian Dollar", "C$", CurrencyPosition.PREFIX),
    18 to Currency(18, "Australia", "Australian Dollar", "A$", CurrencyPosition.PREFIX),
    19 to Currency(19, "Brazil", "Real", "R$", CurrencyPosition.PREFIX),
    20 to Currency(20, "Mexico", "Peso", "$", CurrencyPosition.PREFIX),

    // 🌍 Africa
    21 to Currency(21, "South Africa", "Rand", "R", CurrencyPosition.PREFIX),
    22 to Currency(22, "Egypt", "Egyptian Pound", "£", CurrencyPosition.PREFIX),
    23 to Currency(23, "Nigeria", "Naira", "₦", CurrencyPosition.PREFIX),

    // 🌐 Others
    24 to Currency(24, "Pakistan", "Rupee", "₨", CurrencyPosition.PREFIX),
    25 to Currency(25, "Bangladesh", "Taka", "৳", CurrencyPosition.PREFIX),
    26 to Currency(26, "Sri Lanka", "Rupee", "Rs", CurrencyPosition.PREFIX),
    27 to Currency(27, "Nepal", "Rupee", "₨", CurrencyPosition.PREFIX),

    // 🌐 Crypto (optional but modern apps include it)
    28 to Currency(28, "Global", "Bitcoin", "₿", CurrencyPosition.PREFIX),
    29 to Currency(29, "Global", "Ethereum", "Ξ", CurrencyPosition.PREFIX)
)