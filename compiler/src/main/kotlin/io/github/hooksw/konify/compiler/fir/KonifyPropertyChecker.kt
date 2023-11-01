package io.github.hooksw.konify.compiler.fir

import io.github.hooksw.konify.compiler.conf.KonifyAnnotations
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.utils.hasBackingField

object KonifyPropertyChecker : FirPropertyChecker() {
    override fun check(declaration: FirProperty, context: CheckerContext, reporter: DiagnosticReporter) {
        // `@Konify` is only applicable to property getters, but in K1 we were also checking
        // properties with the annotation on the setter.
        if (declaration.getter?.hasAnnotation(KonifyAnnotations.Component,context.session) != true &&
            declaration.setter?.hasAnnotation(KonifyAnnotations.Component,context.session) != true) {
            return
        }

        if (declaration.isVar) {
            reporter.reportOn(declaration.source, KonifyErrors.Konify_VAR, context)
        }

        if (declaration.hasBackingField) {
            reporter.reportOn(
                declaration.source,
                KonifyErrors.Konify_PROPERTY_BACKING_FIELD,
                context
            )
        }
    }

}
