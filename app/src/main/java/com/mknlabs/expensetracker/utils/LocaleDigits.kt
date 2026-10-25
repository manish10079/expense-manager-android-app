package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.data.constants.normalizeLanguageCode

private const val WESTERN_DIGITS = "0123456789"
private const val DEVANAGARI_DIGITS = "०१२३४५६७८९"
private const val ODIA_DIGITS = "୦୧୨୩୪୫୬୭୮୯"
private const val KANNADA_DIGITS = "೦೧೨೩೪೫೬೭೮೯"
private const val TAMIL_DIGITS = "௦௧௨௩௪௫௬௭௮௯"
private const val BENGALI_DIGITS = "০১২৩৪৫৬৭৮৯"
private const val TELUGU_DIGITS = "౦౧౨౩౪౫౬౭౮౯"
private const val MALAYALAM_DIGITS = "൦൧൨൩൪൫൬൭൮൯"

fun nativeDigitsForLanguage(languageTag: String): String {
    val language = normalizeLanguageCode(languageTag).substringBefore('-')
    return when (language) {
        "hi", "mr" -> DEVANAGARI_DIGITS
        "or" -> ODIA_DIGITS
        "kn" -> KANNADA_DIGITS
        "ta" -> TAMIL_DIGITS
        "bn" -> BENGALI_DIGITS
        "te" -> TELUGU_DIGITS
        "as" -> BENGALI_DIGITS
        "ml" -> MALAYALAM_DIGITS
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
