package io.github.hooksw.konify.runtime.signal

import io.github.hooksw.konify.runtime.annotation.InternalUse
import io.github.hooksw.konify.runtime.utils.*
import io.github.hooksw.konify.runtime.utils.Lock
import io.github.hooksw.konify.runtime.utils.isMainThread
import io.github.hooksw.konify.runtime.utils.post2MainThread

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
            if(isMainThread()){
                autoTrack()
                runWithComputations(this) {
                    if (_field === NOT_INITIAL) value = getValue()
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
            if(isMainThread()){
                dispatchUpdate()
            }else{
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
    return Memo(equality, getValue).apply {
        val node = CurrentNode
        node?.addComputations(this)
    }
}