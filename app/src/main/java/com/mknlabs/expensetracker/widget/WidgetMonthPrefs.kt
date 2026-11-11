package com.mknlabs.expensetracker.widget

import android.content.Context

internal object WidgetMonthPrefs {
    const val TARGET_SUMMARY = "summary"
    const val TARGET_DONUT = "donut"

    private const val FILE = "widget_month_offset"

    fun offset(context: Context, target: String, appWidgetId: Int): Int {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getInt(key(target, appWidgetId), 0)
    }

    fun add(context: Context, target: String, appWidgetId: Int, delta: Int) {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val next = (prefs.getInt(key(target, appWidgetId), 0) + delta).coerceAtMost(0)
        prefs.edit().putInt(key(target, appWidgetId), next).commit()
    }

    fun delete(context: Context, target: String, appWidgetId: Int) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .remove(key(target, appWidgetId))
            .apply()
    }

    private fun key(target: String, appWidgetId: Int) = "${target}_$appWidgetId"
}
