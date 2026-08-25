package com.mknlabs.expensetracker.ai.cloud

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Functions callable interface for Gemini AI voice parsing.
 *
 * Calls the `parseVoiceTransaction` Cloud Function which uses Gemini API
 * to parse complex voice inputs into structured transaction data.
 *
 * Hilt-managed singleton: injected via [GeminiVoiceParser].
 */
@Singleton
class GeminiApiService @Inject constructor(
    private val functions: FirebaseFunctions
) {
    /**
     * Calls the parseVoiceTransaction Cloud Function.
     *
     * @param text The transcribed voice text to parse.
     * @param locale The user's locale (e.g., "en-US", "hi-IN").
     * @param currency The user's currency code (e.g., "INR", "USD").
     * @return [GeminiParseResponse] with parsed transaction data.
     * @throws [GeminiApiException] if the call fails.
     */
    suspend fun parseVoiceTransaction(
        text: String,
        locale: String = "en-US",
        currency: String = "INR",
        allExpenseCategories: List<String> = emptyList(),
        allIncomeCategories: List<String> = emptyList(),
        allPaymentMethods: List<String> = emptyList(),
        topCategories: List<String> = emptyList(),
        topPaymentMethods: List<String> = emptyList()
    ): GeminiParseResponse {
        return try {
            val data = mapOf(
                "text" to text,
                "locale" to locale,
                "currency" to currency,
                "allExpenseCategories" to allExpenseCategories,
                "allIncomeCategories" to allIncomeCategories,
                "allPaymentMethods" to allPaymentMethods,
                "topCategories" to topCategories,
                "topPaymentMethods" to topPaymentMethods
            )

            val result = functions
                .getHttpsCallable("parseVoiceTransaction")
                .call(data)
                .await()

            val resultData = result.data as? Map<*, *>
                ?: throw GeminiApiException("Invalid response format")

            GeminiParseResponse(
                amount = (resultData["amount"] as? Number)?.toLong() ?: 0L,
                currency = resultData["currency"] as? String ?: "",
                transactionTypeId = (resultData["transactionTypeId"] as? Number)?.toInt() ?: 2,
                categoryId = (resultData["categoryId"] as? Number)?.toInt() ?: 23,
                note = resultData["note"] as? String ?: "",
                merchant = resultData["merchant"] as? String,
                paymentTypeId = (resultData["paymentTypeId"] as? Number)?.toInt(),
                createdAt = (resultData["createdAt"] as? Number)?.toLong()
                    ?: System.currentTimeMillis(),
                confidence = resultData["confidence"] as? String ?: "MEDIUM",
                source = resultData["source"] as? String ?: "gemini"
            )
        } catch (e: Exception) {
            throw GeminiApiException(
                message = e.message ?: "Unknown error",
                cause = e
            )
        }
    }
}

/**
 * Response from the Gemini parseVoiceTransaction Cloud Function.
 */
data class GeminiParseResponse(
    val amount: Long,
    val currency: String,
    val transactionTypeId: Int,
    val categoryId: Int,
    val note: String,
    val merchant: String?,
    val paymentTypeId: Int?,
    val createdAt: Long,
    val confidence: String,
    val source: String
)

/**
 * Exception thrown when the Gemini API call fails.
 */
class GeminiApiException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
