package io.github.hooksw.konify.common.component.text

import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.node.component
import io.github.hooksw.konify.runtime.platform.PlatformView
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.p

actual fun ViewNode.Text(text: String)=component {
    // Run custom block.
    val textView = createTextView(
        text = text
    )
    registerPlatformView(textView)
}

// -------- Internal --------

private fun createTextView(text: String): PlatformView {
    val element = document.create.p { text(text) }
    return PlatformView(element)
}
