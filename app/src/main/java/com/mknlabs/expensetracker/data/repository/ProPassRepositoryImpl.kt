package com.mknlabs.expensetracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.domain.repository.ProPassRepository
import com.mknlabs.expensetracker.monetization.Feature
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProPassRepositoryImpl @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
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

            // 2. Redeem server-side. The redeemProPass Cloud Function validates the
            //    coupon and grants premium inside a single atomic Firestore transaction,
            //    and is the ONLY trusted writer of accountTier / proExpiryTimestamp —
            //    a modified client can no longer self-grant Pro.
            val result = FirebaseFunctions.getInstance()
                .getHttpsCallable("redeemProPass")
                .call(mapOf("code" to code))
                .await()

            val data = result.data as? Map<*, *>
                ?: return Result.failure(Exception("Invalid ProPass code"))
            val durationDays = (data["durationDays"] as? Number)?.toInt()
                ?: return Result.failure(Exception("Invalid ProPass code"))
            val newExpiry = (data["newExpiry"] as? Number)?.toLong()
                ?: System.currentTimeMillis() + durationDays * 24L * 60 * 60 * 1000

            // 3. Update local Profile state to match the server-authoritative result
            com.mknlabs.expensetracker.data.local.UserProfileDataStore.updateUserProfile(appContext) { profile ->
                profile.copy(
                    proExpiryTimestamp = newExpiry,
                    accountTier = "PREMIUM",
                    updatedAtMillis = System.currentTimeMillis()
                )
            }

            // 4. Update local AppSettings state to PREMIUM
            com.mknlabs.expensetracker.data.local.AppSettingsDataStore.updateUserTier(
                appContext, com.mknlabs.expensetracker.models.UserTier.PREMIUM
            )

            // 5. Update local state (Ad-free)
            monetizationRepository.grantTemporaryAccess(
                feature = Feature.AD_FREE_GLOBAL,
                optionId = null,
                durationMillis = newExpiry - System.currentTimeMillis()
            )

            Result.success(durationDays)
        } catch (e: Exception) {
            Result.failure(mapRedeemError(e))
        }
    }

    /**
     * Maps callable-function failures back to the same user-facing messages the
     * old client-side flow produced.
     */
    private fun mapRedeemError(e: Exception): Exception {
        val functionsException = e as? FirebaseFunctionsException ?: return e
        return when (functionsException.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED,
            FirebaseFunctionsException.Code.PERMISSION_DENIED ->
                Exception("Please sign in with Google or Email to redeem a ProPass")

            FirebaseFunctionsException.Code.NOT_FOUND ->
                Exception("Invalid ProPass code")

            FirebaseFunctionsException.Code.ALREADY_EXISTS ->
                Exception("You have already redeemed this ProPass code")

            FirebaseFunctionsException.Code.FAILED_PRECONDITION,
            FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                Exception(functionsException.message ?: "Invalid ProPass code")

            else ->
                Exception(functionsException.message ?: "Invalid ProPass code")
        }
    }
}
