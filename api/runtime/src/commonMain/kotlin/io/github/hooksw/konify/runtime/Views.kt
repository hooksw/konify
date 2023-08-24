package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.annotation.ReadOnlyView
import io.github.hooksw.konify.runtime.annotation.View
import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.platform.PlatformView

@View
fun View(
    factory: () -> PlatformView
) {
    // Create child node.
    val node = currentViewNode.createChild()
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
