# 🚀 ExpenseTracker Release Guide

This guide explains how to generate a production-ready Signed APK or App Bundle for the Google Play Store using the latest version of Android Studio.

## 🔑 Phase 1: Generating the Keystore & Signed App

1.  **Open the Wizard**: In Android Studio, go to **Build** > **Generate Signed Bundle / APK...**
2.  **Select Format**: 
    *   Choose **Android App Bundle (.aab)** for Google Play Store uploads.
    *   Choose **APK** if you want a file to share directly with friends for testing.
    *   Click **Next**.
3.  **Create Key Store**:
    *   Click **Create new...** under "Key store path".
    *   **Path**: Save the file outside your project folder (e.g., `Documents/keys/expensetracker.jks`).
    *   **Password**: Create a strong password (write it down!).
    *   **Alias**: `key0` (default is fine).
    *   **Key Password**: Keep this the same as the Store password for simplicity.
    *   **Certificate**: Fill in your "First and Last Name".
    *   Click **OK**.
4.  **Destination**: Select the **release** build variant and click **Finish**.

---

## ⚠️ Phase 2: Critical Security Rules

*   **NEVER LOSE THE KEY**: If you lose the `.jks` file or forget the password, you can **never** update this app on the Play Store again. Back it up in at least two secure places.
*   **DO NOT COMMIT TO GIT**: Never upload your `.jks` file to GitHub or any public repository.
*   **PASSWORDS**: Keep your keystore passwords in a safe password manager.

---

## ✅ Phase 3: Final Verification (Real Ads)

Once you install the **Release** version on a physical device:

1.  **Test Ad Ribbon**: The "Test Ad" label should **disappear**. You are now seeing real ads.
2.  **Ad Validator**: The "Native Ad Validator" tooltip (the "What is this?" bubble) will **disappear** for all users.
3.  **Ad-Free Logic**:
    *   Go to **Settings** > **Monetization**.
    *   Watch a Rewarded Video.
    *   Verify that all ads in the app are hidden for 1 hour.
4.  **DO NOT CLICK**: Do not click your own real ads. Google will detect this as "Invalid Traffic" and may ban your AdMob account.

---

## 🛠️ Technical Note: Environment Detection
The app automatically switches between Test and Real ads using this logic in `AdsCoordinator.kt`:
```kotlin
if (BuildConfig.DEBUG) {
    // Serves Google Test IDs (Safe for Development)
} else {
    // Serves your Real Unit IDs (Revenue Mode)
}
```
