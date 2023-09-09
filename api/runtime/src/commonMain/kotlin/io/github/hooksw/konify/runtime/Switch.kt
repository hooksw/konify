package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.state.State

sealed interface SwitchScope {
    fun If(
        condition: State<Boolean>,
        block: ViewNode. () -> Unit
    )

    fun Else(block: ViewNode. () -> Unit)
}

inline fun ViewNode.Switch(crossinline function: SwitchScope.() -> Unit) {
    val scope = SwitchScopeImpl(this)
    scope.function()
    scope.prepare()
}

// -------- Internal --------

@PublishedApi
internal class SwitchScopeImpl(private val node: ViewNode) : SwitchScope {
    private var prepared: Boolean = false

    private val ifConditions: MutableList<State<Boolean>> = ArrayList(2)

    private val ifBlocks: MutableList<ViewNode. () -> Unit> = ArrayList(2)

    private var elseBlock: (ViewNode. () -> Unit)? = null

    private var lastCondition: State<Boolean>? = null

    fun prepare() {
        for (condition in ifConditions) {
            condition.bind { notifyChange() }
        }
        node.onPrepared {
            notifyChange()
        }
        prepared = true
    }

    private fun notifyChange() {
        val lastCondition = lastCondition
        for (condition in ifConditions) {
            if (condition.value.not()) {
                continue
            }
            if (condition != lastCondition) {
                node.removeAllChildren()
                this.lastCondition = condition
                val currentConditionIndex = ifConditions.indexOf(condition)
                node.(ifBlocks[currentConditionIndex])()
            }
            return
        }
        val elseBlock = elseBlock
        if (elseBlock != null) {
            if (lastCondition != null) {
                node.removeAllChildren()
                this.lastCondition = null
            }
            node.elseBlock()
        }
    }

    override fun If(condition: State<Boolean>, block: ViewNode. () -> Unit) {
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
