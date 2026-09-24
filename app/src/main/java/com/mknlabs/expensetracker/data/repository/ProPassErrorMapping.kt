package com.mknlabs.expensetracker.data.repository

import com.google.firebase.functions.FirebaseFunctionsException
import com.mknlabs.expensetracker.domain.repository.RedemptionError
import java.io.IOException

/**
 * Translates a failed `redeemProPass` call into a [RedemptionError].
 *
 * The function puts a typed `reason` in `HttpsError.details`, because the HttpsError code
 * alone cannot separate "this code expired" from "this code is used up". The code is still
 * consulted first where it is decisive — a signed-out caller is not a coupon problem — and
 * the reason is the fallback for the many refusals that share `failed-precondition`.
 *
 * The server's own message is deliberately never used as copy: it is English, and copy
 * belongs to `strings.xml`.
 */
internal fun redemptionErrorOf(e: Exception): RedemptionError {
    if (e is IOException) return RedemptionError.Network

    val functionsException = e as? FirebaseFunctionsException ?: return RedemptionError.Unknown
    val details = functionsException.details as? Map<*, *>

    return redemptionErrorFrom(
        code = functionsException.code,
        reason = details?.get("reason") as? String,
        subscriptionExpiryMillis = (details?.get("subscriptionExpiry") as? Number)?.toLong() ?: 0L,
    )
}

/**
 * The decision table itself, as a pure function of the callable's two halves.
 *
 * Split out from [redemptionErrorOf] so it is assertable without building a
 * `FirebaseFunctionsException`, which the SDK does not expose a constructor for.
 */
internal fun redemptionErrorFrom(
    code: FirebaseFunctionsException.Code?,
    reason: String?,
    subscriptionExpiryMillis: Long,
): RedemptionError = when (code) {
    FirebaseFunctionsException.Code.UNAUTHENTICATED,
    FirebaseFunctionsException.Code.PERMISSION_DENIED -> RedemptionError.NotSignedIn

    FirebaseFunctionsException.Code.NOT_FOUND -> RedemptionError.InvalidCode

    FirebaseFunctionsException.Code.ALREADY_EXISTS -> RedemptionError.AlreadyRedeemed

    FirebaseFunctionsException.Code.FAILED_PRECONDITION,
    FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
        RedemptionError.fromReason(reason, subscriptionExpiryMillis)

    else -> RedemptionError.Unknown
}
