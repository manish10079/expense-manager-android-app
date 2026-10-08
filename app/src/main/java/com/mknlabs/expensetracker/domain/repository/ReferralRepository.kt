package com.mknlabs.expensetracker.domain.repository

sealed interface ReferralError {
    data object NotSignedIn : ReferralError
    data object InvalidCode : ReferralError
    data object OwnCode : ReferralError
    data object AlreadyReferred : ReferralError
    data object Network : ReferralError
    data object Unknown : ReferralError

    companion object {
        fun fromReason(reason: String?): ReferralError = when (reason) {
            "INVALID_CODE" -> InvalidCode
            "OWN_CODE" -> OwnCode
            "ALREADY_REFERRED" -> AlreadyReferred
            else -> Unknown
        }
    }
}

data class ReferralSnapshot(
    val code: String,
    val successCount: Int,
    val rewardsThisMonth: Int,
    val referredByUid: String,
    val accountTier: String = "",
    val proExpiryTimestamp: Long = 0L
)

data class ReferralInvite(
    val refereeUid: String,
    val displayName: String,
    val attributedAt: Long,
    val qualified: Boolean,
    val rewarded: Boolean
)

sealed interface ApplyReferralOutcome {
    data class Success(
        val welcomeDays: Int,
        val newExpiry: Long,
        val needsFirstTx: Boolean
    ) : ApplyReferralOutcome
    data class Failure(val error: ReferralError) : ApplyReferralOutcome
}

interface ReferralRepository {
    suspend fun ensureCode(): Result<ReferralSnapshot>
    suspend fun applyCode(code: String): ApplyReferralOutcome
    suspend fun qualifyAfterTransaction()
    suspend fun listMyReferrals(): List<ReferralInvite>
}
