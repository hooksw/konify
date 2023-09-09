package io.github.hooksw.konify.runtime.state

import io.github.hooksw.konify.runtime.annotation.ReadOnlyView
import io.github.hooksw.konify.runtime.currentViewNode
import kotlin.reflect.KProperty

interface IntState : State<Int> {
    val intValue: Int

    @Deprecated(
        message = "Use intValue to avoid boxing.",
        replaceWith = ReplaceWith("intValue")
    )
    override val value: Int
        get() = intValue
}

interface MutableIntState : IntState, MutableState<Int> {
    override var intValue: Int

    @Deprecated(
        message = "Use intValue to avoid boxing.",
        replaceWith = ReplaceWith("intValue")
    )
    override var value: Int
        get() = intValue
        set(value) {
            intValue = value
        }
}

fun mutableIntStateOf(
    initialValue: Int = 0,
    equality: Equality<Int> = structuralEquality()
): MutableIntState {
    return ObservedIntState(
        initialValue = initialValue,
        equality = equality
    )
}

operator fun IntState.getValue(thisRef: Any?, property: KProperty<*>): Int {
    return intValue
}

operator fun MutableIntState.setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
    intValue = value
}

@ReadOnlyView
fun IntState.bindWithLifecycle(observer: Observer<Int>) {
    bind(observer)
    currentViewNode.onDispose {
        unbind(observer)
    }
}
