package io.github.hooksw.konify.runtime.signal

import androidx.collection.MutableScatterMap
import androidx.collection.MutableScatterSet
import androidx.collection.mutableScatterSetOf
import io.github.hooksw.konify.runtime.utils.UnitCallBack

internal class DerivedSignal<T>(
    private val equality: Equality<T>,
    private val getValue: () -> T
) : Signal<T>, Computation<T>, StateObserver {
    override val observers: MutableList<UnitCallBack> = mutableListOf()
    private var _field: T? = null
    override var value: T
        get() {
            val owner = Owners.lastOrNull()
            val listener = Listeners.lastOrNull()
            if (owner != null && listener != null) {
                observers.add(listener)
                owner.stateDisposerMap[this] = listener
            }
            Owners.add(this)
            if (_field == null) fn()
            removeLastSelf()
            return _field!!
        }
        private set(value) {
            if (equality.compare(value, _field)) {
                return
            }
            _field = value
            observers.forEach {
                pushUpdate(it)
            }
        }
    override fun fn() {
        val newV = getValue()
        value = newV
    }
    override val stateDisposerMap: MutableScatterMap<StateObserver, UnitCallBack> = MutableScatterMap(2)
}

fun <T> derived(equality: Equality<T> = structuralEquality(), function: () -> T): Signal<T> {
    return DerivedSignal(equality, function)
}