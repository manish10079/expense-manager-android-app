# Expense Tracker Monetization Strategy

## 1. Executive Summary
This document outlines a non-intrusive, premium-first monetization strategy for the Expense Tracker Android application. The core philosophy is to preserve the "Fintech Premium" feel while establishing sustainable revenue streams through a mix of Banner, Native, and Interstitial ads. This strategy leverages the app's clean architecture and high-engagement zones to place ads where they feel like a natural part of the flow.

## 2. Overall Monetization Philosophy
- **User First:** Ads must never block primary actions (Adding transactions, saving data).
- **Premium Aesthetic:** Use Native Advanced ads that match the app's Material 3 design and gradients.
- **Contextual Placement:** Place ads in natural "pause" points (scrolling lists, navigation transitions).
- **Incentivized Value:** Use Rewarded Interstitials for high-value feature unlocks rather than hard paywalls.
- **Privacy Respect:** Ensure ad integration doesn't interfere with the app's "Private by Nature" brand.

## 3. Screen-by-Screen Ad Placement Strategy

| Screen Name | Ad Format | Placement Location | Justification | Priority | Premium Disabled |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Home Screen** | Adaptive Banner | Bottom Sticky (Above Bottom Bar) | Constant visibility in the primary hub without blocking content. | High | Yes |
| **Transactions** | Native Advanced | After every 6-8 items in list | Feels like a "Transaction Card" variant. Non-intrusive during scrolling. | High | Yes |
| **Analytics** | Native Advanced | Below the Hero Chart | High user attention area; provides a natural break before category breakdowns. | Medium | Yes |
| **Budget** | Interstitial | On saving a new Budget | Natural completion point. Low frustration risk after a successful task. | Medium | Yes |
| **Calendar** | Adaptive Banner | Below the Calendar Grid | Utilizes white space in the lower half of the screen. | Low | Yes |
| **Add Transaction**| **PROHIBITED** | N/A | High-sensitivity utility screen. Speed and focus are critical. | N/A | N/A |
| **Calculator** | **PROHIBITED** | N/A | Critical utility; ads would be highly distracting and prone to accidental clicks. | N/A | N/A |
| **Onboarding** | App Open Ad | Cold start only | Monetizes the very first entry without cluttering the onboarding cards. | Medium | N/A |
| **Settings** | Native Advanced | Bottom of settings list | Low interaction density area; good for "End of page" discovery. | Low | Yes |

## 4. Detailed Ad Format Implementation

### 4.1 Native Advanced (Recommended for List Feeds)
- **Visuals:** Must use the project's `surfaceGradient()` and `RoundedCornerShape(Dimens.CardCornerRadius)`.
- **Typography:** Match `MaterialTheme.typography.bodyMedium` for descriptions and `titleMedium` for headings.
- **CTA:** Use the `brandGradient()` for the "Install/Open" button to match the app's primary action buttons.

### 4.2 Adaptive Banners
- **Placement:** Bottom of the screen, anchored above the navigation bar.
- **Spacing:** Ensure at least `Dimens.ScreenPadding` (16dp) distance from any interactive FAB or Bottom Nav items.

### 4.3 Interstitial & Rewarded Ads
- **Trigger:** Only after 3+ successful transaction logs or on "Export/Backup" for non-premium users.
- **Frequency:** Max 1 interstitial per 15-minute session to avoid "Ad Fatigue".

## 5. Frequency Capping & UX Safety Rules
- **Global Cap:** No more than 3 interstitials per user per day.
- **Interaction Buffer:** No ads should appear within the first 60 seconds of a session (except App Open).
- **Click Safety:** Do not place ads near the "Add Transaction" FAB or "Save" buttons.
- **Loading State:** Use Shimmer effects that match the app's existing loading patterns for Native ads.

## 6. Prohibited Zones (Zero-Ad Screens)
- **Add Transaction Screen:** Zero distractions for data entry.
- **App Lock / Biometric Screen:** Security screens must remain clean and trustworthy.
- **Calculator Screen:** Precision utility requires full focus.
- **Splash Screen:** Maintain the clean "Brand Entry" experience.

## 7. Integration with Existing Infrastructure

The monetization strategy is designed to layer seamlessly over the current `monetization/` package:

### 7.1 FeatureRegistry Alignment
- **`AccessLevel.AD_SUPPORTED`:** Any feature marked with this level in `FeatureRegistry.kt` should trigger a **Rewarded Interstitial**. 
- **Example:** When a user clicks a feature gated by `Feature.ANALYTICS_CUSTOM_RANGE`, the app should present the "Watch Ad to Unlock" flow.

### 7.2 AdAccessStore Integration
- Upon successful reward completion, call `AdAccessStore.grantAccess(feature, optionId, durationMillis)` to update the temporary unlock state.
- For "Full Access" rewards (e.g., from a high-value Interstitial), use `AdAccessStore.grantFullAccess(durationMillis)`.

### 7.3 UI Feedback (AccessStatus)
- Use the existing `AccessStatus.DeniedAd` and `AccessStatus.DeniedPremium` signals to determine when to show ad-based CTAs versus direct payment prompts.
- Ensure `FeatureLockedOverlay` and `GatedAction` components are updated to handle the new ad loading states.

## 8. Premium User Experience
- **One-Click Removal:** Premium status (`UserTier.PREMIUM` in `AppSettings`) must globally set an `isAdEnabled` flag to `false`.
- **Gated Features:** Use ads as an alternative to payment for all `AccessLevel.AD_SUPPORTED` features. 

## 8. Compose Implementation Notes
- **Reusability:** Create a `StandardAdContainer` composable that handles `isAdEnabled` checks and shimmer states.
- **Lifecycle:** Ensure ad views are properly disposed of using `DisposableEffect` to prevent memory leaks in Compose.
- **Performance:** Load ads on `Dispatchers.IO` and ensure the UI thread remains smooth during ad rendering.

## 9. Future Optimization Suggestions
- **A/B Testing:** Test "Native vs. Banner" in the Transactions list.
- **Tiered Ads:** Show fewer/smaller ads to users with high engagement but low conversion.
- **Collapsible Banners:** Evaluate for the Transactions screen to maximize list real estate.

## 10. Implementation Checklist
- [ ] Implement `MonetizationProvider` to track ad-free status.
- [ ] Create Native Ad layout matching `TransactionCard` style.
- [ ] Integrate Adaptive Banner in `MainScaffold`.
- [ ] Add Interstitial trigger logic in `AddTransactionViewModel` (post-save).
- [ ] Verify Dark Mode compatibility for all ad backgrounds.
- [ ] Test ad click-through behavior to ensure it doesn't break NavStack.
