package io.github.hooksw.konify.compiler.ir

import androidx.compose.compiler.plugins.kotlin.lower.ModuleLoweringPass
import io.github.hooksw.konify.compiler.conf.Classes
import io.github.hooksw.konify.compiler.conf.KonifyAnnotations
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.backend.common.serialization.proto.IrGetField
import org.jetbrains.kotlin.backend.jvm.ir.isInlineClassType
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFieldAccessExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.isJvm

abstract class AbstractCommonLower(
    val context: IrPluginContext,
): IrElementTransformerVoid(), ModuleLoweringPass {
    fun contextClass(classId: ClassId): IrClass {
        return context.referenceClass(classId)?.owner
            ?: error("Cannot find the ${classId.shortClassName} class in the classpath")
    }
    fun contextFun(classId: CallableId,predicate:(IrSimpleFunctionSymbol)->Boolean): IrSimpleFunction {
        return context.referenceFunctions(classId).firstOrNull(predicate)?.owner
            ?: error("Cannot find the ${classId.callableName} class in the classpath")
    }

    fun IrAnnotationContainer.hasKonifyAnnotation(): Boolean {
        return hasAnnotation(KonifyAnnotations.Component.asSingleFqName())
    }
    fun getTopLevelClassOrNull(classId: ClassId): IrClassSymbol? {
        return context.referenceClass(classId)
    }

    fun getTopLevelClass(classId: ClassId): IrClassSymbol {
        return getTopLevelClassOrNull(classId)
            ?: error("Class not found in the classpath: ${classId.asSingleFqName()}")
    }
    fun IrSimpleFunction.hasReadOnlyAnnotation()=hasAnnotation(KonifyAnnotations.ReadOnly.asSingleFqName())
    fun IrSimpleFunction.isKonifyFunction(): Boolean {
        return hasAnnotation(KonifyAnnotations.Component.asSingleFqName())
    }
    fun IrSimpleFunction.copy(context: IrPluginContext): IrSimpleFunction {
        // TODO(lmr): use deepCopy instead?
        return context.irFactory.createFunction(
            startOffset,
            endOffset,
            origin,
            IrSimpleFunctionSymbolImpl(),
            name,
            visibility,
            modality,
            returnType,
            isInline,
            isExternal,
            isTailrec,
            isSuspend,
            isOperator,
            isInfix,
            isExpect,
            isFakeOverride,
            containerSource
        ).also { fn ->
            fn.copyAttributes(this)
            val propertySymbol = correspondingPropertySymbol
            if (propertySymbol != null) {
                fn.correspondingPropertySymbol = propertySymbol
                if (propertySymbol.owner.getter == this) {
                    propertySymbol.owner.getter = fn
                }
                if (propertySymbol.owner.setter == this) {
                    propertySymbol.owner.setter = fn
                }
            }
            fn.parent = parent
            fn.copyTypeParametersFrom(this)

            fun IrType.remapTypeParameters(): IrType =
                remapTypeParameters(this@copy, fn)

            fn.returnType = returnType.remapTypeParameters()

            fn.dispatchReceiverParameter = dispatchReceiverParameter?.copyTo(fn)
            fn.extensionReceiverParameter = extensionReceiverParameter?.copyTo(fn)
            fn.valueParameters = valueParameters.map { param ->
                // Composable lambdas will always have `IrGet`s of all of their parameters
                // generated, since they are passed into the restart lambda. This causes an
                // interesting corner case with "anonymous parameters" of composable functions.
                // If a parameter is anonymous (using the name `_`) in user code, you can usually
                // make the assumption that it is never used, but this is technically not the
                // case in composable lambdas. The synthetic name that kotlin generates for
                // anonymous parameters has an issue where it is not safe to dex, so we sanitize
                // the names here to ensure that dex is always safe.
                val newName = dexSafeName(param.name)

                val newType = defaultParameterType(param).remapTypeParameters()
                param.copyTo(
                    fn,
                    name = newName,
                    type = newType,
                    isAssignable = param.defaultValue != null,
                    defaultValue = param.defaultValue?.copyWithNewTypeParams(
                        source = this, target = fn
                    )
                )
            }
            fn.contextReceiverParametersCount = contextReceiverParametersCount
            fn.annotations = annotations.toList()
            fn.metadata = metadata
            fn.body = moveBodyTo(fn)?.copyWithNewTypeParams(this, fn)
        }
    }

    private val unsafeSymbolsRegex = "[ <>]".toRegex()
    private fun dexSafeName(name: Name): Name {
        return if (
            name.isSpecial || name.asString().contains(unsafeSymbolsRegex)
        ) {
            val sanitized = name
                .asString()
                .replace(unsafeSymbolsRegex, "\\$")
            Name.identifier(sanitized)
        } else name
    }

    private fun defaultParameterType(param: IrValueParameter): IrType {
        val type = param.type
        if (param.defaultValue == null) return type
        val constructorAccessible = !type.isPrimitiveType() &&
                type.classOrNull?.owner?.primaryConstructor != null
        return when {
            type.isPrimitiveType() -> type
            type.isInlineClassType() -> if (context.platform.isJvm() || constructorAccessible) {
                type
            } else {
                // k/js and k/native: private constructors of value classes can be not accessible.
                // Therefore it won't be possible to create a "fake" default argument for calls.
                // Making it nullable allows to pass null.
                type.makeNullable()
            }
            else -> type.makeNullable()
        }
    }

    fun IrType.isSignalMarker()=
        isSubtypeOfClass(contextClass(Classes.SignalMarker).symbol)

    fun IrType.hasStatelessAnnotation()=hasAnnotation(KonifyAnnotations.Stateless.asSingleFqName())
    fun IrClass.hasStatelessAnnotation()=hasAnnotation(KonifyAnnotations.Stateless.asSingleFqName())

    fun IrSignalGetValue(){

    }

}
internal inline fun <reified T : IrElement> T.copyWithNewTypeParams(
    source: IrFunction,
    target: IrFunction
): T {
    return deepCopyWithSymbols(target) { symbolRemapper, typeRemapper ->
        val typeParamRemapper = object : TypeRemapper by typeRemapper {
            override fun remapType(type: IrType): IrType {
                return typeRemapper.remapType(type.remapTypeParameters(source, target))
            }
        }
        val deepCopy = DeepCopyIrTreeWithSymbols(
            symbolRemapper,
            typeParamRemapper,
            SymbolRenamer.DEFAULT
        )
        (typeRemapper as? DeepCopyTypeRemapper)?.deepCopy = deepCopy
        deepCopy
    }
}