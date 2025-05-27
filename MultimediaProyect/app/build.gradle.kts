// app/build.gradle.kts - Versión optimizada

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlinx-serialization")
}

android {
    namespace = "com.azterketa.multimediaproyect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.azterketa.multimediaproyect"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle components (necesarios para ViewModels)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // Supabase (versión más reciente y estable)
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.6.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.6.0")

    // Cliente HTTP (necesario para Supabase)
    implementation("io.ktor:ktor-client-android:2.3.12")

    // Serialización JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}