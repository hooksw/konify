plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.0.0-Beta2"
    id("com.android.library")
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.multiplatform")
        plugin("org.jetbrains.kotlin.plugin.serialization")
        plugin("com.android.library")
    }
}

allprojects {

    kotlin {
        androidTarget{
            compilations.all {
                kotlinOptions {
                    jvmTarget = "11"
                    freeCompilerArgs += "-Xjvm-default=all"
                }
            }
        }

//        listOf(
//            iosX64(),
//            iosArm64(),
//            iosSimulatorArm64()
//        ).forEach { target ->
//            target.binaries.framework {
//                baseName = "api"
//            }
//        }

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
            getByName("commonMain") {
                dependencies {
                    implementation( "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                    api("androidx.collection:collection:1.4.0-beta02")
                }
            }
            getByName("commonTest") {
                dependencies {
                    implementation(kotlin("test"))
                }
            }

            getByName("androidMain") {
                dependencies {
                    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

                    implementation("androidx.core:core-ktx:1.12.0")
                }
            }
            getByName("androidUnitTest") {
                dependencies {
                    val mockkVersion="1.13.8"
                    implementation( "io.mockk:mockk-android:${mockkVersion}")
                    implementation( "io.mockk:mockk-agent:${mockkVersion}")
                }
            }

            getByName("jsMain") {
                dependencies {
                    api("org.jetbrains.kotlinx:kotlinx-html-js:0.9.0")
                }
            }
            all {
                languageSettings.optIn("io.github.hooksw.konify.runtime.annotation.InternalUse")
            }
        }
    }

    android {
        namespace = "com.example.ui.api"
        compileSdk = 32
        defaultConfig {
            minSdk = 26
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11

        }
    }
}
dependencies {
    api(project(":api:runtime"))
    api(project(":api:foundation"))
    api(project(":api:ui-view"))
}
