rootProject.name = "konify"

pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        maven ("https://maven.aliyun.com/repository/public/")
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
        google()
    }
}

include(":api:runtime")
include(":api:foundation")
include(":api:foundation:layout")
include(":api:ui-view")
include(":layout-compute")

include(":compiler")

include(":sample:android")
include(":api:foundation:layout")
