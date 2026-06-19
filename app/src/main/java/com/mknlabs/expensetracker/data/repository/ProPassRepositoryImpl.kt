package com.mknlabs.expensetracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.domain.repository.ProPassRepository
import com.mknlabs.expensetracker.monetization.Feature
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProPassRepositoryImpl @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val monetizationRepository: MonetizationRepository
) : ProPassRepository {

    override suspend fun redeemCode(code: String): Result<Int> {
        return try {
            // 1. Check if user is signed in and NOT anonymous
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null || currentUser.isAnonymous) {
                return Result.failure(Exception("Please sign in with Google or Email to redeem a ProPass"))
            }

            val normalizedCode = code.uppercase().trim()
            if (normalizedCode.isEmpty()) {
                return Result.failure(Exception("Code cannot be empty"))
            }

            val docRef = firestore.collection("coupons").document(normalizedCode)
            val doc = docRef.get().await()

            if (!doc.exists()) {
                return Result.failure(Exception("Invalid ProPass code"))
            }

            val isActive = doc.getBoolean("isActive") ?: false
            val durationDays = doc.getLong("durationDays")?.toInt() ?: 0
            val maxUses = doc.getLong("maxUses") ?: 0L
            val currentUses = doc.getLong("currentUses") ?: 0L
            val expiryTimestamp = doc.getTimestamp("expiryTimestamp")
            val repeatAllowedUids = doc.get("repeatAllowedUids") as? List<String> ?: emptyList()

            // 2. Validation
            if (!isActive) {
                return Result.failure(Exception("This ProPass code is no longer active"))
            }

            // Check if user has already redeemed this code
            val redemptionDoc = docRef.collection("redemptions").document(currentUser.uid).get().await()
            val isWhitelisted = repeatAllowedUids.contains(currentUser.uid)

            if (redemptionDoc.exists() && !isWhitelisted) {
                return Result.failure(Exception("You have already redeemed this ProPass code"))
            }

            if (currentUses >= maxUses) {
                return Result.failure(Exception("This ProPass code has reached its usage limit"))
            }

            if (expiryTimestamp != null && expiryTimestamp.toDate().before(java.util.Date())) {
                return Result.failure(Exception("This ProPass code has expired"))
            }

            // 3. Calculate and Save Expiry
            val durationMillis = durationDays * 24 * 60 * 60 * 1000L
            val currentExpiry = monetizationRepository.globalAdAccessExpiry.first()
            val newExpiry = Math.max(System.currentTimeMillis(), currentExpiry) + durationMillis

            val newUpdatedAt = System.currentTimeMillis()

            // Save to user's cloud profile for cross-device tracking
            firestore.collection("users").document(currentUser.uid)
                .set(
                    mapOf(
                        "uid" to currentUser.uid,
                        "proExpiryTimestamp" to newExpiry,
                        "accountTier" to "PREMIUM",
                        "profileUpdatedAtMillis" to newUpdatedAt
                    ), 
                    SetOptions.merge()
                ).await()

            // Update local Profile state
            com.mknlabs.expensetracker.data.local.UserProfileDataStore.updateUserProfile(appContext) { profile ->
                profile.copy(
                    proExpiryTimestamp = newExpiry,
                    accountTier = "PREMIUM",
                    updatedAtMillis = newUpdatedAt
                )
            }

            // Update local state (Ad-free)
            monetizationRepository.grantTemporaryAccess(
                feature = Feature.AD_FREE_GLOBAL,
                optionId = null,
                durationMillis = (newExpiry - System.currentTimeMillis())
            )

            // 4. Update Coupon Usage Count and Record Redemption
            docRef.update("currentUses", FieldValue.increment(1)).await()
            docRef.collection("redemptions").document(currentUser.uid).set(
                mapOf(
                    "redeemedAt" to FieldValue.serverTimestamp(),
                    "userId" to currentUser.uid
                )
            ).await()

            Result.success(durationDays)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
