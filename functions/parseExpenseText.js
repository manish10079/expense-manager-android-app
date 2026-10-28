/**
 * parseExpenseText — callable AI parser.
 *
 * Nested round-robin: rotate providers, then models within a provider.
 * Fail over on HTTP / timeout / invalid JSON. Skip a provider when its
 * secret is missing. All fail → unavailable.
 */

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");

const opencodeApiKey = defineSecret("OPENCODE_API_KEY");
const googleAiApiKey = defineSecret("GOOGLE_AI_API_KEY");
const groqApiKey = defineSecret("GROQ_API_KEY");

const CALL_TIMEOUT_MS = 8000;
const DEFAULT_OPENCODE_URL = "https://opencode.ai/zen/v1/chat/completions";
const DEFAULT_GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
const DEFAULT_OPENCODE_MODELS = [
  "opencode/big-pickle",
  "opencode/deepseek-r1-free",
  "opencode/mimo-v2.6-flash-free",
  "opencode/minimax-m2.5-free",
];
const DEFAULT_GOOGLE_MODELS = [
  "gemini-3.5-flash-lite",
  "gemini-3.1-flash-lite",
  "gemini-3.8-flash",
  "gemini-3.6-flash",
];
const DEFAULT_GROQ_MODELS = [
  "llama-3.1-8b-instant",
  "llama-3.3-70b-versatile",
  "gemma2-9b-it",
];

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

let promptCache = { at: 0, instruction: "", providers: [] };
let providerCursor = 0;
const modelCursorByProvider = Object.create(null);

function db() {
  return admin.firestore();
}

function utcDayKey() {
  return new Date().toISOString().slice(0, 10);
}

function secretOrEmpty(secretParam) {
  try {
    const value = secretParam.value();
    return typeof value === "string" && value.trim() ? value.trim() : "";
  } catch {
    return "";
  }
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

function rotate(list, start) {
  const n = list.length;
  if (n === 0) return [];
  const i = ((start % n) + n) % n;
  return list.slice(i).concat(list.slice(0, i));
}

function defaultProvider(id) {
  if (id === "google") {
    return { id: "google", kind: "google", models: DEFAULT_GOOGLE_MODELS.slice() };
  }
  if (id === "groq") {
    return {
      id: "groq",
      kind: "openai",
      baseUrl: DEFAULT_GROQ_URL,
      models: DEFAULT_GROQ_MODELS.slice(),
    };
  }
  return {
    id: "opencode",
    kind: "openai",
    baseUrl: DEFAULT_OPENCODE_URL,
    models: DEFAULT_OPENCODE_MODELS.slice(),
  };
}

function normalizeProviders(raw) {
  if (!Array.isArray(raw) || raw.length === 0) {
    return [defaultProvider("opencode")];
  }
  const out = [];
  for (const entry of raw) {
    if (typeof entry === "string") {
      out.push(defaultProvider(entry.trim().toLowerCase()));
      continue;
    }
    if (!entry || typeof entry !== "object") continue;
    const id = String(entry.id || "").trim().toLowerCase();
    if (!id) continue;
    const base = defaultProvider(id);
    const models = asStringList(entry.models, base.models);
    if (!models.length) continue;
    out.push({
      id,
      kind: id === "google" ? "google" : "openai",
      baseUrl:
        typeof entry.baseUrl === "string" && entry.baseUrl.trim()
          ? entry.baseUrl.trim()
          : base.baseUrl,
      models,
    });
  }
  return out.length ? out : [defaultProvider("opencode")];
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
  promptCache = {
    at: now,
    instruction: instruction.trim(),
    providers: normalizeProviders(data.providers),
  };
  return promptCache;
}

function providersWithKeys(providers, keys) {
  return providers.filter((p) => Boolean(keys[p.id]));
}

async function fetchWithTimeout(url, options) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), CALL_TIMEOUT_MS);
  try {
    return await fetch(url, { ...options, signal: controller.signal });
  } finally {
    clearTimeout(timer);
  }
}

async function callOpenAiCompatible({ apiKey, url, model, systemInstruction, userBlock }) {
  const response = await fetchWithTimeout(url, {
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
    console.error("AI HTTP", response.status, model);
    throw new Error("provider-http");
  }
  const content = payload?.choices?.[0]?.message?.content;
  if (typeof content !== "string") {
    throw new Error("invalid-json");
  }
  return content;
}

async function callGoogle({ apiKey, model, systemInstruction, userBlock }) {
  const modelId = model.replace(/^models\//, "");
  const url =
    `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(modelId)}:generateContent`;
  const response = await fetchWithTimeout(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "x-goog-api-key": apiKey,
    },
    body: JSON.stringify({
      systemInstruction: { parts: [{ text: systemInstruction }] },
      contents: [{ role: "user", parts: [{ text: userBlock }] }],
      generationConfig: {
        temperature: 0.1,
        responseMimeType: "application/json",
      },
    }),
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    console.error("Google HTTP", response.status, modelId);
    throw new Error("provider-http");
  }
  const content = payload?.candidates?.[0]?.content?.parts
    ?.map((part) => part.text)
    .filter(Boolean)
    .join("\n");
  if (typeof content !== "string" || !content.trim()) {
    throw new Error("invalid-json");
  }
  return content;
}

async function callProvider(provider, apiKey, model, systemInstruction, userBlock) {
  if (provider.kind === "google") {
    return callGoogle({ apiKey, model, systemInstruction, userBlock });
  }
  return callOpenAiCompatible({
    apiKey,
    url: provider.baseUrl,
    model,
    systemInstruction,
    userBlock,
  });
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
    throw new Error("invalid-json");
  }
  try {
    return JSON.parse(body.slice(start, end + 1));
  } catch {
    throw new Error("invalid-json");
  }
}

function validateTransactions(parsed, expense, income, payment) {
  if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) {
    throw new Error("invalid-json");
  }
  const extraRoot = Object.keys(parsed).filter((k) => k !== "transactions");
  if (extraRoot.length) {
    throw new Error("invalid-json");
  }
  const rows = parsed.transactions;
  if (!Array.isArray(rows)) {
    throw new Error("invalid-json");
  }

  const expenseMap = nameSet(expense);
  const incomeMap = nameSet(income);
  const paymentMap = nameSet(payment);
  const out = [];

  for (const row of rows) {
    if (!row || typeof row !== "object" || Array.isArray(row)) {
      throw new Error("invalid-json");
    }
    const keys = Object.keys(row);
    if (keys.length !== 5 || keys.some((k) => !ALLOWED_KEYS.has(k))) {
      throw new Error("invalid-json");
    }
    const amount = row.amount;
    if (typeof amount !== "number" || !Number.isFinite(amount)) {
      throw new Error("invalid-json");
    }
    const typeId = row.type_id;
    if (typeId !== 1 && typeId !== 2) {
      throw new Error("invalid-json");
    }
    if (typeof row.note !== "string") {
      throw new Error("invalid-json");
    }
    if (typeof row.category !== "string" || typeof row.payment_method !== "string") {
      throw new Error("invalid-json");
    }

    const catKey = row.category.trim().toLowerCase();
    const payKey = row.payment_method.trim().toLowerCase();
    const canonicalCategory =
      typeId === 1 ? incomeMap.get(catKey) : expenseMap.get(catKey);
    if (!canonicalCategory) {
      throw new Error("unknown-category");
    }
    const canonicalPayment = paymentMap.get(payKey);
    if (!canonicalPayment) {
      throw new Error("unknown-payment");
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

async function parseWithRoundRobin({
  providers,
  keys,
  systemInstruction,
  userBlock,
  expenseCategories,
  incomeCategories,
  paymentMethods,
}) {
  const ready = providersWithKeys(providers, keys);
  if (!ready.length) {
    throw new HttpsError("unavailable", "unavailable");
  }

  const providerOrder = rotate(ready, providerCursor++);
  for (const provider of providerOrder) {
    const start = modelCursorByProvider[provider.id] || 0;
    modelCursorByProvider[provider.id] = start + 1;
    const models = rotate(provider.models, start);
    const apiKey = keys[provider.id];
    for (const model of models) {
      try {
        const raw = await callProvider(
          provider,
          apiKey,
          model,
          systemInstruction,
          userBlock
        );
        const parsed = stripToJsonObject(raw);
        return validateTransactions(
          parsed,
          expenseCategories,
          incomeCategories,
          paymentMethods
        );
      } catch (error) {
        if (error instanceof HttpsError) throw error;
        console.error("AI failover", provider.id, model, error && error.message);
      }
    }
  }
  throw new HttpsError("unavailable", "unavailable");
}

exports.parseExpenseText = onCall(
  {
    region: "us-central1",
    timeoutSeconds: 30,
    memory: "256MB",
    enforceAppCheck: true,
    secrets: [opencodeApiKey, googleAiApiKey, groqApiKey],
  },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "unauthenticated");
    }
    const signInProvider = request.auth.token?.firebase?.sign_in_provider;
    if (signInProvider === "anonymous") {
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

    const keys = {
      opencode: secretOrEmpty(opencodeApiKey),
      google: secretOrEmpty(googleAiApiKey),
      groq: secretOrEmpty(groqApiKey),
    };

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

    return parseWithRoundRobin({
      providers: config.providers,
      keys,
      systemInstruction,
      userBlock,
      expenseCategories,
      incomeCategories,
      paymentMethods,
    });
  }
);
