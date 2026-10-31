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
    val skipQuota: Boolean = false,
) {
    fun toCallableMap(): Map<String, Any> = buildMap {
        put("text", text)
        put("locale", locale)
        put("currency", currency)
        put("expenseCategories", expenseCategories)
        put("incomeCategories", incomeCategories)
        put("paymentMethods", paymentMethods)
        if (skipQuota) put("skipQuota", true)
    }
}

data class AiParsedTransaction(
    val amount: Double,
    val category: String,
    val paymentMethod: String,
    val note: String,
    val typeId: Int,
    val createdAt: Long? = null,
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
        "created_at",
        "time_format",
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
            val createdAt = createdAtToEpochMillis(map["created_at"], map["time_format"])
            AiParsedTransaction(
                amount = amount,
                category = category,
                paymentMethod = payment,
                note = note,
                typeId = typeId,
                createdAt = createdAt,
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

    fun createdAtToEpochMillis(createdAt: Any?, timeFormat: Any?): Long? {
        if (createdAt == null) return null
        if (createdAt is Number) {
            return createdAt.toLong().takeIf { it in 1L..4_102_444_800_000L }
        }
        val raw = (createdAt as? String)?.trim() ?: return null
        val match = CREATED_AT_REGEX.matchEntire(raw) ?: return null
        val year = match.groupValues[1].toInt()
        val month = match.groupValues[2].toInt()
        val day = match.groupValues[3].toInt()
        val hour = match.groupValues[4].toInt()
        val minute = match.groupValues[5].toInt()
        if (intValue(timeFormat) != 12 && intValue(timeFormat) != 24 && timeFormat != null) {
            return null
        }
        if (year !in 1970..2100 || month !in 1..12 || day !in 1..31) return null
        if (hour !in 0..23 || minute !in 0..59) return null
        val cal = java.util.Calendar.getInstance()
        cal.isLenient = false
        return runCatching {
            cal.set(year, month - 1, day, hour, minute, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.getOrNull()
    }

    private val CREATED_AT_REGEX = Regex("""(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2})""")
}
