package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction

/**
 * Abstraction for voice-to-transaction parsing.
 *
 * Implementations:
 * - OfflineVoiceParser (free, on-device, rule-based)
 * - Future: GeminiVoiceParser (Pro, cloud-powered, daily-limited)
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
    data class Success(val transaction: ParsedVoiceTransaction) : VoiceParseResult()
    data class Failed(val errorMessageResId: Int) : VoiceParseResult()
}
