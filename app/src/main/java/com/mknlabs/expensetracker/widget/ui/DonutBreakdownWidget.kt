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
import com.mknlabs.expensetracker.widget.donut.WidgetDonutPrefs
import com.mknlabs.expensetracker.widget.donut.WidgetDonutSampleData
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownKind

class DonutBreakdownWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE_4X4))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val panels = listOf(
            WidgetDonutSampleData.panel(WidgetDonutPrefs.slotA(context, appWidgetId)),
            WidgetDonutSampleData.panel(WidgetDonutPrefs.slotB(context, appWidgetId))
        )
        provideContent {
            GlanceTheme {
                WidgetDonutContent(panels)
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
