package com.mknlabs.expensetracker.ai.cloud

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User context for personalized Gemini AI parsing.
 * Contains only metadata — no transaction data (privacy safe).
 */
data class UserAiContext(
    val currency: String = "INR",
    val locale: String = "en-US"
)

/**
 * Fetches minimal user context from Firestore for personalized Gemini prompts.
 *
 * Reads only:
 * - User profile (currency, locale) — 1 Firestore read per parse
 * - Top categories/payment methods are computed on-device, not fetched here
 *
 * This keeps the Cloud Function lightweight and privacy-safe.
 */
@Singleton
class UserContextProvider @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Fetches user context for AI parsing.
     * Returns defaults if user is not signed in or profile doesn't exist.
     */
    suspend fun getUserContext(): UserAiContext {
        return try {
            val uid = auth.currentUser?.uid ?: return UserAiContext()
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                UserAiContext(
                    currency = userDoc.getString("currency") ?: "INR",
                    locale = userDoc.getString("locale") ?: "en-US"
                )
            } else {
                UserAiContext()
            }
        } catch (e: Exception) {
            // Don't fail parsing due to context fetch errors
            UserAiContext()
        }
    }
}
