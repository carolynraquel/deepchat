// Top-level build file (project/build.gradle.kts)
buildscript {
    repositories {
        google()  // Must be first
        mavenCentral()
        maven {
            url = uri("https://company/com/maven2")
        }
        mavenLocal()
        flatDir {
            dirs("libs")
        }
        gradlePluginPortal()
    }
    dependencies {
        // Use explicit versions instead of Version Catalog references
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.android.tools.build:gradle:8.1.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    }
}

// The plugins block can use the Version Catalog
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.services) apply false
}

tasks.register("myTask") {
    val myBuildDir = layout.buildDirectory.get()
    println("My build dir is ${myBuildDir.asFile}")
}