package com.mknlabs.expensetracker.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import com.mknlabs.expensetracker.data.constants.SYSTEM_LANGUAGE_CODE
import com.mknlabs.expensetracker.data.constants.normalizeLanguageCode

object AppLocaleHelper {
    var appliedLanguageTags by mutableStateOf(
        AppCompatDelegate.getApplicationLocales().toLanguageTags()
    )
        private set

    fun applyLanguage(languageCode: String) {
        val normalized = normalizeLanguageCode(languageCode)
        val locales = if (normalized == SYSTEM_LANGUAGE_CODE) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(normalized)
        }
        AppCompatDelegate.setApplicationLocales(locales)
        appliedLanguageTags = locales.toLanguageTags()
    }

    fun getCurrentLanguageTag(): String {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.isEmpty) {
            return SYSTEM_LANGUAGE_CODE
        }
        return currentLocales.toLanguageTags().ifBlank { SYSTEM_LANGUAGE_CODE }
    }
}
