@Suppress("DSL_SCOPE_VIOLATION") // for using libs alias in plugins if using Gradle version catalog for plugins
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.pushupcounter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pushupcounter"
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // match compose_compiler version from catalog (hard-coded here)
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val libs = extensions.getByType<org.gradle.accessors.dm.LibrariesForLibs>()
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // CameraX
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // ML Kit pose detection (accurate)
    implementation(libs.mlkit.pose.accurate)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Optional
    implementation(libs.material)
}