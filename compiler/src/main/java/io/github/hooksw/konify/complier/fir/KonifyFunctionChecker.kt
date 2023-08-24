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

package io.github.hooksw.konify.complier.fir

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.getSingleCompatibleExpectForActualOrNull
import org.jetbrains.kotlin.fir.declarations.utils.isAbstract
import org.jetbrains.kotlin.fir.declarations.utils.isOperator
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.declarations.utils.nameOrSpecialName
import org.jetbrains.kotlin.util.OperatorNameConventions

object KonifyFunctionChecker : FirFunctionChecker() {
    override fun check(
        declaration: FirFunction,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        val isKonify = declaration.hasKonifyAnnotation(context.session)

        // Check overrides for mismatched composable annotations
        for (override in declaration.getDirectOverriddenFunctions(context)) {
            if (override.isKonify(context.session) != isKonify) {
                reporter.reportOn(
                    declaration.source,
                    FirErrors.CONFLICTING_OVERLOADS,
                    listOf(declaration.symbol, override),
                    context
                )
            }

            // TODO(b/282135108): Check scheme of override against declaration
        }

        // Check that `actual` composable declarations have composable expects
        declaration.symbol.getSingleCompatibleExpectForActualOrNull()?.let { expectDeclaration ->
            if (expectDeclaration.hasKonifyAnnotation(context.session) != isKonify) {
                reporter.reportOn(
                    declaration.source,
                    KonifyErrors.MISMATCHED_COMPOSABLE_IN_EXPECT_ACTUAL,
                    context
                )
            }
        }

        if (!isKonify) return

        // Konify suspend functions are unsupported
        if (declaration.isSuspend) {
            reporter.reportOn(declaration.source, KonifyErrors.COMPOSABLE_SUSPEND_FUN, context)
        }

        // Check that there are no default arguments in abstract composable functions
        if (declaration.isAbstract) {
            for (valueParameter in declaration.valueParameters) {
                val defaultValue = valueParameter.defaultValue ?: continue
                reporter.reportOn(
                    defaultValue.source,
                    KonifyErrors.ABSTRACT_COMPOSABLE_DEFAULT_PARAMETER_VALUE,
                    context
                )
            }
        }

        // Konify main functions are not allowed.
        if (declaration.symbol.isMain(context.session)) {
            reporter.reportOn(declaration.source, KonifyErrors.COMPOSABLE_FUN_MAIN, context)
        }

        // Disallow composable setValue operators
        if (declaration.isOperator) {
            reporter.reportOn(declaration.source, KonifyErrors.COMPOSE_INVALID_DELEGATE, context)
        }
    }
}
