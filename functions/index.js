/**
 * ExpenseTracker — Cloud Functions
 *
 * Security hardening (see implementation_plans/security_implementation_plan.md,
 * Items 14/22): ProPass redemption is moved server-side so the premium fields
 * (accountTier / proExpiryTimestamp / isSubscription) can no longer be
 * self-granted by a modified client. The Admin SDK bypasses Firestore rules,
 * making this function the ONLY trusted writer of those fields.
 *
 * Store subscriptions and Pro Passes grant the same access, so they must not run side
 * by side:
 *  - `redeemProPass` refuses a code while a subscription is active. The client blocks
 *    this in the dialog; this is the authoritative check.
 *  - `onRcCustomerWritten` retires a running pass when a subscription starts.
 *  - `expireProPasses` retires passes whose granted days have run out.
 * Both pass writers only ever REMOVE access. `premium` itself stays owned by
 * RevenueCat, and the app reads it from the billing SDK, never from Firestore.
 */
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

// Import parseVoiceTransaction function
const { parseVoiceTransaction } = require("./parseVoiceTransaction");
exports.parseVoiceTransaction = parseVoiceTransaction;

admin.initializeApp();

const db = admin.firestore();
const DURATION_MS_PER_DAY = 24 * 60 * 60 * 1000;

/** Must match BillingManager.PREMIUM_ENTITLEMENT_ID in the app. */
const PREMIUM_ENTITLEMENT_ID = "premium";

/**
 * Converts one of RevenueCat's date fields to epoch millis.
 *
 * The RevenueCat Firebase Extension stores RevenueCat's payload verbatim, so the same
 * field can arrive as an ISO-8601 string, a Firestore Timestamp, a Date or a number
 * (epoch seconds or millis) depending on the extension version. Returns 0 when the
 * value is missing or cannot be read.
 */
function rcDateToMillis(value) {
  if (value === null || value === undefined) return 0;
  if (value instanceof admin.firestore.Timestamp) return value.toMillis();
  if (value instanceof Date) return value.getTime();
  if (typeof value === "number") return value > 1e11 ? value : value * 1000;
  if (typeof value === "string") {
    const parsed = Date.parse(value);
    return Number.isNaN(parsed) ? 0 : parsed;
  }
  return 0;
}

/**
 * Reads the live subscription entitlement from an `rc_customers/{uid}` document — the
 * snapshot the RevenueCat Firebase Extension maintains for each customer.
 *
 * Returns `{ expiresMillis }` when the `premium` entitlement is live, where 0 means the
 * entitlement carries no expiry (RevenueCat omits `expires_date` for entitlements that
 * never expire). Returns null when there is no such entitlement, when it has expired,
 * or when its date cannot be read.
 *
 * The unreadable case returns null and warns instead of guessing: a date this function
 * cannot parse means the extension's document shape has changed, and neither caller
 * should act on a guess. Access is not lost either way, because the app reads `premium`
 * straight from the billing SDK rather than from this document.
 */
function activeSubscription(rcData) {
  if (!rcData || typeof rcData !== "object") return null;

  const entitlements = rcData.entitlements;
  if (!entitlements || typeof entitlements !== "object") return null;

  const premium = entitlements[PREMIUM_ENTITLEMENT_ID];
  if (!premium || typeof premium !== "object") return null;

  const rawExpiry = premium.expires_date ?? premium.expiresDate ?? null;
  const expiresMillis = rcDateToMillis(rawExpiry);

  if (rawExpiry !== null && expiresMillis === 0) {
    console.warn(
      `rc_customers: cannot read ${PREMIUM_ENTITLEMENT_ID}.expires_date ` +
        `(value: ${JSON.stringify(rawExpiry)}); treating the subscription as inactive`
    );
    return null;
  }
  if (expiresMillis > 0 && expiresMillis < Date.now()) return null;

  return { identifier: PREMIUM_ENTITLEMENT_ID, expiresMillis };
}

/**
 * Redeems a ProPass coupon atomically (read + validate + increment + grant).
 * Mirrors the old client-side ProPassRepositoryImpl.redeemCode behaviour but
 * closes the TOCTOU race (two devices over-redeeming past maxUses) and makes
 * accountTier / proExpiryTimestamp server-authoritative.
 *
 * Request data: { code: string }
 * Returns:      { durationDays: number, newExpiry: number (epoch millis) }
 *
 * Error codes:
 *  - unauthenticated      not signed in
 *  - permission-denied    anonymous/guest account
 *  - invalid-argument     empty code
 *  - not-found            unknown coupon
 *  - already-exists       this user already redeemed this coupon
 *  - failed-precondition  an active store subscription (carries
 *                         `details.reason = "SUBSCRIPTION_ACTIVE"` plus the
 *                         subscription's `details.subscriptionExpiry` in epoch millis),
 *                         or an inactive / expired / used-up coupon. The coupon refusals
 *                         carry `details.reason` as well — INVALID_CODE, INACTIVE, EXPIRED,
 *                         LIMIT_REACHED — because the code alone cannot tell them apart.
 */
exports.redeemProPass = onCall({ enforceAppCheck: true }, async (request) => {
  // --- Auth gate (mirrors the old app check) ---
  const uid = request.auth?.uid;
  if (!uid) {
    throw new HttpsError(
      "unauthenticated",
      "Please sign in with Google or Email to redeem a ProPass"
    );
  }
  const provider = request.auth.token?.firebase?.sign_in_provider;
  if (provider === "anonymous") {
    throw new HttpsError(
      "permission-denied",
      "Please sign in with Google or Email to redeem a ProPass"
    );
  }

  // --- Input validation (mirrors the old app check) ---
  const rawCode = request.data?.code;
  if (typeof rawCode !== "string" || rawCode.trim().length === 0) {
    throw new HttpsError("invalid-argument", "Code cannot be empty", {
      reason: "INVALID_CODE"
    });
  }
  const code = rawCode.trim().toUpperCase();

  const couponRef = db.collection("coupons").doc(code);
  const redemptionRef = couponRef.collection("redemptions").doc(uid);
  const userRef = db.collection("users").doc(uid);
  // Read-only: the RevenueCat Firebase Extension is this document's only writer.
  const rcCustomerRef = db.collection("rc_customers").doc(uid);

  // One transaction: every read happens before any write, so Firestore can
  // guarantee atomicity and the increment can never overshoot maxUses.
  const result = await db.runTransaction(async (tx) => {
    // Subscription gate. A subscription already grants Pro, so redeeming a pass on top
    // would spend its days on access the user is paying for. Checked before the coupon
    // so a subscriber always hears the real reason, whatever code they typed.
    const rcSnap = await tx.get(rcCustomerRef);
    const subscription = activeSubscription(rcSnap.exists ? rcSnap.data() : null);
    if (subscription) {
      throw new HttpsError(
        "failed-precondition",
        "You already have an active subscription",
        {
          reason: "SUBSCRIPTION_ACTIVE",
          subscriptionExpiry: subscription.expiresMillis
        }
      );
    }

    const couponSnap = await tx.get(couponRef);
    if (!couponSnap.exists) {
      throw new HttpsError("not-found", "Invalid ProPass code");
    }
    const coupon = couponSnap.data();

    const isActive = coupon.isActive === true;
    const durationDays = Number(coupon.durationDays ?? 0);
    const maxUses = Number(coupon.maxUses ?? 0);
    const currentUses = Number(coupon.currentUses ?? 0);
    const expiryMillis =
      coupon.expiryTimestamp instanceof admin.firestore.Timestamp
        ? coupon.expiryTimestamp.toMillis()
        : Number(coupon.expiryTimestamp) || 0;
    const repeatAllowedUids = Array.isArray(coupon.repeatAllowedUids)
      ? coupon.repeatAllowedUids.map(String)
      : [];

    if (!isActive) {
      throw new HttpsError("failed-precondition", "This ProPass code is no longer active", {
        reason: "INACTIVE"
      });
    }
    if (durationDays <= 0) {
      throw new HttpsError("failed-precondition", "Invalid ProPass code", {
        reason: "INVALID_CODE"
      });
    }
    if (expiryMillis > 0 && expiryMillis < Date.now()) {
      throw new HttpsError("failed-precondition", "This ProPass code has expired", {
        reason: "EXPIRED"
      });
    }

    const redemptionSnap = await tx.get(redemptionRef);
    const alreadyRedeemed = redemptionSnap.exists && !repeatAllowedUids.includes(uid);
    if (alreadyRedeemed) {
      throw new HttpsError("already-exists", "You have already redeemed this ProPass code");
    }
    if (currentUses >= maxUses) {
      throw new HttpsError("failed-precondition", "This ProPass code has reached its usage limit", {
        reason: "LIMIT_REACHED"
      });
    }

    // Existing premium is extended, not overwritten (matches old app behaviour).
    const userSnap = await tx.get(userRef);
    const existingExpiry = userSnap.exists ? Number(userSnap.data()?.proExpiryTimestamp) || 0 : 0;

    const newExpiry = Math.max(Date.now(), existingExpiry) + durationDays * DURATION_MS_PER_DAY;

    // All mutations land atomically:
    tx.update(couponRef, { currentUses: admin.firestore.FieldValue.increment(1) });
    tx.set(redemptionRef, {
      redeemedAt: admin.firestore.FieldValue.serverTimestamp(),
      userId: uid
    });
    tx.set(
      userRef,
      {
        uid,
        accountTier: "PREMIUM",
        proExpiryTimestamp: newExpiry,
        profileUpdatedAtMillis: Date.now()
      },
      { merge: true }
    );

    return { durationDays, newExpiry };
  });

  return result;
});

/**
 * Sends a data-only FCM push notification to active devices registered under a user's UID.
 * Option to exclude a specific deviceId (e.g. newly registered device).
 * Automatically cleans up stale / invalid FCM registration tokens.
 */
async function sendPushToUser(uid, payload, excludeDeviceId = null) {
  const tokensSnap = await db.collection("users").doc(uid).collection("fcmTokens").get();
  if (tokensSnap.empty) return;

  const tokens = [];
  const tokenDocRefs = [];
  tokensSnap.forEach((doc) => {
    if (excludeDeviceId && doc.id === excludeDeviceId) {
      return; // Exclude self-device from receiving the alert
    }
    const token = doc.data()?.token;
    if (token) {
      tokens.push(token);
      tokenDocRefs.push(doc.ref);
    }
  });

  if (tokens.length === 0) return;

  const message = {
    tokens,
    data: {
      type: String(payload.type || "generic"),
      title: String(payload.title || ""),
      body: String(payload.body || "")
    }
  };

  const response = await admin.messaging().sendEachForMulticast(message);
  
  // Cleanup stale/expired tokens returned by FCM
  const deleteBatch = db.batch();
  let deleteCount = 0;
  response.responses.forEach((resp, idx) => {
    if (!resp.success) {
      const errCode = resp.error?.code;
      if (
        errCode === "messaging/invalid-registration-token" ||
        errCode === "messaging/registration-token-not-registered"
      ) {
        deleteBatch.delete(tokenDocRefs[idx]);
        deleteCount++;
      }
    }
  });

  if (deleteCount > 0) {
    await deleteBatch.commit();
  }
}

const { onDocumentCreated, onDocumentUpdated, onDocumentWritten } = require("firebase-functions/v2/firestore");

/**
 * Firestore trigger: Sends a Cloud & Security FCM Push to existing devices when a new device connects.
 * Excludes the newly connected device itself from receiving the notification.
 */
exports.onFcmTokenCreated = onDocumentCreated({ document: "users/{uid}/fcmTokens/{deviceId}", region: "us-central1" }, async (event) => {
  const uid = event.params.uid;
  const deviceId = event.params.deviceId;
  const data = event.data?.data();

  if (!data) return;

  await sendPushToUser(
    uid,
    {
      type: "cloud_security",
      title: "New Device Connected",
      body: `A new device (${data.model || "Mobile Device"}) signed in to your account.`
    },
    deviceId // Exclude self-device
  );
});

/**
 * Firestore trigger: Sends a Cloud & Security FCM Push when key profile security fields change (e.g. email or auth profile update).
 */
exports.onUserProfileSecurityUpdated = onDocumentUpdated({ document: "users/{uid}", region: "us-central1" }, async (event) => {
  const before = event.data?.before?.data();
  const after = event.data?.after?.data();

  if (!before || !after) return;

  // Check if email or primary profile identity changed
  if (before.email !== after.email && after.email) {
    await sendPushToUser(event.params.uid, {
      type: "cloud_security",
      title: "Account Security Alert",
      body: `Your account email was updated to ${after.email}.`
    });
  }
});

/**
 * Firestore trigger: retires a Pro Pass when a store subscription takes over.
 *
 * A subscription already grants Pro, so pass days running alongside it would be spent
 * on access the user is already paying for. The grant is therefore ended rather than
 * preserved: when the subscription later lapses, the pass does not resume.
 *
 * Only ever REMOVES access — it never grants, so a replay or a forged snapshot cannot
 * upgrade anyone — and it is idempotent: a second run finds accountTier already FREE
 * and returns.
 *
 * The `coupons/{CODE}/redemptions/{uid}` audit record is left alone because
 * `users/{uid}` does not record which coupon was redeemed; see D1 in
 * implementation_plans/PRO_ACCESS_PREMIUM_SYSTEM_PLAN.md for the richer grant document.
 */
exports.onRcCustomerWritten = onDocumentWritten(
  { document: "rc_customers/{uid}", region: "us-central1" },
  async (event) => {
    const after = event.data?.after;
    if (!after?.exists) return;

    if (!activeSubscription(after.data())) return;

    const uid = event.params.uid;
    const userRef = db.collection("users").doc(uid);
    const userSnap = await userRef.get();
    if (!userSnap.exists) return;

    const user = userSnap.data();
    const passExpiry = Number(user.proExpiryTimestamp ?? 0);
    // 0 is a permanent grant; a past expiry has already lapsed and belongs to
    // expireProPasses.
    const hasRunningPass =
      user.accountTier === "PREMIUM" && (passExpiry === 0 || passExpiry > Date.now());
    if (!hasRunningPass) return;

    await userRef.set(
      {
        accountTier: "FREE",
        proExpiryTimestamp: 0,
        profileUpdatedAtMillis: Date.now()
      },
      { merge: true }
    );

    console.log(`Pro Pass retired for ${uid}: a store subscription is now active`);
  }
);

const { onSchedule } = require("firebase-functions/v2/scheduler");

/**
 * Phase C: Scheduled Financial Insights Engine (PubSub Weekly Cron).
 * Runs every Sunday at 12:00 PM UTC.
 * Analyzes user transactions in Firestore for spending trends, category spikes, and budget risk.
 * Enforces server-side premium checks (accountTier === "PREMIUM").
 */
exports.scheduledFinancialInsights = onSchedule(
  {
    schedule: "0 12 * * 0",
    timeZone: "UTC",
    region: "us-central1"
  },
  async () => {
    const usersSnap = await db.collection("users").get();
    if (usersSnap.empty) return;

    const now = Date.now();
    const SEVEN_DAYS_MS = 7 * 24 * 60 * 60 * 1000;
    const currentPeriodStart = now - SEVEN_DAYS_MS;
    const previousPeriodStart = now - 2 * SEVEN_DAYS_MS;

    for (const userDoc of usersSnap.docs) {
      const userData = userDoc.data();
      const uid = userDoc.id;

      // Server-side Premium Enforcement
      const isPremium =
        userData.accountTier === "PREMIUM" &&
        Number(userData.proExpiryTimestamp ?? 0) > now;

      if (!isPremium) continue;

      try {
        // Query user's recent transactions
        const txSnap = await db
          .collection("users")
          .doc(uid)
          .collection("transactions")
          .where("timestamp", ">=", previousPeriodStart)
          .get();

        if (txSnap.empty) continue;

        let currentWeekTotal = 0;
        let previousWeekTotal = 0;
        const categoryTotals = {};

        txSnap.forEach((doc) => {
          const tx = doc.data();
          if (tx.isExpense === false) return; // Skip income

          const txTime = Number(tx.timestamp) || 0;
          const amount = Math.abs(Number(tx.amount) || 0);

          if (txTime >= currentPeriodStart) {
            currentWeekTotal += amount;
            const category = tx.categoryName || "General";
            categoryTotals[category] = (categoryTotals[category] || 0) + amount;
          } else {
            previousWeekTotal += amount;
          }
        });

        if (currentWeekTotal === 0) continue;

        let insightTitle = "Weekly Financial Insight";
        let insightBody = "";

        if (previousWeekTotal > 0) {
          const diffPct = Math.round(((currentWeekTotal - previousWeekTotal) / previousWeekTotal) * 100);
          if (diffPct > 15) {
            insightTitle = "Spending Spike Alert";
            insightBody = `Your spending rose by ${diffPct}% this week compared to last week. Tap to review insights.`;
          } else if (diffPct < -10) {
            insightTitle = "Great Savings This Week!";
            insightBody = `You spent ${Math.abs(diffPct)}% less this week compared to last week. Keep it up!`;
          } else {
            insightBody = `You spent $${currentWeekTotal.toFixed(2)} this week across your active categories.`;
          }
        } else {
          insightBody = `Weekly recap: Total expense recorded is $${currentWeekTotal.toFixed(2)}.`;
        }

        await sendPushToUser(uid, {
          type: "financial_insight",
          title: insightTitle,
          body: insightBody
        });
      } catch (err) {
        console.error(`Error computing financial insights for ${uid}:`, err);
      }
    }
  }
);

/**
 * Daily sweep: retires Pro Passes whose granted days have run out.
 *
 * The app already downgrades itself from its local mirror (ProExpiryResolver) and
 * SyncRepositoryImpl pushes accountTier = "FREE" for an expired user. This job is the
 * backstop that keeps the *server* honest for users who never come back — without it a
 * lapsed pass keeps passing scheduledFinancialInsights' accountTier === "PREMIUM" check
 * and collects premium insights forever.
 *
 * Only passes are affected: accountTier = "PREMIUM" is written exclusively by
 * redeemProPass (the store never writes these fields), so a paying subscriber cannot be
 * downgraded here.
 *
 * Filtering runs in-process over a single-field equality query, so no composite index
 * is required.
 */
exports.expireProPasses = onSchedule(
  { schedule: "30 3 * * *", timeZone: "UTC", region: "us-central1" },
  async () => {
    const now = Date.now();
    const premiumSnap = await db
      .collection("users")
      .where("accountTier", "==", "PREMIUM")
      .get();

    if (premiumSnap.empty) return;

    let batch = db.batch();
    let pending = 0;
    let retired = 0;

    for (const userDoc of premiumSnap.docs) {
      const expiry = Number(userDoc.data().proExpiryTimestamp ?? 0);
      // 0 means the grant never expires (EntitlementResolver reads it as permanent).
      if (expiry === 0 || expiry >= now) continue;

      batch.set(
        userDoc.ref,
        { accountTier: "FREE", proExpiryTimestamp: 0, profileUpdatedAtMillis: now },
        { merge: true }
      );
      pending++;
      retired++;

      // Firestore caps a batch at 500 writes.
      if (pending === 400) {
        await batch.commit();
        batch = db.batch();
        pending = 0;
      }
    }

    if (pending > 0) await batch.commit();
    console.log(`expireProPasses: retired ${retired} Pro Pass(es)`);
  }
);



