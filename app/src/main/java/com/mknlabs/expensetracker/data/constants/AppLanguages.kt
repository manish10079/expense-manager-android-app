package com.mknlabs.expensetracker.data.constants

const val SYSTEM_LANGUAGE_CODE = "system"

data class AppLanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String
)

val supportedAppLanguages: List<AppLanguageOption> = listOf(
    AppLanguageOption(SYSTEM_LANGUAGE_CODE, "System", "Follow device"),
    AppLanguageOption("en", "English", "English"),
    AppLanguageOption("hi", "हिन्दी", "Hindi"),
    AppLanguageOption("es", "Español", "Spanish"),
    AppLanguageOption("pt-BR", "Português (Brasil)", "Portuguese"),
    AppLanguageOption("fr", "Français", "French"),
    AppLanguageOption("de", "Deutsch", "German"),
    AppLanguageOption("id", "Bahasa Indonesia", "Indonesian"),
    AppLanguageOption("ja", "日本語", "Japanese"),
    AppLanguageOption("ko", "한국어", "Korean"),
    AppLanguageOption("it", "Italiano", "Italian"),
    AppLanguageOption("ru", "Русский", "Russian"),
    AppLanguageOption("or", "ଓଡ଼ିଆ", "Odia")
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
