package io.github.hooksw.konify.runtime.signal

import io.github.hooksw.konify.runtime.utils.assertOnMainThread
import io.github.hooksw.konify.runtime.utils.fastForEach

internal class ObservedSignal<T>(
    initialValue: T,
    private val equality: Equality<T> = structuralEquality()
) : MutableSignal<T>, StateObserver {

    override val observers: MutableList<Computation> = mutableListOf()

    override var value: T = initialValue
        get() {
            assertOnMainThread()
            autoTrack()
            return field
        }
        set(value) {
            assertOnMainThread()
            if (equality.compare(field, value)) {
                return
            }
            field = value
            dispatchUpdate()
        }

    private fun dispatchUpdate() {
        observers.fastForEach {
            pushUpdate(it)
        }
    }
}
