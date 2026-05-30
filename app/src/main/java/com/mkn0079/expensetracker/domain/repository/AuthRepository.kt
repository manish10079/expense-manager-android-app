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
     * Sends a magic sign-in link to the user's email.
     */
    suspend fun sendMagicLink(email: String): Result<Unit>

    /**
     * Completes the sign-in process using the link received via email.
     */
    suspend fun completeSignInWithLink(email: String, emailLink: String): Result<Unit>

    /**
     * Returns true if the provided link is a valid Firebase Sign-In link.
     */
    fun isSignInWithEmailLink(link: String): Boolean

    /**
     * Signs the user out of the current session.
     */
    fun signOut()

    /**
     * Returns true if the user is authenticated.
     */
    fun isUserLoggedIn(): Boolean
}
