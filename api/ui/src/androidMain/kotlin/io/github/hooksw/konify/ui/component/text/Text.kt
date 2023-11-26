package io.github.hooksw.konify.ui.component.text

import android.content.Context
import android.widget.TextView
import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.node.component
import io.github.hooksw.konify.runtime.local.getCurrent
import io.github.hooksw.konify.ui.platform.LocalContext
import io.github.hooksw.konify.runtime.platform.PlatformView

actual fun ViewNode.Text(
    text: String
)=component {
    val context = getCurrent(LocalContext).value
    val textView = createTextView(
        context,
        text = text
    )
    registerPlatformView(textView)
}

// -------- Internal --------

private fun createTextView(context: Context, text: String): PlatformView {
    val view = TextView(context).apply {
        setText(text)
    }
    return PlatformView(view)
}
