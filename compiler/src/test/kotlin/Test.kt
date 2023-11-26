
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.hooksw.konify.compiler.KonifyComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertEquals

class Test {
    @Compo
    @Test
    @OptIn(ExperimentalCompilerApi::class)
    fun test() {
        val ktSource = SourceFile.kotlin(
            "file.kt", """
                
    val f={}
        
    """.trimIndent()
        )
        val result = KotlinCompilation().apply {
            sources = listOf(ktSource)
            languageVersion="2.0"
            // pass your own instance of a compiler plugin
            compilerPluginRegistrars = listOf(KonifyComponentRegistrar())
            multiplatform=true
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
        assertEquals(result.exitCode , KotlinCompilation.ExitCode.OK)
    }
}
