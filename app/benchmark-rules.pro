# R8 keep rules for the :app "benchmark" build type only (never applied to release/debug).
#
# The Macrobenchmark instrumentation (androidx.test runner) executes inside the app's
# process for the non-debuggable benchmark variant, sharing one classloader with the app
# dex. To avoid name collisions with the (shrunk-but-unobfuscated) test APK, this variant
# must also never obfuscate — shrinking stays on, so AGP's checkTestedAppObfuscation still
# passes (mapping.txt is produced). The runner's startup classes must also be resolvable
# from the app dex, so keep them with real names. Benchmark-only APK, never shipped —
# keeping whole libraries is acceptable.
-dontobfuscate

-dontwarn androidx.tracing.**
-keep class androidx.tracing.** { *; }

-dontwarn androidx.test.**
-keep class androidx.test.** { *; }

-dontwarn androidx.benchmark.**
-keep class androidx.benchmark.** { *; }

# The runner's startup path also needs the Kotlin stdlib resolvable from the app dex
# (belt-and-suspenders alongside the :benchmark module keeps).
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }

-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.lang.model.element.Modifier
