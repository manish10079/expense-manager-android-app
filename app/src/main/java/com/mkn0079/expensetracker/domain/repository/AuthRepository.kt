package com.mkn0079.expensetracker.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain-level interface for Firebase Authentication.
 */
interface AuthRepository {
    /**
     * Emits the currently logged-in FirebaseUser or null if signed out.
     */
    val currentUser: StateFlow<FirebaseUser?>

    /**
     * Signs in using a Google ID Token (obtained from the Credential Manager).
     */
    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    /**
     * Signs in with Email and Password.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    /**
     * Creates a new account with Email and Password.
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>

    /**
     * Sends a password reset email.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Signs in anonymously. Useful for guest access.
     */
    suspend fun signInAnonymously(): Result<Unit>

    /**
     * Deletes the user's account from Firebase Auth.
     */
    suspend fun deleteAccount(): Result<Unit>

    /**
     * Signs the user out of the current session.
     */
    fun signOut()

    /**
     * Returns true if the user is authenticated.
     */
    fun isUserLoggedIn(): Boolean
}
