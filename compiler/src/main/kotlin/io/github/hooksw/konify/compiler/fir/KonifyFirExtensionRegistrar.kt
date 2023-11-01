package io.github.hooksw.konify.compiler.fir

import io.github.hooksw.konify.compiler.conf.KonifyAnnotations
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirCallableReferenceAccessChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirPropertyAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirFunctionTypeKindExtension
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class KonifyFirExtensionRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::KonifyFunctionTypeKindExtension
        +::KonifyFirCheckersExtension
    }
}

class KonifyFunctionTypeKindExtension(
    session: FirSession
) : FirFunctionTypeKindExtension(session) {
    override fun FunctionTypeKindRegistrar.registerKinds() {
        registerKind(KonifyFunction, KKonifyFunction)
    }
}

object KonifyFunction : FunctionTypeKind(
    FqName.topLevel(Name.identifier("io.github.hooksw.konify")),
    "KonifyFunction",
    KonifyAnnotations.Component,
    isReflectType = false
) {
    override val prefixForTypeRender: String
        get() = "@Konify"

    override fun reflectKind(): FunctionTypeKind = KKonifyFunction
}

object KKonifyFunction : FunctionTypeKind(
    FqName.topLevel(Name.identifier("io.github.hooksw.konify")),
    "KKonifyFunction",
    KonifyAnnotations.Component,
    isReflectType = true
) {
    override fun nonReflectKind(): FunctionTypeKind = KonifyFunction
}

class KonifyFirCheckersExtension(session: FirSession) : FirAdditionalCheckersExtension(session) {
    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val functionCheckers: Set<FirFunctionChecker> =
            setOf(KonifyFunctionChecker)

        override val propertyCheckers: Set<FirPropertyChecker> =
            setOf(KonifyPropertyChecker)
    }

    override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        override val functionCallCheckers: Set<FirFunctionCallChecker> =
            setOf(KonifyFunctionCallChecker)

        override val propertyAccessExpressionCheckers: Set<FirPropertyAccessExpressionChecker> =
            setOf(KonifyPropertyAccessExpressionChecker)

        override val callableReferenceAccessCheckers: Set<FirCallableReferenceAccessChecker> =
            setOf(KonifyCallableReferenceChecker)
    }
}
