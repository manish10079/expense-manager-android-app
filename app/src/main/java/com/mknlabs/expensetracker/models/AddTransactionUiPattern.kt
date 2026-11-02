package com.mknlabs.expensetracker.models

enum class AddTransactionUiPattern {
    UNIFIED_SMART_SHEET,
    QUICK_FLOATING_ARC,
}

fun addTransactionUiPatternOrDefault(value: String?): AddTransactionUiPattern {
    return AddTransactionUiPattern.entries.firstOrNull { it.name == value }
        ?: AddTransactionUiPattern.UNIFIED_SMART_SHEET
}
