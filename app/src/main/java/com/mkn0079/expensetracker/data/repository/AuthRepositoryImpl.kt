package com.mkn0079.expensetracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ActionCodeSettings
import com.mkn0079.expensetracker.domain.repository.AuthRepository
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
            val result = firebaseAuth.signInWithCredential(credential).await()
            Result.success(result.additionalUserInfo?.isNewUser == true)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Firebase Sign-In failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Boolean> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.additionalUserInfo?.isNewUser == true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Boolean> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(true) // signUp always means a new user
        } catch (e: Exception) {
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
            Result.success(result.additionalUserInfo?.isNewUser == true)
        } catch (e: Exception) {
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
                    "com.mkn0079.expensetracker",
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
            val result = firebaseAuth.signInWithEmailLink(email, emailLink).await()
            Result.success(result.additionalUserInfo?.isNewUser == true)
        } catch (e: Exception) {
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
