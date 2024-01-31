package io.github.hooksw.konify.runtime.node

import androidx.collection.MutableScatterMap
import androidx.collection.mutableScatterMapOf
import io.github.hooksw.konify.runtime.annotation.InternalUse
import io.github.hooksw.konify.runtime.context.ContextLocal
import io.github.hooksw.konify.runtime.context.ProvidedViewLocal
import io.github.hooksw.konify.runtime.signal.Computation
import io.github.hooksw.konify.runtime.signal.CurrentNode
import io.github.hooksw.konify.runtime.signal.Signal
import io.github.hooksw.konify.runtime.utils.fastForEach
import kotlin.jvm.JvmField

@InternalUse
open class Node {
    abstract class Event

    enum class LifecycleState {
        Initial,
        Prepared,
        Inactive,
        Disposed
    }

    protected var state: LifecycleState = LifecycleState.Initial

    @JvmField
    protected var parentNode: Node? = null

    @JvmField
    protected var childNodes: MutableList<Node>? = null

    @JvmField
    protected var onMount: MutableList<() -> Unit>? = null

    @JvmField
    protected var onDispose: MutableList<() -> Unit>? = null

    @JvmField
    internal var computations: MutableList<Computation>? = null

    @JvmField
    protected var contextMap: MutableScatterMap<ContextLocal<*>, Signal<*>>? = null

    private fun useChildNodes(): MutableList<Node> {
        if (childNodes == null) childNodes = mutableListOf()
        return childNodes!!
    }

    fun addOnMount(fn: () -> Unit) {
        if (state != LifecycleState.Initial) {
            error("callbacks should be added before initial")
        }
        if (onMount == null) onMount = ArrayList(3)
        onMount!!.add(fn)
    }

    fun addOnDispose(fn: () -> Unit) {
        if (state != LifecycleState.Initial) {
            error("callbacks should be added before initial")
        }
        if (onDispose == null) onDispose = ArrayList(3)
        onDispose!!.add(fn)
    }

    internal fun addComputations(computation: Computation) {
        if (state != LifecycleState.Initial) {
            error("callbacks should be added before initial")
        }
        if (computations == null) computations = mutableListOf()
        computations!!.add(computation)
    }

    //context local
    @Suppress("UNCHECKED_CAST")
    fun <T> getContextLocal(contextLocal: ContextLocal<T>): Signal<T>? {
        val current = contextMap?.get(contextLocal)
            ?: parentNode?.getContextLocal<T>(contextLocal)
            ?: return null
        return current as? Signal<T>
    }

    fun provideContextLocal(providedViewLocal: ProvidedViewLocal<*>) {
        if (contextMap == null) contextMap = mutableScatterMapOf()
        contextMap!![providedViewLocal.contextLocal] = providedViewLocal.getValue
    }

    //lifecycle

    protected open fun onCreate(parent: Node) {
        parentNode = parent
        parent.useChildNodes().add(this)
    }

    fun create(parent: Node) {
        onCreate(parent)
    }

    protected open fun onPrepare() {
        if (state == LifecycleState.Prepared) {
            error("This ViewNode is already prepared.")
        }
        onMount?.fastForEach { it() }
        state = LifecycleState.Prepared
    }

    fun prepare() {
        onPrepare()
        childNodes?.fastForEach {
            it.prepare()
        }
    }

    open fun cache() {
        if (state != LifecycleState.Prepared) {
            error("This ViewNode is not prepared.")
        }
        computations?.fastForEach { comp ->
            comp.sources.fastForEach { obs ->
                obs.observers.remove(comp)
            }
        }
        state = LifecycleState.Inactive
    }

    open fun reuse() {
        if (state != LifecycleState.Inactive) {
            error("This ViewNode is not Inactive.")
        }
        computations?.fastForEach { comp ->
            comp.run()
        }
        state = LifecycleState.Prepared
    }

    protected open fun onCleanup() {
        if (state == LifecycleState.Disposed) {
            error("This ViewNode is already disposed.")
        }
        computations?.fastForEach { comp ->
            comp.sources.fastForEach { obs ->
                obs.observers.remove(comp)
            }
            comp.sources.clear()
        }
        computations = null
        onDispose?.fastForEach { it() }
        onDispose = null
        onMount = null
//        parentNode?.childNodes.remove(this)
        parentNode = null
        contextMap = null
        state = LifecycleState.Disposed
    }

    fun cleanUp() {
        onCleanup()
        childNodes?.fastForEach {
            it.cleanUp()
        }
    }


    fun releaseChildren() {
        childNodes?.fastForEach {
            it.cleanUp()
        }
        childNodes?.clear()
    }

    //event

    fun dispatchEvent(event: Event) {
        val consumed = onDispatchEvent(event)
        if (!consumed) {
            childNodes?.fastForEach {
                it.dispatchEvent(event)
            }
        }
    }

    protected open fun onDispatchEvent(event: Event): Boolean {
        return false
    }
}

@InternalUse
fun createNode(parent: Node = CurrentNode!!, block: (Node) -> Unit) {
    val node = Node()
    CurrentNode = node
    node.create(parent)
    block(node)
    node.prepare()
    CurrentNode = parent

}