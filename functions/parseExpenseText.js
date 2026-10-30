/**
 * parseExpenseText — callable AI parser.
 *
 * Providers: Groq, OpenRouter (live :free models), Xkiro (GET /v1/models).
 * ENABLED_PROVIDERS: groq, openrouter, xkiro, apinex. Rotate each request.
 * Rotate models; fail over on HTTP / timeout / invalid JSON.
 */

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");

const groqApiKey = defineSecret("GROQ_API_KEY");
const openrouterApiKey = defineSecret("OPENROUTER_API_KEY");
const xkiroApiKey = defineSecret("XKIRO_API_KEY");
const apinexApiKey = defineSecret("APINEX_API_KEY");

const ENABLED_PROVIDERS = ["groq", "openrouter", "xkiro", "apinex"];

const CALL_TIMEOUT_MS = 8000;
const DEFAULT_GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
const GROQ_MODELS_URL = "https://api.groq.com/openai/v1/models";
const GROQ_CACHE_MS = 30 * 60 * 1000;
const OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
const OPENROUTER_MODELS_URL = "https://openrouter.ai/api/v1/models";
const OPENROUTER_FREE_CACHE_MS = 30 * 60 * 1000;
const OPENROUTER_FREE_MAX = 8;
const XKIRO_URL = "https://api.xkiro.com/v1/chat/completions";
const XKIRO_CACHE_MS = 30 * 60 * 1000;
const APINEX_URL = "https://api.apinex.bond/v1/chat/completions";
const APINEX_MODELS_URL = "https://api.apinex.bond/v1/models";
const APINEX_CACHE_MS = 30 * 60 * 1000;
const DEFAULT_XKIRO_FREE_MODELS = [
  "mistralai/mistral-large-2512",
  "mistralai/mistral-medium-3.5",
  "mistralai/mistral-small-2603",
  "mistralai/ministral-14b",
  "qwen/qwen3.8-max:free",
  "qwen/qwen3.8-omni-flash:free",
  "qwen/qwen3.7-flash:free",
  "stealth/big-pickle",
  "cohere/command-a",
  "sensenova/sensenova-6.8-flash-lite",
];
const DEFAULT_GROQ_MODELS = [
  "llama-3.1-8b-instant",
  "openai/gpt-oss-20b",
  "meta-llama/llama-4-scout-17b-16e-instruct",
  "qwen/qwen3-32b",
  "llama-3.3-70b-versatile",
  "openai/gpt-oss-120b",
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
let groqModelCache = { at: 0, models: [] };
let openrouterFreeCache = { at: 0, models: [] };
let xkiroModelCache = { at: 0, models: [] };
let apinexModelCache = { at: 0, models: [] };
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

async function quotaContext(uid) {
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
  return { ref, limit, isPro };
}

async function assertDailyQuotaAvailable(uid) {
  const { ref, limit } = await quotaContext(uid);
  const snap = await ref.get();
  const count = snap.exists ? Number(snap.data().count) || 0 : 0;
  if (count >= limit) {
    throw new HttpsError("resource-exhausted", "daily-limit");
  }
}

/** Count one successful parse (including empty transactions[]). Free 4 / Pro 10 UTC day. */
async function incrementDailyQuota(uid) {
  const { ref, limit, isPro } = await quotaContext(uid);
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

function groqProvider(models) {
  return {
    id: "groq",
    kind: "openai",
    baseUrl: DEFAULT_GROQ_URL,
    models: models && models.length ? models : DEFAULT_GROQ_MODELS.slice(),
  };
}

function normalizeProviders(raw) {
  let groq = groqProvider();
  if (Array.isArray(raw)) {
    for (const entry of raw) {
      if (entry && typeof entry === "object" && String(entry.id || "").trim().toLowerCase() === "groq") {
        groq = groqProvider(asStringList(entry.models, DEFAULT_GROQ_MODELS));
      }
    }
  }
  return [groq, openrouterProvider([]), xkiroProvider([]), apinexProvider([])];
}

function apinexProvider(models) {
  return {
    id: "apinex",
    kind: "openai",
    baseUrl: APINEX_URL,
    models: models && models.length ? models : [],
  };
}

function xkiroProvider(models) {
  return {
    id: "xkiro",
    kind: "openai",
    baseUrl: XKIRO_URL,
    models: models && models.length ? models : [],
  };
}

function openrouterProvider(models) {
  return {
    id: "openrouter",
    kind: "openai",
    baseUrl: OPENROUTER_URL,
    models: models && models.length ? models : [],
    extraHeaders: {
      "HTTP-Referer": "https://expensetracker.app",
      "X-Title": "ExpenseTracker",
    },
  };
}

function isOpenRouterFreeChatModel(entry) {
  const id = String(entry && entry.id ? entry.id : "");
  if (!id) return false;
  const lower = id.toLowerCase();
  if (
    lower.includes("whisper") ||
    lower.includes("tts") ||
    lower.includes("lyria") ||
    lower.includes("transcribe") ||
    lower.includes("embed") ||
    lower.includes("safety")
  ) {
    return false;
  }
  const pricing = (entry && entry.pricing) || {};
  const prompt = String(pricing.prompt ?? "1");
  const completion = String(pricing.completion ?? "1");
  const free = id.endsWith(":free") || (prompt === "0" && completion === "0");
  if (!free) return false;
  const params = entry.supported_parameters || [];
  return params.includes("response_format") || params.includes("structured_outputs") || id.endsWith(":free");
}

async function loadOpenRouterFreeModels(apiKey) {
  const now = Date.now();
  if (openrouterFreeCache.models.length && now - openrouterFreeCache.at < OPENROUTER_FREE_CACHE_MS) {
    return openrouterFreeCache.models;
  }
  const response = await fetchWithTimeout(OPENROUTER_MODELS_URL, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
    },
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    console.error("OpenRouter models HTTP", response.status);
    throw new Error("openrouter-models");
  }
  const rows = Array.isArray(payload.data) ? payload.data : [];
  const ids = [];
  for (const entry of rows) {
    if (!isOpenRouterFreeChatModel(entry)) continue;
    ids.push(String(entry.id));
    if (ids.length >= OPENROUTER_FREE_MAX) break;
  }
  console.info("OpenRouter free models", ids);
  openrouterFreeCache = { at: now, models: ids };
  return ids;
}

async function loadGroqModels(apiKey) {
  const now = Date.now();
  if (groqModelCache.models.length && now - groqModelCache.at < GROQ_CACHE_MS) {
    console.info("Groq models (cache)", groqModelCache.models);
    return groqModelCache.models;
  }
  const response = await fetchWithTimeout(GROQ_MODELS_URL, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
    },
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    console.error("Groq models HTTP", response.status, payload);
    throw new Error("groq-models");
  }
  const rows = Array.isArray(payload.data) ? payload.data : [];
  const summary = rows.map((entry) => ({
    id: entry && entry.id,
    owned_by: entry && entry.owned_by,
    active: entry && entry.active,
    context_window: entry && entry.context_window,
  }));
  console.info("Groq models count", rows.length);
  console.info("Groq models meta", summary);
  const ids = rows
    .map((entry) => String(entry && entry.id ? entry.id : "").trim())
    .filter(Boolean);
  console.info("Groq models", ids);
  groqModelCache = { at: now, models: ids };
  return ids;
}

async function loadXkiroModels() {
  const now = Date.now();
  if (xkiroModelCache.models.length && now - xkiroModelCache.at < XKIRO_CACHE_MS) {
    return xkiroModelCache.models;
  }
  const ids = DEFAULT_XKIRO_FREE_MODELS.slice();
  console.info("Xkiro models", ids);
  xkiroModelCache = { at: now, models: ids };
  return ids;
}

async function loadApinexModels(apiKey) {
  const now = Date.now();
  if (apinexModelCache.models.length && now - apinexModelCache.at < APINEX_CACHE_MS) {
    console.info("Apinex models (cache)", apinexModelCache.models);
    return apinexModelCache.models;
  }
  const response = await fetchWithTimeout(APINEX_MODELS_URL, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
    },
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    console.error("Apinex models HTTP", response.status, payload);
    throw new Error("apinex-models");
  }
  const rows = Array.isArray(payload.data) ? payload.data : [];
  const ids = rows
    .map((entry) => String(entry && entry.id ? entry.id : "").trim())
    .filter((id) => id.toLowerCase().startsWith("free/"));
  console.info("Apinex models count", ids.length);
  console.info("Apinex models", ids);
  apinexModelCache = { at: now, models: ids };
  return ids;
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
  return providers.filter((p) => Boolean(keys[p.id]) && p.models && p.models.length);
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

async function callOpenAiCompatible({ apiKey, url, model, systemInstruction, userBlock, extraHeaders }) {
  const response = await fetchWithTimeout(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
      ...(extraHeaders || {}),
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
    const errMsg = payload && payload.error && payload.error.message
      ? String(payload.error.message).slice(0, 180)
      : "";
    console.error("AI HTTP", response.status, model, errMsg);
    throw new Error("provider-http");
  }
  const content = payload?.choices?.[0]?.message?.content;
  if (typeof content !== "string") {
    throw new Error("invalid-json");
  }
  return content;
}

async function callProvider(provider, apiKey, model, systemInstruction, userBlock) {
  return callOpenAiCompatible({
    apiKey,
    url: provider.baseUrl,
    model,
    systemInstruction,
    userBlock,
    extraHeaders: provider.extraHeaders,
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
  const configured = providers.map((p) => p.id);
  const ready = providersWithKeys(providers, keys);
  console.info("AI rr", {
    configured,
    hasKey: {
      groq: Boolean(keys.groq),
      openrouter: Boolean(keys.openrouter),
      xkiro: Boolean(keys.xkiro),
      apinex: Boolean(keys.apinex),
    },
    ready: ready.map((p) => p.id),
  });
  if (!ready.length) {
    console.error("AI rr no providers with keys");
    throw new HttpsError("unavailable", "unavailable");
  }

  const providerOrder = rotate(ready, providerCursor++);
  for (const provider of providerOrder) {
    const start = modelCursorByProvider[provider.id] || 0;
    modelCursorByProvider[provider.id] = start + 1;
    const models = rotate(provider.models, start);
    const apiKey = keys[provider.id];
    for (const model of models) {
      console.info("AI try", provider.id, model);
      try {
        const raw = await callProvider(
          provider,
          apiKey,
          model,
          systemInstruction,
          userBlock
        );
        const parsed = stripToJsonObject(raw);
        const result = validateTransactions(
          parsed,
          expenseCategories,
          incomeCategories,
          paymentMethods
        );
        console.info("AI ok", provider.id, model, "rows", result.transactions.length);
        return result;
      } catch (error) {
        if (error instanceof HttpsError) throw error;
        console.error("AI failover", provider.id, model, error && error.message);
      }
    }
  }
  console.error("AI rr all failed");
  throw new HttpsError("unavailable", "unavailable");
}

exports.parseExpenseText = onCall(
  {
    region: "us-central1",
    timeoutSeconds: 30,
    memory: "256MB",
    enforceAppCheck: true,
    secrets: [groqApiKey, openrouterApiKey, xkiroApiKey, apinexApiKey],
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

    const keys = {
      groq: secretOrEmpty(groqApiKey),
      openrouter: secretOrEmpty(openrouterApiKey),
      xkiro: secretOrEmpty(xkiroApiKey),
      apinex: secretOrEmpty(apinexApiKey),
    };

    const config = await loadParserConfig();
    let providers = config.providers;
    const providerEnabled = (id) =>
      !ENABLED_PROVIDERS.length || ENABLED_PROVIDERS.includes(id);
    if (keys.groq && providerEnabled("groq")) {
      try {
        const groqModels = await loadGroqModels(keys.groq);
        providers = providers.map((p) =>
          p.id === "groq" ? groqProvider(groqModels) : p
        );
      } catch (error) {
        console.error("Groq models list failed", error && error.message);
        providers = providers.filter((p) => p.id !== "groq");
      }
    } else {
      providers = providers.filter((p) => p.id !== "groq");
    }
    if (keys.openrouter && providerEnabled("openrouter")) {
      try {
        const freeModels = await loadOpenRouterFreeModels(keys.openrouter);
        providers = providers.map((p) =>
          p.id === "openrouter" ? openrouterProvider(freeModels) : p
        );
      } catch (error) {
        console.error("OpenRouter models list failed", error && error.message);
        providers = providers.filter((p) => p.id !== "openrouter");
      }
    } else {
      providers = providers.filter((p) => p.id !== "openrouter");
    }
    if (keys.xkiro && providerEnabled("xkiro")) {
      try {
        const xkiroModels = await loadXkiroModels(keys.xkiro);
        providers = providers.map((p) =>
          p.id === "xkiro" ? xkiroProvider(xkiroModels) : p
        );
      } catch (error) {
        console.error("Xkiro models list failed", error && error.message);
        providers = providers.filter((p) => p.id !== "xkiro");
      }
    } else {
      providers = providers.filter((p) => p.id !== "xkiro");
    }
    if (keys.apinex && providerEnabled("apinex")) {
      try {
        const apinexModels = await loadApinexModels(keys.apinex);
        providers = providers.map((p) =>
          p.id === "apinex" ? apinexProvider(apinexModels) : p
        );
      } catch (error) {
        console.error("Apinex models list failed", error && error.message);
        providers = providers.filter((p) => p.id !== "apinex");
      }
    } else {
      providers = providers.filter((p) => p.id !== "apinex");
    }
    const systemInstruction =
      injectLists(
        config.instruction,
        expenseCategories,
        incomeCategories,
        paymentMethods
      ) +
      "\n11. Input may be typed text or OCR from a store receipt, bank SMS/statement, UPI app screenshot, or PDF. OCR is noisy: broken lines, extra spaces, headers/footers, GSTIN, and currency signs (especially ₹) misread as digits. Recover merchant and true amounts; ignore boilerplate that is not a transaction.";
    const userBlock = [
      `User locale: ${locale}`,
      `User currency: ${currency}`,
      "User input:",
      text.trim(),
    ].join("\n");

    const skipQuota = request.data?.skipQuota === true;
    if (!skipQuota) {
      await assertDailyQuotaAvailable(uid);
    }

    if (ENABLED_PROVIDERS.length) {
      providers = providers.filter((p) => ENABLED_PROVIDERS.includes(p.id));
    }

    const parsed = await parseWithRoundRobin({
      providers,
      keys,
      systemInstruction,
      userBlock,
      expenseCategories,
      incomeCategories,
      paymentMethods,
    });
    if (!skipQuota) {
      await incrementDailyQuota(uid);
    }
    return parsed;
  }
);
