package com.mknlabs.expensetracker.ai

import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.domain.models.CategoryPrediction
import com.mknlabs.expensetracker.domain.models.PredictionConfidence
import com.mknlabs.expensetracker.domain.models.PredictionSource
import com.mknlabs.expensetracker.domain.repository.CategoryPredictorRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rule-based category predictor for any text input (voice, SMS, manual).
 *
 * Consolidates keyword/merchant detection from [OfflineVoiceParser] and
 * [com.mknlabs.expensetracker.sms.SmsCategoryDetector] into a single
 * injectable predictor. Follows the GEMINI.md rule: maps to EXISTING
 * category IDs from [categoryMap]; never invents new categories.
 *
 * Consults user-learned merchant overrides (via [SmsLearningStore]) BEFORE
 * static rules, so the next identical input is predicted correctly after
 * a user correction.
 *
 * Hilt-managed singleton: injected via [CategoryPredictorRepository] binding
 * in [com.mknlabs.expensetracker.di.CategoryPredictorModule].
 */
@Singleton
class CategoryPredictor @Inject constructor() : CategoryPredictorRepository {

    // ──────────────────────────────────────────────────────────────────────
    // Merchant rules — exact merchant name matches (highest priority after
    // user overrides). Order matters: first match wins.
    // ──────────────────────────────────────────────────────────────────────

    private data class MerchantRule(
        val keyword: String,
        val categoryName: String,
        val confidence: PredictionConfidence
    )

    private val merchantRules = listOf(
        // Food
        MerchantRule("swiggy", "Food", PredictionConfidence.HIGH),
        MerchantRule("zomato", "Food", PredictionConfidence.HIGH),
        MerchantRule("doordash", "Food", PredictionConfidence.HIGH),
        MerchantRule("ubereats", "Food", PredictionConfidence.HIGH),
        MerchantRule("uber eats", "Food", PredictionConfidence.HIGH),

        // Fuel
        MerchantRule("indianoil", "Fuel", PredictionConfidence.HIGH),
        MerchantRule("hpcl", "Fuel", PredictionConfidence.HIGH),
        MerchantRule("bpcl", "Fuel", PredictionConfidence.HIGH),
        MerchantRule("iocl", "Fuel", PredictionConfidence.HIGH),

        // Shopping
        MerchantRule("amazon", "Shopping", PredictionConfidence.MEDIUM),
        MerchantRule("flipkart", "Shopping", PredictionConfidence.MEDIUM),
        MerchantRule("myntra", "Shopping", PredictionConfidence.MEDIUM),
        MerchantRule("nike", "Shopping", PredictionConfidence.MEDIUM),
        MerchantRule("adidas", "Shopping", PredictionConfidence.MEDIUM),

        // Transport
        MerchantRule("uber", "Transport", PredictionConfidence.HIGH),
        MerchantRule("rapido", "Transport", PredictionConfidence.HIGH),
        MerchantRule("ola", "Transport", PredictionConfidence.HIGH),

        // Entertainment
        MerchantRule("netflix", "Entertainment", PredictionConfidence.HIGH),
        MerchantRule("spotify", "Entertainment", PredictionConfidence.HIGH),
        MerchantRule("bookmyshow", "Entertainment", PredictionConfidence.HIGH),

        // Groceries
        MerchantRule("bigbasket", "Groceries", PredictionConfidence.HIGH),
        MerchantRule("blinkit", "Groceries", PredictionConfidence.HIGH),
        MerchantRule("zepto", "Groceries", PredictionConfidence.HIGH),
        MerchantRule("instamart", "Groceries", PredictionConfidence.HIGH),
        MerchantRule("d mart", "Groceries", PredictionConfidence.HIGH),
        MerchantRule("dmart", "Groceries", PredictionConfidence.HIGH),
        MerchantRule("walmart", "Groceries", PredictionConfidence.HIGH),
        MerchantRule("target", "Groceries", PredictionConfidence.MEDIUM),
        MerchantRule("costco", "Groceries", PredictionConfidence.HIGH),

        // Bills
        MerchantRule("electricity", "Bills", PredictionConfidence.MEDIUM),
        MerchantRule("water bill", "Bills", PredictionConfidence.MEDIUM),
        MerchantRule("gas bill", "Bills", PredictionConfidence.MEDIUM)
    )

    // ──────────────────────────────────────────────────────────────────────
    // Keyword rules — broader text matches (lower priority than merchants).
    // Multi-word keywords listed before single-word to avoid false positives.
    // ──────────────────────────────────────────────────────────────────────

    private data class KeywordRule(
        val keyword: String,
        val categoryName: String,
        val confidence: PredictionConfidence
    )

    private val keywordRules = listOf(
        // ── Food ──
        KeywordRule("pet food", "Pets", PredictionConfidence.HIGH),
        KeywordRule("food", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("meal", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("lunch", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("dinner", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("breakfast", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("snack", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("restaurant", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("cafe", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("coffee", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("tea", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("pizza", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("burger", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("takeout", "Food", PredictionConfidence.MEDIUM),
        KeywordRule("takeaway", "Food", PredictionConfidence.MEDIUM),

        // ── Travel ──
        KeywordRule("train ticket", "Travel", PredictionConfidence.HIGH),
        KeywordRule("bus ticket", "Travel", PredictionConfidence.HIGH),
        KeywordRule("travel", "Travel", PredictionConfidence.MEDIUM),
        KeywordRule("flight", "Travel", PredictionConfidence.MEDIUM),
        KeywordRule("hotel", "Travel", PredictionConfidence.MEDIUM),
        KeywordRule("airbnb", "Travel", PredictionConfidence.HIGH),
        KeywordRule("vacation", "Travel", PredictionConfidence.MEDIUM),
        KeywordRule("trip", "Travel", PredictionConfidence.MEDIUM),
        KeywordRule("holiday", "Travel", PredictionConfidence.MEDIUM),

        // ── Shopping ──
        KeywordRule("shopping", "Shopping", PredictionConfidence.MEDIUM),
        KeywordRule("clothes", "Shopping", PredictionConfidence.MEDIUM),
        KeywordRule("clothing", "Shopping", PredictionConfidence.MEDIUM),
        KeywordRule("shoes", "Shopping", PredictionConfidence.MEDIUM),
        KeywordRule("electronics", "Shopping", PredictionConfidence.MEDIUM),
        KeywordRule("gadget", "Shopping", PredictionConfidence.MEDIUM),

        // ── Bills ──
        KeywordRule("electricity bill", "Bills", PredictionConfidence.HIGH),
        KeywordRule("electric bill", "Bills", PredictionConfidence.HIGH),
        KeywordRule("internet bill", "Bills", PredictionConfidence.HIGH),
        KeywordRule("phone bill", "Bills", PredictionConfidence.HIGH),
        KeywordRule("mobile bill", "Bills", PredictionConfidence.HIGH),
        KeywordRule("broadband", "Bills", PredictionConfidence.MEDIUM),
        KeywordRule("wifi", "Bills", PredictionConfidence.MEDIUM),
        KeywordRule("internet", "Bills", PredictionConfidence.MEDIUM),
        KeywordRule("bill", "Bills", PredictionConfidence.LOW),

        // ── Health ──
        KeywordRule("personal care", "Personal Care", PredictionConfidence.HIGH),
        KeywordRule("gym", "Health", PredictionConfidence.MEDIUM),
        KeywordRule("fitness", "Health", PredictionConfidence.MEDIUM),
        KeywordRule("health", "Health", PredictionConfidence.MEDIUM),
        KeywordRule("medicine", "Health", PredictionConfidence.MEDIUM),
        KeywordRule("doctor", "Health", PredictionConfidence.MEDIUM),
        KeywordRule("hospital", "Health", PredictionConfidence.MEDIUM),
        KeywordRule("pharmacy", "Health", PredictionConfidence.MEDIUM),
        KeywordRule("medical", "Health", PredictionConfidence.MEDIUM),
        KeywordRule("clinic", "Health", PredictionConfidence.MEDIUM),
        KeywordRule("dental", "Health", PredictionConfidence.MEDIUM),

        // ── Entertainment ──
        KeywordRule("entertainment", "Entertainment", PredictionConfidence.MEDIUM),
        KeywordRule("movie", "Entertainment", PredictionConfidence.MEDIUM),
        KeywordRule("cinema", "Entertainment", PredictionConfidence.MEDIUM),
        KeywordRule("concert", "Entertainment", PredictionConfidence.MEDIUM),
        KeywordRule("gaming", "Entertainment", PredictionConfidence.MEDIUM),
        KeywordRule("game", "Entertainment", PredictionConfidence.LOW),
        KeywordRule("book", "Entertainment", PredictionConfidence.LOW),

        // ── Rent ──
        KeywordRule("rent", "Rent", PredictionConfidence.MEDIUM),
        KeywordRule("lease", "Rent", PredictionConfidence.MEDIUM),
        KeywordRule("housing", "Rent", PredictionConfidence.MEDIUM),

        // ── Groceries ──
        KeywordRule("groceries", "Groceries", PredictionConfidence.MEDIUM),
        KeywordRule("grocery", "Groceries", PredictionConfidence.MEDIUM),
        KeywordRule("supermarket", "Groceries", PredictionConfidence.MEDIUM),

        // ── Education ──
        KeywordRule("tuition", "Education", PredictionConfidence.MEDIUM),
        KeywordRule("education", "Education", PredictionConfidence.MEDIUM),
        KeywordRule("school", "Education", PredictionConfidence.MEDIUM),
        KeywordRule("college", "Education", PredictionConfidence.MEDIUM),
        KeywordRule("university", "Education", PredictionConfidence.MEDIUM),
        KeywordRule("course", "Education", PredictionConfidence.MEDIUM),

        // ── Subscriptions ──
        KeywordRule("premium plan", "Subscriptions", PredictionConfidence.HIGH),
        KeywordRule("subscription", "Subscriptions", PredictionConfidence.MEDIUM),
        KeywordRule("subscribe", "Subscriptions", PredictionConfidence.MEDIUM),
        KeywordRule("membership", "Subscriptions", PredictionConfidence.MEDIUM),

        // ── Insurance ──
        KeywordRule("insurance", "Insurance", PredictionConfidence.MEDIUM),

        // ── Gifts ──
        KeywordRule("birthday", "Gifts", PredictionConfidence.MEDIUM),
        KeywordRule("anniversary", "Gifts", PredictionConfidence.MEDIUM),
        KeywordRule("gift", "Gifts", PredictionConfidence.MEDIUM),
        KeywordRule("present", "Gifts", PredictionConfidence.MEDIUM),

        // ── Personal Care ──
        KeywordRule("haircut", "Personal Care", PredictionConfidence.MEDIUM),
        KeywordRule("salon", "Personal Care", PredictionConfidence.MEDIUM),
        KeywordRule("spa", "Personal Care", PredictionConfidence.MEDIUM),
        KeywordRule("beauty", "Personal Care", PredictionConfidence.MEDIUM),
        KeywordRule("cosmetics", "Personal Care", PredictionConfidence.MEDIUM),

        // ── Fuel ──
        KeywordRule("petrol", "Fuel", PredictionConfidence.MEDIUM),
        KeywordRule("diesel", "Fuel", PredictionConfidence.MEDIUM),
        KeywordRule("gas station", "Fuel", PredictionConfidence.HIGH),
        KeywordRule("filling station", "Fuel", PredictionConfidence.HIGH),
        KeywordRule("petrol pump", "Fuel", PredictionConfidence.HIGH),
        KeywordRule("fuel", "Fuel", PredictionConfidence.MEDIUM),

        // ── Maintenance ──
        KeywordRule("maintenance", "Maintenance", PredictionConfidence.MEDIUM),
        KeywordRule("repair", "Maintenance", PredictionConfidence.MEDIUM),
        KeywordRule("plumber", "Maintenance", PredictionConfidence.MEDIUM),
        KeywordRule("electrician", "Maintenance", PredictionConfidence.MEDIUM),

        // ── Transport ──
        KeywordRule("taxi", "Transport", PredictionConfidence.MEDIUM),
        KeywordRule("cab", "Transport", PredictionConfidence.MEDIUM),
        KeywordRule("ride", "Transport", PredictionConfidence.LOW),
        KeywordRule("metro", "Transport", PredictionConfidence.MEDIUM),
        KeywordRule("parking", "Transport", PredictionConfidence.MEDIUM),
        KeywordRule("toll", "Transport", PredictionConfidence.MEDIUM),

        // ── Donations ──
        KeywordRule("donation", "Donations", PredictionConfidence.MEDIUM),
        KeywordRule("donate", "Donations", PredictionConfidence.MEDIUM),
        KeywordRule("charity", "Donations", PredictionConfidence.MEDIUM),

        // ── Pets ──
        KeywordRule("pet", "Pets", PredictionConfidence.LOW),
        KeywordRule("dog", "Pets", PredictionConfidence.LOW),
        KeywordRule("cat", "Pets", PredictionConfidence.LOW),
        KeywordRule("vet", "Pets", PredictionConfidence.MEDIUM),

        // ── Childcare ──
        KeywordRule("childcare", "Childcare", PredictionConfidence.MEDIUM),
        KeywordRule("daycare", "Childcare", PredictionConfidence.MEDIUM),
        KeywordRule("kids", "Childcare", PredictionConfidence.LOW),
        KeywordRule("baby", "Childcare", PredictionConfidence.LOW),

        // ── Taxes ──
        KeywordRule("tax", "Taxes", PredictionConfidence.MEDIUM),
        KeywordRule("taxes", "Taxes", PredictionConfidence.MEDIUM),

        // ── Income keywords (for transactionTypeId=1) ──
        KeywordRule("salary", "Salary", PredictionConfidence.HIGH),
        KeywordRule("wages", "Salary", PredictionConfidence.HIGH),
        KeywordRule("paycheck", "Salary", PredictionConfidence.HIGH),
        KeywordRule("payroll", "Salary", PredictionConfidence.HIGH),
        KeywordRule("freelance", "Freelance", PredictionConfidence.MEDIUM),
        KeywordRule("freelancing", "Freelance", PredictionConfidence.MEDIUM),
        KeywordRule("business income", "Business", PredictionConfidence.MEDIUM),
        KeywordRule("business", "Business", PredictionConfidence.LOW),
        KeywordRule("investment return", "Investment", PredictionConfidence.MEDIUM),
        KeywordRule("dividend", "Investment", PredictionConfidence.MEDIUM),
        KeywordRule("interest", "Investment", PredictionConfidence.LOW),
        KeywordRule("cashback", "Other", PredictionConfidence.MEDIUM),
        KeywordRule("refund", "Other", PredictionConfidence.MEDIUM),
        KeywordRule("reversal", "Other", PredictionConfidence.MEDIUM)
    )

    // ──────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────

    override fun predict(text: String, transactionTypeId: Int): CategoryPrediction {
        if (text.isBlank()) {
            return fallback(transactionTypeId)
        }

        // Locale.ROOT keeps keyword matching deterministic (e.g. 'I' must not
        // become Turkish dotless 'ı', which would break the IOCL fuel rule).
        val lower = text.lowercase(Locale.ROOT)

        // 1. Check merchant rules (highest priority after user overrides)
        for (rule in merchantRules) {
            if (lower.contains(rule.keyword)) {
                val categoryId = resolveCategoryId(rule.categoryName, transactionTypeId)
                if (categoryId != null) {
                    return CategoryPrediction(
                        categoryId = categoryId,
                        confidence = rule.confidence,
                        merchant = rule.keyword.replaceFirstChar {
                            it.uppercase(Locale.ROOT)
                        },
                        source = PredictionSource.MERCHANT_MATCH
                    )
                }
            }
        }

        // 2. Check keyword rules (broader matches)
        for (rule in keywordRules) {
            if (lower.contains(rule.keyword)) {
                val categoryId = resolveCategoryId(rule.categoryName, transactionTypeId)
                if (categoryId != null) {
                    return CategoryPrediction(
                        categoryId = categoryId,
                        confidence = rule.confidence,
                        source = PredictionSource.KEYWORD_MATCH
                    )
                }
            }
        }

        // 3. No signal → fallback to "Other"
        return fallback(transactionTypeId)
    }

    /**
     * Predict with user-learned overrides consulted first.
     *
     * Call this overload when you have access to the user's merchant→category
     * override map (from [com.mknlabs.expensetracker.data.local.SmsLearningStore]).
     * The overrides take priority over all static rules.
     */
    fun predict(
        text: String,
        transactionTypeId: Int,
        userOverrides: Map<String, Int>
    ): CategoryPrediction {
        if (text.isBlank()) {
            return fallback(transactionTypeId)
        }

        val lower = text.lowercase(Locale.ROOT)

        // 0. Check user-learned overrides FIRST
        for ((merchant, categoryId) in userOverrides) {
            if (merchant.isBlank() || !lower.contains(merchant)) continue
            val category = categoryMap[categoryId]
            if (category != null && category.transactionTypeId == transactionTypeId) {
                return CategoryPrediction(
                    categoryId = categoryId,
                    confidence = PredictionConfidence.HIGH,
                    merchant = merchant.replaceFirstChar {
                        it.uppercase(Locale.ROOT)
                    },
                    source = PredictionSource.USER_LEARNED
                )
            }
        }

        // Fall through to static rules
        return predict(text, transactionTypeId)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────────────────────────────

    private fun resolveCategoryId(categoryName: String, transactionTypeId: Int): Int? {
        return categoryMap.entries.firstOrNull {
            it.value.name.equals(categoryName, ignoreCase = true) &&
                it.value.transactionTypeId == transactionTypeId
        }?.key
    }

    private fun fallback(transactionTypeId: Int): CategoryPrediction {
        val fallbackId = resolveCategoryId("Other", transactionTypeId) ?: FALLBACK_CATEGORY_ID
        return CategoryPrediction(
            categoryId = fallbackId,
            confidence = PredictionConfidence.LOW,
            source = PredictionSource.FALLBACK
        )
    }

    private companion object {
        /** "Other" expense category ID — last-resort fallback. */
        const val FALLBACK_CATEGORY_ID = 23
    }
}
