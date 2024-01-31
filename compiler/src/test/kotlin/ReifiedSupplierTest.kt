import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.hooksw.konify.compiler.fir.GenerateReifiedSupplier
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ReifiedSupplierTest {
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun test() {
        val ktSource = SourceFile.kotlin(
            "file.kt", """
    val f={}
        
    """.trimIndent()
        )
        val result = KotlinCompilation().apply {
            sources = listOf(ktSource)

            // pass your own instance of a compiler plugin
            compilerPluginRegistrars = listOf(
                @OptIn(ExperimentalCompilerApi::class)
                object : CompilerPluginRegistrar() {
                    override val supportsK2: Boolean
                        get() = true

                    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
                        FirExtensionRegistrarAdapter.registerExtension(
                            object : FirExtensionRegistrar() {
                                override fun ExtensionRegistrarContext.configurePlugin() {
                                    +::GenerateReifiedSupplier
                                }
                            }
                        )
                    }

                })

            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)

    }
}