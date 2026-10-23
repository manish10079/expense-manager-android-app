package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.data.constants.normalizeLanguageCode

private const val WESTERN_DIGITS = "0123456789"
private const val DEVANAGARI_DIGITS = "०१२३४५६७८९"
private const val ODIA_DIGITS = "୦୧୨୩୪୫୬୭୮୯"

fun nativeDigitsForLanguage(languageTag: String): String {
    val language = normalizeLanguageCode(languageTag).substringBefore('-')
    return when (language) {
        "hi" -> DEVANAGARI_DIGITS
        "or" -> ODIA_DIGITS
        else -> WESTERN_DIGITS
    }
}

fun usesNativeDigits(languageTag: String): Boolean =
    nativeDigitsForLanguage(languageTag) != WESTERN_DIGITS

fun localizeWesternDigits(text: String, languageTag: String): String {
    val digits = nativeDigitsForLanguage(languageTag)
    if (digits == WESTERN_DIGITS) return text
    return buildString(text.length) {
        for (char in text) {
            append(if (char in '0'..'9') digits[char - '0'] else char)
        }
    }
}
