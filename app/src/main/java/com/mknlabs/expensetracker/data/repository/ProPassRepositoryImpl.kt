package com.mknlabs.expensetracker.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.domain.repository.ProPassRepository
import com.mknlabs.expensetracker.domain.repository.RedemptionError
import com.mknlabs.expensetracker.domain.repository.RedemptionOutcome
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

    override suspend fun redeemCode(code: String): RedemptionOutcome {
        // 1. A grant has to hang off a real account
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            return RedemptionOutcome.Failure(RedemptionError.NotSignedIn)
        }

        // 2. Redeem server-side. The redeemProPass Cloud Function validates the coupon and
        //    grants premium inside a single atomic Firestore transaction, and is the ONLY
        //    trusted writer of accountTier / proExpiryTimestamp — a modified client can no
        //    longer self-grant Pro. It also refuses a code while a store subscription is live,
        //    and says why through `details.reason`.
        val result = try {
            FirebaseFunctions.getInstance()
                .getHttpsCallable("redeemProPass")
                .call(mapOf("code" to code))
                .await()
        } catch (e: Exception) {
            return RedemptionOutcome.Failure(redemptionErrorOf(e))
        }

        val data = result.data as? Map<*, *>
        val durationDays = (data?.get("durationDays") as? Number)?.toInt()
        if (data == null || durationDays == null) {
            // A grant always carries durationDays, so reaching here means the call succeeded
            // and this build could not read the answer. The dialog can only report that as a
            // bad code, so the keys are logged to keep the claim checkable.
            Log.w(TAG, "redeemProPass returned no durationDays; keys=${data?.keys}")
            return RedemptionOutcome.Failure(RedemptionError.InvalidCode)
        }
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

        // 5. Clear any temporary ad-pass timer since user is now a full Pro / Premium user
        com.mknlabs.expensetracker.data.local.MonetizationDataStore.updateGlobalAdAccessExpiry(appContext, 0L)

        return RedemptionOutcome.Success(durationDays)
    }

    private companion object {
        /** Shared with the view model, so one tag covers a whole redemption attempt. */
        const val TAG = "ProPassRedeem"
    }
}
