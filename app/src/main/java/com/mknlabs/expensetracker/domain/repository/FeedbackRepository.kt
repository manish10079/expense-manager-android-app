package com.mknlabs.expensetracker.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interface representing the repository for sending user feedback and managing rate limits.
 */
interface FeedbackRepository {
    /**
     * Submits user feedback to the central database (e.g. Firebase Firestore).
     */
    suspend fun submitFeedback(userId: String, email: String, feedback: String): Result<Unit>

    /**
     * Gets the timestamp (in milliseconds) of the last successful feedback submission.
     */
    fun getLastFeedbackTime(): Flow<Long>

    /**
     * Saves the timestamp of the last successful feedback submission.
     */
    suspend fun saveLastFeedbackTime(timestamp: Long)
}
