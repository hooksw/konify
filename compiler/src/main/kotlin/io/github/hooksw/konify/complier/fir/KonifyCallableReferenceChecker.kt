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
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirCallableReferenceAccessChecker
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.functionTypeKind

object KonifyCallableReferenceChecker : FirCallableReferenceAccessChecker() {
    override fun check(
        expression: FirCallableReferenceAccess,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        val kind = expression.typeRef.coneType.functionTypeKind(context.session)
        if (kind == KonifyFunction || kind == KKonifyFunction) {
            reporter.reportOn(
                source = expression.source,
                factory = ComposableErrors.COMPOSABLE_FUNCTION_REFERENCE,
                context = context
            )
        }
    }
}
