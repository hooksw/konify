package io.github.hooksw.konify.runtime.node

import io.github.hooksw.konify.runtime.local.ProvidedViewLocal
import io.github.hooksw.konify.runtime.local.ViewLocal
import io.github.hooksw.konify.runtime.signal.Signal

sealed interface ViewNode {
    // -------- Hierarchy --------

    fun createChild(): ViewNode

    fun insertNode(node: ViewNode, index: Int)
    fun detachNodeAt(index: Int):ViewNode

    // -------- Lifecycle --------

    //for node reuse
    fun detach()

    fun prepare()

    fun release()

    fun registerPrepared(block: () -> Unit)

    fun registerDisposed(block: () -> Unit)

    // -------- ViewLocal --------

    fun <T> getViewLocal(viewLocal: ViewLocal<T>): Signal<T>?

    fun provideViewLocal(providedViewLocal: ProvidedViewLocal<*>)
}
