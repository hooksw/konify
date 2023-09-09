package io.github.hooksw.konify.runtime.node

import io.github.hooksw.konify.runtime.state.State
import io.github.hooksw.konify.runtime.state.mutableStateOf
import io.github.hooksw.konify.runtime.state.staticStateOf

// -------- ViewLocal --------

interface ViewLocal<T> {

    val ViewNode.current: State<T>
}

fun <T> viewLocalOf(defaultProvider: () -> T): ViewLocal<T> {
    return DefaultViewLocal(defaultProvider)
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


fun ViewNode.ViewLocalProvider(
    vararg locals: ProvidedViewLocal<*>,
    block: ViewNode.() -> Unit
) {
    for (local in locals) {
        provideViewLocal(local)
    }
    block()
}

class ProvidedViewLocal<T>(
    val viewLocal: ViewLocal<T>,
    val state: State<T>
)

// -------- Internal --------

@PublishedApi
internal class DefaultViewLocal<T>(private val defaultProvider: () -> T) : ViewLocal<T> {

    internal val default: State<T> by lazy { staticStateOf(defaultProvider()) }

    override val ViewNode.current: State<T>
        get() = getViewLocal(this@DefaultViewLocal) ?: default
}
