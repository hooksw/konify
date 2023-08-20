rootProject.name = "ui"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("multiplatform") version "1.8.21"

        kotlin("android") version "1.8.21"
        id("com.android.library") version "8.0.2"
        id("com.android.application") version "8.0.2"
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
        google()
        mavenCentral()
    }
}

include(":api")
include(":api:runtime")
include(":api:foundation")
include(":api:common")

include(":sample")
include(":sample:android")
