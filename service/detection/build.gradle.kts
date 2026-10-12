plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.focusgate.servicedetection"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
}

dependencies {
    implementation(project(":core:domain"))
}
