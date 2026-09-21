# Testing Guide: RevenueCat Subscription Flow

This guide provides step-by-step instructions for testing the RevenueCat subscription integration in the ExpenseTracker app.

## Prerequisites

1. **RevenueCat Account Setup**
   - Create a RevenueCat account at https://revenuecat.com
   - Create a new app in the RevenueCat dashboard
   - Set your app's platform to Android
   - Add your Google Play Store public key (found in Play Console > Setup > Licensing & in-app billing)

2. **Configure Offerings in RevenueCat**
   - Create offerings with the following identifiers:
     - `offering_monthly` (or similar)
     - `offering_yearly` (or similar)
     - `offering_lifetime` (or similar)
   - Create packages within each offering:
     - Monthly subscription
     - Yearly subscription (with optional trial)
     - Lifetime purchase
   - Ensure each package has a valid product identifier that matches what you'll configure in Google Play

3. **Google Play Configuration**
   - Create in-app products in Google Play Console:
     - Monthly subscription product
     - Yearly subscription product
     - One-time lifetime purchase product
   - Note: For testing, you can use license test accounts and upload an internal test build

4. **Firebase Setup**
   - Ensure your Firebase project is linked to your Android app
   - Enable Firebase Auth and Firestore
   - Install the RevenueCat Firebase Extension in your Firebase project:
     - Go to Firebase Console > Extensions
     - Search for "RevenueCat" and install the extension
     - Configure it to write to the `users/{uid}/subscriptions` collection

## Testing Steps

### 1. Local Properties Configuration
Ensure your `localProperties.properties` file contains your RevenueCat API key:
```
revenueCatApiKey=your_actual_revenuecat_api_key_here
```

### 2. Build and Run the App
- Build and run the app on an emulator or physical device
- Note: You need a Google Play account on the device for billing to work
- For testing without publishing to Play Store, you can use:
  - License test accounts in Google Play Console
  - Internal app sharing
  - RevenueCat's sandbox mode (which works with test accounts)

### 3. Test Authentication Flow
1. Launch the app
2. Sign in with a test Google account (use a license test account for billing tests)
3. Observe that the app logs you into RevenueCat with your Firebase UID
4. Sign out and back in to test the logout/login synchronization

### 4. Test Paywall Screen
1. Navigate to a premium feature that triggers the PaywallScreen (or directly navigate to it for testing)
2. Verify that:
   - Offerings load successfully from RevenueCat
   - Packages are displayed with correct pricing
   - Popular/best offerings are highlighted
   - Savings badges appear for annual vs monthly comparisons
   - Terms and Privacy links work correctly

### 5. Test Purchase Flow (Sandbox)
**Important: Use only test license accounts for actual purchases!**

1. Select a subscription package (monthly or yearly)
2. Complete the purchase flow using a test account
3. Verify:
   - Purchase success callback is received
   - UI updates to reflect active subscription
   - EntitlementGuard components update accordingly
   - CustomerInfo is updated in BillingManager

### 6. Test Purchase Restoration
1. After making a purchase, sign out and back in (or clear app data)
2. Navigate to a premium feature or the paywall
3. Tap "Restore Purchases"
4. Verify:
   - Previously purchased subscription is restored
   - Entitlement status is updated
   - UI reflects active subscription

### 7. Test Firebase Extension Sync
1. After a purchase or restoration, check your Firestore database
2. Verify that a document exists at `users/{uid}/subscriptions` (where uid is your Firebase UID)
3. Check that the document contains subscription information from RevenueCat
4. Confirm that the data updates when subscription status changes

### 8. Test Entitlement Guards
1. Navigate to screens that use EntitlementGuard or SubscribedContent
2. Verify that:
   - Premium content is hidden when not subscribed
   - Locked state with upgrade CTA is shown
   - Premium content is revealed after successful purchase
   - Content is hidden again after subscription expires (if testing with short durations)

## Troubleshooting

### Common Issues

1. **"RevenueCat API key not configured"**
   - Check that `localProperties.properties` exists and contains the correct key
   - Ensure the key matches what's in your RevenueCat app settings
   - Clean and rebuild the project after updating the key

2. **No offerings loading**
   - Verify your app user ID is correctly logged into RevenueCat
   - Check that offerings are properly configured in the RevenueCat dashboard
   - Ensure the app has internet permission
   - Look for errors in Logcat tagged with "BillingManager"

3. **Purchase failures**
   - Confirm you're using a license test account for actual purchases
   - Verify that the product IDs in RevenueCat match those in Google Play
   - Check that billing is set up correctly in Google Play Console
   - For sandbox testing, ensure you have a valid test card on file

4. **Firebase Extension not syncing**
   - Verify the extension is installed and configured in Firebase Console
   - Check that the extension has permission to write to Firestore
   - Look for extension execution logs in Firebase Console
   - Ensure Firestore security rules allow the extension to write

## Verification Checklist

- [ ] localProperties.properties created with RevenueCat API key
- [ ] Firebase App Check initialized correctly (debug/release variants)
- [ ] RevenueCat SDK initialized with correct API key
- [ ] Auth listener properly syncs Firebase UID with RevenueCat
- [ ] Offerings load and display correctly in PaywallScreen
- [ ] Purchase flow completes successfully with test account
- [ ] Purchase restoration works correctly
- [ ] Entitlement status updates across app components
- [ ] Firebase Extension writes subscription data to Firestore
- [ ] Firestore security rules allow extension writes
- [ ] EntitlementGuard components respond to subscription status changes

## Safety Notes

- Always use license test accounts for actual purchases during development
- Never use real payment information in debug/test builds
- Reset test purchases in RevenueCat dashboard if needed for repeat testing
- Be cautious when testing with real money - use very small amounts if absolutely necessary

Once you've completed these tests and verified the checklist items, your subscription infrastructure is ready for production release!