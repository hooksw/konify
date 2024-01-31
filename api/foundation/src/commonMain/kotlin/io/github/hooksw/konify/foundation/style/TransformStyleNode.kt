package io.github.hooksw.konify.foundation.style

import io.github.hooksw.konify.foundation.ImplFactory
import io.github.hooksw.konify.foundation.unit.Dp
import io.github.hooksw.konify.foundation.unit.Unspecified

interface TransformStyleNode : StyleNode<TransformStyleAttr>

class TransformStyleAttr(
    val rotation: Float = Float.Unspecified,
    val scaleX: Float = Float.Unspecified,
    val scaleY: Float = Float.Unspecified,
    val translateX: Dp = Dp.Unspecified,
    val translateY: Dp = Dp.Unspecified,
)

interface TransformAttrHandler : AttrHandler<TransformStyleAttr>

val TransformStyleNodeImpl = ImplFactory<TransformStyleNode>()