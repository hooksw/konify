package com.example.ui.runtime.node

import com.example.ui.runtime.node.InternalViewNode.LifecycleState.Disposed
import com.example.ui.runtime.node.InternalViewNode.LifecycleState.Initial
import com.example.ui.runtime.node.InternalViewNode.LifecycleState.Prepared
import com.example.ui.runtime.platform.PlatformView
import com.example.ui.runtime.state.State

internal class InternalViewNode : ViewNode {
    // -------- Hierarchy --------

    private var parent: InternalViewNode? = null

    private val children: MutableList<InternalViewNode> = ArrayList(4)

    override fun createChildNode(): InternalViewNode {
        val child = InternalViewNode()
        children.add(child)
        child.parent = this
        return child
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
        state = Prepared
    }

    fun dispose() {
        state = Disposed
    }

    override fun onPrepared(block: () -> Unit) {
        if (block in callbacksOnPrepared) {
            return
        }
        callbacksOnPrepared.add(block)
    }

    override fun onDispose(block: () -> Unit) {
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
