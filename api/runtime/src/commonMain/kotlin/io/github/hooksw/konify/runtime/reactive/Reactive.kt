package io.github.hooksw.konify.runtime.reactive

import io.github.hooksw.konify.runtime.node.Node
import io.github.hooksw.konify.runtime.node.ReusableNode
import io.github.hooksw.konify.runtime.utils.assertOnMainThread
import io.github.hooksw.konify.runtime.utils.fastForEach


interface StateObserver {
    val observers: MutableList<Computation>
}

abstract class Computation {
    open val sources: MutableList<StateObserver> = ArrayList(3)
    abstract fun run()
}

internal fun Computation.cleanNode() {
    sources.fastForEach {
        it.observers.remove(this)
    }
    sources.clear()
}

class ReactiveSystem {
    var currentNode: Node? = null
    var currentListener: Computation? = null


    internal fun createComputation(call: () -> Unit): Computation {
        val listener = object : Computation() {

            override fun run() {
                cleanNode()
                runWithComputations(this) {
                    call()
                }
            }
        }

        return listener
    }

    internal fun runWithComputations(computation: Computation, run: () -> Unit) {
        val listener = currentListener
        currentListener = computation
        run()
        currentListener = listener
    }

    inline fun <T> untrack(fn: () -> T): T {
        assertOnMainThread()
        val listener = currentListener
        currentListener = null
        val r = fn()
        currentListener = listener
        return r
    }

    fun StateObserver.autoTrack() {
        val listener = currentListener
        if (listener != null) {
            observers.add(listener)
            listener.sources.add(this)
        }
    }

}

val currentReactiveSystem: ReactiveSystem?
    get() = reactiveSystemGetter?.invoke()

private var reactiveSystemGetter: (() -> ReactiveSystem)? = null
fun initReactiveSystemGetter(call: () -> ReactiveSystem) {
    if (reactiveSystemGetter == null) reactiveSystemGetter = call
    else error("")
}