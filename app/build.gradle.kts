plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // 1. 加入 Hilt 與 KSP 插件
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.login_v3"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.login_v3"
        minSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
    implementation(libs.volley)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    implementation("androidx.compose.material:material-icons-extended:<compose_version>")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-svg:2.5.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.navigation.compose)

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")

    //json stuff
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    //加入 Hilt 核心與編譯器
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // 使用 ksp 取代 kapt，效能更好

    //加入 Compose 專用的 Hilt 導覽支援 (這對導覽注入非常重要)
    implementation(libs.hilt.navigation.compose)

    implementation("androidx.datastore:datastore-preferences:1.2.1")


    //emoji pack
    val emoji2Version = "1.5.0" // 請根據你當前環境選擇最新穩定版
    implementation("androidx.emoji2:emoji2:$emoji2Version")
    implementation("androidx.emoji2:emoji2-bundled:$emoji2Version") // 自帶字型包

}