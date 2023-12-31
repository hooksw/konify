package io.github.hooksw.konify.ui.platform

import android.app.Activity
import android.view.ViewGroup
import android.widget.LinearLayout
import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.local.ViewLocalProvider
import io.github.hooksw.konify.runtime.local.provides

fun Activity.setContent(children:  () -> Unit) {
    val root =
    val frameLayout = LinearLayout(this).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        orientation=LinearLayout.VERTICAL
        tag = root
    }
    val platformView = PlatformView(frameLayout)
    root.registerPlatformView(platformView)
    root.ViewLocalProvider(LocalContext provides this){
        children()
    }
    setContentView(frameLayout)
    root.prepare()
}
