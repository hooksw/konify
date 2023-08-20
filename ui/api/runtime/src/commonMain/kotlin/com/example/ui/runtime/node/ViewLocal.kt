package com.example.ui.runtime.node

import com.example.ui.runtime.annotation.ReadOnlyView
import com.example.ui.runtime.currentViewNode
import com.example.ui.runtime.state.State
import com.example.ui.runtime.state.mutableStateOf

// -------- ViewLocal --------

interface ViewLocal<T> {
    val default: State<T>

    val current: State<T>
        @ReadOnlyView
        get() = currentViewNode.getViewLocal(this) ?: default
}

inline fun <T> viewLocalOf(defaultProvider: () -> T): ViewLocal<T> {
    return DefaultViewLocal(defaultProvider())
}

// -------- ProvidedViewLocal --------

infix fun <T> ViewLocal<T>.provides(value: T): ProvidedViewLocal<T> {
    return ProvidedViewLocal(
        viewLocal = this,
        state = mutableStateOf(value)
    )
}

infix fun <T> ViewLocal<T>.provides(state: State<T>): ProvidedViewLocal<T> {
    return ProvidedViewLocal(
        viewLocal = this,
        state = state
    )
}

@ReadOnlyView
fun ViewLocalProvider(
    vararg locals: ProvidedViewLocal<*>,
    block: @ReadOnlyView () -> Unit
) {
    val node = currentViewNode
    for (local in locals) {
        node.provideViewLocal(local)
    }
    block()
}

class ProvidedViewLocal<T>(
    val viewLocal: ViewLocal<T>,
    val state: State<T>
)

// -------- Internal --------

@PublishedApi
internal class DefaultViewLocal<T>(defaultProvider: T) : ViewLocal<T> {
    override val default: State<T> = mutableStateOf(defaultProvider)
}
