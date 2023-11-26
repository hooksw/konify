package io.github.hooksw.konify.runtime.signal

import io.github.hooksw.konify.runtime.utils.assertOnMainThread

private object NOT_INITIAL

internal class Memo<T>(
    private val equality: Equality<T>,
    private val getValue: () -> T
) : Signal<T>, Computation(), StateObserver {
    override val observers: MutableList<Computation> = ArrayList(5)
    private var _field: T = NOT_INITIAL as T
    override var value: T
        get() {
            assertOnMainThread()
            autoTrack()

            runWithComputations(this) {
                if (_field === NOT_INITIAL) value = getValue()
            }

            return _field
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

    override fun run() {
        assertOnMainThread()
        cleanNode()
        value = getValue()

    }
}

fun <T> memo(equality: Equality<T> = structuralEquality(), getValue: () -> T): Signal<T> {
    return Memo(equality, getValue).apply {
        val node = CurrentNode
        node?.addComputations(this)
    }
}