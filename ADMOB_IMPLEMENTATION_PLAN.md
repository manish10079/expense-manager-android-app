
# AdMob Implementation Plan - ExpenseTracker

This document outlines the final AdMob ad unit strategy for the ExpenseTracker application. It is designed to maximize revenue through granular analytics while maintaining the app's premium "Fintech" user experience.

## 📊 Summary of Ad Units
| Type | Unique Units | Purpose |
| :--- | :--- | :--- |
| **Native Advanced** | 5 | Seamlessly integrated into scrollable lists and dashboards. |
| **Rewarded** | 1 | Value-exchange for global ad-free time. |
| **Interstitial** | 1 | High-revenue transitions for data management actions. |
| **Total** | **7 Units** | |

---

## 1. Native Ads (Advanced)
These ads are designed to look like the app's native UI cards.

| Ad Unit Name | Placement / Screens | Status |
| :--- | :--- | :--- |
| `native_home_dashboard` | `HomeScreen` | **Implemented** |
| `native_transactions_list` | `TransactionsScreen` | **Implemented** |
| `native_analytics_insights` | `AnalyticsScreen` | **Implemented** |
| `native_budget_calendar` | `BudgetScreen`, `CalendarScreen` | **Implemented** |
| `native_settings_general` | `Settings`, `Profile`, `Data Management`, `About` | **Implemented** |

---

## 2. Rewarded Ads
Ads that users choose to watch in exchange for a benefit.

| Ad Unit Name | Trigger / Usage | Status |
| :--- | :--- | :--- |
| `rewarded_ad_free_access` | "Remove Ads for 1 Hour" button in `Settings` | **Implemented** |
| `rewarded_feature_unlock` | `GatedAction` (Any feature marked AD_SUPPORTED) | *Using Test ID* |

---

## 3. Interstitial Ads
Full-screen ads for natural break points in the app.

| Ad Unit Name | Trigger / Usage | Status |
| :--- | :--- | :--- |
| `interstitial_data_action` | After successful JSON Export or DB Backup | **Implemented** |
| `interstitial_on_save` | After clicking "Save" on a Transaction | *Using Test ID* |

---

## 🛠️ Technical Integration Checklist

1. **AdMob Console:**
   - [x] Create the 7 real units above in the AdMob Dashboard.
   - [x] Copy the Production IDs.

2. **Codebase Update (`AdsCoordinator.kt`):**
   - [x] Replace `NATIVE_AD_UNIT_ID` with placement-based logic.
   - [x] Update `REWARDED_AD_FREE_ID` with Real ID.
   - [x] Update `INTERSTITIAL_DATA_ACTION_ID` with Real ID.

3. **Validation:**
   - [x] Verify that `isAdsEnabled` (Premium status) correctly hides all containers.
   - [x] Ensure the 15-minute Interstitial cooldown is working to prevent over-exposure.

---
**Note:** Always use Google Test IDs (`ca-app-pub-3940256099942544/...`) during development to avoid account suspension.
