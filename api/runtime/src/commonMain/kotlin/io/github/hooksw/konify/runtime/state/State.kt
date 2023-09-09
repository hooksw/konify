package io.github.hooksw.konify.runtime.state

import kotlin.reflect.KProperty

interface State<T> {
    val value: T

    fun bind(observer: (T)->Unit)
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
