// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.protobuf") version "0.9.4" apply false
    id ("io.realm.kotlin") version "1.16.0" apply false
}


buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${libs.versions.agp.get()}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
        classpath("com.google.protobuf:protobuf-gradle-plugin:${libs.versions.protobuf.get()}")

    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
