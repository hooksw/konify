package io.github.hooksw.konify.ui

import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.ui.diff.DiffOperation
import io.github.hooksw.konify.ui.diff.DiffUtils

object SyncViewNodeDiff {
    private var parentNativeView: NativeView? = null
    private val diffPreChildren = mutableListOf<UIElementHolder<T>>()
    private val diffNewChildren = mutableListOf<UIElementHolder<T>>()

    private var newChildren: List<NativeView>? = null
    private var preChildren: List<NativeView>? = null
    private val diff = DiffUtils(object : DiffOperation {
        override fun insert(toBeInsertedIndex: Int, newListItemIndex: Int) {
            parentNativeView!!.insertView(newChildren!![newListItemIndex], toBeInsertedIndex)
        }

        override fun remove(index: Int) {
            parentNativeView!!.removeView(preChildren!![index])
        }

        override fun move(from: Int, to: Int) {
            val tobeRemoved = preChildren!![from]
            parentNativeView!!.removeView(preChildren!![from])
            parentNativeView!!.insertView(tobeRemoved, to)
        }

    })

    fun perform(
        nativeView: NativeView,
        newChildren: List<NativeView>,
        preChildren: List<NativeView>
    ) {
        SyncViewNodeDiff.newChildren = newChildren
        SyncViewNodeDiff.preChildren = preChildren
        parentNativeView = nativeView
        diff.perform(preChildren, newChildren)
        SyncViewNodeDiff.newChildren = null
        SyncViewNodeDiff.preChildren = null
        parentNativeView = null
        createNode { }
    }
}