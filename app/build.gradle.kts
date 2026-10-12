import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val branding = Properties().apply { file("../branding.properties").inputStream().use { load(it) } }

android {
    namespace = "com.focusgate.app"
    compileSdk = 37
    defaultConfig {
        applicationId = branding.getProperty("applicationId")
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        resValue("string", "app_name", branding.getProperty("displayName"))
    }
    buildFeatures {
        compose = true
        resValues = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":feature:blocker"))
    implementation(project(":feature:overlay"))
    implementation(project(":service:detection"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
