package com.mknlabs.expensetracker.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.google.firebase.functions.FirebaseFunctionsException
import com.mknlabs.expensetracker.data.local.ReferPromptStore
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.MonetizationDataStore
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.domain.repository.ApplyReferralOutcome
import com.mknlabs.expensetracker.domain.repository.ReferralError
import com.mknlabs.expensetracker.domain.repository.ReferralRepository
import com.mknlabs.expensetracker.domain.repository.ReferralInvite
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
            val snap = ReferralSnapshot(
                code = data["code"] as? String ?: "",
                successCount = (data["successCount"] as? Number)?.toInt() ?: 0,
                rewardsThisMonth = (data["rewardsThisMonth"] as? Number)?.toInt() ?: 0,
                referredByUid = data["referredByUid"] as? String ?: "",
                accountTier = data["accountTier"] as? String ?: "",
                proExpiryTimestamp = (data["proExpiryTimestamp"] as? Number)?.toLong() ?: 0L
            )
            applyRemotePass(snap.proExpiryTimestamp)
            Result.success(snap)
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
            if (welcomeDays > 0) applyRemotePass(newExpiry)
            qualifyAfterTransaction()
            ApplyReferralOutcome.Success(
                welcomeDays = welcomeDays,
                newExpiry = newExpiry,
                needsFirstTx = transactionDao.countActive() < 1
            )
        } catch (e: Exception) {
            ApplyReferralOutcome.Failure(mapError(e))
        }
    }

    override suspend fun listMyReferrals(): List<ReferralInvite> {
        val user = firebaseAuth.currentUser
        if (user == null || user.isAnonymous) return emptyList()
        return try {
            val data = call("listMyReferrals", emptyMap())
            val raw = data["items"] as? List<*> ?: return emptyList()
            raw.mapNotNull { row ->
                val map = row as? Map<*, *> ?: return@mapNotNull null
                ReferralInvite(
                    refereeUid = map["refereeUid"] as? String ?: return@mapNotNull null,
                    displayName = map["displayName"] as? String ?: "Friend",
                    attributedAt = (map["attributedAt"] as? Number)?.toLong() ?: 0L,
                    qualified = map["qualified"] as? Boolean ?: false,
                    rewarded = map["rewarded"] as? Boolean ?: false
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "listMyReferrals failed", e)
            emptyList()
        }
    }

    override suspend fun qualifyAfterTransaction() {
        val user = firebaseAuth.currentUser
        if (user == null || user.isAnonymous) return
        if (transactionDao.countActive() < 1) return
        try {
            call("qualifyReferral", emptyMap())
            ReferPromptStore.setPendingQualify(appContext, false)
        } catch (e: Exception) {
            Log.w(TAG, "qualifyReferral skipped", e)
            ReferPromptStore.setPendingQualify(appContext, true)
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

    private suspend fun applyRemotePass(newExpiry: Long) {
        if (newExpiry <= System.currentTimeMillis()) return
        UserProfileDataStore.updateUserProfile(appContext) { profile ->
            profile.copy(
                proExpiryTimestamp = maxOf(profile.proExpiryTimestamp, newExpiry),
                accountTier = "PREMIUM",
                updatedAtMillis = System.currentTimeMillis()
            )
        }
        AppSettingsDataStore.updateUserTier(appContext, UserTier.PREMIUM)
        MonetizationDataStore.updateGlobalAdAccessExpiry(appContext, 0L)
    }

    private companion object {
        const val TAG = "Referral"
    }
}
