package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.data.constants.SYSTEM_LANGUAGE_CODE
import com.mknlabs.expensetracker.data.constants.normalizeLanguageCode
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import java.util.Locale

private val remoteConfigJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

/**
 * Resolves Remote Config copy that is either a plain string or JSON keyed by
 * app language (`en`, `hi`, `pt-BR`, …). Nested objects may use `title` / `message`.
 * Missing locale keys fall back to English (`en`).
 */
fun localizedRemoteConfigText(
    raw: String,
    languageTag: String,
    nestedField: String? = null,
    fallback: String = "",
    requireLocalizedJson: Boolean = false
): String {
    val trimmed = raw.trim().trimStart('\uFEFF')
    if (trimmed.isEmpty()) return fallback
    if (!trimmed.startsWith("{")) {
        return if (requireLocalizedJson) fallback else trimmed
    }
    return try {
        val root = remoteConfigJson.parseToJsonElement(trimmed).jsonObject
        val byKey = indexByLanguageKey(root)
        for (key in languageLookupKeys(languageTag)) {
            val text = extractLocalizedValue(byKey[key], nestedField)
            if (text.isNotBlank()) return text
        }
        extractLocalizedValue(byKey["en"] ?: root["en"], nestedField)
            .ifBlank { fallback }
    } catch (_: SerializationException) {
        // RC payload is not valid JSON (truncated template, extra commas, etc.).
        fallback
    } catch (_: IllegalArgumentException) {
        // JSON parsed but the root is not an object of language keys.
        fallback
    } catch (_: Exception) {
        // Any other failure while picking a locale string.
        fallback
    }
}

private fun indexByLanguageKey(root: JsonObject): Map<String, JsonElement> {
    val indexed = LinkedHashMap<String, JsonElement>(root.size)
    for ((rawKey, value) in root) {
        val normalized = normalizeLanguageCode(rawKey)
        if (normalized != SYSTEM_LANGUAGE_CODE) {
            indexed.putIfAbsent(normalized, value)
        }
        indexed.putIfAbsent(rawKey.trim(), value)
    }
    return indexed
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
        return listOf(deviceNorm, device.language.lowercase(Locale.ROOT), "en").distinct()
    }
    val languageOnly = normalized.substringBefore('-')
    return listOf(normalized, languageOnly, "en").distinct()
}

private fun extractLocalizedValue(
    element: JsonElement?,
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
