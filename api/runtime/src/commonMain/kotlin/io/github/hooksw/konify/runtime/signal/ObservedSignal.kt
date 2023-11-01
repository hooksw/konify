package io.github.hooksw.konify.runtime.signal

import androidx.collection.MutableScatterSet
import androidx.collection.mutableScatterSetOf
import io.github.hooksw.konify.runtime.utils.UnitCallBack

internal class ObservedSignal<T>(
    initialValue: T,
    private val equality: Equality<T> = structuralEquality()
) : MutableSignal<T>,StateObserver {

    override val observers: MutableScatterSet<UnitCallBack> = mutableScatterSetOf()

    override var value: T = initialValue
        get() {
            val owner = Owners.lastOrNull()
            val listener = Listeners.lastOrNull()
            if (owner != null && listener != null) {
                observers.add(listener)
                owner.stateDisposerMap[this]=listener
            }
            return field
        }
        set(value) {
            if (equality.compare(field, value)) {
                return
            }
            field = value
            dispatchUpdate()
        }

    private fun dispatchUpdate() {
        observers.forEach {
            pushUpdate(it)
        }
    }
}
