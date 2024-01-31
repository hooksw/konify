package io.github.hooksw.konify.ui.component.text

import io.github.hooksw.konify.ui.createViewNode
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.p

actual fun Text(text: () -> String) {
    // Run custom block.
    val textView = document.create.p { }
    createViewNode(textView) {
        set {
            it.textContent = text()
        }
    }
}

// -------- Internal --------

