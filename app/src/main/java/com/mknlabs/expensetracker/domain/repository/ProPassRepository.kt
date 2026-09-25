package com.mknlabs.expensetracker.domain.repository

/**
 * Why a ProPass code was refused.
 *
 * Typed rather than a message: the wording belongs to `strings.xml` (GEMINI.md §5), so the
 * repository reports *what* happened and the UI decides how to say it. The reasons mirror the
 * `details.reason` values the `redeemProPass` Cloud Function throws.
 */
sealed interface RedemptionError {

    /** Signed out, or signed in anonymously: a grant has to hang off a real account. */
    data object NotSignedIn : RedemptionError

    /** Unknown code, or one the backend has no usable duration for. */
    data object InvalidCode : RedemptionError

    /** A real code that an administrator has switched off. */
    data object Inactive : RedemptionError

    /** The code's own redemption deadline has passed. */
    data object Expired : RedemptionError

    /** The code has been redeemed as many times as it allows. */
    data object LimitReached : RedemptionError

    /** This account has already spent this code. */
    data object AlreadyRedeemed : RedemptionError

    /**
     * The account already has a running pass, so this code would sit behind it and its days
     * would be spent on access the user already has.
     *
     * [expiryMillis] is when that pass ends, or 0 when the server did not say.
     */
    data class PassActive(val expiryMillis: Long) : RedemptionError

    /**
     * The account is already Pro through the store, so the pass would run out unused.
     *
     * [expiryMillis] is when that subscription ends, or 0 when it does not expire.
     */
    data class SubscriptionActive(val expiryMillis: Long) : RedemptionError

    /** The call never reached the function. */
    data object Network : RedemptionError

    /** Anything the app cannot classify. */
    data object Unknown : RedemptionError

    companion object {
        /** `details.reason` values thrown by `redeemProPass`. */
        const val REASON_SUBSCRIPTION_ACTIVE = "SUBSCRIPTION_ACTIVE"
        const val REASON_INVALID_CODE = "INVALID_CODE"
        const val REASON_INACTIVE = "INACTIVE"
        const val REASON_EXPIRED = "EXPIRED"
        const val REASON_LIMIT_REACHED = "LIMIT_REACHED"
        const val REASON_ALREADY_REDEEMED = "ALREADY_REDEEMED"
        const val REASON_PASS_ACTIVE = "PASS_ACTIVE"

        /**
         * Maps a server reason onto a typed error. An absent or unfamiliar reason is
         * [Unknown] rather than a guess: the reasons are a contract, and inventing a meaning
         * for one this build has never heard of would put the wrong sentence in front of the
         * user.
         */
        fun fromReason(
            reason: String?,
            blockingExpiryMillis: Long = 0L,
        ): RedemptionError = when (reason) {
            REASON_SUBSCRIPTION_ACTIVE -> SubscriptionActive(blockingExpiryMillis)
            REASON_PASS_ACTIVE -> PassActive(blockingExpiryMillis)
            REASON_INVALID_CODE -> InvalidCode
            REASON_INACTIVE -> Inactive
            REASON_EXPIRED -> Expired
            REASON_LIMIT_REACHED -> LimitReached
            REASON_ALREADY_REDEEMED -> AlreadyRedeemed
            else -> Unknown
        }
    }
}

/** The outcome of a redemption attempt. */
sealed interface RedemptionOutcome {
    data class Success(val durationDays: Int) : RedemptionOutcome
    data class Failure(val error: RedemptionError) : RedemptionOutcome
}

interface ProPassRepository {
    /**
     * Redeems a ProPass code on the server and mirrors the granted access locally.
     *
     * Typed rather than a [Result] so the caller can word the refusal: the server separates an
     * expired code from an exhausted one, and collapsing those into a single message was only
     * ever an accident of passing strings around.
     */
    suspend fun redeemCode(code: String): RedemptionOutcome
}
