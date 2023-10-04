package io.github.hooksw.konify.runtime.state

import io.github.hooksw.konify.runtime.node.ViewNode
import kotlin.reflect.KProperty

interface State<out T> {
    val value: T

    fun bind(observer: (T)->Unit)

    fun unbind(observer: (T)->Unit)
}

interface MutableState<T> : State<T> {
    override var value: T
}

fun <T> mutableStateOf(
    initialValue: T,
    equality: Equality<T> = structuralEquality()
): MutableState<T> {
    return ObservedState(
        initialValue = initialValue,
        equality = equality
    )
}

operator fun <T> State<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
    return value
}

operator fun <T> MutableState<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this.value = value
}

fun <T> State<T>.bindWithLifecycle(node: ViewNode,observer: (T)->Unit) {
    bind(observer)
    node.registerDisposed {
        unbind(observer)
    }
}
