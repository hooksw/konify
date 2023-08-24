package io.github.hooksw.konify.common.component.text

import android.content.Context
import android.widget.TextView
import io.github.hooksw.konify.runtime.annotation.View
import io.github.hooksw.konify.runtime.currentViewNode
import io.github.hooksw.konify.runtime.platform.LocalContext
import io.github.hooksw.konify.runtime.platform.PlatformView

@View
actual fun Text(
    text: String
) {
    // Create child node.
    val node = currentViewNode.createChild()
    // Run custom block.
    val context = LocalContext.current.value
    val textView = createTextView(
        context,
        text = text
    )
    node.registerPlatformView(textView)
    // Prepare.
    node.prepare()
}

// -------- Internal --------

private fun createTextView(context: Context, text: String): PlatformView {
    val view = TextView(context).apply {
        setText(text)
    }
    return PlatformView(view)
}
