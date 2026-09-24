package com.mknlabs.expensetracker

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Debug variant: uses the SDK's debug provider so development is not blocked by
 * integrity checks.
 *
 * Out of the box the provider mints a random debug secret per install and prints it
 * to logcat, so every fresh install costs a trip to the Firebase console before
 * sign-in works again — the secret only survives an update because it is kept in
 * app-private storage.
 *
 * Set `appCheckDebugToken` in the gitignored `localProperties.properties` to pin it.
 * The same secret is then used by every build on every device, so it is registered in
 * the console once and keeps working across fresh installs and data wipes. Left blank,
 * the stock rotating behaviour is unchanged.
 *
 * What gets pinned is the SDK's *debug secret*, not a token. It is written into the
 * slot the SDK's own provider reads from, so the provider still exchanges it with the
 * App Check backend and signs in with the token the backend issues. Returning a
 * self-made [com.google.firebase.appcheck.AppCheckToken] instead does not work:
 * `DefaultFirebaseAppCheck` re-reads the JWT's claims to learn when the token expires,
 * so a plain secret is rejected with "Invalid token (too few subsections)" and
 * sign-in fails.
 */
object AppCheckInitializer {
    fun initialize(context: Context) {
        val pinnedSecret = BuildConfig.APP_CHECK_TOKEN.ifEmpty { BuildConfig.APP_CHECK_DEBUG_TOKEN }
        if (pinnedSecret.isNotBlank()) {
            seedDebugSecret(context, pinnedSecret)
        }
        // The stock factory either way: it is the only thing that knows how to trade
        // the secret for a real App Check token.
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
    }

    /**
     * Writes [secret] where `DebugAppCheckProvider` looks before minting its own.
     *
     * Both strings are the SDK's storage contract (`internal.StorageHelper`): the
     * preferences file is `com.google.firebase.appcheck.debug.store.<persistenceKey>`
     * and the entry is keyed below. If a future SDK renames them the write simply
     * lands nowhere and the provider falls back to minting a fresh secret — the old
     * behaviour, not a broken build.
     */
    private fun seedDebugSecret(context: Context, secret: String) {
        val persistenceKey = FirebaseApp.getInstance().persistenceKey
        context.getSharedPreferences(
            DEBUG_STORE_PREFS_PREFIX + persistenceKey,
            Context.MODE_PRIVATE
        ).edit()
            .putString(DEBUG_SECRET_KEY, secret)
            .apply()
    }

    private const val DEBUG_STORE_PREFS_PREFIX = "com.google.firebase.appcheck.debug.store."
    private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
}
