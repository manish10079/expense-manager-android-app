package com.mknlabs.expensetracker.sms

import com.mknlabs.expensetracker.data.constants.categoryMap
import java.util.Locale

/**
 * Rule-based category detection (GEMINI.md: rule table maps merchants/keywords
 * to EXISTING category IDs from [categoryMap]; never invents new categories).
 *
 * Designed to be extensible for the future learning system: a merchant→category
 * override map (DataStore-backed) can be consulted before these static rules.
 */
object SmsCategoryDetector {

    /** Result of category detection: resolved category ID, confidence, and the
     *  matched merchant name (if a merchant rule matched). */
    data class CategoryDetection(
        val categoryId: Int,
        val confidence: SmsConfidence,
        val merchant: String? = null
    )

    private data class MerchantRule(
        val keyword: String,
        val categoryName: String,
        val confidence: SmsConfidence
    )

    private data class KeywordRule(
        val keyword: String,
        val categoryName: String,
        val confidence: SmsConfidence
    )

    private val merchantRules = listOf(
        // Food
        MerchantRule("swiggy", "Food", SmsConfidence.HIGH),
        MerchantRule("zomato", "Food", SmsConfidence.HIGH),
        // Fuel
        MerchantRule("indianoil", "Fuel", SmsConfidence.HIGH),
        MerchantRule("hpcl", "Fuel", SmsConfidence.HIGH),
        MerchantRule("bpcl", "Fuel", SmsConfidence.HIGH),
        MerchantRule("iocl", "Fuel", SmsConfidence.HIGH),
        // Shopping
        MerchantRule("amazon", "Shopping", SmsConfidence.MEDIUM),
        MerchantRule("flipkart", "Shopping", SmsConfidence.MEDIUM),
        MerchantRule("myntra", "Shopping", SmsConfidence.MEDIUM),
        // Transport
        MerchantRule("uber", "Transport", SmsConfidence.HIGH),
        MerchantRule("rapido", "Transport", SmsConfidence.HIGH),
        MerchantRule("ola", "Transport", SmsConfidence.HIGH),
        // Entertainment
        MerchantRule("netflix", "Entertainment", SmsConfidence.HIGH),
        MerchantRule("spotify", "Entertainment", SmsConfidence.HIGH),
        MerchantRule("bookmyshow", "Entertainment", SmsConfidence.HIGH),
        // Groceries
        MerchantRule("bigbasket", "Groceries", SmsConfidence.HIGH),
        MerchantRule("blinkit", "Groceries", SmsConfidence.HIGH),
        MerchantRule("zepto", "Groceries", SmsConfidence.HIGH),
        MerchantRule("d mart", "Groceries", SmsConfidence.HIGH),
        MerchantRule("dmart", "Groceries", SmsConfidence.HIGH),
        // Bills
        MerchantRule("electricity", "Bills", SmsConfidence.MEDIUM),
        MerchantRule("water bill", "Bills", SmsConfidence.MEDIUM),
        MerchantRule("gas bill", "Bills", SmsConfidence.MEDIUM)
    )

    private val keywordRules = listOf(
        KeywordRule("salary", "Salary", SmsConfidence.HIGH),
        KeywordRule("payroll", "Salary", SmsConfidence.HIGH),
        KeywordRule("cashback", "Other", SmsConfidence.MEDIUM),
        KeywordRule("refund", "Other", SmsConfidence.MEDIUM),
        KeywordRule("reversal", "Other", SmsConfidence.MEDIUM),
        KeywordRule("interest", "Investment", SmsConfidence.MEDIUM)
    )

    /**
     * Detects the best category for [body] given its [transactionTypeId].
     * Falls back to "Other" for the matching transaction type (23 expense / 105 income).
     *
     * [userOverrides] (the learning system, plan §10) is consulted FIRST: a
     * merchant the user explicitly remapped always wins over static rules, as
     * long as its category exists for [transactionTypeId]. Merchant keys are
     * expected pre-normalized (lowercase) by [SmsLearningStore]; matching against
     * the lowercased body is case-insensitive either way.
     */
    fun detect(
        body: String,
        transactionTypeId: Int,
        userOverrides: Map<String, Int> = emptyMap()
    ): CategoryDetection {
        // Locale.ROOT keeps keyword matching deterministic (e.g. 'I' must not
        // become Turkish dotless 'ı', which would break the IOCL fuel rule).
        val text = body.lowercase(Locale.ROOT)

        for ((merchant, categoryId) in userOverrides) {
            if (merchant.isBlank() || !text.contains(merchant)) continue
            val category = categoryMap[categoryId]
            if (category != null && category.transactionTypeId == transactionTypeId) {
                return CategoryDetection(
                    categoryId = categoryId,
                    confidence = SmsConfidence.HIGH,
                    merchant = merchant.replaceFirstChar { it.uppercase(Locale.ROOT) }
                )
            }
        }

        merchantRules.firstOrNull { text.contains(it.keyword) }?.let { rule ->
            val categoryId = resolveCategoryId(rule.categoryName, transactionTypeId)
            if (categoryId != null) {
                return CategoryDetection(
                    categoryId = categoryId,
                    confidence = rule.confidence,
                    merchant = rule.keyword.replaceFirstChar { it.uppercase(Locale.ROOT) }
                )
            }
        }

        keywordRules.firstOrNull { text.contains(it.keyword) }?.let { rule ->
            resolveCategoryId(rule.categoryName, transactionTypeId)?.let { categoryId ->
                return CategoryDetection(categoryId = categoryId, confidence = rule.confidence)
            }
        }

        // No reliable signal → "Other" for the transaction type, low confidence.
        return CategoryDetection(
            categoryId = resolveCategoryId("Other", transactionTypeId) ?: FALLBACK_CATEGORY_ID,
            confidence = SmsConfidence.LOW
        )
    }

    private fun resolveCategoryId(categoryName: String, transactionTypeId: Int): Int? {
        return categoryMap.entries.firstOrNull {
            it.value.name.equals(categoryName, ignoreCase = true) &&
                it.value.transactionTypeId == transactionTypeId
        }?.key
    }

    private const val FALLBACK_CATEGORY_ID = 23
}
