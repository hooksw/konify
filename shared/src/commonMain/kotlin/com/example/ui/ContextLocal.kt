package com.example.ui

/*
* 目前设想是context local 通过包含state来实现响应式更新
* */

class ContextLocal<T>(init: T) {
    private val default = readonlyStateOf(init)
    val current: State<T>
        @ReadOnlyViewNode
        get() = currentViewNode.getContextLocal(this) ?: default
}

@ReadOnlyViewNode
fun ContextProvider(viewNode: ViewNode, contextProvide: ContextProvide<*>, call: () -> Unit) {
    viewNode.addContextLocal(contextProvide)
    call()
}

infix fun <T> ContextLocal<T>.provides(
    local: State<T>
) = ContextProvide(this, local)

infix fun <T> ContextLocal<T>.provides(
    local: T
) = ContextProvide(this, readonlyStateOf(local))

class ContextProvide<T>(
    val context: ContextLocal<T>,
    val local: State<T>
)

