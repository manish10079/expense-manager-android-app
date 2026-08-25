/**
 * parseVoiceTransaction — Cloud Function for Gemini AI voice parsing.
 *
 * Uses Google Gemini API to parse complex voice inputs into structured
 * transaction data. Called by the ExpenseTracker Android app when the
 * offline parser returns LOW confidence.
 *
 * Personalization:
 * - Fetches user's currency and locale from Firestore
 * - Includes ALL available categories and payment methods in prompt
 * - Includes user's top 3 most-used categories and payment methods
 * - No raw transaction data is sent to Gemini (privacy safe)
 *
 * Request:  { text: string, locale: string, currency: string }
 * Response: { amount, currency, transactionTypeId, categoryId, note,
 *             merchant, paymentTypeId, createdAt, confidence, source }
 *
 * Security:
 * - Requires authenticated user (not anonymous)
 * - Rate-limited by client-side AiUsageTracker (10/day for free)
 * - Gemini API key stored in Firebase config (not in client)
 */

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { GoogleGenerativeAI } = require("@google/generative-ai");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();

// Gemini API key from environment
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || "";

/**
 * All available expense categories (matches Android categoryMap).
 */
const EXPENSE_CATEGORIES = [
  "Food", "Travel", "Shopping", "Bills", "Health", "Entertainment",
  "Rent", "Groceries", "Education", "Subscriptions", "Insurance",
  "Gifts", "Personal Care", "Fuel", "Maintenance", "Taxes",
  "Pets", "Childcare", "Donations", "Miscellaneous", "Transport", "Other"
];

/**
 * All available income categories (matches Android categoryMap).
 */
const INCOME_CATEGORIES = [
  "Salary", "Business", "Investment", "Freelance", "Other Income"
];

/**
 * All available payment methods (matches Android paymentTypeMap).
 */
const PAYMENT_METHODS = ["UPI", "Cash", "Bank", "Card", "Other"];

/**
 * Category ID mapping (matches Android categoryMap).
 */
const CATEGORY_MAP = {
  "food": 1, "travel": 2, "shopping": 3, "bills": 4,
  "health": 5, "entertainment": 6, "rent": 7, "groceries": 8,
  "education": 9, "subscriptions": 10, "insurance": 11,
  "gifts": 12, "personal care": 13, "fuel": 14, "maintenance": 15,
  "taxes": 16, "pets": 17, "childcare": 18, "donations": 19,
  "miscellaneous": 20, "transport": 22, "other": 23,
  "salary": 101, "business": 102, "investment": 103,
  "freelance": 104, "other income": 105
};

/**
 * Payment type ID mapping (matches Android paymentTypeMap).
 */
const PAYMENT_TYPE_MAP = {
  "upi": 1, "cash": 2, "bank": 3, "card": 4, "other": 5
};

exports.parseVoiceTransaction = onCall(
  {
    region: "us-central1",
    timeoutSeconds: 30,
    memory: "256MB"
  },
  async (request) => {
    // --- Auth gate ---
    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "Please sign in to use AI parsing");
    }
    const provider = request.auth.token?.firebase?.sign_in_provider;
    if (provider === "anonymous") {
      throw new HttpsError("permission-denied", "Please sign in to use AI parsing");
    }

    // --- Input validation ---
    const text = request.data?.text;
    if (typeof text !== "string" || text.trim().length === 0) {
      throw new HttpsError("invalid-argument", "Text cannot be empty");
    }
    const locale = request.data?.locale || "en-US";
    const clientCurrency = request.data?.currency || "INR";
    const allExpenseCategories = request.data?.allExpenseCategories || [];
    const allIncomeCategories = request.data?.allIncomeCategories || [];
    const allPaymentMethods = request.data?.allPaymentMethods || [];
    const topCategories = request.data?.topCategories || [];
    const topPaymentMethods = request.data?.topPaymentMethods || [];

    // --- Check Gemini API key ---
    if (!GEMINI_API_KEY) {
      throw new HttpsError("failed-precondition", "AI service not configured");
    }

    try {
      // Build user context from client-provided data (all from local Room DB)
      const userContext = {
        currency: clientCurrency,
        locale: locale,
        allExpenseCategories: allExpenseCategories,
        allIncomeCategories: allIncomeCategories,
        allPaymentMethods: allPaymentMethods,
        topCategories: topCategories,
        topPaymentMethods: topPaymentMethods
      };

      const genAI = new GoogleGenerativeAI(GEMINI_API_KEY);
      const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

      const prompt = buildPrompt(text, userContext);
      console.log("Prompt length:", prompt.length, "chars");
      const result = await model.generateContent(prompt);
      const response = await result.response;
      const text_response = response.text();

      // Parse the structured JSON response
      const parsed = parseGeminiResponse(text_response);

      return {
        amount: parsed.amount || 0,
        currency: parsed.currency || userContext.currency,
        transactionTypeId: parsed.transactionTypeId || 2,
        categoryId: resolveCategoryId(parsed.category),
        note: parsed.note || text.trim(),
        merchant: parsed.merchant || null,
        paymentTypeId: resolvePaymentTypeId(parsed.paymentMethod),
        createdAt: parsed.date || Date.now(),
        confidence: parsed.confidence || "MEDIUM",
        source: "gemini"
      };
    } catch (error) {
      console.error("Gemini API error:", error);
      throw new HttpsError(
        "internal",
        "AI parsing failed. Please try again or use manual entry."
      );
    }
  }
);

/**
 * Fetches user context from Firestore for personalized parsing.
 * 
 * Reads only:
 * - User profile (currency, locale) — 1 read
 * - Top categories aggregation — 1 read (computed on-device, stored as summary)
 * - Top payment methods aggregation — 1 read (computed on-device, stored as summary)
 * 
 * Total: 3 Firestore reads per parse (well within free tier)
 */
async function fetchUserContext(uid, fallbackCurrency, fallbackLocale) {
  const context = {
    currency: fallbackCurrency,
    locale: fallbackLocale,
    topCategories: [],
    topPaymentMethods: []
  };

  try {
    // 1. Fetch user profile for currency and locale
    const userSnap = await db.collection("users").doc(uid).get();
    if (userSnap.exists) {
      const userData = userSnap.data();
      context.currency = userData.currency || fallbackCurrency;
      context.locale = userData.locale || fallbackLocale;
    }

    // 2. Fetch aggregated AI stats (computed on-device, synced periodically)
    // These are summary fields stored in the user document — not raw transactions
    const aiStatsSnap = await db.collection("users").doc(uid)
      .collection("aiStats").doc("voiceParsing").get();
    if (aiStatsSnap.exists) {
      const aiStats = aiStatsSnap.data();
      context.topCategories = aiStats.topCategories || [];
      context.topPaymentMethods = aiStats.topPaymentMethods || [];
    }
  } catch (error) {
    console.error("Error fetching user context:", error);
    // Continue with fallback defaults — don't fail the parse
  }

  return context;
}

/**
 * Builds the Gemini prompt with personalized user context.
 * 
 * Includes:
 * - User's currency (for amount conversion)
 * - User's locale (for language parsing)
 * - ALL available categories (so Gemini knows exact options)
 * - ALL available payment methods (so Gemini knows exact options)
 * - User's top 3 most-used categories (for prioritization)
 * - User's top 3 most-used payment methods (for prioritization)
 */
function buildPrompt(text, userContext) {
  const expenseCats = userContext.allExpenseCategories.length > 0
    ? userContext.allExpenseCategories.join(", ")
    : EXPENSE_CATEGORIES.join(", ");
  const incomeCats = userContext.allIncomeCategories.length > 0
    ? userContext.allIncomeCategories.join(", ")
    : INCOME_CATEGORIES.join(", ");
  const paymentMethods = userContext.allPaymentMethods.length > 0
    ? userContext.allPaymentMethods.join(", ")
    : PAYMENT_METHODS.join(", ");
  const topCats = userContext.topCategories.length > 0
    ? userContext.topCategories.slice(0, 3).join(", ")
    : "Food, Transport, Shopping";
  const topPayments = userContext.topPaymentMethods.length > 0
    ? userContext.topPaymentMethods.slice(0, 3).join(", ")
    : "UPI, Card, Cash";

  return `You are a financial transaction parser for an expense tracker app.

USER CONTEXT (use this for better predictions):
- User's currency: ${userContext.currency}
- User's locale: ${userContext.locale}
- User's top 3 most-used categories: ${topCats}
- User's top 3 most-used payment methods: ${topPayments}

AVAILABLE EXPENSE CATEGORIES (use ONLY these exact names — these are from the user's app, may include custom categories):
${expenseCats}

AVAILABLE INCOME CATEGORIES (use ONLY these exact names):
${incomeCats}

AVAILABLE PAYMENT METHODS (use ONLY these exact names — these are from the user's app, may include custom methods):
${paymentMethods}

PARSE THE FOLLOWING VOICE INPUT:
"${text}"

RETURN ONLY a JSON object with these fields:
- amount: numeric amount in smallest currency unit (cents for USD, paise for INR, etc.)
- currency: currency code (e.g., "USD", "INR", "EUR")
- transactionTypeId: 1 for Income, 2 for Expense
- category: EXACT category name from the available lists above (e.g., "Food", "Transport")
- note: clean description of the transaction
- merchant: store/person name (null if not mentioned)
- paymentMethod: EXACT payment method name from available list (e.g., "UPI", "Card")
- date: timestamp in milliseconds (null for today, calculate relative dates)
- confidence: HIGH if amount + category + date detected, MEDIUM if amount + category, LOW if partial

IMPORTANT RULES:
- ALWAYS use the exact category names from the lists above (they may include custom categories like "Pet Food" or "Gym")
- ALWAYS use the exact payment method names from the list above (they may include custom methods like "Google Pay")
- For amounts like "45 dollars", convert to cents (4500)
- For amounts like "500 rupees", convert to paise (50000)
- Default to expense (type 2) if unclear
- Use the user's top categories as hints for ambiguous inputs
- Return ONLY the JSON object, no explanation

EXAMPLE:
Input: "Bought pet food for 500"
Response: {"amount":50000,"currency":"INR","transactionTypeId":2,"category":"Pet Food","note":"Bought pet food","merchant":null,"paymentMethod":null,"date":null,"confidence":"HIGH"}`;
}

/**
 * Parses the Gemini text response into a structured object.
 */
function parseGeminiResponse(text) {
  const jsonMatch = text.match(/\{[\s\S]*\}/);
  if (!jsonMatch) return {};

  try {
    return JSON.parse(jsonMatch[0]);
  } catch (e) {
    console.error("Failed to parse Gemini response:", e);
    return {};
  }
}

/**
 * Resolves a category name to our internal category ID.
 */
function resolveCategoryId(categoryName) {
  if (!categoryName) return 23;
  const normalized = categoryName.toLowerCase().trim();
  return CATEGORY_MAP[normalized] || 23;
}

/**
 * Resolves a payment method name to our internal payment type ID.
 */
function resolvePaymentTypeId(paymentMethod) {
  if (!paymentMethod) return null;
  const normalized = paymentMethod.toLowerCase().trim();
  return PAYMENT_TYPE_MAP[normalized] || null;
}
