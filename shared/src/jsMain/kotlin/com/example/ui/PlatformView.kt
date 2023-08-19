package com.example.ui

import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.p
import kotlinx.html.style
import org.w3c.dom.HTMLElement

actual class PlatformView(private val view: HTMLElement) {
    actual fun addChild(platformView: PlatformView) {
        view.appendChild(platformView.view)
    }

    actual fun removeChild(platformView: PlatformView) {
        view.removeChild(platformView.view)
    }
}

@ViewMarker
actual fun Text(viewNode: ViewNode, text: String) {
    val node = viewNode.newNode()
    node.registerNativeView(com.example.ui.PlatformView(document.create.p { text(text) }))
}

@ViewMarker
actual fun Row(viewNode: ViewNode, children: @ViewMarker (ViewNode) -> Unit) {

    val node = viewNode.newNode()
    node.registerNativeView(com.example.ui.PlatformView(document.create.div {
        style = "display:inline-flex;flex-wrap:no-wrap"
    }))
    children(node)
}