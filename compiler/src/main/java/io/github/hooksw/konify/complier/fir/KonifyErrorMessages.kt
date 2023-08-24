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

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers

object KonifyErrorMessages : BaseDiagnosticRendererFactory() {
    override val MAP = KtDiagnosticFactoryToRendererMap("Konify").also { map ->
        map.put(
            KonifyErrors.COMPOSABLE_INVOCATION,
            "@Konify invocations can only happen from the context of a @Konify function"
        )

        map.put(
            KonifyErrors.COMPOSABLE_EXPECTED,
            "Functions which invoke @Konify functions must be marked with the @Konify " +
                "annotation"
        )

        map.put(
            KonifyErrors.NONREADONLY_CALL_IN_READONLY_COMPOSABLE,
            "Konifys marked with @ReadOnlyKonify can only call other @ReadOnlyKonify " +
                "composables"
        )

        map.put(
            KonifyErrors.CAPTURED_COMPOSABLE_INVOCATION,
            "Konify calls are not allowed inside the {0} parameter of {1}",
            FirDiagnosticRenderers.VARIABLE_NAME,
            FirDiagnosticRenderers.DECLARATION_NAME
        )

        map.put(
            KonifyErrors.ILLEGAL_TRY_CATCH_AROUND_COMPOSABLE,
            "Try catch is not supported around composable function invocations."
        )

        map.put(
            KonifyErrors.MISSING_DISALLOW_COMPOSABLE_CALLS_ANNOTATION,
            "Parameter {0} cannot be inlined inside of lambda argument {1} of {2} " +
                "without also being annotated with @DisallowKonifyCalls",
            FirDiagnosticRenderers.VARIABLE_NAME,
            FirDiagnosticRenderers.VARIABLE_NAME,
            FirDiagnosticRenderers.DECLARATION_NAME,
        )

        map.put(
            KonifyErrors.ABSTRACT_COMPOSABLE_DEFAULT_PARAMETER_VALUE,
            "Abstract Konify functions cannot have parameters with default values"
        )

        map.put(
            KonifyErrors.COMPOSABLE_SUSPEND_FUN,
            "Konify function cannot be annotated as suspend"
        )

        map.put(
            KonifyErrors.COMPOSABLE_FUN_MAIN,
            "Konify main functions are not currently supported"
        )

        map.put(
            KonifyErrors.COMPOSABLE_FUNCTION_REFERENCE,
            "Function References of @Konify functions are not currently supported"
        )

        map.put(
            KonifyErrors.COMPOSABLE_PROPERTY_BACKING_FIELD,
            "Konify properties are not able to have backing fields"
        )

        map.put(
            KonifyErrors.COMPOSABLE_VAR,
            "Konify properties are not able to have backing fields"
        )

        map.put(
            KonifyErrors.COMPOSE_INVALID_DELEGATE,
            "Konify setValue operator is not currently supported."
        )

        map.put(
            KonifyErrors.MISMATCHED_COMPOSABLE_IN_EXPECT_ACTUAL,
            "Mismatched @Konify annotation between expect and actual declaration"
        )
    }
}
