package com.example.ui

class SwitchScope {
    private var building = true
    private var lastState: State<Boolean>? = null
    private val conditionMap = mutableMapOf<State<Boolean>, @ViewMarker (viewNode: ViewNode) -> Unit>()
    private var except: (@ViewMarker (viewNode: ViewNode) -> Unit)? = null
    fun If(b: State<Boolean>, function: @ViewMarker (viewNode: ViewNode) -> Unit) {
        if (!building) throw IllegalStateException("Else should be the end of the control flow")
        conditionMap[b] = function

    }

    fun Else(function: @ViewMarker (viewNode: ViewNode) -> Unit) {
        building = false
        except = function
    }

    @ReadOnlyViewNode
    fun observe(viewNode: ViewNode) {
        conditionMap.keys.forEach { state ->
            state.bind(viewNode) {
                notify(viewNode)
            }
        }
    }

    @ReadOnlyViewNode
    fun notify(viewNode: ViewNode) {
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


@ReadOnlyViewNode
fun Switch(function: SwitchScope.() -> Unit) {
    val switchScope = SwitchScope()
    switchScope.function()
}