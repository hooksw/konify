package io.github.hooksw.konify.complier.fir

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirAnonymousInitializer
import org.jetbrains.kotlin.fir.declarations.FirAnonymousObject
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.InlineStatus
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.expressions.FirCatch
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirLambdaArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirTryExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.references.toResolvedValueParameterSymbol
import org.jetbrains.kotlin.fir.resolve.isInvoke
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.functionTypeKind

object KonifyFunctionCallChecker : FirFunctionCallChecker() {
    override fun check(
        expression: FirFunctionCall,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
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

/**
 * Check if `expression` - a call to a composable function or access to a composable property -
 * is allowed in the current context. It is allowed if:
 *
 * - It is executed as part of the body of a composable function.
 * - It is not executed as part of the body of a lambda annotated with `@DisallowKonifyCalls`.
 * - It is not inside of a `try` block.
 * - It is a call to a readonly composable function if it is executed in the body of a function
 *   that is annotated with `@ReadOnlyKonify`.
 *
 * A function is composable if:
 * - It is annotated with `@Konify`.
 * - It is a lambda whose type is inferred to be `KonifyFunction`.
 * - It is an inline lambda whose enclosing function is composable.
 */
private fun checkKonifyCall(
    expression: FirQualifiedAccessExpression,
    calleeFunction: FirCallableSymbol<*>,
    context: CheckerContext,
    reporter: DiagnosticReporter
) {
    context.visitCurrentScope(
        visitAnonymousFunction = { function ->
            if (function.typeRef.coneType.functionTypeKind(context.session) === KonifyFunction)
                return
        },
        visitFunction = { function ->
            if (function.hasKonifyAnnotation(context.session)) {
                if (
                    function.hasReadOnlyKonifyAnnotation(context.session) &&
                    !calleeFunction.isReadOnlyKonify(context.session)
                ) {
                    reporter.reportOn(
                        expression.calleeReference.source,
                        KonifyErrors.NONREADONLY_CALL_IN_READONLY_COMPOSABLE,
                        context
                    )
                }
                return
            }
            // We allow composable calls in local delegated properties.
            // The only call this could be is a getValue/setValue in the synthesized getter/setter.
            if (function is FirPropertyAccessor && function.propertySymbol.hasDelegate) {
                if (function.propertySymbol.isVar) {
                    reporter.reportOn(
                        function.source,
                        KonifyErrors.COMPOSE_INVALID_DELEGATE,
                        context
                    )
                }
                // Only local variables can be implicitly composable, for top-level or class-level
                // declarations we require an explicit annotation.
                if (!function.propertySymbol.isLocal) {
                    reporter.reportOn(
                        function.propertySymbol.source,
                        KonifyErrors.COMPOSABLE_EXPECTED,
                        context
                    )
                }
                return
            }
            // We've found a non-composable function which contains a composable call.
            val source = if (function is FirPropertyAccessor) {
                function.propertySymbol.source
            } else {
                function.source
            }
            reporter.reportOn(source, KonifyErrors.COMPOSABLE_EXPECTED, context)
        },
        visitTryExpression = { tryExpression, container ->
            // Only report an error if the composable call happens inside of the `try`
            // block. Konify calls are allowed inside of `catch` and `finally` blocks.
            if (container !is FirCatch && tryExpression.finallyBlock != container) {
                reporter.reportOn(
                    tryExpression.source,
                    KonifyErrors.ILLEGAL_TRY_CATCH_AROUND_COMPOSABLE,
                    context
                )
            }
        }
    )
    reporter.reportOn(
        expression.calleeReference.source,
        KonifyErrors.COMPOSABLE_INVOCATION,
        context
    )
}

/**
 * Reports an error if we are invoking a lambda parameter of an inline function in a context
 * where composable calls are not allowed, unless the lambda parameter is itself annotated
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
    if (!param.containingFunctionSymbol.isInline) {
        return
    }

    context.visitCurrentScope()
}

/**
 * Visits all (Anonymous)Functions and `try` expressions in the current scope until it finds
 * a declaration that introduces a new scope. Elements are visited from innermost to outermost.
 */
private inline fun CheckerContext.visitCurrentScope(
    visitAnonymousFunction: (FirAnonymousFunction) -> Unit = {},
    visitFunction: (FirFunction) -> Unit = {},
    visitTryExpression: (FirTryExpression, FirElement) -> Unit = { _, _ -> }
) {
    for ((elementIndex, element) in containingElements.withIndex().reversed()) {
        when (element) {
            is FirAnonymousFunction -> {
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
            // Every other declaration introduces a new scope which cannot be composable.
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