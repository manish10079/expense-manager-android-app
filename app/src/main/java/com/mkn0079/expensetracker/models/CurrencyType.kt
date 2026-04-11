package com.mkn0079.expensetracker.models

data class Currency(
    val id: Int,
    val countryName: String,
    val currencyName: String,
    val currencySymbol: String,
    val position: CurrencyPosition
)