package io.github.hooksw.konify.runtime.node

import androidx.collection.MutableScatterMap
import androidx.collection.mutableScatterMapOf
import io.github.hooksw.konify.runtime.local.ProvidedViewLocal
import io.github.hooksw.konify.runtime.local.ContextLocal
import io.github.hooksw.konify.runtime.signal.*
import io.github.hooksw.konify.runtime.utils.fastForEach
import kotlin.jvm.JvmField

internal class Node {

    private enum class LifecycleState {
        Initial,
        Prepared,
        Inactive,
        Disposed
    }

    private var state: LifecycleState = LifecycleState.Initial

    @JvmField
    var parentNode: Node? = null

    @JvmField
    var childNodes: MutableList<Node>? = null

    @JvmField
    protected var onMount: MutableList<() -> Unit>? = null

    @JvmField
    protected var onDispose: MutableList<() -> Unit>? = null

    @JvmField
    internal var computations: MutableList<Computation>? = null

    @JvmField
    protected var contextMap: MutableScatterMap<ContextLocal<*>, Any>? = null

    internal fun useChildNodes(): MutableList<Node> {
        if (childNodes == null) childNodes = mutableListOf()
        return childNodes!!
    }

    internal fun addOnMount(fn: () -> Unit) {
        if (state != LifecycleState.Initial) {
            error("callbacks should be added before initial")
        }
        if (onMount == null) onMount = ArrayList(3)
        onMount!!.add(fn)
    }

    internal fun addOnDispose(fn: () -> Unit) {
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

    @Suppress("UNCHECKED_CAST")
    fun <T> getContextLocal(contextLocal: ContextLocal<T>): Signal<T>? {
        val current = contextMap?.get(contextLocal)
            ?: parentNode?.getContextLocal(contextLocal)
            ?: return null
        return current as? Signal<T>
    }

    fun provideContextLocal(providedViewLocal: ProvidedViewLocal<*>) {
        if (contextMap == null) contextMap = mutableScatterMapOf()
        contextMap!![providedViewLocal.contextLocal] = providedViewLocal.signal
    }


    fun onCreate(parent: Node) {
        parentNode = parent
        parent.useChildNodes().add(this)
    }

    fun onPrepare() {
        if (state == LifecycleState.Prepared) {
            error("This ViewNode is already prepared.")
        }
        onMount?.fastForEach { it() }
        state = LifecycleState.Prepared
    }

    fun cache() {
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

    fun reuse() {
        if (state != LifecycleState.Inactive) {
            error("This ViewNode is not Inactive.")
        }
        computations?.fastForEach { comp ->
            comp.run()
        }
        state = LifecycleState.Prepared
    }

    fun cleanup() {
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
        childNodes?.fastForEach {
            it.cleanup()
        }
    }
}

fun createNode(block: () -> Unit) {
    val parent = CurrentNode!!
    val node = Node()
    CurrentNode = node
    node.onCreate(parent)
    block()
    node.onPrepare()
    CurrentNode = parent

}