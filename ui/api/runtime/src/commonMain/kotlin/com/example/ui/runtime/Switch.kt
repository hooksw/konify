package com.example.ui.runtime

import com.example.ui.runtime.annotation.ReadOnlyView
import com.example.ui.runtime.node.ViewNode
import com.example.ui.runtime.state.State

sealed interface SwitchScope {
    fun If(
        condition: State<Boolean>,
        block: @ReadOnlyView () -> Unit
    )

    fun Else(block: @ReadOnlyView () -> Unit)
}

@ReadOnlyView
inline fun Switch(function: SwitchScope.() -> Unit) {
    val node = currentViewNode
    val scope = SwitchScopeImpl(node)
    scope.function()
    scope.prepare()
}

// -------- Internal --------

@PublishedApi
internal class SwitchScopeImpl(private val node: ViewNode) : SwitchScope {
    private var prepared: Boolean = false

    private val ifBlocks: MutableList<Pair<State<Boolean>, @ReadOnlyView () -> Unit>> = ArrayList(2)

    private var elseBlock: (@ReadOnlyView () -> Unit)? = null

    fun prepare() {
        ifBlocks.forEach { (condition, block) ->
            condition.bind { current ->

            }
        }
        prepared = true
    }

    override fun If(condition: State<Boolean>, block: @ReadOnlyView () -> Unit) {
        if (prepared) {
            error("Cannot add more blocks after prepared.")
        }
        ifBlocks.add(condition to block)
    }

    override fun Else(block: @ReadOnlyView () -> Unit) {
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
