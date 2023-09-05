/*
 * Copyright 2023 The Android Open Source Project
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

import io.github.hooksw.konify.complier.KonifyIds
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

class KonifyLambdaAnnotator(context: IrPluginContext) : IrElementVisitorVoid {
    private val konifySymbol = context.referenceClass(KonifyIds.ViewClassId)!!

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitFunctionExpression(expression: IrFunctionExpression) {
        if (expression.type.isSyntheticKonifyFunction()) {
            expression.function.mark()
        }
        super.visitFunctionExpression(expression)
    }

    override fun visitFunctionReference(expression: IrFunctionReference) {
        if (expression.type.isSyntheticKonifyFunction()) {
            expression.symbol.owner.mark()
        }
        super.visitFunctionReference(expression)
    }

    private fun IrFunction.mark() {
        if (!hasAnnotation(KonifyIds.ViewFqName)) {
            annotations += IrConstructorCallImpl.fromSymbolOwner(
                type = konifySymbol.owner.defaultType,
                constructorSymbol = konifySymbol.constructors.single(),
            )
        }
    }
}

private fun IrType.isSyntheticKonifyFunction(): Boolean {
    val ownerClass = classOrNull?.owner ?: return false
    return ownerClass.name.asString().startsWith("ViewFunction") &&
            ownerClass.packageFqName == KonifyIds.AnnotationPackageFqName
}
