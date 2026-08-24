package com.mknlabs.expensetracker.ai.offline

import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.models.VoiceConfidence
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
class OfflineVoiceParser @Inject constructor() : VoiceParserRepository {

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
    )

    /**
     * Bare number at the end of a short utterance as a last resort.
     * e.g. "groceries 500" or "coffee 5"
     */
    private val bareNumberAtEnd = Regex("""(\d[\d,]*(?:\.\d{1,2})?)\s*$""")

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
    // Category detection (keyword → category name, matching categoryMap)
    // ──────────────────────────────────────────────────────────────────────

    private data class CategoryKeyword(val keyword: String, val categoryName: String)

    private val categoryKeywords = listOf(
        // Food (id=1)
        CategoryKeyword("food", "Food"),
        CategoryKeyword("meal", "Food"),
        CategoryKeyword("lunch", "Food"),
        CategoryKeyword("dinner", "Food"),
        CategoryKeyword("breakfast", "Food"),
        CategoryKeyword("snack", "Food"),
        CategoryKeyword("restaurant", "Food"),
        CategoryKeyword("cafe", "Food"),
        CategoryKeyword("coffee", "Food"),
        CategoryKeyword("tea", "Food"),
        CategoryKeyword("pizza", "Food"),
        CategoryKeyword("burger", "Food"),
        CategoryKeyword("swiggy", "Food"),
        CategoryKeyword("zomato", "Food"),
        CategoryKeyword("doordash", "Food"),
        CategoryKeyword("uber eats", "Food"),
        CategoryKeyword("takeout", "Food"),
        CategoryKeyword("takeaway", "Food"),
        CategoryKeyword("dine", "Food"),
        CategoryKeyword("eatery", "Food"),

        // Travel (id=2)
        CategoryKeyword("travel", "Travel"),
        CategoryKeyword("flight", "Travel"),
        CategoryKeyword("hotel", "Travel"),
        CategoryKeyword("airbnb", "Travel"),
        CategoryKeyword("vacation", "Travel"),
        CategoryKeyword("trip", "Travel"),
        CategoryKeyword("holiday", "Travel"),
        CategoryKeyword("train ticket", "Travel"),
        CategoryKeyword("bus ticket", "Travel"),

        // Shopping (id=3)
        CategoryKeyword("shopping", "Shopping"),
        CategoryKeyword("clothes", "Shopping"),
        CategoryKeyword("clothing", "Shopping"),
        CategoryKeyword("shoes", "Shopping"),
        CategoryKeyword("electronics", "Shopping"),
        CategoryKeyword("gadget", "Shopping"),
        CategoryKeyword("amazon", "Shopping"),
        CategoryKeyword("flipkart", "Shopping"),
        CategoryKeyword("myntra", "Shopping"),
        CategoryKeyword("nike", "Shopping"),
        CategoryKeyword("adidas", "Shopping"),

        // Bills (id=4)
        CategoryKeyword("bill", "Bills"),
        CategoryKeyword("electricity", "Bills"),
        CategoryKeyword("electric", "Bills"),
        CategoryKeyword("water bill", "Bills"),
        CategoryKeyword("gas bill", "Bills"),
        CategoryKeyword("internet", "Bills"),
        CategoryKeyword("wifi", "Bills"),
        CategoryKeyword("broadband", "Bills"),
        CategoryKeyword("phone bill", "Bills"),
        CategoryKeyword("mobile bill", "Bills"),

        // Health (id=5)
        CategoryKeyword("health", "Health"),
        CategoryKeyword("medicine", "Health"),
        CategoryKeyword("doctor", "Health"),
        CategoryKeyword("hospital", "Health"),
        CategoryKeyword("pharmacy", "Health"),
        CategoryKeyword("medical", "Health"),
        CategoryKeyword("clinic", "Health"),
        CategoryKeyword("dental", "Health"),
        CategoryKeyword("gym", "Health"),
        CategoryKeyword("fitness", "Health"),
        CategoryKeyword("insurance premium", "Health"),

        // Entertainment (id=6)
        CategoryKeyword("entertainment", "Entertainment"),
        CategoryKeyword("movie", "Entertainment"),
        CategoryKeyword("cinema", "Entertainment"),
        CategoryKeyword("concert", "Entertainment"),
        CategoryKeyword("netflix", "Entertainment"),
        CategoryKeyword("spotify", "Entertainment"),
        CategoryKeyword("youtube", "Entertainment"),
        CategoryKeyword("game", "Entertainment"),
        CategoryKeyword("gaming", "Entertainment"),
        CategoryKeyword("book", "Entertainment"),
        CategoryKeyword("bookmyshow", "Entertainment"),

        // Rent (id=7)
        CategoryKeyword("rent", "Rent"),
        CategoryKeyword("lease", "Rent"),
        CategoryKeyword("housing", "Rent"),

        // Groceries (id=8)
        CategoryKeyword("groceries", "Groceries"),
        CategoryKeyword("grocery", "Groceries"),
        CategoryKeyword("supermarket", "Groceries"),
        CategoryKeyword("bigbasket", "Groceries"),
        CategoryKeyword("blinkit", "Groceries"),
        CategoryKeyword("zepto", "Groceries"),
        CategoryKeyword("instamart", "Groceries"),
        CategoryKeyword("dmart", "Groceries"),
        CategoryKeyword("d mart", "Groceries"),
        CategoryKeyword("walmart", "Groceries"),
        CategoryKeyword("target", "Groceries"),
        CategoryKeyword("costco", "Groceries"),

        // Education (id=9)
        CategoryKeyword("education", "Education"),
        CategoryKeyword("school", "Education"),
        CategoryKeyword("college", "Education"),
        CategoryKeyword("university", "Education"),
        CategoryKeyword("course", "Education"),
        CategoryKeyword("tuition", "Education"),
        CategoryKeyword("udemy", "Education"),
        CategoryKeyword("coursera", "Education"),

        // Subscriptions (id=10)
        CategoryKeyword("subscription", "Subscriptions"),
        CategoryKeyword("subscribe", "Subscriptions"),
        CategoryKeyword("membership", "Subscriptions"),
        CategoryKeyword("premium plan", "Subscriptions"),

        // Insurance (id=11)
        CategoryKeyword("insurance", "Insurance"),

        // Gifts (id=12)
        CategoryKeyword("gift", "Gifts"),
        CategoryKeyword("present", "Gifts"),
        CategoryKeyword("birthday", "Gifts"),
        CategoryKeyword("anniversary", "Gifts"),

        // Personal Care (id=13)
        CategoryKeyword("personal care", "Personal Care"),
        CategoryKeyword("haircut", "Personal Care"),
        CategoryKeyword("salon", "Personal Care"),
        CategoryKeyword("spa", "Personal Care"),
        CategoryKeyword("beauty", "Personal Care"),
        CategoryKeyword("cosmetics", "Personal Care"),

        // Fuel (id=14)
        CategoryKeyword("fuel", "Fuel"),
        CategoryKeyword("petrol", "Fuel"),
        CategoryKeyword("diesel", "Fuel"),
        CategoryKeyword("gas station", "Fuel"),
        CategoryKeyword("filling station", "Fuel"),
        CategoryKeyword("petrol pump", "Fuel"),

        // Maintenance (id=15)
        CategoryKeyword("maintenance", "Maintenance"),
        CategoryKeyword("repair", "Maintenance"),
        CategoryKeyword("service", "Maintenance"),
        CategoryKeyword("plumber", "Maintenance"),
        CategoryKeyword("electrician", "Maintenance"),

        // Transport (id=22)
        CategoryKeyword("taxi", "Transport"),
        CategoryKeyword("uber", "Transport"),
        CategoryKeyword("ola", "Transport"),
        CategoryKeyword("rapido", "Transport"),
        CategoryKeyword("auto", "Transport"),
        CategoryKeyword("bus", "Transport"),
        CategoryKeyword("metro", "Transport"),
        CategoryKeyword("parking", "Transport"),
        CategoryKeyword("toll", "Transport"),
        CategoryKeyword("cab", "Transport"),
        CategoryKeyword("ride", "Transport"),

        // Donations (id=19)
        CategoryKeyword("donation", "Donations"),
        CategoryKeyword("donate", "Donations"),
        CategoryKeyword("charity", "Donations"),

        // Pets (id=17)
        CategoryKeyword("pet", "Pets"),
        CategoryKeyword("dog", "Pets"),
        CategoryKeyword("cat", "Pets"),
        CategoryKeyword("vet", "Pets"),
        CategoryKeyword("pet food", "Pets"),

        // Childcare (id=18)
        CategoryKeyword("childcare", "Childcare"),
        CategoryKeyword("daycare", "Childcare"),
        CategoryKeyword("kids", "Childcare"),
        CategoryKeyword("baby", "Childcare"),

        // Taxes (id=16)
        CategoryKeyword("tax", "Taxes"),
        CategoryKeyword("taxes", "Taxes"),
    )

    // ──────────────────────────────────────────────────────────────────────
    // Merchant extraction
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Extracts merchant name from patterns like:
     * - "at Walmart", "from Amazon", "to John"
     * - "at Starbucks for coffee"
     */
    private val merchantPatterns = listOf(
        Regex("""at\s+([A-Z][a-zA-Z\s]+?)(?:\s+for\b|\s+on\b|\s+\d|\s*$)"""),
        Regex("""from\s+([A-Z][a-zA-Z\s]+?)(?:\s+for\b|\s+on\b|\s+\d|\s*$)"""),
        Regex("""to\s+([A-Z][a-zA-Z\s]+?)(?:\s+for\b|\s+on\b|\s+\d|\s*$)"""),
        Regex("""at\s+([A-Z][a-zA-Z\s]+?)(?:\s*$)"""),
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
        val lower = text.lowercase(Locale.ROOT)

        for ((keyword, categoryName) in categoryKeywords) {
            if (lower.contains(keyword)) {
                val categoryId = resolveCategoryId(categoryName, transactionTypeId)
                if (categoryId != null) return categoryId
            }
        }

        // Fallback to "Other" for the transaction type
        return resolveCategoryId("Other", transactionTypeId) ?: FALLBACK_CATEGORY_ID
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

    private fun resolveCategoryId(categoryName: String, transactionTypeId: Int): Int? {
        return categoryMap.entries.firstOrNull {
            it.value.name.equals(categoryName, ignoreCase = true) &&
                    it.value.transactionTypeId == transactionTypeId
        }?.key
    }

    companion object {
        private const val FALLBACK_CATEGORY_ID = 23 // "Other" expense
    }
}
