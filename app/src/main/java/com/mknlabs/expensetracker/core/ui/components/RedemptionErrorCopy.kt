package com.mknlabs.expensetracker.core.ui.components

import androidx.annotation.StringRes
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.repository.RedemptionError

/**
 * The line shown under the code field when a redemption is refused.
 *
 * Pure, and separate from the dialog, so the table is assertable without Compose: this is the
 * copy a user reads at the moment they are told "no", which is exactly when a generic
 * "something went wrong" costs the most.
 *
 * [RedemptionError.InvalidCode] reuses the long-standing "invalid or expired" string instead
 * of inventing a second wording for the same advice. A code the server has never heard of and
 * a code it cannot grant are one problem as far as the user can act on it.
 *
 * [RedemptionError.SubscriptionActive] carries the subscription's expiry, which the screen
 * that also holds a date formatter can surface; the dialog states the fact without a date
 * rather than formatting one of its own.
 */
@StringRes
internal fun redemptionErrorMessageRes(error: RedemptionError): Int = when (error) {
    RedemptionError.NotSignedIn -> R.string.error_redeem_not_signed_in
    RedemptionError.InvalidCode -> R.string.error_invalid_pro_pass
    RedemptionError.Inactive -> R.string.error_redeem_inactive
    RedemptionError.Expired -> R.string.error_redeem_expired
    RedemptionError.LimitReached -> R.string.error_redeem_limit_reached
    RedemptionError.AlreadyRedeemed -> R.string.error_redeem_already_redeemed
    is RedemptionError.SubscriptionActive -> R.string.error_redeem_subscription_active
    RedemptionError.Network -> R.string.error_redeem_network
    RedemptionError.Unknown -> R.string.error_redeem_unknown
}
