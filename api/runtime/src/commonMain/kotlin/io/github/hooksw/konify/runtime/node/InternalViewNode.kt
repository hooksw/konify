package io.github.hooksw.konify.runtime.node

import io.github.hooksw.konify.runtime.local.ProvidedViewLocal
import io.github.hooksw.konify.runtime.local.ViewLocal
import io.github.hooksw.konify.runtime.node.InternalViewNode.LifecycleState.*
import io.github.hooksw.konify.runtime.platform.PlatformView
import io.github.hooksw.konify.runtime.state.State
import io.github.hooksw.konify.runtime.utils.fastForEach
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/*
node生命周期和各种初始化流程
生命周期：
1. Initial:created, but not ready for use,we can do things like:
    register prepared callback
    register dispose callback
    register interceptor
2. Prepared:config all needed:
    call prepared callbacks
    inject interceptor
3. Paused,
    call dispose callbacks
4. Disposed
    if not paused , call dispose callbacks
    clear all register callbacks and interceptor
*/
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

    override fun insertNodeTo(node: ViewNode, index: Int) {
        children.add(index, node as InternalViewNode)
        node.parent = this
        node.prepareRecursion()
    }

    override fun removeNodeAt(index: Int) {

    }

    private fun prepareRecursion() {
        prepare()
        children.fastForEach {
            it.prepareRecursion()
        }
    }

    override fun release() {
        disposeRecursion(true, true)
        children.clear()
    }

    override fun detach() {
        disposeRecursion(true, false)
        children.clear()
    }


    override fun resume() {
        children.fastForEach {
            if (it.state == Paused) it.prepare()
        }
    }

    // -------- Platform --------

    private var platformView: PlatformView? = null
    private var parentPlatformView: PlatformView? = null

    val isAttachPlatformView
        get() = platformView != null


    fun registerPlatformView(platformView: PlatformView) {
        this.platformView = platformView
        findParentPlatformView().addView(platformView)
    }

    private fun findParentPlatformView(): PlatformView {
        val parent = parent ?: error("node should be added first.")
        return parent.platformView ?: parent.parentPlatformView ?: error("parentPlatformView mustn't be null")
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
        if (platformView != null && parentPlatformView == null) {
            parentPlatformView = findParentPlatformView()
            parentPlatformView!!.addView(platformView!!)
        }
        callbacksOnPrepared.fastForEach { it.invoke() }
//        callbacksOnPrepared.clear()
        state = Prepared
    }


    private fun disposeRecursion(detachPlatformView: Boolean, release: Boolean) {
        if (state == Disposed) {
            error("This ViewNode is already disposed.")
        }
        children.fastForEach { it.disposeRecursion(platformView == null && detachPlatformView, release) }
        if (detachPlatformView) {
            this.platformView?.removeFromParent()
        }
        callbacksOnDisposed.fastForEach { it.invoke() }
        if (release) {
            platformView = null
            parent = null
            callbacksOnDisposed.clear()
            callbacksOnPrepared.clear()
            providedViewLocals?.clear()
        }
        state = Disposed
    }

    override fun registerPrepared(block: () -> Unit) {
        if (state != Initial) {
            error("Cannot schedule callback after prepared.")
        }
        if (block in callbacksOnPrepared) {
            return
        }
        callbacksOnPrepared.add(block)
    }

    override fun pause() {
        if (state != Prepared) {
            error("This ViewNode should be prepared.")
        }
        callbacksOnDisposed.fastForEach { it.invoke() }
        children.fastForEach {
            if (it.state == Prepared) it.pause()
        }
        state = Paused
    }


    override fun registerDisposed(block: () -> Unit) {
        if (state == Disposed) {
            error("Cannot schedule callback after disposed.")
        }
        if (block in callbacksOnDisposed) {
            return
        }
        callbacksOnDisposed.add(block)
    }

    // -------- ViewLocal --------

    private var providedViewLocals: MutableMap<ViewLocal<*>, State<*>>? = null

    override fun <T> getViewLocal(viewLocal: ViewLocal<T>): State<T>? {
        val current = providedViewLocals?.get(viewLocal)
            ?: parent?.getViewLocal(viewLocal)
            ?: return null
        @Suppress("UNCHECKED_CAST")
        return current as? State<T>
    }

    override fun provideViewLocal(providedViewLocal: ProvidedViewLocal<*>) {
        if (providedViewLocals == null) providedViewLocals = hashMapOf()
        providedViewLocals!![providedViewLocal.viewLocal] = providedViewLocal.state
    }
}
