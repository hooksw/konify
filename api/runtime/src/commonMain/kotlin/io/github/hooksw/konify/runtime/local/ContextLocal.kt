package io.github.hooksw.konify.runtime.local

import io.github.hooksw.konify.runtime.annotation.Component
import io.github.hooksw.konify.runtime.node.Node
import io.github.hooksw.konify.runtime.signal.Nodes
import io.github.hooksw.konify.runtime.signal.signalOf

// -------- ViewLocal --------

interface ContextLocal<T> {

    val current: T
}

fun <T> contextLocalOf(defaultProvider: () -> T): ContextLocal<T> {
    return DefaultContextLocal(defaultProvider)
}
fun <T> staticContextLocalOf(defaultProvider: () -> T): ContextLocal<T> {
    return DefaultContextLocal(defaultProvider)
}

// -------- ProvidedViewLocal --------
@Component
infix fun <T> ContextLocal<T>.provides(value: T): ProvidedViewLocal<T> {
    val node= Node
    return ProvidedViewLocal(
        contextLocal = this,
        signal = signalOf(value)
    )
}


fun Node.ContextLocalProvider(
    vararg locals: ProvidedViewLocal<*>,
    block: Node.() -> Unit
) {
    for (local in locals) {
        provideContextLocal(local)
    }
    block()
}

class ProvidedViewLocal<T>(
    val contextLocal: ContextLocal<T>,
    val signal: ()->T
)

// -------- Internal --------

@PublishedApi
internal class DefaultContextLocal<T>(private val defaultProvider: () -> T) : ContextLocal<T> {

    internal val default: T by lazy { defaultProvider()   }

    override val current: T
        get() =  ?: default
}