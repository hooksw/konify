rootProject.name = "konify"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
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

include(":compiler")

include(":sample")
include(":sample:android")
