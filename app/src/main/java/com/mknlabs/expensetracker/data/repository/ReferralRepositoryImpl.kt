package com.mknlabs.expensetracker.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.google.firebase.functions.FirebaseFunctionsException
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.MonetizationDataStore
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.domain.repository.ApplyReferralOutcome
import com.mknlabs.expensetracker.domain.repository.ReferralError
import com.mknlabs.expensetracker.domain.repository.ReferralRepository
import com.mknlabs.expensetracker.domain.repository.ReferralSnapshot
import com.mknlabs.expensetracker.models.UserTier
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferralRepositoryImpl @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
    private val firebaseAuth: FirebaseAuth,
    private val transactionDao: TransactionDao
) : ReferralRepository {

    override suspend fun ensureCode(): Result<ReferralSnapshot> {
        val user = firebaseAuth.currentUser
        if (user == null || user.isAnonymous) {
            return Result.failure(IllegalStateException("not signed in"))
        }
        return try {
            val data = call("ensureReferralCode", emptyMap())
            Result.success(
                ReferralSnapshot(
                    code = data["code"] as? String ?: "",
                    successCount = (data["successCount"] as? Number)?.toInt() ?: 0,
                    rewardsThisMonth = (data["rewardsThisMonth"] as? Number)?.toInt() ?: 0,
                    referredByUid = data["referredByUid"] as? String ?: ""
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "ensureReferralCode failed", e)
            Result.failure(e)
        }
    }

    override suspend fun applyCode(code: String): ApplyReferralOutcome {
        val user = firebaseAuth.currentUser
        if (user == null || user.isAnonymous) {
            return ApplyReferralOutcome.Failure(ReferralError.NotSignedIn)
        }
        return try {
            val data = call("applyReferralCode", mapOf("code" to code))
            val welcomeDays = (data["welcomeDays"] as? Number)?.toInt() ?: 0
            val newExpiry = (data["newExpiry"] as? Number)?.toLong() ?: 0L
            if (welcomeDays > 0 && newExpiry > 0L) {
                UserProfileDataStore.updateUserProfile(appContext) { profile ->
                    profile.copy(
                        proExpiryTimestamp = newExpiry,
                        accountTier = "PREMIUM",
                        updatedAtMillis = System.currentTimeMillis()
                    )
                }
                AppSettingsDataStore.updateUserTier(appContext, UserTier.PREMIUM)
                MonetizationDataStore.updateGlobalAdAccessExpiry(appContext, 0L)
            }
            val outcome = ApplyReferralOutcome.Success(welcomeDays, newExpiry)
            qualifyAfterTransaction()
            outcome
        } catch (e: Exception) {
            ApplyReferralOutcome.Failure(mapError(e))
        }
    }

    override suspend fun qualifyAfterTransaction() {
        val user = firebaseAuth.currentUser
        if (user == null || user.isAnonymous) return
        if (transactionDao.countActive() < 1) return
        try {
            call("qualifyReferral", emptyMap())
        } catch (e: Exception) {
            Log.w(TAG, "qualifyReferral skipped", e)
        }
    }

    private suspend fun call(name: String, payload: Map<String, Any>): Map<*, *> {
        val result = FirebaseFunctions.getInstance()
            .getHttpsCallable(name)
            .call(payload)
            .await()
        return result.data as? Map<*, *> ?: emptyMap<String, Any>()
    }

    private fun mapError(e: Exception): ReferralError {
        if (e is FirebaseFunctionsException) {
            when (e.code) {
                FirebaseFunctionsException.Code.UNAUTHENTICATED,
                FirebaseFunctionsException.Code.PERMISSION_DENIED -> return ReferralError.NotSignedIn
                FirebaseFunctionsException.Code.NOT_FOUND,
                FirebaseFunctionsException.Code.INVALID_ARGUMENT -> return ReferralError.InvalidCode
                else -> {
                    val reason = (e.details as? Map<*, *>)?.get("reason") as? String
                    return ReferralError.fromReason(reason)
                }
            }
        }
        return ReferralError.Network
    }

    private companion object {
        const val TAG = "Referral"
    }
}
