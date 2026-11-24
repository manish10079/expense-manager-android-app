/**
 * revenuecat.js — live subscription state straight from RevenueCat's REST API.
 *
 * The backend no longer reads `rc_customers/{uid}` (the snapshot the RevenueCat
 * Firebase Extension maintains) to decide whether a user is subscribed. It asks
 * RevenueCat instead, so a subscription answer never depends on a webhook having
 * landed in Firestore first.
 *
 * The lookup works because the app signs the customer into RevenueCat with the
 * Firebase UID (BillingManager calls Purchases.logIn(firebaseUid)), so the
 * RevenueCat app user id is always the Firebase UID — the same id `request.auth.uid`
 * gives every callable.
 *
 * Requires the `REVENUECAT_SECRET_KEY` secret (Secret Manager). Any function that
 * calls into this module must declare `secrets: [revenueCatSecretKey]`.
 */
const { defineSecret } = require("firebase-functions/params");

const revenueCatSecretKey = defineSecret("REVENUECAT_SECRET_KEY");

/** Must match BillingManager.PREMIUM_ENTITLEMENT_ID in the app. */
const PREMIUM_ENTITLEMENT_ID = "premium";

const SUBSCRIBERS_URL = "https://api.revenuecat.com/v1/subscribers";
const REQUEST_TIMEOUT_MS = 4000;
/** Short enough that a purchase/cancellation is seen promptly, long enough to absorb
 *  the two quota lookups one parseExpenseText request makes. */
const CACHE_TTL_MS = 60 * 1000;
const CACHE_MAX_ENTRIES = 500;

/** uid -> { at, value }. Only confirmed answers are cached; failures never are. */
const cache = new Map();
/** uid -> Promise, so concurrent callers share one request. */
const inFlight = new Map();

/**
 * Reads a RevenueCat date field as epoch millis.
 *
 * RevenueCat's REST API returns ISO-8601 strings, but the same field reaches us
 * from varying shapes, so numbers (epoch seconds or millis) and Date objects are
 * accepted too. Returns 0 when the value is missing or unreadable.
 */
function dateToMillis(value) {
  if (value === null || value === undefined) return 0;
  if (value instanceof Date) return value.getTime();
  if (typeof value === "number") return value > 1e11 ? value : value * 1000;
  if (typeof value === "string") {
    const parsed = Date.parse(value);
    return Number.isNaN(parsed) ? 0 : parsed;
  }
  return 0;
}

function secretOrEmpty() {
  try {
    const value = revenueCatSecretKey.value();
    return typeof value === "string" && value.trim() ? value.trim() : "";
  } catch {
    // Not bound to this function (missing `secrets: [revenueCatSecretKey]`).
    return "";
  }
}

function readCache(uid) {
  const hit = cache.get(uid);
  if (!hit) return undefined;
  if (Date.now() - hit.at > CACHE_TTL_MS) {
    cache.delete(uid);
    return undefined;
  }
  return hit.value;
}

function writeCache(uid, value) {
  const now = Date.now();
  cache.set(uid, { at: now, value });
  // Map preserves insertion order, so the first key is the oldest entry.
  while (cache.size > CACHE_MAX_ENTRIES) {
    cache.delete(cache.keys().next().value);
  }
}

/**
 * Turns a RevenueCat subscriber payload into the same shape the rest of the
 * backend already uses for a live subscription.
 *
 * Returns `{ identifier, expiresMillis }` when the `premium` entitlement is live,
 * where 0 means the entitlement carries no expiry (RevenueCat omits `expires_date`
 * for entitlements that never expire). Returns null when there is no such
 * entitlement, when it has expired, or when its date cannot be read.
 */
function activeFromSubscriber(subscriber) {
  const entitlements = subscriber && subscriber.entitlements;
  if (!entitlements || typeof entitlements !== "object") return null;

  const premium = entitlements[PREMIUM_ENTITLEMENT_ID];
  if (!premium || typeof premium !== "object") return null;

  const rawExpiry = premium.expires_date ?? premium.expiresDate ?? null;
  const expiresMillis = dateToMillis(rawExpiry);

  if (rawExpiry !== null && expiresMillis === 0) {
    console.warn(
      `RevenueCat: cannot read ${PREMIUM_ENTITLEMENT_ID}.expires_date ` +
        `(value: ${JSON.stringify(rawExpiry)}); treating the subscription as inactive`
    );
    return null;
  }
  if (expiresMillis > 0 && expiresMillis < Date.now()) return null;

  return { identifier: PREMIUM_ENTITLEMENT_ID, expiresMillis };
}

async function fetchSubscriber(uid, apiKey) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  try {
    const response = await fetch(
      `${SUBSCRIBERS_URL}/${encodeURIComponent(uid)}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${apiKey}`,
          "Content-Type": "application/json"
        },
        signal: controller.signal
      }
    );

    if (response.status === 404) {
      // Never purchased / never seen by RevenueCat.
      return { ok: true, subscriber: null };
    }
    if (!response.ok) {
      console.error(`RevenueCat: subscribers/${uid} returned HTTP ${response.status}`);
      return { ok: false };
    }
    const payload = await response.json().catch(() => null);
    if (!payload || typeof payload !== "object") {
      console.error(`RevenueCat: subscribers/${uid} returned an unreadable body`);
      return { ok: false };
    }
    return { ok: true, subscriber: payload.subscriber || null };
  } catch (error) {
    console.error(
      `RevenueCat: subscribers/${uid} lookup failed:`,
      error && (error.name === "AbortError" ? "timeout" : error.message)
    );
    return { ok: false };
  } finally {
    clearTimeout(timer);
  }
}

/**
 * Live subscription state for a Firebase UID, read from RevenueCat.
 *
 * Resolves to `{ identifier, expiresMillis }` when the `premium` entitlement is
 * active, otherwise null. Every failure mode — no key configured, HTTP error,
 * timeout, unreadable payload — also resolves to null, because a caller must be
 * able to answer without RevenueCat: the app itself takes Pro straight from the
 * billing SDK, so nothing here can take access away from a paying user. The
 * failures are logged rather than silent.
 */
async function getActiveSubscription(uid) {
  if (!uid) return null;

  const cached = readCache(uid);
  if (cached !== undefined) return cached;

  const pending = inFlight.get(uid);
  if (pending) return pending;

  const apiKey = secretOrEmpty();
  if (!apiKey) {
    console.warn("RevenueCat: REVENUECAT_SECRET_KEY is not set; treating as no subscription");
    return null;
  }

  const request = fetchSubscriber(uid, apiKey)
    .then((result) => {
      if (!result.ok) return null;
      const value = activeFromSubscriber(result.subscriber);
      writeCache(uid, value);
      return value;
    })
    .finally(() => {
      inFlight.delete(uid);
    });

  inFlight.set(uid, request);
  return request;
}

/** Convenience wrapper for callers that only need a boolean. */
async function hasActiveRevenueCatSubscription(uid) {
  return (await getActiveSubscription(uid)) !== null;
}

module.exports = {
  revenueCatSecretKey,
  PREMIUM_ENTITLEMENT_ID,
  getActiveSubscription,
  hasActiveRevenueCatSubscription
};
