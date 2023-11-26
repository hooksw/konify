package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.annotation.Component
import io.github.hooksw.konify.runtime.annotation.ReadOnly
import io.github.hooksw.konify.runtime.internal.toOriginal
import io.github.hooksw.konify.runtime.signal.*
import io.github.hooksw.konify.runtime.utils.fastForEach
import io.github.hooksw.konify.runtime.utils.fastForEachIndex

interface SwitchScope {
    @ReadOnly
    @Component
    fun If(
        condition: Boolean,
        block:@Component () -> Unit
    )

    @ReadOnly
    @Component
    fun Else(block:@Component () -> Unit)
}

@Component
fun Switch(function: SwitchScope.() -> Unit) {
    val scope = SwitchScopeImpl()
    scope.function()
    val node= CurrentNode!!
    createComputation {
        node.childNodes?.fastForEach {
            it.cleanup()
        }
        node.childNodes?.clear()
        scope.notifyChange()
    }.run()
    node.addOnDispose {
        scope.clear()
    }
}

// -------- Internal --------

@PublishedApi
internal class SwitchScopeImpl() : SwitchScope {

    private val ifConditions: MutableList<() -> Boolean> = ArrayList(2)

    private val ifBlocks: MutableList<() -> Unit> = ArrayList(2)

    private var elseBlock: (() -> Unit)? = null
    private var lastCondition: (() -> Boolean)? = null

    fun notifyChange() {
        var block: (() -> Unit)? = null
        ifConditions.fastForEachIndex { index, item ->
            val r = item()
            if (r) {
                if (lastCondition == item) {
                    return
                }
                block = ifBlocks[index]
                return@fastForEachIndex
            }
        }
        if (block == null && elseBlock != null) {
            block = elseBlock
        }
        block?.invoke()
    }

    fun clear() {
        ifConditions.clear()
        ifBlocks.clear()
        elseBlock = null
        lastCondition = null
    }


    override fun If(condition: Boolean, block: () -> Unit) {
        if (elseBlock != null) {
            error("Illegal order.")
        }
        ifConditions.add(toOriginal(condition))
        ifBlocks.add(block)
    }

    override fun Else(block: () -> Unit) {
        if (ifBlocks.isEmpty()) {
            error("If should be before Else.")
        }
        if (elseBlock != null) {
            error("A Switch can only have 1 Else.")
        }
        elseBlock = block
    }
}
