rootProject.name = "konify"

pluginManagement {
    repositories {
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public/")
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven ("https://maven.aliyun.com/repository/public/")
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
        google()
    }
}

include(":api")
include(":api:runtime")
include(":api:foundation")
include(":api:ui-view")


include(":sample")
include(":sample:android")
include(":compiler")
