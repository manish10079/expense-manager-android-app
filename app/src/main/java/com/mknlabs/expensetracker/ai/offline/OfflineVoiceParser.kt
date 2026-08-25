package com.mknlabs.expensetracker.ai.offline

import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.models.VoiceConfidence
import com.mknlabs.expensetracker.domain.repository.CategoryPredictorRepository
import com.mknlabs.expensetracker.domain.repository.VoiceParseResult
import com.mknlabs.expensetracker.domain.repository.VoiceParserRepository
import com.mknlabs.expensetracker.utils.toMinorUnits
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Rule-based NLU parser for voice transaction input.
 *
 * Extracts: amount, category, merchant, date, and transaction type from
 * natural language voice input using regex patterns and keyword dictionaries.
 *
 * Example inputs:
 * - "Add $45 for groceries at Walmart yesterday"
 * - "Spent 500 rupees on food"
 * - "I paid 20 dollars for taxi"
 * - "Received salary 50000"
 * - "Bought coffee for 5 dollars today"
 *
 * Follows the same convention as [com.mknlabs.expensetracker.sms.SmsParser]:
 * pure Kotlin, side-effect free, fully unit-testable on JVM.
 *
 * Hilt-managed singleton: injected via [VoiceParserRepository] binding
 * in [com.mknlabs.expensetracker.di.VoiceParserModule].
 */
@Singleton
class OfflineVoiceParser @Inject constructor(
    private val categoryPredictor: CategoryPredictorRepository
) : VoiceParserRepository {

    // ──────────────────────────────────────────────────────────────────────
    // Amount extraction
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Matches currency amounts in various formats:
     * - $45, $45.50, $1,234
     * - 45 dollars, 45.50 dollars
     * - 500 rupees, Rs 500, ₹500, INR 500
     * - 500 (bare number after keywords like "for", "of", "on")
     */
    private val amountPatterns = listOf(
        // $45, $45.50, $1,234.56
        Regex("""\$\s*(\d[\d,]*(?:\.\d{1,2})?)"""),
        // 45 dollars, 45.50 dollars
        Regex("""(\d[\d,]*(?:\.\d{1,2})?)\s*dollars?""", RegexOption.IGNORE_CASE),
        // Rs 500, Rs. 500, INR 500, ₹500
        Regex("""(?:\bRs\.?|\bINR\.?|₹)\s*(\d[\d,]*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        // 500 rupees
        Regex("""(\d[\d,]*(?:\.\d{1,2})?)\s*rupees?""", RegexOption.IGNORE_CASE),
        // 500 inr
        Regex("""(\d[\d,]*(?:\.\d{1,2})?)\s*inr""", RegexOption.IGNORE_CASE),
        // Bare number after spending keywords: "for 45", "of 500", "on 20"
        Regex("""(?:for|of|on|amount|price|cost|worth)\s+(\d[\d,]*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        // Bare number after income/spending verbs: "Paid 350", "Received 5000", "Earned 300"
        Regex("""(?:spent|paid|bought|ordered|booked|charged|sent|transferred|recharged|received|earned|got|refund(?:ed)?|cashback|deposited|credited|add|record|log|track|create)\s+(\d[\d,]*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        // Verb + word + number: "Received salary 75000", "Paid bill 500"
        Regex("""(?:spent|paid|bought|received|earned|add|record|log|track|create|refund(?:ed)?|cashback)\s+\w+\s+(\d[\d,]*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
    )

    /**
     * Bare number at the end of a short utterance as a last resort.
     * e.g. "groceries 500" or "coffee 5"
     */
    private val bareNumberAtEnd = Regex("""(\d[\d,]*(?:\.\d{1,2})?)\s*$""")

    /**
     * Bare number at the beginning of a short utterance.
     * e.g. "45 groceries" or "500 rent"
     */
    private val bareNumberAtStart = Regex("""^(\d[\d,]*(?:\.\d{1,2})?)\s+""")

    // ──────────────────────────────────────────────────────────────────────
    // Transaction type detection
    // ──────────────────────────────────────────────────────────────────────

    private val incomeKeywords = Regex(
        pattern = """\b(?:received|earned|salary|income|got|refund(?:ed)?|cashback|deposit(?:ed)?|credit(?:ed)?|payment received|freelance|business income|investment return)\b""",
        option = RegexOption.IGNORE_CASE
    )

    private val expenseKeywords = Regex(
        pattern = """\b(?:spent|paid|bought|purchased|booked|ordered|charged|sent|transferred|recharged|topped up|subscription|bill|rent|fee|tip|donation)\b""",
        option = RegexOption.IGNORE_CASE
    )

    // ──────────────────────────────────────────────────────────────────────
    // Category detection — delegated to CategoryPredictor
    // ──────────────────────────────────────────────────────────────────────

    // ──────────────────────────────────────────────────────────────────────
    // Merchant extraction
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Extracts merchant name from patterns like:
     * - "at Walmart", "from Amazon", "to John"
     * - "at Starbucks for coffee"
     */
    // Words that terminate a merchant name (dates, prepositions, amounts)
    private val merchantTerminators = """(?:for|on|at|from|to|in|with|the|a|an|\$|\d|yesterday|today|tomorrow|monday|tuesday|wednesday|thursday|friday|saturday|sunday|days?\s+ago|weeks?\s+ago)"""

    private val merchantPatterns = listOf(
        Regex("""at\s+([A-Z][a-zA-Z]+(?:\s+[A-Z][a-zA-Z]+)*?)(?:\s+$merchantTerminators|\s*$)"""),
        Regex("""from\s+([A-Z][a-zA-Z]+(?:\s+[A-Z][a-zA-Z]+)*?)(?:\s+$merchantTerminators|\s*$)"""),
        Regex("""to\s+([A-Z][a-zA-Z]+(?:\s+[A-Z][a-zA-Z]+)*?)(?:\s+$merchantTerminators|\s*$)"""),
    )

    // ──────────────────────────────────────────────────────────────────────
    // Date extraction
    // ──────────────────────────────────────────────────────────────────────

    private val dayNames = mapOf(
        "monday" to Calendar.MONDAY,
        "tuesday" to Calendar.TUESDAY,
        "wednesday" to Calendar.WEDNESDAY,
        "thursday" to Calendar.THURSDAY,
        "friday" to Calendar.FRIDAY,
        "saturday" to Calendar.SATURDAY,
        "sunday" to Calendar.SUNDAY
    )

    private val dateKeywords = Regex(
        pattern = """\b(?:today|yesterday|tomorrow|last\s+(?:monday|tuesday|wednesday|thursday|friday|saturday|sunday)|(\d+)\s+days?\s+ago|(\d+)\s+weeks?\s+ago|this\s+(?:morning|afternoon|evening|week))\b""",
        option = RegexOption.IGNORE_CASE
    )

    // ──────────────────────────────────────────────────────────────────────
    // Note generation
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Patterns to strip from the input to generate a clean note.
     */
    private val stripPatterns = listOf(
        Regex("""\b(?:add|new|record|log|track|create)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:expense|transaction|payment|purchase)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:for|of|on|at|from|to|amount|price|cost|worth)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:dollars?|rupees?|inr|usd)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:today|yesterday|tomorrow|last\s+\w+|\d+\s+days?\s+ago|\d+\s+weeks?\s+ago|this\s+\w+)\b""", RegexOption.IGNORE_CASE),
        Regex("""\$[\d,]+(?:\.\d{1,2})?"""),
        Regex("""\bRs\.?\s*[\d,]+(?:\.\d{1,2})?"""),
        Regex("""\b₹[\d,]+(?:\.\d{1,2})?"""),
        Regex("""\bINR\s*[\d,]+(?:\.\d{1,2})?"""),
        Regex("""\b\d[\d,]*(?:\.\d{1,2})?\s*(?:dollars?|rupees?|inr)"""),
    )

    // ──────────────────────────────────────────────────────────────────────
    // Main parse entry point
    // ──────────────────────────────────────────────────────────────────────

    override fun parse(text: String): VoiceParseResult {
        if (text.isBlank()) {
            return VoiceParseResult.Failed(R.string.msg_voice_error_empty_input)
        }

        val trimmed = text.trim()

        // 1. Extract amount
        val amount = extractAmount(trimmed)
            ?: return VoiceParseResult.Failed(R.string.msg_voice_error_no_amount)

        // 2. Detect transaction type
        val transactionTypeId = detectTransactionType(trimmed)

        // 3. Detect category
        val categoryId = detectCategory(trimmed, transactionTypeId)

        // 4. Extract merchant
        val merchant = extractMerchant(trimmed)

        // 5. Extract date
        val createdAt = extractDate(trimmed)

        // 6. Generate note from input
        val note = generateNote(trimmed, merchant)

        // 7. Calculate confidence
        val confidence = calculateConfidence(amount, categoryId, createdAt, trimmed)

        return VoiceParseResult.Success(
            ParsedVoiceTransaction(
                amountMinor = amount.toMinorUnits(),
                transactionTypeId = transactionTypeId,
                categoryId = categoryId,
                note = note,
                merchant = merchant,
                paymentTypeId = null, // Not detected from voice
                createdAt = createdAt,
                confidence = confidence
            )
        )
    }

    // ──────────────────────────────────────────────────────────────────────
    // Private extraction helpers
    // ──────────────────────────────────────────────────────────────────────

    private fun extractAmount(text: String): Double? {
        for (pattern in amountPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val digits = match.groupValues[1].replace(",", "")
                val value = digits.toDoubleOrNull()
                if (value != null && value > 0) return value
            }
        }
        // Last resort: bare number at end
        val bareMatch = bareNumberAtEnd.find(text)
        if (bareMatch != null) {
            val digits = bareMatch.groupValues[1].replace(",", "")
            val value = digits.toDoubleOrNull()
            if (value != null && value > 0) return value
        }
        // Bare number at start (e.g. "45 groceries")
        val bareStartMatch = bareNumberAtStart.find(text)
        if (bareStartMatch != null) {
            val digits = bareStartMatch.groupValues[1].replace(",", "")
            val value = digits.toDoubleOrNull()
            if (value != null && value > 0) return value
        }
        return null
    }

    private fun detectTransactionType(text: String): Int {
        val lower = text.lowercase(Locale.ROOT)
        return when {
            incomeKeywords.containsMatchIn(lower) -> 1 // Income
            expenseKeywords.containsMatchIn(lower) -> 2 // Expense
            else -> 2 // Default to expense
        }
    }

    private fun detectCategory(text: String, transactionTypeId: Int): Int {
        return categoryPredictor.predict(text, transactionTypeId).categoryId
    }

    private fun extractMerchant(text: String): String? {
        for (pattern in merchantPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val merchant = match.groupValues[1].trim()
                if (merchant.length >= 2 && !merchant.contains(Regex("""\d"""))) {
                    return merchant
                }
            }
        }
        return null
    }

    private fun extractDate(text: String): Long {
        val lower = text.lowercase(Locale.ROOT)
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        return when {
            lower.contains("today") || lower.contains("this morning") ||
                    lower.contains("this afternoon") || lower.contains("this evening") -> {
                now
            }
            lower.contains("yesterday") -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.timeInMillis
            }
            lower.contains("tomorrow") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.timeInMillis
            }
            lower.contains("days ago") -> {
                val match = Regex("""(\d+)\s+days?\s+ago""").find(lower)
                if (match != null) {
                    val days = match.groupValues[1].toIntOrNull() ?: 1
                    calendar.add(Calendar.DAY_OF_YEAR, -days)
                    calendar.timeInMillis
                } else now
            }
            lower.contains("weeks ago") -> {
                val match = Regex("""(\d+)\s+weeks?\s+ago""").find(lower)
                if (match != null) {
                    val weeks = match.groupValues[1].toIntOrNull() ?: 1
                    calendar.add(Calendar.WEEK_OF_YEAR, -weeks)
                    calendar.timeInMillis
                } else now
            }
            else -> {
                // Check for "last <day>" patterns
                for ((dayName, dayConstant) in dayNames) {
                    if (lower.contains("last $dayName")) {
                        calendar.set(Calendar.DAY_OF_WEEK, dayConstant)
                        // If the target day is in the future, go back one more week
                        if (calendar.timeInMillis > now) {
                            calendar.add(Calendar.WEEK_OF_YEAR, -1)
                        }
                        return calendar.timeInMillis
                    }
                }
                now // Default to now if no date detected
            }
        }
    }

    private fun generateNote(text: String, merchant: String?): String {
        var note = text.trim()

        // Strip common command phrases
        for (pattern in stripPatterns) {
            note = pattern.replace(note, " ")
        }

        // Remove merchant if present (it's stored separately)
        if (merchant != null) {
            note = note.replace(merchant, "", ignoreCase = true)
        }

        // Clean up whitespace
        note = note.replace(Regex("""\s+"""), " ").trim()

        // Capitalize first letter
        if (note.isNotEmpty()) {
            note = note.replaceFirstChar { it.uppercase(Locale.ROOT) }
        }

        return note.ifEmpty { "Transaction" }
    }

    private fun calculateConfidence(
        amount: Double?,
        categoryId: Int,
        createdAt: Long,
        text: String
    ): VoiceConfidence {
        var score = 0

        // Amount detected
        if (amount != null && amount > 0) score++

        // Category is not "Other"
        if (categoryId != 23 && categoryId != 105) score++

        // Date is not "now" (meaning a specific date was mentioned)
        val now = System.currentTimeMillis()
        if (abs(createdAt - now) > 60_000) score++ // More than 1 minute from now

        // Input has reasonable length (not just a number)
        if (text.split(" ").size >= 3) score++

        return when {
            score >= 3 -> VoiceConfidence.HIGH
            score >= 2 -> VoiceConfidence.MEDIUM
            else -> VoiceConfidence.LOW
        }
    }

    companion object
}
