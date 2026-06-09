plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "com.blivtech.emptrack"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.blivtech.emptrack"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // ✅ Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // ✅ Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ✅ Hilt — using KSP now
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)             // ✅ ksp() instead of kapt()

    // ✅ Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // ✅ OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // ✅ Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // ✅ DataStore
    implementation(libs.androidx.datastore.preferences)

    // ✅ Splash Screen
    implementation(libs.androidx.core.splashscreen)
    // ✅ Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}