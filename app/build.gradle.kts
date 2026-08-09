import java.util.Properties

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
        versionCode = 129
        versionName = "2.40.0"

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

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        getByName("main") {
            assets {
                directories.add(
                    "src/main/java/com/mknlabs/expensetracker/data/legacyimport"
                )
            }
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
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
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.glance.preview)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

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
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
}
