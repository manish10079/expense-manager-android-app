# Complete Android App Security Checklist 🛡️📱

Think of app security as a spaceship hull. One tiny crack and vacuum starts sipping your user data through a straw.

## 1. Secure Network Communication 🌐

### Use HTTPS Only
Always use:
```text
https://
```

### Disable Cleartext HTTP
```xml
<application
    android:usesCleartextTraffic="false">
</application>
```

---

## 2. Certificate Pinning 🔒

### Why
Prevents fake certificate MITM attacks.

### OkHttp Example
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add(
        "yourdomain.com",
        "sha256/AAAAAAAAAAAAAAAAAAAA"
    )
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

---

## 3. API Key Protection 🔑

### Never hardcode secrets
```kotlin
const val API_KEY = "secret"
```

### Better
- Store on backend
- Use short-lived tokens
- Use Android Keystore

---

## 4. Obfuscation & Shrinking 🕵️

### Enable R8
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
    }
}
```

---

## 5. Encrypt Local Storage 🔐

### EncryptedSharedPreferences
```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()
```

---

## 6. Android Keystore 🔑

### Generate Secure Key
```kotlin
val keyGenerator = KeyGenerator.getInstance(
    KeyProperties.KEY_ALGORITHM_AES,
    "AndroidKeyStore"
)
```

---

## 7. Root Detection 🧨

### Using RootBeer
```kotlin
val rootBeer = RootBeer(context)

if(rootBeer.isRooted){
    finish()
}
```

---

## 8. Emulator Detection 🤖

```kotlin
val isEmulator =
    Build.FINGERPRINT.contains("generic")
```

---

## 9. Play Integrity API ✅

### Dependency
```gradle
implementation 'com.google.android.play:integrity:1.3.0'
```

---

## 10. Secure Authentication 👤

### Recommended
- OAuth2
- JWT
- MFA
- Refresh Tokens

---

## 11. Prevent Screenshots 📸

```kotlin
window.setFlags(
    WindowManager.LayoutParams.FLAG_SECURE,
    WindowManager.LayoutParams.FLAG_SECURE
)
```

---

## 12. Secure WebView 🌍

```kotlin
webView.settings.javaScriptEnabled = false
webView.settings.allowFileAccess = false
```

---

## 13. SQL Injection Prevention 💉

### BAD
```kotlin
db.rawQuery(
    "SELECT * FROM users WHERE id = '$id'",
    null
)
```

### GOOD
```kotlin
db.query(
    "users",
    null,
    "id=?",
    arrayOf(id),
    null,
    null,
    null
)
```

---

## 14. Secure Firebase 🔥

### BAD
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

### GOOD
```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

---

## 15. Anti-Tamper Protection 📦

### Verify App Signature
```kotlin
val signatures = packageManager.getPackageInfo(
    packageName,
    PackageManager.GET_SIGNING_CERTIFICATES
)
```

---

## 16. Detect Frida / Xposed 🧬

### Detect Suspicious Runtime
```kotlin
fun detectFrida(): Boolean {
    return try {
        Runtime.getRuntime().exec("su")
        true
    } catch (e: Exception) {
        false
    }
}
```

---

## 17. Native Code Security ⚙️

Use NDK (C/C++) for:
- encryption
- anti-tamper
- integrity checks

---

## 18. Remove Logs in Production 🪵

```proguard
-assumenosideeffects class android.util.Log {
    *;
}
```

---

## 19. Runtime Permission Security 🎯

Ask only for required permissions.

---

## 20. Deep Link Validation 🔗

Validate:
- host
- parameters
- authentication

---

## 21. Prevent Backup Extraction 📂

```xml
<application
    android:allowBackup="false">
</application>
```

---

## 22. Backend Validation 🧠

Never trust app data.

Always validate:
- purchases
- rewards
- balances
- subscriptions

---

## 23. Dependency Security 📦

Keep libraries updated using:
- Dependabot
- Snyk

---

## 24. CI/CD Security 🚀

Protect:
- signing keys
- keystore files
- secrets

Never upload sensitive files publicly.

---

## 25. Banking-Level Protections 🏦

Advanced apps use:
- RASP
- DexGuard
- anti-debugging
- hardware attestation

---

# Recommended Stack 🚀

## Beginner Apps
- HTTPS
- Firebase Rules
- EncryptedSharedPreferences
- R8

## Professional Apps
- Play Integrity API
- Certificate Pinning
- Root Detection
- Backend Validation

## Banking-Level Apps
- Frida Detection
- Runtime Protection
- Strong Integrity
- Native Anti-Tamper

---

# Resources

- https://developer.android.com/topic/security/best-practices
- https://mas.owasp.org/MASVS/
- https://developer.android.com/google/play/integrity

---

# Expense Tracker Security Implementation Status (May 2026)

## ✅ Implemented
- [x] **Item 1: Disable Cleartext HTTP** (Explicitly set `android:usesCleartextTraffic="false"` in Manifest)
- [x] **Item 4: Obfuscation & Shrinking** (R8 enabled in release builds)
- [x] **Item 5: Encrypt Local Storage** (EncryptedSharedPreferences used for App Lock)
- [x] **Item 6: Android Keystore** (Used via AndroidX Security library for encryption)
- [x] **Item 10: Secure Authentication** (4-digit PIN lock with salted SHA-256 and Biometric integration)
- [x] **Item 11: Prevent Screenshots** (FLAG_SECURE integration with user-controlled toggle)
- [x] **Item 13: SQL Injection Prevention** (Using Room persistence library with parameter binding)
- [x] **Item 18: Remove Logs in Production** (ProGuard rules added to strip all `android.util.Log` calls)
- [x] **Item 19: Runtime Permission Security** (Requesting minimal permissions: Biometrics and Notifications only)

## 🟡 Not Yet Implemented / Pending
- [ ] **Item 7: Root Detection** (No root check currently implemented)
- [ ] **Item 8: Emulator Detection** (No emulator check currently implemented)
- [ ] **Item 9: Play Integrity API** (Required for strong device/app attestation)
- [ ] **Item 21: Prevent Backup Extraction** (`android:allowBackup` is currently set to `true` to allow local backups, but poses a risk of data extraction via ADB)
