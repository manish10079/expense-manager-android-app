package com.mknlabs.expensetracker

import android.content.Context
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Release variant: uses debug token for testing purposes.
 * TODO: Switch to PlayIntegrityAppCheckProviderFactory after Play Store release.
 */
object AppCheckInitializer {
    fun initialize(@Suppress("UNUSED_PARAMETER") context: Context) {
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }
}
