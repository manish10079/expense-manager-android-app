package com.mknlabs.expensetracker.data.constants

const val SYSTEM_LANGUAGE_CODE = "system"

data class AppLanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String,
    /**
     * The one letter the language picker shows in a row's leading tile, in that language's own
     * script: the letter a speaker of it would recognise the script by, not necessarily the
     * first letter of [nativeName] (Hindi is "हिन्दी" but the tile reads "अ"). Null for the
     * System row, which is a device choice rather than a language and keeps an icon.
     */
    val glyph: String? = null
)

val supportedAppLanguages: List<AppLanguageOption> = listOf(
    AppLanguageOption(SYSTEM_LANGUAGE_CODE, "System", "Follow device"),
    AppLanguageOption("en", "English", "English", glyph = "A"),
    AppLanguageOption("or", "ଓଡ଼ିଆ", "Odia", glyph = "ଅ"),
    AppLanguageOption("hi", "हिन्दी", "Hindi", glyph = "अ"),
    AppLanguageOption("kn", "ಕನ್ನಡ", "Kannada", glyph = "ಕ"),
    AppLanguageOption("ta", "தமிழ்", "Tamil", glyph = "த"),
    AppLanguageOption("bn", "বাংলা", "Bengali", glyph = "ব"),
    AppLanguageOption("mr", "मराठी", "Marathi", glyph = "म"),
    AppLanguageOption("ml", "മലയാളം", "Malayalam", glyph = "മ"),
    AppLanguageOption("te", "తెలుగు", "Telugu", glyph = "త"),
    AppLanguageOption("as", "অসমীয়া", "Assamese", glyph = "অ"),
    AppLanguageOption("pa", "ਪੰਜਾਬੀ", "Punjabi", glyph = "ਪ"),
    AppLanguageOption("gu", "ગુજરાતી", "Gujarati", glyph = "ગ"),
    AppLanguageOption("es", "Español", "Spanish", glyph = "E"),
    AppLanguageOption("pt-BR", "Português (Brasil)", "Portuguese", glyph = "P"),
    AppLanguageOption("fr", "Français", "French", glyph = "F"),
    AppLanguageOption("de", "Deutsch", "German", glyph = "D"),
    AppLanguageOption("id", "Bahasa Indonesia", "Indonesian", glyph = "B"),
    AppLanguageOption("ja", "日本語", "Japanese", glyph = "日"),
    AppLanguageOption("ko", "한국어", "Korean", glyph = "한"),
    AppLanguageOption("it", "Italiano", "Italian", glyph = "I"),
    AppLanguageOption("ru", "Русский", "Russian", glyph = "Р")
)

fun languageDisplayName(code: String): String {
    return supportedAppLanguages.firstOrNull { it.code == normalizeLanguageCode(code) }
        ?.nativeName
        ?: code
}

fun normalizeLanguageCode(code: String): String {
    val trimmed = code.trim()
    if (trimmed.isEmpty() || trimmed.equals("system", ignoreCase = true)) {
        return SYSTEM_LANGUAGE_CODE
    }
    return when (trimmed.replace('_', '-').lowercase()) {
        "in" -> "id"
        "pt", "pt-br", "pt-rbr" -> "pt-BR"
        else -> {
            val parts = trimmed.replace('_', '-').split('-')
            if (parts.size == 1) {
                parts[0].lowercase()
            } else {
                parts[0].lowercase() + "-" + parts.drop(1).joinToString("-") { it.uppercase() }
            }
        }
    }
}
