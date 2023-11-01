package io.github.hooksw.konify.compiler.fir

import io.github.hooksw.konify.compiler.conf.KonifyAnnotations
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
    override fun check(declaration: FirFunction, context: CheckerContext, reporter: DiagnosticReporter) {
        val isKonify = declaration.hasAnnotation(KonifyAnnotations.Component,context.session)

        // Check overrides for mismatched Konify annotations
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

        // Check that `actual` Konify declarations have Konify expects
        declaration.symbol.getSingleCompatibleExpectForActualOrNull()?.let { expectDeclaration ->
            if (expectDeclaration.hasAnnotation(KonifyAnnotations.Component,context.session) != isKonify) {
                reporter.reportOn(
                    declaration.source,
                    KonifyErrors.MISMATCHED_Konify_IN_EXPECT_ACTUAL,
                    context
                )
            }
        }
        if (!isKonify) return

        // Konify suspend functions are unsupported
        if (declaration.isSuspend) {
            reporter.reportOn(declaration.source, KonifyErrors.Konify_SUSPEND_FUN, context)
        }

        // Check that there are no default arguments in abstract Konify functions
        if (declaration.isAbstract) {
            for (valueParameter in declaration.valueParameters) {
                val defaultValue = valueParameter.defaultValue ?: continue
                reporter.reportOn(
                    defaultValue.source,
                    KonifyErrors.ABSTRACT_Konify_DEFAULT_PARAMETER_VALUE,
                    context
                )
            }
        }

        // Konify main functions are not allowed.
        if (declaration.symbol.isMain(context.session)) {
            reporter.reportOn(declaration.source, KonifyErrors.Konify_FUN_MAIN, context)
        }

        // Disallow Konify setValue operators
        if (declaration.isOperator &&
            declaration.nameOrSpecialName == OperatorNameConventions.SET_VALUE
        ) {
            reporter.reportOn(declaration.source, KonifyErrors.Konify_INVALID_DELEGATE, context)
        }
    }

}
