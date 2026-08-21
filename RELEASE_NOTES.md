# Expense Tracker v2.67.0 (168) — Internal Testing

**August 21, 2026 · Android 7.0+ · Jetpack Compose + Material 3**

---

## What's Included

**Transactions** — Quick keypad entry, itemized calculator, search/sort/filter, paginated list with bulk select, swipe-to-delete, customizable cards.

**Budgets & Recurring** — Monthly category budgets with 75%/90% alerts, recurring rules (daily/weekly/monthly/yearly) with tiered Pro gating and duplicate detection.

**Analytics** — Weekly/monthly/yearly/custom trends, spending breakdowns by category & payment mode, smart insights.

**Calendar** — Year & month views with per-day spending totals.

**Security** — PIN + biometric app lock, encrypted backups (AES-256-GCM), screenshot blocking, root detection, server-verified Pro.

**Cloud Sync** — Google/email/magic link sign-in, Firestore cross-device sync, JSON & SQLite export/import, automated backups.

**Notifications** — 8 categories (reminders, budget alerts, large transactions, weekly summaries, goal milestones, bill reminders, backup/sync status), FCM push support.

**SMS Import** — Auto-parse bank SMS with one-tap save, spam filtering.

**Monetization** — AdMob with UMP consent, Pro membership (6/12 month plans), rewarded ad unlock.

**Adaptive UI** — Portrait/landscape, tablets, foldables, large font scaling.

---

## Recent Changes (v2.58–v2.67)
- Paginated transaction list with "Select all in view"
- Tiered recurring rule creation gating & duplicate detection
- Full adaptive layout refactor (rotation, tablets, foldables)
- Notification center with 8 configurable categories
- Pricing carousel with 6/12 month subscription plans
- Recurring transaction badge on cards
- Landscape home layout + immersive status bar

---

## Known Considerations
- Test ad units are active — production IDs will be swapped before public release
- Cloud Functions (Blaze plan) required for ProPass redemption
