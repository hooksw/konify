package com.example.ui.runtime

import com.example.ui.runtime.annotation.ReadOnlyView
import com.example.ui.runtime.annotation.View
import com.example.ui.runtime.node.ViewNode
import com.example.ui.runtime.platform.PlatformView

@View
fun View(
    factory: () -> PlatformView
) {
    // Create child node.
    val node = currentViewNode.createChildNode()
    // Run factory.
    val platformView = factory()
    node.registerPlatformView(platformView)
    // Prepare.
    node.prepare()
}

val currentViewNode: ViewNode
    @ReadOnlyView
    get() = throw NotImplementedError("Implemented as intrinsic.")

inline fun injected(
    node: ViewNode,
    block: @ReadOnlyView () -> Unit
) {
    throw NotImplementedError("Implemented as intrinsic.")
}
