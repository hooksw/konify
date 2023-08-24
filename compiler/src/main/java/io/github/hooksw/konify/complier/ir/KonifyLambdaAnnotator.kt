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

import io.github.hooksw.konify.complier.KonifyClassIds
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
import org.jetbrains.kotlin.name.FqName

/**
 * In K1, the frontend used to annotate inferred Konify lambdas with `@Konify`.
 * The K2 frontend instead uses a different type for Konify lambdas. This pass adds
 * the annotation, since the backend expects it.
 */
class KonifyLambdaAnnotator(context: IrPluginContext) : IrElementVisitorVoid {
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

    private val KonifySymbol = context.referenceClass(KonifyClassIds.View)!!

    private fun IrFunction.mark() {
        if (!hasAnnotation(FqName("io.github.hooksw.konify.runtime.annotation.View"))) {
            annotations = annotations + IrConstructorCallImpl.fromSymbolOwner(
                KonifySymbol.owner.defaultType,
                KonifySymbol.constructors.single(),
            )
        }
    }
}
fun IrType.isSyntheticKonifyFunction() =
    classOrNull?.owner?.let {
        it.name.asString().startsWith("ComposableFunction") &&
                it.packageFqName == FqName("io.github.hooksw.konify.runtime.annotation")
    } ?: false
