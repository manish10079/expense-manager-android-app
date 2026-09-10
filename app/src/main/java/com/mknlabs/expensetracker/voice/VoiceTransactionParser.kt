package com.mknlabs.expensetracker.voice

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses natural language voice input into structured transaction data.
 * Supports phrases like:
 * - "Spent 250 on food"
 * - "Paid 600 to Rahul"
 * - "Received salary 30000"
 * - "Petrol 450"
 */
@Singleton
class VoiceTransactionParser @Inject constructor() {

    data class ParsedTransaction(
        val amount: Double,
        val isExpense: Boolean,
        val categoryName: String,
        val categoryId: Int,
        val note: String,
        val payee: String? = null
    )

    // Expense keywords
    private val expenseKeywords = listOf(
        "spent", "paid", "bought", "purchase", "purchase", "expense",
        "cost", "bill", "payment", "charge", "fee", "debit"
    )

    // Income keywords
    private val incomeKeywords = listOf(
        "received", "earned", "salary", "income", "credit", "refund",
        "cashback", "reward", "bonus", "gift"
    )

    // Category mapping
    private val categoryMap = mapOf(
        // Food
        "food" to 1, "meal" to 1, "lunch" to 1, "dinner" to 1, "breakfast" to 1,
        "restaurant" to 1, "cafe" to 1, "coffee" to 1, "tea" to 1, "snack" to 1,
        "swiggy" to 1, "zomato" to 1, "dominos" to 1, "pizza" to 1, "burger" to 1,
        "grocery" to 1, "groceries" to 1, "vegetables" to 1, "fruits" to 1,
        "bakery" to 1, "cake" to 1, "ice cream" to 1, "juice" to 1,
        // Transport
        "transport" to 2, "uber" to 2, "ola" to 2, "taxi" to 2, "cab" to 2,
        "bus" to 2, "metro" to 2, "train" to 2, "flight" to 2, "auto" to 2,
        "ride" to 2, "travel" to 2, "commute" to 2,
        // Shopping
        "shopping" to 3, "clothes" to 3, "clothing" to 3, "shoes" to 3,
        "amazon" to 3, "flipkart" to 3, "myntra" to 3, "ajio" to 3,
        "electronics" to 3, "gadget" to 3, "phone" to 3, "laptop" to 3,
        // Bills
        "bill" to 4, "bills" to 4, "electricity" to 4, "water" to 4,
        "gas" to 4, "internet" to 4, "wifi" to 4, "recharge" to 4,
        "phone bill" to 4, "mobile" to 4, "broadband" to 4,
        // Entertainment
        "entertainment" to 5, "movie" to 5, "cinema" to 5, "concert" to 5,
        "game" to 5, "gaming" to 5, "netflix" to 5, "hotstar" to 5,
        "spotify" to 5, "music" to 5, "book" to 5, "magazine" to 5,
        // Health
        "health" to 6, "medical" to 6, "medicine" to 6, "doctor" to 6,
        "hospital" to 6, "pharmacy" to 6, "clinic" to 6, "gym" to 6,
        "fitness" to 6, "insurance" to 6,
        // Education
        "education" to 7, "school" to 7, "college" to 7, "course" to 7,
        "book" to 7, "tuition" to 7, "fees" to 7, "exam" to 7,
        // Fuel
        "fuel" to 8, "petrol" to 8, "diesel" to 8, "gas" to 8,
        "汽油" to 8, "pump" to 8, "shell" to 8, "bp" to 8,
        // Salary (income)
        "salary" to 9, "wage" to 9, "income" to 9
    )

    /**
     * Parse voice input text into a ParsedTransaction.
     */
    fun parse(text: String): ParsedTransaction? {
        if (text.isBlank()) return null

        val normalizedText = text.lowercase().trim()

        // Extract amount
        val amount = extractAmount(normalizedText) ?: return null

        // Determine if expense or income
        val isExpense = determineIfExpense(normalizedText)

        // Extract category
        val (categoryName, categoryId) = extractCategory(normalizedText)

        // Extract payee
        val payee = extractPayee(normalizedText)

        // Build note
        val note = buildNote(normalizedText, categoryName, payee)

        return ParsedTransaction(
            amount = amount,
            isExpense = isExpense,
            categoryName = categoryName,
            categoryId = categoryId,
            note = note,
            payee = payee
        )
    }

    private fun extractAmount(text: String): Double? {
        // Match patterns like "250", "1,250", "1250.50"
        val patterns = listOf(
            Regex("""(\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?)"""),
            Regex("""(\d+(?:\.\d{1,2})?)""")
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val amountStr = match.value.replace(",", "")
                return amountStr.toDoubleOrNull()
            }
        }
        return null
    }

    private fun determineIfExpense(text: String): Boolean {
        // Check for income keywords first
        for (keyword in incomeKeywords) {
            if (text.contains(keyword)) {
                return false
            }
        }
        // Default to expense
        return true
    }

    private fun extractCategory(text: String): Pair<String, Int> {
        // Check for category keywords
        for ((keyword, categoryId) in categoryMap) {
            if (text.contains(keyword)) {
                val categoryName = when (categoryId) {
                    1 -> "Food"
                    2 -> "Transport"
                    3 -> "Shopping"
                    4 -> "Bills"
                    5 -> "Entertainment"
                    6 -> "Health"
                    7 -> "Education"
                    8 -> "Fuel"
                    9 -> "Salary"
                    else -> "Other"
                }
                return Pair(categoryName, categoryId)
            }
        }
        // Default to Others
        return Pair("Others", 10)
    }

    private fun extractPayee(text: String): String? {
        // Look for "to [name]" or "from [name]" patterns
        val toPattern = Regex("""to\s+(\w+)""", RegexOption.IGNORE_CASE)
        val fromPattern = Regex("""from\s+(\w+)""", RegexOption.IGNORE_CASE)

        toPattern.find(text)?.let { return it.groupValues[1] }
        fromPattern.find(text)?.let { return it.groupValues[1] }

        return null
    }

    private fun buildNote(text: String, categoryName: String, payee: String?): String {
        val parts = mutableListOf<String>()

        if (payee != null) {
            parts.add("To $payee")
        }

        // Add original text as note if it's short enough
        if (text.length <= 50) {
            parts.add(text)
        }

        return parts.joinToString(" - ").ifBlank { categoryName }
    }
}
