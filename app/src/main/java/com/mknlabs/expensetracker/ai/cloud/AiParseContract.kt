package com.mknlabs.expensetracker.ai.cloud

/**
 * Frozen JSON contract for the device-text Cloud Function parser (Phase 0).
 * The app sends names from this install; the function returns names from those lists.
 */
data class AiParseRequest(
    val text: String,
    val locale: String,
    val currency: String,
    val expenseCategories: List<String>,
    val incomeCategories: List<String>,
    val paymentMethods: List<String>,
) {
    fun toCallableMap(): Map<String, Any> = mapOf(
        "text" to text,
        "locale" to locale,
        "currency" to currency,
        "expenseCategories" to expenseCategories,
        "incomeCategories" to incomeCategories,
        "paymentMethods" to paymentMethods,
    )
}

data class AiParsedTransaction(
    val amount: Double,
    val category: String,
    val paymentMethod: String,
    val note: String,
    val typeId: Int,
)

data class AiParseResponse(
    val transactions: List<AiParsedTransaction>,
)

object AiParseContract {
    const val TYPE_INCOME = 1
    const val TYPE_EXPENSE = 2

    const val ERROR_UNAUTHENTICATED = "unauthenticated"
    const val ERROR_INVALID_JSON = "invalid-json"
    const val ERROR_UNKNOWN_CATEGORY = "unknown-category"
    const val ERROR_UNKNOWN_PAYMENT = "unknown-payment"

    val ALLOWED_TRANSACTION_KEYS = setOf(
        "amount",
        "category",
        "payment_method",
        "note",
        "type_id",
    )

    val FALLBACK_EXPENSE_CATEGORIES = listOf(
        "Food", "Travel", "Shopping", "Bills", "Health", "Entertainment", "Rent",
        "Groceries", "Education", "Subscriptions", "Insurance", "Gifts", "Personal Care",
        "Fuel", "Maintenance", "Taxes", "Pets", "Childcare", "Donations", "Miscellaneous",
        "Transport", "Other",
    )
    val FALLBACK_INCOME_CATEGORIES = listOf(
        "Salary", "Business", "Investment", "Freelance", "Other",
    )
    val FALLBACK_PAYMENT_METHODS = listOf(
        "UPI", "Cash", "Bank", "Card", "Other", "Unknown",
    )

    @Suppress("UNCHECKED_CAST")
    fun responseFromCallableData(data: Map<*, *>): AiParseResponse {
        val rawList = data["transactions"] as? List<*> ?: emptyList<Any>()
        val items = rawList.mapNotNull { row ->
            val map = row as? Map<*, *> ?: return@mapNotNull null
            val typeId = intValue(map["type_id"]) ?: return@mapNotNull null
            if (typeId != TYPE_INCOME && typeId != TYPE_EXPENSE) return@mapNotNull null
            val amount = doubleValue(map["amount"]) ?: return@mapNotNull null
            val category = map["category"] as? String ?: return@mapNotNull null
            val payment = map["payment_method"] as? String ?: return@mapNotNull null
            val note = map["note"] as? String ?: return@mapNotNull null
            AiParsedTransaction(
                amount = amount,
                category = category,
                paymentMethod = payment,
                note = note,
                typeId = typeId,
            )
        }
        return AiParseResponse(transactions = items)
    }

    fun extraKeys(row: Map<*, *>): Set<String>? {
        val keys = row.keys.mapNotNull { it as? String }.toSet()
        val extra = keys - ALLOWED_TRANSACTION_KEYS
        return extra.takeIf { it.isNotEmpty() }
    }

    private fun intValue(value: Any?): Int? = when (value) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull()
        else -> null
    }

    private fun doubleValue(value: Any?): Double? = when (value) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull()
        else -> null
    }
}
