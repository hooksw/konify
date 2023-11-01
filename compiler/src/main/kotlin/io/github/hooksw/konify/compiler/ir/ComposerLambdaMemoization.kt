/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.compose.compiler.plugins.kotlin.lower

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.isLocal

private class CaptureCollector {
    val captures = mutableSetOf<IrValueDeclaration>()
    val capturedDeclarations = mutableSetOf<IrSymbolOwner>()
    val hasCaptures: Boolean get() = captures.isNotEmpty() || capturedDeclarations.isNotEmpty()

    fun recordCapture(local: IrValueDeclaration) {
        captures.add(local)
    }

    fun recordCapture(local: IrSymbolOwner) {
        capturedDeclarations.add(local)
    }
}

private abstract class DeclarationContext {
    val localDeclarationCaptures = mutableMapOf<IrSymbolOwner, Set<IrValueDeclaration>>()
    fun recordLocalDeclaration(local: DeclarationContext) {
        localDeclarationCaptures[local.declaration] = local.captures
    }

    abstract val composable: Boolean
    abstract val symbol: IrSymbol
    abstract val declaration: IrSymbolOwner
    abstract val captures: Set<IrValueDeclaration>
    abstract val functionContext: FunctionContext?
    abstract fun declareLocal(local: IrValueDeclaration?)
    abstract fun recordCapture(local: IrValueDeclaration?): Boolean
    abstract fun recordCapture(local: IrSymbolOwner?)
    abstract fun pushCollector(collector: CaptureCollector)
    abstract fun popCollector(collector: CaptureCollector)
}

private fun List<DeclarationContext>.recordCapture(value: IrValueDeclaration) {
    for (dec in reversed()) {
        val shouldBreak = dec.recordCapture(value)
        if (shouldBreak) break
    }
}

private fun List<DeclarationContext>.recordLocalDeclaration(local: DeclarationContext) {
    for (dec in reversed()) {
        dec.recordLocalDeclaration(local)
    }
}

private fun List<DeclarationContext>.recordLocalCapture(local: IrSymbolOwner) {
    val capturesForLocal = reversed().firstNotNullOfOrNull { it.localDeclarationCaptures[local] }
    if (capturesForLocal != null) {
        capturesForLocal.forEach { recordCapture(it) }
        for (dec in reversed()) {
            dec.recordCapture(local)
            if (dec.localDeclarationCaptures.containsKey(local)) {
                // this is the scope that the class was defined in, so above this we don't need
                // to do anything
                break
            }
        }
    }
}

private class SymbolOwnerContext(override val declaration: IrSymbolOwner) : DeclarationContext() {
    override val composable get() = false
    override val functionContext: FunctionContext? get() = null
    override val symbol get() = declaration.symbol
    override val captures: Set<IrValueDeclaration> get() = emptySet()
    override fun declareLocal(local: IrValueDeclaration?) {}
    override fun recordCapture(local: IrValueDeclaration?): Boolean {
        return false
    }

    override fun recordCapture(local: IrSymbolOwner?) {}
    override fun pushCollector(collector: CaptureCollector) {}
    override fun popCollector(collector: CaptureCollector) {}
}

private class FunctionLocalSymbol(
    override val declaration: IrSymbolOwner,
    override val functionContext: FunctionContext
) : DeclarationContext() {
    override val composable: Boolean get() = functionContext.composable
    override val symbol: IrSymbol get() = declaration.symbol
    override val captures: Set<IrValueDeclaration> get() = functionContext.captures
    override fun declareLocal(local: IrValueDeclaration?) = functionContext.declareLocal(local)
    override fun recordCapture(local: IrValueDeclaration?) = functionContext.recordCapture(local)
    override fun recordCapture(local: IrSymbolOwner?) = functionContext.recordCapture(local)
    override fun pushCollector(collector: CaptureCollector) =
        functionContext.pushCollector(collector)

    override fun popCollector(collector: CaptureCollector) =
        functionContext.popCollector(collector)
}

private class FunctionContext(
    override val declaration: IrFunction,
    override val composable: Boolean,
    val canRemember: Boolean
) : DeclarationContext() {
    override val symbol get() = declaration.symbol
    override val functionContext: FunctionContext get() = this
    val locals = mutableSetOf<IrValueDeclaration>()
    override val captures: MutableSet<IrValueDeclaration> = mutableSetOf()
    var collectors = mutableListOf<CaptureCollector>()

    init {
        declaration.valueParameters.forEach {
            declareLocal(it)
        }
        declaration.dispatchReceiverParameter?.let { declareLocal(it) }
        declaration.extensionReceiverParameter?.let { declareLocal(it) }
    }

    override fun declareLocal(local: IrValueDeclaration?) {
        if (local != null) {
            locals.add(local)
        }
    }

    override fun recordCapture(local: IrValueDeclaration?): Boolean {
        val containsLocal = locals.contains(local)
        if (local != null && collectors.isNotEmpty() && containsLocal) {
            for (collector in collectors) {
                collector.recordCapture(local)
            }
        }
        if (local != null && declaration.isLocal && !containsLocal) {
            captures.add(local)
        }
        return containsLocal
    }

    override fun recordCapture(local: IrSymbolOwner?) {
        if (local != null) {
            val captures = localDeclarationCaptures[local]
            for (collector in collectors) {
                collector.recordCapture(local)
                if (captures != null) {
                    for (capture in captures) {
                        collector.recordCapture(capture)
                    }
                }
            }
        }
    }

    override fun pushCollector(collector: CaptureCollector) {
        collectors.add(collector)
    }

    override fun popCollector(collector: CaptureCollector) {
        require(collectors.lastOrNull() == collector)
        collectors.removeAt(collectors.size - 1)
    }
}

private class ClassContext(override val declaration: IrClass) : DeclarationContext() {
    override val composable: Boolean = false
    override val symbol get() = declaration.symbol
    override val functionContext: FunctionContext? = null
    override val captures: MutableSet<IrValueDeclaration> = mutableSetOf()
    val thisParam: IrValueDeclaration? = declaration.thisReceiver!!
    var collectors = mutableListOf<CaptureCollector>()
    override fun declareLocal(local: IrValueDeclaration?) {}
    override fun recordCapture(local: IrValueDeclaration?): Boolean {
        val isThis = local == thisParam
        val isCtorParam = (local?.parent as? IrConstructor)?.parent === declaration
        val isClassParam = isThis || isCtorParam
        if (local != null && collectors.isNotEmpty() && isClassParam) {
            for (collector in collectors) {
                collector.recordCapture(local)
            }
        }
        if (local != null && declaration.isLocal && !isClassParam) {
            captures.add(local)
        }
        return isClassParam
    }

    override fun recordCapture(local: IrSymbolOwner?) {}
    override fun pushCollector(collector: CaptureCollector) {
        collectors.add(collector)
    }

    override fun popCollector(collector: CaptureCollector) {
        require(collectors.lastOrNull() == collector)
        collectors.removeAt(collectors.size - 1)
    }
}

// This must match the highest value of FunctionXX which is current Function22
private const val MAX_RESTART_ARGUMENT_COUNT = 22
