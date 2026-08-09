plugins {
    // com.android.test is already on the classpath via AGP; requesting it with a
    // version triggers "plugin is already on the classpath with an unknown version".
    // AGP 9 ships built-in Kotlin support (the kotlin extension is already registered),
    // so org.jetbrains.kotlin.android must NOT be applied in this module.
    // (Mirrors :benchmark's working config.)
    id("com.android.test")
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.mknlabs.expensetracker.baselineprofile"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    // NOTE: Gradle-managed devices (testOptions.managedDevices / baselineProfile.managedDevices)
    // are intentionally NOT configured here — the DSL fails to resolve under AGP 9.3.1 (the
    // same issue :benchmark documents). Baseline profile generation runs on a CONNECTED
    // device instead (emulator or physical).
    //
    // The androidx.baselineprofile producer plugin maps its `nonMinifiedRelease` test variant to
    // the target app's `release` build type via matchingFallbacks. That build has NO BenchmarkHooks
    // (they're gated on BUILD_TYPE == "benchmark"), so the app would show onboarding with an empty
    // DB and the ad-enabled journeys would fail. Pre-declaring `nonMinifiedRelease` HERE makes the
    // plugin's createBuildTypeIfNotExists skip it (it only configures missing build types), so our
    // matchingFallbacks → app `benchmark` variant wins: the hooks seed data + skip onboarding, and
    // the collected profile covers the free-tier (ads-on) path exactly like the benchmark journeys.
    // R8 keeps real names there (-dontobfuscate in benchmark-rules.pro), so profile rules
    // reference un-obfuscated class names.
    buildTypes {
        create("nonMinifiedRelease") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("benchmark")
        }
    }
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.macrobenchmark)
}
