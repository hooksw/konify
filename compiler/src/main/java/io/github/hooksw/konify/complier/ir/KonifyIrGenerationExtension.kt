/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hooksw.konify.complier.ir

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.platform.jvm.isJvm

class KonifyIrGenerationExtension(
    private val validateIr: Boolean = false,
    private val useK2: Boolean = false,
) : IrGenerationExtension {

    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        val isKlibTarget = !pluginContext.platform.isJvm()

        // Input check.  This should always pass, else something is horribly wrong upstream.
        // Necessary because oftentimes the issue is upstream (compiler bug, prior plugin, etc)
        if (validateIr)
            validateIr(moduleFragment, pluginContext.irBuiltIns)


        if (useK2) {
            moduleFragment.acceptVoid(KonifyLambdaAnnotator(pluginContext))
        }

        moduleFragment.transform(KonifyrIrVisitor(pluginContext),null)

//        if (!useK2) {
//            CopyDefaultValuesFromExpectLowering(pluginContext).lower(moduleFragment)
//        }



        // Verify that our transformations didn't break something
        if (validateIr)
            validateIr(moduleFragment, pluginContext.irBuiltIns)
    }
}
