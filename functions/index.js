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
