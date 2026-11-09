package com.mknlabs.expensetracker.widget.model

internal enum class WidgetBreakdownKind {
    EXPENSE,
    PAYMENT,
    INCOME,
    TAGS,
    FUNDS,
    TOP10
}

internal data class WidgetBreakdownSlice(
    val label: String,
    val percent: Int,
    val amountText: String,
    val colorArgb: Int
)

internal data class WidgetBreakdownPanel(
    val kind: WidgetBreakdownKind,
    val title: String,
    val trailing: String,
    val slices: List<WidgetBreakdownSlice>
)
