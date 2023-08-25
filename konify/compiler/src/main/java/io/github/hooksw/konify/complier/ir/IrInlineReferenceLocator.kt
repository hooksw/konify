/*
 * Copyright 2021 The Android Open Source Project
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
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

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.util.isLambda

fun IrExpression.unwrapLambda(): IrFunctionSymbol? {
    fun IrStatement.toFunctionReferenceOrNull(): IrFunctionReference? {
        return this as? IrFunctionReference
    }
    return when {
        this is IrBlock && origin.isLambdaBlockOrigin -> {
            statements.lastOrNull()?.toFunctionReferenceOrNull()?.symbol
        }
        this is IrFunctionExpression -> function.symbol
        else -> null
    }
}

private val IrStatementOrigin?.isLambdaBlockOrigin: Boolean
    get() = isLambda ||
            this == IrStatementOrigin.ADAPTED_FUNCTION_REFERENCE ||
            this == IrStatementOrigin.SUSPEND_CONVERSION
