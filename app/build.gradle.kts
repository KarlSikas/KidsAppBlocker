plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Remove Firebase Crashlytics plugins
    // id("com.google.gms.google-services")
    // id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.kidsappblocker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kidsappblocker"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation ("org.slf4j:slf4j-api:1.7.32")
    implementation ("ch.qos.logback:logback-classic:1.2.10")

    // Remove Firebase Crashlytics dependencies
    // implementation("com.google.firebase:firebase-crashlytics:18.2.6")
    // implementation("com.google.firebase:firebase-analytics:21.3.0")
    // implementation("com.google.firebase:firebase-core:21.1.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Remove Firebase Crashlytics plugins
// apply(plugin = "com.google.gms.google-services")
// apply(plugin = "com.google.firebase.crashlytics")