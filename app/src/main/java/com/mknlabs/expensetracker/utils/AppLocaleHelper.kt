package com.mknlabs.expensetracker.utils

import android.app.Activity
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
import androidx.core.text.TextUtilsCompat
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
     * 4. Updates legacy resources configurations on the active context and application context
     *    to immediately invalidate cached OEM resources (e.g. Xiaomi MIUI / HyperOS, Samsung One UI).
     * 5. Updates the reactive Compose state [appliedLanguageTags] to trigger immediate UI recomposition.
     * 6. Recreates the Activity if alive and its current configuration is still stale, ensuring
     *    deterministic refresh across all Android versions (API 24 to 36) including OEM ROMs that
     *    suppress system-driven recreations.
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

        // 3. Update JVM default locale
        val primaryLocale = if (normalized == SYSTEM_LANGUAGE_CODE) {
            getSystemPrimaryLocale()
        } else {
            Locale.forLanguageTag(normalized)
        }
        Locale.setDefault(primaryLocale)

        // 4. Update legacy resources on the active context and application context
        // to immediately invalidate OEM resource caches (Samsung One UI, Xiaomi MIUI/HyperOS, ColorOS)
        context?.let { ctx ->
            updateLegacyResources(ctx, primaryLocale)
            val appCtx = ctx.applicationContext
            if (appCtx != null && appCtx !== ctx) {
                updateLegacyResources(appCtx, primaryLocale)
            }
        }

        // 5. Check if the Activity configuration is currently stale
        val activity = context?.findFragmentActivity()
        val activityIsStale = activity != null && activityNeedsLocaleRecreate(activity, normalized)
        val localesAlreadyApplied =
            !activityIsStale && currentDelegateTags == targetTags && appliedLanguageTags == targetTags

        if (!localesAlreadyApplied) {
            AppCompatDelegate.setApplicationLocales(targetLocales)
        }
        appliedLanguageTags = targetTags

        // Synchronize window layout direction
        activity?.window?.decorView?.let { decor ->
            decor.layoutDirection = TextUtilsCompat.getLayoutDirectionFromLocale(primaryLocale)
        }

        // 6. Deterministic recreation fallback across all API versions (24-36)
        // Many OEM skins (One UI 5/6, MIUI 14 / HyperOS, ColorOS 13/14) suppress system-driven
        // activity recreations on Android 13+. If the Activity's configuration is still stale,
        // recreate it safely.
        activity?.let { act ->
            if (activityNeedsLocaleRecreate(act, normalized)) {
                act.window?.decorView?.post {
                    if (!act.isFinishing && !act.isDestroyed && !act.isChangingConfigurations) {
                        act.recreate()
                    }
                }
            }
        }
    }

    private fun getSystemPrimaryLocale(): Locale {
        val systemLocales = Resources.getSystem().configuration.locales
        return if (!systemLocales.isEmpty) {
            systemLocales[0] ?: Locale.getDefault()
        } else {
            Locale.getDefault()
        }
    }

    @Suppress("DEPRECATION")
    private fun updateLegacyResources(context: Context, locale: Locale) {
        runCatching {
            val res = context.resources
            val config = Configuration(res.configuration).apply {
                setLocale(locale)
                setLocales(LocaleList(locale))
                setLayoutDirection(locale)
            }
            res.updateConfiguration(config, res.displayMetrics)
        }
    }

    internal fun activityNeedsLocaleRecreate(
        activity: Activity,
        normalized: String
    ): Boolean {
        val configLocales = activity.resources.configuration.locales
        val current = if (!configLocales.isEmpty) {
            configLocales[0] ?: return true
        } else {
            return true
        }

        val target = if (normalized == SYSTEM_LANGUAGE_CODE) {
            getSystemPrimaryLocale()
        } else {
            Locale.forLanguageTag(normalized)
        }

        return !areLocalesEquivalent(current, target)
    }

    internal fun areLocalesEquivalent(l1: Locale, l2: Locale): Boolean {
        val lang1 = normalizeIsoLanguage(l1.language)
        val lang2 = normalizeIsoLanguage(l2.language)
        if (lang1 != lang2) return false

        val country1 = l1.country.orEmpty()
        val country2 = l2.country.orEmpty()
        if (country1.isNotEmpty() && country2.isNotEmpty()) {
            return country1.equals(country2, ignoreCase = true)
        }
        return true
    }

    private fun normalizeIsoLanguage(lang: String): String {
        val lower = lang.lowercase()
        return when (lower) {
            "in" -> "id"
            "iw" -> "he"
            "ji" -> "yi"
            else -> lower
        }
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
        val locale = if (normalized == SYSTEM_LANGUAGE_CODE) {
            getSystemPrimaryLocale()
        } else {
            Locale.forLanguageTag(normalized)
        }

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
        val locale = if (normalized == SYSTEM_LANGUAGE_CODE) {
            getSystemPrimaryLocale()
        } else {
            Locale.forLanguageTag(normalized)
        }

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
