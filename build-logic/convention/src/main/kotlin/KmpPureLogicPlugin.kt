import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpPureLogicPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val kmpPlugin = libs.findPlugin("kotlin.multiplatform").get().get().pluginId
            pluginManager.apply(kmpPlugin)
            configureKmp(kmpPlugin)
        }
    }

    private fun Project.configureKmp(kmpPlugin: String) {
        project.pluginManager.withPlugin(kmpPlugin) {
            project.extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
                js {
                    browser()
                }
                jvm()
                sourceSets.apply {
                    commonTest.dependencies {
                        implementation(libs.findLibrary("kotlin.test").get())
                    }
                }
            }
        }
    }
}