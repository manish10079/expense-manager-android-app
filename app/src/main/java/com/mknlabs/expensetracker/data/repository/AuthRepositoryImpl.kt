package com.mknlabs.expensetracker.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ActionCodeSettings
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
                android.util.Log.i("AuthRepo", "Linking anonymous user ${currentUser.uid} with Google credential")
                currentUser.linkWithCredential(credential).await()
            } else {
                android.util.Log.i("AuthRepo", "Signing in with Google credential (no anonymous user to link)")
                firebaseAuth.signInWithCredential(credential).await()
            }
            
            Result.success(result.additionalUserInfo?.isNewUser == true)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Firebase Google Sign-In/Link failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Boolean> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val currentUser = firebaseAuth.currentUser

            val result = if (currentUser != null && currentUser.isAnonymous) {
                android.util.Log.i("AuthRepo", "Attempting to link anonymous user ${currentUser.uid} with existing Email account")
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
            android.util.Log.e("AuthRepo", "Email Sign-In failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Boolean> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val currentUser = firebaseAuth.currentUser

            val result = if (currentUser != null && currentUser.isAnonymous) {
                android.util.Log.i("AuthRepo", "Upgrading anonymous user ${currentUser.uid} to Email account")
                currentUser.linkWithCredential(credential).await()
            } else {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            }
            Result.success(true) 
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Email Sign-Up failed: ${e.message}", e)
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
            android.util.Log.i("AuthRepo", "Anonymous Sign-In successful. isNewUser: $isNewUser, uid: ${result.user?.uid}")
            Result.success(isNewUser)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Anonymous Sign-In failed: ${e.message}", e)
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
                android.util.Log.i("AuthRepo", "Linking anonymous user ${currentUser.uid} with Email Link credential")
                currentUser.linkWithCredential(credential).await()
            } else {
                android.util.Log.i("AuthRepo", "Signing in with Email Link credential (no anonymous user to link)")
                firebaseAuth.signInWithEmailLink(email, emailLink).await()
            }
            Result.success(result.additionalUserInfo?.isNewUser == true)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Firebase Email Link Sign-In/Link failed: ${e.message}", e)
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
}
