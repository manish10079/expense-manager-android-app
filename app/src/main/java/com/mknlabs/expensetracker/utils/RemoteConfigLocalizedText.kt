package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.data.constants.SYSTEM_LANGUAGE_CODE
import com.mknlabs.expensetracker.data.constants.normalizeLanguageCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import java.util.Locale

/**
 * Resolves Remote Config copy that is either a plain string or JSON keyed by
 * app language (`en`, `hi`, `pt-BR`, …). Nested objects may use `title` / `message`.
 */
fun localizedRemoteConfigText(
    raw: String,
    languageTag: String,
    nestedField: String? = null
): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ""
    if (!trimmed.startsWith("{")) return trimmed
    return try {
        val root = Json.parseToJsonElement(trimmed).jsonObject
        for (key in languageLookupKeys(languageTag)) {
            val text = extractLocalizedValue(root[key], nestedField)
            if (text.isNotBlank()) return text
        }
        extractLocalizedValue(root["en"], nestedField)
    } catch (_: Exception) {
        trimmed
    }
}

private fun languageLookupKeys(languageTag: String): List<String> {
    val normalized = normalizeLanguageCode(languageTag)
    if (normalized == SYSTEM_LANGUAGE_CODE || normalized.isBlank()) {
        val device = Locale.getDefault()
        val tag = if (device.country.isNotBlank()) {
            "${device.language}-${device.country}"
        } else {
            device.language
        }
        val deviceNorm = normalizeLanguageCode(tag)
        return listOf(deviceNorm, device.language.lowercase(), "en").distinct()
    }
    val languageOnly = normalized.substringBefore('-')
    return listOf(normalized, languageOnly, "en").distinct()
}

private fun extractLocalizedValue(
    element: kotlinx.serialization.json.JsonElement?,
    nestedField: String?
): String {
    if (element == null) return ""
    if (element is JsonPrimitive) {
        return element.contentOrNull?.trim().orEmpty()
    }
    if (element is JsonObject && nestedField != null) {
        val nested = element[nestedField] ?: return ""
        if (nested is JsonPrimitive) {
            return nested.contentOrNull?.trim().orEmpty()
        }
    }
    return ""
}
