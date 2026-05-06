package com.mkn0079.expensetracker.utils

import android.content.Context
import android.content.SharedPreferences

object ThemePreferenceSync {
    private const val PREFS_NAME = "theme_prefs_sync"
    private const val KEY_THEME_MODE = "theme_mode"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getTheme(context: Context): String {
        return getPrefs(context).getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
    }

    fun setTheme(context: Context, themeMode: String) {
        getPrefs(context).edit().putString(KEY_THEME_MODE, themeMode).apply()
    }
}
