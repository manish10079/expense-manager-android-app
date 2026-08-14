package com.mknlabs.expensetracker.data.repository

import android.content.Context
import android.provider.Settings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mknlabs.expensetracker.domain.repository.FcmTokenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed FCM device-token registry (notification plan §5.6):
 *
 *     /users/{uid}/fcmTokens/{deviceId} {
 *         token, platform: "android", timezone, createdAt, lastSeen
 *     }
 *
 * The deviceId is the stable ANDROID_ID, matching the sync device registry
 * (SyncRepositoryImpl) so one device = one entry in both collections. The
 * Firestore rules already grant signed-in users read/write on their own
 * subcollections (/users/{uid}/{document=**}), so no rule change is needed.
 */
@Singleton
class FcmTokenRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : FcmTokenRepository {

    private val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"
    }

    override suspend fun registerCurrentDeviceToken(token: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val uid = firebaseAuth.currentUser?.uid
                ?: return@withContext Result.success(Unit)
            try {
                firestore.collection("users").document(uid)
                    .collection("fcmTokens").document(deviceId)
                    .set(
                        mapOf(
                            "token" to token,
                            "platform" to "android",
                            "timezone" to java.util.TimeZone.getDefault().id,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "lastSeen" to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()
                    )
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                android.util.Log.w("FcmToken", "Token registration failed: ${e.message}")
                Result.failure(e)
            }
        }

    override suspend fun removeCurrentDeviceToken(): Result<Unit> =
        withContext(Dispatchers.IO) {
            val uid = firebaseAuth.currentUser?.uid
                ?: return@withContext Result.success(Unit)
            try {
                firestore.collection("users").document(uid)
                    .collection("fcmTokens").document(deviceId)
                    .delete()
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                android.util.Log.w("FcmToken", "Token removal failed: ${e.message}")
                Result.failure(e)
            }
        }
}
