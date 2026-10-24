package com.mknlabs.expensetracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.mknlabs.expensetracker.data.constants.SYSTEM_LANGUAGE_CODE
import com.mknlabs.expensetracker.data.constants.normalizeLanguageCode

/**
 * Synchronous storage for the selected app language preference.
 *
 * Like [ThemePreferenceSync], this allows MainActivity to read the chosen language
 * synchronously in `attachBaseContext` and before `super.onCreate()`, ensuring that
 * custom locales are reliably applied on cold boot and across OEM ROMs
 * (e.g., Samsung OneUI 3.x and Xiaomi MIUI on Android 11 / API <= 32) without
 * waiting for asynchronous DataStore flow emissions.
 */
object LocalePreferenceSync {
    private const val PREFS_NAME = "locale_prefs_sync"
    private const val KEY_LANGUAGE_CODE = "language_code"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getLanguage(context: Context): String {
        val raw = getPrefs(context).getString(KEY_LANGUAGE_CODE, SYSTEM_LANGUAGE_CODE) ?: SYSTEM_LANGUAGE_CODE
        return normalizeLanguageCode(raw)
    }

    fun setLanguage(context: Context, languageCode: String) {
        val normalized = normalizeLanguageCode(languageCode)
        getPrefs(context).edit().putString(KEY_LANGUAGE_CODE, normalized).apply()
    }
}
