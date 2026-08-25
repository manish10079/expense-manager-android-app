package com.mknlabs.expensetracker.domain.models

/**
 * Result of automatic category detection from text input (voice, SMS, or manual).
 *
 * Follows the same convention as [ParsedVoiceTransaction]: ephemeral in-memory
 * model passed between the category predictor and the calling layer.
 *
 * @see com.mknlabs.expensetracker.domain.repository.CategoryPredictorRepository
 */
data class CategoryPrediction(
    /** Resolved category ID from [categoryMap]. */
    val categoryId: Int,
    /** How confident the predictor is about this match. */
    val confidence: PredictionConfidence,
    /** Matched merchant name (if a merchant rule matched), null otherwise. */
    val merchant: String? = null,
    /** The rule source that produced this prediction. */
    val source: PredictionSource
)

/**
 * Confidence level for category predictions.
 */
enum class PredictionConfidence {
    /** Merchant exact match or user-learned override — very reliable. */
    HIGH,
    /** Keyword match or partial merchant match — reliable but may need verification. */
    MEDIUM,
    /** Fallback / no signal — user should verify. */
    LOW
}

/**
 * Where the category prediction came from.
 */
enum class PredictionSource {
    /** User explicitly corrected a category for this merchant (learning system). */
    USER_LEARNED,
    /** Exact merchant name matched a rule. */
    MERCHANT_MATCH,
    /** Keyword found in text matched a category. */
    KEYWORD_MATCH,
    /** No signal found — fell back to "Other". */
    FALLBACK
}
