package io.github.hooksw.konify.compiler

import io.github.hooksw.konify.compiler.conf.KEY_ENABLED
import io.github.hooksw.konify.compiler.conf.KEY_SkipSimpleFunction
import io.github.hooksw.konify.compiler.conf.KEY_SkipSuspendFunction
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
public class RedactedCommandLineProcessor : CommandLineProcessor {

  internal companion object {
    val OPTION_ENABLED =
      CliOption(
        optionName = "enabled",
        valueDescription = "<true | false>",
        description = KEY_ENABLED.toString(),
        required = true,
        allowMultipleOccurrences = false
      )
    val SkipSuspend =
      CliOption(
        optionName = "skipSuspendFunction",
        valueDescription = "<true | false>",
        description = KEY_SkipSuspendFunction.toString(),
        required = false,
        allowMultipleOccurrences = false
      )
    val SkipSimpleFunction =
      CliOption(
        optionName = "skipSimpleFunction",
        valueDescription = "<true | false>",
        description = KEY_SkipSimpleFunction.toString(),
        required = false,
        allowMultipleOccurrences = false
      )

  }

  override val pluginId: String = "io.github.hooksw.konify"

  override val pluginOptions: Collection<AbstractCliOption> =
    listOf(OPTION_ENABLED, SkipSuspend, SkipSimpleFunction)

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration
  ): Unit =
    when (option.optionName) {
      "enabled" -> configuration.put(KEY_ENABLED, value.toBoolean())
      "skipSuspendFunction" -> configuration.put(KEY_SkipSuspendFunction, value.toBoolean())
      "skipSimpleFunction" -> configuration.put(KEY_SkipSimpleFunction, value.toBoolean())
      else -> error("Unknown plugin option: ${option.optionName}")
    }
}