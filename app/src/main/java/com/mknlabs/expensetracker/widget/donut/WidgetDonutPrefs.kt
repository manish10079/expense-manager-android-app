package com.mknlabs.expensetracker.widget.donut

import android.content.Context
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownKind

internal object WidgetDonutPrefs {
    private const val FILE = "donut_breakdown_widget"
    private const val SLOT_A = "slot_a_"
    private const val SLOT_B = "slot_b_"

    fun slotA(context: Context, appWidgetId: Int): WidgetBreakdownKind =
        read(context, SLOT_A + appWidgetId, WidgetBreakdownKind.EXPENSE)

    fun slotB(context: Context, appWidgetId: Int): WidgetBreakdownKind =
        read(context, SLOT_B + appWidgetId, WidgetBreakdownKind.PAYMENT)

    fun save(context: Context, appWidgetId: Int, a: WidgetBreakdownKind, b: WidgetBreakdownKind) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putString(SLOT_A + appWidgetId, a.name)
            .putString(SLOT_B + appWidgetId, b.name)
            .apply()
    }

    fun delete(context: Context, appWidgetId: Int) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .remove(SLOT_A + appWidgetId)
            .remove(SLOT_B + appWidgetId)
            .apply()
    }

    private fun read(context: Context, key: String, fallback: WidgetBreakdownKind): WidgetBreakdownKind {
        val raw = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(key, fallback.name)
        return runCatching { WidgetBreakdownKind.valueOf(raw ?: fallback.name) }.getOrDefault(fallback)
    }
}
