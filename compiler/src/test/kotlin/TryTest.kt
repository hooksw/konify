
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import kotlin.test.Test
import kotlin.test.assertEquals

class TryTest {
    @Test
    @OptIn(ExperimentalCompilerApi::class)
    fun `func expression`() {
        val ktSource = SourceFile.kotlin(
            "file.kt", """
    val f={}
        
    """.trimIndent()
        )
        val result = KotlinCompilation().apply {
            sources = listOf(ktSource)

            // pass your own instance of a compiler plugin
            compilerPluginRegistrars = listOf(FunExpReplacePluginRegistrar())

            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
        assertEquals(result.exitCode , KotlinCompilation.ExitCode.OK)
    }
}

@OptIn(ExperimentalCompilerApi::class)
class FunExpReplacePluginRegistrar() : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(
            object : IrGenerationExtension {
                override fun generate(
                    moduleFragment: IrModuleFragment,
                    pluginContext: IrPluginContext
                ) {
                    moduleFragment.transformChildrenVoid(object :
                        IrElementTransformerVoidWithContext() {
                        override fun visitFunctionExpression(expression: IrFunctionExpression): IrExpression {
                            expression.function.apply {
                                addValueParameter {
                                    name= Name.identifier("a")
                                    type=pluginContext.referenceClass(ClassId.fromString("kotlin.Int"))?.owner?.defaultType!!
                                }
                            }
                            return super.visitFunctionExpression(expression)
                        }
                    })
                }

            }
        )
    }

}