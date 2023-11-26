package io.github.hooksw.konify.compiler.ir

import androidx.compose.compiler.plugins.kotlin.lower.ModuleLoweringPass
import io.github.hooksw.konify.compiler.conf.Annotations
import io.github.hooksw.konify.compiler.conf.Classes
import io.github.hooksw.konify.compiler.conf.rootPackage
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.interpreter.hasAnnotation
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions

abstract class AbstractCommonLower(
    val context: IrPluginContext,
) : IrElementTransformerVoid(), ModuleLoweringPass {

    fun IrAnnotationContainer.hasKonifyAnnotation(): Boolean {
        return hasAnnotation(Annotations.Component.asSingleFqName())
    }

    fun getTopLevelClassOrNull(classId: ClassId): IrClassSymbol? {
        return context.referenceClass(classId)
    }

    fun getTopLevelClass(classId: ClassId): IrClassSymbol {
        return getTopLevelClassOrNull(classId) ?: error("Class not found in the classpath: ${classId.asSingleFqName()}")
    }

    fun IrCall.isInvoke(): Boolean {
        if (origin == IrStatementOrigin.INVOKE)
            return true
        val function = symbol.owner
        return function.name == OperatorNameConventions.INVOKE &&
                function.parentClassOrNull?.defaultType?.let {
                    it.isFunction() || it.isSyntheticComposableFunction()
                } ?: false
    }

    fun IrCall.isKonifyLambdaInvoke(): Boolean {
        if (!isInvoke()) return false
        // [ComposerParamTransformer] replaces composable function types of the form
        // `@Composable Function1<T1, T2>` with ordinary functions with extra parameters, e.g.,
        // `Function3<T1, Composer, Int, T2>`. After this lowering runs we have to check the
        // `attributeOwnerId` to recover the original type.
        val receiver = dispatchReceiver?.let { it.attributeOwnerId as? IrExpression ?: it }
        return receiver?.type?.let {
            it.hasKonifyAnnotation() || it.isSyntheticComposableFunction()
        } ?: false
    }

    fun IrSimpleFunction.hasReadOnlyAnnotation() = hasAnnotation(Annotations.ReadOnly.asSingleFqName())
    fun IrSimpleFunction.isKonifyFunction(): Boolean {
        return hasAnnotation(Annotations.Component.asSingleFqName())
    }

    fun IrType.hasStatelessAnnotation() = hasAnnotation(Annotations.Stateless.asSingleFqName())
    fun IrClass.hasStatelessAnnotation() = hasAnnotation(Annotations.Stateless.asSingleFqName())

}

internal inline fun <reified T : IrElement> T.copyWithNewTypeParams(
    source: IrFunction, target: IrFunction
): T {
    return deepCopyWithSymbols(target) { symbolRemapper, typeRemapper ->
        val typeParamRemapper = object : TypeRemapper by typeRemapper {
            override fun remapType(type: IrType): IrType {
                return typeRemapper.remapType(type.remapTypeParameters(source, target))
            }
        }
        val deepCopy = DeepCopyIrTreeWithSymbols(
            symbolRemapper, typeParamRemapper, SymbolRenamer.DEFAULT
        )
        (typeRemapper as? DeepCopyTypeRemapper)?.deepCopy = deepCopy
        deepCopy
    }
}

fun IrType.isSyntheticComposableFunction() =
    classOrNull?.owner?.let {
        it.name.asString().startsWith("KonifyFunction") &&
                it.packageFqName?.asString() == rootPackage
    } ?: false


private fun IrType.noBoxType(context: IrPluginContext): IrType {
    return when {
        isInt() -> context.contextClass(Classes.IntSupplier).defaultType
        isLong() -> context.contextClass(Classes.LongSupplier).defaultType
        isFloat() -> context.contextClass(Classes.FloatSupplier).defaultType
        isDouble() -> context.contextClass(Classes.DoubleSupplier).defaultType
        classOrFail.owner.hasAnnotation(Annotations.ReifiedSupplier.asSingleFqName()) -> context.contextClass(
            classOrFail.owner.classId!!.createNestedClassId(
                Annotations.ReifiedSupplier.shortClassName
            )
        ).defaultType

        else -> this
    }
}

fun IrType.lazyType(context: IrPluginContext): IrType {
    val ntype = this.noBoxType(context)
    return if (this == ntype) {
        IrSimpleTypeImpl(
            context.irBuiltIns.functionN(0).symbol,
            SimpleTypeNullability.DEFINITELY_NOT_NULL,
            listOf(this as IrTypeArgument),
            annotations
        )
    } else {
        ntype
    }
}

fun IrExpression.lazy(context: IrPluginContext): IrExpression {
    val ntype = type.noBoxType(context)
    return if (ntype == type) {
        IrFunctionExpressionImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            context.irBuiltIns.functionN(0).typeWith(type),
            createSimpleNoArgsLambda(context, ntype, this),
            IrStatementOrigin.LAMBDA
        )
    } else
        IrTypeOperatorCallImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, ntype, IrTypeOperator.SAM_CONVERSION, ntype,
            IrFunctionExpressionImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                context.irBuiltIns.functionN(0).typeWith(type),
                createSimpleNoArgsLambda(context, ntype, this),
                IrStatementOrigin.LAMBDA
            )
        )

}

fun createSimpleNoArgsLambda(context: IrPluginContext, _returnType: IrType, value: IrExpression) =
    context.irFactory.buildFun {
        name = Name.special("<anonymous>")
        returnType = _returnType
        visibility = DescriptorVisibilities.LOCAL
        modality = Modality.FINAL
        origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
    }.also { lambda ->
        lambda.dispatchReceiverParameter = null
        lambda.extensionReceiverParameter = null
        lambda.body = IrBlockBodyBuilder(
            context, Scope(lambda.symbol), UNDEFINED_OFFSET,
            UNDEFINED_OFFSET
        ).irBlockBody {
            +irReturn(value)
        }
    }

fun IrPluginContext.contextClass(classId: ClassId): IrClass {
    return referenceClass(classId)?.owner
        ?: error("Cannot find the ${classId.shortClassName} class in the classpath")
}

fun IrPluginContext.contextFun(classId: CallableId, predicate: (IrSimpleFunctionSymbol) -> Boolean): IrSimpleFunction {
    return referenceFunctions(classId).firstOrNull(predicate)?.owner
        ?: error("Cannot find the ${classId.callableName} class in the classpath")
}