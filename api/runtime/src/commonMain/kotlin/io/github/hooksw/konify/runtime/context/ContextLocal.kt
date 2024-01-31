package io.github.hooksw.konify.runtime.context

import io.github.hooksw.konify.runtime.annotation.Component
import io.github.hooksw.konify.runtime.signal.CurrentNode
import io.github.hooksw.konify.runtime.signal.Signal
import io.github.hooksw.konify.runtime.signal.constantOf
import io.github.hooksw.konify.runtime.signal.memo

// -------- ContextLocal --------

interface ContextLocal<T> {
    val default: T
    val isStatic: Boolean
}

fun <T> useContext(local: ContextLocal<T>): Signal<T> {
    val node = CurrentNode!!
    return node.getContextLocal(local) ?: constantOf { local.default }
}

fun <T> contextLocalOf(defaultProvider: () -> T): ContextLocal<T> {
    return DefaultContextLocal(defaultProvider)
}

fun <T> staticContextLocalOf(defaultProvider: () -> T): ContextLocal<T> {
    return DefaultContextLocal(defaultProvider, true)
}

// -------- ProvidedViewLocal --------

infix fun <T> ContextLocal<T>.provides(value: () -> T): ProvidedViewLocal<T> {
    return ProvidedViewLocal(
        contextLocal = this,
        getValue = if (isStatic) constantOf(value) else memo(getValue = value)
    )
}

@Component
fun ContextLocalProvider(
    vararg locals: ProvidedViewLocal<*>,
    block: () -> Unit
) {
    val node = CurrentNode!!
    for (local in locals) {
        node.provideContextLocal(local)
    }
    block()
}

class ProvidedViewLocal<T>(
    val contextLocal: ContextLocal<T>,
    val getValue: Signal<T>
)

// -------- Internal --------

internal class DefaultContextLocal<T>(
    defaultProvider: () -> T,
    override val isStatic: Boolean = false
) : ContextLocal<T> {

    override val default: T by lazy { defaultProvider() }

}