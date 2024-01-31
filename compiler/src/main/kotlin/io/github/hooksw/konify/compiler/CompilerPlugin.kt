package io.github.hooksw.konify.compiler

import io.github.hooksw.konify.compiler.conf.KEY_ENABLED
import io.github.hooksw.konify.compiler.conf.KEY_SkipSimpleFunction
import io.github.hooksw.konify.compiler.conf.KEY_SkipSuspendFunction
import io.github.hooksw.konify.compiler.fir.KonifyFirExtensionRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
public class KonifyComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (configuration[KEY_ENABLED] == false) return
        val skipSuspendFunction = configuration[KEY_SkipSuspendFunction] ?: false
        val skipSimpleFunction = configuration[KEY_SkipSimpleFunction] ?: false

        IrGenerationExtension.registerExtension(
            KonifyIrGenerationExtension(
                skipSuspendFunction,
                skipSimpleFunction
            )
        )

        FirExtensionRegistrarAdapter.registerExtension(
            KonifyFirExtensionRegistrar()
        )
    }
}