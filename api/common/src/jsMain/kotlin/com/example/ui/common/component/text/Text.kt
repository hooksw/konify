package com.example.ui.common.component.text

import com.example.ui.runtime.annotation.View
import com.example.ui.runtime.currentViewNode
import com.example.ui.runtime.platform.PlatformView
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
