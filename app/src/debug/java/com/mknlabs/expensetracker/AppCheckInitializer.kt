package com.mknlabs.expensetracker

import android.content.Context
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Debug variant: uses the debug token provider so development is not
 * blocked by integrity checks.  The debug token is printed to logcat
 * and must be registered in the Firebase Console → App Check → Debug tokens.
 */
object AppCheckInitializer {
    fun initialize(@Suppress("UNUSED_PARAMETER") context: Context) {
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }
}
