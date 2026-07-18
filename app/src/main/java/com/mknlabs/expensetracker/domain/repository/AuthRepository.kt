package com.mknlabs.expensetracker.domain.repository

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
     * Returns true if a new user was created.
     */
    suspend fun signInWithGoogle(idToken: String): Result<Boolean>

    /**
     * Signs in with Email and Password.
     * Returns true if a new user was created (should usually be false).
     */
    suspend fun signInWithEmail(email: String, password: String): Result<Boolean>

    /**
     * Creates a new account with Email and Password.
     * Returns true on success.
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<Boolean>

    /**
     * Sends a password reset email.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Signs in anonymously. Useful for guest access.
     * Returns true if a new user was created.
     */
    suspend fun signInAnonymously(): Result<Boolean>

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
     * Returns true if a new user was created.
     */
    suspend fun completeSignInWithLink(email: String, emailLink: String): Result<Boolean>

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

    /**
     * Sends a verification email to the currently logged in user.
     */
    suspend fun sendEmailVerification(): Result<Unit>

    /**
     * Reloads the current user's profile and auth state from Firebase.
     */
    suspend fun reloadUser(): Result<Unit>

    /**
     * Updates the password for the current email-password user.
     * Requires re-authentication with the current password.
     */
    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit>
}
