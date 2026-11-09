package com.mknlabs.expensetracker.widget.overlay

import android.content.Context
import android.content.Intent
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionStartActivity

internal const val EXTRA_OVERLAY_UI_MODE = "ui_mode"

internal object OverlayUiMode {
    const val TEXT = "TEXT"
    const val VOICE = "VOICE"
    const val SCAN = "SCAN"
}

internal val OverlayUiModeKey = ActionParameters.Key<String>(EXTRA_OVERLAY_UI_MODE)

internal fun widgetOverlayIntent(context: Context, mode: String): Intent {
    return Intent(context, WidgetOverlayActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        .putExtra(EXTRA_OVERLAY_UI_MODE, mode)
}

internal fun widgetOverlayAction(context: Context, mode: String): Action {
    return actionStartActivity(
        widgetOverlayIntent(context, mode),
        actionParametersOf(OverlayUiModeKey to mode)
    )
}
