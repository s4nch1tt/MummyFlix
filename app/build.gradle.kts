plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
}

android {
    namespace = "com.mummyflix"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mummyflix"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += listOf("META-INF/*.kotlin_module")
        }
    }
}

dependencies {
    // Core Android - use BOM compatible versions
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Material Components - provides Material theme resources
    implementation("com.google.android.material:material:1.12.0")

    // Leanback - provides Theme.Leanback for Android TV
    implementation("androidx.leanback:leanback:1.2.0")

    // Compose for TV
    implementation("androidx.tv:tv-material:1.1.0")
    implementation("androidx.tv:tv-foundation:1.0.0")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.ui:ui:1.7.3")
    implementation("androidx.compose.ui:ui-graphics:1.7.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.3")
    implementation("androidx.compose.foundation:foundation:1.7.3")
    implementation("androidx.compose.runtime:runtime:1.7.3")
    implementation("androidx.compose.animation:animation:1.7.3")
    implementation("androidx.compose.material:material:1.7.3")
    implementation("androidx.compose.material:material-icons-extended:1.7.3")

    // ExoPlayer for HLS
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-hls:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Serialization for playlist parsing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.2")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.3")
}