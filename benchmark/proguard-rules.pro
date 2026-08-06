# AndroidJUnitRunner discovers test classes reflectively — keep them intact under R8
# (required because the tested :app benchmark variant is shrunk, so this APK shrinks too).
-keep class com.mknlabs.expensetracker.benchmark.** { *; }

# CRITICAL: never obfuscate this APK. The runner executes inside the :app benchmark
# process sharing one classloader with the app dex; obfuscation rewrites internal
# references to renamed classes (kotlin.Lazy -> zc2) that break the kept test classes
# (IncompatibleClassChangeError) and collide with the app's dex. AGP's
# checkTestedAppObfuscation is still satisfied: R8 runs (shrinking), which produces
# mapping.txt — only name obfuscation is disabled.
-dontobfuscate

# AndroidJUnitRunner + the whole androidx.test/benchmark surface must stay intact and
# DEFINED in this APK. R8 shrinking otherwise removes runner dependency classes that are
# only referenced via inlined calls (observed: NoClassDefFoundError for
# androidx.tracing.Trace and kotlin.LazyKt at runner startup — the dex contained the
# reference but not the class definition). Keep per Google's macrobenchmark sample rules.
-dontwarn androidx.test.**
-keep class androidx.test.** { *; }

-keep class androidx.tracing.** { *; }
-dontwarn androidx.tracing.**

-keep class androidx.benchmark.** { *; }
-dontwarn androidx.benchmark.**

# Runner startup path (RunnerArgs -> PlatformTestStorageRegistry -> TestDirCalculator)
# uses kotlin.LazyKt from the Kotlin stdlib.
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }

-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.lang.model.element.Modifier
