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

import io.github.hooksw.konify.complier.KonifyIds
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
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.scopes.getDirectOverriddenFunctions
import org.jetbrains.kotlin.fir.scopes.getDirectOverriddenProperties
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertyAccessorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ProjectionKind
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isArrayType
import org.jetbrains.kotlin.fir.types.isString
import org.jetbrains.kotlin.fir.types.isUnit
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.name.StandardClassIds

fun FirFunction.hasKonifyAnnotation(session: FirSession): Boolean {
    return hasAnnotation(KonifyIds.ViewClassId, session)
}

fun FirAnnotationContainer.hasKonifyAnnotation(session: FirSession): Boolean {
    return hasAnnotation(KonifyIds.ViewClassId, session)
}

fun FirBasedSymbol<*>.hasKonifyAnnotation(session: FirSession): Boolean {
    return hasAnnotation(KonifyIds.ViewClassId, session)
}

fun FirAnnotationContainer.hasReadOnlyKonifyAnnotation(session: FirSession): Boolean {
    return hasAnnotation(KonifyIds.ReadOnlyViewClassId, session)
}

fun FirBasedSymbol<*>.hasReadOnlyKonifyAnnotation(session: FirSession): Boolean {
    return hasAnnotation(KonifyIds.ReadOnlyViewClassId, session)
}

fun FirCallableSymbol<*>.isKonify(session: FirSession): Boolean {
    return when (this) {
        is FirFunctionSymbol<*> -> hasKonifyAnnotation(session)
        is FirPropertySymbol -> {
            val symbol = getterSymbol
            if (symbol == null) {
                false
            } else {
                symbol.hasKonifyAnnotation(session) || symbol.isKonifyDelegate(session)
            }
        }
        else -> false
    }
}

fun FirCallableSymbol<*>.isReadOnlyKonify(session: FirSession): Boolean {
    return when (this) {
        is FirFunctionSymbol<*> -> hasReadOnlyKonifyAnnotation(session)
        is FirPropertySymbol -> getterSymbol?.hasReadOnlyKonifyAnnotation(session) == true
        else -> false
    }
}

@OptIn(SymbolInternals::class)
private fun FirPropertyAccessorSymbol.isKonifyDelegate(session: FirSession): Boolean {
    if (propertySymbol.hasDelegate.not()) {
        return false
    }
    fun FirStatement.toReturnExpressionOrNull(): FirReturnExpression? {
        return this as? FirReturnExpression
    }
    fun FirExpression.toFunctionCallOrNull(): FirFunctionCall? {
        return this as? FirFunctionCall
    }
    return fir
        .body
        ?.statements
        ?.singleOrNull()
        ?.toReturnExpressionOrNull()
        ?.result
        ?.toFunctionCallOrNull()
        ?.calleeReference
        ?.toResolvedCallableSymbol()
        ?.isKonify(session)
        ?: false
}

fun FirFunction.getDirectOverriddenFunctions(context: CheckerContext): List<FirFunctionSymbol<*>> {
    fun FirFunction.toPropertyAccessorOrNull(): FirPropertyAccessor? {
        return this as? FirPropertyAccessor?
    }
    fun FirClassLikeSymbol<*>.toClassSymbolOrNull(): FirClassSymbol<*>? {
        return this as? FirClassSymbol<*>
    }
    if (!isOverride && toPropertyAccessorOrNull()?.propertySymbol?.isOverride != true) {
        return emptyList()
    }
    val scope = containingClassLookupTag()
        ?.toSymbol(context.session)
        ?.toClassSymbolOrNull()
        ?.unsubstitutedScope(context)
        ?: return emptyList()
    return when (val symbol = symbol) {
        is FirNamedFunctionSymbol -> {
            scope.processFunctionsByName(symbol.name) {}
            scope.getDirectOverriddenFunctions(
                function = symbol,
                unwrapIntersectionAndSubstitutionOverride = true
            )
        }
        is FirPropertyAccessorSymbol -> {
            scope.getDirectOverriddenProperties(
                property = symbol.propertySymbol,
                unwrapIntersectionAndSubstitutionOverride = true
            ).mapNotNull { propertySymbol ->
                if (symbol.isGetter) {
                    propertySymbol.getterSymbol
                } else {
                    propertySymbol.setterSymbol
                }
            }
        }
        else -> emptyList()
    }
}

fun FirFunctionSymbol<*>.isMain(session: FirSession): Boolean {
    if (this !is FirNamedFunctionSymbol) {
        return false
    }
    if (typeParameterSymbols.isNotEmpty()) {
        return false
    }
    if (resolvedReturnType.isUnit.not()) {
        return false
    }
    if (jvmNameAsString(session) != "main") {
        return false
    }
    val parameterTypes = explicitParameterTypes
    when (parameterTypes.size) {
        1 -> {
            val type = parameterTypes.single()
            if (type.isArrayType.not() || type.typeArguments.size != 1) {
                return false
            }
            val elementTypeArgument = type.typeArguments[0]
            if (elementTypeArgument.kind != ProjectionKind.IN) {
                return false
            }
            val elementType = elementTypeArgument.type
            if (elementType == null || elementType.isString.not()) {
                return false
            }
        }
        else -> return false
    }
    return true
}

private fun FirNamedFunctionSymbol.jvmNameAsString(session: FirSession): String {
    return getAnnotationStringParameter(
        classId = StandardClassIds.Annotations.JvmName,
        session = session
    ) ?: name.asString()
}

private val FirFunctionSymbol<*>.explicitParameterTypes: List<ConeKotlinType>
    get() = buildList {
        resolvedContextReceivers.mapTo(this) { it.typeRef.coneType }
        val receiver = receiverParameter
        if (receiver != null) {
            add(receiver.typeRef.coneType)
        }
        valueParameterSymbols.mapTo(this) { it.resolvedReturnType }
    }
