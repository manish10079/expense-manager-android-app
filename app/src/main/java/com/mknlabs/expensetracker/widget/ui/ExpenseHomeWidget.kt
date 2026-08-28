package com.mknlabs.expensetracker.widget.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.widget.model.WidgetState
import com.mknlabs.expensetracker.widget.model.WidgetUiState
import com.mknlabs.expensetracker.widget.voice.WidgetVoiceSessionStore

/**
 * Glance home widget — premium glassmorphic dark UI.
 *
 * Uses ImageProvider(R.drawable.bg_*) for drawable backgrounds.
 * This is the correct way to apply drawable backgrounds in Glance.
 */
class ExpenseHomeWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "provideGlance called")

        val sessionStore = WidgetVoiceSessionStore.getInstance(context)
        val uiState = buildUiState(context, sessionStore)

        updateAppWidgetState(context, id) { prefs ->
            prefs[KEY_STATE_NAME] = uiState.state::class.java.simpleName
            prefs[KEY_TODAY_SPENDING] = uiState.todaySpendingMinor
            prefs[KEY_CURRENCY_SYMBOL] = uiState.currencySymbol
            prefs[KEY_TRANSCRIPT] = uiState.transcript

            val parsed = sessionStore.getParsedTransaction()
            if (parsed != null) {
                prefs[KEY_PARSED_AMOUNT_TEXT] = parsed.amountText
                prefs[KEY_PARSED_CATEGORY_NAME] = parsed.categoryName
                prefs[KEY_PARSED_MERCHANT] = parsed.merchant ?: ""
                prefs[KEY_PARSED_NOTE] = parsed.note
                prefs[KEY_PARSED_CONFIDENCE] = parsed.confidenceText ?: ""
            }

            val errorResId = sessionStore.getErrorResId()
            if (errorResId > 0) {
                prefs[KEY_ERROR_RES_ID] = errorResId
            }
        }

        provideContent {
            GlanceTheme {
                ExpenseWidgetContent(uiState)
            }
        }
    }

    companion object {
        private const val TAG = "ExpenseHomeWidget"

        val KEY_STATE_NAME = androidx.datastore.preferences.core.stringPreferencesKey("widget_state_name")
        val KEY_TODAY_SPENDING = androidx.datastore.preferences.core.longPreferencesKey("widget_today_spending")
        val KEY_CURRENCY_SYMBOL = androidx.datastore.preferences.core.stringPreferencesKey("widget_currency_symbol")
        val KEY_TRANSCRIPT = androidx.datastore.preferences.core.stringPreferencesKey("widget_transcript")
        val KEY_PARSED_AMOUNT_TEXT = androidx.datastore.preferences.core.stringPreferencesKey("widget_parsed_amount")
        val KEY_PARSED_CATEGORY_NAME = androidx.datastore.preferences.core.stringPreferencesKey("widget_parsed_category")
        val KEY_PARSED_MERCHANT = androidx.datastore.preferences.core.stringPreferencesKey("widget_parsed_merchant")
        val KEY_PARSED_NOTE = androidx.datastore.preferences.core.stringPreferencesKey("widget_parsed_note")
        val KEY_PARSED_CONFIDENCE = androidx.datastore.preferences.core.stringPreferencesKey("widget_parsed_confidence")
        val KEY_ERROR_RES_ID = androidx.datastore.preferences.core.intPreferencesKey("widget_error_res_id")
    }
}

/**
 * Pure renderer — switches UI by WidgetState.
 * Root uses bg_widget_card.xml for glassmorphic card background.
 */
@Composable
internal fun ExpenseWidgetContent(uiState: WidgetUiState) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.bg_widget_card))
    ) {
        when (val state = uiState.state) {
            is WidgetState.Idle -> {
                val spendingText = if (uiState.todaySpendingMinor > 0) {
                    val major = uiState.todaySpendingMinor / 100.0
                    val formatted = if (major == major.toLong().toDouble()) {
                        "${uiState.currencySymbol}${major.toLong()}"
                    } else {
                        "${uiState.currencySymbol}${String.format("%.2f", major)}"
                    }
                    "Today: $formatted"
                } else ""
                WidgetIdleContent(
                    todaySpendingText = spendingText,
                    currencySymbol = uiState.currencySymbol
                )
            }
            is WidgetState.Listening -> WidgetListeningContent()
            is WidgetState.Processing -> WidgetProcessingContent()
            is WidgetState.Preview -> WidgetPreviewContent(parsedTransaction = state.parsedTransaction)
            is WidgetState.Saving -> WidgetProcessingContent()
            is WidgetState.Error -> WidgetErrorContent(errorMessageResId = state.errorMessageResId)
        }
    }
}

private fun buildUiState(
    context: Context,
    sessionStore: WidgetVoiceSessionStore
): WidgetUiState {
    val state = sessionStore.getState() ?: WidgetState.Idle
    val parsed = sessionStore.getParsedTransaction()
    val actualState = when {
        state is WidgetState.Preview && parsed == null -> WidgetState.Idle
        state is WidgetState.Preview && parsed != null -> state
        else -> state
    }
    return WidgetUiState(
        state = actualState,
        transcript = sessionStore.getTranscript(),
        errorMessageResId = sessionStore.getErrorResId().takeIf { it > 0 }
    )
}
