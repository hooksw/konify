package com.example.ui.common.component.text

@View
actual fun Text(text: String) {
    // Create child node.
    val node = currentViewNode.createChildNode()
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
