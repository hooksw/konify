package io.github.hooksw.konify.runtime.signal

import io.github.hooksw.konify.runtime.annotation.InternalUse
import io.github.hooksw.konify.runtime.node.Node
import io.github.hooksw.konify.runtime.utils.assertOnMainThread
import io.github.hooksw.konify.runtime.utils.fastForEach
import kotlin.jvm.JvmField


internal interface StateObserver {
    val observers: MutableList<Computation>
}

internal fun StateObserver.autoTrack() {
    val listener = CurrentListener
    if (listener != null) {
        observers.add(listener)
        listener.sources.add(this)
    }
}

internal abstract class Computation {
    open val sources: MutableList<StateObserver> = ArrayList(3)
    abstract fun run()
}

internal fun Computation.cleanNode() {
    sources.fastForEach {
        it.observers.remove(this)
    }
    sources.clear()
}
//listeners

private val Updates = mutableListOf<Computation>()

private var batching = false
internal fun pushUpdate(call: Computation) {
    if (batching) {
        Updates.add(call)
    } else {
        call.run()
    }
}

internal inline fun batch(call: () -> Unit) {
    batching = true
    call()
    Updates.fastForEach {
        it.run()
    }
    Updates.clear()
    batching = false
}


@JvmField
internal var CurrentNode: Node? = null

@JvmField
@PublishedApi
internal var CurrentListener: Computation? = null


internal fun createComputation(call: () -> Unit): Computation {
    val listener = object : Computation() {

        override fun run() {
            cleanNode()
            runWithComputations(this) {
                call()
            }
        }
    }
    val node = CurrentNode
    node?.addComputations(listener)
    return listener
}
@InternalUse
fun bind(call: () -> Unit){
    createComputation(call).run()
}
internal fun runWithComputations(computation: Computation, run: () -> Unit) {
    val listener = CurrentListener
    CurrentListener = computation
    run()
    CurrentListener = listener
}

inline fun <T> untrack(fn: () -> T): T {
    assertOnMainThread()
    val listener = CurrentListener
    CurrentListener = null
    val r = fn()
    CurrentListener = listener
    return r
}
