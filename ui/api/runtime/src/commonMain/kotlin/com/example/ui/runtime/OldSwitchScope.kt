package com.example.ui.runtime

import com.example.ui.core.foundation.annotation.ReadOnlyView
import com.example.ui.core.foundation.annotation.View
import com.example.ui.core.foundation.node.OldViewNode
import com.example.ui.core.foundation.state.State

class OldSwitchScope {
    private var building = true
    private var lastState: State<Boolean>? = null
    private val conditionMap = mutableMapOf<State<Boolean>, @View (viewNode: OldViewNode) -> Unit>()
    private var except: (@View (viewNode: OldViewNode) -> Unit)? = null
    fun If(b: State<Boolean>, function: @View (viewNode: OldViewNode) -> Unit) {
        if (!building) throw IllegalStateException("Else should be the end of the control flow")
        conditionMap[b] = function

    }

    fun Else(function: @View (viewNode: OldViewNode) -> Unit) {
        building = false
        except = function
    }

    @ReadOnlyView
    fun observe(viewNode: OldViewNode) {
        conditionMap.keys.forEach { state ->
            state.bind {
                notify(viewNode)
            }
        }
    }

    @ReadOnlyView
    fun notify(viewNode: OldViewNode) {
        var inCondition = false
        conditionMap.forEach { (state, call) ->
            if (state.value && (lastState == null || lastState == state)) {
                viewNode.removeAllChildren()
                lastState = state
                inCondition = true
                call(viewNode)
                return@forEach
            }
        }
        if (!inCondition) {
            except?.invoke(viewNode)
        }
    }
}
