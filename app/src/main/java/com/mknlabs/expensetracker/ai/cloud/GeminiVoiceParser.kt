package com.mknlabs.expensetracker.ai.cloud

import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.models.VoiceConfidence
import com.mknlabs.expensetracker.domain.repository.VoiceParseResult
import com.mknlabs.expensetracker.domain.repository.VoiceParserRepository
import com.mknlabs.expensetracker.domain.repository.VoiceParserType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud-powered voice parser using Google Gemini AI via Firebase Functions.
 *
 * For complex/ambiguous voice inputs that the offline parser can't handle
 * well (LOW confidence). Falls back gracefully on network errors.
 *
 * Usage is rate-limited to [com.mknlabs.expensetracker.ai.AiUsageTracker.dailyLimit]
 * parses per day for free users. Pro users have unlimited access.
 *
 * Hilt-managed singleton: bound in [com.mknlabs.expensetracker.di.VoiceParserModule]
 * when the user has remaining AI parses.
 */
@Singleton
class GeminiVoiceParser @Inject constructor(
    private val apiService: GeminiApiService
) : VoiceParserRepository {

    /**
     * Parses [text] using Gemini AI via Firebase Cloud Function.
     *
     * @param text The transcribed text from speech recognition.
     * @return [VoiceParseResult.Success] with the parsed transaction, or
     *         [VoiceParseResult.Failed] with a string resource ID for the error message.
     */
    override fun parse(text: String): VoiceParseResult {
        // This is called synchronously from VoiceAddViewModel.
        // We use runBlocking to bridge coroutine suspension into sync context.
        // The actual network call happens in the ViewModel's coroutine scope.
        return kotlinx.coroutines.runBlocking {
            try {
                val response = apiService.parseVoiceTransaction(text)

                val confidence = when (response.confidence.uppercase()) {
                    "HIGH" -> VoiceConfidence.HIGH
                    "MEDIUM" -> VoiceConfidence.MEDIUM
                    "LOW" -> VoiceConfidence.LOW
                    else -> VoiceConfidence.MEDIUM
                }

                VoiceParseResult.Success(
                    parserType = VoiceParserType.GEMINI,
                    transaction = ParsedVoiceTransaction(
                        amountMinor = response.amount,
                        transactionTypeId = response.transactionTypeId,
                        categoryId = response.categoryId,
                        note = response.note.ifBlank { text.trim() },
                        merchant = response.merchant,
                        paymentTypeId = response.paymentTypeId,
                        createdAt = response.createdAt,
                        confidence = confidence
                    )
                )
            } catch (e: GeminiApiException) {
                VoiceParseResult.Failed(R.string.msg_voice_error_network)
            } catch (e: Exception) {
                VoiceParseResult.Failed(R.string.msg_voice_error_network)
            }
        }
    }
}
