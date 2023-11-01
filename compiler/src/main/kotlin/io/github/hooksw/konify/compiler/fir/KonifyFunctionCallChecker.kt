package io.github.hooksw.konify.compiler.fir

import io.github.hooksw.konify.compiler.conf.KonifyAnnotations
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirPropertyAccessExpressionChecker
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.references.toResolvedValueParameterSymbol
import org.jetbrains.kotlin.fir.resolve.isInvoke
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.functionTypeKind

object KonifyFunctionCallChecker : FirFunctionCallChecker() {
    override fun check(expression: FirFunctionCall, context: CheckerContext, reporter: DiagnosticReporter) {

        val calleeFunction = expression.calleeReference.toResolvedCallableSymbol()
            ?: return

        // K2 propagates annotation from the fun interface method to the constructor.
        // https://youtrack.jetbrains.com/issue/KT-47708.
        if (calleeFunction.origin == FirDeclarationOrigin.SamConstructor) return

        if (calleeFunction.isKonify(context.session)) {
            checkKonifyCall(expression, calleeFunction, context, reporter)
        } else if (calleeFunction.callableId.isInvoke()) {
            checkInvoke(expression, context, reporter)
        }
    }

}

object KonifyPropertyAccessExpressionChecker : FirPropertyAccessExpressionChecker() {
    override fun check(expression: FirPropertyAccessExpression, context: CheckerContext, reporter: DiagnosticReporter) {

        val calleeFunction = expression.calleeReference.toResolvedCallableSymbol()
            ?: return
        if (calleeFunction.isKonify(context.session)) {
            checkKonifyCall(expression, calleeFunction, context, reporter)
        }
    }

}

/**
 * Check if `expression` - a call to a Konify function or access to a Konify property -
 * is allowed in the current context. It is allowed if:
 *
 * - It is executed as part of the body of a Konify function.
 * - It is not executed as part of the body of a lambda annotated with `@DisallowKonifyCalls`.
 * - It is not inside of a `try` block.
 * - It is a call to a readonly Konify function if it is executed in the body of a function
 *   that is annotated with `@ReadOnlyKonify`.
 *
 * A function is Konify if:
 * - It is annotated with `@Konify`.
 * - It is a lambda whose type is inferred to be `KonifyFunction`.
 * - It is an inline lambda whose enclosing function is Konify.
 */
private fun checkKonifyCall(
    expression: FirQualifiedAccessExpression,
    calleeFunction: FirCallableSymbol<*>,
    context: CheckerContext,
    reporter: DiagnosticReporter
) {
    context.visitCurrentScope(
        visitInlineLambdaParameter = { parameter ->
            if (parameter.returnTypeRef.hasDisallowKonifyCallsAnnotation(context.session)) {
                reporter.reportOn(
                    expression.calleeReference.source,
                    KonifyErrors.CAPTURED_Konify_INVOCATION,
                    parameter.symbol,
                    parameter.containingFunctionSymbol,
                    context
                )
            }
        },
        visitAnonymousFunction = { function ->
            if (function.typeRef.coneType.functionTypeKind(context.session) === KonifyFunction)
                return
        },
        visitFunction = { function ->
            if (function.hasAnnotation(KonifyAnnotations.Component,context.session)) {
                if (
                    function.hasAnnotation(KonifyAnnotations.ReadOnly,context.session) &&
                    !calleeFunction.isReadOnlyKonify(context.session)
                ) {
                    reporter.reportOn(
                        expression.calleeReference.source,
                        KonifyErrors.NONREADONLY_CALL_IN_READONLY_Konify,
                        context
                    )
                }
                return
            }
            // We allow Konify calls in local delegated properties.
            // The only call this could be is a getValue/setValue in the synthesized getter/setter.
            if (function is FirPropertyAccessor && function.propertySymbol.hasDelegate) {
                if (function.propertySymbol.isVar) {
                    reporter.reportOn(
                        function.source,
                        KonifyErrors.Konify_INVALID_DELEGATE,
                        context
                    )
                }
                // Only local variables can be implicitly Konify, for top-level or class-level
                // declarations we require an explicit annotation.
                if (!function.propertySymbol.isLocal) {
                    reporter.reportOn(
                        function.propertySymbol.source,
                        KonifyErrors.Konify_EXPECTED,
                        context
                    )
                }
                return
            }
            // We've found a non-Konify function which contains a Konify call.
            val source = if (function is FirPropertyAccessor) {
                function.propertySymbol.source
            } else {
                function.source
            }
            reporter.reportOn(source, KonifyErrors.Konify_EXPECTED, context)
        },
        visitTryExpression = { tryExpression, container ->
            // Only report an error if the Konify call happens inside of the `try`
            // block. Konify calls are allowed inside of `catch` and `finally` blocks.
            if (container !is FirCatch && tryExpression.finallyBlock != container) {
                reporter.reportOn(
                    tryExpression.source,
                    KonifyErrors.ILLEGAL_TRY_CATCH_AROUND_Konify,
                    context
                )
            }
        }
    )
    reporter.reportOn(
        expression.calleeReference.source,
        KonifyErrors.Konify_INVOCATION,
        context
    )
}

/**
 * Visits all (Anonymous)Functions and `try` expressions in the current scope until it finds
 * a declaration that introduces a new scope. Elements are visited from innermost to outermost.
 */
private inline fun CheckerContext.visitCurrentScope(
    visitInlineLambdaParameter: (FirValueParameter) -> Unit,
    visitAnonymousFunction: (FirAnonymousFunction) -> Unit = {},
    visitFunction: (FirFunction) -> Unit = {},
    visitTryExpression: (FirTryExpression, FirElement) -> Unit = { _, _ -> }
) {
    for ((elementIndex, element) in containingElements.withIndex().reversed()) {
        when (element) {
            is FirAnonymousFunction -> {
                if (element.inlineStatus == InlineStatus.Inline) {
                    findValueParameterForLambdaAtIndex(elementIndex)?.let { parameter ->
                        visitInlineLambdaParameter(parameter)
                    }
                }
                visitAnonymousFunction(element)
                if (element.inlineStatus != InlineStatus.Inline) {
                    return
                }
            }
            is FirFunction -> {
                visitFunction(element)
                return
            }
            is FirTryExpression -> {
                val container = containingElements.getOrNull(elementIndex + 1)
                    ?: continue
                visitTryExpression(element, container)
            }
            is FirProperty -> {
                // Coming from an initializer or delegate expression, otherwise we'd
                // have hit a FirFunction and would already report an error.
            }
            is FirValueParameter -> {
                // We're coming from a default value in a function declaration, we need to
                // look at the enclosing function.
            }
            is FirAnonymousObject, is FirAnonymousInitializer -> {
                // Anonymous objects don't change the current scope, continue.
            }
            // Every other declaration introduces a new scope which cannot be Konify.
            is FirDeclaration -> return
        }
    }
}

private fun CheckerContext.findValueParameterForLambdaAtIndex(
    elementIndex: Int
): FirValueParameter? {
    val argument = containingElements.getOrNull(elementIndex - 1) as? FirLambdaArgumentExpression
        ?: return null
    val argumentList = containingElements.getOrNull(elementIndex - 2) as? FirResolvedArgumentList
        ?: return null
    return argumentList.mapping[argument]
}


/**
 * Reports an error if we are invoking a lambda parameter of an inline function in a context
 * where Konify calls are not allowed, unless the lambda parameter is itself annotated
 * with `@DisallowKonifyCalls`.
 */
private fun checkInvoke(
    expression: FirQualifiedAccessExpression,
    context: CheckerContext,
    reporter: DiagnosticReporter
) {
    // Check that we're invoking a value parameter of an inline function
    val param = (expression.dispatchReceiver as? FirPropertyAccessExpression)
        ?.calleeReference
        ?.toResolvedValueParameterSymbol()
        ?: return
    if (param.resolvedReturnTypeRef.hasDisallowKonifyCallsAnnotation(context.session) ||
        !param.containingFunctionSymbol.isInline) {
        return
    }

    context.visitCurrentScope(
        visitInlineLambdaParameter = { parameter ->
            if (parameter.returnTypeRef.hasDisallowKonifyCallsAnnotation(context.session)) {
                reporter.reportOn(
                    param.source,
                    KonifyErrors.MISSING_DISALLOW_Konify_CALLS_ANNOTATION,
                    param,
                    parameter.symbol,
                    parameter.containingFunctionSymbol,
                    context
                )
            }
        }
    )
}
