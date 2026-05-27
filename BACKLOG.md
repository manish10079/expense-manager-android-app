# Project Backlog & Production Readiness

## 🚀 Production Readiness (May 2026)
*Goal: Finalize core assets and monetization for Play Store submission.*

- [ ] **Production Ad IDs:** Replace Google Test Ad IDs in `AdsCoordinator.kt` with actual AdMob IDs.
- [x] **Real Native Ad Implementation:** Integrated `NativeAdView` from AdMob SDK into `NativeAdCard.kt` with custom branding.
- [ ] **Play Billing Integration:** Replace simulated purchases with Google Play Billing Library for real "Premium" transactions.
- [x] **Adaptive Icons**: Create `ic_launcher.xml` and generate proper assets for `mipmap-anydpi-v26` (Required for Play Store).

- [x] **i18n Cleanup:** Localize remaining hardcoded strings in `BottomNavConfig.kt` (Bottom Bar titles) and `NativeAdCard.kt`.


- [x] **Privacy Policy:** Hosted on Google Sites and linked in `AboutScreen.kt` and `AndroidManifest.xml`.



## 🌐 Internationalization (i18n) Progress Tracker
**Last Audit:** 2026-05-15 (Verified current status of all items)
 



### ViewModels (Core Business Logic Labels)
- [x] **PreferencesViewModel.kt**
    - [x] Theme Labels: `"System"`, `"Light"`, `"Dark"`.
    - [x] Theme Descriptions: `"Follow your device app
    earance"`, `"Always use the light theme"`, `"Always use the dark theme"`.
- [x] **AnalyticsViewModel.kt**
    - [x] Chart Labels: `"W1"`, `"W2"`, `"W3"`, `"W4"`.
    - [x] Formatting: `"%"` symbol in `formatPercent`.
- [x] **TransactionsViewModel.kt**
    - [x] Period Labels: `"All Records"`.
- [x] **CategoryManagementViewModel.kt**
    - [x] Fallbacks: `"Custom income category"`, `"Custom expense category"`, `"Custom payment method"`.

### Notifications & Background Tasks
- [x] **NotificationHelper.kt**
    - [x] Channel Names: `"Daily Reminders"`, `"Budget Alerts"`, `"Recurring Transactions"`.
    - [x] Channel Descriptions: `"Periodic reminders to log your expenses."`, `"Alerts when you exceed your monthly budget."`, etc.
    - [x] Notification Titles: `"Budget Alert!"`, `"Missed Today?"`.
- [x] **DynamicNotificationEngine.kt**
    - [x] This file contains dozens of sarcastic/dynamic notification strings (openers, reactions, guilt lines, etc.) that are all hardcoded.
    - [x] **Issue:** Hardcoded "₹" symbol in `generateExpenseMessage`. Need to use dynamic currency formatting.

### Utilities & Infrastructure
- [x] **BiometricAuthManager.kt**
    - [x] Availability Messages: `"Set up fingerprint or face unlock..."`, `"This device does not support..."`, `"Biometric hardware is currently unavailable."`, etc.
- [x] **BackupFileManager.kt**
    - [x] File Size Units: `"B"`, `"KB"`, `"MB"`, `"GB"`, `"TB"`.

### UI Components & Screens (Missed Edge Cases)
- [x] **AddTransactionScreen.kt**
    - [x] Installments Picker: `"Other"` (Placeholder), `"Current transaction is installment #1. Future entries will be generated automatically."` (Info message).
- [x] **WheelDateTimePicker.kt**
    - [x] Time Picker: `"AM"`, `"PM"`.
- [x] **SortFilterModal.kt**
    - [x] Sort Options Fallbacks: `"Date"`, `"Amount"`, `"Category"`.
- [x] **AnimatedTabSwitcher.kt / BudgetScreen.kt**
    - [x] Content Descriptions: Templates like `"$label locked"` need string resources.

## 🟡 Medium Priority (Pending)
## ✅ Completed (Properly Done)
- [x] `CategoryConstants.kt` (Localized 100+ icon labels and fallback descriptions; refactored UI models and screens)
- [x] `SortChip.kt` (Localized active state label)
- [x] `InputField.kt` (Localized email validation and date placeholder)
- [x] `CategoryManagementScreen.kt` (Localized manage category title, fallback subtitles, and delete content descriptions)
- [x] `ProfileAvatar.kt` (Localized accessibility descriptions for photo and placeholder)
- [x] `SelectionHeader.kt` (Localized selected count and accessibility descriptions)
- [x] `EditProfileScreen.kt` (Gender selection IDs, field placeholders, photo action descriptions localized)
- [x] `SortFilterModal.kt` (Localized date ranges, amount placeholders, and fixed build errors)
- [x] `AddCategoryScreen.kt` (Refactored all hardcoded strings and placeholders)
- [x] `CategoryManagementUiModels.kt` (Enum titles migrated to string resources)
- [x] `AppLockScreen.kt` (Refactored all hardcoded strings and localized security questions)
- [x] `StatsCard.kt` (Dashboard labels fixed)
- [x] `TransactionCard.kt` (Hardcoded labels fixed)
- [x] `AppLockKeypadLayout.kt` (Internal labels verified)
- [x] `SplashOverlay.kt` (App name fallback fixed)
- [x] `AddTransactionScreen.kt` (UI text & Logic fixed)
- [x] `AboutScreen.kt` (Full i18n - Technical URLs/Dev name excluded)
- [x] `SettingsScreen.kt`
- [x] `PreferencesScreen.kt`
- [x] `SecurityPrivacyScreen.kt`
- [x] `HomeScreen.kt`
- [x] `TransactionsScreen.kt`
- [x] `AnalyticsScreen.kt` & `AnalyticsViewModel.kt` (Full i18n, resolved compilation errors, and refactored UI models)
- [x] `BudgetScreen.kt`
- [x] `CalendarScreen.kt`
- [x] `OnboardingScreen.kt`
- [x] `DataManagementScreen.kt`
- [x] `BackupRestoreSheet.kt`
- [x] `PremiumGateSheet.kt`
- [x] `GatedAction.kt`
- [x] `WheelDateTimePickerModal.kt`
- [x] `TodaySpendingCard.kt`
- [x] `AdRewardDialog.kt`
- [x] `AppBottomBar.kt`
- [x] `BottomNavConfig.kt`
- [x] `NativeAdCard.kt`
- [x] `BudgetViewModel.kt` (Localized budget status, insights, and formatting)
- [x] `CalendarViewModel.kt` (Localized financials, month abbreviations, and empty states)
- [x] `PreferencesViewModel.kt` (Localized theme labels, descriptions, and time formats)
- [x] `TransactionsViewModel.kt` (Localized period labels, date patterns, and filter keys)
- [x] `CategoryManagementViewModel.kt` (Localized fallback subtitles and tab labels)
- [x] `NotificationHelper.kt` (Localized channel names, descriptions, and notification titles)
- [x] `DynamicNotificationEngine.kt` (Refactored to use resource IDs for all dynamic/sarcastic messages and main notification formats)
- [x] `BiometricAuthManager.kt` (Localized security and hardware availability messages)
- [x] `BackupFileManager.kt` (Localized file size units and integrated with localized components)

