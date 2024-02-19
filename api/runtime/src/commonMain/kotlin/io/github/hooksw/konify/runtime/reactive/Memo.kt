package io.github.hooksw.konify.runtime.reactive

import io.github.hooksw.konify.runtime.utils.*

private object NOT_INITIAL

internal class Memo<T>(
    private val equality: Equality<T>,
    private val getValue: () -> T
) : Signal<T>, Computation(), StateObserver {


    override val observers: MutableList<Computation> = ArrayList(5)
    private var _field: T = NOT_INITIAL as T
    private val lock = Lock()
    override var value: T
        get() {
            if (isMainThread()) {
                currentReactiveSystem?.apply {
                    autoTrack()
                    runWithComputations(this@Memo) {
                        if (_field === NOT_INITIAL) value = getValue()
                    }
                }
            }

            return lock.read {
                _field
            }
        }
        private set(value) {
            lock.write {
                if (equality.compare(value, _field)) {
                    return
                }
                _field = value
            }
            if (isMainThread()) {
                dispatchUpdate()
            } else {
                post2MainThread { dispatchUpdate() }
            }
        }

    override fun run() {
        assertOnMainThread()
        cleanNode()
        value = getValue()

    }

    private fun dispatchUpdate() {
        observers.fastForEach {
            pushUpdate(it)
        }
    }
}

fun <T> memo(equality: Equality<T> = structuralEquality(), getValue: () -> T): Signal<T> {
    return Memo(equality, getValue)
}