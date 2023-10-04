package io.github.hooksw.konify.runtime.node

import io.github.hooksw.konify.runtime.local.ProvidedViewLocal
import io.github.hooksw.konify.runtime.local.ViewLocal
import io.github.hooksw.konify.runtime.state.State

sealed interface ViewNode {
    // -------- Hierarchy --------

    fun createChild(): ViewNode

    fun insertNodeTo(node: ViewNode, index: Int = -1)
    fun removeNodeAt(index: Int)

    // -------- Lifecycle --------

    //for node reuse
    fun detach()

    fun prepare()

    fun pause()

    fun resume()

    fun release()

    fun registerPrepared(block: () -> Unit)

    fun registerDisposed(block: () -> Unit)

    // -------- ViewLocal --------

    fun <T> getViewLocal(viewLocal: ViewLocal<T>): State<T>?

    fun provideViewLocal(providedViewLocal: ProvidedViewLocal<*>)
}
