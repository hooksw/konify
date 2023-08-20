package com.example.ui.runtime.platform

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.ui.runtime.annotation.ReadOnlyView
import com.example.ui.runtime.injected
import com.example.ui.runtime.node.InternalViewNode

fun Activity.setContent(children: @ReadOnlyView () -> Unit) {
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
