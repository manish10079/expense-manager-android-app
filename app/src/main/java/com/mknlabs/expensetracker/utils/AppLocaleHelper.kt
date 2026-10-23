package com.mknlabs.expensetracker.utils

import android.app.Activity
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

    fun applyLanguage(languageCode: String, activity: Activity? = null) {
        val normalized = normalizeLanguageCode(languageCode)
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val targetLocales = if (normalized == SYSTEM_LANGUAGE_CODE) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(normalized)
        }

        val currentTags = currentLocales.toLanguageTags()
        val targetTags = targetLocales.toLanguageTags()

        if (currentTags == targetTags) {
            return
        }

        AppCompatDelegate.setApplicationLocales(targetLocales)
        appliedLanguageTags = targetTags

        // Deterministic recreation fallback:
        // On Android 13+ (API 33-36), AppCompatDelegate.setApplicationLocales delegates to
        // system LocaleManager which recreates the Activity if not handled in configChanges.
        // On API 24-32 or certain OEM ROMs (Samsung OneUI, Xiaomi HyperOS, Oppo/Vivo),
        // AppCompat's recreation can be suppressed or delayed. We post a check to recreate
        // if the Activity is still alive and has not started recreating yet.
        activity?.let { act ->
            act.window?.decorView?.post {
                if (!act.isFinishing && !act.isDestroyed && !act.isChangingConfigurations) {
                    act.recreate()
                }
            }
        }
    }

    fun getCurrentLanguageTag(): String {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.isEmpty) {
            return SYSTEM_LANGUAGE_CODE
        }
        return currentLocales.toLanguageTags().ifBlank { SYSTEM_LANGUAGE_CODE }
    }
}
