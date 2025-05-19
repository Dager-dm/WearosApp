plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.watch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.watch"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

    }
    signingConfigs {
        create("release") {
            storeFile = file("../kys.jks") // Ruta relativa al keystore
            storePassword = "123456" // Reemplaza con tu contraseña del keystore
            keyAlias =  "kysalias" // El alias que usaste al crear la clave
            keyPassword = "123456" // Reemplaza con la contraseña de tu clave
        }
    }
    buildTypes {
        release {
            //signingConfig signingConfigs.release
            signingConfig = signingConfigs.getByName("release")
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

    implementation("com.google.android.gms:play-services-wearable:19.0.0")
    implementation ("androidx.wear:wear:1.2.0")
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("androidx.health:health-services-client:1.1.0-alpha05") // O la última versión estable/alfa
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.7.3") // Para await() con ListenableFuture, o usa kotlinx-coroutines-play-services si tus Tasks son de Play Services
    implementation("androidx.activity:activity-ktx:1.8.0") // Para registerForActivityResult
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.wear.compose:compose-material:1.4.0-beta03")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.compose.material:material-icons-core:1.7.0")
    implementation (files("libs/samsung-health-sensor-api-v1.3.0.aar"))
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.tiles)
    implementation(libs.tiles.material)
    implementation(libs.tiles.tooling.preview)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.tiles)
    implementation(libs.watchface.complications.data.source.ktx)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    debugImplementation(libs.tiles.tooling)
}