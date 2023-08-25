package io.github.hooksw.konify.complier

import com.google.auto.service.AutoService
import io.github.hooksw.konify.complier.fir.KonifyFirExtensionRegistrar
import io.github.hooksw.konify.complier.ir.KonifyIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class KonifyCompilerRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (configuration[KEY_ENABLED] == false) {
            return
        }
        FirExtensionRegistrarAdapter.registerExtension(KonifyFirExtensionRegistrar())
        IrGenerationExtension.registerExtension(KonifyIrGenerationExtension())
    }
}
