// app/build.gradle.kts - Versión ultra simplificada

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlinx-serialization")
}

android {
    namespace = "com.azterketa.multimediaproyect"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.azterketa.multimediaproyect"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android (mínimo necesario)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle solo ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Supabase (versiones estables)
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.4")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.4")

    // Cliente HTTP
    implementation("io.ktor:ktor-client-android:2.3.7")

    // Serialización
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}