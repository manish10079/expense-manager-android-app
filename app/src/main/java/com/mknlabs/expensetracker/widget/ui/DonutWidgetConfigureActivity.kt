package com.mknlabs.expensetracker.widget.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        setFinishOnTouchOutside(true)
        setContent {
            ExpenseTrackerTheme {
                var selected by remember {
                    mutableStateOf(
                        listOf(
                            WidgetDonutPrefs.slotA(this@DonutWidgetConfigureActivity, appWidgetId),
                            WidgetDonutPrefs.slotB(this@DonutWidgetConfigureActivity, appWidgetId)
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { finish() },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ConfigureMock.card),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ConfigureMock.cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Choose breakdowns",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = ConfigureMock.title
                                        )
                                        Spacer(Modifier.size(8.dp))
                                        val ready = selected.size == 2
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (ready) {
                                                ConfigureMock.indigo.copy(alpha = 0.2f)
                                            } else {
                                                ConfigureMock.amber.copy(alpha = 0.2f)
                                            }
                                        ) {
                                            Text(
                                                "${selected.size} / 2 selected",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                color = if (ready) {
                                                    ConfigureMock.indigoText
                                                } else {
                                                    ConfigureMock.amber
                                                },
                                                fontSize = 11.sp,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                    Text(
                                        "Select exactly 2 charts for your dashboard widget.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ConfigureMock.muted,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { finish() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Close",
                                        tint = ConfigureMock.muted
                                    )
                                }
                            }
                            ConfigureLivePreview(selected)
                            WidgetBreakdownKind.entries.chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { kind ->
                                        val on = kind in selected
                                        ConfigureOptionChip(
                                            kind = kind,
                                            selected = on,
                                            onClick = {
                                                selected = when {
                                                    on -> selected.filter { it != kind }
                                                    selected.size >= 2 -> selected.drop(1) + kind
                                                    else -> selected + kind
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                            if (selected.size != 2) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ConfigureMock.amber.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        ConfigureMock.amber.copy(alpha = 0.25f)
                                    )
                                ) {
                                    Text(
                                        "Please select exactly 2 charts to continue.",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        color = ConfigureMock.amber,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Button(
                                enabled = selected.size == 2,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ConfigureMock.indigoBtn,
                                    contentColor = Color.White,
                                    disabledContainerColor = ConfigureMock.indigoBtn.copy(alpha = 0.4f),
                                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                                ),
                                onClick = { save(selected[0], selected[1]) }
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.size(8.dp))
                                Text("Add widget", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun save(a: WidgetBreakdownKind, b: WidgetBreakdownKind) {
        WidgetDonutPrefs.save(this, appWidgetId, a, b)
        lifecycleScope.launch {
            val app = applicationContext
            try {
                val glanceId = GlanceAppWidgetManager(app).getGlanceIdBy(appWidgetId)
                updateAppWidgetState(app, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[stringPreferencesKey("donut_slots")] =
                            "${a.name}|${b.name}|${System.currentTimeMillis()}"
                    }
                }
                DonutBreakdownWidget().update(app, glanceId)
            } catch (_: Exception) {
                DonutBreakdownWidget().updateAll(app)
            }
            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}

@androidx.compose.runtime.Composable
private fun ConfigureOptionChip(
    kind: WidgetBreakdownKind,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val border = if (selected) {
        androidx.compose.foundation.BorderStroke(1.dp, ConfigureMock.indigo.copy(alpha = 0.6f))
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, ConfigureMock.cardBorder)
    }
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) ConfigureMock.indigo.copy(alpha = 0.15f) else Color(0x80374151),
        border = border
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (selected) ConfigureMock.indigo.copy(alpha = 0.2f) else Color(0xFF1F2937)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = kind.previewIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (selected) kind.accent() else ConfigureMock.muted
                    )
                }
            }
            Text(
                kind.label(),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) ConfigureMock.indigoText else ConfigureMock.chipText,
                maxLines = 1,
                fontSize = 12.sp
            )
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (selected) ConfigureMock.indigoText else Color.Transparent
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun ConfigureLivePreview(selected: List<WidgetBreakdownKind>) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xB3030712),
        border = androidx.compose.foundation.BorderStroke(1.dp, ConfigureMock.cardBorder.copy(alpha = 0.8f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Widget Live Preview",
                    style = MaterialTheme.typography.labelMedium,
                    color = ConfigureMock.muted
                )
                val pulse = rememberInfiniteTransition(label = "dynamic")
                val pulseAlpha by pulse.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dynamicAlpha"
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(pulseAlpha)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Text(
                        "Dynamic",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = ConfigureMock.indigoText,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ConfigurePreviewTile(
                    title = selected.getOrNull(0)?.shortTitle() ?: "Select 1st",
                    kind = selected.getOrNull(0),
                    dimmed = selected.getOrNull(0) == null,
                    modifier = Modifier.weight(1f)
                )
                ConfigurePreviewTile(
                    title = selected.getOrNull(1)?.shortTitle() ?: "Select 2nd",
                    kind = selected.getOrNull(1),
                    dimmed = selected.getOrNull(1) == null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ConfigurePreviewTile(
    title: String,
    kind: WidgetBreakdownKind?,
    dimmed: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 64.dp)
            .alpha(if (dimmed) 0.4f else 1f),
        shape = RoundedCornerShape(8.dp),
        color = ConfigureMock.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, ConfigureMock.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .then(if (dimmed) Modifier else Modifier),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = ConfigureMock.chipText,
                    maxLines = 1
                )
                Icon(
                    imageVector = kind.previewIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = kind.accent()
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (dimmed) 0.2f else 0.7f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = kind.accent(),
                trackColor = Color(0xFF1F2937)
            )
        }
    }
}

private object ConfigureMock {
    val card = Color(0xFF111827)
    val cardBorder = Color(0xFF1F2937)
    val title = Color.White
    val muted = Color(0xFF9CA3AF)
    val chipText = Color(0xFFE5E7EB)
    val indigo = Color(0xFF6366F1)
    val indigoBtn = Color(0xFF4F46E5)
    val indigoText = Color(0xFFA5B4FC)
    val amber = Color(0xFFFBBF24)
}

private fun WidgetBreakdownKind?.accent(): Color = when (this) {
    WidgetBreakdownKind.EXPENSE -> Color(0xFF818CF8)
    WidgetBreakdownKind.PAYMENT -> Color(0xFFC084FC)
    WidgetBreakdownKind.INCOME -> Color(0xFF34D399)
    WidgetBreakdownKind.TAGS -> Color(0xFFFBBF24)
    WidgetBreakdownKind.FUNDS -> Color(0xFF60A5FA)
    WidgetBreakdownKind.TOP10 -> Color(0xFFFB7185)
    null -> Color(0xFF818CF8)
}

private fun WidgetBreakdownKind?.previewIcon() = when (this) {
    WidgetBreakdownKind.EXPENSE -> Icons.Filled.PieChart
    WidgetBreakdownKind.PAYMENT -> Icons.Filled.CreditCard
    WidgetBreakdownKind.INCOME -> Icons.Filled.AccountBalanceWallet
    WidgetBreakdownKind.TAGS -> Icons.Filled.Sell
    WidgetBreakdownKind.FUNDS -> Icons.Filled.AccountBalance
    WidgetBreakdownKind.TOP10 -> Icons.Filled.Star
    null -> Icons.Filled.PieChart
}

private fun WidgetBreakdownKind.shortTitle(): String = when (this) {
    WidgetBreakdownKind.EXPENSE -> "Expense"
    WidgetBreakdownKind.PAYMENT -> "Payment"
    WidgetBreakdownKind.INCOME -> "Income"
    WidgetBreakdownKind.TAGS -> "Tags"
    WidgetBreakdownKind.FUNDS -> "Funds"
    WidgetBreakdownKind.TOP10 -> "Top 10"
}

private fun WidgetBreakdownKind.label(): String = when (this) {
    WidgetBreakdownKind.EXPENSE -> "Expense breakdown"
    WidgetBreakdownKind.PAYMENT -> "Payment breakdown"
    WidgetBreakdownKind.INCOME -> "Income breakdown"
    WidgetBreakdownKind.TAGS -> "Tags breakdown"
    WidgetBreakdownKind.FUNDS -> "Funds breakdown"
    WidgetBreakdownKind.TOP10 -> "Top 10 spending"
}
