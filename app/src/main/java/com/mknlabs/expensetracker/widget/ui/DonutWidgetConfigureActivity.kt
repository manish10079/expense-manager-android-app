package com.mknlabs.expensetracker.widget.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.widget.donut.WidgetDonutPrefs
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownKind
import kotlinx.coroutines.launch

class DonutWidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        appWidgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            ExpenseTrackerTheme {
                var selected by remember {
                    mutableStateOf(
                        listOf(WidgetBreakdownKind.EXPENSE, WidgetBreakdownKind.PAYMENT)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Choose two breakdowns", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "This widget shows two charts. Pick any two.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    WidgetBreakdownKind.entries.forEach { kind ->
                        val on = kind in selected
                        FilterChip(
                            selected = on,
                            onClick = {
                                selected = when {
                                    on -> selected.filter { it != kind }
                                    selected.size >= 2 -> selected.drop(1) + kind
                                    else -> selected + kind
                                }
                            },
                            label = { Text(kind.label()) }
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        enabled = selected.size == 2,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { save(selected[0], selected[1]) }
                    ) {
                        Text("Add widget")
                    }
                }
            }
        }
    }

    private fun save(a: WidgetBreakdownKind, b: WidgetBreakdownKind) {
        WidgetDonutPrefs.save(this, appWidgetId, a, b)
        lifecycleScope.launch {
            DonutBreakdownWidget().updateAll(this@DonutWidgetConfigureActivity)
            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}

private fun WidgetBreakdownKind.label(): String = when (this) {
    WidgetBreakdownKind.EXPENSE -> "Expense breakdown"
    WidgetBreakdownKind.PAYMENT -> "Payment breakdown"
    WidgetBreakdownKind.INCOME -> "Income breakdown"
    WidgetBreakdownKind.TAGS -> "Tags breakdown"
    WidgetBreakdownKind.FUNDS -> "Funds breakdown"
    WidgetBreakdownKind.TOP10 -> "Top 10 spending of the month"
}
