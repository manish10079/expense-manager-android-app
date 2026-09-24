# Subscription Testing Checklist — RevenueCat + Google Play

> Status legend: ✅ Done · ⬜ Not done · ⛔ Blocked (waiting on something else)
> Last audited: 2026-09-23 against branch `main` (commit `d57ccdd`).
> ⚠️ Section 6 items 1–4 exist **only as uncommitted working-tree changes** — they are not in git yet.

---

## 1. Google Play Console

- [x] `com.android.vending.BILLING` permission in AndroidManifest
- [x] `<queries>` billing-service intent (Android 11+ package visibility)
- [x] Create subscription product + base plans/offers in Play Console
- [ ] Build signed release AAB (`./gradlew bundleRelease`)
- [ ] Upload AAB to Internal Testing (or Closed) track
- [x] Add license tester emails → Setup → License testing → `RESPOND_NORMALLY`
- [ ] Copy Opt-In URL from test track
- [ ] Accept opt-in on the test device ("Become a Tester")

## 2. RevenueCat Dashboard

- [x] RevenueCat SDK dependency (`com.revenuecat.purchases:purchases:10.15.1`)
- [x] Public API key obtained (`goog_…`, stored in `localProperties.properties`)
- [x] Create app/project in RevenueCat (if not done)
- [x] Link Google Cloud service account in RevenueCat settings
- [x] Verify Google Play RTDN (Real-Time Developer Notifications) active — verified 2026-09-23 08:10 via "Send test notification" in Play Console; "Last received" timestamp confirmed in RevenueCat. Topic: `projects/expense-tracker-2ea00/topics/Play-Store-Notifications`
- [x] Create entitlement `premium`
- [x] Create Packages (monthly / annual / six-month) and attach Play products
- [x] Attach packages to a default Offering — COMPLETE 2026-09-23. Offering `default` (Default Offering) with 3 packages in paywall order:
  - `$rc_annual` → `pro_features:sub-12-month`
  - `$rc_six_month` (Six Months) → `pro_features:sub-6-month`
  - `$rc_monthly` → `pro_features:sub-1-month`
  - All fallback slots = "No product", all Test Store rows = "No product", empty `$rc_lifetime` removed. Fallbacks correctly empty: app uses SDK 10.15.1 (≥ v6), so no legacy-SDK product is needed.
- [x] Restore Behavior = **"Transfer to new App User ID"** — REQUIRED BEHAVIOR: if mkn0079@gmail.com buys, any other account signing in on that device CAN restore and takes the subscription over. This is RevenueCat's DEFAULT (Project settings → General → behavior dropdown) — just confirm it wasn't switched to another option.
  - Effect: the new account gains access AND the previous account loses it (transfer, not share) — one subscription can never serve two accounts at once. mkn0079 signing back in later takes it back on the next restore/login.
  - Rejected alternative: "Keep with original App User ID" was considered and dropped — it returns `RECEIPT_ALREADY_IN_USE` to the other account AND blocks that account from buying its own subscription while the device's Play Store account is still the original purchaser's.
  - Device constraint (Google's rule, not RevenueCat's): a restore can only find receipts the device's Play Store account can see, so if the Play account changes too, there is nothing to restore.
- [x] "Sandbox data" toggle turned ON for testing — RevenueCat → **Overview** → toggle above the metrics. Effect: metrics report sandbox/Test Store purchases instead of production. Does NOT affect the Customers list, entitlements, webhooks, or the New/Active Customers cards.

## 3. Firebase

- [x] Firebase Auth sign-in flow exists (Google/email)
- [x] Install RevenueCat Firebase Extension
- [x] Configure extension to write subscription docs — **path changed to `rc_customers`** (docs at `rc_customers/{uid}`), replacing the original `users/{uid}/subscriptions` plan. Reason: the recursive `users/{uid}/{document=**}` rule makes every nested path client-writable, and Firestore rules are additive so a nested deny is impossible — entitlement data must live outside `users/{uid}`. Rules for `rc_customers` (owner read-only) and `rc_events` (no client access) are **deployed to production 2026-09-23**. **Extension params configured 2026-09-23** (Firestore → Extensions → Reconfigure): customers collection = `rc_customers`, webhook events collection = `rc_events`. Both sides now agree with `firestore.rules`, which matches `/rc_customers/{uid}` and `/rc_events/{eventId}` — a mismatch here is what would make a successful sandbox purchase invisible to the app.
- [x] RevenueCat webhook → Firebase extension — satisfied by the Firebase integration: RevenueCat pushes lifecycle events to the extension's auto-created HTTPS endpoint once the **shared secret** is set (Project → Integrations → Firebase → generate shared secret → pasted into the extension config). No manual webhook URL exists to configure. Events update `rc_customers/{uid}` and are appended to `rc_events`.
- [x] Confirm App Check enforcement won't block the extension — **verified 2026-09-23 by code inspection**: App Check enforcement applies only to *client SDK* requests. The extension's functions run with a service account and write via the **Admin SDK**, which Firebase exempts from App Check (the same reason it already writes to `rc_customers` despite that rule being `allow list, write: if false`). Confirmed in-repo: `enforceAppCheck: true` is set **only** on our own **callable** functions (`redeemProPass`, `parseVoiceTransaction`); the flag does not exist for HTTPS functions, so the extension's plain HTTPS webhook receiver is unaffected by Cloud Functions enforcement too.
  - Residual risk is **client-side, not extension-side**: if Firestore App Check enforcement is ON, the app's *read* of `rc_customers/{uid}` needs a valid token (`PlayIntegrityAppCheckProviderFactory` in release, `DebugAppCheckProviderFactory` with the pinned `APP_CHECK_DEBUG_TOKEN` in debug — that token must be registered in App Check → Apps → Manage debug tokens).
  - Split symptom: `permission-denied` on the app read = client token problem (extension still writing fine); an empty `rc_customers` = extension-side problem (check extension logs).
  - Recommendation: leave Firestore App Check enforcement **OFF** through the testing window (rules already enforce per-user access); enable it after launch. Reversible toggle at App Check → APIs. Verification recipe in `APP_CHECK_VERIFICATION` below.

## 4. Physical Test Device

- [ ] Real device (not emulator) with Google Play services
- [ ] License tester account is the PRIMARY profile in Play Store app
- [ ] Screen-lock PIN set (required for Google test cards)

## 5. Code — already in the codebase

- [x] `BillingManager.kt` — configure, logIn/logOut sync, fetch offerings, purchase, restore, entitlement getters
- [x] Auth listener auto-logs into RevenueCat with Firebase UID
- [x] `FeatureRegistry` gating system (~35 features, FREE / AD_SUPPORTED / PREMIUM)
- [x] `PremiumGateSheet`, `GatedAction`, ProPass redeem UI, rewarded-ad pass flow
- [x] Debug build compiles clean (exit 0)
- [x] All **675** unit tests pass — 63 suites, 0 failures, 0 errors, 0 skipped (verified 2026-09-23). Breakdown: 616 baseline on `main`, plus `PurchaseStateTest` (8), `HiltInjectReachabilityTest` (1), `PaywallCopyTest` (11), `PaywallViewModelTest` (19) and `SubscriptionOfferMapperTest` (8) for the paywall work, plus `EntitlementResolverTest` (12) for the entitlement bridge. `:app:compileReleaseKotlin` green too.

## 6. Code — must be built (blocks all testing)

> Progress: **6 of 6 complete** (DI wiring, purchase state flow, `PaywallScreen`, button routing, entitlement bridge, ProPass decision) as of 2026-09-23.
> Section 6 is closed; section 7 is unblocked and can be run on a device.

- [x] **Wire `BillingManager` into DI** — DONE 2026-09-23. `ExpenseTrackerApplication` now holds `@Inject lateinit var billingManager: BillingManager`, so the `@Singleton` is constructed at process start and its `init` block runs `Purchases.configure()`. Injecting it was also the first time Hilt ever validated this class, and doing so exposed two latent bugs that had gone unnoticed precisely because nothing demanded the binding:
  - Its constructor took `appLockPreferences: AppLockPreferences`, a Kotlin `object` with no `@Provides` — unsatisfiable, **and the parameter was never used**. Removed.
  - It took `@ApplicationContext Application`, but Hilt provides `Application` *unqualified*; the qualifier only binds `Context`. Changed to `@ApplicationContext Context`, matching the other 34 injectables in the codebase.
  - Verified: `:app:testDebugUnitTest` and `:app:compileReleaseKotlin` both succeed. These compile the generated Hilt component, which is what actually validates the graph — `:app:compileDebugKotlin` alone does **not**, which is why the `@ApplicationContext Application` error was invisible before.
- [x] **Purchase state flow** — DONE 2026-09-23. New `monetization/PurchaseState.kt`: a sealed `PurchaseState` of `Idle` / `InProgress` / `Completed` / `Cancelled` / `PaymentPending` / `Failed`, with `Operation` (Purchase | Restore) and a typed `FailureReason`. `BillingManager` exposes `purchaseState: StateFlow<PurchaseState>` and publishes `InProgress` synchronously, so the UI can disable its actions before the Play sheet appears; `acknowledgePurchaseState()` returns it to `Idle` so a stale outcome is not re-announced on the next composition.
  - Cancellation and pending payment are **separate states, not `Failed`** — dismissing the Play sheet must not render an error.
  - Carries **no strings at all**: the UI maps `FailureReason` to a `@StringRes` id (GEMINI §5 i18n). RevenueCat's English diagnostic messages stay in logcat rather than leaking untranslated text into the UI.
  - Failure paths now report `BillingNotConfigured` instead of letting `Purchases.sharedInstance` throw when the API key is missing.
  - Verified by `monetization/PurchaseStateTest.kt` (8 tests), including one that pins the entire `PurchasesErrorCode` → `FailureReason` table, so an SDK upgrade that adds an error code fails the test rather than silently degrading to `Unknown`.
  - COMPLETED with the paywall item: the `strings.xml` copy that maps each `FailureReason` now exists — 11 `paywall_error_*` strings, one per reason, mapped by `feature/paywall/ui/PaywallCopy.kt` and pinned by `PaywallCopyTest` (11 tests, including one asserting every reason maps to a **distinct non-zero** id, which catches two reasons silently sharing one message).
- [x] **`PaywallScreen`** — DONE 2026-09-23. New `feature/paywall/ui/PaywallScreen.kt` (Route + Content + previews), `PaywallViewModel.kt`, `PaywallCopy.kt`, plus `monetization/SubscriptionOffer.kt` (SDK-free offer model + mapper), `domain/repository/BillingRepository.kt` (interface so the ViewModel is testable without the SDK), and `AppRoute.Paywall` registered in the navigation host.
  - [x] Store-provided formatted prices (`Package.product.price.formatted`) — never hardcode prices. Note for anyone revisiting this: RevenueCat 10.x has **no `priceString`** on `StoreProduct` (`Package.product`, then `price: Price`, then `.formatted`); confirmed against the 10.15.1 `sources.jar` rather than guessed. Prices are rendered exactly as the store supplies them. The only price literals anywhere are the three `@Preview` demo states, matching how every other screen in this project feeds its previews.
  - [x] Buy button → `PurchaseParams.Builder(activity, pkg)` via `purchase(activity, offerId)`, which resolves the id through the offerings map back into the SDK `Package`. The Activity is obtained with `findFragmentActivity()`, not a cast: gated content can live in its own dialog window, where `LocalContext` is a `ContextThemeWrapper` and a plain cast returns null.
  - [x] Restore purchases button → `restore()` → `restorePurchases()` → `awaitRestore()`. A restore that finds nothing is still `Completed` (not a failure), so the screen decides via `isPremium` and says *"No previous purchase was found"* rather than falsely claiming success.
  - [x] Manage-subscription / terms links. Manage opens RevenueCat's `CustomerInfo.managementURL` — the **store's own** page, surfaced as a new `BillingRepository.managementUrl` flow — never a URL built by hand, and shown only when the user is premium *and* the URL is non-blank. Terms and Privacy reuse the existing `url_terms_conditions` / `url_privacy_policy`, the same strings `AboutScreen` opens, so there is one source of truth. Both open in an external browser **after** `onPrepareForExternalActivity()`, so returning does not trip the auto-lock and demand the PIN mid-purchase. Also added a renewal-disclosure line under the buy button, which Play requires on the purchase screen itself rather than behind a link.
  - Behaviour: the first plan is preselected; buy/restore/manage are all disabled while an attempt is in flight; a finished-but-empty offerings fetch shows Retry instead of spinning forever.
  - i18n and a11y: no hardcoded strings, `@StringRes` throughout; plan cards use `selectable(role = RadioButton)` so the selection is *announced*, not merely coloured; the loading indicator carries a `contentDescription`; light/dark/`fontScale = 2` previews exist; width is capped on tablets via the project's existing `AdaptiveContent`.
  - Verified by `PaywallCopyTest` (11 tests), `PaywallViewModelTest` (15 tests) and `SubscriptionOfferMapperTest` (8 tests) — 34 tests, 0 failures — with `:app:compileReleaseKotlin` green. One test pins all seven `PackageType` values, so an SDK upgrade that adds one fails the build instead of silently degrading to generic wording.
  - **Not verified:** the screen has never actually been rendered — no device run and no screenshot test. Compilation and unit tests cannot prove the layout; that is section 4's job.
- [x] **Route all "Upgrade to Pro" buttons to the paywall** — DONE 2026-09-23. All six sites now open the paywall instead of `ComingSoonDialog`, and `ComingSoonDialog.kt` is **deleted** — after the edits it had zero references anywhere in the repo.
  - `LocalUpgradeToPro` (`staticCompositionLocalOf<() -> Unit>`, `core/ui/navigation/UpgradeNavigation.kt`) is installed **once** in `MainScaffold`, so `GatedAction` and the other upsells open the paywall without carrying a navigation dependency of their own — `GatedAction` alone has ~34 call sites.
  - Sites: `GatedAction.kt`; `MembershipDetailsScreen.kt` (upgrade, manage and restore CTAs); `DataManagementScreen.kt`; `AddTransactionScreen.kt`; `AppNavigationHost.kt` (Connected Devices — previously navigated to **Settings and did nothing**, behind a comment promising an AdFree flow that was never wired); and `MainScreen.kt`'s own premium sheet, which navigates directly because it renders *outside* the subtree that installs the provider.
  - `AppRoute.Paywall` added and mapped in `NavigationUtils` to `previousRoute`: the paywall is reachable from anywhere, so Back returns to whichever screen opened it. Unmapped it fell into `else -> null`, which disables the scaffold's back handler and sends the app to the **background** instead of back.
  - Gotcha for future readers: `LocalUpgradeToPro.current()` compiles but is wrong — `current` is a *property* of function type, so `()` **invokes the returned lambda** and the expression evaluates to `Unit`. The correct form is `LocalUpgradeToPro.current` with no parentheses.
  - Verified: `:app:testDebugUnitTest` and `:app:compileReleaseKotlin` green, 675 tests / 0 failures. No rendering test — routing is proven by compilation plus each call site's reachability, not by drawing it.
- [x] **Entitlement bridge** — DONE 2026-09-23. `MonetizationRepositoryImpl` now takes `BillingRepository` and folds the store entitlement into every gate, so a completed purchase unlocks Pro immediately — read from the SDK rather than Firestore on purpose, since the RevenueCat → Firestore sync lags and must not be on the critical path for unlocking what the user just paid for.
  - All three flows gained the entitlement as a `combine` input, not a synchronous read: `userTier`, `isAdsEnabled` and `observeAccessStatus`. A purchase, renewal, login or expiration therefore re-evaluates the tier the moment `CustomerInfo` changes, with no polling and no manual refresh.
  - The rule itself lives in one pure place, `monetization/EntitlementResolver.kt`, rather than being restated three times. It short-circuits on the store entitlement *before* any local expiry maths, which is the case worth protecting: a subscriber whose cached local `proExpiryTimestamp` has lapsed but whose store entitlement is live must **stay** Pro. Deciding it the other way round would lock a paying subscriber out on every renewal the app had not yet learned about.
  - `ad_free_global` is honored via a deliberately **wider** `BillingRepository.isAdFree` (premium **or** standalone ad-free), which the ads path reads, while feature gates read the **narrower** `isPremium`. So buying ad removal alone stops the ads without unlocking the Pro feature set — the two entitlements are never inferred from each other.
  - `BillingManager` gained `PREMIUM_ENTITLEMENT_ID = "premium"` / `AD_FREE_ENTITLEMENT_ID = "ad_free_global"` as constants, kept in the billing layer because entitlement identifiers are billing vocabulary the monetization layer should not need to know.
  - Verified: `:app:testDebugUnitTest` + `:app:compileReleaseKotlin` green — **675 tests, 0 failures, 63 suites** — including `monetization/EntitlementResolverTest.kt` (12 tests). The boundary cases are pinned on purpose: entitlement-survives-lapsed-local-expiry, a grant expiring *exactly now* counts as expired, and `proExpiryTimestamp = 0` stays Pro so legacy holders are not silently downgraded.
- [x] **`onPurchaseSimulated()` / ProPass decision** — DECIDED 2026-09-23, on evidence from the code rather than a judgement call.
  - **Removed** `onPurchaseSimulated()` and the whole simulated-purchase path, including `BecomePremiumUseCase.kt` (deleted) and the parameter `DataManagementScreen` passed but **never invoked** in its UI. The decisive reason is not that it was dead — it is that it wrote **permanent** Pro with no expiry, so it would have silently outranked a real subscription and masked every billing failure during exactly the testing it was meant to help with. No path can now set a tier that never expires.
  - **Kept ProPass.** It is a legitimate grant channel, not a backdoor: redemption goes through the `redeemProPass` Cloud Function, which is the only trusted writer of `accountTier` / `proExpiryTimestamp`, and the grant it writes is **time-limited**, so it expires on its own. That is a supported way to give Pro away (promos, support, beta testers), and removing it would have taken a working feature with it.
  - `MonetizationRepositoryImpl.becomePremium()` was removed with the path, so the interface no longer offers a way to self-grant Pro at all; `isPremiumUser()` is the single decision point, shared by all three flows.
  - Verified by the same green run: **675 tests, 0 failures**, with the three test files that referenced the removed API updated rather than deleted.

⛔ **Everything in section 7 is blocked until sections 1–6 are complete.**

## 7. Test Scenarios (run after 1–6)

### 7.1 First purchase — success
- [ ] Offerings load in paywall (products + prices visible)
- [ ] Buy → test card "Always approves"
- [ ] App: entitlement active (`hasEntitlement("premium") == true`)
- [ ] RevenueCat dashboard: `INITIAL_PURCHASE` event (sandbox data ON)
- [ ] Firestore: entitlement doc created/updated

### 7.2 First purchase — failure
- [ ] Cancel sheet → `userCancelled == true`, UI recovers, no entitlement
- [ ] Test card "Always declines" → error surfaced, no entitlement

### 7.3 Cancellation & expiration
- [ ] Cancel via Play Store → Payments & subscriptions
- [ ] Immediately after: entitlement STILL ACTIVE (paid period running)
- [ ] After 5–10 min sandbox period: entitlement INACTIVE in app
- [ ] `CANCELLATION` / `EXPIRATION` events visible (webhook logs / dashboard)

### 7.4 Auto-renewals
- [ ] Leave subscription active → RENEWAL event each 5-min interval
- [ ] After 6 renewals Google stops auto-renewing (sandbox cap) → expires

### 7.5 Restore
- [ ] Uninstall/reinstall (or `logOut`) → `restorePurchases()` → entitlement returns
- [ ] Cross-account: User_A purchases → logout → User_B restores → entitlement transfers (or blocked, per project setting)

### 7.6 Refund / revoke
- [ ] Refund/revoke from Play Console or grant-then-revoke in RevenueCat
- [ ] Entitlement stripped from `CustomerInfo` promptly
- [ ] Webhook delivers revocation to Firebase

---

## Sandbox timing reference

| Production | Sandbox renewal | Max renewals |
|---|---|---|
| 1 week | 5 min | 6 |
| 1 month | 5 min | 6 |
| 1 year | 30 min | 6 |

## RTDN_VERIFICATION (how to complete the RTDN item)

1. RevenueCat Dashboard → Project → Google Play App Settings → where service credentials are added → click **Connect to Google** → copy the generated **Pub/Sub topic ID** (`projects/{project_id}/topics/{name}`).
2. Play Console → Monetize → Monetization Setup → Real-time developer notifications → paste topic ID into **Topic name** → Notification content = **Subscriptions, voided purchases and all one-time products** → **Save**.
3. Click **Send test notification**.
4. RevenueCat Dashboard → same Google Play App Settings page → confirm **"Last received"** shows a fresh timestamp → RTDN verified ✅.
5. If no "Last received": Cloud Console → Pub/Sub → topic → Permissions → add `google-play-developer-notifications@system.gserviceaccount.com` with role **Pub/Sub Publisher**, then resend test.
6. Note: freshly created service credentials take ~36h to activate on RevenueCat side.

## Recommended build order

1. Dashboard setup (sections 1–3) — needs no code, produces IDs you'll reference
2. Code section 6, in order: DI wiring ✅ → buy-state flow ✅ → PaywallScreen ✅ → button routing ✅ → **entitlement bridge ⬅ next**
3. Ship to internal track, then run section 7 top to bottom
