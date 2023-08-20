package com.example.ui.runtime.node

import com.example.ui.core.foundation.state.State

class OldViewNode {
    private var parent: OldViewNode? = null
    private var nativeView: PlatformView? = null
    private var children: MutableList<OldViewNode>? = null
    private val localMap = hashMapOf<ViewLocal<*>, State<*>>()

    fun removeChild(viewNode: OldViewNode) {
        viewNode.release()
        children!!.remove(viewNode)
    }

    fun removeAllChildren() {
        //用于switch判断
        children!!.forEach {
            it.release()
        }
        children!!.clear()
    }

    private fun release() {
        localMap.clear()
        parent = null
        nativeView?.let { findParentNativeView()?.removeChild(it) }
    }

    private fun findParentNativeView(): PlatformView? {
        return when (parent) {
            null -> parent?.findParentNativeView()
            else -> parent?.nativeView
        }
    }
}
