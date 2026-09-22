# 🏗️ Migration Plan: Layer-First to Feature-First Architecture

## 📌 Executive Summary
This document defines the step-by-step migration strategy to transition **ExpenseTracker** from a Layer-First architecture (`ui/screens`, `ui/viewmodels`, `data/`, `domain/`) to a standardized, modular **Feature-First Architecture** (`feature/<feature-name>/`).

---

## 🎯 Target Directory Structure

```
com.mknlabs.expensetracker/
├── core/                               # Shared across all features
│   ├── database/                       # Room DB, Shared DAOs (TransactionDao, CategoryDao)
│   ├── domain/                         # Shared Business Models (Transaction, Category, Budget)
│   ├── data/                           # DataStore & Common Repositories
│   ├── ui/                             # Theme, Navigation, Shared Components
│   │   ├── components/                 # AppHeader, MainScaffold, DateHeader
│   │   ├── navigation/                 # AppNavigationHost, AppRoute
│   │   └── theme/                      # Color, Theme, Typography, DynamicTypography
│   └── utils/                          # Currency, Date, UiText, Extensions
│
├── feature/                            # Independent Feature Packages
│   ├── home/                           # Home Dashboard
│   │   ├── ui/                         # HomeScreen, HomeViewModel, GreetingWave
│   │   └── domain/                     # Home specific state & use cases
│   ├── transactions/                   # Transactions & Add/Edit Form
│   │   ├── ui/                         # TransactionsScreen, AddTransactionScreen, Calculator
│   │   ├── domain/                     # Transaction filters, sorting & calculations
│   │   └── data/                       # Transaction export / import helpers
│   ├── analytics/                      # Charts, Insights & Trends
│   │   └── ui/                         # AnalyticsScreen, AnalyticsViewModel
│   ├── calendar/                       # Calendar View & Timeline
│   │   └── ui/                         # CalendarScreen, CalendarViewModel
│   ├── budget/                         # Budgets & Recurring Expenses
│   │   └── ui/                         # BudgetAndRecurringScreen, BudgetViewModel
│   ├── goals/                          # Savings Goals
│   │   └── ui/                         # GoalsScreen, GoalsViewModel
│   ├── smsinbox/                       # SMS Detection Inbox (Already Feature-First)
│   │   ├── ui/                         # SmsInboxScreen, SmsInboxViewModel, BellButton
│   │   ├── domain/                     # SMS Detection UseCases & Models
│   │   ├── data/                       # Detected SMS DAO & Repository
│   │   └── worker/                     # SmsInboxCleanupWorker
│   ├── profile/                        # User Profile & Membership
│   │   └── ui/                         # EditProfileScreen, ProfileViewModel, MembershipScreen
│   ├── settings/                       # App Settings, Security, Backup
│   │   └── ui/                         # SettingsScreen, SecurityPrivacyScreen, DataManagement
│   └── auth/                           # Authentication, Onboarding & App Lock
│       └── ui/                         # AuthContent, OnboardingScreen, AppLockScreen
│
├── monetization/                       # Pro Access & Billing Engine
├── notifications/                      # WorkManager & Notification Managers
├── sms/                                # Background SMS Parsing Engine
├── voice/                              # Voice Assistant Feature
├── widget/                             # Home Screen App Widgets
├── workers/                            # System Background Workers
└── di/                                 # Global Hilt Dependency Injection Modules
```

---

## 🗺️ Feature Mapping Matrix

| Existing Layer-First Files | Target Feature Package |
| :--- | :--- |
| `HomeScreen.kt`, `HomeViewModel.kt` | `feature/home/ui/` |
| `TransactionsScreen.kt`, `TransactionsViewModel.kt`, `AddTransactionScreen.kt`, `TransactionCardCustomizeScreen.kt`, `ItemizedCalculatorScreen.kt` | `feature/transactions/ui/` |
| `AnalyticsScreen.kt`, `AnalyticsViewModel.kt`, `CalendarScreen.kt` | `feature/analytics/ui/` |
| `BudgetAndRecurringScreen.kt`, `BudgetViewModel.kt` | `feature/budget/ui/` |
| `GoalsScreen.kt`, `GoalsViewModel.kt` | `feature/goals/ui/` |
| `SmsInboxScreen.kt`, `SmsInboxViewModel.kt`, `SmsInboxBellButton.kt` | `feature/smsinbox/ui/` *(Already done)* |
| `EditProfileScreen.kt`, `ProfileViewModel.kt`, `MembershipDetailsScreen.kt` | `feature/profile/ui/` |
| `SettingsScreen.kt`, `SettingsViewModel.kt`, `SecurityPrivacyScreen.kt`, `DataManagementScreen.kt`, `PreferencesScreen.kt`, `AboutScreen.kt` | `feature/settings/ui/` |
| `AuthContent.kt`, `OnboardingScreen.kt`, `AppLockScreen.kt`, `SplashOverlay.kt` | `feature/auth/ui/` |

---

## 📋 Step-by-Step Migration Phases

### Phase 1: Core Base Extraction (`core/`)
1. Create `core/ui/theme`, `core/ui/components`, `core/ui/navigation`.
2. Move theme files (`Color.kt`, `Theme.kt`, `Typography.kt`) to `core/ui/theme/`.
3. Move shared components (`AppHeader.kt`, `MainScaffold.kt`, `CashFlowStatsCard.kt`) to `core/ui/components/`.
4. Move navigation files (`AppNavigationHost.kt`, `AppRoute.kt`, `MainNavigationState.kt`) to `core/ui/navigation/`.

### Phase 2: Feature Migration (Iterative, Non-Breaking)
Migrate features one by one using `git mv` to preserve git commit history:

1. **Step 2.1: Migrate `feature/home`**
   - Move `HomeScreen.kt` & `HomeViewModel.kt` → `feature/home/ui/`
   - Move `HomeViewModelTest.kt` → `src/test/.../feature/home/ui/`
   - Update package statements and verify with `./gradlew testDebugUnitTest`.

2. **Step 2.2: Migrate `feature/transactions`**
   - Move transaction screens and viewmodels → `feature/transactions/ui/`
   - Update tests and verify build.

3. **Step 2.3: Migrate `feature/analytics` & `feature/budget`**
   - Move `AnalyticsScreen`, `CalendarScreen`, `BudgetAndRecurringScreen` into respective `feature/` packages.

4. **Step 2.4: Migrate `feature/goals`, `feature/profile`, `feature/settings`, `feature/auth`**
   - Complete remaining feature packaging.

### Phase 3: Cleanup & Verification
1. Remove old empty directories (`ui/screens/`, `ui/viewmodels/`).
2. Run full test suite: `./gradlew testDebugUnitTest`.
3. Verify Hilt DI bindings and Room KSP generation compile cleanly (`./gradlew assembleDebug`).

---

## 🔒 Safety & Rules During Migration
- **Preserve Git History:** Always use `git mv` or CLI tools when moving files.
- **Incremental Builds:** Execute `./gradlew testDebugUnitTest` after moving each feature package.
- **Maintain DI Bindings:** Ensure `@HiltViewModel` annotations and `@Provides` modules in `di/` are updated with new package paths.

---

## ✅ Migration Status

All phases are complete. Both legacy layer-first directories (`ui/screens/`, `ui/viewmodels/`) have been removed and no source file references a legacy package.

| Phase | Status | Commit |
| :--- | :--- | :--- |
| Rules — feature-first layout enforced | ✅ Done | `57ca09a` |
| 2.1 `feature/home` | ✅ Done | `79d696d` |
| 2.2 `feature/transactions` | ✅ Done | `0f84cdc` |
| 2.3 `feature/analytics` + `budget` + `calendar` | ✅ Done | `3afa881` |
| 2.4a `feature/goals` | ✅ Done | `a3ba8be` |
| 2.4b `feature/profile` | ✅ Done | `2e571a3` |
| 2.4c `feature/settings` | ✅ Done | `797b72e` |
| 2.4d `feature/auth` | ✅ Done | `1545aec` |
| Remaining shared / cross-cutting ViewModels | ✅ Done | `1fa14c3` |
| 1 Core extraction (`core/ui/*`) | ✅ Done | `b7ba353` |
| 3 Cleanup & verification | ✅ Done | `b7ba353` |

### Deviations from the original plan

- **Phase 2 ran before Phase 1.** Features were migrated first, then `core/` was extracted in a single sweep.
- **`calendar` was split out of `analytics`** into its own `feature/calendar/ui` package.
- **Component co-location was not performed.** All shared UI composables live in `core/ui/components/` regardless of how many features use them, matching the documented tree. Pulling single-feature components (e.g. `TransactionCard`, `ProfileCard`, `UserBadge`) into their owning features remains a possible follow-up.
- **Files with no listed home** were placed by closest fit: `MainViewModel` → `core/ui/`, `MonetizationViewModel` → `monetization/`, `VoiceAddViewModel` + `VoiceTransactionViewModel` → `voice/`, `PaymentMethodPredictorViewModel` → `feature/transactions/ui/`, `SmsChange*` + `SmsSetupViewModel` → `feature/smsinbox/ui/`, and the app gate screens `MaintenanceScreen` / `UpdateRequiredScreen` → `feature/settings/ui/`.
- **`MainScreen.kt` and `MainActivity.kt` remain at the package root** as the Android entry point and app shell.

### Verification

- `./gradlew testDebugUnitTest` — **600 tests, 0 failures**
- `./gradlew assembleDebug` — Hilt DI bindings and Room KSP generation compile cleanly

---

## 📌 Not covered by this migration (remaining work)

The phases above migrated the **UI layer only**. The following items are intentionally out of scope and still outstanding:

1. **`core/database`, `core/domain`, `core/data`, `core/utils` do not exist yet.** Phase 1 was scoped to `core/ui` only. The shared data layer (`data/local/room`, `data/repository`), shared models (`models/`, `domain/`), and helpers (`utils/`) still sit in their original top-level packages. Extracting them is a separate, larger migration.
2. **Feature-specific components still live in `core/ui/components/`.** ~14 composables are used by a single feature (e.g. `TransactionCard`, `ActiveFilterBar`, `AddTransactionFab`, `VoiceInputSheet` → transactions; `CashFlowStatsCard`, `SmallHomeCard` → home; `UserBadge` → auth; `ProfileCard`, `BackupRestoreSheet` → settings; `TabCountBadge` → budget; `DialogModeSelector` → analytics). Co-locating them inside their owning features would tighten encapsulation.
3. **Several `feature/*` packages are `ui`-only.** Only `smsinbox` currently has `data/`, `domain/`, `di/` and `worker/` layers. Feature-specific data and domain logic elsewhere still routes through the shared top-level `data/` and `domain/` packages.
4. **`.idea/workspace.xml`** holds stale Compose-preview run configurations pointing at the old `ui.screens.*` paths. That is IDE-local state, not build input, and resolves itself when the previews are re-run.
