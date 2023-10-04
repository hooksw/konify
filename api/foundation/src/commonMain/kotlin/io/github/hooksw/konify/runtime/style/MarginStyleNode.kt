package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.unit.Dp
import io.github.hooksw.konify.runtime.unit.dp

data class MarginStyleAttr(
    var marginTop: Dp = 0.dp,
    var marginBottom: Dp = 0.dp,
    var marginLeft: Dp = 0.dp,
    var marginRight: Dp = 0.dp,
)

expect class MarginStyleNode : StyleNode<MarginStyleAttr>

