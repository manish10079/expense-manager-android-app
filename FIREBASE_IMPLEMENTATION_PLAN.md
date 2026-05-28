# Firebase & Cloud Integration Roadmap 🚀

This document outlines the strategic plan for transitioning **Expense Tracker** from an offline-only app to a cloud-connected service using the Firebase ecosystem.

---

## 1. Firebase Remote Config (Global Control)
*Control the app's behavior for all users without a Play Store update.*

- [ ] **Force Update:** Implement `min_required_version` logic to prompt users to update.
- [ ] **Maintenance Mode:** Add `is_under_maintenance` flag to show a "Be Right Back" screen.
- [ ] **Feature Flags:** Toggle `is_sync_enabled` or `is_ads_enabled` remotely.
- [ ] **Ad Tuning:** Control `interstitial_ad_interval` and `native_ad_placement` from the console.
- [ ] **Global Promo Codes:** Set a `current_promo_code` for seasonal giveaways.

## 2. Firestore Cloud Sync (Data Management)
*Real-time synchronization of financial data across devices.*

- [ ] **User Data Mapping:** Sync Transactions, Budgets, and Categories to a per-user Firestore collection.
- [ ] **Bidirectional Sync:** Ensure local Room DB and remote Firestore stay consistent.
- [ ] **Offline Persistence:** Utilize Firebase's local cache for seamless offline use.
- [ ] **Security Rules (Item 14):** Enforce `request.auth.uid == resource.data.uid` to protect user privacy.

## 3. User Access & Moderation (Firestore)
*Control access and permissions for specific individual users.*

- [ ] **The "Ban Hammer":** Check a user's Firestore document for `isBlocked: true` on every app startup.
- [ ] **Shadow Banning:** Logic to allow bad actors to "sync" locally while ignoring their data on the backend.
- [ ] **Account Suspension:** Direct integration with Firebase Auth to disable malicious accounts.

## 4. Free Passes & Premium Logic (Firestore)
*Award "Free Premium" or VIP status from the backend.*

- [ ] **VIP List:** A collection of User IDs that bypass billing and get automatic Pro access.
- [ ] **Gifted Subscriptions:** Add a `premium_expiry` field (Timestamp) to user documents.
- [ ] **Remote Premium Revoke:** Implement a `force_revoke_premium` flag to strip access from accounts that abused leaked codes.
- [ ] **Backend-Verified Premium:** Update `MonetizationViewModel` to check both local Google Play Billing and backend "Gifted/VIP" status.

## 5. Security & Integrity (Play Integrity API)
*Protect the backend from unauthorized access and scripts.*

- [ ] **Firebase App Check (Item 9):** Implement App Check to verify that ONLY the official, untampered app talks to the database.
- [ ] **Play Integrity Provider:** Link the app with the Google Play Console for strong device attestation.

---

## 🛠️ Implementation Phases

### Phase 1: SDK Foundation
- Add `google-services.json`.
- Integrate Firebase BOM, Auth, Firestore, and Remote Config SDKs.
- Initialize Firebase in `MainActivity`.

### Phase 2: Configuration & Auth
- Implement `RemoteConfigManager` for fetching global settings.
- Setup Google Sign-In and Anonymous Auth for seamless onboarding.

### Phase 3: Sync & Moderation
- Build the `FirestoreRepository` for background data synchronization.
- Implement the "Blocked User" and "VIP Pass" checks.

---
**Status:** 🟡 Not Started (Awaiting `google-services.json`)
