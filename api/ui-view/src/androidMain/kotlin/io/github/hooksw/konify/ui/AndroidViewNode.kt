package io.github.hooksw.konify.ui

import android.view.View
import android.view.ViewGroup

class AndroidViewNode(override val element: View) : ViewNode<View>() {
    override fun View.addChild(child: View) {
        (this as ViewGroup).addView(child)
    }

    override fun View.remove(child: View) {
        (this as ViewGroup).remove(child)
    }

    override fun View.insert(index: Int, child: View) {
        (this as ViewGroup).addView(child, index)
    }
}

fun <T : View> createViewNode(element: T, block: ViewNodeCreateScope.(T) -> Unit) {
    createViewNodeInternal({ AndroidViewNode(element) }) {
        ViewNodeCreateScopeImpl().block(element)
    }
}