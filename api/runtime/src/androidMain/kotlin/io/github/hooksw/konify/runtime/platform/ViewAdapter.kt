package io.github.hooksw.konify.runtime.platform

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import io.github.hooksw.konify.runtime.annotation.View
import io.github.hooksw.konify.runtime.injected
import io.github.hooksw.konify.runtime.node.InternalViewNode

fun Activity.setContent(children: @View () -> Unit) {
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
    injected(root, children)
    setContentView(frameLayout)
}
