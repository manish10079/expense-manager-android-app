package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.domain.models.CategoryPrediction

/**
 * Abstraction for automatic category detection from text input.
 *
 * Implementations:
 * - [com.mknlabs.expensetracker.ai.CategoryPredictor] (offline, rule-based, Hilt-injectable)
 *
 * Works for any text source: voice input, SMS body, manual description, etc.
 * Consults user-learned merchant overrides before static rules.
 */
interface CategoryPredictorRepository {

    /**
     * Predicts the best category for [text] given its [transactionTypeId].
     *
     * @param text The input text to analyze (voice transcript, SMS body, etc.).
     * @param transactionTypeId 1 = Income, 2 = Expense. Determines which
     *        categoryMap entries are eligible.
     * @return [CategoryPrediction] with the resolved category, confidence, and source.
     */
    fun predict(text: String, transactionTypeId: Int): CategoryPrediction
}
