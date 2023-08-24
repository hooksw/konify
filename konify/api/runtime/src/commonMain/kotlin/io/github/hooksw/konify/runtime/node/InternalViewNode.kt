package io.github.hooksw.konify.runtime.node

import io.github.hooksw.konify.runtime.node.InternalViewNode.LifecycleState.Disposed
import io.github.hooksw.konify.runtime.node.InternalViewNode.LifecycleState.Initial
import io.github.hooksw.konify.runtime.node.InternalViewNode.LifecycleState.Prepared
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
        Disposed
    }

    private val callbacksOnPrepared: MutableList<() -> Unit> = ArrayList(2)

    private val callbacksOnDisposed: MutableList<() -> Unit> = ArrayList(2)

    override fun prepare() {
        if (state >= Prepared) {
            error("This ViewNode is already prepared.")
        }
        callbacksOnPrepared.forEach { it.invoke() }
        callbacksOnPrepared.clear()
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
        state = Disposed
    }

    override fun onPrepared(block: () -> Unit) {
        if (state >= Prepared) {
            error("Cannot schedule callback after prepared.")
        }
        if (block in callbacksOnPrepared) {
            return
        }
        callbacksOnPrepared.add(block)
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
