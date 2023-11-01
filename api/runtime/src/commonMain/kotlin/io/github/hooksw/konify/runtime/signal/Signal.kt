package io.github.hooksw.konify.runtime.signal

import androidx.collection.MutableScatterMap
import androidx.collection.MutableScatterSet
import androidx.collection.mutableScatterSetOf
import io.github.hooksw.konify.runtime.utils.UnitCallBack
import kotlin.jvm.JvmField
import kotlin.reflect.KProperty

sealed interface SignalMarker

interface Signal<out T> : SignalMarker {
    val value: T
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }
}

interface MutableSignal<T> : Signal<T> {
    override var value: T

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

fun <T> signalOf(
    initialValue: T,
    equality: Equality<T> = structuralEquality()
): MutableSignal<T> {
    return ObservedSignal(
        initialValue = initialValue,
        equality = equality
    )
}
