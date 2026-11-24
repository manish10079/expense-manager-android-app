/**
 * revenuecatWebhook.js — Real-time server-to-server webhook handler for RevenueCat.
 *
 * RevenueCat invokes this HTTPS endpoint whenever a customer's subscription status
 * changes (purchase, renewal, cancellation, expiration, revocation).
 *
 * This writes directly to `users/{uid}` with the Firebase Admin SDK, keeping
 * `accountTier` up to date without needing `rc_customers/{uid}` or the Firebase
 * Extension.
 *
 * Requires `REVENUECAT_WEBHOOK_SECRET` in Secret Manager. RevenueCat must be configured
 * with the header: `Authorization: Bearer <secret>`.
 */
const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");

const revenueCatWebhookSecret = defineSecret("REVENUECAT_WEBHOOK_SECRET");
const PREMIUM_ENTITLEMENT_ID = "premium";

function db() {
  return admin.firestore();
}

/**
 * Extracts the entitlement IDs affected by the event.
 */
function getEntitlementIds(event) {
  if (Array.isArray(event.entitlement_ids) && event.entitlement_ids.length > 0) {
    return event.entitlement_ids;
  }
  if (typeof event.entitlement_id === "string" && event.entitlement_id.trim()) {
    return [event.entitlement_id.trim()];
  }
  return [];
}

/**
 * Validates the Authorization header against REVENUECAT_WEBHOOK_SECRET.
 */
function isAuthorized(req) {
  let secret = "";
  try {
    secret = revenueCatWebhookSecret.value();
    secret = typeof secret === "string" ? secret.trim() : "";
  } catch {
    secret = "";
  }

  if (!secret) {
    console.error("RevenueCat Webhook: REVENUECAT_WEBHOOK_SECRET is not configured");
    return false;
  }

  const header = req.headers.authorization || "";
  return header === `Bearer ${secret}`;
}

const revenueCatWebhook = onRequest(
  {
    region: "us-central1",
    secrets: [revenueCatWebhookSecret]
  },
  async (req, res) => {
    // 1. Only accept POST
    if (req.method !== "POST") {
      res.status(405).send("Method Not Allowed");
      return;
    }

    // 2. Validate Authorization
    if (!isAuthorized(req)) {
      res.status(401).send("Unauthorized");
      return;
    }

    // 3. Parse Event payload
    const body = req.body;
    const event = body && typeof body === "object" ? body.event : null;
    if (!event || typeof event !== "object") {
      res.status(400).send("Bad Request: Missing event object");
      return;
    }

    const eventType = event.type;
    const uid = event.app_user_id;

    if (!uid || typeof uid !== "string") {
      console.warn("RevenueCat Webhook: Received event without valid app_user_id");
      res.status(200).send("OK: Missing app_user_id");
      return;
    }

    // Skip anonymous RevenueCat generated IDs (user not logged in with Firebase UID yet)
    if (uid.startsWith("$RCAnonymousID:")) {
      console.log(`RevenueCat Webhook: Skipping anonymous RevenueCat user ${uid}`);
      res.status(200).send("OK: Anonymous user");
      return;
    }

    const entitlementIds = getEntitlementIds(event);
    // If entitlement info is present but does NOT include premium, ignore it
    if (entitlementIds.length > 0 && !entitlementIds.includes(PREMIUM_ENTITLEMENT_ID)) {
      console.log(
        `RevenueCat Webhook: Event ${eventType} for ${uid} does not affect ${PREMIUM_ENTITLEMENT_ID} (found: ${entitlementIds.join(", ")})`
      );
      res.status(200).send("OK: Irrelevant entitlement");
      return;
    }

    console.log(`RevenueCat Webhook: Handling ${eventType} for user ${uid}`);

    const userRef = db().collection("users").doc(uid);
    const now = Date.now();

    try {
      switch (eventType) {
        // Active subscription grant / renewal / recovery
        case "INITIAL_PURCHASE":
        case "RENEWAL":
        case "UNCANCELLATION":
        case "NON_RENEWING_PURCHASE":
        case "SUBSCRIPTION_EXTENDED": {
          await userRef.set(
            {
              accountTier: "Paid_Subscription",
              proExpiryTimestamp: 0,
              referralWelcomeGranted: false,
              profileUpdatedAtMillis: now
            },
            { merge: true }
          );
          console.log(`RevenueCat Webhook: Set accountTier = Paid_Subscription for ${uid}`);
          break;
        }

        // Subscription access ended (billing period elapsed or refunded)
        case "EXPIRATION":
        case "REVOCATION": {
          const userSnap = await userRef.get();
          if (userSnap.exists) {
            const currentTier = userSnap.data()?.accountTier;
            // Only downgrade if the user was on Paid_Subscription.
            // If they are on a valid Pro_Pass, leave their pass intact.
            if (currentTier === "Paid_Subscription") {
              await userRef.set(
                {
                  accountTier: "Free",
                  profileUpdatedAtMillis: now
                },
                { merge: true }
              );
              console.log(`RevenueCat Webhook: Downgraded ${uid} from Paid_Subscription to Free`);
            }
          }
          break;
        }

        // Auto-renew cancelled in store, but user still has access until period ends
        case "CANCELLATION": {
          console.log(
            `RevenueCat Webhook: User ${uid} cancelled auto-renew. Access remains active until expiration.`
          );
          break;
        }

        // Payment issue / grace period
        case "BILLING_ISSUE": {
          console.warn(`RevenueCat Webhook: Billing issue reported for user ${uid}`);
          break;
        }

        default: {
          console.log(`RevenueCat Webhook: Unhandled event type: ${eventType}`);
          break;
        }
      }

      res.status(200).send("OK");
    } catch (err) {
      console.error(`RevenueCat Webhook: Error processing ${eventType} for ${uid}:`, err);
      // Respond with 500 so RevenueCat will retry the webhook delivery
      res.status(500).send("Internal Server Error");
    }
  }
);

module.exports = {
  revenueCatWebhookSecret,
  revenueCatWebhook
};
