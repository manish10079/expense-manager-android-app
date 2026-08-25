package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction

/**
 * Type of voice parser to use.
 */
enum class VoiceParserType {
    /** Offline, rule-based parser (free, unlimited). */
    OFFLINE,
    /** Cloud-powered Gemini parser (daily-limited). */
    GEMINI
}

/**
 * Abstraction for voice-to-transaction parsing.
 *
 * Implementations:
 * - OfflineVoiceParser (free, on-device, rule-based)
 * - GeminiVoiceParser (cloud, daily-limited)
 */
interface VoiceParserRepository {

    /**
     * Parses [text] from voice input into a [ParsedVoiceTransaction].
     *
     * @param text The transcribed text from speech recognition.
     * @return [VoiceParseResult.Success] with the parsed transaction, or
     *         [VoiceParseResult.Failed] with a string resource ID for the error message.
     */
    fun parse(text: String): VoiceParseResult
}

/**
 * Result of voice parsing — either a successfully parsed transaction or a failure.
 *
 * Error messages use @StringRes Int IDs instead of hardcoded strings,
 * per GEMINI.md i18n requirements.
 */
sealed class VoiceParseResult {
    data class Success(
        val transaction: ParsedVoiceTransaction,
        val parserType: VoiceParserType = VoiceParserType.OFFLINE
    ) : VoiceParseResult()
    data class Failed(val errorMessageResId: Int) : VoiceParseResult()
}
