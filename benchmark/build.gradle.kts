plugins {
    // com.android.test is already on the classpath via AGP; requesting it with a
    // version triggers "plugin is already on the classpath with an unknown version".
    // AGP 9 ships built-in Kotlin support (the kotlin extension is already registered),
    // so org.jetbrains.kotlin.android must NOT be applied in this module.
    id("com.android.test")
}

android {
    namespace = "com.mknlabs.expensetracker.benchmark"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testProguardFiles("proguard-rules.pro")
    }

    // Target app to benchmark.
    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    buildTypes {
        // Mirrors the :app benchmark variant; matchingFallbacks resolves multi-module
        // variant matching for the release-shaped build type.
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            // Shrunk (R8) but NOT obfuscated (see proguard-rules.pro -dontobfuscate).
            // AGP's checkTestedAppObfuscation requires this test APK to shrink when the
            // tested app (:app benchmark) is shrunk; but the instrumentation runner runs
            // inside the app process, so obfuscating this APK too rewrites the runner's
            // references to obfuscated names (kotlin.Lazy -> zc2, etc.) that collide with
            // the app's dex -> IncompatibleClassChangeError / NoClassDefFoundError at
            // startup. Shrink + keep real names = AGP check satisfied + runner works.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )
        }
    }

    // NOTE: Gradle-managed devices (testOptions.managedDevices) intentionally not configured
    // here — the DSL failed to resolve in this module under AGP 9.3.1 (the repo's
    // baselineprofile module uses the same block but is not part of the build).
    // Benchmarks run via :benchmark:connectedCheck against any connected device/CI runner.
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.macrobenchmark)
    // The com.android.test plugin does NOT add kotlin-stdlib to the runtime classpath the
    // way com.android.application does, so the runner's Kotlin classes (kotlin.LazyKt at
    // startup) are missing from the test APK entirely -> ClassNotFoundException. Add it
    // explicitly; proguard-rules.pro keeps it defined under R8.
    implementation(libs.kotlin.stdlib)
    // Resolves R8 references from androidx.benchmark.macro.ProfileInstallBroadcast to
    // the receiver in the target app (transitively pulls androidx.startup).
    implementation(libs.androidx.profileinstaller)
    // androidx.test/lifecycle internals reference these; make the classes present for R8.
    implementation(libs.androidx.lifecycle.runtime.ktx) // -> androidx.arch.core
    implementation(libs.errorprone.annotations)
}
