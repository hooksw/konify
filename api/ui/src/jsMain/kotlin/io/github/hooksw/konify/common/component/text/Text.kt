package io.github.hooksw.konify.common.component.text

import io.github.hooksw.konify.runtime.annotation.View
import io.github.hooksw.konify.runtime.currentViewNode
import io.github.hooksw.konify.runtime.platform.PlatformView
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.p

@View
actual fun Text(text: String) {
    // Create child node.
    val node = currentViewNode.createChild()
    // Run custom block.
    val textView = createTextView(
        text = text
    )
    node.registerPlatformView(textView)
    // Prepare.
    node.prepare()
}

// -------- Internal --------

private fun createTextView(text: String): PlatformView {
    val element = document.create.p { text(text) }
    return PlatformView(element)
}
