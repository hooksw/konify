/*
 * Copyright 2022 The Android Open Source Project
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

import org.jetbrains.kotlin.backend.common.CheckIrElementVisitor
import org.jetbrains.kotlin.backend.common.IrValidatorConfig
import org.jetbrains.kotlin.backend.common.ScopeValidator
import org.jetbrains.kotlin.backend.common.checkDeclarationParents
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

fun validateIr(
    fragment: IrModuleFragment,
    irBuiltIns: IrBuiltIns
) {
    val validatorConfig = IrValidatorConfig(
        abortOnError = true,
        ensureAllNodesAreDifferent = true,
        checkTypes = false, // This should be enabled.
        checkDescriptors = false,
        checkProperties = true,
        checkScopes = false
    )
    val validator = IrValidator(irBuiltIns, validatorConfig)
    fragment.accept(validator, data = null)
    fragment.checkDeclarationParents()
}

class IrValidator(
    irBuiltIns: IrBuiltIns,
    private val config: IrValidatorConfig
) : IrElementVisitorVoid {
    private var currentFile: IrFile? = null

    private val elementChecker = CheckIrElementVisitor(irBuiltIns, this::error, config)

    override fun visitFile(declaration: IrFile) {
        currentFile = declaration
        super.visitFile(declaration)
        if (config.checkScopes) {
            ScopeValidator(this::error).check(declaration)
        }
    }

    override fun visitElement(element: IrElement) {
        element.acceptVoid(elementChecker)
        element.acceptChildrenVoid(this)
    }

    private fun error(
        element: IrElement,
        message: String
    ) {
        val currentFileName = currentFile?.name ?: "???"
        val errorMessage = "Validation error ($message) for ${element.dumpSrc()}... " +
                "${element.render()} in $currentFileName."
        throw Error(errorMessage)
    }
}
