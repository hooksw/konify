rootProject.name = "konify"

pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public/")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven ("https://maven.aliyun.com/repository/public/")
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
        google()
        mavenCentral()
    }
}

include(":api")
include(":api:runtime")
include(":api:foundation")
include(":api:ui")


include(":sample")
include(":sample:android")
