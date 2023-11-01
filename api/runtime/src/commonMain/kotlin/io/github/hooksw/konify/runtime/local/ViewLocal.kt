package io.github.hooksw.konify.runtime.local

import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.signal.Signal
import io.github.hooksw.konify.runtime.signal.constant
import io.github.hooksw.konify.runtime.signal.signalOf

// -------- ViewLocal --------

interface ViewLocal<T> {

    val ViewNode.current: Signal<T>
}

fun <T> viewLocalOf(defaultProvider: () -> T): ViewLocal<T> {
    return DefaultViewLocal(defaultProvider)
}

// -------- ProvidedViewLocal --------

infix fun <T> ViewLocal<T>.provides(value: T): ProvidedViewLocal<T> {
    return ProvidedViewLocal(
        viewLocal = this,
        signal = signalOf(value)
    )
}

infix fun <T> ViewLocal<T>.provides(signal: Signal<T>): ProvidedViewLocal<T> {
    return ProvidedViewLocal(
        viewLocal = this,
        signal = signal
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
    val signal: Signal<T>
)

// -------- Internal --------

@PublishedApi
internal class DefaultViewLocal<T>(private val defaultProvider: () -> T) : ViewLocal<T> {

    internal val default: Signal<T> by lazy { constant(defaultProvider()) }

    override val ViewNode.current: Signal<T>
        get() = getViewLocal(this@DefaultViewLocal) ?: default
}
