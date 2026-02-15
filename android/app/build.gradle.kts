plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

import java.util.Properties

// Load local.properties for API keys
val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localPropsFile.inputStream().use { localProps.load(it) }
}

android {
    namespace = "com.discipl.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.discipl.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // SDK keys — loaded from local.properties (gitignored) or CI env vars
        buildConfigField("String", "POSTHOG_API_KEY", "\"${localProps.getProperty("POSTHOG_API_KEY", System.getenv("POSTHOG_API_KEY") ?: "")}\"")
        buildConfigField("String", "POSTHOG_HOST", "\"${localProps.getProperty("POSTHOG_HOST", System.getenv("POSTHOG_HOST") ?: "https://us.i.posthog.com")}\"")
        buildConfigField("String", "REVENUECAT_API_KEY", "\"${localProps.getProperty("REVENUECAT_API_KEY", System.getenv("REVENUECAT_API_KEY") ?: "")}\"")
        buildConfigField("String", "SUPERWALL_API_KEY", "\"${localProps.getProperty("SUPERWALL_API_KEY", System.getenv("SUPERWALL_API_KEY") ?: "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Room + SQLCipher encryption
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")

    // Security (EncryptedSharedPreferences, Keystore)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.53.1")
    ksp("com.google.dagger:hilt-compiler:2.53.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Glance (Widgets)
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    // Gson
    implementation("com.google.code.gson:gson:2.11.0")

    // PostHog
    implementation("com.posthog:posthog-android:3.9.1")

    // RevenueCat
    implementation("com.revenuecat.purchases:purchases:8.12.0")
    implementation("com.revenuecat.purchases:purchases-ui:8.12.0")

    // Superwall
    implementation("com.superwall.sdk:superwall-android:2.1.0")

    // DataStore (for widget data sharing)
    implementation("androidx.datastore:datastore-preferences:1.1.2")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
