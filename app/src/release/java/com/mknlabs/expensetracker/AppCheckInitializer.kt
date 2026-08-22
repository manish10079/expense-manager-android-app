package com.mknlabs.expensetracker

import android.content.Context
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Release variant: uses Play Integrity to verify the app is genuine
 * and running on an uncompromised device.
 */
object AppCheckInitializer {
    fun initialize(@Suppress("UNUSED_PARAMETER") context: Context) {
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }
}
