plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.spotifyandyoutube"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.spotifyandyoutube"
        minSdk = 24
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(libs.okhttp) // 添加OkHttp依赖
    implementation(libs.google.gson) // 添加Gson依赖
    implementation(libs.play.services.maps)
    implementation(libs.places)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // All other dependencies for your app should also be here:
    implementation(libs.androidx.browser.browser)
    implementation(libs.appcompat)
}