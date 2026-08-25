package com.mknlabs.expensetracker.ai.cloud

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerationConfig
import com.google.firebase.ai.type.GenerativeBackend
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.models.VoiceConfidence
import com.mknlabs.expensetracker.domain.repository.VoiceParseResult
import com.mknlabs.expensetracker.domain.repository.VoiceParserRepository
import com.mknlabs.expensetracker.domain.repository.VoiceParserType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini voice parser using Firebase AI Logic (direct from app).
 *
 * Uses Firebase's built-in Gemini integration — no Cloud Function needed.
 * The API key is managed by Firebase project, not by the app.
 *
 * Flow:
 * 1. Build personalized prompt with user context from Room DB
 * 2. Call Gemini via Firebase AI Logic
 * 3. Parse JSON response into ParsedVoiceTransaction
 */
@Singleton
class FirebaseGeminiParser @Inject constructor(
    private val userContextProvider: UserContextProvider
) : VoiceParserRepository {

    private val generativeModel: GenerativeModel by lazy {
        val config = GenerationConfig.Builder()
            .setTemperature(0.3f)
            .setMaxOutputTokens(2048)
            .build()

        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-2.0-flash",
                generationConfig = config
            )
    }

    override fun parse(text: String): VoiceParseResult {
        return runBlocking {
            parseAsync(text)
        }
    }

    private suspend fun parseAsync(text: String): VoiceParseResult {
        return withContext(Dispatchers.IO) {
            try {
                val ctx = userContextProvider.getUserContext()
                val prompt = buildPrompt(text, ctx)
                val response: GenerateContentResponse = generativeModel.generateContent(prompt)
                val responseText = response.text ?: return@withContext VoiceParseResult.Failed(
                    R.string.msg_voice_error_network
                )

                val parsed = parseJsonResponse(responseText)
                if (parsed == null) {
                    return@withContext VoiceParseResult.Failed(R.string.msg_voice_error_network)
                }

                val confidence = when (parsed.optString("confidence", "MEDIUM").uppercase()) {
                    "HIGH" -> VoiceConfidence.HIGH
                    "LOW" -> VoiceConfidence.LOW
                    else -> VoiceConfidence.MEDIUM
                }

                VoiceParseResult.Success(
                    parserType = VoiceParserType.GEMINI,
                    transaction = ParsedVoiceTransaction(
                        amountMinor = parsed.optLong("amount", 0),
                        transactionTypeId = parsed.optInt("transactionTypeId", 2),
                        categoryId = parsed.optInt("categoryId", 23),
                        note = parsed.optString("note", text.trim()),
                        merchant = parsed.optString("merchant", "").ifBlank { null },
                        paymentTypeId = if (parsed.has("paymentTypeId")) parsed.optInt("paymentTypeId") else null,
                        createdAt = parsed.optLong("createdAt", System.currentTimeMillis()),
                        confidence = confidence
                    )
                )
            } catch (e: Exception) {
                VoiceParseResult.Failed(R.string.msg_voice_error_network)
            }
        }
    }

    private fun parseJsonResponse(text: String): JSONObject? {
        return try {
            val jsonMatch = Regex("\\{[\\s\\S]*\\}").find(text) ?: return null
            JSONObject(jsonMatch.value)
        } catch (e: Exception) {
            null
        }
    }

    private fun buildPrompt(text: String, ctx: UserAiContext): String {
        val expenseCats = ctx.allExpenseCategories.ifEmpty { listOf("Food","Travel","Shopping","Bills","Health","Entertainment","Rent","Groceries","Education","Subscriptions","Insurance","Gifts","Personal Care","Fuel","Maintenance","Taxes","Pets","Childcare","Donations","Miscellaneous","Transport","Other") }
        val incomeCats = ctx.allIncomeCategories.ifEmpty { listOf("Salary","Business","Investment","Freelance","Other Income") }
        val paymentMethods = ctx.allPaymentMethods.ifEmpty { listOf("UPI","Cash","Bank","Card","Other") }
        val topCats = ctx.topCategories.ifEmpty { listOf("Food","Transport","Shopping") }
        val topPayments = ctx.topPaymentMethods.ifEmpty { listOf("UPI","Card","Cash") }

        return """You are a financial transaction parser for an expense tracker app.

USER CONTEXT:
- Currency: ${ctx.currency}
- Locale: ${ctx.locale}
- Top3 categories: ${topCats.joinToString(", ")}
- Top3 payment methods: ${topPayments.joinToString(", ")}

EXPENSE CATEGORIES (use ONLY these exact names):
${expenseCats.joinToString(", ")}

INCOME CATEGORIES (use ONLY these exact names):
${incomeCats.joinToString(", ")}

PAYMENT METHODS (use ONLY these exact names):
${paymentMethods.joinToString(", ")}

PARSE: "$text"

Return ONLY a JSON object:
{
  "amount": <smallest currency unit, e.g. cents/paise>,
  "currency": "${ctx.currency}",
  "transactionTypeId": <1 for Income, 2 for Expense>,
  "categoryId": <numeric ID from categoryMap>,
  "note": "<clean description>",
  "merchant": "<store name or null>",
  "paymentTypeId": <1=UPI,2=Cash,3=Bank,4=Card,5=Other or null>,
  "createdAt": <timestamp ms or null for today>,
  "confidence": "HIGH" or "MEDIUM" or "LOW"
}

RULES:
- Use exact category/payment names from lists above
- Default to expense (type 2) if unclear
- Convert amounts to smallest unit (500 rupees → 50000)
- Return ONLY the JSON, no explanation"""
    }
}
