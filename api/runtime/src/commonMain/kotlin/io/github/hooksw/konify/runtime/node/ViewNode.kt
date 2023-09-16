package io.github.hooksw.konify.runtime.node

import io.github.hooksw.konify.runtime.platform.PlatformView
import io.github.hooksw.konify.runtime.state.State

sealed interface ViewNode {
    // -------- Hierarchy --------

    fun createChild(): ViewNode
    val children:List<ViewNode>
    fun addNode(node: ViewNode)
    fun removeAllChildren()
    fun detachChildren()
    fun pauseAllChildren()
    fun resumeAllChildren()

    // -------- Platform --------

    fun registerPlatformView(platformView: PlatformView)

    // -------- Lifecycle --------

    fun prepare()

    fun onPrepared(block: () -> Unit)

    fun onDispose(block: () -> Unit)

    // -------- ViewLocal --------

    fun <T> getViewLocal(viewLocal: ViewLocal<T>): State<T>?

    fun provideViewLocal(providedViewLocal: ProvidedViewLocal<*>)
}
