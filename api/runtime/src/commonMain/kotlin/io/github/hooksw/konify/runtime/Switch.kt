package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.node.InternalViewNode
import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.state.State
import io.github.hooksw.konify.runtime.utils.fastForEach
import io.github.hooksw.konify.runtime.utils.fastForEachIndex

sealed interface SwitchScope {
    fun If(
        condition: State<Boolean>,
        block: ViewNode. () -> Unit
    )

    fun Else(block: ViewNode. () -> Unit)
}

inline fun ViewNode.Switch(crossinline function: SwitchScope.() -> Unit) {
    val switchNode = createChild()
    val scope = SwitchScopeImpl(switchNode)
    scope.function()
    scope.prepare()
    switchNode.prepare()
}

// -------- Internal --------

@PublishedApi
internal class SwitchScopeImpl(private val switchNode: ViewNode) : SwitchScope {
    private val notifyObserver: (Any?) -> Unit = { notifyChange() }

    private var prepared: Boolean = false

    private val ifConditions: MutableList<State<Boolean>> = ArrayList(2)

    private val ifBlocks: MutableList<ViewNode. () -> Unit> = ArrayList(2)

    private var elseBlock: (ViewNode.() -> Unit)? = null

    private var lastCondition: State<Boolean>? = null

    private lateinit var cacheNodes: MutableList<List<ViewNode>?>

    fun prepare() {
        for (condition in ifConditions) {
            condition.bind(notifyObserver)
        }
        cacheNodes = MutableList(ifConditions.size + 1) { null }
        switchNode.registerPrepared(this::notifyChange)
        switchNode.registerDisposed(this::dispose)
        prepared = true
    }

    private fun notifyChange() {
        val switchNode = switchNode as InternalViewNode
        val lastCondition = lastCondition
        var match = false
        ifConditions.fastForEachIndex { index, condition ->
            if (condition.value) {
                if (condition != lastCondition) {
                    switchNode.detachNodeAt(0)
                    this.lastCondition = condition
                    val currentConditionIndex = ifConditions.indexOf(condition)
                    val cacheNode = cacheNodes[index]
                    if (cacheNode.isNullOrEmpty()) {
                        switchNode.(ifBlocks[currentConditionIndex])()
                        cacheNodes[index] = switchNode.children
                    } else {
                        cacheNode.fastForEach {
                            switchNode.insertNodeTo(it,0)
                        }
                    }
                } else {
                    return
                }
                match = true
            }
        }
        if (match) return
        val elseBlock = elseBlock
        this.lastCondition = null
        if (elseBlock != null) {
            switchNode.detachNodeAt(0)
            val elseNodes = cacheNodes.last()
            if (elseNodes.isNullOrEmpty()) {
                switchNode.elseBlock()
                cacheNodes[cacheNodes.size - 1] = switchNode.children
            } else {
                elseNodes.fastForEach {
                    switchNode.insertNodeTo(it,0)
                }
            }
        }
    }

    private fun dispose() {
        ifConditions.fastForEach {condition->
            condition.unbind(notifyObserver)
        }
    }

    override fun If(condition: State<Boolean>, block: ViewNode.() -> Unit) {
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
