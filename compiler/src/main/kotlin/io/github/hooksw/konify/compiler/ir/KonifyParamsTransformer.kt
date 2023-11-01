package io.github.hooksw.konify.compiler.ir

import io.github.hooksw.konify.compiler.conf.Classes
import io.github.hooksw.konify.compiler.conf.Funs
import io.github.hooksw.konify.compiler.conf.ParameterNames
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.utils.typeArguments
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrPropertyImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.argumentsCount

class KonifyParamsTransformer(
    context: IrPluginContext,
) : AbstractCommonLower(context) {

    private var inlineLambdaInfo = KonifyInlineLambdaLocator(context)
    private val transformedFunctionSet = mutableSetOf<IrSimpleFunction>()

    private val transformedFunctions: MutableMap<IrSimpleFunction, IrSimpleFunction> =
        mutableMapOf()

    override fun lower(module: IrModuleFragment) {

        module.transformChildrenVoid(this)

        module.patchDeclarationParents()
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        return super.visitSimpleFunction(declaration.transformedIfNeeded())
    }

    private fun IrSimpleFunction.transformedIfNeeded(): IrSimpleFunction {
        // don't transform functions that themselves were produced by this function. (ie, if we
        // call this with a function that has the synthetic composer parameter, we don't want to
        // transform it further).
        if (transformedFunctionSet.contains(this)) return this


        // some functions were transformed during previous compilations or in other modules
        if (this.externallyTransformed()) {
            return this
        }

        // if not a composable fn, nothing we need to do
        if (!this.isKonifyFunction()) {
            return this
        }

        // we don't bother transforming expect functions. They exist only for type resolution and
        // don't need to be transformed to have a composer parameter
        if (isExpect) return this

        // cache the transformed function with composer parameter
        return transformedFunctions[this] ?: transformed()
    }

    private fun IrSimpleFunction.transformed(): IrSimpleFunction {

        assert(explicitParameters.lastOrNull()?.name != ParameterNames.ViewNode) {
            "Attempted to add composer param to $this, but it has already been added."
        }
        return copy(context).also { fn ->
            val oldFn = this

            // NOTE: it's important to add these here before we recurse into the body in
            // order to avoid an infinite loop on circular/recursive calls
            transformedFunctionSet.add(fn)
            transformedFunctions[oldFn] = fn

            // The overridden symbols might also be composable functions, so we want to make sure
            // and transform them as well
            fn.overriddenSymbols = overriddenSymbols.map {
                it.owner.transformedIfNeeded().symbol
            }

            // if we are transforming a composable property, the jvm signature of the
            // corresponding getters and setters have a composer parameter. Since Kotlin uses the
            // lack of a parameter to determine if it is a getter, this breaks inlining for
            // composable property getters since it ends up looking for the wrong jvmSignature.
            // In this case, we manually add the appropriate "@JvmName" annotation so that the
            // inliner doesn't get confused.
            fn.correspondingPropertySymbol?.let { propertySymbol ->
                if (!fn.hasAnnotation(DescriptorUtils.JVM_NAME)) {
                    val propertyName = propertySymbol.owner.name.identifier
                    val name = if (fn.isGetter) {
                        JvmAbi.getterName(propertyName)
                    } else {
                        JvmAbi.setterName(propertyName)
                    }
                    fn.annotations += jvmNameAnnotation(name)
                }
            }
            fn.valueParameters = fn.valueParameters.map { param ->
                val newType = when {
                    param.type.hasStatelessAnnotation() ||
                            ((param.type as? IrSimpleType)?.classifier?.owner as? IrClass)?.hasStatelessAnnotation() == true -> {
                        param.type
                    }

                    else -> {
                        if (param.type.isSignalMarker()) {
                            param.type
                        } else {
                            val cls = when {
                                param.type.isInt() -> Classes.IntSignal
                                param.type.isLong() -> Classes.LongSignal
                                param.type.isFloat() -> Classes.FloatSignal
                                param.type.isDouble() -> Classes.DoubleSignal
                                else -> Classes.Signal
                            }
                            IrSimpleTypeImpl(
                                contextClass(cls).symbol,
                                if (param.type.isNullable()) SimpleTypeNullability.MARKED_NULLABLE else SimpleTypeNullability.NOT_SPECIFIED,
                                listOf(param.type as IrTypeArgument),
                                param.type.annotations
                            )
                        }
                    }

                }
                param.copyTo(
                    fn,
                    name = param.name,
                    type = newType,
                    isAssignable = param.defaultValue != null,
                    defaultValue = param.defaultValue?.let {
                        IrExpressionBodyImpl(
                            it.startOffset, it.endOffset,
                            IrCallImpl(
                                UNDEFINED_OFFSET,
                                UNDEFINED_OFFSET,
                                newType,
                                contextFun(Funs.constant) { it.owner.valueParameters.size == 1 }.symbol,
                                1,
                                1
                            ).apply {
                                putValueArgument(0, it.expression.copyWithNewTypeParams(this@transformed, fn))
                            }
                        )
                    }
                )
            }
            val valueParametersMapping = explicitParameters
                .zip(fn.explicitParameters)
                .toMap()

            // $viewNode
            val ViewNodeParam = fn.addValueParameter {
                name = ParameterNames.ViewNode
                type = contextClass(Classes.ViewNode).defaultType
                origin = IrDeclarationOrigin.DEFINED
                isAssignable = true
            }
            inlineLambdaInfo.scan(fn)
            fn.transformChildrenVoid(object : IrElementTransformerVoid() {
                var isNestedScope = false
                override fun visitExpression(expression: IrExpression): IrExpression {
                    if(expression is IrGetValue){
                        val newParam = valueParametersMapping[expression.symbol.owner]

                        return if (newParam != null) {
                            if(newParam.type.isSignalMarker()){
                                val type=newParam.type as IrSimpleType
                                IrCallImpl(
                                    expression.startOffset,
                                    expression.endOffset,
                                    type,
                                    type.classOrNull!!.getPropertyGetter("value")!!,
                                    type.argumentsCount(),0,expression.origin,
                                ).apply {
                                    type.arguments.forEachIndexed { index, irTypeArgument ->
                                        putTypeArgument(index, type)
                                    }
                                }
                            }else{
                                IrGetValueImpl(
                                    expression.startOffset,
                                    expression.endOffset,
                                    expression.type,
                                    newParam.symbol,
                                    expression.origin
                                )
                            }
                        } else expression
                    }
                    return super.visitExpression(expression)
                }

                override fun visitReturn(expression: IrReturn): IrExpression {
                    if (expression.returnTargetSymbol == oldFn.symbol) {
                        // update the return statement to point to the new function, or else
                        // it will be interpreted as a non-local return
                        return super.visitReturn(
                            IrReturnImpl(
                                expression.startOffset,
                                expression.endOffset,
                                expression.type,
                                fn.symbol,
                                expression.value
                            )
                        )
                    }
                    return super.visitReturn(expression)
                }

                override fun visitFunction(declaration: IrFunction): IrStatement {
                    val wasNested = isNestedScope
                    try {
                        // we don't want to pass the composer parameter in to composable calls
                        // inside of nested scopes.... *unless* the scope was inlined.
                        isNestedScope = wasNested ||
                                !inlineLambdaInfo.isInlineLambda(declaration) ||
                                declaration.hasKonifyAnnotation()
                        return super.visitFunction(declaration)
                    } finally {
                        isNestedScope = wasNested
                    }
                }

                override fun visitCall(expression: IrCall): IrExpression {
                    val expr = if (!isNestedScope) {
                        expression.transformedIfNeeded(ViewNodeParam)
                    } else
                        expression
                    return super.visitCall(expr)
                }
            })
        }
    }

    fun IrCall.transformedIfNeeded(composerParam: IrValueParameter): IrCall {
        val ownerFn = when {
//            symbol.owner.isComposableDelegatedAccessor() -> {
//                if (!symbol.owner.hasKonifyAnnotation()) {
//                    symbol.owner.annotations += createComposableAnnotation()
//                }
//                symbol.owner.withComposerParamIfNeeded()
//            }

            isComposableLambdaInvoke() ->
                symbol.owner.lambdaInvokeWithComposerParam()

            symbol.owner.hasComposableAnnotation() ->
                symbol.owner.withComposerParamIfNeeded()
            // Not a composable call
            else -> return this
        }

        return IrCallImpl(
            startOffset,
            endOffset,
            type,
            ownerFn.symbol,
            typeArgumentsCount,
            ownerFn.valueParameters.size,
            origin,
            superQualifierSymbol
        ).also {
            it.copyAttributes(this)
            it.copyTypeArgumentsFrom(this)
            it.dispatchReceiver = dispatchReceiver
            it.extensionReceiver = extensionReceiver
            val argumentsMissing = mutableListOf<Boolean>()
            for (i in 0 until valueArgumentsCount) {
                val arg = getValueArgument(i)
                val param = ownerFn.valueParameters[i]
                val hasDefault = ownerFn.hasDefaultExpressionDefinedForValueParameter(i)
                argumentsMissing.add(arg == null && hasDefault)
                if (arg != null) {
                    it.putValueArgument(i, arg)
                } else if (param.isVararg) {
                    // do nothing
                } else {
                    it.putValueArgument(i, defaultArgumentFor(param))
                }
            }
            val valueParams = valueArgumentsCount
            val realValueParams = valueParams - ownerFn.contextReceiverParametersCount
            var argIndex = valueArgumentsCount
            it.putValueArgument(
                argIndex++,
                IrGetValueImpl(
                    UNDEFINED_OFFSET,
                    UNDEFINED_OFFSET,
                    composerParam.symbol
                )
            )

        }
    }

    private fun jvmNameAnnotation(name: String): IrConstructorCall {
        val jvmName = getTopLevelClass(StandardClassIds.Annotations.JvmName)
        val ctor = jvmName.constructors.first { it.owner.isPrimary }
        val type = jvmName.createType(false, emptyList())
        return IrConstructorCallImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            type,
            ctor,
            0, 0, 1
        ).also {
            it.putValueArgument(
                0,
                IrConstImpl.string(
                    UNDEFINED_OFFSET,
                    UNDEFINED_OFFSET,
                    context.irBuiltIns.stringType,
                    name
                )
            )
        }
    }
}


private fun IrSimpleFunction.externallyTransformed(): Boolean {
    return valueParameters.firstOrNull {
        it.name == ParameterNames.ViewNode
    } != null
}

fun IrExpressionBody.wrapWithSignal():IrExpressionBody{
    val hasSignal=false
    tra(object :IrElementVisitorVoid{
        override fun visitExpression(expression: IrExpression) {
            val type=expression.type as? IrSimpleType
            expression
            super.visitExpression(expression)
        }
    })
}