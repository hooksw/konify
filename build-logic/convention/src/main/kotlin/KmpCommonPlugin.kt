import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion

class KmpCommonPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val kmpPlugin = libs.findPlugin("kotlin.multiplatform").get().get().pluginId
            pluginManager.apply(kmpPlugin)
            val androidLibsPlugin = libs.findPlugin("android.library").get().get().pluginId
            pluginManager.apply(androidLibsPlugin)
            configureKmp(kmpPlugin)
            project.pluginManager.withPlugin(androidLibsPlugin) {
                project.extensions.getByType(LibraryExtension::class.java).apply {
                    namespace = "io.github.hooksw.konify" + target.name
                    compileSdk = libs.findVersion("compileSdk").get().toString().toInt()
                    defaultConfig {
                        minSdk = libs.findVersion("minSdk").get().toString().toInt()
                    }
                    compileOptions {
                        sourceCompatibility = JavaVersion.VERSION_11
                        targetCompatibility = JavaVersion.VERSION_11

                    }
                }
            }
        }
    }

    private fun Project.configureKmp(kmpPlugin: String) {
        project.pluginManager.withPlugin(kmpPlugin) {
            project.extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
                androidTarget {
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
                applyDefaultHierarchyTemplate()

                sourceSets.apply {
                    getByName("commonMain") {
                        dependencies {
                            api(libs.findLibrary("kotlinx.coroutines.core").get())
                            api(libs.findLibrary("androidx.collection").get())
                        }
                    }
                    getByName("commonTest") {
                        dependencies {
                            implementation(libs.findLibrary("kotlin.test").get())
                        }
                    }

                    getByName("androidMain") {
                        dependencies {
                            api(libs.findLibrary("kotlinx.coroutines.android").get())
                            implementation(libs.findLibrary("androidx.core").get())
                        }
                    }
                    getByName("androidUnitTest") {
                        dependencies {
                            implementation(libs.findLibrary("mockk.android").get())
                            implementation(libs.findLibrary("mockk.agent").get())
                        }
                    }

                    getByName("jsMain") {
                        dependencies {
                            api(libs.findLibrary("kotlinx.html").get())
                        }
                    }
                    all {
                        languageSettings.optIn("io.github.hooksw.konify.runtime.annotation.InternalUse")
                    }
                }
            }
        }
    }
}