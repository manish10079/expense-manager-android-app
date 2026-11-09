package com.mknlabs.expensetracker.widget.donut

import com.mknlabs.expensetracker.widget.model.WidgetBreakdownKind
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownPanel
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownSlice

internal object WidgetDonutSampleData {
    fun panel(kind: WidgetBreakdownKind): WidgetBreakdownPanel = when (kind) {
        WidgetBreakdownKind.EXPENSE -> WidgetBreakdownPanel(
            kind = kind,
            title = "Expense breakdown",
            trailing = "Total: ₹34,500",
            slices = listOf(
                slice("Food", 35, "₹12,075", 0xFFF59E0B),
                slice("Bills", 30, "₹10,350", 0xFFF43F5E),
                slice("Shopping", 20, "₹6,900", 0xFF8B5CF6),
                slice("Other", 15, "₹5,175", 0xFF10B981)
            )
        )
        WidgetBreakdownKind.PAYMENT -> WidgetBreakdownPanel(
            kind = kind,
            title = "Payment breakdown",
            trailing = "42 txns",
            slices = listOf(
                slice("UPI", 55, "₹18,975", 0xFF6366F1),
                slice("Card", 25, "₹8,625", 0xFF38BDF8),
                slice("NetBank", 12, "₹4,140", 0xFFC084FC),
                slice("Cash", 8, "₹2,760", 0xFF94A3B8)
            )
        )
        WidgetBreakdownKind.INCOME -> WidgetBreakdownPanel(
            kind = kind,
            title = "Income breakdown",
            trailing = "Total: ₹1,20,000",
            slices = listOf(
                slice("Salary", 70, "₹84,000", 0xFF10B981),
                slice("Freelance", 20, "₹24,000", 0xFF6366F1),
                slice("Interest", 6, "₹7,200", 0xFFF59E0B),
                slice("Other", 4, "₹4,800", 0xFF94A3B8)
            )
        )
        WidgetBreakdownKind.TAGS -> WidgetBreakdownPanel(
            kind = kind,
            title = "Tags breakdown",
            trailing = "18 tags",
            slices = listOf(
                slice("Work", 40, "₹13,800", 0xFF8B5CF6),
                slice("Family", 25, "₹8,625", 0xFF38BDF8),
                slice("Health", 20, "₹6,900", 0xFF10B981),
                slice("Travel", 15, "₹5,175", 0xFFF59E0B)
            )
        )
        WidgetBreakdownKind.FUNDS -> WidgetBreakdownPanel(
            kind = kind,
            title = "Funds breakdown",
            trailing = "4 funds",
            slices = listOf(
                slice("Main", 50, "₹17,250", 0xFF8B5CF6),
                slice("Savings", 25, "₹8,625", 0xFF10B981),
                slice("Travel", 15, "₹5,175", 0xFF38BDF8),
                slice("Emergency", 10, "₹3,450", 0xFFF43F5E)
            )
        )
        WidgetBreakdownKind.TOP10 -> WidgetBreakdownPanel(
            kind = kind,
            title = "Top 10 spending",
            trailing = "This month",
            slices = listOf(
                slice("Rent", 22, "₹7,590", 0xFF8B5CF6),
                slice("Groceries", 18, "₹6,210", 0xFFF59E0B),
                slice("Fuel", 12, "₹4,140", 0xFF38BDF8),
                slice("Dining", 11, "₹3,795", 0xFFF43F5E),
                slice("Utilities", 9, "₹3,105", 0xFF10B981),
                slice("Shopping", 8, "₹2,760", 0xFFC084FC)
            )
        )
    }

    private fun slice(label: String, percent: Int, amount: String, rgb: Long) =
        WidgetBreakdownSlice(label, percent, amount, rgb.toInt())
}
