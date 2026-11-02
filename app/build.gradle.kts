plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "app.apartment.mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.apartment.mobile"
        minSdk = 33 // NFR-006: Android 13 or later
        targetSdk = 33 // as verified on the Owner device (ADR-0003)
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // The release key lives outside the repository (see DOC-OPS); the passwords are in the Gradle user properties.
    signingConfigs {
        create("release") {
            val storePath = providers.gradleProperty("APARTMENT_STORE_FILE").orNull
            if (storePath != null) {
                storeFile = file(storePath)
                storePassword = providers.gradleProperty("APARTMENT_STORE_PASSWORD").get()
                keyAlias = providers.gradleProperty("APARTMENT_KEY_ALIAS").get()
                keyPassword = providers.gradleProperty("APARTMENT_KEY_PASSWORD").get()
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (providers.gradleProperty("APARTMENT_STORE_FILE").orNull != null) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Room keeps a JSON copy of each schema version so migrations can be tested (ADR-0002).
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
