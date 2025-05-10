plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.watchtest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.watchtest"
        minSdk = 29
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

    implementation ("com.google.android.gms:play-services-wearable:18.0.0")
    implementation ("androidx.wear:wear:1.2.0")
    implementation ("androidx.core:core-ktx:1.10.1")
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