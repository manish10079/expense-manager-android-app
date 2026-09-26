import java.util.Properties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.mknlabs.expensetracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mknlabs.expensetracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 248
        versionName = "2.124.32"
        resValue("string", "label_app_version", "v$versionName")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    // Load signing credentials from keystore.properties (gitignored)
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties().apply {
        if (keystorePropertiesFile.exists()) {
            keystorePropertiesFile.inputStream().use { load(it) }
        }
    }

    // Load local properties for secrets (gitignored)
    val localProperties = Properties().apply {
        val f1 = rootProject.file("local.properties")
        if (f1.exists()) {
            f1.inputStream().use { load(it) }
        }
        val f2 = rootProject.file("localProperties.properties")
        if (f2.exists()) {
            f2.inputStream().use { load(it) }
        }
    }
    val rcKey = localProperties.getProperty("revenueCatApiKey", "")
    // Pinned App Check debug token for local development (gitignored). Left blank,
    // the SDK mints a new token on every fresh install and it has to be registered
    // in the Firebase console again; pinned, one registered value is used forever.
    val appCheckDebugToken = localProperties.getProperty("APP_CHECK_DEBUG_TOKEN")
        ?: localProperties.getProperty("appCheckDebugToken")
        ?: ""

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    // Set custom base file name without extensions
    val vName = defaultConfig.versionName ?: "1.0.0"
    val vCode = defaultConfig.versionCode ?: 1
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("ddMMyyyy_HH_mm_ss")
    val timestamp = current.format(formatter)
    base.archivesName.set("ExpenseTracker-v${vName}-vc${vCode}-${timestamp}")

    buildTypes {
        release {
            optimization {
                enable = true // Enables code and resource optimizations.
            }
            signingConfig = signingConfigs.getByName("release")
            ndk {
                // Generates symbol files for native libraries (.so)
                debugSymbolLevel = "FULL" // Options: "FULL" or "SYMBOL_TABLE"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // RevenueCat API key from local properties (kept secret)
            buildConfigField("String", "REVENUE_CAT_API_KEY", "\"$rcKey\"")
            // The App Check debug-secret pinning in ExpenseTrackerApplication lives in
            // the main source set, so it is compiled for release too and needs this
            // field to resolve. It is deliberately empty here — the debug secret must
            // never ship in the bundle — and the only read sits behind
            // BuildConfig.DEBUG, so R8 sees the branch as dead code in release.
            buildConfigField("String", "APP_CHECK_TOKEN", "\"\"")
        }
        // Macrobenchmark target variant: a release-equivalent build (non-debuggable,
        // minified) signed with the debug key. Official docs: create a copy of the
        // release build type via initWith + debug signing.
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            // Keep androidx.tracing/test/benchmark in the target APK so the Macrobenchmark
            // instrumentation runner's startup dependencies resolve inside the app process
            // (see benchmark-rules.pro). Benchmark-only — release stays untouched.
            proguardFiles("benchmark-rules.pro")
        }
        // Debug build type — no minification so stack traces are readable and
        // build times are faster. R8 is strictly release-only.
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            // RevenueCat API key from local properties (kept secret)
            buildConfigField("String", "REVENUE_CAT_API_KEY", "\"$rcKey\"")
            // Pinned App Check debug token (kept secret, debug-only). Blank means
            // the SDK's own rotating debug token is used, as before.
            buildConfigField("String", "APP_CHECK_TOKEN", "\"$appCheckDebugToken\"")
            buildConfigField("String", "APP_CHECK_DEBUG_TOKEN", "\"$appCheckDebugToken\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    sourceSets {
        // Room migration tests read the exported schema JSONs from assets.
        getByName("androidTest") {
            assets {
                directories.add("$projectDir/schemas")
            }
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    ndkVersion = "30.0.16248370"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {
    compilerOptions {
        // KT-73255: apply use-site-less annotations to both the value parameter
        // and the property (Kotlin's future default), silencing the warning for
        // the 22 @ApplicationContext constructor parameters. Dagger still reads
        // the annotation from the constructor parameter, so behavior is unchanged.
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.window)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.glance.preview)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    // Room 2.8.4's schema parser (room-migration, via room-testing) is compiled
    // against kotlinx-serialization 1.8+, but the app's transitive version is
    // pinned to 1.7.3 by datastore. AGP's consistent resolution mirrors the app
    // classpath into androidTest, so the bump has to live here — otherwise the
    // v14->v15 migration tests crash with AbstractMethodError on
    // GeneratedSerializer.typeParametersSerializers().
    implementation(libs.kotlinx.serialization.json)
    ksp(libs.androidx.room.compiler)

    // Paging 3
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // RevenueCat (uses Google Play Billing Library 8.x via v10.15.1)
    implementation(libs.revenuecat.purchases)

    // Glassmorphic Backdrop Blur (Haze)
    implementation(libs.haze)
    implementation(libs.haze.materials)

    // Phosphor Icons (Light, Thin, Regular, Bold, Fill, Duotone)
    implementation(libs.phosphor.icon)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.work.compiler)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.config)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.messaging)
    // Firebase AI Logic — Gemini integration
    implementation(libs.firebase.ai)
    // Firebase App Check — Play Integrity for release, debug token for development
    implementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
    implementation(libs.rootbeer.lib)
    implementation(libs.coil.compose)
}
