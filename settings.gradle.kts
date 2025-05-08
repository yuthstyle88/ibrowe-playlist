pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.android.library") version "8.2.0"
        id("org.jetbrains.kotlin.android") version "1.9.22"
        id("com.google.devtools.ksp") version "1.9.22-1.0.17"
    }
}