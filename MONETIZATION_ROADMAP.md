# 🗺️ Monetization Implementation Roadmap

This document serves as the master checklist for implementing the monetization strategy in the ExpenseTracker app. It follows a modular, scalable approach aligned with Clean Architecture and Jetpack Compose.

## 🛠️ Phase 1: Foundation & SDK Integration
*Goal: Initialize infrastructure and global ad-state management.*

- [x] **SDK Initialization:** Add Google Mobile Ads SDK dependencies and initialize in `ExpenseTrackerApplication`.
- [x] **Global Ad Toggle:** Update `MonetizationRepository` to expose `isAdsEnabled: Flow<Boolean>` (returning false for `UserTier.PREMIUM`).
- [x] **Ads Coordinator:** Create a Hilt-injected `AdsCoordinator` to handle low-level loading, caching, and display logic.
- [x] **Privacy & Consent:** Implement UMP (User Messaging Platform) for GDPR/CCPA compliance if targeting global markets.

## 🎨 Phase 2: Common UI Layer (Compose) ✅
*Goal: Create reusable wrappers and placeholders to maintain "Fintech Premium" UI.*

- [x] **`AdContainer` Composable:** Create a wrapper that handles visibility logic based on `isAdsEnabled`.
- [x] **Native Ad Shimmer:** Build a shimmer placeholder that matches the `TransactionCard` layout.
- [x] **Lifecycle Safety:** Implement `DisposableEffect` patterns to prevent memory leaks in Compose.

## 📌 Phase 3: Static Ad Placements (Banners) ✅
*Goal: Implement non-intrusive, constant visibility ads.*

- [x] **`BannerAdView` Component:** Create a reusable wrapper for the AdMob `AdView` with adaptive sizing.
- [x] **Calendar Screen Banner:** Place an Adaptive Banner at the bottom of the Calendar screen.
- [x] **Layout Anchoring:** Ensure banners do not overlap with UI elements via anchored `AdContainer`.

## 📑 Phase 4: Dynamic Ad Placements (Native Ads)
*Goal: Seamlessly blend ads into scrollable content.*

- [ ] **Native Layout Design:** Implement a custom Native Ad layout using `surfaceGradient()` and `brandGradient()`.
- [ ] **Transactions List:** Inject a Native Ad card every 6-8 items in the `TransactionsScreen`.
- [ ] **Analytics Screen:** Place a Native Ad card below the Hero Chart.
- [ ] **Settings Screen:** Add a Native Ad at the bottom of the Settings list.

## ⚡ Phase 5: Interstitial & Event-Based Ads
*Goal: Monetize natural completion points.*

- [ ] **App Open Ad:** Implement a cold-start ad trigger in `MainActivity`.
- [ ] **Budget Completion Interstitial:** Trigger an interstitial ad after a user successfully saves a new Budget.
- [x] **Frequency Capping:** Implement a 15-minute cooldown timer between interstitials to prevent user fatigue.

## 🎁 Phase 6: Rewarded Ad Flow (Feature Gating)
*Goal: Exchange ad engagement for premium feature access.*

- [ ] **Unlock Dialog:** Create a "Watch Ad to Unlock" prompt for features with `AccessLevel.AD_SUPPORTED`.
- [ ] **`AdAccessStore` Integration:** Link reward success callbacks to `AdAccessStore.grantAccess()`.
- [ ] **Real-time UI Update:** Ensure `FeatureLockedOverlay` vanishes immediately upon successful reward.

## ✅ Phase 7: Validation & Optimization
*Goal: Ensure quality, performance, and premium integrity.*

- [ ] **Premium Audit:** Verify that all ads vanish instantly when `UserTier` changes to `PREMIUM`.
- [ ] **Dark Mode Verification:** Test all ad gradients and text colors in both Light and Dark themes.
- [ ] **Performance Benchmarking:** Ensure Native Ads in the Transactions list do not cause frame drops during scrolling.
- [ ] **Unit Tests:** Add tests for `AdsCoordinator` logic (frequency capping, ad-free logic).

---
*Last Updated: May 13, 2026*
