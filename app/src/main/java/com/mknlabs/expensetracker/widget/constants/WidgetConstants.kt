package com.mknlabs.expensetracker.widget.constants

/**
 * Constants for the Expense Home Widget.
 *
 * Contains action parameter keys and values used by widget receivers,
 * services, and action callbacks. No hardcoded strings elsewhere.
 */
internal object WidgetConstants {

    /** Intent extra key for the widget action to execute. */
    const val ACTION_KEY = "widget_action"

    /** Intent extra key for the session ID linking service → widget. */
    const val SESSION_ID_KEY = "widget_session_id"

    /** Intent extra key for error message resource ID. */
    const val ERROR_RES_ID_KEY = "widget_error_res_id"

    /** Start voice recording from widget mic tap. */
    const val ACTION_START_RECORDING = "com.mknlabs.expensetracker.action.START_RECORDING"

    /** Save the parsed transaction from widget preview. */
    const val ACTION_SAVE_TRANSACTION = "com.mknlabs.expensetracker.action.SAVE_TRANSACTION"

    /** Cancel the current voice session and return to idle. */
    const val ACTION_CANCEL_TRANSACTION = "com.mknlabs.expensetracker.action.CANCEL_TRANSACTION"

    /** Retry after an error state. */
    const val ACTION_RETRY = "com.mknlabs.expensetracker.action.RETRY"

    /** Notification channel for foreground voice recording service. */
    const val VOICE_CHANNEL_ID = "widget_voice_recording"

    /** Notification ID for foreground voice recording service. */
    const val VOICE_NOTIFICATION_ID = 7701

    /** Widget data store file name. */
    const val DATA_STORE_NAME = "widget_session_store"

    /** Max duration for speech recognition before auto-stop (ms). */
    const val SPEECH_TIMEOUT_MS = 15000L
}
