
# AdMob Implementation Plan - ExpenseTracker

This document outlines the final AdMob ad unit strategy for the ExpenseTracker application. It is designed to maximize revenue through granular analytics while maintaining the app's premium "Fintech" user experience.

## 📊 Summary of Ad Units
| Type | Unique Units | Purpose |
| :--- | :--- | :--- |
| **Native Advanced** | 5 | Seamlessly integrated into scrollable lists and dashboards. |
| **Rewarded** | 2 | Value-exchange for unlocking Pro features or ad-free time. |
| **Interstitial** | 2 | High-revenue transitions between major tasks. |
| **Total** | **9 Units** | |

---

## 1. Native Ads (Advanced)
These ads are designed to look like the app's native UI cards.

| Ad Unit Name | Placement / Screens | Rationale |
| :--- | :--- | :--- |
| `native_home_dashboard` | `HomeScreen` | **Prime Real Estate:** High traffic. Needs unique tracking to optimize the first-impression revenue. |
| `native_transactions_list` | `TransactionsScreen` | **Contextual Flow:** Users spend time scrolling here. Best for "In-feed" ad performance. |
| `native_analytics_insights` | `AnalyticsScreen` | **High Intent:** Users are analyzing finances; attracts high-value financial services ads (Higher eCPM). |
| `native_budget_calendar` | `BudgetScreen`, `CalendarScreen` | **Planning Group:** Shared unit for screens where users plan future spending. |
| `native_settings_general` | `Settings`, `Profile`, `Data Management`, `About` | **Low Frequency:** Groups low-traffic screens to keep management simple while still tracking "Utility" areas. |

---

## 2. Rewarded Ads
Ads that users choose to watch in exchange for a benefit.

| Ad Unit Name | Trigger / Usage | Rationale |
| :--- | :--- | :--- |
| `rewarded_feature_unlock` | `GatedAction` (Any feature marked AD_SUPPORTED) | **Direct Monetization:** Directly links specific features (e.g., Search, Export) to revenue. |
| `rewarded_ad_free_access` | "Remove Ads for 1 Hour" button in `Settings` | **User Choice:** Provides an alternative for users who want a clean experience without paying cash. |

---

## 3. Interstitial Ads
Full-screen ads for natural break points in the app.

| Ad Unit Name | Trigger / Usage | Rationale |
| :--- | :--- | :--- |
| `interstitial_on_save` | After clicking "Save" on a Transaction | **Post-Task Reward:** Shown when the user has finished a primary task and is transitioning back to the dashboard. |
| `interstitial_data_action` | After successful JSON Export or DB Backup | **Utility Gateway:** High-value actions that justify a transition ad without annoying the user. |

---

## 🛠️ Technical Integration Checklist

1. **AdMob Console:**
   - [ ] Create the 9 units above in the AdMob Dashboard.
   - [ ] Copy the Production IDs.

2. **Codebase Update (`AdsCoordinator.kt`):**
   - [ ] Replace `NATIVE_AD_UNIT_ID` with a logic-based picker or individual variables.
   - [ ] Update `REWARDED_AD_UNIT_ID`.
   - [ ] Update `INTERSTITIAL_AD_UNIT_ID`.

3. **Validation:**
   - [ ] Verify that `isAdsEnabled` (Premium status) correctly hides all containers.
   - [ ] Ensure the 15-minute Interstitial cooldown is working to prevent over-exposure.

---
**Note:** Always use Google Test IDs (`ca-app-pub-3940256099942544/...`) during development to avoid account suspension.
