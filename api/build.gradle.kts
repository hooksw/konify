plugins {
    kotlin("multiplatform")

    id("com.android.library")
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.multiplatform")

        plugin("com.android.library")
    }
}

allprojects {
    kotlin {
        androidTarget {
            compilations.all {
                kotlinOptions {
                    jvmTarget = "1.8"
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
                    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
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

                    implementation("androidx.core:core-ktx:1.10.1")
                }
            }

            getByName("jsMain") {
                dependencies {
                    api("org.jetbrains.kotlinx:kotlinx-html-js:0.9.0")
                }
            }
        }
    }

    android {
        namespace = "com.example.ui.api"
        compileSdk = 32

        defaultConfig {
            minSdk = 26
        }
    }
}

dependencies {
    api(project(":api:runtime"))
    api(project(":api:foundation"))
    api(project(":api:ui"))
}
