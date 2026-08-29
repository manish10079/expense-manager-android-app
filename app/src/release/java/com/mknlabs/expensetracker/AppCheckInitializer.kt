package com.mknlabs.expensetracker

import android.content.Context
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Release variant: uses Play Integrity App Check provider.
 */
object AppCheckInitializer {
    fun initialize(@Suppress("UNUSED_PARAMETER") context: Context) {
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }
}
