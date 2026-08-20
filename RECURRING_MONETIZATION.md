# Recurring Transaction — Monetization Strategy

> Psychologically triggered tier design to maximize conversion from Free → Ad-Supported → Premium

---

## Current State (Before)

| Component | Current Gating |
|-----------|---------------|
| `RECURRING_RULES` (create) | **PREMIUM** only |
| `RECURRING_RULE_EDIT` (edit) | **AD_SUPPORTED** |
| View recurring list | Free |
| Toggle enable/disable | Free |
| Delete rules | Free |
| Bill notifications | Free |
| Auto-create transactions | Free (all rules) |

### Problem

Users can't even *try* creating a single recurring rule without paying ₹99/month. That's a massive friction point — they never experience the value, so they never convert.

---

## Recommended Tier Design (After)

> **Instructions:** Edit the `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]` placeholder on each feature to decide its tier.

---

### 1. CREATE RECURRING RULE ✅ IMPLEMENTED

**Description:** User creates a new recurring expense (e.g. rent, Netflix, phone bill) with frequency and repeat count. The app auto-generates future transactions based on this rule.

- Create 1st recurring rule — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`
- Create 2nd & 3rd recurring rules — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`
- Create 4th+ recurring rules (unlimited) — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 2. EDIT RECURRING RULE ✅ IMPLEMENTED

**Description:** User modifies an existing recurring rule — change amount, frequency, repeat count, or anchor date. All fields are editable.

- Edit any existing rule — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 3. DELETE RECURRING RULE ✅ IMPLEMENTED

**Description:** User permanently removes a recurring rule. Future auto-generated transactions from this rule stop being created.

- Delete any recurring rule — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 4. TOGGLE ENABLE/DISABLE ✅ IMPLEMENTED

**Description:** User pauses or resumes a recurring rule without deleting it. Disabled rules skip their next occurrence but can be re-enabled anytime.

- Enable/disable a recurring rule — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 5. VIEW RECURRING LIST ✅ IMPLEMENTED

**Description:** User sees all their recurring rules in one place — name, amount, frequency, next run date, and enable/disable status. Displayed on the Budget & Recurring screen.

- View all recurring rules — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 6. FREQUENCY — MONTHLY ✅ IMPLEMENTED

**Description:** Rule repeats every month on the same date. The most common frequency for bills like rent, EMIs, and subscriptions.

- Monthly frequency — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 7. FREQUENCY — DAILY ✅ IMPLEMENTED

**Description:** Rule repeats every day. Used for daily expenses like chai, auto fare, or lunch. High-frequency = more ad impressions if gated.

- Daily frequency — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 8. FREQUENCY — WEEKLY ✅ IMPLEMENTED

**Description:** Rule repeats every week on the same day. Used for weekly groceries, cleaning service, or nanny salary.

- Weekly frequency — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 9. FREQUENCY — YEARLY ✅ IMPLEMENTED

**Description:** Rule repeats once a year. Used for annual payments like insurance premiums, domain renewals, or festival gifts.

- Yearly frequency — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 10. CUSTOM INTERVAL ❌ NOT IMPLEMENTED

**Description:** Rule repeats at a non-standard interval — every 2 weeks, every 3 months, every 6 months. Advanced scheduling for power users.

> **Note:** `RecurringFrequency` enum only has `Daily`, `Weekly`, `Monthly`, `Yearly`. The `intervalCount` field exists but is always `1`. No UI or logic for custom intervals (e.g. every 2 weeks, every 3 months).

- Custom interval (e.g. every 2 weeks, every 3 months) — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 11. AUTO-GENERATE TRANSACTIONS ✅ IMPLEMENTED

**Description:** Background worker (`RecurringTransactionWorker`) automatically creates new transactions when a recurring rule's due date arrives. Runs daily via WorkManager and backfills missed occurrences.

- Auto-generate transactions from rules — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 12. BILL DUE NOTIFICATIONS

#### 12.1 Default reminders (7/3/1 day before) ✅ IMPLEMENTED

**Description:** Push notification reminding user about upcoming recurring bills at 7 days, 3 days, and 1 day before due date. Builds daily app dependency.

> **Note:** Hardcoded in `ADVANCE_WINDOW_DAYS = listOf(7, 3, 1, 0)` in `RecurringTransactionWorker.kt`.

- Default bill reminders (7/3/1 day before) — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

#### 12.2 Custom reminder days ❌ NOT IMPLEMENTED

**Description:** User can set custom reminder days (e.g. 14 days, 2 days, same day) instead of the default 7/3/1.

> **Note:** `ADVANCE_WINDOW_DAYS` is hardcoded to `[7, 3, 1, 0]`. No UI exists to customize these. Would need: new field on `RecurringTransactionRule`, a settings UI, and worker logic to use custom windows.

- Custom reminder days (e.g. 14 days, 2 days, same day) — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 13. RECURRING BADGE ON TRANSACTIONS ✅ IMPLEMENTED

**Description:** A visual badge/icon shown on transaction cards that were auto-generated by a recurring rule. Helps user distinguish manual vs automated entries.

> **Note:** `RecurringTransactionRule` has `transactionId`. The `isRecurring` flag is computed via `isRecurringTransaction()` utility. Badge is shown in `TransactionCard.kt` when `isProUser && isRecurring`.

- Recurring badge on transaction cards — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 14. WEEKLY BILL SUMMARY ⚠️ PARTIALLY IMPLEMENTED (WRONG DESCRIPTION)

**Description:** A weekly notification summarizing spending.

> **Note:** `WeeklySummaryWorker` exists and runs on Sundays. However, it shows **general spending** (total ₹ spent, delta %, top category), NOT upcoming recurring bills. The .md description was wrong — it said "upcoming recurring bills" but the actual implementation is a spending recap. To make it show upcoming bills, new logic would need to query upcoming `RecurringTransactionRule` occurrences.

- Weekly spending summary (current implementation) — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`
- Weekly upcoming bills summary (NOT YET BUILT) — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 15. NOTIFICATION MUTE PER RULE ⚠️ BACKEND ONLY (NO UI)

**Description:** User can mute notifications for a specific recurring rule while keeping others active. Useful for rules they've memorized (e.g. daily chai).

> **Note:** The `notificationsEnabled: Boolean = true` field exists on `RecurringTransactionRule` model and is checked in `RecurringTransactionWorker`. However, there is **NO UI toggle** — `BudgetRecurringExpenseUi` doesn't have a `notificationsEnabled` field, `RecurringExpenseCard` has no mute toggle, and no `onNotificationsEnabledChange` callback exists. The backend is ready but the UI needs to be built.

- Per-rule notification mute — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 16. RECURRING ANALYTICS ❌ NOT IMPLEMENTED

**Description:** Insights into recurring spending — projected monthly recurring total, breakdown by category, comparison with last month, and cash flow forecast showing future obligations.

> **Note:** The `CashFlowCard` in `AnalyticsScreen.kt` shows income vs expense ratio, but that's a general analytics feature. There is NO recurring-specific analytics — no "projected monthly recurring spend", no "recurring breakdown by category", no "cash flow forecast for upcoming obligations". All 3 sub-items below would need to be built from scratch.

- Projected monthly recurring spend — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`
- Recurring breakdown by category — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`
- Cash flow forecast (next 3/6/12 months) — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 17. RECURRING RULE TEMPLATES ❌ NOT IMPLEMENTED

**Description:** Pre-built templates for common bills (Rent, Electricity, Phone, Netflix, Insurance, SIP). One-tap setup instead of manual entry.

> **Note:** No template system exists anywhere in the codebase. Would need: a templates data source (local JSON or remote), a templates UI picker, and a "use template" action that pre-fills the creation form.

- Access recurring templates — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 18. BULK IMPORT RECURRING RULES ❌ NOT IMPLEMENTED

**Description:** Import multiple recurring rules from a CSV/JSON file or from a previous app backup. Power user migration tool.

> **Note:** `LegacyImportRepository` handles JSON migration from older app versions, but not bulk recurring rule import. The Firestore sync pulls rules from cloud, but there's no user-facing bulk import (CSV/JSON upload).

- Bulk import recurring rules — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 19. SMART DUPLICATE DETECTION ❌ NOT IMPLEMENTED

**Description:** When creating a new rule, the app detects if a similar rule already exists (same category + similar amount + same frequency) and warns user to avoid duplicates.

> **Note:** SMS duplicate detection exists for transactions (`isDuplicate` in `SmsRepository`), but there's NO duplicate check when creating recurring rules. User can create 10 identical rent rules without any warning.

- Duplicate rule detection — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

### 20. RECURRING RULE NOTES/DESCRIPTION ❌ NOT IMPLEMENTED

**Description:** User can add a custom note or description to each recurring rule (e.g. "Netflix Premium - family plan", "Rent - 2BHK Koramangala"). Helps identify rules at a glance.

> **Note:** `RecurringTransactionRule` model has NO `notes` or `description` field. The rule references a `transactionId` which has a `note` field on the `Transaction` model, but that's the transaction note, not a dedicated rule description. Would need: new field on model + DB migration + creation/edit UI update.

- Add notes to recurring rules — `[FREE / AD_SUPPORTED (INTERSTITIAL) / PRO]`

---

## Implementation Status Summary

| # | Feature | Status |
|---|---------|--------|
| 1 | Create Recurring Rule | ✅ Implemented | 
| 2 | Edit Recurring Rule | ✅ Implemented |
| 3 | Delete Recurring Rule | ✅ Implemented |
| 4 | Toggle Enable/Disable | ✅ Implemented |
| 5 | View Recurring List | ✅ Implemented |
| 6 | Frequency — Monthly | ✅ Implemented |
| 7 | Frequency — Daily | ✅ Implemented |
| 8 | Frequency — Weekly | ✅ Implemented |
| 9 | Frequency — Yearly | ✅ Implemented |
| 10 | Custom Interval | ❌ Not implemented |
| 11 | Auto-Generate Transactions | ✅ Implemented |
| 12.1 | Bill Reminders (7/3/1) | ✅ Implemented |
| 12.2 | Custom Reminder Days | ❌ Not implemented |
| 13 | Recurring Badge on Cards | ✅ Implemented |
| 14 | Weekly Summary | ⚠️ Exists but shows spending, not recurring bills |
| 15 | Notification Mute Per Rule | ⚠️ Backend only (no UI toggle) |
| 16.1 | Projected Monthly Recurring Spend | ❌ Not implemented |
| 16.2 | Recurring Breakdown by Category | ❌ Not implemented |
| 16.3 | Cash Flow Forecast | ❌ Not implemented |
| 17 | Recurring Templates | ❌ Not implemented |
| 18 | Bulk Import Rules | ❌ Not implemented |
| 19 | Duplicate Detection | ❌ Not implemented |
| 20 | Rule Notes/Description | ❌ Not implemented |

**Score: 12 implemented, 9 not implemented, 1 partially implemented**

---

## The Psychological Triggers

### 1. Foot-in-the-Door (Free 1st Rule)

> User creates their rent rule for free → sees ₹15,000 auto-added every month → tells themselves "this app is essential" → wants more rules for electricity, Netflix, phone bill → hits the ad gate → watches an ad → habit forms

### 2. Loss Aversion (They already have 3 rules)

> After 3 rules, the user has built recurring automation. They *depend* on it. Taking it away feels like loss. The upgrade path feels like protecting what they've built, not spending money.

### 3. Effort Escalation (Editing costs an ad)

> Every edit = small ad. They invest effort + watch ads → sunk cost fallacy → "I've already invested so much, might as well go Pro"

### 4. Frequency Locking

> Monthly bills are free/ad. But a user with daily expenses (chai, auto, lunch) wants Daily frequency → that's ad-gated → they watch ads daily → ad dependency grows

### 5. Rule Count Cliff

> 1st free, 2-3 ad, 4+ premium. The jump from 3→4 is where most users hit because by then they have: rent, electricity, phone, Netflix, insurance, SIP... The app becomes irreplaceable.

---

## Conversion Funnel

```
FREE           →  1 rule, view/toggle/delete, notifications
                 ↓  "I love this, I need more"
AD-SUPPORTED   →  2-3 rules, edit, daily/weekly freq
                 ↓  "I use this every day, ads are annoying"
PREMIUM        →  Unlimited rules, custom intervals, analytics, templates
```

The cliff is at **rule #4** — by then the user has automated enough bills that the app feels essential. That's when ₹99/mo feels like a no-brainer instead of an ask.

---

## Implementation Notes

### FeatureRegistry Changes

```kotlin
// BEFORE
Feature.RECURRING_RULES to AccessLevel.PREMIUM,
Feature.RECURRING_RULE_EDIT to AccessLevel.AD_SUPPORTED,

// AFTER — update these based on your placeholder selections above
Feature.RECURRING_RULES to AccessLevel.FREE,                      // 1st rule free
Feature.RECURRING_RULES_MULTI to AccessLevel.AD_SUPPORTED,        // 2nd & 3rd rules
Feature.RECURRING_RULES_UNLIMITED to AccessLevel.PREMIUM,         // 4th+ rules
Feature.RECURRING_RULE_EDIT to AccessLevel.AD_SUPPORTED,
Feature.RECURRING_FREQUENCY_DAILY to AccessLevel.AD_SUPPORTED,
Feature.RECURRING_FREQUENCY_WEEKLY to AccessLevel.AD_SUPPORTED,
Feature.RECURRING_FREQUENCY_YEARLY to AccessLevel.PREMIUM,
Feature.RECURRING_CUSTOM_INTERVAL to AccessLevel.PREMIUM,
Feature.RECURRING_BULK_IMPORT to AccessLevel.PREMIUM,
Feature.RECURRING_ANALYTICS to AccessLevel.PREMIUM,
Feature.RECURRING_TEMPLATES to AccessLevel.PREMIUM,
```

### Rule Count Logic

```kotlin
// In the creation flow, check current rule count:
val currentRuleCount = recurringRules.size
when {
    currentRuleCount == 0 -> AccessStatus.Granted          // 1st rule: FREE
    currentRuleCount < 3 -> AccessStatus.DeniedAd          // 2nd & 3rd: watch ad
    else -> AccessStatus.DeniedPremium                      // 4th+: Premium
}
```

### Frequency Gating

```kotlin
// Gate frequency options in the editor:
RecurringFrequency.Daily   -> AccessLevel.AD_SUPPORTED
RecurringFrequency.Weekly  -> AccessLevel.AD_SUPPORTED
RecurringFrequency.Monthly -> AccessLevel.FREE
RecurringFrequency.Yearly  -> AccessLevel.PREMIUM
```

---

## Pricing Context

| Plan | Price | Total | Savings |
|------|-------|-------|---------|
| Monthly | ₹99/mo | ₹99 | — |
| 6 Months | ₹79/mo | ₹474 | Save ₹120 (20% off) |
| 12 Months | ₹59/mo | ₹708 | Save ₹480 (40% off) |

---

## Metrics to Track

- [ ] Free → Ad conversion rate (1st rule created → 2nd rule ad watched)
- [ ] Ad → Premium conversion rate (3rd rule ad → subscription purchase)
- [ ] Average rules per user by tier
- [ ] Time from 1st rule to subscription
- [ ] Most common rule #4 (the cliff trigger)
- [ ] Frequency preference distribution (Daily/Weekly/Monthly/Yearly)
