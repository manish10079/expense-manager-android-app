package com.mknlabs.expensetracker.widget.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.utils.CustomMonthUtils
import com.mknlabs.expensetracker.widget.WidgetMonthPrefs
import com.mknlabs.expensetracker.widget.donut.WidgetDonutLiveData
import com.mknlabs.expensetracker.widget.donut.WidgetDonutPrefs
import com.mknlabs.expensetracker.widget.donut.WidgetDonutSampleData
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownKind
import kotlinx.coroutines.flow.first

class DonutBreakdownWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE_4X4))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val offset = WidgetMonthPrefs.offset(context, WidgetMonthPrefs.TARGET_DONUT, appWidgetId)
        val settings = AppSettingsDataStore.getAppSettingsFlow(context).first()
        val (start, endInclusive) = CustomMonthUtils.getCustomMonthRange(
            nowMillis = System.currentTimeMillis(),
            monthStartDay = settings.monthStartDay,
            monthOffset = offset
        )
        val labelCal = java.util.Calendar.getInstance().apply { timeInMillis = start }
        val monthName = labelCal.getDisplayName(
            java.util.Calendar.MONTH,
            java.util.Calendar.SHORT,
            java.util.Locale.US
        ) ?: ""
        val endExclusive = endInclusive + 1
        val panels = listOf(
            WidgetDonutLiveData.panel(context, WidgetDonutPrefs.slotA(context, appWidgetId), start, endExclusive),
            WidgetDonutLiveData.panel(context, WidgetDonutPrefs.slotB(context, appWidgetId), start, endExclusive)
        )
        provideContent {
            GlanceTheme {
                WidgetDonutContent(
                    panels = panels,
                    appWidgetId = appWidgetId,
                    monthLabel = "$monthName ${labelCal.get(java.util.Calendar.YEAR)}",
                    canGoNext = offset < 0,
                )
            }
        }
    }

    companion object {
        val SIZE_4X4 = DpSize(250.dp, 250.dp)
    }
}

@Composable
internal fun DonutBreakdownPreviewContent(forceDark: Boolean) {
    WidgetDonutContent(
        panels = listOf(
            WidgetDonutSampleData.panel(WidgetBreakdownKind.EXPENSE),
            WidgetDonutSampleData.panel(WidgetBreakdownKind.PAYMENT)
        ),
        forceDark = forceDark
    )
}
