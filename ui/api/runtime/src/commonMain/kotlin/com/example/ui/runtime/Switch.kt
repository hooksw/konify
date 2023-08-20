package com.example.ui.runtime

import com.example.ui.runtime.annotation.View
import com.example.ui.runtime.node.ViewNode
import com.example.ui.runtime.state.State

sealed interface SwitchScope {
    fun If(
        condition: State<Boolean>,
        block: @View () -> Unit
    )

    fun Else(block: @View () -> Unit)
}

@View
inline fun Switch(crossinline function: SwitchScope.() -> Unit) {
    val node = currentViewNode
    val scope = SwitchScopeImpl(node)
    scope.function()
    scope.prepare()
}

// -------- Internal --------

@PublishedApi
internal class SwitchScopeImpl(private val node: ViewNode) : SwitchScope {
    private var prepared: Boolean = false

    private val ifConditions: MutableList<State<Boolean>> = ArrayList(2)

    private val ifBlocks: MutableList<@View () -> Unit> = ArrayList(2)

    private var elseBlock: (@View () -> Unit)? = null

    private var lastCondition: State<Boolean>? = null

    fun prepare() {
        for (condition in ifConditions) {
            condition.bind { notify() }
        }
        node.onPrepared {
            notify()
        }
        prepared = true
    }

    private fun notify() {
        val lastCondition = lastCondition
        for (condition in ifConditions) {
            if (condition.value.not()) {
                continue
            }
            if (condition != lastCondition) {
                node.removeAllChildren()
                this.lastCondition = condition
                val currentConditionIndex = ifConditions.indexOf(condition)
                injected(node, ifBlocks[currentConditionIndex])
            }
            return
        }
        val elseBlock = elseBlock
        if (elseBlock != null) {
            if (lastCondition != null) {
                node.removeAllChildren()
                this.lastCondition = null
            }
            injected(node, elseBlock)
        }
    }

    override fun If(condition: State<Boolean>, block: @View () -> Unit) {
        if (prepared) {
            error("Cannot add more blocks after prepared.")
        }
        ifConditions.add(condition)
        ifBlocks.add(block)
    }

    override fun Else(block: @View () -> Unit) {
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
