package io.github.hooksw.konify.runtime.reactive

import io.github.hooksw.konify.runtime.utils.*
import io.github.hooksw.konify.runtime.utils.Lock
import io.github.hooksw.konify.runtime.utils.isMainThread
import io.github.hooksw.konify.runtime.utils.post2MainThread

internal class ObservedSignal<T>(
    initialValue: T,
    private val equality: Equality<T> = structuralEquality()
) : MutableSignal<T>, StateObserver {

    override val observers: MutableList<Computation> = mutableListOf()
    private val lock = Lock()
    override var value: T = initialValue
        get() {
            if (isMainThread()) {
                currentReactiveSystem?.apply {
                    autoTrack()
                }
            }
            return lock.read { field }

        }
        set(value) {
            lock.write {
                if (equality.compare(field, value)) {
                    return
                }
                field = value
            }
            if(isMainThread()){
                dispatchUpdate()
            }else{
                post2MainThread { dispatchUpdate() }
            }
        }

    private fun dispatchUpdate() {
        observers.fastForEach {
            pushUpdate(it)
        }
    }
}
