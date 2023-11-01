package io.github.hooksw.konify.compiler.fir

import io.github.hooksw.konify.compiler.conf.KonifyAnnotations
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.getAnnotationStringParameter
import org.jetbrains.kotlin.fir.analysis.checkers.unsubstitutedScope
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isOverride
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.scopes.getDirectOverriddenFunctions
import org.jetbrains.kotlin.fir.scopes.getDirectOverriddenProperties
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds


fun FirAnnotationContainer.hasAnnotation(classId: ClassId, session: FirSession): Boolean =
    hasAnnotation(classId, session)

fun FirBasedSymbol<*>.hasAnnotation(classId: ClassId, session: FirSession): Boolean =
    hasAnnotation(classId, session)

fun FirCallableSymbol<*>.isKonify(session: FirSession): Boolean =
    when (this) {
        is FirFunctionSymbol<*> ->
            hasAnnotation(KonifyAnnotations.Component,session)
        is FirPropertySymbol ->
            getterSymbol?.let {
                it.hasAnnotation(KonifyAnnotations.Component,session) || it.isKonifyDelegate(session)
            } ?: false
        else -> false
    }

fun FirBasedSymbol<*>.hasReadOnlyComposableAnnotation(session: FirSession): Boolean =
    hasAnnotation(KonifyAnnotations.ReadOnly, session)

fun FirAnnotationContainer.hasDisallowComposableCallsAnnotation(session: FirSession): Boolean =
    hasAnnotation(KonifyAnnotations.DisallowKonifyCalls, session)

fun FirCallableSymbol<*>.isReadOnlyKonify(session: FirSession): Boolean =
    when (this) {
        is FirFunctionSymbol<*> ->
            hasReadOnlyComposableAnnotation(session)
        is FirPropertySymbol ->
            getterSymbol?.hasReadOnlyComposableAnnotation(session) ?: false
        else -> false
    }

fun FirAnnotationContainer.hasDisallowKonifyCallsAnnotation(session: FirSession): Boolean =
    hasAnnotation(KonifyAnnotations.DisallowKonifyCalls, session)
@OptIn(SymbolInternals::class)
private fun FirPropertyAccessorSymbol.isKonifyDelegate(session: FirSession): Boolean {
    if (!propertySymbol.hasDelegate) return false
    return ((fir
        .body
        ?.statements
        ?.singleOrNull() as? FirReturnExpression)
        ?.result as? FirFunctionCall)
        ?.calleeReference
        ?.toResolvedCallableSymbol()
        ?.isKonify(session)
        ?: false
}

fun FirFunction.getDirectOverriddenFunctions(
    context: CheckerContext
): List<FirFunctionSymbol<*>> {
    if (!isOverride && (this as? FirPropertyAccessor)?.propertySymbol?.isOverride != true)
        return listOf()

    val scope = (containingClassLookupTag()
        ?.toSymbol(context.session) as? FirClassSymbol<*>)
        ?.unsubstitutedScope(context)
        ?: return listOf()

    return when (val symbol = symbol) {
        is FirNamedFunctionSymbol -> {
            scope.processFunctionsByName(symbol.name) {}
            scope.getDirectOverriddenFunctions(symbol, true)
        }
        is FirPropertyAccessorSymbol -> {
            scope.getDirectOverriddenProperties(symbol.propertySymbol, true).mapNotNull {
                if (symbol.isGetter) it.getterSymbol else it.setterSymbol
            }
        }
        else -> listOf()
    }
}

// TODO: Replace this with the FIR MainFunctionDetector once it lands upstream!
fun FirFunctionSymbol<*>.isMain(session: FirSession): Boolean {
    if (this !is FirNamedFunctionSymbol) return false
    if (typeParameterSymbols.isNotEmpty()) return false
    if (!resolvedReturnType.isUnit) return false
    if (jvmNameAsString(session) != "main") return false

    val parameterTypes = explicitParameterTypes
    when (parameterTypes.size) {
        0 -> {
            /*
            assert(DescriptorUtils.isTopLevelDeclaration(descriptor)) { "main without parameters works only for top-level" }
            val containingFile = DescriptorToSourceUtils.getContainingFile(descriptor)
            // We do not support parameterless entry points having JvmName("name") but different real names
            // See more at https://github.com/Kotlin/KEEP/blob/master/proposals/enhancing-main-convention.md#parameterless-main
            if (descriptor.name.asString() != "main") return false
            if (containingFile?.declarations?.any { declaration -> isMainWithParameter(declaration, checkJvmStaticAnnotation) } == true) {
                return false
            }*/
        }
        1 -> {
            val type = parameterTypes.single()
            if (!type.isArrayType || type.typeArguments.size != 1) return false
            val elementType = type.typeArguments[0].takeIf { it.kind != ProjectionKind.IN }?.type
                ?: return false
            if (!elementType.isString) return false
        }
        else -> return false
    }
    /*
    if (DescriptorUtils.isTopLevelDeclaration(descriptor)) return true

    val containingDeclaration = descriptor.containingDeclaration
    return containingDeclaration is ClassDescriptor
            && containingDeclaration.kind.isSingleton
            && (descriptor.hasJvmStaticAnnotation() || !checkJvmStaticAnnotation)
     */
    return true
}

private fun FirNamedFunctionSymbol.jvmNameAsString(session: FirSession): String =
    getAnnotationStringParameter(StandardClassIds.Annotations.JvmName, session)
        ?: name.asString()

private val FirFunctionSymbol<*>.explicitParameterTypes: List<ConeKotlinType>
    get() = resolvedContextReceivers.map { it.typeRef.coneType } +
            listOfNotNull(receiverParameter?.typeRef?.coneType) +
            valueParameterSymbols.map { it.resolvedReturnType }
