package io.github.hooksw.konify.compiler.fir

import io.github.hooksw.konify.compiler.conf.Annotations
import io.github.hooksw.konify.compiler.conf.Names
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.isInlineOrValueClass
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createNestedClass
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name


class GenerateReifiedSupplier(session: FirSession) : FirDeclarationGenerationExtension(session) {

    private val predicate = DeclarationPredicate.create {
        annotated(Annotations.ReifiedSupplier.asSingleFqName())
    }
    private val map = hashMapOf<FirClassSymbol<*>, FirClassLikeSymbol<*>>()

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext
    ): Set<Name> {
        val result = mutableSetOf<Name>()
        if (classSymbol is FirRegularClassSymbol
            && classSymbol.isInlineOrValueClass()
            && session.predicateBasedProvider.matches(predicate, classSymbol)
        ) {
            result += Annotations.ReifiedSupplier.shortClassName
        }

        return result
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext
    ): FirClassLikeSymbol<*>? {
        if (owner !is FirRegularClassSymbol) return null
        if (!session.predicateBasedProvider.matches(predicate, owner)) return null
        return createNestedClass(
            owner,
            Annotations.ReifiedSupplier.shortClassName,
            KonifyPluginKey,
            ClassKind.INTERFACE
        ) {
            modality = Modality.ABSTRACT
            status {
                isFun = true
            }
        }.symbol.apply {
            map[this] = owner
        }
    }


    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        if (classSymbol.name == Annotations.ReifiedSupplier.shortClassName) {
            return setOf(Names.invoke)
        }
        return super.getCallableNamesForClass(classSymbol, context)
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()
        if (callableId.callableName == Names.invoke) {
            val fn = createMemberFunction(
                owner, KonifyPluginKey, Names.invoke,
                map[owner]!!.constructType(emptyArray(), false)
            ) {
                modality = Modality.ABSTRACT
                status {
                    isOperator=true
                }
            }
            return listOf(fn.symbol)
        }
        return super.generateFunctions(callableId, context)
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(predicate)
    }
}

object KonifyPluginKey : GeneratedDeclarationKey() {
    override fun toString(): String {
        return "KonifyPlugin"
    }
}