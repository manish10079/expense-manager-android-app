/**
 * parseExpenseText — callable AI parser (Phase 1).
 *
 * Reads systemInstruction from Firestore ai_config/parser, injects this
 * device's category/payment lists, calls OpenCode with OPENCODE_API_KEY secret,
 * returns { transactions: [...] } with exactly five fields per item.
 *
 * Does not replace parseVoiceTransaction. Leave that export unused from the app.
 */

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");

const opencodeApiKey = defineSecret("OPENCODE_API_KEY");
const OPENCODE_URL = "https://opencode.ai/zen/v1/chat/completions";
const DEFAULT_OPENCODE_MODEL = "gpt-4o-mini";

const ALLOWED_KEYS = new Set([
  "amount",
  "category",
  "payment_method",
  "note",
  "type_id",
]);

const FALLBACK_EXPENSE = [
  "Food", "Travel", "Shopping", "Bills", "Health", "Entertainment", "Rent",
  "Groceries", "Education", "Subscriptions", "Insurance", "Gifts", "Personal Care",
  "Fuel", "Maintenance", "Taxes", "Pets", "Childcare", "Donations", "Miscellaneous",
  "Transport", "Other",
];
const FALLBACK_INCOME = ["Salary", "Business", "Investment", "Freelance", "Other"];
const FALLBACK_PAYMENT = ["UPI", "Cash", "Bank", "Card", "Other", "Unknown"];

const CACHE_MS = 5 * 60 * 1000;
const FREE_OR_GUEST_DAILY_LIMIT = 4;
const PRO_DAILY_LIMIT = 10;
let promptCache = { at: 0, instruction: "", model: DEFAULT_OPENCODE_MODEL };

function db() {
  return admin.firestore();
}

function utcDayKey() {
  return new Date().toISOString().slice(0, 10);
}

function isProFromUserAndRc(userData, rcData) {
  const now = Date.now();
  const entitlements = rcData && typeof rcData === "object" ? rcData.entitlements : null;
  const premium = entitlements && typeof entitlements === "object" ? entitlements.premium : null;
  if (premium && typeof premium === "object") {
    const rawExpiry = premium.expires_date ?? premium.expiresDate ?? null;
    if (rawExpiry == null) return true;
    const expiresMillis = Date.parse(rawExpiry);
    if (Number.isFinite(expiresMillis) && expiresMillis > now) return true;
  }
  if (!userData || typeof userData !== "object") return false;
  const tier = userData.accountTier;
  if (tier === "Paid_Subscription") return true;
  if (tier === "Pro_Pass" || tier === "PREMIUM") {
    const expiry = Number(userData.proExpiryTimestamp ?? 0);
    return expiry === 0 || expiry > now;
  }
  return false;
}

async function consumeDailyQuota(uid) {
  const [userSnap, rcSnap] = await Promise.all([
    db().collection("users").doc(uid).get(),
    db().collection("rc_customers").doc(uid).get(),
  ]);
  const isPro = isProFromUserAndRc(
    userSnap.exists ? userSnap.data() : null,
    rcSnap.exists ? rcSnap.data() : null
  );
  const limit = isPro ? PRO_DAILY_LIMIT : FREE_OR_GUEST_DAILY_LIMIT;
  const day = utcDayKey();
  const ref = db().collection("ai_usage").doc(uid).collection("days").doc(day);
  await db().runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    const count = snap.exists ? Number(snap.data().count) || 0 : 0;
    if (count >= limit) {
      throw new HttpsError("resource-exhausted", "daily-limit");
    }
    tx.set(
      ref,
      {
        count: count + 1,
        limit,
        isPro,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  });
}


function asStringList(value, fallback) {
  if (!Array.isArray(value) || value.length === 0) return fallback.slice();
  return value.map((v) => String(v).trim()).filter(Boolean);
}

function nameSet(list) {
  const map = new Map();
  for (const name of list) {
    map.set(name.toLowerCase(), name);
  }
  return map;
}

async function loadParserConfig() {
  const now = Date.now();
  if (promptCache.instruction && now - promptCache.at < CACHE_MS) {
    return promptCache;
  }
  const snap = await db().collection("ai_config").doc("parser").get();
  const data = snap.exists ? snap.data() : {};
  const instruction = data.systemInstruction;
  if (typeof instruction !== "string" || !instruction.trim()) {
    throw new HttpsError("failed-precondition", "AI prompt not configured");
  }
  const model =
    typeof data.model === "string" && data.model.trim()
      ? data.model.trim()
      : DEFAULT_OPENCODE_MODEL;
  promptCache = { at: now, instruction: instruction.trim(), model };
  return promptCache;
}

async function callOpenCode({ apiKey, model, systemInstruction, userBlock }) {
  const response = await fetch(OPENCODE_URL, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      model,
      temperature: 0.1,
      response_format: { type: "json_object" },
      messages: [
        { role: "system", content: systemInstruction },
        { role: "user", content: userBlock },
      ],
    }),
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    console.error("OpenCode HTTP", response.status, payload);
    throw new HttpsError("internal", "AI parsing failed");
  }
  const content = payload?.choices?.[0]?.message?.content;
  if (typeof content !== "string") {
    throw new HttpsError("invalid-argument", "invalid-json");
  }
  return content;
}

function injectLists(template, expense, income, payment) {
  return template
    .replaceAll("{{EXPENSE_CATEGORIES}}", expense.join(", "))
    .replaceAll("{{INCOME_CATEGORIES}}", income.join(", "))
    .replaceAll("{{PAYMENT_METHODS}}", payment.join(", "));
}

function stripToJsonObject(text) {
  const trimmed = String(text || "").trim();
  const fence = trimmed.match(/```(?:json)?\s*([\s\S]*?)```/i);
  const body = fence ? fence[1].trim() : trimmed;
  const start = body.indexOf("{");
  const end = body.lastIndexOf("}");
  if (start < 0 || end <= start) {
    throw new HttpsError("invalid-argument", "invalid-json");
  }
  try {
    return JSON.parse(body.slice(start, end + 1));
  } catch {
    throw new HttpsError("invalid-argument", "invalid-json");
  }
}

function validateTransactions(parsed, expense, income, payment) {
  if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) {
    throw new HttpsError("invalid-argument", "invalid-json");
  }
  const extraRoot = Object.keys(parsed).filter((k) => k !== "transactions");
  if (extraRoot.length) {
    throw new HttpsError("invalid-argument", "invalid-json");
  }
  const rows = parsed.transactions;
  if (!Array.isArray(rows)) {
    throw new HttpsError("invalid-argument", "invalid-json");
  }

  const expenseMap = nameSet(expense);
  const incomeMap = nameSet(income);
  const paymentMap = nameSet(payment);
  const out = [];

  for (const row of rows) {
    if (!row || typeof row !== "object" || Array.isArray(row)) {
      throw new HttpsError("invalid-argument", "invalid-json");
    }
    const keys = Object.keys(row);
    if (keys.length !== 5 || keys.some((k) => !ALLOWED_KEYS.has(k))) {
      throw new HttpsError("invalid-argument", "invalid-json");
    }
    const amount = row.amount;
    if (typeof amount !== "number" || !Number.isFinite(amount)) {
      throw new HttpsError("invalid-argument", "invalid-json");
    }
    const typeId = row.type_id;
    if (typeId !== 1 && typeId !== 2) {
      throw new HttpsError("invalid-argument", "invalid-json");
    }
    if (typeof row.note !== "string") {
      throw new HttpsError("invalid-argument", "invalid-json");
    }
    if (typeof row.category !== "string" || typeof row.payment_method !== "string") {
      throw new HttpsError("invalid-argument", "invalid-json");
    }

    const catKey = row.category.trim().toLowerCase();
    const payKey = row.payment_method.trim().toLowerCase();
    const canonicalCategory =
      typeId === 1 ? incomeMap.get(catKey) : expenseMap.get(catKey);
    if (!canonicalCategory) {
      throw new HttpsError("invalid-argument", "unknown-category");
    }
    const canonicalPayment = paymentMap.get(payKey);
    if (!canonicalPayment) {
      throw new HttpsError("invalid-argument", "unknown-payment");
    }

    out.push({
      amount,
      category: canonicalCategory,
      payment_method: canonicalPayment,
      note: row.note,
      type_id: typeId,
    });
  }

  return { transactions: out };
}

exports.parseExpenseText = onCall(
  {
    region: "us-central1",
    timeoutSeconds: 30,
    memory: "256MB",
    enforceAppCheck: true,
    secrets: [opencodeApiKey],
  },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "unauthenticated");
    }
    const provider = request.auth.token?.firebase?.sign_in_provider;
    if (provider === "anonymous") {
      throw new HttpsError("unauthenticated", "unauthenticated");
    }

    const text = request.data?.text;
    if (typeof text !== "string" || text.trim().length === 0) {
      throw new HttpsError("invalid-argument", "Text cannot be empty");
    }

    const locale = typeof request.data?.locale === "string" ? request.data.locale : "en-US";
    const currency =
      typeof request.data?.currency === "string" ? request.data.currency : "INR";
    const expenseCategories = asStringList(
      request.data?.expenseCategories,
      FALLBACK_EXPENSE
    );
    const incomeCategories = asStringList(
      request.data?.incomeCategories,
      FALLBACK_INCOME
    );
    const paymentMethods = asStringList(
      request.data?.paymentMethods,
      FALLBACK_PAYMENT
    );

    await consumeDailyQuota(uid);

    const apiKey = opencodeApiKey.value();
    if (!apiKey) {
      throw new HttpsError("failed-precondition", "AI service not configured");
    }

    const config = await loadParserConfig();
    const systemInstruction = injectLists(
      config.instruction,
      expenseCategories,
      incomeCategories,
      paymentMethods
    );

    const userBlock = [
      `User locale: ${locale}`,
      `User currency: ${currency}`,
      "User input:",
      text.trim(),
    ].join("\n");

    try {
      const raw = await callOpenCode({
        apiKey,
        model: config.model,
        systemInstruction,
        userBlock,
      });
      const parsed = stripToJsonObject(raw);
      return validateTransactions(
        parsed,
        expenseCategories,
        incomeCategories,
        paymentMethods
      );
    } catch (error) {
      if (error instanceof HttpsError) throw error;
      console.error("parseExpenseText model error:", error);
      throw new HttpsError("internal", "AI parsing failed");
    }
  }
);
