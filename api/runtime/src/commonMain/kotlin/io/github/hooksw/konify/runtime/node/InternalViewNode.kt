package io.github.hooksw.konify.runtime.node

import io.github.hooksw.konify.runtime.node.InternalViewNode.LifecycleState.*
import io.github.hooksw.konify.runtime.platform.PlatformView
import io.github.hooksw.konify.runtime.state.State
import io.github.hooksw.konify.runtime.utils.fastForEach

internal class InternalViewNode : ViewNode {
    // -------- Hierarchy --------

    private var parent: InternalViewNode? = null

    private val _children: MutableList<InternalViewNode> = ArrayList(4)

    override val children: List<ViewNode>
        get() = ArrayList(_children)

    override fun createChild(): InternalViewNode {
        val child = InternalViewNode()
        _children.add(child)
        child.parent = this
        return child
    }

    override fun addNode(node: ViewNode) {
        _children.add(node as InternalViewNode)
        node.parent = this
        node.registerLatestPlatformView()
        node.prepare()
    }

    private fun registerLatestPlatformView() {
        if (platformView == null) {
            _children.fastForEach {
                it.registerLatestPlatformView()
            }
        } else {
            val parentPlatformView=findParentPlatformView()!!
            parentPlatformView.appendChild(platformView!!)
        }
    }


    override fun removeAllChildren() {
        _children.fastForEach { it.disposeRecursion(true, true) }
        _children.clear()
    }

    override fun detachChildren() {
        _children.fastForEach { it.disposeRecursion(true, false) }
        _children.clear()
    }


    override fun pauseAllChildren() {
        _children.fastForEach {
            if (it.state == Prepared) it.pause()
        }
    }

    override fun resumeAllChildren() {
        _children.fastForEach {
            if (it.state == Paused) it.prepare()
        }
    }

    // -------- Platform --------

    private var platformView: PlatformView? = null

    override fun registerPlatformView(platformView: PlatformView) {
        this.platformView = platformView
        val parent = findParentPlatformView()
        parent?.appendChild(platformView)
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
        callbacksOnPrepared.fastForEach { it.invoke() }
//        callbacksOnPrepared.clear()
        state = Prepared
    }

    private fun disposeRecursion(detachPlatformView: Boolean, release: Boolean) {
        if (state == Disposed) {
            error("This ViewNode is already disposed.")
        }
        _children.fastForEach { it.disposeRecursion(platformView == null && detachPlatformView, release) }
        if (detachPlatformView) {
            val platformView = platformView
            if (platformView != null) {
                findParentPlatformView()?.removeChild(platformView)
            }
        }
        callbacksOnDisposed.fastForEach { it.invoke() }
        if (release) {
            this.platformView = null
            parent = null
            callbacksOnDisposed.clear()
            callbacksOnPrepared.clear()
            providedViewLocals.clear()
        }
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

    private fun pause() {
        if (state != Prepared) {
            error("This ViewNode should be prepared.")
        }
        callbacksOnDisposed.fastForEach { it.invoke() }
        pauseAllChildren()
        state = Paused
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
