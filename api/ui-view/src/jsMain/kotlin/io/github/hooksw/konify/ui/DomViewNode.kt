package io.github.hooksw.konify.ui

import org.w3c.dom.Element
import org.w3c.dom.get

class DomViewNode(override val element: Element) : ViewNode<Element>() {
    override fun Element.addChild(child: Element) {
        this.appendChild(child)
    }

    override fun Element.remove(child: Element) {
        this.removeChild(child)
    }

    override fun Element.insert(index: Int, child: Element) {
        this.insertBefore(child, this.children[index])
    }

}

fun <T : Element> createViewNode(element: T, block: ViewNodeCreateScope.(T) -> Unit) {
    createViewNodeInternal({ DomViewNode(element) }) {
        ViewNodeCreateScopeImpl().block(element)
    }
}