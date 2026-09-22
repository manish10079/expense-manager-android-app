package com.mknlabs.expensetracker

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckProvider
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.AppCheckToken
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import java.util.concurrent.TimeUnit

/**
 * Debug variant: uses a debug token provider so development is not blocked by
 * integrity checks.
 *
 * Out of the box the SDK mints a random token per install and prints it to
 * logcat, so every fresh install costs a trip to the Firebase console
 * (App Check → Apps → Manage debug tokens) before sign-in works again — the
 * token only survives an update because it is kept in app-private storage.
 *
 * Set `appCheckDebugToken` in the gitignored `localProperties.properties` to pin
 * the token instead. The same value is then sent by every build, on any device,
 * and it keeps working across fresh installs and data wipes: register it once and
 * it never has to be registered again. Left blank, the stock rotating debug
 * provider is used exactly as before.
 */
object AppCheckInitializer {
    fun initialize(@Suppress("UNUSED_PARAMETER") context: Context) {
        val appCheck = FirebaseAppCheck.getInstance()
        val pinnedToken = BuildConfig.APP_CHECK_DEBUG_TOKEN
        appCheck.installAppCheckProviderFactory(
            if (pinnedToken.isBlank()) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PinnedTokenAppCheckProviderFactory(pinnedToken)
            }
        )
    }
}

/**
 * Serves one fixed token instead of letting the SDK mint a fresh one. The token
 * still has to be registered as a debug token in the console — that is what makes
 * the backend accept it — but the value never changes, so it is a one-time step.
 */
private class PinnedTokenAppCheckProviderFactory(
    private val token: String
) : AppCheckProviderFactory {
    override fun create(firebaseApp: FirebaseApp): AppCheckProvider =
        PinnedTokenAppCheckProvider(token)
}

private class PinnedTokenAppCheckProvider(private val token: String) : AppCheckProvider {
    override fun getToken(): Task<AppCheckToken> = Tasks.forResult(
        PinnedAppCheckToken(token, System.currentTimeMillis() + TOKEN_LIFETIME_MILLIS)
    )

    private companion object {
        /**
         * A registered debug token is valid until it is deleted in the console;
         * this only decides how often the SDK re-fetches, so keep it long.
         */
        val TOKEN_LIFETIME_MILLIS = TimeUnit.DAYS.toMillis(30)
    }
}

/** [AppCheckToken] is abstract, so a fixed token is one small implementation of it. */
private class PinnedAppCheckToken(
    private val value: String,
    private val expiresAt: Long
) : AppCheckToken() {
    override fun getToken(): String = value
    override fun getExpireTimeMillis(): Long = expiresAt
}
