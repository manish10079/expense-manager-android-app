package com.mknlabs.expensetracker.widget.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.utils.CustomMonthUtils
import com.mknlabs.expensetracker.utils.getCurrency
import com.mknlabs.expensetracker.widget.di.WidgetEntryPoint
import com.mknlabs.expensetracker.widget.model.WidgetUiState
import kotlinx.coroutines.flow.first

class ExpenseHomeWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE_2X2))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val uiState = loadMonthUiState(context)
        provideContent {
            GlanceTheme {
                Widget2x2SummaryContent(uiState)
            }
        }
    }

    companion object {
        val SIZE_2X2 = DpSize(155.dp, 155.dp)
    }
}

class Widget4x2HomeWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE_4X2))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val uiState = loadMonthUiState(context)
        provideContent {
            GlanceTheme {
                Widget4x2Content(uiState)
            }
        }
    }

    companion object {
        val SIZE_4X2 = DpSize(250.dp, 155.dp)
    }
}

class Widget4x4HomeWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE_4X4))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val uiState = loadMonthUiState(context)
        provideContent {
            GlanceTheme {
                Widget4x4Content(uiState)
            }
        }
    }

    companion object {
        val SIZE_4X4 = DpSize(250.dp, 250.dp)
    }
}

@Composable
internal fun ExpenseWidgetContent(uiState: WidgetUiState) {
    Widget2x2SummaryContent(uiState)
}

internal suspend fun loadMonthUiState(context: Context): WidgetUiState {
    return try {
        val settings = AppSettingsDataStore.getAppSettingsFlow(context).first()
        val now = System.currentTimeMillis()
        val (start, endInclusive) = CustomMonthUtils.getCustomMonthRange(
            nowMillis = now,
            monthStartDay = settings.monthStartDay,
            monthOffset = 0
        )
        val summary = WidgetEntryPoint.get(context).transactionDao()
            .getRangeSummary(start, endInclusive + 1)
        WidgetUiState(
            monthIncomeMinor = summary.incomeMinor,
            monthExpenseMinor = summary.expenseMinor,
            currencySymbol = getCurrency(settings.currencyId).currencySymbol
        )
    } catch (e: Exception) {
        Log.e("ExpenseHomeWidget", "Failed to load month totals", e)
        WidgetUiState()
    }
}
