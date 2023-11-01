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

package io.github.hooksw.konify.compiler.fir

import org.jetbrains.kotlin.com.intellij.lang.LighterASTNode
import org.jetbrains.kotlin.com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.kotlin.diagnostics.LightTreePositioningStrategies
import org.jetbrains.kotlin.diagnostics.LightTreePositioningStrategy
import org.jetbrains.kotlin.diagnostics.PositioningStrategies
import org.jetbrains.kotlin.diagnostics.PositioningStrategy
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategy
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.error3
import org.jetbrains.kotlin.diagnostics.findChildByType
import org.jetbrains.kotlin.diagnostics.markElement
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtTryExpression

object KonifyErrors {
    // error goes on the Konify call in a non-Konify function
    val Konify_INVOCATION by error0<PsiElement>()

    // error goes on the non-Konify function with Konify calls
    val Konify_EXPECTED by error0<PsiElement>(
        SourceElementPositioningStrategies.DECLARATION_NAME
    )

    val NONREADONLY_CALL_IN_READONLY_Konify by error0<PsiElement>()

    val CAPTURED_Konify_INVOCATION by
        error2<PsiElement, FirVariableSymbol<*>, FirCallableSymbol<*>>()

    // Konify calls are not allowed in try expressions
    // error goes on the `try` keyword
    val ILLEGAL_TRY_CATCH_AROUND_Konify by error0<KtTryExpression>(
        KonifySourceElementPositioningStrategies.TRY_KEYWORD
    )

    val MISSING_DISALLOW_Konify_CALLS_ANNOTATION by error3<
        PsiElement,
        FirValueParameterSymbol, // unmarked
        FirValueParameterSymbol, // marked
        FirCallableSymbol<*>>()

    val ABSTRACT_Konify_DEFAULT_PARAMETER_VALUE by error0<PsiElement>()

    val Konify_SUSPEND_FUN by error0<PsiElement>(
        SourceElementPositioningStrategies.DECLARATION_NAME
    )

    val Konify_FUN_MAIN by error0<PsiElement>(
        SourceElementPositioningStrategies.DECLARATION_NAME
    )

    val Konify_FUNCTION_REFERENCE by error0<PsiElement>()

    val Konify_PROPERTY_BACKING_FIELD by error0<PsiElement>(
        SourceElementPositioningStrategies.DECLARATION_NAME
    )

    val Konify_VAR by error0<PsiElement>(SourceElementPositioningStrategies.DECLARATION_NAME)

    val Konify_INVALID_DELEGATE by error0<PsiElement>(
        KonifySourceElementPositioningStrategies.DECLARATION_NAME_OR_DEFAULT
    )

    val MISMATCHED_Konify_IN_EXPECT_ACTUAL by error0<PsiElement>(
        SourceElementPositioningStrategies.DECLARATION_NAME
    )

    init {
        RootDiagnosticRendererFactory.registerFactory(KonifyErrorMessages)
    }
}

object KonifySourceElementPositioningStrategies {
    private val PSI_TRY_KEYWORD: PositioningStrategy<KtTryExpression> =
        object : PositioningStrategy<KtTryExpression>() {
            override fun mark(element: KtTryExpression): List<TextRange> {
                element.tryKeyword?.let {
                    return markElement(it)
                }
                return PositioningStrategies.DEFAULT.mark(element)
            }
    }

    private val LIGHT_TREE_TRY_KEYWORD: LightTreePositioningStrategy =
        object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            val target = tree.findChildByType(node, KtTokens.TRY_KEYWORD) ?: node
            return markElement(target, startOffset, endOffset, tree, node)
        }
    }

    private val PSI_DECLARATION_NAME_OR_DEFAULT: PositioningStrategy<PsiElement> =
        object : PositioningStrategy<PsiElement>() {
            override fun mark(element: PsiElement): List<TextRange> {
                if (element is KtNamedDeclaration) {
                    return PositioningStrategies.DECLARATION_NAME.mark(element)
                }
                return PositioningStrategies.DEFAULT.mark(element)
            }
        }

    val TRY_KEYWORD = SourceElementPositioningStrategy(
        LIGHT_TREE_TRY_KEYWORD,
        PSI_TRY_KEYWORD
    )

    val DECLARATION_NAME_OR_DEFAULT = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DECLARATION_NAME,
        PSI_DECLARATION_NAME_OR_DEFAULT
    )
}
