package io.github.hooksw.konify.runtime.signal

import androidx.collection.MutableScatterMap
import androidx.collection.mutableScatterSetOf
import io.github.hooksw.konify.runtime.utils.UnitCallBack
import kotlin.jvm.JvmField


internal interface StateObserver {
    val observers: MutableList<UnitCallBack>
}

internal interface Owner {
    val stateDisposerMap: MutableScatterMap<StateObserver, UnitCallBack>

    private fun dispose(stateObserver: StateObserver, listener: UnitCallBack) {
        stateObserver.observers.remove(listener)
        if (stateObserver is Owner && stateObserver.observers.isEmpty()) {
            stateObserver.stateDisposerMap.forEach { obs, lis ->
                dispose(obs, lis)
            }
            stateObserver.stateDisposerMap.clear()
        }
    }

    fun disposeObservers() {
        stateDisposerMap.forEach { observer, listener ->
            dispose(observer, listener)
        }
    }
}

internal interface Computation<T> : Owner {
    fun fn()
}

@JvmField
internal val Updates = mutableScatterSetOf<UnitCallBack>()

@JvmField
internal var batching = false
internal fun pushUpdate(call: UnitCallBack) {
    if (batching) {
        Updates.add(call)
    } else {
        call()
    }
}

internal inline fun batch(call: () -> Unit) {
    batching = true
    call()
    Updates.forEach {
        it()
    }
    Updates.clear()
    batching = true
}

@JvmField
internal val Owners = mutableListOf<Owner>()
internal fun Owner.removeLastSelf() {
    if(Owners.isNotEmpty()&& Owners.last()==this){
        Owners.removeLast()
    }
}

@JvmField
internal val Listeners = mutableListOf<UnitCallBack>()

internal inline fun signalConsume(crossinline call: () -> Unit) {
    val listener = object : UnitCallBack {
        override fun invoke() {
            Listeners.add(this)
            call()
            Listeners.removeLast()
        }
    }
    listener()
}

