package io.github.hooksw.konify.ui

import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.runtime.node.Node
import io.github.hooksw.konify.runtime.signal.bind
import io.github.hooksw.konify.ui.platform.CurrentViewNode

abstract class ViewNode<T> : Node(), UIElementHolder<T> {

    object Release : Event()


    // -------- Hierarchy --------
    private lateinit var parentViewNode: ViewNode<T>

    private val childViewNodes: MutableList<ViewNode<T>> = mutableListOf()

    override fun onCreate(parent: Node) {
        super.onCreate(parent)
        parentViewNode = CurrentViewNode
        parentViewNode.element.addChild(element)
    }

    override fun onCleanup() {
        childViewNodes.clear()
//        parentViewNode = null
        super.onCleanup()
    }

    override fun onDispatchEvent(event: Event): Boolean {
        when (event) {
            Release -> {
                parentViewNode.element.remove(element)
                cleanUp()
                return true
            }
        }
        return super.onDispatchEvent(event)
    }


    protected abstract fun T.addChild(child: T)
    protected abstract fun T.remove(child: T)
    protected abstract fun T.insert(index: Int, child: T)

    fun insertNode(index: Int, node: ViewNode<T>) {
        childViewNodes.add(index, node)
        element.insert(index, node.element)
    }


}

internal inline fun <T> createViewNodeInternal(viewNodeFactory: () -> ViewNode<T>, block: () -> Unit) {
    val parent = CurrentViewNode!!
    val node = viewNodeFactory()
    CurrentViewNode = node
    node.create(parent)
    block()
    node.prepare()
    CurrentViewNode = parent
}
interface ViewNodeCreateScope{
    fun set(call:()->Unit)
}
internal class ViewNodeCreateScopeImpl:ViewNodeCreateScope{
    override fun set(call:()->Unit){
        bind(call)
    }
}
