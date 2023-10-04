package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.unit.Dp
import io.github.hooksw.konify.runtime.unit.dp

data class PaddingAttr(
    var paddingTop: Dp = 0.dp,
    var paddingBottom: Dp = 0.dp,
    var paddingLeft: Dp = 0.dp,
    var paddingRight: Dp = 0.dp,
)

expect class PaddingStyleNode() : StyleNode<PaddingAttr>

var Style.paddingTop: Dp
    get() = paddingStyleNode.attr.paddingTop
    set(value) {
        paddingStyleNode.attr.paddingTop = value
        if (!ready) return
        paddingStyleNode.update(density!!, platformView!!)
    }

var Style.paddingRight: Dp
    get() = paddingStyleNode.attr.paddingRight
    set(value) {
        paddingStyleNode.attr.paddingRight = value
        if (!ready) return
        paddingStyleNode.update(density!!, platformView!!)
    }

var Style.paddingBottom: Dp
    get() = paddingStyleNode.attr.paddingBottom
    set(value) {
        paddingStyleNode.attr.paddingBottom = value
        if (!ready) return
        paddingStyleNode.update(density!!, platformView!!)
    }

var Style.paddingLeft: Dp
    get() = paddingStyleNode.attr.paddingLeft
    set(value) {
        paddingStyleNode.attr.paddingLeft = value
        if (!ready) return
        paddingStyleNode.update(density!!, platformView!!)
    }