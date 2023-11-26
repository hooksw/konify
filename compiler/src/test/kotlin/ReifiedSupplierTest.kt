import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.hooksw.konify.compiler.fir.GenerateReifiedSupplier
import io.github.hooksw.konify.compiler.fir.KonifyFirExtensionRegistrar
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
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
        assertEquals(result.exitCode , KotlinCompilation.ExitCode.OK)
    }
    }
}