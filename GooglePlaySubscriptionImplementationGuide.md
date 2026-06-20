# Google Play Subscription Billing Implementation Guide

## ROLE

You are a Senior Android Staff Engineer with deep expertise in:

* Kotlin
* Jetpack Compose
* MVVM
* Google Play Billing Library v8+
* Firebase
* Firestore
* DataStore
* Coroutines
* Flow
* Dependency Injection
* Play Console Subscription Management
* Production Android Architecture

Your goal is to implement a COMPLETE PRODUCTION-READY Google Play Subscription System.

---

## IMPORTANT RULES

1. Never jump directly to coding.
2. Work phase-by-phase.
3. After every phase:

   * Compile project.
   * Fix all build errors.
   * Fix warnings if relevant.
   * Run tests.
   * Verify app behavior.
4. Do not continue to next phase until current phase passes validation.
5. At the end of each phase provide:

   * Changes made
   * Files modified
   * Build status
   * Test results
   * Next phase plan
6. Follow Clean Architecture and MVVM.
7. Use latest Google Play Billing Library.
8. Use Kotlin best practices.
9. Avoid technical debt.
10. Code should be production quality.

---

# PROJECT GOAL

Implement Google Play Subscription Billing with:

* 1 Month Plan
* 6 Month Plan
* 12 Month Plan

### Features

* Auto-renew subscriptions
* Purchase flow
* Subscription restore
* Premium unlock
* DataStore persistence
* Firestore sync
* Purchase acknowledgement
* Subscription verification
* Premium state management
* Upgrade/Downgrade support
* Cancellation handling
* Offline support
* Production architecture

---

# PHASE 0 - PROJECT AUDIT

## Analyze Existing Project

Create report:

1. Package structure
2. Current architecture
3. Existing ViewModels
4. Existing repositories
5. Existing Firebase implementation
6. Existing authentication system
7. Existing navigation system
8. Existing DataStore implementation
9. Existing dependency injection setup
10. Existing user model

### Identify

* Missing dependencies
* Architecture problems
* Billing integration points
* Firestore integration points
* Subscription storage location

### Deliverable

Subscription Integration Audit Report

### IMPORTANT

DO NOT WRITE CODE YET.

---

# PHASE 1 - DEPENDENCY SETUP

## Implement

```gradle
implementation("com.android.billingclient:billing-ktx:LATEST")
```

### Verify Manifest

```xml
<uses-permission android:name="com.android.vending.BILLING"/>
```

### Tasks

* Sync project
* Resolve dependency conflicts
* Verify compilation

### Tests

* Project builds successfully
* No Gradle errors
* No dependency conflicts

### STOP AND REPORT

---

# PHASE 2 - SUBSCRIPTION DOMAIN DESIGN

Create:

* SubscriptionPlan
* PremiumState
* PurchaseResult
* BillingUiState
* SubscriptionStatus
* EntitlementState

Support:

* Monthly
* Six Month
* Yearly
* Active
* Expired
* Cancelled
* On Hold
* Grace Period

### Tests

* Project compiles
* Models usable
* Serialization verified

### STOP AND REPORT

---

# PHASE 3 - DATASTORE PREMIUM STORAGE

Create:

## PremiumPreferences

Store:

* isPremium
* productId
* purchaseToken
* purchaseDate
* expiryDate
* autoRenewEnabled
* lastVerificationTime

## Repository

PremiumLocalRepository

### Requirements

* Flow support
* Atomic updates
* Coroutine safe

### Tests

* Save premium state
* Read premium state
* App restart persistence
* Corruption handling

### Compile and Report

---

# PHASE 4 - BILLING MANAGER

Create:

## BillingManager

Responsibilities:

* Billing connection
* Product loading
* Purchase flow
* Restore purchases
* Purchase updates
* Purchase acknowledgements

Functions:

* connect()
* disconnect()
* loadProducts()
* purchase()
* restorePurchases()
* acknowledgePurchase()
* queryActiveSubscriptions()

### Requirements

* Robust logging
* Error handling
* Retry support

### Tests

* Billing connection established
* Reconnection works
* No crashes

### Compile and Report

---

# PHASE 5 - PRODUCT FETCHING

Products:

* premium_monthly
* premium_6month
* premium_yearly

Load dynamically:

* Title
* Description
* Price
* Currency
* Offer Details

### Requirements

* No hardcoded pricing
* Dynamic Play Store values

### Tests

* Products returned
* Prices displayed
* Empty state handled
* Network failure handled

### Compile and Report

---

# PHASE 6 - SUBSCRIPTION SCREEN UI

Display:

* Monthly Plan
* 6 Month Plan
* 12 Month Plan

Each card shows:

* Name
* Price
* Benefits
* Selected state

Buttons:

* Subscribe
* Restore Purchase

### Requirements

* Material 3
* Jetpack Compose
* Responsive
* Dark theme support

### Tests

* Screen loads
* Prices visible
* Selection works
* Rotation safe

### Compile and Report

---

# PHASE 7 - PURCHASE FLOW

Implement purchase initiation.

Use:

```kotlin
launchBillingFlow()
```

Handle:

* Success
* User Cancelled
* Error
* Pending Purchase

Store:

* Product ID
* Token
* Purchase Time

### Tests

* Monthly purchase
* 6 Month purchase
* Yearly purchase
* Cancellation
* Pending purchase

### Compile and Report

---

# PHASE 8 - PURCHASE ACKNOWLEDGEMENT

Critical phase.

Implement:

```kotlin
acknowledgePurchase()
```

### Requirements

* Auto acknowledgement
* Retry mechanism
* Duplicate prevention

### Tests

* Purchase acknowledged
* No refund risk
* Duplicate protection

### Compile and Report

---

# PHASE 9 - PREMIUM UNLOCK SYSTEM

Create:

## PremiumGuard

Function:

```kotlin
canAccessPremium()
```

### Requirements

* Observe entitlement via Flow
* Unlock premium automatically
* Remove access after expiry

### Tests

* Premium unlocks
* Premium removed after expiry
* Premium survives restart

### Compile and Report

---

# PHASE 10 - RESTORE PURCHASES

Implement:

Restore Purchase Button

Use:

```kotlin
queryPurchasesAsync()
```

### Requirements

* Reinstall support
* New device support
* Login support

### Tests

* Reinstall
* Device migration
* Restore works

### Compile and Report

---

# PHASE 11 - FIRESTORE INTEGRATION

Firestore path:

```text
users/{uid}
```

Store:

* subscriptionType
* purchaseToken
* expiryDate
* autoRenewEnabled
* active

Create:

SubscriptionFirestoreRepository

### Requirements

* Sync after purchase
* Sync after restore
* Sync on startup

### Tests

* Firestore updates
* Reads work
* Offline handling

### Compile and Report

---

# PHASE 12 - SERVER SIDE VERIFICATION PREPARATION

Design architecture for:

Google Play Developer API

Prepare:

* VerificationRepository
* VerificationResult
* VerificationState

### Important

Do NOT implement Cloud Functions yet.

### Tests

* Architecture review
* Compile success

### Compile and Report

---

# PHASE 13 - SUBSCRIPTION STATUS HANDLING

Handle:

* ACTIVE
* EXPIRED
* CANCELLED
* GRACE_PERIOD
* ON_HOLD
* PAUSED
* UNKNOWN

### Requirement

State machine architecture.

### Tests

* Every status mapped correctly
* UI updates correctly

### Compile and Report

---

# PHASE 14 - OFFLINE SUPPORT

Requirements:

* Use cached premium state
* Continue working offline
* Resume verification when online

### Tests

* Airplane mode
* Reconnect
* Restart offline

### Compile and Report

---

# PHASE 15 - UPGRADE & DOWNGRADE SUPPORT

Handle:

* Monthly → Yearly
* Monthly → 6 Month
* 6 Month → Yearly
* Yearly → Monthly

### Requirement

Replacement Modes

### Tests

* Upgrade
* Downgrade
* Entitlement preserved

### Compile and Report

---

# PHASE 16 - ERROR HANDLING

Handle:

* Network Failure
* Play Store Missing
* Billing Unavailable
* Purchase Failure
* Corrupted State
* Firestore Failure

### Requirement

User-friendly messages

### Tests

* Simulated failures
* Recovery paths

### Compile and Report

---

# PHASE 17 - ANALYTICS

Track:

* Subscription Screen Viewed
* Purchase Started
* Purchase Success
* Purchase Failed
* Restore Success
* Restore Failed
* Subscription Active
* Subscription Expired

### Tests

* Events fire correctly

### Compile and Report

---

# PHASE 18 - SECURITY REVIEW

Audit:

* Hardcoded secrets
* Premium bypasses
* Local storage tampering
* Token storage
* Firestore Rules impact

### Deliverable

Security Audit Report

### Compile and Report

---

# PHASE 19 - FULL TESTING

## Purchase Tests

* Monthly
* 6 Month
* Yearly

## Restore Tests

* Reinstall
* New Device
* Logout/Login

## Premium Tests

* Unlock
* Lock
* Expiry

## Offline Tests

* Offline launch
* Offline usage

## Failure Tests

* Billing unavailable
* Network failure
* Firestore failure

## Regression Tests

* Existing features still work

### Deliverable

Complete Test Report

---

# PHASE 20 - PRODUCTION READINESS REVIEW

Review:

* Architecture
* Code Quality
* Performance
* Memory
* Security
* Scalability

Generate:

1. Final Architecture Diagram
2. Remaining Risks
3. Future Improvements
4. Production Readiness Score
5. Launch Checklist

---

# FINAL DELIVERABLES

Provide:

1. File Tree
2. Modified Files
3. New Files
4. Architecture Diagram
5. Data Flow Diagram
6. Purchase Flow Diagram
7. Firestore Schema
8. DataStore Schema
9. Testing Report
10. Security Report
11. Production Checklist
12. Technical Debt List
13. Future Improvements

---

# FINAL RULE

Never skip a phase.

After every phase:

1. Build project
2. Run tests
3. Verify functionality
4. Fix issues
5. Report results

Only then continue to the next phase.
