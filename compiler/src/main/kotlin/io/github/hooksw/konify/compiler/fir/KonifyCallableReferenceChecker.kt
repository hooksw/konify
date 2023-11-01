package io.github.hooksw.konify.compiler.fir

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirCallableReferenceAccessChecker
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.functionTypeKind

object KonifyCallableReferenceChecker : FirCallableReferenceAccessChecker() {
    override fun check(expression: FirCallableReferenceAccess, context: CheckerContext, reporter: DiagnosticReporter) {

        // The type of a function reference depends on the context where it is used.
        // We could allow non-reflective Konify function references, but this would be fragile
        // and depend on details of the frontend resolution.
        val kind = expression.typeRef.coneType.functionTypeKind(context.session)
        if (kind == KonifyFunction || kind == KKonifyFunction) {
            reporter.reportOn(
                expression.source,
                KonifyErrors.Konify_FUNCTION_REFERENCE,
                context
            )
        }
    }

}
