plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.freshcookapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.freshcookapp"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(platform("io.coil-kt:coil-bom:2.7.0"))
    implementation("io.coil-kt:coil-compose")
    // Firebase Bill of Materials (BoM) - quản lý phiên bản
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    // Firebase Analytics (để xem người dùng)
    implementation("com.google.firebase:firebase-analytics")
    // Firebase Authentication (để đăng nhập)
    implementation("com.google.firebase:firebase-auth")
    // Google Sign-In (hỗ trợ đăng nhập)
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

}