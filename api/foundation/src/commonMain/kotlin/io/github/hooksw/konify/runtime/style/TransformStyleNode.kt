package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.unit.Dp
import io.github.hooksw.konify.runtime.unit.dp

data class TransformStyleAttr(
    var rotation: Float = 0f,
    var rotationX: Float = 0f,
    var rotationY: Float = 0f,

    var scaleX: Float = 0f,
    var scaleY: Float = 0f,

    var translateX: Dp = 0.dp,
    var translateY: Dp = 0.dp,
    var translateZ: Dp = 0.dp,
)

expect class TransformStyleNode() : StyleNode<TransformStyleAttr>