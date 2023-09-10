package io.github.hooksw.konify.runtime.node

import io.github.hooksw.konify.runtime.node.InternalViewNode.LifecycleState.*
import io.github.hooksw.konify.runtime.platform.PlatformView
import io.github.hooksw.konify.runtime.state.State

internal class InternalViewNode : ViewNode {
    // -------- Hierarchy --------

    private var parent: InternalViewNode? = null

    private val children: MutableList<InternalViewNode> = ArrayList(4)

    override fun createChild(): InternalViewNode {
        val child = InternalViewNode()
        children.add(child)
        child.parent = this
        return child
    }

    override fun removeAllChildren() {
        children.forEach { it.dispose() }
        children.clear()
    }

    override fun pauseAllChildren() {
        children.forEach {
            if (it.state != Paused) it.dispose()
        }
    }

    override fun resumeAllChildren() {
        children.forEach { it.prepare() }
    }

    // -------- Platform --------

    private var platformView: PlatformView? = null

    override fun registerPlatformView(platformView: PlatformView) {
        this.platformView = platformView
        val parent = findParentPlatformView()
        parent?.addChild(platformView)
    }

    private fun findParentPlatformView(): PlatformView? {
        val parent = parent ?: return null
        return parent.platformView ?: parent.findParentPlatformView()
    }

    // -------- Lifecycle --------

    private var state: LifecycleState = Initial

    private enum class LifecycleState {
        Initial,
        Prepared,
        Paused,
        Disposed
    }

    private val callbacksOnPrepared: MutableList<() -> Unit> = ArrayList(2)

    private val callbacksOnDisposed: MutableList<() -> Unit> = ArrayList(2)

    override fun prepare() {
        if (state == Prepared) {
            error("This ViewNode is already prepared.")
        }
        if (state == Disposed) {
            error("This ViewNode has been disposed.")
        }
        callbacksOnPrepared.forEach { it.invoke() }
//        callbacksOnPrepared.clear()
        state = Prepared
    }

    private fun dispose() {
        if (state == Disposed) {
            error("This ViewNode is already disposed.")
        }
        providedViewLocals.clear()
        removeAllChildren()
        val platformView = platformView
        if (platformView != null) {
            findParentPlatformView()?.removeChild(platformView)
            this.platformView = null
        }
        parent = null
        callbacksOnDisposed.forEach { it.invoke() }
        callbacksOnDisposed.clear()
        callbacksOnPrepared.clear()
        state = Disposed
    }

    override fun onPrepared(block: () -> Unit) {
        if (state != Initial) {
            error("Cannot schedule callback after prepared.")
        }
        if (block in callbacksOnPrepared) {
            return
        }
        callbacksOnPrepared.add(block)
    }

    fun pause() {
        if (state != Prepared) {
            error("This ViewNode should be prepared.")
        }
        pauseAllChildren()
        callbacksOnDisposed.forEach { it.invoke() }
        state = Paused
    }

    fun resume() {
        if (state != Paused) {
            error("This ViewNode should be paused.")
        }
        resumeAllChildren()
        callbacksOnPrepared.forEach { it.invoke() }
        state = Prepared
    }

    override fun onDispose(block: () -> Unit) {
        if (state == Disposed) {
            error("Cannot schedule callback after disposed.")
        }
        if (block in callbacksOnDisposed) {
            return
        }
        callbacksOnDisposed.add(block)
    }

    // -------- ViewLocal --------

    private val providedViewLocals: MutableMap<ViewLocal<*>, State<*>> = mutableMapOf()

    override fun <T> getViewLocal(viewLocal: ViewLocal<T>): State<T>? {
        val current = providedViewLocals[viewLocal]
            ?: parent?.getViewLocal(viewLocal)
            ?: return null
        @Suppress("UNCHECKED_CAST")
        return current as? State<T>
    }

    override fun provideViewLocal(providedViewLocal: ProvidedViewLocal<*>) {
        providedViewLocals[providedViewLocal.viewLocal] = providedViewLocal.state
    }
}
