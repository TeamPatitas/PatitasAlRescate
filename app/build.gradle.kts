plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.patitasalrescate"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.patitasalrescate"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation("androidx.cardview:cardview:1.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // Para HTTP (llamadas a Supabase REST API)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
// Para parsear JSON (Gson es más simple que JSONObject)
    implementation("com.google.code.gson:gson:2.10.1")
// Glide (ya lo tenías, pero por si acaso)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    

}