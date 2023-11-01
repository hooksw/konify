package io.github.hooksw.konify.runtime.node

import androidx.collection.MutableScatterMap
import androidx.collection.MutableScatterSet
import io.github.hooksw.konify.runtime.diff.DiffOperation
import io.github.hooksw.konify.runtime.diff.DiffUtils
import io.github.hooksw.konify.runtime.local.ProvidedViewLocal
import io.github.hooksw.konify.runtime.local.ViewLocal
import io.github.hooksw.konify.runtime.node.InternalViewNode.LifecycleState.*
import io.github.hooksw.konify.runtime.platform.PlatformView
import io.github.hooksw.konify.runtime.signal.*
import io.github.hooksw.konify.runtime.signal.Owner
import io.github.hooksw.konify.runtime.signal.Owners
import io.github.hooksw.konify.runtime.signal.StateObserver
import io.github.hooksw.konify.runtime.signal.removeLastSelf
import io.github.hooksw.konify.runtime.utils.UnitCallBack
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
internal class InternalViewNode private constructor(): ViewNode, Owner {
    // -------- Hierarchy --------

    private var parent: InternalViewNode? = null

    @JvmField
    internal val children: MutableList<InternalViewNode> = ArrayList(4)
    companion object{
        @JvmStatic
        fun createRoot(): InternalViewNode {
            return InternalViewNode()
        }
    }

    override fun createChild(): InternalViewNode {
        val child = InternalViewNode()
        children.add(child)
        child.parent = this
        Owners.add(child)
        return child
    }

    override fun insertNode(node: ViewNode, index: Int) {
        require(index >= 0) {
            "index less than 0"
        }
        children.add(index, node as InternalViewNode)
        node.parent = this
        node.prepareRecursion()
        preparePatch()
    }

    private var patching = false

    private fun preparePatch() {
        if (!patching) {
            patching = true
            diffAndPatch()
            patching = false
        }
    }

    private fun diffAndPatch() {
        val preChildren = parentViewNode.platformView!!.children()
        val newChildren = parentViewNode.getCurrentSubViews()
        SyncViewNodeDiff.perform(parentViewNode.platformView!!, newChildren, preChildren)
    }


    private fun getCurrentSubViews(): List<PlatformView> = buildList {
        children.fastForEach {
            buildDirectViewList(this)
        }
    }

    private fun buildDirectViewList(list: MutableList<PlatformView>) {
        if (platformView != null) {
            list.add(platformView!!)
        } else {
            children.fastForEach { buildDirectViewList(list) }
        }
    }

    private fun prepareRecursion() {
        children.fastForEach {
            it.prepareRecursion()
        }
        prepare()
    }

    override fun detachNodeAt(index: Int): InternalViewNode {
        val node = children.removeAt(index)
        node.detach()
        return node
    }


    override fun release() {
        disposeRecursion(parentViewNode.platformView!!, release = true)
        children.clear()
    }

    override fun detach() {
        parent!!.children.remove(this)
        disposeRecursion(parentViewNode.platformView!!, release = false)
    }


    // -------- Platform --------

    @JvmField
    internal var platformView: PlatformView? = null
    private lateinit var parentViewNode: InternalViewNode

    val isAttachPlatformView
        get() = platformView != null


    fun registerPlatformView(platformView: PlatformView) {
        this.platformView = platformView
        findParentPlatformView().addView(platformView)
    }

    private fun findParentPlatformView(): PlatformView {
        val parent = parent ?: error("node should be added first.")
        return parent.platformView ?: parentViewNode.platformView!!
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
        if (state == Prepared) {
            error("This ViewNode is already prepared.")
        }
        parentViewNode = if (parent!!.platformView != null) parent!! else parent!!.parentViewNode
        callbacksOnPrepared.fastForEach { it.invoke() }
//        callbacksOnPrepared.clear()
        removeLastSelf()
        state = Prepared
    }


    private fun disposeRecursion(topPlatformView: PlatformView, release: Boolean) {
        if (state == Disposed) {
            error("This ViewNode is already disposed.")
        }
        children.fastForEach { it.disposeRecursion(topPlatformView, release) }
        if (topPlatformView == parentViewNode.platformView && platformView != null) {
            topPlatformView.removeView(platformView!!)
        }
        callbacksOnDisposed.fastForEach { it() }
        disposeObservers()
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

    private var providedViewLocals: MutableMap<ViewLocal<*>, Signal<*>>? = null

    override fun <T> getViewLocal(viewLocal: ViewLocal<T>): Signal<T>? {
        val current = providedViewLocals?.get(viewLocal)
            ?: parent?.getViewLocal(viewLocal)
            ?: return null
        @Suppress("UNCHECKED_CAST")
        return current as? Signal<T>
    }

    override fun provideViewLocal(providedViewLocal: ProvidedViewLocal<*>) {
        if (providedViewLocals == null) providedViewLocals = hashMapOf()
        providedViewLocals!![providedViewLocal.viewLocal] = providedViewLocal.signal
    }


    override val stateDisposerMap: MutableScatterMap<StateObserver, UnitCallBack>
        = MutableScatterMap()
}

object SyncViewNodeDiff {
    private var parentPlatformView: PlatformView? = null
    private var newChildren: List<PlatformView>? = null
    private var preChildren: List<PlatformView>? = null
    private val diff = DiffUtils(object : DiffOperation {
        override fun insert(toOldIndex: Int, fromNewIndex: Int) {
            parentPlatformView!!.insertView(newChildren!![fromNewIndex], toOldIndex)
        }

        override fun remove(index: Int) {
            parentPlatformView!!.removeView(preChildren!![index])
        }

        override fun move(from: Int, to: Int) {
            val tobeRemoved = preChildren!![from]
            parentPlatformView!!.removeView(preChildren!![from])
            parentPlatformView!!.insertView(tobeRemoved, to)
        }

    })

    fun perform(
        platformView: PlatformView,
        newChildren: List<PlatformView>,
        preChildren: List<PlatformView>
    ) {
        this.newChildren = newChildren
        this.preChildren = preChildren
        this.parentPlatformView = platformView
        diff.perform(preChildren, newChildren)
        this.newChildren = null
        this.preChildren = null
        this.parentPlatformView = null
    }
}
