buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        // Remove Firebase Crashlytics dependencies
        // classpath("com.google.gms:google-services:4.3.15")
        // classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.4")
    }
}