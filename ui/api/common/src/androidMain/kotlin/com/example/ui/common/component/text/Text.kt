package com.example.ui.common.component.text

import android.widget.TextView
import com.example.ui.runtime.annotation.ReadOnlyView
import com.example.ui.runtime.annotation.View
import com.example.ui.runtime.currentViewNode
import com.example.ui.runtime.platform.LocalContext
import com.example.ui.runtime.platform.PlatformView

@View
actual fun Text(
    text: String
) {
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

@ReadOnlyView
private fun createTextView(text: String): PlatformView {
    val context = LocalContext.current.value
    val view = TextView(context).apply {
        setText(text)
    }
    return PlatformView(view)
}
