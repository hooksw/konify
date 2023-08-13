plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        dependencies{
            implementation("androidx.core:core-ktx:1.10.1")
            implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha10")
        }
    }

//    listOf(
//        iosX64(),
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach {
//        it.binaries.framework {
//            baseName = "shared"
//        }
//    }

    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                    mode.set("inline")
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting{
            dependencies{
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.2")
            }
        }
        val jsMain by getting{
            dependencies{
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.9.0")
            }
        }
        val jsTest by getting
    }
}

android {
    namespace = "com.example.ui"
    compileSdk = 33
    defaultConfig {
        minSdk = 24
    }
}
dependencies {
    implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.8")
}
