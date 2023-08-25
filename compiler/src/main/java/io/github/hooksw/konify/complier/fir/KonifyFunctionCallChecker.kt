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
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirTryExpression
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
        val calleeFunction = expression.calleeReference.toResolvedCallableSymbol() ?: return
        if (calleeFunction.origin == FirDeclarationOrigin.SamConstructor) {
            return
        }
        if (calleeFunction.isKonify(context.session)) {
            checkKonifyCall(expression, calleeFunction, context, reporter)
        } else if (calleeFunction.callableId.isInvoke()) {
            checkInvoke(expression, context, reporter)
        }
    }
}

private fun checkKonifyCall(
    expression: FirQualifiedAccessExpression,
    calleeFunction: FirCallableSymbol<*>,
    context: CheckerContext,
    reporter: DiagnosticReporter
) {
    context.visitCurrentScope(
        visitAnonymousFunction = { function ->
            if (function.typeRef.coneType.functionTypeKind(context.session) === KonifyFunction) {
                return
            }
        },
        visitFunction = { function ->
            if (function.hasKonifyAnnotation(context.session)) {
                if (function.hasReadOnlyKonifyAnnotation(context.session) &&
                    calleeFunction.isReadOnlyKonify(context.session).not()
                ) {
                    reporter.reportOn(
                        source = expression.calleeReference.source,
                        factory = ComposableErrors.NONREADONLY_CALL_IN_READONLY_COMPOSABLE,
                        context = context
                    )
                }
                return
            }
            if (function is FirPropertyAccessor && function.propertySymbol.hasDelegate) {
                if (function.propertySymbol.isVar) {
                    reporter.reportOn(
                        source = function.source,
                        factory = ComposableErrors.COMPOSE_INVALID_DELEGATE,
                        context = context
                    )
                }
                if (!function.propertySymbol.isLocal) {
                    reporter.reportOn(
                        source = function.propertySymbol.source,
                        factory = ComposableErrors.COMPOSABLE_EXPECTED,
                        context = context
                    )
                }
                return
            }
            val source = if (function is FirPropertyAccessor) {
                function.propertySymbol.source
            } else {
                function.source
            }
            reporter.reportOn(
                source = source,
                factory = ComposableErrors.COMPOSABLE_EXPECTED,
                context = context
            )
        },
        visitTryExpression = { tryExpression, container ->
            if (container !is FirCatch && tryExpression.finallyBlock != container) {
                reporter.reportOn(
                    source = tryExpression.source,
                    factory = ComposableErrors.ILLEGAL_TRY_CATCH_AROUND_COMPOSABLE,
                    context = context
                )
            }
        }
    )
    reporter.reportOn(
        source = expression.calleeReference.source,
        factory = ComposableErrors.COMPOSABLE_INVOCATION,
        context = context
    )
}

private fun checkInvoke(
    expression: FirQualifiedAccessExpression,
    context: CheckerContext,
    reporter: DiagnosticReporter
) {
    fun FirExpression.toPropertyAccessExpressionOrNull(): FirPropertyAccessExpression? {
        return this as? FirPropertyAccessExpression
    }
    val param = expression
        .dispatchReceiver
        .toPropertyAccessExpressionOrNull()
        ?.calleeReference
        ?.toResolvedValueParameterSymbol()
        ?: return
    if (param.containingFunctionSymbol.isInline.not()) {
        return
    }
    context.visitCurrentScope()
}

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
                val container = containingElements.getOrNull(elementIndex + 1) ?: continue
                visitTryExpression(element, container)
            }
            is FirProperty -> {}
            is FirValueParameter -> {}
            is FirAnonymousObject, is FirAnonymousInitializer -> {}
            is FirDeclaration -> return
        }
    }
}
