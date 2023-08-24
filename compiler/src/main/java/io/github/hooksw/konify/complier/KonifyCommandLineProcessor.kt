package io.github.hooksw.konify.complier

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CommandLineProcessor::class)
class KonifyCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "ui"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption("enable", "<true|false>", "whether the plugin is enabled"),

    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        when(option.optionName){
            "enable"->configuration.put(KEY_ENABLED,value.toBoolean())
            else-> error("Unexpected config option ${option.optionName}")
        }
    }
}