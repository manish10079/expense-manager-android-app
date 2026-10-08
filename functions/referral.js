const REFERRAL_DAYS = 7;
const REFERRAL_MONTHLY_CAP = 4;

function requireRegisteredUid(HttpsError, request) {
  const uid = request.auth?.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "Please sign in to continue");
  }
  const provider = request.auth.token?.firebase?.sign_in_provider;
  if (provider === "anonymous") {
    throw new HttpsError(
      "permission-denied",
      "Register with Google or email to get a referral code"
    );
  }
  return uid;
}

function referralCodeFromUid(uid) {
  const u = String(uid);
  if (u.length < 8) return u.toUpperCase();
  return (u.slice(0, 4) + u.slice(-4)).toUpperCase();
}

function utcMonthKey(nowMs) {
  const d = new Date(nowMs);
  return `${d.getUTCFullYear()}-${String(d.getUTCMonth() + 1).padStart(2, "0")}`;
}

function stackExpiry(existingExpiry, days, nowMs, durationMs) {
  return Math.max(nowMs, Number(existingExpiry) || 0) + days * durationMs;
}

function registerReferral({
  onCall,
  HttpsError,
  admin,
  db,
  activeSubscription,
  DURATION_MS_PER_DAY
}) {
  const ensureReferralCode = onCall({ enforceAppCheck: true }, async (request) => {
    const uid = requireRegisteredUid(HttpsError, request);
    const userRef = db.collection("users").doc(uid);

    return db.runTransaction(async (tx) => {
      const userSnap = await tx.get(userRef);
      const existing = userSnap.exists ? userSnap.data()?.referralCode : null;
      if (typeof existing === "string" && existing.length > 0) {
        const data = userSnap.data() || {};
        return {
          code: existing,
          successCount: Number(data.referralSuccessCount) || 0,
          rewardsThisMonth: Number(data.referralRewardsThisMonth) || 0,
          monthKey: data.referralRewardMonthKey || utcMonthKey(Date.now()),
          referredByUid: data.referredByUid || "",
          accountTier: data.accountTier || "",
          proExpiryTimestamp: Number(data.proExpiryTimestamp) || 0
        };
      }

      let code = referralCodeFromUid(uid);
      let codeRef = db.collection("referralCodes").doc(code);
      let codeSnap = await tx.get(codeRef);
      if (codeSnap.exists && codeSnap.data()?.uid !== uid) {
        code = `${code}${uid.length > 4 ? String(uid.charAt(4)).toUpperCase() : "X"}`;
        codeRef = db.collection("referralCodes").doc(code);
        codeSnap = await tx.get(codeRef);
        if (codeSnap.exists && codeSnap.data()?.uid !== uid) {
          throw new HttpsError("already-exists", "Could not reserve a referral code");
        }
      }
      if (!codeSnap.exists) {
        tx.set(codeRef, { uid, createdAt: admin.firestore.FieldValue.serverTimestamp() });
      }
      tx.set(userRef, { uid, referralCode: code, profileUpdatedAtMillis: Date.now() }, { merge: true });
      const created = userSnap.exists ? userSnap.data() || {} : {};
      return {
        code,
        successCount: 0,
        rewardsThisMonth: 0,
        monthKey: utcMonthKey(Date.now()),
        referredByUid: created.referredByUid || "",
        accountTier: created.accountTier || "",
        proExpiryTimestamp: Number(created.proExpiryTimestamp) || 0
      };
    });
  });

  const applyReferralCode = onCall({ enforceAppCheck: true }, async (request) => {
    const uid = requireRegisteredUid(HttpsError, request);
    const raw = typeof request.data?.code === "string" ? request.data.code : "";
    const code = raw.trim().toUpperCase();
    if (!code) {
      throw new HttpsError("invalid-argument", "Enter a referral code", { reason: "INVALID_CODE" });
    }

    const userRef = db.collection("users").doc(uid);
    const attrRef = db.collection("referralAttributions").doc(uid);
    const codeRef = db.collection("referralCodes").doc(code);
    const rcRef = db.collection("rc_customers").doc(uid);

    return db.runTransaction(async (tx) => {
      const [userSnap, attrSnap, codeSnap, rcSnap] = await Promise.all([
        tx.get(userRef),
        tx.get(attrRef),
        tx.get(codeRef),
        tx.get(rcRef)
      ]);

      if (!codeSnap.exists) {
        throw new HttpsError("not-found", "Unknown referral code", { reason: "INVALID_CODE" });
      }
      const referrerUid = codeSnap.data()?.uid;
      if (!referrerUid) {
        throw new HttpsError("not-found", "Unknown referral code", { reason: "INVALID_CODE" });
      }
      if (referrerUid === uid) {
        throw new HttpsError("failed-precondition", "You cannot use your own code", { reason: "OWN_CODE" });
      }
      if (attrSnap.exists || (userSnap.exists && userSnap.data()?.referredByUid)) {
        throw new HttpsError("already-exists", "You already used a referral code", {
          reason: "ALREADY_REFERRED"
        });
      }

      const now = Date.now();
      const sub = activeSubscription(rcSnap.exists ? rcSnap.data() : null);
      let welcomeDays = 0;
      let newExpiry = userSnap.exists ? Number(userSnap.data()?.proExpiryTimestamp) || 0 : 0;
      if (!sub) {
        welcomeDays = REFERRAL_DAYS;
        newExpiry = stackExpiry(newExpiry, REFERRAL_DAYS, now, DURATION_MS_PER_DAY);
        tx.set(
          userRef,
          {
            uid,
            referredByUid: referrerUid,
            referralWelcomeGranted: true,
            accountTier: "Pro_Pass",
            proExpiryTimestamp: newExpiry,
            profileUpdatedAtMillis: now
          },
          { merge: true }
        );
      } else {
        tx.set(
          userRef,
          {
            uid,
            referredByUid: referrerUid,
            referralWelcomeGranted: false,
            profileUpdatedAtMillis: now
          },
          { merge: true }
        );
      }

      tx.set(attrRef, {
        code,
        referrerUid,
        attributedAt: admin.firestore.FieldValue.serverTimestamp(),
        qualifiedAt: null,
        referrerRewarded: false,
        welcomeGranted: welcomeDays > 0
      });

      return { welcomeDays, newExpiry, grantSkipped: Boolean(sub) };
    });
  });

  const qualifyReferral = onCall({ enforceAppCheck: true }, async (request) => {
    const uid = requireRegisteredUid(HttpsError, request);
    const attrRef = db.collection("referralAttributions").doc(uid);

    return db.runTransaction(async (tx) => {
      const attrSnap = await tx.get(attrRef);
      if (!attrSnap.exists) {
        return { rewarded: false, already: true, rewardsUsedThisMonth: 0 };
      }
      const attr = attrSnap.data() || {};
      if (attr.qualifiedAt) {
        return { rewarded: Boolean(attr.referrerRewarded), already: true, rewardsUsedThisMonth: 0 };
      }
      const referrerUid = attr.referrerUid;
      if (!referrerUid) {
        throw new HttpsError("failed-precondition", "Invalid attribution", { reason: "UNKNOWN" });
      }

      const referrerRef = db.collection("users").doc(referrerUid);
      const rcRef = db.collection("rc_customers").doc(referrerUid);
      const [referrerSnap, rcSnap] = await Promise.all([tx.get(referrerRef), tx.get(rcRef)]);
      const now = Date.now();
      const key = utcMonthKey(now);
      const data = referrerSnap.exists ? referrerSnap.data() || {} : {};
      let used = Number(data.referralRewardsThisMonth) || 0;
      if (data.referralRewardMonthKey !== key) used = 0;

      let rewarded = false;
      let newExpiry = Number(data.proExpiryTimestamp) || 0;
      const sub = activeSubscription(rcSnap.exists ? rcSnap.data() : null);
      const underCap = used < REFERRAL_MONTHLY_CAP;
      if (underCap) used += 1;
      if (underCap && !sub) {
        rewarded = true;
        newExpiry = stackExpiry(newExpiry, REFERRAL_DAYS, now, DURATION_MS_PER_DAY);
      }
      const patch = {
        uid: referrerUid,
        referralRewardsThisMonth: used,
        referralRewardMonthKey: key,
        referralSuccessCount: admin.firestore.FieldValue.increment(1),
        profileUpdatedAtMillis: now
      };
      if (rewarded) {
        patch.accountTier = "Pro_Pass";
        patch.proExpiryTimestamp = newExpiry;
      }
      tx.set(referrerRef, patch, { merge: true });

      tx.set(
        attrRef,
        {
          qualifiedAt: admin.firestore.FieldValue.serverTimestamp(),
          referrerRewarded: rewarded
        },
        { merge: true }
      );

      return { rewarded, already: false, rewardsUsedThisMonth: used, newExpiry };
    });
  });

  const listMyReferrals = onCall({ enforceAppCheck: true }, async (request) => {
    const uid = requireRegisteredUid(HttpsError, request);
    const snap = await db.collection("referralAttributions").where("referrerUid", "==", uid).get();
    const items = [];
    for (const doc of snap.docs) {
      const d = doc.data() || {};
      const refereeUid = doc.id;
      let displayName = "Friend";
      const userSnap = await db.collection("users").doc(refereeUid).get();
      if (userSnap.exists) {
        const name = userSnap.data()?.fullName;
        if (typeof name === "string" && name.trim()) {
          displayName = name.trim().split(/\s+/)[0];
        }
      }
      const attributedAt = d.attributedAt && typeof d.attributedAt.toMillis === "function"
        ? d.attributedAt.toMillis()
        : 0;
      const qualified = Boolean(d.qualifiedAt);
      items.push({
        refereeUid,
        displayName,
        attributedAt,
        qualified,
        rewarded: Boolean(d.referrerRewarded)
      });
    }
    items.sort((a, b) => b.attributedAt - a.attributedAt);
    return { items };
  });

  return { ensureReferralCode, applyReferralCode, qualifyReferral, listMyReferrals };
}

module.exports = { registerReferral, referralCodeFromUid };
