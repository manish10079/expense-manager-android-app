/**
 * parseVoiceTransaction — Cloud Function for Gemini AI voice parsing.
 *
 * Uses Google Gemini API to parse complex voice inputs into structured
 * transaction data. Called by the ExpenseTracker Android app when the
 * offline parser returns LOW confidence.
 *
 * Request:  { text: string, locale: string }
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

// Gemini API key from Firebase Remote Config or environment
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || "";

/**
 * Category ID mapping (matches Android categoryMap).
 * Used to map Gemini's category suggestions to our internal IDs.
 */
const CATEGORY_MAP = {
  // Expense categories
  "food": 1, "travel": 2, "shopping": 3, "bills": 4,
  "health": 5, "entertainment": 6, "rent": 7, "groceries": 8,
  "education": 9, "subscriptions": 10, "insurance": 11,
  "gifts": 12, "personal care": 13, "fuel": 14, "maintenance": 15,
  "taxes": 16, "pets": 17, "childcare": 18, "donations": 19,
  "miscellaneous": 20, "transport": 22, "other": 23,
  // Income categories
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

    // --- Check Gemini API key ---
    if (!GEMINI_API_KEY) {
      throw new HttpsError("failed-precondition", "AI service not configured");
    }

    try {
      const genAI = new GoogleGenerativeAI(GEMINI_API_KEY);
      const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

      const prompt = buildPrompt(text, locale);
      const result = await model.generateContent(prompt);
      const response = await result.response;
      const text_response = response.text();

      // Parse the structured JSON response
      const parsed = parseGeminiResponse(text_response);

      return {
        amount: parsed.amount || 0,
        currency: parsed.currency || "",
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
 * Builds the Gemini prompt for voice transaction parsing.
 */
function buildPrompt(text, locale) {
  return `You are a financial transaction parser. Parse the following voice input into a structured transaction.

Voice input: "${text}"
Locale: ${locale}

Extract and return ONLY a JSON object with these fields:
- amount: numeric amount (Long, in smallest currency unit, e.g., cents or paise)
- currency: currency code (e.g., "USD", "INR", "EUR")
- transactionTypeId: 1 for Income, 2 for Expense
- category: one of [food, travel, shopping, bills, health, entertainment, rent, groceries, education, subscriptions, insurance, gifts, personal care, fuel, maintenance, taxes, pets, childcare, donations, miscellaneous, transport, other, salary, business, investment, freelance, other income]
- note: clean description of the transaction (without amount, date, merchant)
- merchant: store/person name (null if not mentioned)
- paymentMethod: one of [upi, cash, bank, card, other] (null if not mentioned)
- date: timestamp in milliseconds (null for today, calculate relative dates like "yesterday")
- confidence: HIGH if amount + category + date detected, MEDIUM if amount + category, LOW if partial

Important:
- For amounts like "45 dollars", convert to cents (4500)
- For amounts like "500 rupees", convert to paise (50000)
- Default to expense (type 2) if unclear
- Use context clues for category (e.g., "uber" = transport, "swiggy" = food)
- Return ONLY the JSON object, no explanation

Example response:
{"amount":4500,"currency":"USD","transactionTypeId":2,"category":"food","note":"Lunch at restaurant","merchant":"Restaurant","paymentMethod":"card","date":null,"confidence":"HIGH"}`;
}

/**
 * Parses the Gemini text response into a structured object.
 */
function parseGeminiResponse(text) {
  // Try to extract JSON from the response (may have markdown formatting)
  const jsonMatch = text.match(/\{[\s\S]*\}/);
  if (!jsonMatch) {
    return {};
  }

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
  if (!categoryName) return 23; // Other
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
