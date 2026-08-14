/**
 * ExpenseTracker — Cloud Functions
 *
 * Security hardening (see implementation_plans/security_implementation_plan.md,
 * Items 14/22): ProPass redemption is moved server-side so the premium fields
 * (accountTier / proExpiryTimestamp / isSubscription) can no longer be
 * self-granted by a modified client. The Admin SDK bypasses Firestore rules,
 * making this function the ONLY trusted writer of those fields.
 */
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();
const DURATION_MS_PER_DAY = 24 * 60 * 60 * 1000;

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
 *  - failed-precondition  inactive / expired / usage limit reached
 */
exports.redeemProPass = onCall(async (request) => {
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
    throw new HttpsError("invalid-argument", "Code cannot be empty");
  }
  const code = rawCode.trim().toUpperCase();

  const couponRef = db.collection("coupons").doc(code);
  const redemptionRef = couponRef.collection("redemptions").doc(uid);
  const userRef = db.collection("users").doc(uid);

  // One transaction: every read happens before any write, so Firestore can
  // guarantee atomicity and the increment can never overshoot maxUses.
  const result = await db.runTransaction(async (tx) => {
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
      throw new HttpsError("failed-precondition", "This ProPass code is no longer active");
    }
    if (durationDays <= 0) {
      throw new HttpsError("failed-precondition", "Invalid ProPass code");
    }
    if (expiryMillis > 0 && expiryMillis < Date.now()) {
      throw new HttpsError("failed-precondition", "This ProPass code has expired");
    }

    const redemptionSnap = await tx.get(redemptionRef);
    const alreadyRedeemed = redemptionSnap.exists && !repeatAllowedUids.includes(uid);
    if (alreadyRedeemed) {
      throw new HttpsError("already-exists", "You have already redeemed this ProPass code");
    }
    if (currentUses >= maxUses) {
      throw new HttpsError("failed-precondition", "This ProPass code has reached its usage limit");
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

const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");

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



