# 🚀 AI Features: Free vs Pro — Implementation Roadmap

> **Expense Tracker App (v2.74.3)**  
> Last updated: August 25, 2026

---

## 📋 Executive Summary

This roadmap outlines the implementation of AI-powered features split into **Free** (daily usability) and **Pro** (premium AI-heavy) tiers. The goal is to provide genuine value to free users while creating a compelling reason to upgrade to Pro — leveraging AI API costs as the natural monetization lever.

---

## ✅ Implementation Progress

| Phase | Status | Notes |
|-------|--------|-------|
| **1.1 Offline Voice Parser** | ✅ Complete | Parser + domain model + Hilt module + UI + tests all done. |
| **1.2 Auto Category Detection** | ✅ Complete | Standalone CategoryPredictor + 55 tests. OfflineVoiceParser delegates to it. |
| **1.3 Auto Payment Method Detection** | ✅ Complete | PaymentMethodPredictor + DataStore learning + 16 tests. Auto-fills in AddTransactionScreen. |
| **2 Gemini AI Voice Parser** | ✅ Complete | GeminiVoiceParser + AiUsageTracker + Cloud Function + 10/day free limit + internet check. |
| **3 Enhanced SMS Detection** | 🟡 ~30% | Currency-aware regex + bare amount fallback done. Auto-add logic pending. |
| **4 Voice Home Widget** | ⬜ Not Started | — |
| **5 Unlimited Gemini (Pro)** | ⬜ Not Started | — |
| **6 AI Monthly Insights** | ⬜ Not Started | — |
| **7 AI Budget Suggestions** | ⬜ Not Started | — |
| **8 Receipt OCR + AI** | ⬜ Not Started | — |
| **9 Bulk SMS Import** | ⬜ Not Started | — |
| **10 Google Assistant Actions** | ⬜ Not Started | — |
| **11 Merchant Memory Sync** | ⬜ Not Started | — |

### Completed Files

```
app/src/main/java/com/mknlabs/expensetracker/
├── ai/
│   ├── CategoryPredictor.kt               ✅ Standalone, injectable category predictor (27 merchant + 100+ keyword rules)
│   ├── PaymentMethodPredictor.kt          ✅ Offline, DataStore-backed payment method predictor
│   └── offline/
│       └── OfflineVoiceParser.kt          ✅ Hilt-injectable, delegates category detection to CategoryPredictor
├── domain/
│   ├── models/
│   │   ├── CategoryPrediction.kt          ✅ Data model + PredictionConfidence + PredictionSource enums
│   │   └── ParsedVoiceTransaction.kt      ✅ Data model + VoiceConfidence enum
│   └── repository/
│       ├── CategoryPredictorRepository.kt ✅ Interface for auto category detection
│       └── VoiceParserRepository.kt       ✅ Interface + VoiceParseResult sealed class
├── data/local/
│   ├── PaymentMethodLearningStore.kt      ✅ DataStore-backed merchant→payment method learning store
│   └── SmsLearningStore.kt                ✅ DataStore-backed merchant→category learning store
├── di/
│   ├── CategoryPredictorModule.kt         ✅ Hilt @Binds binding for CategoryPredictor
│   ├── PaymentMethodPredictorModule.kt    ✅ Hilt @Binds + @Provides for PaymentMethodPredictor
│   └── VoiceParserModule.kt               ✅ Hilt @Binds binding for VoiceParser
├── ui/
│   ├── components/
│   │   └── VoiceInputSheet.kt             ✅ Bottom sheet: Listening/Processing/Result/Error states
│   └── viewmodels/
│       ├── PaymentMethodPredictorViewModel.kt ✅ @HiltViewModel: predict + learn + StateFlow
│       └── VoiceAddViewModel.kt           ✅ @HiltViewModel: state machine + parser integration
├── sms/
│   ├── SmsRegex.kt                        ✅ Currency-aware regex + BARE_AMOUNT fallback
│   ├── SmsParser.kt                       ✅ currencySymbol parameter + fallback
│   ├── SmsDetector.kt                     ✅ currencySymbol parameter
│   └── SmsReceiver.kt                     ✅ Reads user's currency from AppSettings

app/src/main/AndroidManifest.xml           ✅ RECORD_AUDIO permission added

app/src/main/res/values/
└── strings.xml                            ✅ Voice parser + error string resources added

app/src/test/java/com/mknlabs/expensetracker/
├── ai/
│   ├── CategoryPredictorTest.kt           ✅ 55 unit tests for category prediction
│   └── PaymentMethodPredictorTest.kt      ✅ 16 unit tests for payment method prediction
├── ai/offline/
│   └── OfflineVoiceParserTest.kt          ✅ 55 unit tests (updated for CategoryPredictor injection)
├── sms/
│   ├── SmsParserTest.kt                   ✅ SBI/Kotak test cases added
│   ├── SmsParserDebugTest.kt              ✅ Debug test for bare amount detection
│   ├── SmsRepositoryTest.kt               ✅ Fixed missing interface method stub
│   └── ui/viewmodels/
│       ├── SettingsViewModelTest.kt       ✅ Fixed missing auth method stubs
│       └── SmsChangeViewModelTest.kt      ✅ Fixed missing DAO method stub
```

### What's Working

- **CategoryPredictor** — Standalone, injectable category detection for any text input
  - 27 merchant rules (Swiggy, Zomato, Amazon, Uber, Netflix, Walmart, etc.)
  - 100+ keyword rules across 20+ categories (Food, Travel, Shopping, Bills, Health, etc.)
  - User correction learning via `SmsLearningStore` overrides (consulted first)
  - Confidence scoring: HIGH / MEDIUM / LOW with source tracking (USER_LEARNED, MERCHANT_MATCH, KEYWORD_MATCH, FALLBACK)
  - Works for voice, SMS, or manual text input
  - 55 unit tests

- **OfflineVoiceParser** — Parses natural language voice input into structured transactions
  - Amount extraction: `$45`, `₹500`, `Rs 500`, `INR 2500`, `500 rupees`, bare numbers
  - Transaction type detection: income vs expense from keywords
  - Category detection: delegated to CategoryPredictor (70+ keywords)
  - Merchant extraction: `at Walmart`, `from Amazon`, `to John` patterns
  - Date extraction: `today`, `yesterday`, `3 days ago`, `2 weeks ago`, `last Tuesday`
  - Confidence scoring: HIGH / MEDIUM / LOW based on parsed fields
  - Note generation: clean text from voice input

- **Voice Input UI** — End-to-end voice transaction flow
  - Mic button in AddTransactionScreen header (top-right)
  - RECORD_AUDIO permission request on first use
  - Android SpeechRecognizer integration with live partial results
  - Bottom sheet with 4 states: Listening (pulsing mic), Processing, Result (parsed preview), Error
  - Auto-fill form fields on confirm (amount, type, category, note, merchant, date)
  - Error handling: no permission, no speech, network error, audio error

- **PaymentMethodPredictor** — Auto payment method detection from merchant history
  - Exact + partial merchant matching (case-insensitive)
  - Learns from user transaction history via DataStore
  - Auto-fills payment method picker in AddTransactionScreen
  - Respects manual user selection (won't override after manual pick)
  - 16 unit tests

- **Currency-Aware SMS Detection** — Reads user's selected currency
  - Dynamic regex generation via `SmsRegex.getAmountRegex(currencySymbol)`
  - Supports: ₹, $, €, £, RM, AED, and more
  - BARE_AMOUNT fallback for banks (e.g. SBI) that omit currency prefix
  - User's currency read from `AppSettingsDataStore` in `SmsReceiver`

- **GEMINI.md Compliance** — All code follows project conventions
  - Interfaces in `domain/repository`
  - Domain models in `domain/models`
  - Hilt `@Inject constructor` (not object)
  - `@Binds` in `di/VoiceParserModule.kt`
  - No hardcoded UI strings (uses `@StringRes Int`)
  - `sealed class` for results
  - `data class` for state models
  - `@HiltViewModel` with `StateFlow` for UI state
  - Route + Content pattern (sheets follow `TransactionNoteBottomSheet` pattern)

---

## 🏗️ Architecture Overview

### Existing Foundation

The app already has a robust monetization system that we'll extend:

```
┌─────────────────────────────────────────────────────────┐
│                    MONETIZATION LAYER                     │
├─────────────────────────────────────────────────────────┤
│  UserTier (FREE / PREMIUM)                              │
│  FeatureRegistry → AccessLevel (FREE / AD_SUPPORTED / PREMIUM) │
│  GatedAction → PremiumGateSheet / AdRewardDialog         │
│  MonetizationRepository → ObserveAccessStatusUseCase     │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                   DATA / AI LAYER                        │
├─────────────────────────────────────────────────────────┤
│  SmsParser (offline regex, currency-aware)              │
│  Room DB (local transactions, categories, merchants)    │
│  Firebase Functions (server-side AI processing)         │
│  DataStore (preferences, settings)                      │
└─────────────────────────────────────────────────────────┘
```

### New Components (Built + Planned)

```
┌─────────────────────────────────────────────────────────┐
│                    AI SERVICES LAYER                      │
├─────────────────────────────────────────────────────────┤
│  ✅ VoiceParserRepository (interface, domain layer)     │
│  ✅ OfflineVoiceParser (rule-based, Hilt-injectable)    │
│  ✅ ParsedVoiceTransaction (domain model)               │
│  ✅ VoiceParserModule (Hilt binding)                    │
│  ✅ VoiceInputSheet (bottom sheet UI)                   │
│  ✅ VoiceAddViewModel (@HiltViewModel, state machine)   │
│  ✅ CategoryPredictor (standalone, Hilt-injectable)     │
│  ✅ CategoryPrediction (domain model + enums)           │
│  ✅ CategoryPredictorRepository (interface)             │
│  ✅ CategoryPredictorModule (Hilt binding)              │
│  ✅ FirebaseGeminiParser (Firebase AI Logic, direct)    │
│  ✅ UserContextProvider (dynamic context from Room DB)   │
│  ✅ ConnectivityHelper (internet availability check)    │
│  ✅ AiUsageTracker (daily limit tracker)                │
│  ⬜ ReceiptOcrService (ML Kit + Gemini)                 │
│  ⬜ InsightsEngine (Gemini-powered analytics)           │
│  ⬜ MerchantMemoryService (cloud sync)                  │
│  ⬜ SmsHistoryImporter (bulk SMS reader)                │
│  ⬜ AssistantService (App Actions / Gemini)             │
└─────────────────────────────────────────────────────────┘
```

---

## 🟢 FREE Features — Implementation Plan

### Phase 1: Voice Transaction Parser (Offline)

> **Priority: HIGH** — Core daily-use feature  
> **Timeline: Week 1–2**  
> **Status: ✅ COMPLETE**

#### 1.1 Offline Voice Parser

**Goal:** Allow users to add transactions via voice with on-device NLU

**Implementation:**

- **New files:**
  - ~~`domain/usecase/ParseVoiceTransactionUseCase.kt`~~ (pending — not critical, ViewModel handles this)
  - ~~`ai/VoiceParserService.kt` (interface)~~ → `domain/repository/VoiceParserRepository.kt` ✅
  - ~~`ai/offline/OfflineVoiceParser.kt`~~ → `ai/offline/OfflineVoiceParser.kt` ✅
  - `ui/components/VoiceInputSheet.kt` ✅
  - `ui/viewmodels/VoiceAddViewModel.kt` ✅

- **Approach:**
  - Use Android's `SpeechRecognizer` API for STT (Speech-to-Text) ✅
  - Build a rule-based NLU parser in Kotlin (no ML dependency) ✅
  - Pattern matching with regex + keyword dictionaries for:
    - ✅ Amount extraction (`$`, `Rs`, `₹`, `INR`, bare numbers)
    - ✅ Category keywords (20+ categories mapped)
    - ✅ Merchant detection (at/from/to patterns)
    - ✅ Date/time relative parsing ("yesterday", "last Tuesday", "3 days ago")

- **Integration with existing systems:**
  - ✅ Reuse `categoryMap` for category resolution
  - ✅ Reuse `transactionTypeMap` for income/expense detection
  - ✅ Auto-fill form fields via VoiceInputSheet → AddTransactionScreen

- **UI:** ✅ Mic button in header → bottom sheet → voice input → parsed preview → confirm → form auto-filled

#### 1.2 Auto Category Detection (Offline)

> **Priority: HIGH**  
> **Timeline: Week 2–3**  
> **Status: ✅ COMPLETE**

**Goal:** Automatically categorize transactions based on merchant/description patterns

**Implementation:**

- ✅ Category detection built into `OfflineVoiceParser.kt` (70+ keywords)
- ✅ Standalone `CategoryPredictor.kt` — injectable, works for any text source (voice, SMS, manual)
  - 27 merchant rules (Swiggy, Zomato, Amazon, Uber, Netflix, etc.)
  - 100+ keyword rules across 20+ categories
  - User correction learning via `SmsLearningStore` overrides (consulted first)
  - Confidence scoring: HIGH / MEDIUM / LOW with source tracking
- ✅ `CategoryPrediction` data model — `categoryId`, `confidence`, `source`, optional `merchant`
- ✅ `CategoryPredictorRepository` interface in `domain/repository`
- ✅ `CategoryPredictorModule` Hilt `@Binds` binding
- ✅ Refactored `OfflineVoiceParser` — delegates to `CategoryPredictor` (removed 200+ lines of duplicate rules)
- ✅ 55 unit tests in `CategoryPredictorTest.kt` — all pass

#### 1.3 Auto Payment Method Detection (Offline)

> **Priority: MEDIUM**  
> **Timeline: Week 3**  
> **Status: ✅ COMPLETE**

**Goal:** Remember merchant → payment method associations

**Implementation:**

- ✅ `PaymentMethodPredictor.kt` — offline, injectable predictor
  - Exact + partial merchant matching
  - Learns from user transaction history via `PaymentMethodLearningStore`
  - Auto-populates payment method in AddTransactionScreen
  - User can override (learns from corrections)
- ✅ `PaymentMethodLearningStore.kt` — DataStore-backed merchant→paymentMethodId store
- ✅ `PaymentMethodPredictorRepository.kt` — interface: predict(), learn(), forget()
- ✅ `PaymentMethodPredictorViewModel.kt` — @HiltViewModel with StateFlow
- ✅ `PaymentMethodPredictorModule.kt` — Hilt bindings
- ✅ Integrated into `AddTransactionScreen.kt` — auto-fills on note change, learns on save
- ✅ 16 unit tests in `PaymentMethodPredictorTest.kt` — all pass

---

### Phase 2: Gemini AI Voice Parser (Free Tier — 10/day)

> **Priority: HIGH**  
> **Timeline: Week 3–4**  
> **Status: ✅ COMPLETE**

**Goal:** Cloud-powered voice parsing for complex/ambiguous inputs

**Implementation:**

- ✅ `ai/cloud/GeminiVoiceParser.kt` — cloud-powered parser using Firebase AI Logic (not Cloud Functions)
- ✅ `ai/cloud/FirebaseGeminiParser.kt` — Firebase AI Logic integration with `Firebase.ai(backend = GenerativeBackend.googleAI())`
- ✅ `ai/cloud/GeminiApiService.kt` — Firebase Functions callable interface (legacy, kept for reference)
- ✅ `ai/cloud/UserContextProvider.kt` — Dynamic user context from Room DB (categories, payment methods, currency, locale)
- ✅ `ai/AiUsageTracker.kt` — DataStore-backed daily limit tracker (10/day free)
- ✅ `functions/parseVoiceTransaction.js` — Firebase Cloud Function with personalized Gemini prompt
- ✅ Updated `VoiceAddViewModel.kt` — AndroidViewModel with internet check + Gemini fallback
- ✅ Updated `VoiceParserRepository.kt` — added `VoiceParserType` enum and `parserType` to result
- ✅ Added AI features to `FeatureRegistry` (AI_VOICE_OFFLINE, AI_VOICE_GEMINI, AI_VOICE_UNLIMITED)
- ✅ Created `AiUsageModule.kt` — Hilt DataStore provider
- ✅ Created `ConnectivityHelper.kt` — internet availability check utility
- ✅ Added `@Named("offline")` and `@Named("gemini")` Hilt bindings in `VoiceParserModule.kt`
- ✅ Added `payment_methods.getTopPaymentMethods()` DAO query for dynamic context
- ✅ Unit tests for AiUsageTracker

**Parser Selection Flow:**
1. Check internet availability via `ConnectivityHelper`
2. If online AND within daily limit (10/day) → use Gemini AI parser via `FirebaseGeminiParser`
3. If offline OR limit reached → use offline parser
4. Gemini fails → fallback to offline parser
5. Daily limit only decrements when Gemini is used successfully

**Personalized Prompt (Dynamic Context from Room DB):**
- ✅ Currency from `AppSettingsDataStore` (maps currencyId → ISO code)
- ✅ Locale derived from currency region (IN → en-IN, US → en-US, etc.)
- ✅ All categories (including custom) from `CategoryDao.getAllActiveCategories()`
- ✅ All payment methods (including custom) from `PaymentMethodDao.getAllActivePaymentMethods()`
- ✅ Top 3 most-used categories from `CategoryDao.getFrequentlyUsedCategories()`
- ✅ Top 3 most-used payment methods from `PaymentMethodDao.getTopPaymentMethods()`
- ✅ All this context sent to Gemini for accurate predictions

**Security (App Check):**
- ✅ Cloud Functions enforce App Check (`enforceAppCheck: true`)
- ✅ Release builds use `DebugAppCheckProviderFactory` (for testing; switch to `PlayIntegrity` after Play Store release)
- ✅ Debug builds use `DebugAppCheckProviderFactory`
- ✅ `FirebaseGeminiParser` uses `Firebase.ai()` which auto-attaches App Check tokens
- ✅ Console set to "Monitoring only" — will switch to "Enforced" after 1-2 weeks

**Cost:**
- Firebase AI Logic: Free tier available (Gemini Developer API)
- Gemini API: Free tier available (limited RPM/TPM)
- Estimated: ~$0.12/month for 1K users
- Model: `gemini-3.7-flash` (configurable via Firebase Remote Config)
- Deprecated: `gemini-2.0-flash` (shutting down per Firebase FAQ)

---

### Phase 3: SMS Transaction Detection (Enhanced)

> **Priority: HIGH**  
> **Timeline: Week 4–5**  
> **Status: 🟡 ~30% — Currency-aware detection done**

**Goal:** Improve existing SMS detection + auto-add capability

**Implementation:**

- **Enhance existing:** `SmsParser.kt`, `SmsDetector.kt`
  - ✅ Currency-aware regex via `SmsRegex.getAmountRegex(currencySymbol)`
  - ✅ BARE_AMOUNT fallback for banks that omit currency prefix (SBI)
  - ✅ Reads user's currency from `AppSettingsDataStore`
  - Add more bank SMS patterns (extend `SmsRegex.kt`) — pending
  - Support international banks (IN, US, EU, etc.) — pending
  - Add confidence thresholds for auto-add vs manual review — pending

- **New file:** `sms/AutoAddSmsTransaction.kt` (pending)
  - High-confidence SMS (>0.9) → auto-add to transactions
  - Medium confidence (0.7–0.9) → notification for user confirmation
  - Low confidence (<0.7) → saved to inbox for manual review

- **Integration:** Extend `SmsActionReceiver` for auto-add flow

---

### Phase 4: Voice Home Widget

> **Priority: MEDIUM**  
> **Timeline: Week 5–6**

**Goal:** Glance widget with voice input for quick transaction entry

**Implementation:**

- **Extend existing:** `androidx.glance.preview` (already in deps)
- **New files:**
  - `ui/widget/VoiceTransactionWidget.kt` (pending)
  - `ui/widget/VoiceWidgetReceiver.kt` (pending)

- **Widget features:**
  - Display today's spending summary
  - Mic button for voice input
  - Quick-add amount button
  - Recent transaction list (last 3)

- **Integration:** Widget triggers same `VoiceParserRepository` as main app

---

## 💎 PRO Features — Implementation Plan

### Phase 5: Unlimited Gemini AI Voice Parsing

> **Priority: HIGH**  
> **Timeline: Week 4 (parallel with Phase 2)**

**Goal:** Remove daily limit for Pro users

**Implementation:**

- **Extend:** `AiUsageTracker.kt` (pending)
  - Check `UserTier` before enforcing limit
  - Pro users bypass daily counter entirely
  - Free users see upgrade prompt when limit reached

- **UI:** Show "✨ Pro Feature" badge on unlimited parses
  - In `PremiumGateSheet`: "Unlimited AI Voice Parsing" as Pro benefit

---

### Phase 6: AI Monthly Spending Insights

> **Priority: HIGH**  
> **Timeline: Week 6–8**

**Goal:** Personalized AI analysis of spending patterns

**Implementation:**

- **New files:**
  - `ai/insights/InsightsEngine.kt` (pending)
  - `ai/insights/InsightsRepository.kt` (pending)
  - `ui/screens/InsightsScreen.kt` (pending)
  - `ui/viewmodels/InsightsViewModel.kt` (pending)
  - Firebase Function: `/generateInsights` (pending)

- **Data collection:**
  - Aggregate transactions by category, merchant, time period
  - Compare to user's historical averages
  - Detect anomalies (unusual spending spikes)

- **Gemini prompt template:**
  ```
  You are a financial advisor. Analyze this user's spending data:
  
  Month: {month}
  Total spent: {total}
  Categories breakdown: {categories}
  Top merchants: {merchants}
  Previous month comparison: {comparison}
  
  Provide:
  1. Top 3 spending categories
  2. Unusual patterns detected
  3. One actionable saving tip
  4. Budget recommendation for next month
  ```

- **Caching:** Store insights in Room DB, regenerate monthly via `WeeklySummaryWorker` extension

- **UI:** Dedicated "Insights" tab in Analytics screen with Pro badge

---

### Phase 7: AI Budget & Saving Suggestions

> **Priority: MEDIUM**  
> **Timeline: Week 8–9**

**Goal:** Proactive budget recommendations based on AI analysis

**Implementation:**

- **Extend:** `InsightsEngine.kt` (pending)
  - Add `BudgetSuggestion` generation
  - Compare actual vs ideal budget allocation
  - Provide category-specific limits

- **Data model:**
  ```kotlin
  data class BudgetSuggestion(
      val categoryId: Int,
      val suggestedLimit: Long,
      val reasoning: String,
      val confidence: Float,
      val potentialSavings: Long
  )
  ```

- **Integration:** Connect to existing `BudgetAndRecurringScreen`
  - Show AI suggestions as "Recommended" badges on budget categories
  - Auto-apply with user confirmation

---

### Phase 8: Receipt OCR + AI Extraction

> **Priority: HIGH**  
> **Timeline: Week 7–10**

**Goal:** Camera-based receipt scanning with AI-powered data extraction

**Implementation:**

- **New files:**
  - `ai/receipt/ReceiptOcrService.kt` (pending)
  - `ai/receipt/ReceiptParser.kt` (pending)
  - `ui/screens/ReceiptScanScreen.kt` (pending)
  - `ui/viewmodels/ReceiptScanViewModel.kt` (pending)

- **Tech stack:**
  - **ML Kit Text Recognition** (on-device, free, no API costs)
  - **Gemini** for intelligent field extraction (Pro only)

- **Flow:**
  1. Camera capture or gallery import
  2. ML Kit extracts raw text
  3. Gemini parses structured fields:
     - Merchant name
     - Total amount
     - Tax amount
     - Line items (optional)
     - Date
     - Payment method
  4. User confirms/edits extracted data
  5. Save as transaction

- **Dependencies to add:**
  ```kotlin
  implementation("com.google.mlkit:text-recognition:16.0.0")
  implementation("com.google.mlkit:text-recognition-chinese:16.0.0") // for Asian receipts
  implementation("com.google.mlkit:text-recognition-japanese:16.0.0")
  ```

- **Gemini prompt for receipt parsing:**
  ```
  Extract the following fields from this receipt text:
  - merchant: store/restaurant name
  - total: total amount paid
  - tax: tax amount (if separate)
  - items: list of {name, quantity, price}
  - date: transaction date
  - payment_method: cash/card/upi/etc
  
  Receipt text:
  {ocr_text}
  ```

- **UI:** Camera icon in AddTransaction screen → receipt capture → extracted preview → confirm

---

### Phase 9: Import 1+ Year SMS History

> **Priority: MEDIUM**  
> **Timeline: Week 10–11**

**Goal:** Bulk import historical SMS for better AI training and insights

**Implementation:**

- **New files:**
  - `sms/BulkSmsImporter.kt` (pending)
  - `ui/screens/SmsImportScreen.kt` (pending)
  - `ui/viewmodels/SmsImportViewModel.kt` (pending)
  - Worker: `SmsImportWorker.kt` (pending)

- **Approach:**
  - Request `READ_SMS` permission (sensitive, requires justification)
  - Query `content://sms/inbox` for messages from financial senders
  - Batch process through existing `SmsParser`
  - Background worker for large imports (>1000 messages)

- **Progress UI:**
  - Progress bar with count: "Importing... 234/1,892 messages"
  - Pause/resume capability
  - Deduplication against existing transactions

- **Pro gating:** Show as Pro benefit in upgrade flow

---

### Phase 10: Google Assistant / Gemini App Actions

> **Priority: LOW**  
> **Timeline: Week 11–13**

**Goal:** Hands-free transaction entry via voice commands

**Implementation:**

- **New files:**
  - `assistant/AppActionService.kt` (pending)
  - `assistant/TransactionAction.kt` (pending)
  - `res/xml/shortcuts.xml` (App Shortcuts, pending)
  - `res/xml/assistant_actions.xml` (App Actions, pending)

- **Voice commands:**
  ```
  "Hey Google, add expense 50 dollars for lunch"
  "Hey Google, how much did I spend on groceries this month?"
  "Hey Google, show my recent transactions"
  ```

- **Integration:**
  - Use existing `VoiceParserRepository` for parsing
  - Firebase Functions for intent fulfillment
  - Display results via Android notification or launcher activity

- **Dependencies:**
  ```kotlin
  // Already has play-services-auth, may need:
  implementation("com.google.android.gms:play-services-appset:16.0.0")
  ```

---

### Phase 11: AI Merchant Memory Sync Across Devices

> **Priority: LOW**  
> **Timeline: Week 12–14**

**Goal:** Sync merchant preferences and AI learnings across user devices via cloud

**Implementation:**

- **New files:**
  - `data/repository/MerchantMemoryRepository.kt` (pending)
  - `ai/MerchantMemorySyncWorker.kt` (pending)

- **Data model:**
  ```kotlin
  @Entity(tableName = "merchant_memory")
  data class MerchantMemory(
      @PrimaryKey val merchantName: String,
      val defaultCategoryId: Int,
      val defaultPaymentMethodId: Int,
      val lastUsedTimestamp: Long,
      val usageCount: Int,
      val lastSyncTimestamp: Long // For cloud sync
  )
  ```

- **Sync via Firestore:**
  - Store in `users/{uid}/merchant_memory/{merchant}`
  - Bidirectional sync with conflict resolution (last-write-wins)
  - Background sync via existing `SyncWorker`

- **Pro gating:** Cloud sync is Pro-only; free users keep local memory only

---

## 📅 Implementation Timeline

```
Week 1-2:   Phase 1.1 - Offline Voice Parser          ✅ COMPLETE
Week 2-3:   Phase 1.2 - Auto Category Detection       ✅ COMPLETE
Week 3:     Phase 1.3 - Auto Payment Method Detection  ✅ COMPLETE
Week 3-4:   Phase 2 - Gemini AI Voice Parser           ✅ COMPLETE
Week 4-5:   Phase 3 - Enhanced SMS Detection           🟡 ~30%
Week 4:     Phase 5 - Unlimited Gemini for Pro         ⬜ PENDING
Week 5-6:   Phase 4 - Voice Home Widget                ⬜ PENDING
Week 6-8:   Phase 6 - AI Monthly Insights              ⬜ PENDING
Week 7-10:  Phase 8 - Receipt OCR + AI Extraction      ⬜ PENDING
Week 8-9:   Phase 7 - AI Budget Suggestions            ⬜ PENDING
Week 10-11: Phase 9 - Bulk SMS Import                  ⬜ PENDING
Week 11-13: Phase 10 - Google Assistant Actions         ⬜ PENDING
Week 12-14: Phase 11 - Merchant Memory Cloud Sync      ⬜ PENDING
```

### Milestones

| Milestone | Target | Status | Key Deliverables |
|-----------|--------|--------|------------------|
| **M1: Voice MVP** | Week 2 | ✅ 100% | Parser + domain model + Hilt module + VoiceInputSheet + VoiceAddViewModel + 55 tests |
| **M1.2: Category Detection** | Week 3 | ✅ 100% | CategoryPredictor + CategoryPrediction model + 55 tests + OfflineVoiceParser refactored |
| **M1.3: Payment Method Detection** | Week 3 | ✅ 100% | PaymentMethodPredictor + PaymentMethodLearningStore + 16 tests + AddTransactionScreen integration |
| **M2: AI Voice** | Week 4 | ✅ 100% | FirebaseGeminiParser + UserContextProvider + AiUsageTracker + Personalized prompts + App Check enforcement + 10/day limits + internet check + Remote Config model selection |
| **M3: Widget** | Week 6 | ⬜ | Home widget with voice |
| **M4: Insights** | Week 8 | ⬜ | Pro insights dashboard |
| **M5: Receipts** | Week 10 | ⬜ | OCR scanning live |
| **M6: Full Suite** | Week 14 | ⬜ | All features complete |

---

## 🔧 Technical Dependencies

### New Dependencies to Add

```kotlin
// build.gradle.kts additions

// ML Kit (Receipt OCR - free, on-device)
implementation("com.google.mlkit:text-recognition:16.0.0")
implementation("com.google.mlkit:text-recognition-chinese:16.0.0")

// ML Kit Object Detection (receipt boundary detection)
implementation("com.google.android.gms:play-services-mlkit-image-labeling:17.0.9")

// CameraX (Receipt capture)
implementation("androidx.camera:camera-camera2:1.4.0")
implementation("androidx.camera:camera-lifecycle:1.4.0")
implementation("androidx.camera:camera-view:1.4.0")

// OkHttp (Gemini API calls - if not using Firebase Functions)
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// ML Kit Language ID (for voice parsing optimization)
implementation("com.google.mlkit:language-id:17.0.5")
```

### Firebase Cloud Functions

New functions to deploy:

```javascript
// functions/src/index.js additions

// Voice transaction parsing
exports.parseVoiceTransaction = functions.https.onCall(async (data, context) => {
  // Gemini API call for complex parsing
});

// Generate monthly insights
exports.generateInsights = functions.https.onCall(async (data, context) => {
  // Gemini API call for spending analysis
});

// Receipt field extraction
exports.extractReceiptData = functions.https.onCall(async (data, context) => {
  // Gemini API call for receipt parsing
});
```

### Database Schema Extensions

```sql
-- New Room tables

CREATE TABLE merchant_memory (
    merchant_name TEXT PRIMARY KEY,
    default_category_id INTEGER NOT NULL,
    default_payment_method_id INTEGER,
    last_used_timestamp INTEGER NOT NULL,
    usage_count INTEGER NOT NULL DEFAULT 1,
    last_sync_timestamp INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE ai_usage_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    feature TEXT NOT NULL, -- "voice_gemini", "receipt_ocr", "insights"
    timestamp INTEGER NOT NULL,
    tokens_used INTEGER,
    was_pro INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE cached_insights (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    month TEXT NOT NULL, -- "2026-08"
    insight_type TEXT NOT NULL,
    content TEXT NOT NULL,
    generated_at INTEGER NOT NULL,
    UNIQUE(month, insight_type)
);

CREATE TABLE receipt_images (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    image_uri TEXT NOT NULL,
    ocr_text TEXT,
    extracted_data TEXT, -- JSON
    transaction_id INTEGER,
    created_at INTEGER NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);
```

---

## 💰 Cost Projections

### Gemini API Costs (Estimates)

| Feature | Calls/User/Month | Tokens/Call | Monthly Cost/1K Users |
|---------|------------------|-------------|----------------------|
| Voice Parse (Free 10/day) | 300 | ~200 | $0.12 |
| Insights (Pro) | 1 | ~2000 | $0.08 |
| Receipt OCR (Pro) | 20 | ~500 | $0.08 |
| Budget Suggestions (Pro) | 4 | ~1500 | $0.06 |
| **Total** | | | **~$0.34/1K users** |

> Gemini 1.5 Flash pricing: $0.075/1M input tokens, $0.30/1M output tokens  
> First 1M tokens/month free on Google AI Studio

### ML Kit Costs

- **Free** for on-device processing (no API calls)
- Text recognition runs locally, no cloud dependency

### Firebase Functions Costs

- **Free tier:** 2M invocations/month, 400K GB-seconds
- Estimated: ~10K invocations/month for 1K users → **Free**

---

## 🎯 Feature Gating Configuration

Add to `FeatureRegistry.kt`:

```kotlin
// New AI features to add to Feature enum

enum class Feature(val id: String, val displayName: String) {
    // ... existing features ...
    
    // FREE AI Features
    AI_VOICE_OFFLINE("ai_voice_offline", "Voice Add Transaction (Offline)"),
    AI_VOICE_GEMINI("ai_voice_gemini", "Voice Add Transaction (AI)"),
    AI_CATEGORY_DETECTION("ai_category_detection", "Auto Category Detection"),
    AI_PAYMENT_MEMORY("ai_payment_memory", "Payment Method Memory"),
    AI_VOICE_WIDGET("ai_voice_widget", "Voice Home Widget"),
    
    // PRO AI Features
    AI_VOICE_UNLIMITED("ai_voice_unlimited", "Unlimited AI Voice Parsing"),
    AI_INSIGHTS("ai_insights", "AI Spending Insights"),
    AI_BUDGET_SUGGESTIONS("ai_budget_suggestions", "AI Budget Suggestions"),
    AI_RECEIPT_OCR("ai_receipt_ocr", "Receipt OCR + AI Extraction"),
    AI_SMS_IMPORT("ai_sms_import", "Import 1+ Year SMS History"),
    AI_ASSISTANT("ai_assistant", "Google Assistant Integration"),
    AI_MERCHANT_SYNC("ai_merchant_sync", "Merchant Memory Cloud Sync"),
}

// Registry additions
Feature.AI_VOICE_OFFLINE to AccessLevel.FREE,
Feature.AI_VOICE_GEMINI to AccessLevel.FREE, // with daily limit
Feature.AI_CATEGORY_DETECTION to AccessLevel.FREE,
Feature.AI_PAYMENT_MEMORY to AccessLevel.FREE,
Feature.AI_VOICE_WIDGET to AccessLevel.FREE,
Feature.AI_VOICE_UNLIMITED to AccessLevel.PREMIUM,
Feature.AI_INSIGHTS to AccessLevel.PREMIUM,
Feature.AI_BUDGET_SUGGESTIONS to AccessLevel.PREMIUM,
Feature.AI_RECEIPT_OCR to AccessLevel.PREMIUM,
Feature.AI_SMS_IMPORT to AccessLevel.PREMIUM,
Feature.AI_ASSISTANT to AccessLevel.PREMIUM,
Feature.AI_MERCHANT_SYNC to AccessLevel.PREMIUM,
```

---

## 📊 Success Metrics

### User Engagement

| Metric | Target (Month 1) | Target (Month 3) |
|--------|------------------|------------------|
| Voice transactions/day | 2 per user | 5 per user |
| AI parse usage (Free) | 60% hit 10/day limit | 40% (more aware) |
| Receipt scans/month (Pro) | 5 per user | 12 per user |
| Insights views/week (Pro) | 2 per user | 4 per user |

### Monetization

| Metric | Target (Month 1) | Target (Month 3) |
|--------|------------------|------------------|
| Free→Pro conversion from AI | 2% | 5% |
| AI feature usage (Pro users) | 80% | 90% |
| Retention lift (Pro vs Free) | +10% | +20% |

### Technical

| Metric | Target |
|--------|--------|
| Voice parse latency (offline) | <500ms |
| Voice parse latency (Gemini) | <2s |
| Receipt OCR accuracy | >90% |
| Insight generation time | <5s |
| Offline capability | 100% of Free features |

---

## 🔒 Privacy & Security Considerations

1. **Voice data:** Processed on-device (offline) or sent to Google Gemini (cloud). Clear disclosure in privacy policy.

2. **Receipt images:** Stored locally only. Cloud processing via Firebase Functions (images not persisted server-side).

3. **SMS data:** Read permission requires Play Store declaration. Process on-device, never upload raw SMS.

4. **Merchant memory:** Cloud sync encrypted in transit (TLS) and at rest (Firestore default encryption).

5. **Analytics:** AI usage logged locally for limits. Opt-in for anonymous usage analytics via Firebase Analytics.

---

## 🎨 UI/UX Design Guidelines

### Voice Input Flow (IMPLEMENTED)
```
[+] button (bottom bar) → AddTransactionScreen → [🎙️ mic button (header)]
  → [Bottom Sheet: Listening...] → [Live transcript]
  → [Parsed Preview Card] → [Confirm/Retry]
  → [Form auto-filled] → [Save]
```

### Receipt Scan Flow
```
[Camera Icon] → [Camera/Gallery] → [Processing...] → [Extracted Fields Card] → [Confirm]
```

### Insights Dashboard
```
[Analytics Tab] → [AI Insights Card (Pro badge)] → [Monthly Summary] → [Tips & Suggestions]
```

### Upgrade Prompt
```
[Limit Reached Banner] → [PremiumGateSheet] → ["Unlock Unlimited AI" CTA]
```

---

## ⚠️ Risk Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Gemini API costs spike | High | Daily limits + caching + cost alerts |
| OCR accuracy poor on receipts | Medium | ML Kit fallback + user correction UI |
| SMS permission rejected by Play Store | High | Declare in Data Safety, provide manual import alternative |
| Voice recognition accuracy | Medium | Offline parser as fallback, clear error messages |
| Offline parser limitations | Low | Always offer Gemini option (with limit) |

---

## 📝 Next Steps

1. **Immediate (This Week):**
   - ~~Set up Gemini API credentials~~
   - ~~Create `ai/` package structure~~
   - ~~Implement `OfflineVoiceParser.kt`~~
   - ~~Build `VoiceInputSheet.kt` UI component~~
   - ~~Build `VoiceAddViewModel.kt`~~
   - ~~Build standalone `CategoryPredictor.kt`~~
   - Add feature flags to `FeatureRegistry`

2. **Short-term (Week 1–2):**
   - ~~Integrate voice input into transaction flow~~
   - ~~Fix pre-existing test compilation errors~~
   - ~~Build standalone `CategoryPredictor.kt`~~
   - Build `PaymentMethodPredictor.kt` (Phase 1.3)

3. **Medium-term (Week 3–6):**
   - Deploy Firebase Functions for cloud parsing
   - Implement daily usage limits
   - Build home widget

4. **Long-term (Week 7–14):**
   - Receipt OCR pipeline
   - Insights engine
   - App Actions integration

---

## 📚 References

- [Gemini API Documentation](https://ai.google.dev/docs)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition)
- [Firebase Cloud Functions](https://firebase.google.com/docs/functions)
- [Android SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer)
- [App Actions for Android](https://developer.google.com/assistant/app-actions)
- [Glance Widgets](https://developer.android.com/develop/ui/compose/glance)
