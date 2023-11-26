package io.github.hooksw.konify.ui.platform

import io.github.hooksw.konify.runtime.diff.DiffOperation
import io.github.hooksw.konify.runtime.diff.DiffUtils
import io.github.hooksw.konify.runtime.node.Node
import io.github.hooksw.konify.runtime.signal.ViewNodes
import io.github.hooksw.konify.runtime.utils.fastForEach
import kotlin.jvm.JvmField

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
internal class ViewNode : Node() {
    // -------- Hierarchy --------
    @JvmField
    var parentViewNode: ViewNode? = null

    @JvmField
    val childViewNodes: MutableList<ViewNode> = mutableListOf()
    override fun onCreate(parent: Node) {
        super.onCreate(parent)
        parentViewNode = CurrentViewNode
    }

    override fun cleanup() {
        childViewNodes.clear()
        parentViewNode = null
        super.cleanup()
    }

    override fun cache() {
        super.cache()
    }

    override fun reuse() {
        super.reuse()
    }

    //diff

    private val diffPreChildren by lazy(LazyThreadSafetyMode.NONE) { mutableListOf<PlatformView>() }
    private val diffNewChildren by lazy(LazyThreadSafetyMode.NONE) { mutableListOf<PlatformView>() }

    fun diffAndPatch() {
        parentViewNode!!.platformView.foreachChild {
            diffPreChildren.add(it)
        }
        parentViewNode!!.childViewNodes.fastForEach {
            diffNewChildren.add(it.platformView)
        }
        SyncViewNodeDiff.perform(parentViewNode!!.platformView, diffPreChildren, diffNewChildren)
        diffPreChildren.clear()
        diffNewChildren.clear()
    }


    // -------- Platform --------

    lateinit var platformView: PlatformView


    fun registerPlatformView(platformView: PlatformView) {
        this.platformView = platformView
        parentPlatformView().addView(platformView)
    }

    private fun parentPlatformView() = parentViewNode!!.platformView

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
        SyncViewNodeDiff.newChildren = newChildren
        SyncViewNodeDiff.preChildren = preChildren
        parentPlatformView = platformView
        diff.perform(preChildren, newChildren)
        SyncViewNodeDiff.newChildren = null
        SyncViewNodeDiff.preChildren = null
        parentPlatformView = null
    }
}
