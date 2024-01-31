package io.github.hooksw.konify.ui.component.text

import android.widget.TextView
import io.github.hooksw.konify.runtime.context.useContext
import io.github.hooksw.konify.ui.createViewNode
import io.github.hooksw.konify.ui.platform.LocalContext

actual fun Text(
    text: () -> String
) {
    val context by useContext(LocalContext)
    val view = TextView(context)
    createViewNode(view) {
        set {
            it.text=text()
        }
    }
}

