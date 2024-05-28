plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.ptst"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ptst"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat.get())
    implementation(libs.material.get())
    implementation(libs.activity.get())
    implementation(libs.constraintlayout.get())
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation(files("libs/AMap_Location_V6.4.3_20240314.jar"))
    implementation(files("libs/AMap_Search_V9.4.0_20220808.jar"))
    implementation(files("libs/Amap_2DMap_V6.0.0_20191106.jar"))
    implementation(files("libs/android-support-v4.jar"))
    implementation(files("libs/Volley.jar"))
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("io.socket:socket.io-client:2.1.0")
    implementation(libs.legacy.support.v4)
    implementation(libs.recyclerview)
    testImplementation(libs.junit.get())
    androidTestImplementation(libs.ext.junit.get())
    androidTestImplementation(libs.espresso.core.get())
}