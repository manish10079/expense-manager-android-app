package com.mknlabs.expensetracker.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.revenuecat.purchases.CustomerInfo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sandbox-testing aid: prints the RevenueCat SDK's view of entitlements next to the
 * snapshot the RevenueCat Firebase Extension wrote to Firestore, so the
 * RevenueCat -> Firestore sync can be confirmed from logcat without opening the
 * Firebase console.
 *
 * The two sides are written by different systems and reach the app by different routes:
 *  - [CustomerInfo] comes back from the store/RevenueCat API in response to this device's
 *    own SDK call, so it is always current.
 *  - `rc_customers/{appUserId}` is written by the extension **asynchronously**, driven by a
 *    RevenueCat webhook. It therefore lags the SDK by seconds after a purchase, renewal,
 *    cancellation or transfer.
 *
 * That lag is why this reports rather than asserts: a mismatch immediately after a purchase
 * is expected behaviour, not a bug.
 *
 * Read-only. The document is client-writable nowhere — `firestore.rules` grants the owner
 * `get` only, and the extension writes with the Admin SDK.
 */
@Singleton
class SubscriptionSyncDiagnostics @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    companion object {
        private const val TAG = "SubSyncCheck"

        /**
         * Must match the extension's "customers collection" parameter and the
         * `rc_customers` block in `firestore.rules`.
         */
        const val CUSTOMERS_COLLECTION = "rc_customers"

        /** The extension's document shape is RevenueCat's, not ours, so values are dumped
         *  raw — a deep field could otherwise flood logcat. */
        private const val MAX_RENDERED_VALUE_LENGTH = 600

        /** Field RevenueCat's own docs use for this collection, checked when present. */
        private const val DOCUMENT_ENTITLEMENTS_FIELD = "entitlements"

        /**
         * Custom claim the extension writes onto the Firebase ID token.
         *
         * Spelled exactly as the extension names it. It is reported specifically when
         * present, but the log never *requires* it — see the custom-claim dump below.
         */
        private const val ENTITLEMENTS_CLAIM = "revenueCatEntitlements"

        /**
         * Claims Firebase puts on every ID token.
         *
         * Anything outside this set is a custom claim, which is how the extension's output
         * is identified without assuming which key it chose.
         */
        private val STANDARD_TOKEN_CLAIMS = setOf(
            "iss", "aud", "exp", "iat", "sub", "auth_time", "user_id", "firebase",
            "email", "email_verified", "name", "picture", "phone_number",
            "given_name", "family_name", "locale",
        )
    }

    /**
     * Logs both views of the subscriber. Safe to call when [appUserId] is null — the check
     * is simply skipped, because an anonymous RevenueCat user has no Firebase uid to look up.
     */
    suspend fun logSyncCheck(appUserId: String?, customerInfo: CustomerInfo?) {
        val activeEntitlements = customerInfo?.entitlements?.all
            ?.filterValues { it.isActive }
            ?.toSortedMap()
            .orEmpty()

        Log.d(TAG, "── RevenueCat -> Firestore sync check ──")

        logEntitlementClaims()

        if (appUserId.isNullOrBlank()) {
            Log.w(
                TAG,
                "SKIPPED: no signed-in user, so there is no $CUSTOMERS_COLLECTION document to compare"
            )
            return
        }

        if (activeEntitlements.isEmpty()) {
            Log.d(TAG, "SDK   : RevenueCat reports no active entitlements")
        } else {
            activeEntitlements.values.forEach { entitlement ->
                Log.d(
                    TAG,
                    "SDK   : '${entitlement.identifier}' ACTIVE, sandbox=${entitlement.isSandbox}, " +
                        "store=${entitlement.store}, product=${entitlement.productIdentifier}, " +
                        "plan=${entitlement.productPlanIdentifier}, expires=${entitlement.expirationDate}"
                )
            }
        }

        val documentPath = "$CUSTOMERS_COLLECTION/$appUserId"

        val snapshot = try {
            firestore.collection(CUSTOMERS_COLLECTION).document(appUserId).get().await()
        } catch (e: Exception) {
            Log.e(
                TAG,
                "FIRESTORE: could not read $documentPath — check the Firestore rules deploy and " +
                    "that this build holds a valid App Check token",
                e
            )
            return
        }

        if (!snapshot.exists()) {
            Log.w(
                TAG,
                "FIRESTORE: $documentPath does not exist. Expected when the extension's customers " +
                    "collection is not set to '$CUSTOMERS_COLLECTION', when the shared secret was " +
                    "never saved, or when no RevenueCat event has been delivered yet."
            )
            return
        }

        val data = snapshot.data.orEmpty()
        Log.d(TAG, "FIRESTORE: $documentPath exists with ${data.size} top-level field(s)")

        // Dumped raw on purpose: this document is RevenueCat's schema, and seeing exactly
        // what the extension wrote is more useful than any guess at field names.
        data.keys.sorted().forEach { key ->
            Log.d(TAG, "  $key = ${render(data[key])}")
        }

        reportEntitlementComparison(activeEntitlements.keys, data)
    }

    /**
     * Logs the [`revenueCatEntitlements`][ENTITLEMENTS_CLAIM] custom claim from the ID token.
     *
     * A third, independent signal. The claim is served by Firebase Auth rather than
     * Firestore, so it verifies the extension's custom-claims path on its own — including
     * when the customers collection is misconfigured or unreadable. Claim present while
     * [CustomerInfo] reports an active entitlement is the strongest confirmation that the
     * RevenueCat -> Firebase sync is genuinely live.
     *
     * The token is force-refreshed (`getIdToken(true)`) deliberately: claims are stamped
     * onto the *next* token, so the cached one would predate the claim and report a false
     * negative at exactly the moment this check is most useful. That costs one Auth round
     * trip, which is why this whole diagnostic is debug-only.
     */
    private suspend fun logEntitlementClaims() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.d(TAG, "CLAIM : skipped — not signed in")
            return
        }

        val claims = try {
            user.getIdToken(true).await().claims
        } catch (e: Exception) {
            // Usually offline, or a revoked/expired refresh token.
            Log.e(TAG, "CLAIM : could not refresh the ID token", e)
            return
        }

        val entitlements = claims[ENTITLEMENTS_CLAIM]
        if (entitlements == null) {
            Log.w(
                TAG,
                "CLAIM : the ID token has no '$ENTITLEMENTS_CLAIM' claim. Expected before the " +
                    "extension's first sync, or when custom claims are not enabled on it."
            )
        } else {
            Log.d(TAG, "CLAIM : $ENTITLEMENTS_CLAIM = ${render(entitlements)}")
        }

        // The extension's claim shape is RevenueCat's, not ours. It can represent
        // entitlements as one blob under a single key or as one claim per entitlement, so
        // every non-standard key is listed rather than guessing at the layout. This is what
        // turns "the claim is missing" into "the claim is there, under this name".
        val customClaimKeys = claims.keys
            .filterNot { it in STANDARD_TOKEN_CLAIMS }
            .sorted()

        if (customClaimKeys.isEmpty()) {
            Log.w(
                TAG,
                "CLAIM : the token carries no custom claims at all — the extension is not " +
                    "writing claims for this user."
            )
            return
        }

        Log.d(TAG, "CLAIM : custom claim keys = $customClaimKeys")
        customClaimKeys
            .filter { it != ENTITLEMENTS_CLAIM }
            .forEach { key ->
                Log.d(TAG, "  $key = ${render(claims[key])}")
            }
    }

    /**
     * Best-effort comparison, and deliberately not an assertion: the extension snapshot
     * legitimately lags the SDK, and its field names belong to RevenueCat rather than to us.
     */
    private fun reportEntitlementComparison(
        sdkEntitlementIds: Set<String>,
        data: Map<String, Any?>,
    ) {
        val documentedIds = (data[DOCUMENT_ENTITLEMENTS_FIELD] as? Map<*, *>)
            ?.keys
            ?.map { it.toString() }
            ?.sorted()

        if (documentedIds == null) {
            Log.d(
                TAG,
                "COMPARE: the document has no '$DOCUMENT_ENTITLEMENTS_FIELD' map, so there is nothing " +
                    "to compare automatically. The raw fields above are the source of truth."
            )
            return
        }

        val sdkIds = sdkEntitlementIds.sorted()
        Log.d(TAG, "COMPARE: SDK active=$sdkIds  Firestore '$DOCUMENT_ENTITLEMENTS_FIELD'=$documentedIds")

        if (sdkIds == documentedIds) {
            Log.d(TAG, "VERDICT: consistent — the extension snapshot matches CustomerInfo")
        } else {
            Log.w(
                TAG,
                "VERDICT: DIFFERS — expected for a few seconds after a purchase, renewal, " +
                    "cancellation or transfer, because the extension is driven by a webhook. " +
                    "Re-run the check before assuming a fault."
            )
        }
    }

    private fun render(value: Any?): String {
        val text = when (value) {
            null -> "null"
            is Map<*, *> -> value.entries.joinToString(", ", "{", "}") { "${it.key}=${render(it.value)}" }
            is Iterable<*> -> value.joinToString(", ", "[", "]") { render(it) }
            else -> value.toString()
        }

        return if (text.length <= MAX_RENDERED_VALUE_LENGTH) {
            text
        } else {
            text.take(MAX_RENDERED_VALUE_LENGTH) + "... (truncated)"
        }
    }
}
