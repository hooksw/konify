package io.github.hooksw.konify.runtime.platform

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import io.github.hooksw.konify.runtime.node.InternalViewNode
import io.github.hooksw.konify.runtime.node.ViewLocalProvider
import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.node.provides

fun Activity.setContent(children:  ViewNode.() -> Unit) {
    val root = InternalViewNode()
    val frameLayout = FrameLayout(this).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        tag = root
    }
    val platformView = PlatformView(frameLayout)
    root.registerPlatformView(platformView)
    root.ViewLocalProvider(LocalContext provides this){
        children()
    }
    root.prepare()
    setContentView(frameLayout)
}
