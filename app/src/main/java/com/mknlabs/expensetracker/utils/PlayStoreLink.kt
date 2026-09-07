package com.mknlabs.expensetracker.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens this app's Play Store listing.
 *
 * Tries the native `market://` deep link first and falls back to the HTTPS
 * web URL when no Play Store client is installed (e.g. emulators, some
 * custom ROMs, or restricted devices).
 */
object PlayStoreLink {

    fun openPlayStore(context: Context) {
        val packageName = context.packageName

        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(marketIntent)
        } catch (e: ActivityNotFoundException) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }
}