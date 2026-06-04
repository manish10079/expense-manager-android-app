package com.mknlabs.expensetracker.utils

import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess

object AppRestartUtils {
    /**
     * Restarts the application by launching the main activity and killing the current process.
     * This is necessary when the underlying database file is replaced to ensure Hilt
     * singletons and Room instances are re-initialized correctly.
     */
    fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val mainIntent = Intent.makeRestartActivityTask(intent?.component)
        context.startActivity(mainIntent)
        exitProcess(0)
    }
}
