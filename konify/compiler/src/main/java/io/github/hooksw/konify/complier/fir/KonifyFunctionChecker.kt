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

object KonifyFunctionChecker : FirFunctionChecker() {
    override fun check(
        declaration: FirFunction,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        val isKonify = declaration.hasKonifyAnnotation(context.session)
        for (override in declaration.getDirectOverriddenFunctions(context)) {
            if (override.isKonify(context.session) != isKonify) {
                reporter.reportOn(
                    source = declaration.source,
                    factory = FirErrors.CONFLICTING_OVERLOADS,
                    a = listOf(declaration.symbol, override),
                    context = context
                )
            }
        }
        val expectDeclaration = declaration.symbol.getSingleCompatibleExpectForActualOrNull()
        if (expectDeclaration != null && expectDeclaration.hasKonifyAnnotation(context.session) != isKonify) {
            reporter.reportOn(
                source = declaration.source,
                factory = ComposableErrors.MISMATCHED_COMPOSABLE_IN_EXPECT_ACTUAL,
                context = context
            )
        }
        if (!isKonify) {
            return
        }
        if (declaration.isSuspend) {
            reporter.reportOn(
                source = declaration.source,
                factory = ComposableErrors.COMPOSABLE_SUSPEND_FUN,
                context = context
            )
        }
        if (declaration.isAbstract) {
            for (valueParameter in declaration.valueParameters) {
                val defaultValue = valueParameter.defaultValue ?: continue
                reporter.reportOn(
                    source = defaultValue.source,
                    factory = ComposableErrors.ABSTRACT_COMPOSABLE_DEFAULT_PARAMETER_VALUE,
                    context = context
                )
            }
        }
        if (declaration.symbol.isMain(context.session)) {
            reporter.reportOn(
                source = declaration.source,
                factory = ComposableErrors.COMPOSABLE_FUN_MAIN,
                context = context
            )
        }
        if (declaration.isOperator) {
            reporter.reportOn(
                source = declaration.source,
                factory = ComposableErrors.COMPOSE_INVALID_DELEGATE,
                context = context
            )
        }
    }
}
