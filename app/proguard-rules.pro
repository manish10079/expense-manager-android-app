# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Log stripping intentionally DISABLED so Google Sign-In failures are visible
# in release logcat (filter by tag "AUTH" or "AuthRepo"). Re-enable once auth
# is stable in production.
#-assumenosideeffects class android.util.Log {
#    public static *** d(...);
#    public static *** v(...);
#    public static *** i(...);
#}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.lang.model.element.Modifier

# RootBeer (root detection) uses JNI + reflection — keep the whole library under R8.
-keep class com.scottyab.rootbeer.** { *; }

# Firebase App Check — Play Integrity and Debug providers use reflection
# for factory instantiation; keep them under R8 minification.
-keep class com.google.firebase.appcheck.** { *; }
-keep class com.google.android.play.core.integrity.** { *; }

# AndroidX Credential Manager + Google Identity — required for Google Sign-In
# via CredentialManager. Without these, R8 can strip or rename classes that
# the Credential Manager uses to resolve Google ID tokens at runtime.
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }