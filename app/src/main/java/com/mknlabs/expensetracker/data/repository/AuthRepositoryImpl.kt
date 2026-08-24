package com.mknlabs.expensetracker.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuthException
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    override val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    /**
     * Produces a safe, identifier-free description of an auth failure.
     * `FirebaseAuthException` messages can embed the user's email (e.g. "The email
     * address is badly formatted. [foo@bar.com]"), so only the error code or the
     * exception class name is logged — never the raw message or its stack trace.
     */
    private fun authErrorSummary(e: Exception): String {
        val code = (e as? FirebaseAuthException)?.errorCode
        return if (code != null) "code=$code" else "type=${e.javaClass.simpleName}"
    }

    init {
        // Listen for auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Boolean> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val currentUser = firebaseAuth.currentUser
            
            val result = if (currentUser != null && currentUser.isAnonymous) {
                android.util.Log.i("AuthRepo", "Linking anonymous user with Google credential")
                try {
                    currentUser.linkWithCredential(credential).await()
                } catch (e: Exception) {
                    // If linking fails (e.g. Google account already exists as a separate
                    // Firebase user), fall back to a standard credential sign-in.
                    android.util.Log.w("AuthRepo", "Linking failed (likely account exists), switching to standard sign-in: ${authErrorSummary(e)}")
                    firebaseAuth.signInWithCredential(credential).await()
                }
            } else {
                android.util.Log.i("AuthRepo", "Signing in with Google credential (no anonymous user to link)")
                firebaseAuth.signInWithCredential(credential).await()
            }
            
            Result.success(result.additionalUserInfo?.isNewUser == true)
        } catch (e: Exception) {
            val summary = authErrorSummary(e)
            val detailMsg = (e as? FirebaseAuthException)?.message ?: e.message
            android.util.Log.e("AuthRepo", "Firebase Google Sign-In/Link failed: summary=[$summary], class=[${e.javaClass.name}], message=[$detailMsg]", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Boolean> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val currentUser = firebaseAuth.currentUser

            val result = if (currentUser != null && currentUser.isAnonymous) {
                android.util.Log.i("AuthRepo", "Attempting to link anonymous user with existing Email account")
                try {
                    currentUser.linkWithCredential(credential).await()
                } catch (e: Exception) {
                    // If linking fails (e.g. email already in use), perform a standard sign-in
                    android.util.Log.w("AuthRepo", "Linking failed (likely account exists), switching to standard sign-in")
                    firebaseAuth.signInWithEmailAndPassword(email, password).await()
                }
            } else {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            }
            Result.success(result.additionalUserInfo?.isNewUser == true)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Email Sign-In failed: ${authErrorSummary(e)}")
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Boolean> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val currentUser = firebaseAuth.currentUser

            val result = if (currentUser != null && currentUser.isAnonymous) {
                android.util.Log.i("AuthRepo", "Upgrading anonymous user to Email account")
                currentUser.linkWithCredential(credential).await()
            } else {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            }
            
            // Automatically send verification email on account creation
            firebaseAuth.currentUser?.sendEmailVerification()?.await()
            
            Result.success(true) 
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Email Sign-Up failed: ${authErrorSummary(e)}")
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<Boolean> {
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            val isNewUser = result.additionalUserInfo?.isNewUser == true
            android.util.Log.i("AuthRepo", "Anonymous Sign-In successful. isNewUser: $isNewUser")
            Result.success(isNewUser)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Anonymous Sign-In failed: ${authErrorSummary(e)}")
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMagicLink(email: String): Result<Unit> {
        return try {
            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://expense-tracker-2ea00.web.app/login")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    "com.mknlabs.expensetracker",
                    true, /* installIfNotAvailable */
                    "1" /* minimumVersion */
                )
                .build()

            firebaseAuth.sendSignInLinkToEmail(email, actionCodeSettings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeSignInWithLink(email: String, emailLink: String): Result<Boolean> {
        return try {
            val credential = EmailAuthProvider.getCredentialWithLink(email, emailLink)
            val currentUser = firebaseAuth.currentUser

            val result = if (currentUser != null && currentUser.isAnonymous) {
                android.util.Log.i("AuthRepo", "Linking anonymous user with Email Link credential")
                currentUser.linkWithCredential(credential).await()
            } else {
                android.util.Log.i("AuthRepo", "Signing in with Email Link credential (no anonymous user to link)")
                firebaseAuth.signInWithEmailLink(email, emailLink).await()
            }
            Result.success(result.additionalUserInfo?.isNewUser == true)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Firebase Email Link Sign-In/Link failed: ${authErrorSummary(e)}")
            Result.failure(e)
        }
    }

    override fun isSignInWithEmailLink(link: String): Boolean {
        return firebaseAuth.isSignInWithEmailLink(link)
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Send verification email failed: ${authErrorSummary(e)}")
            Result.failure(e)
        }
    }

    override suspend fun reloadUser(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.reload()?.await()
            _currentUser.value = firebaseAuth.currentUser
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Reload user failed: ${authErrorSummary(e)}")
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw IllegalStateException("No user logged in")
            val email = user.email ?: throw IllegalStateException("User email not found")
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Update password failed: ${authErrorSummary(e)}")
            Result.failure(e)
        }
    }

    override suspend fun verifyBeforeUpdateEmail(newEmail: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw IllegalStateException("No user logged in")
            user.verifyBeforeUpdateEmail(newEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Verify before update email failed: ${authErrorSummary(e)}")
            Result.failure(e)
        }
    }

    override suspend fun reauthenticate(email: String, password: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw IllegalStateException("No user logged in")
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Reauthentication failed: ${authErrorSummary(e)}")
            Result.failure(e)
        }
    }
}
