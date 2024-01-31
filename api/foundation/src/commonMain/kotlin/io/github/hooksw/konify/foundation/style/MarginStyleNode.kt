package io.github.hooksw.konify.foundation.style

import io.github.hooksw.konify.foundation.ImplFactory
import io.github.hooksw.konify.foundation.unit.Dp

interface MarginStyleNode : StyleNode<MarginStyleAttr>

data class MarginStyleAttr(
    var top: Dp = Dp.Unspecified,
    var right: Dp = Dp.Unspecified,
    var bottom: Dp = Dp.Unspecified,
    var left: Dp = Dp.Unspecified,
)
interface MarginAttrHandler : AttrHandler<MarginStyleAttr>

val MarginStyleNodeImpl = ImplFactory<MarginStyleNode>()