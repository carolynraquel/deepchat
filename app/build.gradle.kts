plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
}
//Oh,lawd baby, what can i say. this gradle config been a pain in my watusi.

android {
    namespace = "com.example.deepchat" // package name
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.deepchat" // package name
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0" //my first official version OMG

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // Define your signing configuration
    signingConfigs {
        create("release") { //define signing config with create keyword
            // secure keystore
            storeFile = file("C:/Users/Carolyn/keystores/deepchat-release.jks")
            storePassword = "300gabe@R!"
            keyAlias = "deepchat-key"
            keyPassword = "300gabe@R!"
        }
    }
    buildTypes {
         getByName("release") {
            isMinifyEnabled = true // Enable ProGuard for release builds
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"  // Use explicit version instead
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.material3.android)

    // Firebase (if needed)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // Credentials (if needed)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // ElevenLabs API
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Google Sign-In. This line is essential.
    implementation(libs.play.services.auth)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Google Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // WebSocket for ElevenLabs
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okio:okio:3.0.0")  // or the latest version

    // For audio handling
    implementation("androidx.media3:media3-exoplayer:1.6.1")
    implementation("androidx.media3:media3-ui:1.6.1")
}

