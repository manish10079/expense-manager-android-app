package com.mknlabs.expensetracker.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mknlabs.expensetracker.widget.ui.DonutBreakdownWidget
import com.mknlabs.expensetracker.widget.ui.ExpenseHomeWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll

class WidgetMonthNavReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        val delta = intent.getIntExtra(EXTRA_DELTA, 0)
        val target = intent.getStringExtra(EXTRA_TARGET) ?: return
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (delta == 0 || appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
        val pending = goAsync()
        val app = context.applicationContext
        scope.launch {
            try {
                WidgetMonthPrefs.add(app, target, appWidgetId, delta)
                when (target) {
                    WidgetMonthPrefs.TARGET_SUMMARY -> ExpenseHomeWidget().updateAll(app)
                    WidgetMonthPrefs.TARGET_DONUT -> DonutBreakdownWidget().updateAll(app)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION = "com.mknlabs.expensetracker.widget.SHIFT_MONTH"
        const val EXTRA_DELTA = "month_delta"
        const val EXTRA_TARGET = "month_target"

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun intent(context: Context, appWidgetId: Int, target: String, delta: Int): Intent {
            return Intent(context, WidgetMonthNavReceiver::class.java).apply {
                action = ACTION
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(EXTRA_TARGET, target)
                putExtra(EXTRA_DELTA, delta)
                data = Uri.parse("expensetracker://widget-month/$target/$appWidgetId/$delta")
            }
        }
    }
}
