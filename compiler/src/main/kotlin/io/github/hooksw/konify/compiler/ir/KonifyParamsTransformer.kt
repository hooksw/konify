package io.github.hooksw.konify.compiler.ir

import io.github.hooksw.konify.compiler.conf.*
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.backend.jvm.ir.isInlineClassType
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.interpreter.hasAnnotation
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.util.OperatorNameConventions

class KonifyParamsTransformer(
    context: IrPluginContext,
    private val skipSuspendFunction: Boolean,
    private val skipSimpleFunction: Boolean,
) : AbstractCommonLower(context) {

    private val transformedFunctionSet = mutableSetOf<IrSimpleFunction>()

    private val transformedFunctions: MutableMap<IrSimpleFunction, IrSimpleFunction> =
        mutableMapOf()
    private val functionsWithChangedParams = mutableMapOf<IrSimpleFunction, List<Name>>()

    override fun lower(module: IrModuleFragment) {

        module.transformChildrenVoid(this)

        val typeRemapper = ComposerTypeRemapper(context, DeepCopySymbolRemapper())
        val transformer = DeepCopyIrTreeWithRemappedComposableTypes(
            context,
            typeRemapper
        ).also { typeRemapper.deepCopy = it }
        module.transformChildren(
            transformer,
            null
        )
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
        return copy().also { fn ->
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
            val changedList = mutableListOf<IrValueParameter>()
            fn.valueParameters = fn.valueParameters.map { param ->

                var newType = param.type
                val shouldSkip =
                    param.type.hasStatelessAnnotation() ||
                            ((param.type as? IrSimpleType)?.classifier?.owner as? IrClass)?.hasStatelessAnnotation() == true ||
                            skipSuspendFunction&&param.type.isSuspendFunctionTypeOrSubtype() || skipSimpleFunction&&param.type.isFunctionTypeOrSubtype()
                if (!shouldSkip) {
                    changedList += param
                    if (param.isVararg && param.varargElementType?.isFunctionTypeOrSubtype() != true) {

                        param.copyTo(
                            fn,
                            name = param.name,
                            type = IrSimpleTypeImpl(
                                context.irBuiltIns.arrayClass,
                                (param.type as IrSimpleType).nullability,
                                listOf(param.type as IrTypeArgument),
                                param.annotations
                            ),
                            isAssignable = param.defaultValue != null,
                            defaultValue = null,
                            varargElementType = param.type,
                        )
                    } else {
                        newType = newType.lazyType(context)

                        param.copyTo(
                            fn,
                            name = param.name,
                            type = newType,
                            isAssignable = param.defaultValue != null,
                            defaultValue = param.defaultValue?.let {
                                IrExpressionBodyImpl(
                                    it.startOffset, it.endOffset,
                                    it.expression.lazy(context)
                                )
                            }
                        )
                    }

                } else param
            }

            val valueParametersMapping = explicitParameters
                .zip(fn.explicitParameters)
                .toMap()
            functionsWithChangedParams[fn] = changedList.map { it.name }
            fn.transformChildrenVoid(object : IrElementTransformerVoid() {
                override fun visitExpression(expression: IrExpression): IrExpression {
                    if (expression is IrGetValue) {
                        val newParam = valueParametersMapping[expression.symbol.owner]

                        return if (newParam != null) {
                            if (newParam in changedList) {
                                if (newParam.isVararg) {
                                    IrCallImpl.fromSymbolOwner(
                                        expression.startOffset,
                                        expression.endOffset, context.contextFun(Funs.VarargMap) {
                                            it.owner.returnType == context.irBuiltIns.arrayClass
                                        }.symbol
                                    ).also {
                                        it.dispatchReceiver = expression
                                        it.putTypeArgument(0, newParam.varargElementType!!)
                                    }

                                } else {
                                    IrCallImpl.fromSymbolOwner(
                                        expression.startOffset,
                                        expression.endOffset,
                                        newParam.type.classOrFail.getSimpleFunction(Names.invoke.asString())!!
                                    ).also {
                                        it.dispatchReceiver =
                                            IrGetValueImpl(
                                                expression.startOffset,
                                                expression.endOffset,
                                                expression.type,
                                                newParam.symbol,
                                                expression.origin
                                            )
                                    }
                                }
                            } else {
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


                override fun visitCall(expression: IrCall): IrExpression {

                    return super.visitCall(expression.transformedIfNeeded())
                }
            })
        }
    }

    fun IrCall.transformedIfNeeded(): IrCall {
        val ownerFn = when {
            isKonifyLambdaInvoke() ->
                symbol.owner.lambdaInvoke()

            symbol.owner.hasKonifyAnnotation() ->
                symbol.owner.transformedIfNeeded()

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
            for (i in 0 until valueArgumentsCount) {
                var arg = getValueArgument(i)
                if (arg != null) {
                    val changedList = functionsWithChangedParams[symbol.owner]
                    if (changedList != null && changedList.contains(symbol.owner.valueParameters[i].name)) {
                        arg = arg.lazy(context)
                    }
                    it.putValueArgument(i, arg)
                }
            }
        }
    }


    fun IrSimpleFunction.copy(): IrSimpleFunction {
        // TODO(lmr): use deepCopy instead?
        return context.irFactory.createSimpleFunction(
            startOffset = startOffset,
            endOffset = endOffset,
            origin = origin,
            name = name,
            visibility = visibility,
            isInline = isInline,
            isExpect = isExpect,
            returnType = returnType,
            modality = modality,
            symbol = IrSimpleFunctionSymbolImpl(),
            isTailrec = isTailrec,
            isSuspend = isSuspend,
            isOperator = isOperator,
            isInfix = isInfix,
            isExternal = isExternal,
            containerSource = containerSource
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

            fun IrType.remapTypeParameters(): IrType = remapTypeParameters(this@copy, fn)

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
        return if (name.isSpecial || name.asString().contains(unsafeSymbolsRegex)) {
            val sanitized = name.asString().replace(unsafeSymbolsRegex, "\\$")
            Name.identifier(sanitized)
        } else name
    }

    private fun defaultParameterType(param: IrValueParameter): IrType {
        val type = param.type
        if (param.defaultValue == null) return type
        val constructorAccessible = !type.isPrimitiveType() && type.classOrNull?.owner?.primaryConstructor != null
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

    private fun IrSimpleFunction.lambdaInvoke(): IrSimpleFunction {
        val argCount = valueParameters.size
        val newFnClass = context.irBuiltIns.functionN(argCount).symbol.owner
        val newInvoke = newFnClass.functions.first {
            it.name == OperatorNameConventions.INVOKE
        }
        return newInvoke
    }

}
