plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "spike.alarmtest"
    compileSdk = 34

    defaultConfig {
        applicationId = "spike.alarmtest"
        minSdk = 33
        // Targets 33 on purpose: the case the Android docs describe for SCHEDULE_EXACT_ALARM.
        targetSdk = 33
        versionCode = 1
        versionName = "0.1-throwaway"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
