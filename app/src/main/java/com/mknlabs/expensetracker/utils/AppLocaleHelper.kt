package com.mknlabs.expensetracker.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import com.mknlabs.expensetracker.data.constants.SYSTEM_LANGUAGE_CODE
import com.mknlabs.expensetracker.data.constants.normalizeLanguageCode
import java.util.Locale

object AppLocaleHelper {
    var appliedLanguageTags by mutableStateOf(
        AppCompatDelegate.getApplicationLocales().toLanguageTags()
    )
        private set

    /**
     * Applies a chosen language across the application.
     *
     * 1. Synchronously records the preference to [LocalePreferenceSync] for instant cold-start restore.
     * 2. Sets [AppCompatDelegate.setApplicationLocales] for system / AppCompat integration.
     * 3. Sets [Locale.setDefault] so standard formatters and number parsing adapt immediately.
     * 4. Updates the reactive Compose state [appliedLanguageTags] to trigger immediate UI recomposition.
     * 5. On Android 12 and below (API <= 32), recreates the Activity if alive to ensure native window
     *    and OEM-level caches (e.g. Samsung OneUI 3.x, Xiaomi MIUI 12.5) apply the new configuration.
     */
    fun applyLanguage(languageCode: String, context: Context? = null) {
        val normalized = normalizeLanguageCode(languageCode)

        // 1. Synchronously persist to SharedPreferences
        context?.let { LocalePreferenceSync.setLanguage(it, normalized) }

        // 2. Prepare target locales
        val targetLocales = if (normalized == SYSTEM_LANGUAGE_CODE) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(normalized)
        }

        val targetTags = targetLocales.toLanguageTags()
        val currentDelegateTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val localesAlreadyApplied =
            currentDelegateTags == targetTags && appliedLanguageTags == targetTags

        // 3. Update JVM default locale
        val primaryLocale = if (normalized == SYSTEM_LANGUAGE_CODE) {
            Resources.getSystem().configuration.locales[0] ?: Locale.getDefault()
        } else {
            Locale.forLanguageTag(normalized)
        }
        Locale.setDefault(primaryLocale)

        // 4-5. Skip setApplicationLocales when unchanged: on API 32 and below it
        // recreates the Activity even for a no-op, which retriggers splash.
        if (!localesAlreadyApplied) {
            AppCompatDelegate.setApplicationLocales(targetLocales)
        }
        appliedLanguageTags = targetTags

        // 6. On API <= 32, recreate only if the Activity config is still stale.
        val activity = context?.findFragmentActivity()
        activity?.let { act ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                activityNeedsLocaleRecreate(act, normalized)
            ) {
                act.window?.decorView?.post {
                    if (!act.isFinishing && !act.isDestroyed && !act.isChangingConfigurations) {
                        act.recreate()
                    }
                }
            }
        }
    }

    private fun activityNeedsLocaleRecreate(
        activity: android.app.Activity,
        normalized: String
    ): Boolean {
        val current = activity.resources.configuration.locales[0] ?: return true
        if (normalized == SYSTEM_LANGUAGE_CODE) return false
        val currentTag = current.toLanguageTag()
        return !currentTag.equals(normalized, ignoreCase = true) &&
            !currentTag.startsWith("$normalized-", ignoreCase = true)
    }

    fun getCurrentLanguageTag(): String {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.isEmpty) {
            val fromState = appliedLanguageTags
            if (fromState.isNotBlank()) return fromState
            return SYSTEM_LANGUAGE_CODE
        }
        return currentLocales.toLanguageTags().ifBlank { SYSTEM_LANGUAGE_CODE }
    }

    /**
     * Creates a Configuration-based context for [MainActivity.attachBaseContext].
     */
    fun createConfigurationContext(base: Context, languageCode: String): Context {
        val normalized = normalizeLanguageCode(languageCode)
        if (normalized == SYSTEM_LANGUAGE_CODE) return base

        val locale = Locale.forLanguageTag(normalized)
        val config = Configuration(base.resources.configuration).apply {
            setLocale(locale)
            setLocales(LocaleList(locale))
            setLayoutDirection(locale)
        }
        return base.createConfigurationContext(config)
    }

    /**
     * Creates a [ContextWrapper] that overrides [Resources] with the specified language
     * while preserving the underlying [android.app.Activity] reference so that
     * [findFragmentActivity], Hilt ViewModel injection, and window controllers work seamlessly.
     */
    fun createLocalizedContext(base: Context, languageCode: String): Context {
        val normalized = normalizeLanguageCode(languageCode)
        if (normalized == SYSTEM_LANGUAGE_CODE) return base

        val locale = Locale.forLanguageTag(normalized)
        val config = Configuration(base.resources.configuration).apply {
            setLocale(locale)
            setLocales(LocaleList(locale))
            setLayoutDirection(locale)
        }
        return LocalizedContext(base, config)
    }

    private class LocalizedContext(
        base: Context,
        private val localizedConfig: Configuration
    ) : ContextWrapper(base) {
        private val localizedResources: Resources by lazy {
            base.createConfigurationContext(localizedConfig).resources
        }

        override fun getResources(): Resources = localizedResources

        override fun getApplicationContext(): Context {
            return baseContext.applicationContext ?: baseContext
        }

        override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
            val merged = Configuration(localizedConfig).apply {
                setTo(overrideConfiguration)
            }
            return LocalizedContext(baseContext, merged)
        }
    }
}
