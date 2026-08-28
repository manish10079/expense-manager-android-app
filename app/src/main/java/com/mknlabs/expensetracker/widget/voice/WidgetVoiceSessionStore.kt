package com.mknlabs.expensetracker.widget.voice

import android.content.Context
import android.content.SharedPreferences
import com.mknlabs.expensetracker.widget.model.WidgetParsedTransaction
import com.mknlabs.expensetracker.widget.model.WidgetState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Persists temporary widget voice session state.
 *
 * Survives widget refreshes (process death) so the user can still
 * save or cancel after the widget re-renders.
 *
 * Uses SharedPreferences (not DataStore) because widget callbacks
 * need synchronous reads and DataStore doesn't support that from
 * BroadcastReceiver context.
 *
 * Session lifecycle:
 * 1. StartRecording → creates new session ID
 * 2. Listening → transcript stored
 * 3. Processing → state updated
 * 4. Preview → parsed transaction serialized
 * 5. Save/Cancel → session cleared
 */
internal class WidgetVoiceSessionStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    /** Generate a new session ID and reset all session data. */
    fun startNewSession(): String {
        val sessionId = UUID.randomUUID().toString()
        prefs.edit().clear().putString(KEY_SESSION_ID, sessionId).apply()
        return sessionId
    }

    /** Get the current session ID, or null if no active session. */
    fun getSessionId(): String? = prefs.getString(KEY_SESSION_ID, null)

    /** Update the widget state. */
    fun setState(state: WidgetState) {
        prefs.edit().putString(KEY_STATE, state::class.java.simpleName).apply()
    }

    /** Get the current widget state. */
    fun getState(): WidgetState? {
        val stateName = prefs.getString(KEY_STATE, null) ?: return null
        return when (stateName) {
            "Idle" -> WidgetState.Idle
            "Listening" -> WidgetState.Listening
            "Processing" -> WidgetState.Processing
            "Saving" -> WidgetState.Saving
            else -> null
        }
    }

    /** Store the speech transcript. */
    fun setTranscript(transcript: String) {
        prefs.edit().putString(KEY_TRANSCRIPT, transcript).apply()
    }

    /** Get the stored transcript. */
    fun getTranscript(): String = prefs.getString(KEY_TRANSCRIPT, "") ?: ""

    /** Store parsed transaction data for preview. */
    fun setParsedTransaction(parsed: WidgetParsedTransaction) {
        prefs.edit()
            .putLong(KEY_PARSED_AMOUNT_MINOR, parsed.amountMinor)
            .putString(KEY_PARSED_AMOUNT_TEXT, parsed.amountText)
            .putString(KEY_PARSED_CATEGORY_NAME, parsed.categoryName)
            .putInt(KEY_PARSED_CATEGORY_ID, parsed.categoryId)
            .putString(KEY_PARSED_MERCHANT, parsed.merchant ?: "")
            .putString(KEY_PARSED_NOTE, parsed.note)
            .putInt(KEY_PARSED_TX_TYPE_ID, parsed.transactionTypeId)
            .putInt(KEY_PARSED_PAYMENT_TYPE_ID, parsed.paymentTypeId ?: 0)
            .putString(KEY_PARSED_CONFIDENCE, parsed.confidenceText ?: "")
            .putLong(KEY_PARSED_CREATED_AT, parsed.createdAt)
            .putString(KEY_STATE, "Preview")
            .apply()
    }

    /** Retrieve the stored parsed transaction, or null if not present. */
    fun getParsedTransaction(): WidgetParsedTransaction? {
        val amountMinor = prefs.getLong(KEY_PARSED_AMOUNT_MINOR, 0L)
        if (amountMinor == 0L && !prefs.contains(KEY_PARSED_AMOUNT_MINOR)) return null

        return WidgetParsedTransaction(
            amountMinor = amountMinor,
            amountText = prefs.getString(KEY_PARSED_AMOUNT_TEXT, "") ?: "",
            categoryName = prefs.getString(KEY_PARSED_CATEGORY_NAME, "") ?: "",
            categoryId = prefs.getInt(KEY_PARSED_CATEGORY_ID, 0),
            merchant = prefs.getString(KEY_PARSED_MERCHANT, "")?.takeIf { it.isNotBlank() },
            note = prefs.getString(KEY_PARSED_NOTE, "") ?: "",
            transactionTypeId = prefs.getInt(KEY_PARSED_TX_TYPE_ID, 2),
            paymentTypeId = prefs.getInt(KEY_PARSED_PAYMENT_TYPE_ID, 0).takeIf { it > 0 },
            confidenceText = prefs.getString(KEY_PARSED_CONFIDENCE, "")?.takeIf { it.isNotBlank() },
            createdAt = prefs.getLong(KEY_PARSED_CREATED_AT, System.currentTimeMillis())
        )
    }

    /** Store the error message resource ID. */
    fun setError(errorMessageResId: Int) {
        prefs.edit()
            .putString(KEY_STATE, "Error")
            .putInt(KEY_ERROR_RES_ID, errorMessageResId)
            .apply()
    }

    /** Get the stored error message resource ID. */
    fun getErrorResId(): Int = prefs.getInt(KEY_ERROR_RES_ID, 0)

    /** Clear all session data. */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    /** Whether there is an active session (not idle, not cleared). */
    fun hasActiveSession(): Boolean {
        return getSessionId() != null && getState() != null && getState() != WidgetState.Idle
    }

    companion object {
        private const val PREFS_NAME = "widget_voice_session"

        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_STATE = "state"
        private const val KEY_TRANSCRIPT = "transcript"
        private const val KEY_PARSED_AMOUNT_MINOR = "parsed_amount_minor"
        private const val KEY_PARSED_AMOUNT_TEXT = "parsed_amount_text"
        private const val KEY_PARSED_CATEGORY_NAME = "parsed_category_name"
        private const val KEY_PARSED_CATEGORY_ID = "parsed_category_id"
        private const val KEY_PARSED_MERCHANT = "parsed_merchant"
        private const val KEY_PARSED_NOTE = "parsed_note"
        private const val KEY_PARSED_TX_TYPE_ID = "parsed_tx_type_id"
        private const val KEY_PARSED_PAYMENT_TYPE_ID = "parsed_payment_type_id"
        private const val KEY_PARSED_CONFIDENCE = "parsed_confidence"
        private const val KEY_PARSED_CREATED_AT = "parsed_created_at"
        private const val KEY_ERROR_RES_ID = "error_res_id"

        @Volatile
        private var instance: WidgetVoiceSessionStore? = null

        fun getInstance(context: Context): WidgetVoiceSessionStore {
            return instance ?: synchronized(this) {
                instance ?: WidgetVoiceSessionStore(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
