package io.github.hooksw.konify.runtime.node

import androidx.collection.MutableScatterMap
import androidx.collection.mutableScatterMapOf
import io.github.hooksw.konify.runtime.context.ContextLocal
import io.github.hooksw.konify.runtime.context.ProvidedViewLocal
import io.github.hooksw.konify.runtime.reactive.Signal
import io.github.hooksw.konify.runtime.reactive.currentReactiveSystem
import io.github.hooksw.konify.runtime.utils.fastForEach

interface Node {
    fun addOnMount(fn: () -> Unit)
    fun addOnDispose(fn: () -> Unit)
    fun <T> getContextLocal(contextLocal: ContextLocal<T>): Signal<T>?
    fun provideContextLocal(providedViewLocal: ProvidedViewLocal<*>)
}

@PublishedApi
internal open class NodeImpl<T> constructor(val nodeValue: T) : Node {
    enum class LifecycleState {
        Initial, Prepared, Inactive, Disposed
    }

    protected var state: LifecycleState = LifecycleState.Initial


    private var parentNode: NodeImpl<*>? = null


    private var childNodes: MutableList<NodeImpl<*>>? = null


    private var onMount: MutableList<() -> Unit>? = null


    private var onDispose: MutableList<() -> Unit>? = null


    private var contextMap: MutableScatterMap<ContextLocal<*>, Signal<*>>? = null

    private fun useChildNodes(): MutableList<NodeImpl<*>> {
        if (childNodes == null) childNodes = mutableListOf()
        return childNodes!!
    }

    override fun addOnMount(fn: () -> Unit) {
        if (state != LifecycleState.Initial) {
            error("callbacks should be added before initial")
        }
        if (onMount == null) onMount = ArrayList(3)
        onMount!!.add(fn)
    }

    override fun addOnDispose(fn: () -> Unit) {
        if (state != LifecycleState.Initial) {
            error("callbacks should be added before initial")
        }
        if (onDispose == null) onDispose = ArrayList(3)
        onDispose!!.add(fn)
    }

    //context local
    @Suppress("UNCHECKED_CAST")
    override fun <T> getContextLocal(contextLocal: ContextLocal<T>): Signal<T>? {
        val current = contextMap?.get(contextLocal) ?: parentNode?.getContextLocal<T>(contextLocal)
        ?: return null
        return current as? Signal<T>
    }

    override fun provideContextLocal(providedViewLocal: ProvidedViewLocal<*>) {
        if (contextMap == null) contextMap = mutableScatterMapOf()
        contextMap!![providedViewLocal.contextLocal] = providedViewLocal.getValue
    }

    //lifecycle


    fun create(parent: NodeImpl<*>) {
        parentNode = parent
        parent.useChildNodes().add(this)
    }

    private fun onPrepare() {
        onMount?.fastForEach { it() }
        state = LifecycleState.Prepared
    }

    fun prepare() {
        if (state == LifecycleState.Prepared) {
            error("This ViewNode is already prepared.")
        }
        onPrepare()
        childNodes?.fastForEach {
            it.prepare()
        }
    }

    private fun onCleanup() {
        onDispose?.fastForEach { it() }
        onDispose = null
        onMount = null
//        parentNode?.childNodes.remove(this)
        parentNode = null
        contextMap = null
        state = LifecycleState.Disposed
    }

    fun cleanUp() {
        if (state == LifecycleState.Disposed) {
            error("This ViewNode is already disposed.")
        }
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

}

//todo factory:T ???
inline fun <T> createNode(factory: () -> T, initial: Node.(T) -> Unit) {
    currentReactiveSystem ?: error("")
    val parent = currentReactiveSystem!!.currentNode
    val value = factory()
    val node = NodeImpl(value)
    node.create(parent!! as NodeImpl<*>)
    currentReactiveSystem!!.currentNode = node
    node.initial(value)
    currentReactiveSystem!!.currentNode = parent
}