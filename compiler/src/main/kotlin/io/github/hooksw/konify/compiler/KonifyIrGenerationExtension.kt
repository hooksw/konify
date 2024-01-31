package io.github.hooksw.konify.compiler

import io.github.hooksw.konify.compiler.ir.validateIr
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.platform.jvm.isJvm

class KonifyIrGenerationExtension(
    private val skipSuspendFunction: Boolean = false,
    private val skipSimpleFunction: Boolean = false,
    private val validateIr: Boolean = false,
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        val isKlibTarget = !pluginContext.platform.isJvm()
//        VersionChecker(pluginContext).check()


        // Input check.  This should always pass, else something is horribly wrong upstream.
        // Necessary because oftentimes the issue is upstream (compiler bug, prior plugin, etc)
        if (validateIr)
            validateIr(moduleFragment, pluginContext.irBuiltIns)


        // Verify that our transformations didn't break something
        if (validateIr)
            validateIr(moduleFragment, pluginContext.irBuiltIns)
    }

}
