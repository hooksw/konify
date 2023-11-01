package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.node.InternalViewNode
import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.signal.*
import io.github.hooksw.konify.runtime.signal.DerivedSignal
import io.github.hooksw.konify.runtime.utils.fastForEachIndex

sealed interface SwitchScope {
    fun If(
        condition: ()->Boolean,
        block: ViewNode. () -> Unit
    )

    fun Else(block: ViewNode. () -> Unit)
}

inline fun ViewNode.Switch(crossinline function: SwitchScope.() -> Unit) {
    val scope = SwitchScopeImpl()
    scope.function()
    scope.prepare()
}

// -------- Internal --------

@PublishedApi
internal class SwitchScopeImpl() : SwitchScope {
    private var prepared: Boolean = false

    private val ifConditions: MutableList<()->Boolean> = ArrayList(2)

    private val ifBlocks: MutableList<ViewNode. () -> Unit> = ArrayList(2)

    private var elseBlock: (ViewNode.() -> Unit)? = null

    private var lastCondition: (()->Boolean)? = null

    private lateinit var cacheNodes: Array<ViewNode?>

    fun prepare() {
        val owner= Owners.last()
        (derived(nonEqualEquality()) {
            notifyChange(owner)
        } as DerivedSignal<Unit>).getValue()
        cacheNodes = arrayOfNulls(ifConditions.size + 1)
        prepared = true
    }

    private fun notifyChange(owner: Owner) {
        val switchNode = owner as InternalViewNode
        val lastCondition = lastCondition
        var matchInCondition = false
        ifConditions.fastForEachIndex { index, condition ->
            if (condition()) {
                if (condition != lastCondition) {
                    val child=switchNode.detachNodeAt(0)
                    this.lastCondition = condition
                    val currentConditionIndex = ifConditions.indexOf(condition)
                    val cacheNode = cacheNodes[index]
                    if (cacheNode == null) {
                        switchNode.(ifBlocks[currentConditionIndex])()
                        cacheNodes[index] = child
                    } else {
                        switchNode.insertNode(cacheNode, 0)
                    }
                } else {
                    return
                }
                matchInCondition = true
            }
        }
        if (matchInCondition) return
        val elseBlock = elseBlock
        this.lastCondition = null
        if (elseBlock != null) {
            val child = switchNode.detachNodeAt(0)
            val elseNodes = cacheNodes.last()
            if (elseNodes == null) {
                switchNode.elseBlock()
                cacheNodes[cacheNodes.size - 1] = child
            } else {
                switchNode.insertNode(elseNodes, 0)
            }
        }
    }

    override fun If(condition:()->Boolean, block: ViewNode.() -> Unit) {
        if (prepared) {
            error("Cannot add more blocks after prepared.")
        }
        ifConditions.add(condition)
        ifBlocks.add(block)
    }

    override fun Else(block: ViewNode. () -> Unit) {
        if (prepared) {
            error("Cannot add more blocks after prepared.")
        }
        if (ifBlocks.isEmpty()) {
            error("If should be before Else.")
        }
        if (elseBlock != null) {
            error("A Switch can only have 1 Else.")
        }
        elseBlock = block
    }
}
